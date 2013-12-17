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
package leshan.server.lwm2m.message;

/**
 * Data format defined by the LWM2M specification
 */
public enum ContentFormat {

    LINK("application/link-format"), TEXT("application/vnd.oma.lwm2m+text"), TLV("application/vnd.oma.lwm2m+tlv"),
    JSON("application/vnd.oma.lwm2m+json"), OPAQUE("application/vnd.oma.lwm2m+opaque");

    private final String mediaType;

    private ContentFormat(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaType() {
        return mediaType;
    }

    /**
     * Find the {@link ContentFormat} for the given media type (<code>null</code> if not found)
     */
    public static ContentFormat fromMediaType(String mediaType) {
        for (ContentFormat t : ContentFormat.values()) {
            if (t.getMediaType() == mediaType) {
                return t;
            }
        }
        return null;
    }

}
