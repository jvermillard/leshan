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
package leshan.server.lwm2m.node;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

public class LwM2mPath {

    private final int objectId;
    private final Integer objectInstanceId;
    private final Integer resourceId;

    public LwM2mPath(int objectId) {
        this.objectId = objectId;
        this.objectInstanceId = null;
        this.resourceId = null;
    }

    public LwM2mPath(int objectId, int objectInstanceId) {
        this.objectId = objectId;
        this.objectInstanceId = objectInstanceId;
        this.resourceId = null;
    }

    public LwM2mPath(int objectId, int objectInstanceId, int resourceId) {
        this.objectId = objectId;
        this.objectInstanceId = objectInstanceId;
        this.resourceId = resourceId;
    }

    public LwM2mPath(Integer objectId, Integer objectInstanceId, Integer resourceId) {
        this.objectId = objectId;
        this.objectInstanceId = objectInstanceId;
        this.resourceId = resourceId;
    }

    /**
     * Constructs a {@link LwM2mPath} from a string representation
     * 
     * @param path the path (e.g. /3/0/1)
     */
    public LwM2mPath(String path) {
        Validate.notEmpty(path);
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Invalid LWM2M path: " + path);
        }
        String[] p = StringUtils.split(path, '/');
        if (p.length < 1 || p.length > 3) {
            throw new IllegalArgumentException("Invalid length for path: " + path);
        }
        try {
            this.objectId = Integer.valueOf(p[0]);
            this.objectInstanceId = (p.length >= 2) ? Integer.valueOf(p[1]) : null;
            this.resourceId = (p.length == 3) ? Integer.valueOf(p[2]) : null;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid elements in path: " + path, e);
        }
    }

    /**
     * Returns the object ID in the path.
     * 
     * @return the object ID
     */
    public int getObjectId() {
        return objectId;
    }

    /**
     * Returns the object instance ID in the path.
     * 
     * @return the object instance ID. Can be <code>null</code> when this is an object path.
     */
    public Integer getObjectInstanceId() {
        return objectInstanceId;
    }

    /**
     * Returns the resource ID in the request path.
     * 
     * @return the resource ID. Can be <code>null</code> when this is a object/object instance path.
     */
    public Integer getResourceId() {
        return resourceId;
    }

    /**
     * @return <code>true</code> if this is an Object path.
     */
    public boolean isObject() {
        return objectInstanceId == null && resourceId == null;
    }

    /**
     * @return <code>true</code> if this is an ObjectInstance path.
     */
    public boolean isObjectInstance() {
        return objectInstanceId != null && resourceId == null;
    }

    /**
     * @return <code>true</code> if this is a Resource path.
     */
    public boolean isResource() {
        return objectInstanceId != null && resourceId != null;
    }

    /**
     * The string representation of the path: /{Object ID}/{ObjectInstance ID}/{Resource ID}
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("/");
        b.append(getObjectId());
        if (getObjectInstanceId() != null) {
            b.append("/").append(getObjectInstanceId());
            if (getResourceId() != null) {
                b.append("/").append(getResourceId());
            }
        }
        return b.toString();
    }

}