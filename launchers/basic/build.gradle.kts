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

dependencies {
    api(project(":core:bootstrap"))
    implementation(project(":core:protocol:web"))
    implementation(project(":core:transfer"))
    implementation(project(":data-protocols:ids"))
    implementation(project(":extensions:in-memory:policy-registry-memory"))
    implementation(project(":extensions:in-memory:metadata-memory"))
    implementation(project(":extensions:in-memory:transfer-store-memory"))
    implementation(project(":extensions:iam:iam-mock"))
    implementation(project(":data-protocols:ids:ids-policy-mock"))
    implementation(project(":extensions:filesystem:configuration-fs"))

    implementation(project(":samples:copy-file-to-s3bucket"))
    implementation(project(":samples:public-rest-api"))
    implementation(project(":extensions:filesystem:vault-fs"))


    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")

}

application {
    @Suppress("DEPRECATION")
    mainClassName = "org.eclipse.dataspaceconnector.runtime.ConnectorRuntime"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("dataspaceconnector-basic.jar")
}
