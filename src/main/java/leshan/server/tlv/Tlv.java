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
package leshan.server.tlv;

import java.util.Arrays;

/**
 * a Type-Length-Value container, can contain more TLV.
 */
public class Tlv {
   
    /**
     * build a TLV container
     * @param type the type of TLV
     * @param children the list of children TLV (must be <code>null</code> for type {@link TlvType#RESOURCE_VALUE} 
     * @param value the raw contained value, only for type {@link TlvType#RESOURCE_VALUE} <code>null</code> for the other types
     * @param identifier the TLV identifier (resource id, instance id,..)
     */
    public Tlv(TlvType type, Tlv[] children, byte[] value, int identifier) {
        super();
        this.type = type;
        this.children = children;
        this.value = value;
        this.identifier = identifier;
        
        if (type == TlvType.RESOURCE_VALUE || type == TlvType.RESOURCE_INSTANCE) {
            if (value  == null) {
                throw new IllegalArgumentException("a "+type.name()+" must have a value");
            } else if (children != null) {
                throw new IllegalArgumentException("a "+type.name()+" can't have children");
            }
        } else {
            if (value  != null) {
                throw new IllegalArgumentException("a "+type.name()+" can't have a value");
            } else if (children == null) {
                throw new IllegalArgumentException("a "+type.name()+" must have children");
            }
        }
    }

    // type of TLV, indicate if it's containing a value or TLV containing more TLV or values
    private TlvType type;
    
    // if of type OBJECT_INSTANCE,MULTIPLE_RESOURCE or null
    private Tlv[] children;
    
    // if type RESOURCE_VALUE or RESOURCE_INSTANCE => null
    private byte[] value;

    private int identifier;
    
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
        return "Tlv [type=" + type + ", children=" + Arrays.toString(children) + ", value=" + Arrays.toString(value)
                + ", identifier=" + identifier + "]";
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
