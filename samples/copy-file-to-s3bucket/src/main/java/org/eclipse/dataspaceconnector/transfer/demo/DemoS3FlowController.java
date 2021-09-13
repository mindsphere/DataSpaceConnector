/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.dataspaceconnector.transfer.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jodah.failsafe.RetryPolicy;
import org.eclipse.dataspaceconnector.provision.aws.AwsTemporarySecretToken;
import org.eclipse.dataspaceconnector.schema.s3.S3BucketSchema;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowController;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowInitiateResponse;
import org.eclipse.dataspaceconnector.spi.transfer.response.ResponseStatus;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.temporal.ChronoUnit;

public class DemoS3FlowController implements DataFlowController {
    private final Vault vault;
    private final Monitor monitor;
    private final RetryPolicy<Object> retryPolicy;

    public DemoS3FlowController(Vault vault, Monitor monitor) {
        this.vault = vault;
        this.monitor = monitor;
        retryPolicy = new RetryPolicy<>()
                .withBackoff(500, 5000, ChronoUnit.MILLIS)
                .withMaxRetries(3);
    }

    @Override
    public boolean canHandle(DataRequest dataRequest) {
        return "dataspaceconnector:s3".equals(dataRequest.getDataDestination().getType());
    }

    @Override
    public @NotNull DataFlowInitiateResponse initiateFlow(DataRequest dataRequest) {

        final String sourceKey = dataRequest.getDataEntry().getCatalogEntry().getAddress()
            .getKeyName();

        final String sourceBucketName = dataRequest.getDataEntry().getCatalogEntry().getAddress()
            .getProperty(S3BucketSchema.BUCKET_NAME);

        var destinationKey = dataRequest.getDataDestination().getKeyName();
        var awsSecret = vault.resolveSecret(destinationKey);
        var destinationBucketName = dataRequest.getDataDestination().getProperty(S3BucketSchema.BUCKET_NAME);

        var region = dataRequest.getDataDestination().getProperty(S3BucketSchema.REGION);
        var dt = convertSecret(awsSecret);

        return copyToBucket(destinationBucketName, region, dt, destinationKey, sourceBucketName, sourceKey);
    }

    @NotNull
    private DataFlowInitiateResponse copyToBucket(
        String destinationBucketName,
        String region,
        AwsTemporarySecretToken dt,
        final String destinationKey,
        final String sourceBucketName,
        final String sourceKey
    ) {
        try (S3Client s3 = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsSessionCredentials.create(dt.getAccessKeyId(), dt.getSecretAccessKey(), dt.getSessionToken())))
                .region(Region.of(region))
                .build()) {

            String etag = null;

            try {
                monitor.debug("Data request: begin transfer...");

                final CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                    .copySource(sourceBucketName + "/" + sourceKey)
                    .destinationBucket(destinationBucketName)
                    .destinationKey(destinationKey)
                    .build();

                var response = s3.copyObject(copyObjectRequest);

                monitor.debug("Data request done.");
                etag = response.copyObjectResult().eTag();
            } catch (S3Exception tmpEx) {
                monitor.info("Data request: transfer not successful");
            }

            return new DataFlowInitiateResponse(ResponseStatus.OK, etag);
        } catch (S3Exception | EdcException ex) {
            monitor.severe("Data request: transfer failed!");
            return new DataFlowInitiateResponse(ResponseStatus.FATAL_ERROR, ex.getLocalizedMessage());
        }
    }

    private AwsTemporarySecretToken convertSecret(String awsSecret) {
        try {
            var mapper = new ObjectMapper();
            return mapper.readValue(awsSecret, AwsTemporarySecretToken.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

