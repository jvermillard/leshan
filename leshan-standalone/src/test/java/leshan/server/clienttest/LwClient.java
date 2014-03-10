/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package leshan.server.clienttest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class LwClient implements Closeable {

    private Process p;

    private BufferedReader br;

    private BufferedWriter bw;

    public void start() {
        try {

            p = Runtime.getRuntime().exec(
                    "src/test/resources/testclient-" + System.getProperty("os.name").toLowerCase() + "-"
                            + System.getProperty("os.arch").toLowerCase());
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            Thread.sleep(500);
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Unable to run LW test client", e);
        }
    }

    public void quit() {
        sendCmd("quit");
    }

    public void die() {
        sendCmd("die");
    }

    private void sendCmd(String cmd) {
        String line;
        try {
            while ((line = br.readLine()) != null) {
                System.err.println("line : " + line);
                if (line.startsWith("> ")) {
                    // trigger unregister
                    bw.write(cmd);

                    bw.newLine();
                    bw.flush();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("IO error", e);
        }
    }

    @Override
    public void close() throws IOException {
        System.err.println("wait for termination");
        p.destroy();
        try {
            int code = p.waitFor();
            System.err.println("CODE : " + code);
            br.close();
            bw.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
