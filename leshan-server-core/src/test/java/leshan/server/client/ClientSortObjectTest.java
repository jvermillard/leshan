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

package leshan.server.client;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import leshan.LinkObject;

import org.junit.Assert;
import org.junit.Test;

public class ClientSortObjectTest {

    @Test
    public void sort_link_object_on_get() throws UnknownHostException {
        LinkObject[] objs = new LinkObject[3];
        objs[0] = new LinkObject("/0/1024/2");
        objs[1] = new LinkObject("/0/2");
        objs[2] = null;
        Client c = new Client("registrationId", "endpoint", Inet4Address.getByName("127.0.0.1"), 1, "1.0", null, null,
                null, objs, new InetSocketAddress(212));

        LinkObject[] res = c.getSortedObjectLinks();
        Assert.assertEquals(3, res.length);
        Assert.assertNull(res[0]);
        Assert.assertEquals("/0/2", res[1].getPath());
        Assert.assertEquals("/0/1024/2", res[2].getPath());
    }
}
