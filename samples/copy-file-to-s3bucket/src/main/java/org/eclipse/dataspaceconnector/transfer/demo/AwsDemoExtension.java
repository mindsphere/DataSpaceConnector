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
package org.eclipse.dataspaceconnector.transfer.demo;

import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.AtomicConstraint;
import org.eclipse.dataspaceconnector.policy.model.LiteralExpression;
import static org.eclipse.dataspaceconnector.policy.model.Operator.IN;
import org.eclipse.dataspaceconnector.policy.model.OrConstraint;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.schema.s3.S3BucketSchema;
import org.eclipse.dataspaceconnector.spi.metadata.MetadataStore;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.policy.PolicyRegistry;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowManager;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.GenericDataCatalogEntry;
import java.util.List;

public class AwsDemoExtension implements ServiceExtension {

    private Monitor monitor;

    private ServiceExtensionContext context;

    public static final String USE_EU_POLICY = "use-eu";

    @Override
    public void initialize(ServiceExtensionContext context) {
        this.context = context;
        monitor = context.getMonitor();

        var dataFlowMgr = context.getService(DataFlowManager.class);

        var flowController = new DemoS3FlowController(context.getService(Vault.class), monitor);

        dataFlowMgr.register(flowController);
    }

    @Override
    public void start() {
        monitor.info("Started AWS Demo extension");
        saveDataEntries();
        savePolicies();
    }

    private void saveDataEntries() {
        MetadataStore metadataStore = context.getService(MetadataStore.class);

        GenericDataCatalogEntry sourceFileCatalog = GenericDataCatalogEntry.Builder.newInstance()
            .property(S3BucketSchema.BUCKET_NAME, "041167962674-eu-central-1-gaia-x-data")
            .property("keyName", "demo_image.jpg")
            .property("type", "dataspaceconnector:s3")
            .build();

        DataEntry entry1 = DataEntry.Builder.newInstance().id("demo_image.jpg").policyId(USE_EU_POLICY).catalogEntry(sourceFileCatalog).build();
        metadataStore.save(entry1);


        GenericDataCatalogEntry sourceFileCatalog2 = GenericDataCatalogEntry.Builder.newInstance()
            .property(S3BucketSchema.BUCKET_NAME, "datalake-prod-a-presiot-1586345024501")
            .property("keyName", "data/ten=presiot/data/ten=presiot/RC-MY/test.txt/part-00000-beb25069-8ffc-496d-be96-bcda0ed55edd-c000.csv")
            .property("type", "dataspaceconnector:s3")
            .build();

        DataEntry entry2 = DataEntry.Builder.newInstance().id("data/ten=presiot/data/ten=presiot/RC-MY/test.txt/part-00000-beb25069-8ffc-496d-be96-bcda0ed55edd-c000.csv")
            .policyId(USE_EU_POLICY)
            .catalogEntry(sourceFileCatalog2)
            .build();
        metadataStore.save(entry2);
    }

    private void savePolicies() {
        PolicyRegistry policyRegistry = context.getService(PolicyRegistry.class);

        LiteralExpression spatialExpression = new LiteralExpression("ids:absoluteSpatialPosition");
        var euConstraint = AtomicConstraint.Builder.newInstance().leftExpression(spatialExpression).operator(IN).rightExpression(new LiteralExpression("eu")).build();
        var euUsePermission = Permission.Builder.newInstance().action(Action.Builder.newInstance().type("idsc:USE").build()).constraint(euConstraint).build();
        var euPolicy = Policy.Builder.newInstance().id(USE_EU_POLICY).permission(euUsePermission).build();
        policyRegistry.registerPolicy(euPolicy);
    }
}
