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
package leshan.standalone.servlet.json;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;

import leshan.server.security.SecurityInfo;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class SecurityDeserializer implements JsonDeserializer<SecurityInfo> {

    @Override
    public SecurityInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        if (json == null) {
            return null;
        }

        SecurityInfo info = null;

        if (json.isJsonObject()) {
            JsonObject object = (JsonObject) json;

            String endpoint = null;
            if (object.has("endpoint")) {
                endpoint = object.get("endpoint").getAsString();
            } else {
                throw new JsonParseException("Missing endpoint");
            }

            JsonObject psk = (JsonObject) object.get("psk");
            JsonObject rpk = (JsonObject) object.get("rpk");
            if (psk != null) {
                // PSK Deserialization
                String identity = null;
                if (psk.has("identity")) {
                    identity = psk.get("identity").getAsString();
                } else {
                    throw new JsonParseException("Missing PSK identity");
                }
                byte[] key;
                try {
                    key = Hex.decodeHex(psk.get("key").getAsString().toCharArray());
                } catch (DecoderException e) {
                    throw new JsonParseException(e);
                }

                info = SecurityInfo.newPreSharedKeyInfo(endpoint, identity, key);
            } else if (rpk != null) {
                PublicKey key;
                try {
                    byte[] x = Hex.decodeHex(rpk.get("x").getAsString().toCharArray());
                    byte[] y = Hex.decodeHex(rpk.get("y").getAsString().toCharArray());
                    String params = rpk.get("params").getAsString();

                    AlgorithmParameters algoParameters = AlgorithmParameters.getInstance("EC");
                    algoParameters.init(new ECGenParameterSpec(params));
                    ECParameterSpec parameterSpec = algoParameters.getParameterSpec(ECParameterSpec.class);

                    KeySpec keySpec = new ECPublicKeySpec(new ECPoint(new BigInteger(x), new BigInteger(y)),
                            parameterSpec);

                    key = KeyFactory.getInstance("EC").generatePublic(keySpec);
                } catch (DecoderException | InvalidKeySpecException | NoSuchAlgorithmException
                        | InvalidParameterSpecException e) {
                    throw new JsonParseException("Invalid security info content", e);
                }
                info = SecurityInfo.newRawPublicKeyInfo(endpoint, key);
            } else {
                throw new JsonParseException("Invalid security info content");
            }
        }

        return info;
    }
}
