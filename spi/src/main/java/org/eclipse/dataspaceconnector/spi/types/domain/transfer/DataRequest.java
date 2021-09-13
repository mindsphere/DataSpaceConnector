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

package org.eclipse.dataspaceconnector.spi.types.domain.transfer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.eclipse.dataspaceconnector.spi.types.domain.Polymorphic;
import org.eclipse.dataspaceconnector.spi.types.domain.message.RemoteMessage;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataEntry;

/**
 * Polymorphic data request.
 */
@JsonTypeName("dataspaceconnector:datarequest")
@JsonDeserialize(builder = DataRequest.Builder.class)
public class DataRequest implements RemoteMessage, Polymorphic {
    private String id;

    private String processId;

    private String connectorAddress;

    private String protocol;

    private String connectorId;

    private DataEntry dataEntry;

    private DataAddress dataDestination;

    private boolean managedResources = true;

    private TransferType transferType;

    private DataRequest() {
        transferType = new TransferType();
    }

    /**
     * The unique request id. Request ids are provided by the originating consumer and must be unique.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the process id this request is associated with.
     */
    public String getProcessId() {
        return processId;
    }

    void setProcessId(String processId) {
        this.processId = processId;
    }

    /**
     * The protocol-specific address of the provider connector.
     */
    public String getConnectorAddress() {
        return connectorAddress;
    }

    /**
     * The protocol over which the data request is sent to the provider connector.
     */
    @Override
    public String getProtocol() {
        return protocol;
    }

    /**
     * The provider connector id.
     */
    public String getConnectorId() {
        return connectorId;
    }

    /**
     * The requested data.
     */
    public DataEntry getDataEntry() {
        return dataEntry;
    }

    /**
     * The type of destination the requested data should be routed to.
     */
    public String getDestinationType() {
        return dataDestination != null ? dataDestination.getType() : null;
    }

    /**
     * The target address the data is to be sent to. Set by the request originator, e.g., the consumer connector.
     */
    public DataAddress getDataDestination() {
        return dataDestination;
    }

    public boolean isManagedResources() {
        return managedResources;
    }

    public DataRequest copy(String newId) {
        return Builder.newInstance()
                .id(newId)
                .processId(processId)
                .connectorAddress(connectorAddress)
                .protocol(protocol)
                .connectorId(connectorId)
                .dataEntry(dataEntry)    // shallow copy, may need to revisit
                .dataAddress(dataDestination)
                .transferType(transferType)
                .managedResources(managedResources)
                .build();
    }

    public void updateDestination(DataAddress dataAddress) {
        dataDestination = dataAddress;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private final DataRequest request;

        private Builder() {
            request = new DataRequest();
        }

        @JsonCreator
        public static Builder newInstance() {
            return new Builder();
        }

        public Builder id(String id) {
            request.id = id;
            return this;
        }

        public Builder processId(String id) {
            request.processId = id;
            return this;
        }

        public Builder connectorAddress(String address) {
            request.connectorAddress = address;
            return this;
        }

        public Builder protocol(String protocol) {
            request.protocol = protocol;
            return this;
        }

        public Builder connectorId(String connectorId) {
            request.connectorId = connectorId;
            return this;
        }

        public Builder dataEntry(DataEntry entry) {
            request.dataEntry = entry;
            return this;
        }

        public Builder destinationType(String type) {
            if (request.dataDestination == null) {
                request.dataDestination = DataAddress.Builder.newInstance()
                        .type(type).build();
            } else {
                request.dataDestination.setType(type);
            }
            return this;
        }

        public Builder dataDestination(DataAddress destination) {
            request.dataDestination = destination;
            return this;
        }

        public Builder managedResources(boolean value) {
            request.managedResources = value;
            return this;
        }

        public DataRequest build() {
            if (request.dataDestination == null && request.getDestinationType() == null) {
                throw new IllegalArgumentException("A data destination or type must be specified");
            }
            return request;
        }

        private Builder dataAddress(DataAddress dataAddress) {
            request.dataDestination = dataAddress;
            return this;
        }

        private Builder transferType(TransferType transferType) {
            request.transferType = transferType;
            return this;
        }

    }
}
