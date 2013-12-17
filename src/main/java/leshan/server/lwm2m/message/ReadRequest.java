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

import org.apache.commons.lang.Validate;

/**
 * The request to access the value of a Resource, an array of Resource Instances, an Object Instance or all the Object
 * Instances of an Object.
 */
public class ReadRequest {

    private final Integer objectId;

    private final Integer objectInstanceId;

    private final Integer resourceId;

    public ReadRequest(Integer objectId, Integer objectInstanceId, Integer resourceId) {
        Validate.notNull(objectId);

        this.objectId = objectId;
        this.objectInstanceId = objectInstanceId;
        this.resourceId = resourceId;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ReadRequest [objectId=").append(objectId).append(", objectInstanceId=")
                .append(objectInstanceId).append(", resourceId=").append(resourceId).append("]");
        return builder.toString();
    }

}
