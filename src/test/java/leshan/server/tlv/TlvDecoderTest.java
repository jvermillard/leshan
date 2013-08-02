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
import java.util.Arrays;

import org.apache.mina.util.ByteBufferDumper;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link TlvDecoder}
 */
public class TlvDecoderTest {
    @Test
    public void decode_device_object() {
        TlvDecoder decoder = new TlvDecoder();
        
        // the /3// from liwblwm2m
        String dataStr = "C800144F70656E204D6F62696C6520416C6C69616E6365C801164C69676874776569676874204D324D20436C69656E74C80209333435303030313233C303312E30860641000141010588070842000ED842011388870841007D42010384C10964C10A0F830B410000C40D5182428FC60E2B30323A3030C10F55";
        ByteBuffer b = ByteBufferDumper.fromHexString(dataStr);
        Tlv[] tlv = decoder.decode(b, null);
        System.err.println(Arrays.toString(tlv));
        
        TlvEncoder encoder = new TlvEncoder();
        ByteBuffer buff = encoder.encode(tlv, null);
        Assert.assertEquals(dataStr, ByteBufferDumper.toHex(buff));
    }
}
