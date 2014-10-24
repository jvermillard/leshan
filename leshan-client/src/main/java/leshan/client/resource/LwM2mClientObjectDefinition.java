/*
 * Copyright (c) 2013, Sierra Wireless,
 * Copyright (c) 2014, Zebra Technologies,
 * 
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
package leshan.client.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import leshan.tlv.Tlv;

public class LwM2mClientObjectDefinition {

    private final int id;
    private final Map<Integer, LwM2mClientResourceDefinition> defMap;
    private final boolean isMandatory;
    private final boolean isSingle;

    public LwM2mClientObjectDefinition(final int objectId, final boolean isMandatory, final boolean isSingle,
            final LwM2mClientResourceDefinition... definitions) {
        this.id = objectId;
        this.isMandatory = isMandatory;
        this.isSingle = isSingle;

        this.defMap = mapFromResourceDefinitions(definitions);
    }

    private Map<Integer, LwM2mClientResourceDefinition> mapFromResourceDefinitions(
            final LwM2mClientResourceDefinition[] definitions) {
        final Map<Integer, LwM2mClientResourceDefinition> map = new HashMap<Integer, LwM2mClientResourceDefinition>();
        for (final LwM2mClientResourceDefinition def : definitions) {
            map.put(def.getId(), def);
        }

        return map;
    }

    public int getId() {
        return id;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public boolean isSingle() {
        return isSingle;
    }

    public boolean hasAllRequiredResourceIds(final Tlv[] tlvs) {
        final Set<Integer> resourceIds = new HashSet<>();
        for (final Tlv tlv : tlvs) {
            resourceIds.add(tlv.getIdentifier());
        }
        for (final LwM2mClientResourceDefinition def : defMap.values()) {
            if (def.isRequired() && !resourceIds.contains(def.getId())) {
                return false;
            }
        }
        return true;
    }

    public LwM2mClientResourceDefinition getResourceDefinition(final int identifier) {
        return defMap.get(identifier);
    }

    public Collection<LwM2mClientResourceDefinition> getResourceDefinitions() {
        return new ArrayList<>(defMap.values());
    }

}
