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
package leshan.server.lwm2m.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang.StringUtils;

public class LinkObject {

    private final String url;

    private final Map<String, Object> attributes;

    private final Integer objectId;

    private final Integer objectInstanceId;

    private final Integer resourceId;

    /**
     * Creates a new link object without attributes.
     * 
     * @param url the object link URL
     */
    public LinkObject(String url) {
        this(url, null);
    }

    /**
     * Creates a new instance from a URL and attributes.
     * 
     * @param url the object link URL
     * @param attributes the object link attributes or <code>null</code> if the link has no attributes
     */
    public LinkObject(String url, Map<String, ?> attributes) {
        this.url = url;
        if (attributes != null) {
            this.attributes = Collections.unmodifiableMap(new HashMap<String, Object>(attributes));
        } else {
            this.attributes = Collections.unmodifiableMap(new HashMap<String, Object>());
        }

        Matcher mat = Pattern.compile("(/(\\d+))(/(\\d+))?(/(\\d+))?").matcher(url);

        if (mat.find()) {
            objectId = mat.group(2) == null ? null : new Integer(mat.group(2));
            objectInstanceId = mat.group(4) == null ? null : new Integer(mat.group(4));
            resourceId = mat.group(6) == null ? null : new Integer(mat.group(6));
        } else {
            objectId = null;
            objectInstanceId = null;
            resourceId = null;
        }
    }

    public String getUrl() {
        return url;
    }

    /**
     * Gets the link attributes
     * 
     * @return an unmodifiable map containing the link attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getPath() {
        StringBuilder sb = new StringBuilder("/");
        if (objectId != null) {
            sb.append(objectId);
        }

        if (objectInstanceId != null) {
            sb.append("/").append(objectInstanceId);
        }

        if (resourceId != null) {
            sb.append("/").append(resourceId);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("LinkObject [url=%s, attributes=%s]", url, attributes);
    }

    public Integer getObjectId() {
        return objectId;
    }

    public Integer getObjectInstanceId() {
        return objectInstanceId;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public static LinkObject[] parse(byte[] content) {
        String s = new String(content, Charsets.UTF_8);
        String[] links = s.split(",");
        LinkObject[] linksResult = new LinkObject[links.length];
        int index = 0;
        for (String link : links) {
            String[] linkParts = link.split(";");

            // clean URL
            String url = StringUtils.trim(linkParts[0]);
            url = StringUtils.removeStart(StringUtils.removeEnd(url, ">"), "<");

            // parse attributes
            Map<String, Object> attributes = new HashMap<>();

            if (linkParts.length > 1) {
                for (int i = 1; i < linkParts.length; i++) {
                    String[] attParts = linkParts[i].split("=");
                    if (attParts.length > 0) {
                        String key = attParts[0];
                        Object value = null;
                        if (attParts.length > 1) {
                            String rawvalue = attParts[1];
                            try {
                                value = Integer.valueOf(rawvalue);
                            } catch (NumberFormatException e) {

                                value = rawvalue.replaceFirst("^\"(.*)\"$", "$1");
                            }
                        }
                        attributes.put(key, value);
                    }
                }
            }
            linksResult[index] = new LinkObject(url, attributes);
            index++;
        }
        return linksResult;
    }
}