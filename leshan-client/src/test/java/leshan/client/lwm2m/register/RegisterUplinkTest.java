package leshan.client.lwm2m.register;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.InterfaceTypes;
import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.OperationTypes;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.client.lwm2m.util.ResponseCallback;
import leshan.server.lwm2m.client.LinkObject;

import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/*
 * Requirements for this part of the interface can be found on Table 3.
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterUplinkTest {
	private static final String LOCATION = "/LOCATION";
	private static final int SYNC_TIMEOUT_MS = 2000;
	private static final String SERVER_HOST = "localhost";
	private static final int SERVER_PORT = 1234;
	private static final String ENDPOINT_NAME = UUID.randomUUID().toString();
	private String actualResponseLocation;
	private String actualRequest;
	private Code actualCode;
	private ResponseCallback callback;

	@Mock
	private CoAPEndpoint endpoint;
	@Mock
	private LwM2mClient client;

	private String expectedRequestRoot;
	private Map<String, String> validMap;
	private OperationResponse operationResponse;
	private final String VALID_REQUEST_PAYLOAD = "</lwm2m>, </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";
	private final String INVALID_REQUEST_PAYLOAD = "";
	private Object actualRequestPayload;
	private InetSocketAddress serverAddress;
	private RegisterUplink uplink;
	private int tearDownEndpointStops;

	@Before
	public void setUp() {
		callback = new ResponseCallback();
		serverAddress = InetSocketAddress.createUnresolved(SERVER_HOST, SERVER_PORT);
		expectedRequestRoot = "coap://" + serverAddress.getHostString() + ":" + serverAddress.getPort() + "/rd?ep=" + ENDPOINT_NAME;

		validMap = new HashMap<String, String>();
		validMap.put("lt", "1000000");
		validMap.put("lwm2m", "1.1");
		validMap.put("b", "U");
		

	}

	@After
	public void tearDown(){
		uplink.stop();

		verify(endpoint, times(tearDownEndpointStops)).stop();
	}

	private void initializeServerResponse(final InterfaceTypes interfaceType, final OperationTypes operationType, final ResponseCode responseCode, final String objectsAndInstances) {
		tearDownEndpointStops = 1;
		
		if(objectsAndInstances != null){
			Mockito.when(client.getObjectModel()).thenReturn(LinkObject.parse(objectsAndInstances.getBytes()));
		}

		doAnswer(new Answer<Void>(){

			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Request request = (Request) invocation.getArguments()[0];
				actualRequest = request.getURI();
				actualCode = request.getCode();
				actualRequestPayload = request.getPayloadString();

				final Response response = new Response(responseCode);
				response.getOptions().setLocationPath(LOCATION.substring(1));

				request.setResponse(response);

				return null;
			}
		}).when(endpoint).sendRequest(any(Request.class));

		uplink = new RegisterUplink(serverAddress, endpoint, client);
	}

	private void sendRegisterAndGetAsyncResponse(final RegisterUplink uplink, final String payload) {
		sendRegisterAndGetAsyncResponse(uplink, new HashMap<String, String>(),  payload);
	}

	private void sendRegisterAndGetAsyncResponse(final RegisterUplink uplink, final Map<String, String> parameters, final String payload) {
		uplink.register(ENDPOINT_NAME, parameters, callback);

		await().untilTrue(callback.isCalled());
		if(callback.isSuccess()){
			actualResponseLocation = callback.getResponse().getLocation();
		}
	}

	private void sendRegisterAndGetSyncResponse(final RegisterUplink uplink, final String payload) {
		sendRegisterAndGetSyncResponse(uplink, new HashMap<String,String>(), payload);
	}

	private void sendRegisterAndGetSyncResponse(final RegisterUplink uplink, final Map<String, String> parameters, final String payload) {
		operationResponse = uplink.register(ENDPOINT_NAME, parameters, SYNC_TIMEOUT_MS);
		if(operationResponse.isSuccess()){
			actualResponseLocation = operationResponse.getLocation();
		}
	}

	private void verifyResponse(final String expectedResponseLocation, final String expectedRequest) {
		assertEquals(expectedRequest, actualRequest);
		assertEquals(Code.POST, actualCode);
		assertEquals(expectedResponseLocation, actualResponseLocation);
	}

	@Test
	public void testAsyncGoodRegistration() {
		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED, VALID_REQUEST_PAYLOAD);

		sendRegisterAndGetAsyncResponse(uplink, VALID_REQUEST_PAYLOAD);

		verifyRequest(VALID_REQUEST_PAYLOAD);
		verifyResponse(LOCATION, expectedRequestRoot);
	}


	@Test
	public void testAsyncBadRegistration() {
		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.BAD_REQUEST, VALID_REQUEST_PAYLOAD);

		sendRegisterAndGetAsyncResponse(uplink, VALID_REQUEST_PAYLOAD);

		verifyRequest(VALID_REQUEST_PAYLOAD);
		verifyResponse(null, expectedRequestRoot);
	}

	@Test
	public void testSyncGoodRegistration(){
		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED, VALID_REQUEST_PAYLOAD);

		sendRegisterAndGetSyncResponse(uplink, VALID_REQUEST_PAYLOAD);

		verifyRequest(VALID_REQUEST_PAYLOAD);
		verifyResponse(LOCATION, expectedRequestRoot);
	}

	@Test
	public void testSyncBadRegistration() {
		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.BAD_REQUEST, VALID_REQUEST_PAYLOAD);

		sendRegisterAndGetSyncResponse(uplink, VALID_REQUEST_PAYLOAD);

		verifyRequest(VALID_REQUEST_PAYLOAD);
		verifyResponse(null, expectedRequestRoot);
	}

	@Test
	public void testSyncBadNullParametersRegistration(){
		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED, INVALID_REQUEST_PAYLOAD);

		sendRegisterAndGetSyncResponse(uplink, null, INVALID_REQUEST_PAYLOAD);

		assertNull(actualResponseLocation);
		assertFalse(operationResponse.isSuccess());
		assertEquals(operationResponse.getResponseCode(), ResponseCode.BAD_REQUEST);
	}

	@Test
	public void testSyncBadSmsOneParametersRegistration(){
		final Map<String, String> invalidSmsMap = new HashMap<String, String>();
		invalidSmsMap.put("sms", UUID.randomUUID().toString());

		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED, INVALID_REQUEST_PAYLOAD);

		sendRegisterAndGetSyncResponse(uplink, invalidSmsMap, INVALID_REQUEST_PAYLOAD);

		assertNull(actualResponseLocation);
		assertFalse(operationResponse.isSuccess());
		assertEquals(operationResponse.getResponseCode(), ResponseCode.BAD_REQUEST);
	}

	@Test
	public void testSyncBadSmsTwoParametersRegistration(){
		final Map<String, String> invalidSmsMap = new HashMap<String, String>();
		invalidSmsMap.put("b", "US");

		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED, INVALID_REQUEST_PAYLOAD);

		sendRegisterAndGetSyncResponse(uplink, invalidSmsMap, INVALID_REQUEST_PAYLOAD);

		assertNull(actualResponseLocation);
		assertFalse(operationResponse.isSuccess());
		assertEquals(operationResponse.getResponseCode(), ResponseCode.BAD_REQUEST);
	}

	@Test
	public void testSyncBadQueueParametersRegistration(){
		final HashMap<String, String> invalidQueueMap = new HashMap<String, String>();
		invalidQueueMap.put("b", "UQ");

		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED, INVALID_REQUEST_PAYLOAD);

		sendRegisterAndGetSyncResponse(uplink, invalidQueueMap, INVALID_REQUEST_PAYLOAD);

		assertNull(actualResponseLocation);
		assertFalse(operationResponse.isSuccess());
		assertEquals(operationResponse.getResponseCode(), ResponseCode.BAD_REQUEST);
	}

	@Test
	public void testSyncBadInvalidParametersRegistration(){
		final HashMap<String, String> invalidIllegalMap = new HashMap<String, String>();
		invalidIllegalMap.put("b", "X");
		invalidIllegalMap.put("InvalidKey", "Lulz");

		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED, INVALID_REQUEST_PAYLOAD);

		sendRegisterAndGetSyncResponse(uplink, invalidIllegalMap, INVALID_REQUEST_PAYLOAD);

		assertNull(actualResponseLocation);
		assertFalse(operationResponse.isSuccess());
		assertEquals(operationResponse.getResponseCode(), ResponseCode.BAD_REQUEST);
	}

	@Test
	public void testSyncGoodAllParametersSyncRegistration(){
		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED, VALID_REQUEST_PAYLOAD);

		final String validQuery = leshan.client.lwm2m.request.Request.toQueryStringMap(validMap);

		sendRegisterAndGetSyncResponse(uplink, validMap, VALID_REQUEST_PAYLOAD);

		verifyRequest(VALID_REQUEST_PAYLOAD);
		verifyResponse(LOCATION, expectedRequestRoot + "&" + validQuery);
	}

	@Test
	public void testAsyncGoodParametersRegistration() {
		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED, VALID_REQUEST_PAYLOAD);
		
		final String validQuery = leshan.client.lwm2m.request.Request.toQueryStringMap(validMap);

		sendRegisterAndGetAsyncResponse(uplink, validMap, VALID_REQUEST_PAYLOAD);

		verifyRequest(VALID_REQUEST_PAYLOAD);
		verifyResponse(LOCATION, expectedRequestRoot + "&" + validQuery);
	}

	@Test
	public void testAsyncBadParametersRegistration() {
		final HashMap<String, String> invalidIllegalMap = new HashMap<String, String>();
		invalidIllegalMap.put("b", "X");
		invalidIllegalMap.put("InvalidKey", "Lulz");

		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED, VALID_REQUEST_PAYLOAD);

		sendRegisterAndGetAsyncResponse(uplink, invalidIllegalMap, VALID_REQUEST_PAYLOAD);

		assertNull(actualResponseLocation);
		assertFalse(callback.isSuccess());
		assertEquals(callback.getResponseCode(), ResponseCode.BAD_REQUEST);
	}

	@Test
	public void testSyncGoodPayloadRegistration(){
		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED, VALID_REQUEST_PAYLOAD);

		final String validQuery = leshan.client.lwm2m.request.Request.toQueryStringMap(validMap);


		sendRegisterAndGetSyncResponse(uplink, validMap, VALID_REQUEST_PAYLOAD);

		verifyRequest(VALID_REQUEST_PAYLOAD);
		verifyResponse(LOCATION, expectedRequestRoot + "&" + validQuery);
	}

	private void verifyRequest(final String expectedPayload) {
		assertEquals(expectedPayload, actualRequestPayload);
	}

	@Test
	public void testSyncBadPayloadRegistration(){
		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED, INVALID_REQUEST_PAYLOAD);

		sendRegisterAndGetSyncResponse(uplink, validMap, INVALID_REQUEST_PAYLOAD);

		assertNull(actualResponseLocation);
		assertFalse(operationResponse.isSuccess());
		assertEquals(operationResponse.getResponseCode(), ResponseCode.BAD_REQUEST);
	}


	@Test
	public void testSyncBadNullPayloadRegistration(){
		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED, null);

		sendRegisterAndGetSyncResponse(uplink, validMap, null);

		assertNull(actualResponseLocation);
		assertFalse(operationResponse.isSuccess());
		assertEquals(operationResponse.getResponseCode(), ResponseCode.BAD_REQUEST);
	}
}
