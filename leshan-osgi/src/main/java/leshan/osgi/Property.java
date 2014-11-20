/*
 * Copyright (c) 2014, Bosch Software Innovations GmbH
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
package leshan.osgi;

/**
 * Property Class for the leshan-osgi Module.
 *
 */
public final class Property {

    public static final String LWM2M_OBJECTS = "LWM2M_OBJECTS";
    public static final String REGISTRATION_EXPIRATION = "LWM2M_REGISTRATION_EXPIRATION";
    public static final String LWM2M_REGISTRATION_EXPIRATION = "LWM2M_REGISTRATION_EXPIRATION";
    public static final String REGISTRATION_ID = "LWM2M_REGISTRATIONID";

    public static final String DEVICE_CATEGORY = "DEVICE_CATEGORY";
    public static final String CATEGORY_LWM2M_CLIENT = "LWM2MClient";
    public static final String MANUFACTURER = "LWM2M_MANUFACTURER";
    public static final String SERIAL = "LWM2M_SERIAL";
    public static final String MODELNUMBER = "LWM2M_MODELNUMBER";
    public static final String LWM2MPATH = "lwm2mpath";
    public static final String RESOURCESPEC = "resourcespec";
    public static final String NEWVALUE = "newvalue";
    public static final String LWM2MNODE = "node";
    public static final String CONTENTFORMAT = "contentformat";
    public static final String CLIENT = "client";

    public static final String REGISTERED_EVENT = "CLIENT_REGISTERED";
    public static final String UPDATED_EVENT = "CLIENT_UPDATED";
    public static final String UNREGISTERED_EVENT = "CLIENT_UNREGISTERED";

    private Property() {

    }

}
