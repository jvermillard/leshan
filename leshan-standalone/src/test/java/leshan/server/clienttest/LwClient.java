/*
 * Copyright (c) 2013, Sierra Wireless
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
