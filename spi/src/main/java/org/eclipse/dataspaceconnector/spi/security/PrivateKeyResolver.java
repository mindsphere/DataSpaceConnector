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

package org.eclipse.dataspaceconnector.spi.security;

import org.jetbrains.annotations.Nullable;

import java.security.interfaces.RSAPrivateKey;

/**
 * Resolves RSA private keys.
 */
public interface PrivateKeyResolver {

    /**
     * Returns the private key associated with the id or null if not found.
     */
    @Nullable
    RSAPrivateKey resolvePrivateKey(String id);

}
