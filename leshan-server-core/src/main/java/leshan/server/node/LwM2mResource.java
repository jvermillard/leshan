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
package leshan.server.node;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * A resource is an information made available by the LWM2M Client.
 * <p>
 * A resource may have a single {@link Value} or consist of multiple instances.
 * </p>
 */
public class LwM2mResource implements LwM2mNode {

    private final int id;

    private final Value<?>[] values;

    private final boolean isMultiInstances;

    /**
     * New single instance resource
     */
    public LwM2mResource(int id, Value<?> value) {
        Validate.notNull(value);
        this.id = id;
        this.values = new Value[] { value };
        this.isMultiInstances = false;
    }

    /**
     * New multiple instances resource
     */
    public LwM2mResource(int id, Value<?>[] values) {
        Validate.notEmpty(values);
        this.id = id;
        this.values = Arrays.copyOf(values, values.length);
        this.isMultiInstances = true;
    }

    @Override
    public void accept(LwM2mNodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * @return the resource value (for a single instance resource) or the first value (for a multi-instance resource)
     */
    public Value<?> getValue() {
        return values[0];
    }

    /**
     * @return the resource values
     */
    public Value<?>[] getValues() {
        return values;
    }

    /**
     * @return <code>true</code> if this is a resource supporting multiple instances and <code>false</code> otherwise
     */
    public boolean isMultiInstances() {
        return isMultiInstances;
    }

    @Override
    public String toString() {
        return String.format("LwM2mResource [id=%s, values=%s, isMultiInstances=%s]", id, Arrays.toString(values),
                isMultiInstances);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + (isMultiInstances ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(values);
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
        LwM2mResource other = (LwM2mResource) obj;
        if (id != other.id)
            return false;
        if (isMultiInstances != other.isMultiInstances)
            return false;
        if (!Arrays.equals(values, other.values))
            return false;
        return true;
    }

}
