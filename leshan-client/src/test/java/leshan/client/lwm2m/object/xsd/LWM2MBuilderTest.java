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
package leshan.client.lwm2m.object.xsd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

public class LWM2MBuilderTest {

	private static final String LWM2M_ACCESS_CONTROL_V1_0_XML = "LWM2M_Device-v1_0.xml";
	private static final String LWM2M_CONNECTIVITY_MONITORING_V1_0_XML = "LWM2M_Connectivity_Monitoring-v1_0.xml";
	private static final String LWM2M_CONNECTIVITY_STATISTICS_V1_0_XML = "LWM2M_Connectivity_Statistics-v1_0.xml";
	private static final String LWM2M_DEVICE_V1_0_XML = "LWM2M_Device-v1_0.xml";
	private static final String LWM2M_FIRMWARE_UPDATE_V1_0_XML = "LWM2M_Firmware_Update-v1_0.xml";
	private static final String LWM2M_LOCATION_V1_0_XML = "LWM2M_Device-v1_0.xml";
	private static final String LWM2M_SECURITY_V1_0_XML = "LWM2M_Security-v1_0.xml";
	private static final String LWM2M_SERVER_V1_0_XML = "LWM2M_Server-v1_0.xml";

	private static final String[] XML_FILES = {
		LWM2M_ACCESS_CONTROL_V1_0_XML,
		LWM2M_CONNECTIVITY_MONITORING_V1_0_XML,
		LWM2M_CONNECTIVITY_STATISTICS_V1_0_XML,
		LWM2M_DEVICE_V1_0_XML,
		LWM2M_FIRMWARE_UPDATE_V1_0_XML,
		LWM2M_LOCATION_V1_0_XML,
		LWM2M_SECURITY_V1_0_XML,
		LWM2M_SERVER_V1_0_XML
	};

	private static final String LWM2M_MODEL_DIRECTORY = "src/main/resources/schemas/";
	private static final String LWM2M_TEST_MODEL_DIRECTORY = "src/test/resources/catalog/";

	@Test
	public void testBuildModels() throws IOException, JAXBException {
		for(final String xml : XML_FILES) {
			final LWM2M deviceModel = LWM2MBuilder.create(LWM2M_MODEL_DIRECTORY + xml);

			assertNotEquals(deviceModel, LWM2MBuilder.EMPTY);

			//printOutModel(deviceModel);
		}
	}



	@Test(expected=IllegalArgumentException.class)
	public void testExceptionOnIllegalFile(){
		LWM2MBuilder.create(LWM2M_MODEL_DIRECTORY + "aa;lskdjfa;sldkj");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testExceptionOnNullFile(){
		final String str = null;
		LWM2MBuilder.create(str);
	}

	@Test
	public void testIllegalModelReturnsEmpty(){
		final LWM2M deviceModel = LWM2MBuilder.create(LWM2M_TEST_MODEL_DIRECTORY + LWM2M_DEVICE_V1_0_XML);

		assertEquals(deviceModel, LWM2MBuilder.EMPTY);
	}

}
