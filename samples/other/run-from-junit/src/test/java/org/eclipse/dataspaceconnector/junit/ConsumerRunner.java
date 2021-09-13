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

package org.eclipse.dataspaceconnector.junit;

import org.easymock.EasyMock;
import org.eclipse.dataspaceconnector.junit.launcher.EdcExtension;
import org.eclipse.dataspaceconnector.schema.s3.S3BucketSchema;
import org.eclipse.dataspaceconnector.spi.iam.IdentityService;
import org.eclipse.dataspaceconnector.spi.iam.TokenResult;
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.transfer.TransferInitiateResponse;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessListener;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessObservable;
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.QueryRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.common.types.Cast.cast;

@ExtendWith(EdcExtension.class)
@Disabled
public class ConsumerRunner {
    private static final String PROVIDER_CONNECTOR = "http://localhost:8181/";
    private static final TokenResult US_TOKEN = TokenResult.Builder.newInstance().token("mock-us").build();
    private static final TokenResult EU_TOKEN = TokenResult.Builder.newInstance().token("mock-eu").build();
    private static final DataEntry EU_ARTIFACT = DataEntry.Builder.newInstance().id("test123").build();
    private static final DataEntry US_OR_EU_ARTIFACT = DataEntry.Builder.newInstance().id("test456").build();

    private CountDownLatch latch;

    @Test
    @Disabled
    void processConsumerRequest_toAws(RemoteMessageDispatcherRegistry dispatcherRegistry, TransferProcessManager processManager, TransferProcessObservable observable, TransferProcessStore store) throws Exception {

        var query = QueryRequest.Builder.newInstance()
                .connectorAddress(PROVIDER_CONNECTOR)
                .connectorId(PROVIDER_CONNECTOR)
                .queryLanguage("dataspaceconnector")
                .query("select *")
                .protocol("ids-rest").build();

        CompletableFuture<List<String>> future = cast(dispatcherRegistry.send(List.class, query, () -> null));

        var artifacts = future.get();
        artifacts = artifacts.stream().findAny().stream().collect(Collectors.toList());
        latch = new CountDownLatch(artifacts.size());
        for (String artifact : artifacts) {
            System.out.println("processing artifact " + artifact);
            // Initiate a request as a U.S.-based connector for an EU or US allowed artifact (will be accepted)
            var usOrEuRequest = createRequestAws("us-eu-request-" + UUID.randomUUID(), DataEntry.Builder.newInstance().id(artifact).build());

            TransferInitiateResponse response = processManager.initiateConsumerRequest(usOrEuRequest);
            observable.registerListener(new TransferProcessListener() {
                @Override
                public void completed(TransferProcess process) {
                    if (process.getId().equals(response.getId())) {
                        return;
                    }
                    //simulate data egress
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    process.transitionDeprovisionRequested();
                    store.update(process);
                }

                @Override
                public void deprovisioned(TransferProcess process) {
                    if (process.getId().equals(response.getId())) {
                        return;
                    }
                    latch.countDown();
                }
            });
        }

        // Initiate a request as a U.S.-based connector for an EU-restricted artifact (will be denied)
//        var usRequest = createRequestAws("us-request", EU_ARTIFACT);
//
//        processManager.initiateConsumerRequest(usRequest);


        assertThat(latch.await(5, TimeUnit.MINUTES)).isTrue();
    }


    @Test
//    @Disabled
    void processConsumerRequest_toAzureStorage(RemoteMessageDispatcherRegistry dispatcherRegistry, TransferProcessManager processManager, TransferProcessObservable observable, TransferProcessStore store) throws Exception {
        var query = QueryRequest.Builder.newInstance()
                .connectorAddress(PROVIDER_CONNECTOR)
                .connectorId(PROVIDER_CONNECTOR)
                .queryLanguage("dataspaceconnector")
                .query("select *")
                .protocol("ids-rest").build();

        CompletableFuture<List<String>> future = cast(dispatcherRegistry.send(List.class, query, () -> null));

        var artifacts = future.get();

        assertThat(artifacts).describedAs("Should have returned artifacts!").isNotEmpty();

        latch = new CountDownLatch(artifacts.size());

        for (String artifact : artifacts) {
            // Initiate a request as a U.S.-based connector for an EU or US allowed artifact (will be accepted)
            var usOrEuRequest = createRequestAzure("us-eu-request-" + UUID.randomUUID(), DataEntry.Builder.newInstance().id(artifact).build());

            TransferInitiateResponse response = processManager.initiateConsumerRequest(usOrEuRequest);
            observable.registerListener(new TransferProcessListener() {
                @Override
                public void completed(TransferProcess process) {
                    if (process.getId().equals(response.getId())) {
                        return;
                    }
                    //simulate data egress
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    process.transitionDeprovisionRequested();
                    store.update(process);
                }

                @Override
                public void deprovisioned(TransferProcess process) {
                    if (process.getId().equals(response.getId())) {
                        return;
                    }
                    latch.countDown();
                }
            });
        }

        // Initiate a request as a U.S.-based connector for an EU-restricted artifact (will be denied)
//        var usRequest = createRequestAzure("us-request", EU_ARTIFACT);
//
//        processManager.initiateConsumerRequest(usRequest);

        assertThat(latch.await(5, TimeUnit.MINUTES)).isTrue();
    }

    @BeforeEach
    void before(EdcExtension extension) {
        IdentityService identityService = EasyMock.createMock(IdentityService.class);
        EasyMock.expect(identityService.obtainClientCredentials(EasyMock.isA(String.class))).andReturn(US_TOKEN).anyTimes();
        EasyMock.replay(identityService);
        latch = new CountDownLatch(1);

        extension.registerSystemExtension(ServiceExtension.class, TestExtensions.mockIamExtension(identityService));
    }

    private DataRequest createRequestAws(String id, DataEntry artifactId) {
        return DataRequest.Builder.newInstance()
                .id(id)
                .protocol("ids-rest")
                .dataEntry(artifactId)
                .connectorId(PROVIDER_CONNECTOR)
                .connectorAddress(PROVIDER_CONNECTOR)
                .destinationType(S3BucketSchema.TYPE).build();
    }

    private DataRequest createRequestAzure(String id, DataEntry artifactId) {
        return DataRequest.Builder.newInstance()
                .id(id)
                .protocol("ids-rest")
                .dataEntry(artifactId)
                .connectorId(PROVIDER_CONNECTOR)
                .connectorAddress(PROVIDER_CONNECTOR)
                .dataDestination(DataAddress.Builder.newInstance()
                        .type("type")
                        .property("account", "edcdemogpstorage")
                        .property("container", "temp-dest-container-" + UUID.randomUUID())
                        .build())
                .destinationType("type")
                .build();
    }
}
