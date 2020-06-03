/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openmrs.cdc.pipeline;

import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.debezium.DebeziumConstants;

import io.debezium.data.Envelope;

public class EncObsCDCNotifier extends RouteBuilder {

    private static final String EVENT_TYPE_OBS = ".obs";
    private static final String EVENT_TYPE_ENCOUNTER = ".encounter";

    @Override
    public void configure() throws Exception {
        final Predicate isCreateOrUpdateEvent =
                    header(DebeziumConstants.HEADER_OPERATION).in(
                            constant(Envelope.Operation.READ.code()),
                            constant(Envelope.Operation.CREATE.code()),
                            constant(Envelope.Operation.UPDATE.code()));

        final Predicate isCreateEvent =
                header(DebeziumConstants.HEADER_OPERATION).in(
                        constant(Envelope.Operation.READ.code()),
                        constant(Envelope.Operation.CREATE.code()));

        final Predicate isObsEvent =
                header(DebeziumConstants.HEADER_IDENTIFIER).endsWith(EVENT_TYPE_OBS);

        final Predicate isEncounterEvent =
                header(DebeziumConstants.HEADER_IDENTIFIER).endsWith(EVENT_TYPE_ENCOUNTER);
        

        from("debezium-mysql:{{database.hostname}}?"
                + "databaseHostname={{database.hostname}}"
                + "&databaseServerId=77" // TODO: make this config-able
                + "&databasePort={{database.port}}"
                + "&databaseUser={{database.user}}"
                + "&databasePassword={{database.password}}"
                + "&name=mysql" // TODO: make this config-able
                + "&databaseServerName=mysql"
                + "&databaseWhitelist={{database.schema}}"
                + "&tableWhitelist={{database.schema}}.encounter,{{database.schema}}.obs"
               // + "&offsetStorage=org.apache.kafka.connect.storage.MemoryOffsetBackingStore" // use this for kafka
                + "&offsetStorageFileName=/tmp/offset.dat" // TODO: make this config-able
                + "&databaseHistoryFileFilename=/tmp/dbhistory.dat" // TODO: make this config-able
            )
                .routeId(EncObsCDCNotifier.class.getName() + ".DatabaseReader")
                .log(LoggingLevel.DEBUG, "Incoming message ${body} with headers ${headers}")
                .choice()
                    .when(isObsEvent)
                        .filter(isCreateOrUpdateEvent)
                            //.convertBodyTo(Obs.class)
                            .log(LoggingLevel.TRACE, "Obs CreateOrUpdateEvent Emitted ---> ${body}")
                            //.bean(store, "readFromStoreAndUpdateIfNeeded")
                            //.to(ROUTE-XXXX)
                        .endChoice()
                    .when(isEncounterEvent)
                        .filter(isCreateOrUpdateEvent)
                            .log(LoggingLevel.TRACE, "Enc CreateOrUpdateEvent Emitted ---> ${body}")
                        .endChoice()
                    .otherwise()
                        .log(LoggingLevel.WARN, "Unknown type ${headers[" + DebeziumConstants.HEADER_IDENTIFIER + "]}")
                .endParent();
    }
}
