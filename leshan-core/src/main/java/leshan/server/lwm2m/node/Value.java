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

import java.util.Arrays;
import java.util.Date;

/**
 * A resource value.
 * 
 * @param <T> the value primitive type
 */
public class Value<T> {

    /**
     * The data type for a resource value
     */
    public enum DataType {
        /** String */
        STRING,
        /** A 8, 16, or 32-bit signed integer */
        INTEGER,
        /** a 64-bit signed integer */
        LONG,
        /** A 32-bit floating point number */
        FLOAT,
        /** A 64-bit floating point number */
        DOUBLE,
        /** Boolean */
        BOOLEAN,
        /** Binary */
        OPAQUE,
        /** Date */
        TIME
    }

    public final T value;

    public final DataType type;

    private Value(T value, DataType type) {
        this.value = value;
        this.type = type;
    }

    public static Value<String> newStringValue(String value) {
        return new Value<String>(value, DataType.STRING);
    }

    public static Value<Integer> newIntegerValue(int value) {
        return new Value<Integer>(value, DataType.INTEGER);
    }

    public static Value<Long> newLongValue(long value) {
        return new Value<Long>(value, DataType.LONG);
    }

    public static Value<Boolean> newBooleanValue(boolean value) {
        return new Value<Boolean>(value, DataType.BOOLEAN);
    }

    public static Value<Float> newFloatValue(float value) {
        return new Value<Float>(value, DataType.FLOAT);
    }

    public static Value<Double> newDoubleValue(double value) {
        return new Value<Double>(value, DataType.DOUBLE);
    }

    public static Value<Date> newDateValue(Date value) {
        return new Value<Date>(value, DataType.TIME);
    }

    public static Value<byte[]> newBinaryValue(byte[] value) {
        return new Value<byte[]>(value, DataType.OPAQUE);
    }

    @Override
    public String toString() {
        return String.format("Value [value=%s, type=%s]", value, type);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        Value<?> other = (Value<?>) obj;
        if (type != other.type)
            return false;
        if (value == null) {
            return other.value == null;
        } else {
            return type == DataType.OPAQUE ? Arrays.equals((byte[])value, (byte[])other.value) : value.equals(other.value);
        }
    }

}
