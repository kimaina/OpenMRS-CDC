/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openmrs.cdc.pipeline;

import org.apache.camel.main.Main;

public class Runner {
    private static final Main MAIN = new Main();

    public static void main(String[] args) throws Exception {
        MAIN.addRouteBuilder(EncObsCDCNotifier.class);
        MAIN.run();
    }
}
