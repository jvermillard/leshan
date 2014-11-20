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
package leshan.tlv;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for {@link TlvDecoder}
 */
public class TlvDecoderTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void decode_device_object() throws TlvException {
        // // the /3// from liwblwm2m
        String dataStr = "C800144F70656E204D6F62696C6520416C6C69616E6365C801164C69676874776569676874204D324D20436C69656E74C80209333435303030313233C303312E30860641000141010588070842000ED842011388870841007D42010384C10964C10A0F830B410000C40D5182428FC60E2B30323A3030C10F55";
        byte[] bytes = hexStringToByteArray(dataStr);
        ByteBuffer b = ByteBuffer.wrap(bytes);
        Tlv[] tlv = TlvDecoder.decode(b);
        log.debug(Arrays.toString(tlv));

        ByteBuffer buff = TlvEncoder.encode(tlv);
        Assert.assertTrue(Arrays.equals(bytes, buff.array()));
    }

    protected byte[] hexStringToByteArray(String hexString) {

        if (hexString.length() % 2 > 0) {
            throw new IllegalArgumentException("Hex String must have even number of chars");
        }

        byte[] bytes = new byte[hexString.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            int currentByteIdx = i * 2;
            bytes[i] = (byte) Integer.parseInt(hexString.substring(currentByteIdx, currentByteIdx + 2), 16);
            ;
        }
        return bytes;
    }
    
    @Test
    public void testHexStringToByteArray()
    {
        String   dataStr = "C800144F70656E204D6F62696C6520416C6C69616E6365C801164C69676874776569676874204D324D20436C69656E74C80209333435303030313233C303312E30860641000141010588070842000ED842011388870841007D42010384C10964C10A0F830B410000C40D5182428FC60E2B30323A3030C10F55";
        byte[] dataBytes1 = com.sun.org.apache.xerces.internal.impl.dv.util.HexBin.decode((String) dataStr);
        byte[] dataBytes2 = javax.xml.bind.DatatypeConverter.parseHexBinary(dataStr);
        byte[] dataBytes3 = hexStringToByteArray(dataStr);
        Assert.assertArrayEquals(dataBytes1, dataBytes3);
        Assert.assertArrayEquals(dataBytes1, dataBytes2);
    }
}
