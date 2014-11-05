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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import leshan.util.Validate;

/**
 * The top level element in the LWM2M resource tree.
 * <p>
 * An Objects defines a grouping of Resources and may consist of multiple instances.
 * </p>
 */
public class LwM2mObject implements LwM2mNode {

    private int id;

    private final Map<Integer, LwM2mObjectInstance> instances;

    public LwM2mObject(int id, LwM2mObjectInstance[] instances) {
        Validate.notNull(instances);

        this.id = id;
        this.instances = new HashMap<>(instances.length);
        for (LwM2mObjectInstance instance : instances) {
            this.instances.put(instance.getId(), instance);
        }
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
     * Returns a map of object intances by id.
     *
     * @return the instances
     */
    public Map<Integer, LwM2mObjectInstance> getInstances() {
        return Collections.unmodifiableMap(instances);
    }

    @Override
    public String toString() {
        return String.format("LwM2mObject [id=%s, instances=%s]", id, instances);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((instances == null) ? 0 : instances.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LwM2mObject other = (LwM2mObject) obj;
        if (id != other.id) {
            return false;
        }
        if (instances == null) {
            if (other.instances != null) {
                return false;
            }
        } else if (!instances.equals(other.instances)) {
            return false;
        }
        return true;
    }

}
