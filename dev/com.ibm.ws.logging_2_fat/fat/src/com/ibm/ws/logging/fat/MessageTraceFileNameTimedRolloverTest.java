/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.logging.fat;

import static org.junit.Assert.assertFalse; // For negative assertions
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ibm.websphere.simplicity.RemoteFile;

import componenttest.annotation.Server;
import componenttest.custom.junit.runner.FATRunner;
import componenttest.topology.impl.LibertyServer;

@RunWith(FATRunner.class)
public class MessageTraceFileNameTimedRolloverTest {

    @Server("messageTraceRolloverServer")
    public static LibertyServer server;

    @BeforeClass
    public static void setUp() throws Exception {
        // Any setup needed before tests
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (server != null && server.isStarted()) {
            server.stopServer();
        }
    }

    @Test
    public void testTraceFileNameStdoutBootstrapWithTimedRollover() throws Exception {
        // Configure server
        server.copyFileToLibertyInstallRoot("lib/features", "internalFeatures/logging-1.0.mf");
        server.copyFileToLibertyServerRoot("server.env", "server.env.timedRollover");
        server.copyFileToLibertyServerRoot("bootstrap.properties", "bootstrap.properties.traceStdout");

        // Start server and verify
        server.startServer();
        RemoteFile messageLog = new RemoteFile(server.getMachine(), server.getLogsRoot() + "message.log");
        RemoteFile traceLog = new RemoteFile(server.getMachine(), server.getLogsRoot() + "trace.log");

        assertTrue("message.log should exist", messageLog.exists());
        assertFalse("trace.log should NOT exist", traceLog.exists());

        // TODO: Add rollover verification (wait for rollover, check for rotated file)
    }

    @Test
    public void testTraceFileNameStdoutServerXmlWithTimedRollover() throws Exception {
        server.setServerConfigurationFile("server_traceStdout.xml");
        server.copyFileToLibertyServerRoot("server.env", "server.env.timedRollover");

        server.startServer();
        RemoteFile traceLog = new RemoteFile(server.getMachine(), server.getLogsRoot() + "trace.log");
        assertFalse("trace.log should NOT roll over when redirected to stdout",
                    traceLog.exists());
    }

    @Test
    public void testDynamicTraceFileNameStdoutToTraceLog() throws Exception {
        // Initial config (stdout)
        server.copyFileToLibertyServerRoot("bootstrap.properties", "bootstrap.properties.traceStdout");
        server.startServer();
        assertFalse("Initial trace.log should not exist",
                    new RemoteFile(server.getMachine(), server.getLogsRoot() + "trace.log").exists());

        // Dynamic update to trace.log
        server.setMarkToEndOfLog();
        server.setServerConfigurationFile("server_traceLog.xml");
        assertNotNull("Config update should complete",
                      server.waitForConfigUpdateInLogUsingMark(null));

        // Verify trace.log now exists and rolls over
        RemoteFile traceLog = new RemoteFile(server.getMachine(), server.getLogsRoot() + "trace.log");
        assertTrue("trace.log should exist after update", traceLog.exists());
        // TODO: Add rollover verification
    }

    @Test
    public void testDynamicTraceFileNameToCustomFile() throws Exception {
        server.startServer(); // Default config

        // Update to custom trace file
        server.setServerConfigurationFile("server_customTrace.xml");
        assertNotNull("Config update should complete",
                      server.waitForConfigUpdateInLogUsingMark(null));

        RemoteFile customFile = new RemoteFile(server.getMachine(), server.getLogsRoot() + "custom_trace.log");
        RemoteFile defaultTrace = new RemoteFile(server.getMachine(), server.getLogsRoot() + "trace.log");

        assertTrue("custom_trace.log should exist", customFile.exists());
        assertFalse("Default trace.log should NOT roll over", defaultTrace.exists());
    }

    @Test
    public void testDynamicMessageFileNameToCustomFile() throws Exception {
        server.startServer(); // Default config

        // Update to custom message file
        server.setServerConfigurationFile("server_customMessage.xml");
        assertNotNull("Config update should complete",
                      server.waitForConfigUpdateInLogUsingMark(null));

        RemoteFile customFile = new RemoteFile(server.getMachine(), server.getLogsRoot() + "custom_message.log");
        RemoteFile defaultMessage = new RemoteFile(server.getMachine(), server.getLogsRoot() + "message.log");

        assertTrue("custom_message.log should exist", customFile.exists());
        assertFalse("Default message.log should NOT roll over", defaultMessage.exists());
    }

}