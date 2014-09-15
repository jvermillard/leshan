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
package leshan.server.lwm2m.impl.objectspec;

/**
 * A resource description
 */
public class ResourceSpec {

    public enum Operations {
        NONE, R, W, RW, E, RE, WE, RWE
    }

    public enum Type {
        STRING, INTEGER, FLOAT, BOOLEAN, OPAQUE, TIME
    }

    public final int id;
    public final String name;
    public final Operations operations;
    public final boolean multiple;
    public final boolean mandatory;
    public final Type type;
    public final String rangeEnumeration;
    public final String units;
    public final String description;

    ResourceSpec(int id, String name, Operations operations, boolean multiple, boolean mandatory, Type type,
            String rangeEnumeration, String units, String description) {
        this.id = id;
        this.name = name;
        this.operations = operations;
        this.multiple = multiple;
        this.mandatory = mandatory;
        this.type = type;
        this.rangeEnumeration = rangeEnumeration;
        this.units = units;
        this.description = description;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ResourceDesc [id=").append(id).append(", name=").append(name).append(", operations=")
                .append(operations).append(", multiple=").append(multiple).append(", mandatory=").append(mandatory)
                .append(", type=").append(type).append(", rangeEnumeration=").append(rangeEnumeration)
                .append(", units=").append(units).append(", description=").append(description).append("]");
        return builder.toString();
    }

}
