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
package leshan.server.lwm2m.tlv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang.Validate;

/**
 * A Type-Length-Value container, can contain multiple TLV entries.
 */
public class Tlv {

    // type of TLV, indicate if it's containing a value or TLV containing more TLV or values
    private TlvType type;

    // if of type OBJECT_INSTANCE,MULTIPLE_RESOURCE or null
    private Tlv[] children;

    // if type RESOURCE_VALUE or RESOURCE_INSTANCE => null
    private byte[] value;

    private int identifier;

    /**
     * Creates a TLV container.
     * 
     * @param type the type of TLV
     * @param children the list of children TLV (must be <code>null</code> for type {@link TlvType#RESOURCE_VALUE}
     * @param value the raw contained value, only for type {@link TlvType#RESOURCE_VALUE} <code>null</code> for the
     *        other types
     * @param identifier the TLV identifier (resource id, instance id,..)
     */
    public Tlv(TlvType type, Tlv[] children, byte[] value, int identifier) {
        this.type = type;
        this.children = children;
        this.value = value;
        this.identifier = identifier;

        if (type == TlvType.RESOURCE_VALUE || type == TlvType.RESOURCE_INSTANCE) {
            if (value == null) {
                throw new IllegalArgumentException("a " + type.name() + " must have a value");
            } else if (children != null) {
                throw new IllegalArgumentException("a " + type.name() + " can't have children");
            }
        } else {
            if (value != null) {
                throw new IllegalArgumentException("a " + type.name() + " can't have a value");
            } else if (children == null) {
                throw new IllegalArgumentException("a " + type.name() + " must have children");
            }
        }
    }

    /**
     * Creates a TLV container for an Integer value.
     * 
     * @param type the type of TLV. Must be {@link TlvType#RESOURCE_VALUE} or {@link TlvType#RESOURCE_INSTANCE}
     * @param value the integer value
     * @param identifier the TLV identifier (resource id, instance id...)
     */
    public static Tlv newIntegerValue(TlvType type, int value, int identifier) {
        // TODO optimize for 1-byte and 2-byte long values
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(value);
        return new Tlv(type, null, bb.array(), identifier);
    }

    /**
     * Creates a TLV container for an Long value.
     * 
     * @param type the type of TLV. Must be {@link TlvType#RESOURCE_VALUE} or {@link TlvType#RESOURCE_INSTANCE}
     * @param value the long value
     * @param identifier the TLV identifier (resource id, instance id...)
     */
    public static Tlv newLongValue(TlvType type, long value, int identifier) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putLong(value);
        return new Tlv(type, null, bb.array(), identifier);
    }

    /**
     * Creates a TLV container for a String value.
     * 
     * @param type the type of TLV. Must be {@link TlvType#RESOURCE_VALUE} or {@link TlvType#RESOURCE_INSTANCE}
     * @param value the String value
     * @param identifier the TLV identifier (resource id, instance id...)
     */
    public static Tlv newStringValue(TlvType type, String value, int identifier) {
        Validate.notEmpty(value);
        return new Tlv(type, null, value.getBytes(Charsets.UTF_8), identifier);
    }

    /**
     * Creates a TLV container for a boolean value.
     * 
     * @param type the type of TLV. Must be {@link TlvType#RESOURCE_VALUE} or {@link TlvType#RESOURCE_INSTANCE}
     * @param value the boolean value
     * @param identifier the TLV identifier (resource id, instance id...)
     */
    public static Tlv newBooleanValue(TlvType type, boolean value, int identifier) {
        byte[] bValue;
        if (value) {
            bValue = new byte[] { 0x01 };
        } else {
            bValue = new byte[] { 0x00 };
        }
        return new Tlv(type, null, bValue, identifier);
    }

    /**
     * Creates a TLV container for a Date value.
     * 
     * @param type the type of TLV. Must be {@link TlvType#RESOURCE_VALUE} or {@link TlvType#RESOURCE_INSTANCE}
     * @param value the Date value
     * @param identifier the TLV identifier (resource id, instance id...)
     */
    public static Tlv newDateValue(TlvType type, Date value, int identifier) {
        // the number of seconds since Jan 1st, 1970 in the UTC time zone.
        return newLongValue(type, value.getTime() / 1000L, identifier);
    }

    public TlvType getType() {
        return type;
    }

    public void setType(TlvType type) {
        this.type = type;
    }

    public Tlv[] getChildren() {
        return children;
    }

    public void setChildren(Tlv[] children) {
        this.children = children;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return String.format(
                "Tlv [type=%s, children=%s, value=%s, identifier=%s]",
                new Object[] { type.name(), Arrays.toString(children), Arrays.toString(value),
                                        Integer.toString(identifier) });
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(children);
        result = prime * result + identifier;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + Arrays.hashCode(value);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tlv other = (Tlv) obj;
        if (!Arrays.equals(children, other.children))
            return false;
        if (identifier != other.identifier)
            return false;
        if (type != other.type)
            return false;
        if (!Arrays.equals(value, other.value))
            return false;
        return true;
    }
}
