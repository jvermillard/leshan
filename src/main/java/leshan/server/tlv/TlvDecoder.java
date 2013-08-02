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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.apache.mina.codec.StatelessProtocolDecoder;

public class TlvDecoder implements StatelessProtocolDecoder<ByteBuffer, Tlv[]> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Void createDecoderState() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tlv[] decode(ByteBuffer input, Void context) {
        List<Tlv> tlvs = new ArrayList<>();
        
        while(input.remaining()>0) {
            input.order(ByteOrder.BIG_ENDIAN);
            int typeByte = input.get() & 0xFF;
            TlvType type;
            switch (typeByte & 0b1100_0000) {
            case 0b0000_0000:
                type = TlvType.OBJECT_INSTANCE;
                break;
            case 0b0100_0000:
                type = TlvType.RESOURCE_INSTANCE;
                break;
            case 0b1000_0000:
                type = TlvType.MULTIPLE_RESOURCE;
                break;
            case 0b1100_0000:
                type = TlvType.RESOURCE_VALUE;
                break;
            default:
                throw new IllegalStateException("unknown type : " + (typeByte & 0b1100_0000));
            }
    
            int identifier;
            // decode identifier
            if ((typeByte & 0b0010_0000) == 0) {
                identifier = input.get() & 0xFF;
            } else {
                identifier = input.getShort() & 0xFFFF;
            }
    
            int length;
            int lengthType = typeByte & 0b0001_1000;
    
            // decode length
            switch (lengthType) {
            case 0b0000_0000:
                // 2 bit length
                length = typeByte & 0b0000_0111;
                break;
            case 0b0000_1000:
                // 8 bit length
                length = input.get() & 0xFF;
                break;
            case 0b0001_0000:
                // 16 bit length 
                length = input.getShort() & 0xFFFF;
                break;
            case 0b0001_1000:
                // 24 bit length
                length = ((input.get() & 0xFF) << 16) + input.getShort() & 0xFFFF;
                break;
            default:
                throw new IllegalStateException("unknown length type : " + (typeByte & 0b0001_1000));
            }
    
            if (type == TlvType.RESOURCE_VALUE || type ==  TlvType.RESOURCE_INSTANCE) {
                byte[] payload = new byte[length];
                input.get(payload);
                tlvs.add( new Tlv(type, null, payload, identifier));
            } else {
                // create a view of the contained TLVs
                ByteBuffer slice = input.slice();
                slice.limit(length);
                
                Tlv[] children = decode(slice, null);

                // skip the children, it will be decoded by the view
                input.position(input.position()+length);
   
                Tlv tlv = new Tlv(type, children, null, identifier);
                tlvs.add(tlv);
            }
        }
        
        return tlvs.toArray(new Tlv[]{});
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void finishDecode(Void context) {
    }
}