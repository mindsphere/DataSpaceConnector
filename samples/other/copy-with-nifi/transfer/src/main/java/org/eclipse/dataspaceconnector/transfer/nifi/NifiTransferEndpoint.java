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

package org.eclipse.dataspaceconnector.transfer.nifi;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.eclipse.dataspaceconnector.spi.types.domain.Polymorphic;

import java.util.HashMap;
import java.util.Map;

@JsonTypeName("dataspaceconnector:nifitransferendpoint")
@JsonDeserialize(builder = NifiTransferEndpoint.NifiTransferEndpointBuilder.class)
public class NifiTransferEndpoint implements Polymorphic {
    private final String type;
    private Map<String, String> properties;

    protected NifiTransferEndpoint(@JsonProperty("type") String type) {
        this.type = type;
        properties = new HashMap<>();
    }

    public String getType() {
        return type;
    }

    @JsonAnyGetter
    public Map<String, String> getProperties() {
        return properties;
    }


    @JsonPOJOBuilder(withPrefix = "")
    public static class NifiTransferEndpointBuilder {
        private final Map<String, String> properties;
        private String type;

        private NifiTransferEndpointBuilder() {
            properties = new HashMap<>();
        }

        @JsonCreator
        public static NifiTransferEndpointBuilder newInstance() {
            return new NifiTransferEndpointBuilder();
        }


        public NifiTransferEndpointBuilder type(String type) {
            this.type = type;
            return this;
        }

        @JsonAnySetter
        public NifiTransferEndpointBuilder properties(Map<String, String> additionalProperties) {
            properties.putAll(additionalProperties);
            return this;
        }

        @JsonAnySetter
        public NifiTransferEndpointBuilder property(String key, String value) {
            properties.put(key, value);
            return this;
        }

        public NifiTransferEndpoint build() {
            NifiTransferEndpoint nifiTransferEndpoint = new NifiTransferEndpoint(type);
            nifiTransferEndpoint.properties = properties;
            return nifiTransferEndpoint;
        }
    }
}
