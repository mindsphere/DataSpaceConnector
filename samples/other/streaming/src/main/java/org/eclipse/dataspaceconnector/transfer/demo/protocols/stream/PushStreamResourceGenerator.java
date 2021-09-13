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

package org.eclipse.dataspaceconnector.transfer.demo.protocols.stream;

import org.eclipse.dataspaceconnector.spi.transfer.provision.ResourceDefinitionGenerator;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;
import org.eclipse.dataspaceconnector.transfer.demo.protocols.spi.DemoProtocols;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Generates resource definitions for push stream transfers. If the data destination is unmanaged (i.e. it is already created and managed independently) a definition will not be
 * generated. Otherwise, a definition containing metadata to create a destination topic will be returned.
 */
public class PushStreamResourceGenerator implements ResourceDefinitionGenerator {
    private final String wsEndpointAddress;
    private final String httpEndpointAddress;

    public PushStreamResourceGenerator(String wsEndpointAddress, String httpEndpointAddress) {
        this.wsEndpointAddress = wsEndpointAddress;
        this.httpEndpointAddress = httpEndpointAddress;
    }

    @Override
    public @Nullable PushStreamResourceDefinition generate(TransferProcess process) {
        var dataRequest = process.getDataRequest();
        if (!dataRequest.isManagedResources()) {
            // The resource is unmanaged, which means it was created by an external system. In this case it does not need to be provisioned.
            return null;
        }

        String endpointAddress;
        if (DemoProtocols.PUSH_STREAM_WS.equals(dataRequest.getDestinationType())) {
            endpointAddress = wsEndpointAddress;
        } else if (DemoProtocols.PUSH_STREAM_HTTP.equals(dataRequest.getDestinationType())) {
            endpointAddress = httpEndpointAddress;
        } else {
            return null;
        }
        var destinationName = dataRequest.getDataDestination().getProperty(DemoProtocols.DESTINATION_NAME);
        if (destinationName == null) {
            destinationName = UUID.randomUUID().toString();
        }
        return PushStreamResourceDefinition.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .topicName(destinationName)
                .endpointAddress(endpointAddress)
                .build();
    }
}
