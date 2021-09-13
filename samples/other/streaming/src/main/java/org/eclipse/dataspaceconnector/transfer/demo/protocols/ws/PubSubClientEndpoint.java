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

package org.eclipse.dataspaceconnector.transfer.demo.protocols.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.transfer.demo.protocols.spi.stream.message.DataMessage;
import org.eclipse.dataspaceconnector.transfer.demo.protocols.spi.stream.message.PubSubMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

/**
 * Implements a web socket client endpoint that receives data published to a topic that the current runtime is subscribed to.
 */
@ClientEndpoint
public class PubSubClientEndpoint {
    private final ObjectMapper objectMapper;
    private final Monitor monitor;
    private final PubSubConsumer consumer;

    public PubSubClientEndpoint(ObjectMapper objectMapper, Monitor monitor, PubSubConsumer consumer) {
        this.objectMapper = objectMapper;
        this.monitor = monitor;
        this.consumer = consumer;
    }

    @OnMessage
    public void message(Session session, byte[] payload) throws IOException {
        PubSubMessage message = objectMapper.readValue(payload, PubSubMessage.class);
        if (message.getProtocol() == PubSubMessage.Protocol.DATA) {
            consumer.accept((DataMessage) message);
        } else {
            monitor.severe("Unexpected message type: " + message.getProtocol());
        }
    }

    @OnClose
    public void close(CloseReason reason) {
        consumer.closed();
    }

    @OnError
    public void error(Throwable e) {
        if (e instanceof ClosedChannelException) {
            // ignore
            return;
        }
        monitor.severe("Websocket error", e);
    }

}
