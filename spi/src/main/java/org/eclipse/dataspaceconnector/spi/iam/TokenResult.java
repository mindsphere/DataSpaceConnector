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

package org.eclipse.dataspaceconnector.spi.iam;

/**
 * The result of an obtain token operation.
 */
public class TokenResult {
    private boolean success = true;
    private String token;
    private String error;
    private long expiresIn;

    private TokenResult() {
    }

    /**
     * Returns true if the flow was successful.
     */
    public boolean success() {
        return success;
    }

    /**
     * Returns the bearer token if the flow was successful; otherwise null.
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns the token expiration.
     */
    public long expiresIn() {
        return expiresIn;
    }

    /**
     * Returns the error details if the flow was not successful; otherwise null.
     */
    public String error() {
        return error;
    }

    public static class Builder {
        private final TokenResult result;

        private Builder() {
            result = new TokenResult();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder error(String error) {
            result.success = false;
            result.error = error;
            return this;
        }

        public Builder token(String token) {
            result.token = token;
            return this;
        }

        public Builder expiresIn(long expiresIn) {
            result.expiresIn = expiresIn;
            return this;
        }

        public TokenResult build() {
            return result;
        }
    }
}
