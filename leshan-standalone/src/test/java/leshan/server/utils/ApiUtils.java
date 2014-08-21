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
package leshan.server.utils;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.google.gson.Gson;

public class ApiUtils {

    private static final Gson gson = new Gson();

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getRegisteredClients() {

        String json = getAPI("api/clients");

        List<Map<String, Object>> clients = new ArrayList<>();
        return gson.fromJson(json, clients.getClass());
    }

    @SuppressWarnings("unchecked")
    public static String readResource(String endpoint, String... resourcePath) {
        Validate.notEmpty(resourcePath);

        String json = getAPI("api/clients/" + endpoint + "/" + StringUtils.join(resourcePath, "/"));

        Map<String, Object> result = new HashMap<>();
        result = gson.fromJson(json, result.getClass());

        assertEquals("CONTENT", result.get("status"));
        return (String) result.get("value");
    }

    public static void createPskSecurityInfo(String endpoint, String identity, String psk) {
        Map<String, Object> body = new HashMap<>();
        body.put("endpoint", endpoint);

        Map<String, String> pskFields = new HashMap<>();
        pskFields.put("identity", identity);
        pskFields.put("key", Hex.encodeHexString(psk.getBytes(Charsets.UTF_8)));
        body.put("psk", pskFields);

        putAPI("api/security", gson.toJson(body));
    }

    private static String getAPI(String url) {
        try {
            URL u = new URL("http://127.0.0.1:8080/" + url);
            URLConnection uc = u.openConnection();

            try (InputStream is = uc.getInputStream()) {
                String res = IOUtils.toString(is);
                System.err.println("GET " + url + " => '" + res + "'");

                return res;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void putAPI(String url, String json) {
        try {
            URL u = new URL("http://127.0.0.1:8080/" + url);
            HttpURLConnection uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestMethod("PUT");

            try (OutputStream output = uc.getOutputStream()) {
                output.write(json.getBytes(Charsets.UTF_8));
            }

            int status = ((HttpURLConnection) uc).getResponseCode();
            System.err.println("POST " + url + " content '" + json + "' => '" + status + "'");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
