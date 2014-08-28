package leshan.client.lwm2m.object.xsd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.util.List;

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
		LWM2MBuilder.create(null);
	}

	@Test
	public void testIllegalModelReturnsEmpty(){
		final LWM2M deviceModel = LWM2MBuilder.create(LWM2M_TEST_MODEL_DIRECTORY + LWM2M_DEVICE_V1_0_XML);

		assertEquals(deviceModel, LWM2MBuilder.EMPTY);
	}

	private void printOutModel(final LWM2M deviceModel) {
		final List<Object> object = deviceModel.getObject();
		for(final Object o : object){
			System.out.println("Object '" + o.getObjectID() + "' '" + o.getDescription1() + "'");
			for (final Item i : o.getResources().getItem()) {
				System.out.println("\tResource Item " + i.getID() + " '" + i.getDescription() + "'" + i.getRangeEnumeration());
			}
		}

		System.out.println("+++++++++++++++++++++");
	}

}
