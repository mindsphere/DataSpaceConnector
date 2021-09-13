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

plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

val jupiterVersion: String by project
val rsApi: String by project

dependencies {
    implementation(project(":core:bootstrap"))
    implementation(project(":core:transfer"))
    implementation(project(":core:protocol:web"))
    implementation(project(":core:policy:policy-model"))
    implementation(project(":core:policy:policy-engine"))

    implementation(project(":extensions:in-memory:transfer-store-memory"))
    implementation(project(":extensions:filesystem:configuration-fs"))
    implementation(project(":extensions:iam:iam-mock"))
    implementation(project(":extensions:in-memory:policy-registry-memory"))
    implementation(project(":extensions:in-memory:metadata-memory"))
    implementation(project(":data-protocols:ids"))
    implementation(project(":data-protocols:ids:ids-policy-mock"))

    implementation(project(":samples:04-file-transfer:transfer-file"))

    implementation("jakarta.ws.rs:jakarta.ws.rs-api:${rsApi}")
}

application {
    @Suppress("DEPRECATION")
    mainClassName = "org.eclipse.dataspaceconnector.runtime.ConnectorRuntime"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("provider.jar")
}
