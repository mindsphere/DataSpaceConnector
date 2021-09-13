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

val awsVersion: String by project

plugins {
    `java-library`
}


dependencies {
    api(project(":spi"))
    api(project(":common:util"))
    api(project(":core:bootstrap"))
    implementation(project(":extensions:aws:s3:s3-schema"))
    implementation(project(":extensions:azure:blob:blob-schema"))

    testImplementation(project(":extensions:atlas"))
    testImplementation(project(":samples:other:dataseed"))
    testImplementation(project(":extensions:azure:blob:provision"))
    testImplementation(testFixtures(project(":common:util")))


    // There is an incompatibility between the Amazon SDK, which internally uses httpclient 4.5.4 and
    // the Atlas Client, which is pulled in by the "atlas" extension, and which uses an earlier version
    // causing all requests to fail with a LambdaConversionException (of all things).
    // Since this situation can only arise in a test scenario, requiring a strict version is acceptable. Another
    // possibility would be to exclude one of the transitive dependencies.
    //
    // more about this problem can be found here:
    // https://github.com/aws/aws-sdk-java-v2/issues/652
    // https://docs.gradle.org/current/userguide/dependency_downgrade_and_exclude.html
    testImplementation("org.apache.httpcomponents:httpclient") {
        version {
            strictly("4.5.5")
        }
    }

    testImplementation("com.azure:azure-storage-blob:12.11.0")
    testImplementation(platform("software.amazon.awssdk:bom:${awsVersion}"))
    testImplementation("software.amazon.awssdk:s3")
}


