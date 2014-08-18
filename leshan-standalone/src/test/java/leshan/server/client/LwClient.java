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
package leshan.server.client;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

import leshan.server.clienttest.TestClient;

import org.apache.commons.lang.StringUtils;

public class LwClient implements Closeable, TestClient {

    private Process p;
    private BufferedReader br;

    public void start(String endpoint, String script, String... params) {

        String luaScript = this.getClass().getResource(script).getFile();

        try {
            // Run a docker container to simulate a client
            p = Runtime.getRuntime().exec(
                    "sudo docker run -t --rm --name=lwm2mClientIT -e ENDPOINT=" + endpoint + " -v " + luaScript
                            + ":/lwm2m/script.lua jvermillard/lualwm2m script.lua " + StringUtils.join(params, " "));
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));

        } catch (IOException e) {
            throw new IllegalStateException("Unable to run LW test client", e);
        }
    }

    @Override
    public void close() throws IOException {

        System.err.println("Stopping LWM2M client");

        StringBuilder builder = new StringBuilder();
        String current = "";
        while (br.ready() && (current = br.readLine()) != null) {
            builder.append(current).append("\n");
        }
        System.err.println("---------\nClient logs:\n" + builder.toString() + "\n----------");

        try {
            Process stop = Runtime.getRuntime().exec("sudo docker stop lwm2mClientIT");

            System.err.println("Waiting for docker container to stop");
            stop.waitFor();
            p.waitFor();

            br.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void quit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void die() {
		// TODO Auto-generated method stub

	}
}
