package leshan.client.lwm2m.object.xsd;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import leshan.client.lwm2m.object.xsd.LWM2M;
import leshan.client.lwm2m.object.xsd.LWM2M.Object;
import leshan.client.lwm2m.object.xsd.LWM2M.Object.Resources.Item;
import leshan.client.lwm2m.object.xsd.LWM2MBuilder;

import org.junit.Assert;
import org.junit.Test;

public class LWM2MBuilderTest {

	private static final String LWM2M_DEVICE_V1_0_XML = "LWM2M_Device-v1_0.xml";
	private static final String LWM2M_MODEL_DIRECTORY = "src/main/resources/catalog/";
	private static final String LWM2M_TEST_MODEL_DIRECTORY = "src/test/resources/catalog/";

	@Test
	public void testBuildDeviceModel() throws IOException, JAXBException {
		final LWM2M deviceModel = LWM2MBuilder.create(LWM2M_MODEL_DIRECTORY + LWM2M_DEVICE_V1_0_XML);
		
		assertNotEquals(deviceModel, LWM2MBuilder.EMPTY);
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
				System.out.println("\tResource Item " + i.getID() + " '" + i.getDescription() + "'");
			}
		}
	}
	
}
