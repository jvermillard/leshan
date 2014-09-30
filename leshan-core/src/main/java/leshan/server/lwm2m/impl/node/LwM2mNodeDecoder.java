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
package leshan.server.lwm2m.impl.node;

import java.nio.ByteBuffer;
import java.util.Date;

import leshan.server.lwm2m.impl.objectspec.ResourceSpec;
import leshan.server.lwm2m.impl.objectspec.ResourceSpec.Type;
import leshan.server.lwm2m.impl.objectspec.Resources;
import leshan.server.lwm2m.impl.tlv.Tlv;
import leshan.server.lwm2m.impl.tlv.TlvDecoder;
import leshan.server.lwm2m.node.LwM2mNode;
import leshan.server.lwm2m.node.LwM2mObject;
import leshan.server.lwm2m.node.LwM2mObjectInstance;
import leshan.server.lwm2m.node.LwM2mPath;
import leshan.server.lwm2m.node.LwM2mResource;
import leshan.server.lwm2m.node.Value;
import leshan.server.lwm2m.request.ContentFormat;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LwM2mNodeDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(LwM2mNodeDecoder.class);

    /**
     * Deserializes a binary content into a {@link LwM2mNode}.
     * 
     * @param content the content
     * @param format the content format
     * @param path the path of the node to build
     * @return the resulting node
     * @throws InvalidValueException
     */
    public static LwM2mNode decode(byte[] content, ContentFormat format, LwM2mPath path) throws InvalidValueException {
        LOG.debug("Decoding value for path {} and format {}: {}", path, format, content);

        Validate.notNull(path);

        // default to plain/text
        if (format == null) {
            if (path.isResource()) {
                ResourceSpec rDesc = Resources.getDescription(path.getObjectId(), path.getResourceId());
                if (rDesc != null && rDesc.multiple) {
                    format = ContentFormat.TLV;
                } else {
                    format = ContentFormat.TEXT;
                }
            } else {
                // HACK: client should return a content type
                // but specific lwm2m ones are not yet defined
                format = ContentFormat.TLV;
            }
        }

        switch (format) {
        case TEXT:
            // single resource value
            Validate.notNull(path.getResourceId());
            ResourceSpec rDesc = Resources.getDescription(path.getObjectId(), path.getResourceId());

            String strValue = new String(content, Charsets.UTF_8);
            Value<?> value = null;
            if (rDesc != null) {
                value = parseTextValue(strValue, rDesc.type, path);
            } else {
                // unknown resource, returning a default string value
                value = Value.newStringValue(strValue);
            }
            return new LwM2mResource(path.getResourceId(), value);

        case TLV:
            Tlv[] tlvs = TlvDecoder.decode(ByteBuffer.wrap(content));
            return parseTlv(tlvs, path);

        case JSON:
        case LINK:
        case OPAQUE:
            throw new NotImplementedException("Content format " + format + " not yet implemented");
        }
        return null;

    }

    private static Value<?> parseTextValue(String value, Type type, LwM2mPath path) throws InvalidValueException {
        LOG.trace("TEXT value for path {} and expected type {}: {}", path, type, value);

        try {
            switch (type) {
            case STRING:
                return Value.newStringValue(value);
            case INTEGER:
                try {
                    Long lValue = Long.valueOf(value);
                    if (lValue >= Integer.MIN_VALUE && lValue <= Integer.MAX_VALUE) {
                        return Value.newIntegerValue(lValue.intValue());
                    } else {
                        return Value.newLongValue(lValue);
                    }
                } catch (NumberFormatException e) {
                    throw new InvalidValueException("Invalid value for integer resource: " + value, path);
                }
            case BOOLEAN:
                switch (value) {
                case "0":
                    return Value.newBooleanValue(false);
                case "1":
                    return Value.newBooleanValue(true);
                default:
                    throw new InvalidValueException("Invalid value for boolean resource: " + value, path);
                }
            case FLOAT:
                try {
                    Double dValue = Double.valueOf(value);
                    if (dValue >= Float.MIN_VALUE && dValue <= Float.MAX_VALUE) {
                        return Value.newFloatValue(dValue.floatValue());
                    } else {
                        return Value.newDoubleValue(dValue);
                    }
                } catch (NumberFormatException e) {
                    throw new InvalidValueException("Invalid value for float resource: " + value, path);
                }
            case TIME:
                // number of seconds since 1970/1/1
                return Value.newDateValue(new Date(Long.valueOf(value) * 1000L));
            case OPAQUE:
                // not specified
            default:
                throw new InvalidValueException("Could not parse opaque value with content format " + type, path);
            }
        } catch (NumberFormatException e) {
            throw new InvalidValueException("Invalid numeric value: " + value, path, e);
        }
    }

    private static LwM2mNode parseTlv(Tlv[] tlvs, LwM2mPath path) throws InvalidValueException {
        LOG.trace("Parsing TLV content for path {}: {}", path, tlvs);

        if (path.isObject()) {
            // object level request
            LwM2mObjectInstance[] instances = new LwM2mObjectInstance[tlvs.length];
            for (int i = 0; i < tlvs.length; i++) {
                instances[i] = parseObjectInstancesTlv(tlvs[i], path.getObjectId());
            }
            return new LwM2mObject(path.getObjectId(), instances);

        } else if (path.isObjectInstance()) {
            // object instance level request
            LwM2mResource[] resources = new LwM2mResource[tlvs.length];
            for (int i = 0; i < tlvs.length; i++) {
                resources[i] = parseResourceTlv(tlvs[i], path.getObjectId(), path.getObjectInstanceId());
            }
            return new LwM2mObjectInstance(path.getObjectInstanceId(), resources);

        } else {
            // resource level request
            if (tlvs.length == 1) {
                switch (tlvs[0].getType()) {
                case RESOURCE_VALUE:
                    // single value
                    return new LwM2mResource(tlvs[0].getIdentifier(), parseTlvValue(tlvs[0].getValue(), path));
                case MULTIPLE_RESOURCE:
                    // supported but not compliant with the TLV specification
                    return parseResourceTlv(tlvs[0], path.getObjectId(), path.getObjectInstanceId());

                default:
                    throw new InvalidValueException("Invalid TLV type: " + tlvs[0].getType(), path);
                }
            } else {
                // array of values
                Value<?>[] values = new Value[tlvs.length];
                for (int j = 0; j < tlvs.length; j++) {
                    values[j] = parseTlvValue(tlvs[j].getValue(), path);
                }
                return new LwM2mResource(path.getResourceId(), values);
            }
        }
    }

    private static LwM2mObjectInstance parseObjectInstancesTlv(Tlv tlv, int objectId) throws InvalidValueException {
        // read resources
        LwM2mResource[] resources = new LwM2mResource[tlv.getChildren().length];
        for (int i = 0; i < tlv.getChildren().length; i++) {
            resources[i] = parseResourceTlv(tlv.getChildren()[i], objectId, tlv.getIdentifier());
        }
        return new LwM2mObjectInstance(tlv.getIdentifier(), resources);
    }

    private static LwM2mResource parseResourceTlv(Tlv tlv, int objectId, int objectInstanceId)
            throws InvalidValueException {
        LwM2mPath rscPath = new LwM2mPath(objectId, objectInstanceId, tlv.getIdentifier());
        switch (tlv.getType()) {
        case MULTIPLE_RESOURCE:
            // read values
            Value<?>[] values = new Value[tlv.getChildren().length];
            for (int j = 0; j < tlv.getChildren().length; j++) {
                values[j] = parseTlvValue(tlv.getChildren()[j].getValue(), rscPath);
            }
            return new LwM2mResource(tlv.getIdentifier(), values);
        case RESOURCE_VALUE:
            return new LwM2mResource(tlv.getIdentifier(), parseTlvValue(tlv.getValue(), rscPath));
        default:
            throw new InvalidValueException("Invalid TLV value", rscPath);
        }
    }

    private static Value<?> parseTlvValue(byte[] value, LwM2mPath rscPath) throws InvalidValueException {

        ResourceSpec rscDesc = Resources.getDescription(rscPath.getObjectId(), rscPath.getResourceId());
        if (rscDesc == null) {
            LOG.trace("TLV value for path {} and unknown type: {}", rscPath, value);
            // no resource description... opaque
            return Value.newBinaryValue(value);
        }

        LOG.trace("TLV value for path {} and expected type {}: {}", rscPath, rscDesc.type, value);
        try {
            switch (rscDesc.type) {
            case STRING:
                return Value.newStringValue(TlvDecoder.decodeString(value));
            case INTEGER:
                Number intNb = TlvDecoder.decodeInteger(value);
                if (value.length < 8) {
                    return Value.newIntegerValue(intNb.intValue());
                } else {
                    return Value.newLongValue(intNb.longValue());
                }

            case BOOLEAN:
                return Value.newBooleanValue(TlvDecoder.decodeBoolean(value));

            case FLOAT:
                Number floatNb = TlvDecoder.decodeFloat(value);
                if (value.length < 8) {
                    return Value.newFloatValue(floatNb.floatValue());
                } else {
                    return Value.newDoubleValue(floatNb.doubleValue());
                }

            case TIME:
                return Value.newDateValue(TlvDecoder.decodeDate(value));

            case OPAQUE:
            default:
                return Value.newBinaryValue(value);
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException("Invalid content for type " + rscDesc.type, rscPath, e);
        }
    }
}
