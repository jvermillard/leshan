package leshan.server.integration.register.normal.register;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.manage.ManageDownlink;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.resource.ObjectResource;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.client.lwm2m.util.ResponseCallback;
import leshan.server.LeshanMain;
import leshan.server.clienttest.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;

import ch.ethz.inf.vs.californium.WebLink;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.LinkFormat;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

public class AbstractRegisteringTest {

	protected static final int TIMEOUT_MS = 5000;
	private LeshanMain server;
	protected InetSocketAddress serverAddress;
	protected String clientEndpoint;
	protected Map<String, String> clientParameters;
	protected Set<WebLink> objectsAndInstances;
	protected int clientPort;
	private final String clientDataModel = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";
	private final String[] clientDataModelResources = new String[]{
				"/lwm2m",
				"/lwm2m/1/101",
				"/lwm2m/1/102",
				"/lwm2m/2/0",
				"/lwm2m/2/1",
				"/lwm2m/2/2",
				"/lwm2m/3/0",
				"/lwm2m/4/0",
				"/lwm2m/5",
		};
	private Set<String> clientDataModelResourceSet;

	@Mock
	protected ManageDownlink downlink;

	protected ResponseCallback callback;
	protected RegisterUplink registerUplink;

	public AbstractRegisteringTest() {
		super();
	}

	@Before
	public void setUp() throws UnknownHostException {
		clientDataModelResourceSet = new HashSet<String>(Arrays.asList(clientDataModelResources));
		server = new LeshanMain();
		server.start();
		serverAddress = new InetSocketAddress(InetAddress.getLocalHost(), 5683);
		clientPort = 9000;
		clientEndpoint = UUID.randomUUID().toString();
		clientParameters = new HashMap<>();
		objectsAndInstances = LinkFormat.parse(clientDataModel);
		callback = new ResponseCallback();

		final LwM2mClient lwM2mClient = new LwM2mClient(new ObjectResource(1));
		registerUplink = lwM2mClient.startRegistration(clientPort, serverAddress, downlink);
	}

	@After
	public void tearDown() {
		server.stop();
		registerUplink.stop();
	}

	protected void validateResponsesToClient(final OperationResponse registerResponse, final String locationPathOptions,
			final OperationResponse deregisterResponse) {
				assertTrue(locationPathOptions.startsWith("/rd/"));
				assertTrue(registerResponse.isSuccess());
				assertEquals(registerResponse.getResponseCode(), ResponseCode.CREATED);
				assertTrue(deregisterResponse.isSuccess());
				assertEquals(deregisterResponse.getResponseCode(), ResponseCode.DELETED);
			}

	protected void validateRegisteredClientOnServer(final Long lifetime)
			throws UnknownHostException {
				final Gson gson = new Gson();

				final String serverKnownClientsJson = TestUtils.getAPI("api/clients");
				List<Map<String, Object>> serverKnownClients = new ArrayList<>();
				serverKnownClients = gson.fromJson(serverKnownClientsJson, serverKnownClients.getClass());
				assertEquals(1, serverKnownClients.size());

				final Map<String, Object> clientParameters = serverKnownClients.get(0);
				assertEquals(clientEndpoint, clientParameters.get("endpoint"));
				assertNotNull(clientParameters.get("registrationId"));
				assertNotNull(clientParameters.get("registrationDate"));
				assertEquals("/" + InetAddress.getLocalHost().getHostAddress() + ":" + clientPort, clientParameters.get("address"));
				assertEquals("1.0", clientParameters.get("lwM2MmVersion"));
				assertEquals(lifetime.doubleValue(), Double.parseDouble(clientParameters.get("lifetime").toString()), 0.001);

				final Collection<LinkedTreeMap> links = (Collection<LinkedTreeMap>) clientParameters.get("objectLinks");
				for (final LinkedTreeMap link : links) {
					assertTrue(clientDataModelResourceSet.contains(link.get("url")));
				}

			}

	protected void validateNoRegisteredClientOnServer() {
		final Gson gson = new Gson();

		final String serverKnownClientsJson = TestUtils.getAPI("api/clients");
		List<Map<String, Object>> serverKnownClients = new ArrayList<>();
		serverKnownClients = gson.fromJson(serverKnownClientsJson, serverKnownClients.getClass());
		assertEquals(0, serverKnownClients.size());
	}

}