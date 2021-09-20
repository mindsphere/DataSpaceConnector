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

package org.eclipse.dataspaceconnector.demo.ui;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.QueryRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 *
 */
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Path("/demo-ui")
public class DemoUiApiController {
  private static final String PREFIX = "http://";

  private final RemoteMessageDispatcherRegistry dispatcherRegistry;

  private final TransferProcessManager processManager;

  private final Monitor monitor;

  public DemoUiApiController(
      RemoteMessageDispatcherRegistry dispatcherRegistry,
      TransferProcessManager processManager,
      Monitor monitor
  ) {
    this.dispatcherRegistry = dispatcherRegistry;
    this.processManager = processManager;
    this.monitor = monitor;
  }

  @GET
  @Path("artifacts/{connector}")
  public Response getArtifacts(@PathParam("connector") String connector) {
    try {
      var query = QueryRequest.Builder.newInstance()
          .connectorAddress(PREFIX + connector)
          .connectorId(connector)
          .queryLanguage("dataspaceconnector")
          .query("select *")
          .protocol("ids-rest").build();

      @SuppressWarnings({"unchecked", "rawtypes"}) CompletableFuture<List<String>> future
          = (CompletableFuture) dispatcherRegistry.send(List.class, query, () -> null);

      var artifacts = future.get();
      return Response.ok().entity(artifacts).build();
    } catch (InterruptedException | ExecutionException e) {
      monitor.severe("Error serving request", e);
      return Response.serverError().build();
    }
  }

  @POST
  @Path("data/request")
  public Response initiateDataRequest(Map<String, String> request) {
    var connector = (String) request.get("connector");
    var artifact = (String) request.get("artifact");

    var destinationRegion = System.getProperty("destinationRegion");
    var destinationBucket = System.getProperty("destinationBucket");
    var destinationName = extractDestinationArtifactName(artifact);

    var usRequest = createRequest(
        connector,
        UUID.randomUUID().toString(),
        DataEntry.Builder.newInstance().id(artifact).build(),
        destinationBucket,
        destinationName,
        destinationRegion);

    processManager.initiateConsumerRequest(usRequest);
    return Response.ok().build();
  }

  private String extractDestinationArtifactName(String artifactId) {
    return artifactId.substring(artifactId.lastIndexOf('/') + 1);
  }

  private DataRequest createRequest(
      final String connector,
      final String id,
      final DataEntry artifactId,
      final String destinationBucket,
      final String destinationName,
      final String destinationRegion
  ) {

    return DataRequest.Builder.newInstance()
        .id(id)
        .protocol("ids-rest")
        .dataEntry(artifactId)
        .connectorId(connector)
        .connectorAddress(PREFIX + connector)
        .dataDestination(DataAddress.Builder.newInstance()
            .keyName(destinationName)
            .property("bucketName", destinationBucket)
            .property("region", destinationRegion)
            .type("dataspaceconnector:s3")
            .build())
        .build();
  }
}
