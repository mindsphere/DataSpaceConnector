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

package org.eclipse.dataspaceconnector.transfer.demo.protocols.http;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.transfer.demo.protocols.spi.stream.TopicManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * A JAX-RS endpoint that accepts data to be published to a topic.
 */
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Path("/demo/pubsub")
public class PubSubHttpEndpoint {
    private final TopicManager topicManager;

    private final AtomicBoolean active = new AtomicBoolean();

    private final ExecutorService executorService;
    private final LinkedBlockingQueue<QueueEntry> queue = new LinkedBlockingQueue<>();

    public PubSubHttpEndpoint(TopicManager topicManager) {
        this.topicManager = topicManager;
        executorService = Executors.newSingleThreadExecutor();
    }

    public void start() {
        active.set(true);
        executorService.submit(this::run);
    }

    public void stop() {
        active.set(false);
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    @POST
    @Path("{destinationName}")
    public Response publish(@PathParam("destinationName") String topicName, @HeaderParam("X-Authorization") String token, byte[] data) {
        var result = topicManager.connect(topicName, token);
        if (result.success()) {
            var entry = new QueueEntry(result.getConsumer(), data);
            try {
                queue.put(entry);
            } catch (InterruptedException e) {
                Thread.interrupted();
                throw new EdcException(e);
            }
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    private void run() {
        while (active.get()) {
            var entry = queue.poll();
            if (entry != null) {
                entry.connection.accept(entry.data);
            }
        }
    }

    private static class QueueEntry {
        Consumer<byte[]> connection;
        byte[] data;

        public QueueEntry(Consumer<byte[]> connection, byte[] data) {
            this.connection = connection;
            this.data = data;
        }
    }

}
