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


val securityType: String by rootProject.extra
val iamType: String by rootProject.extra
val configFs: String by rootProject.extra
val jupiterVersion: String by project

plugins {
    `java-library`
}

dependencies {
    api(project(":core:bootstrap"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")

    println("Using security type: ${securityType}")

    if (securityType != "default") {
        api(project(":extensions:security:security-${securityType}"))
    }

    if (iamType == "oauth2") {
        api(project(":extensions:iam:oauth2"))
    }

    if (configFs == "enabled") {
        api(project(":extensions:filesystem:configuration-fs"))
    }

}

