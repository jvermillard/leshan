package leshan.client.lwm2m;

import java.net.InetSocketAddress;

import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.manage.ManageDownlink;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.response.OperationResponse;

import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class LwM2mClient {

	private final Server clientSideServer;
	private final String VALID_REQUEST_PAYLOAD = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";
	ManageDownlink downlink;
	private final Resource readResource;

	public LwM2mClient() {
		this(new Server());
	}

	public LwM2mClient(final Server server, final ClientObject... objs) {
		if(objs == null || objs.length == 0){
			throw new IllegalArgumentException("LWM2M Clients must support minimum required Objects defined in the LWM2M Specification.");
		}
		clientSideServer = server;

		readResource = new ObjectResource(this, 1);
		clientSideServer.add(readResource);
	}

	public void start() {
		clientSideServer.start();
	}

	public void stop() {
		clientSideServer.stop();
	}

	public BootstrapUplink startBootstrap(final int port, final InetSocketAddress destination, final BootstrapDownlink downlink){
		final BootstrapUplink uplink = new BootstrapUplink(destination, new CoAPEndpoint(port), downlink);

		return uplink;
	}

	public RegisterUplink startRegistration(final int port, final InetSocketAddress destination, final ManageDownlink downlink){
		final CoAPEndpoint endpoint = new CoAPEndpoint(port);
		clientSideServer.addEndpoint(endpoint);
		clientSideServer.start();

		this.downlink = downlink;

//		final String uri = "coap://" + destination.getHostString() + ":" + destination.getPort() + "/rd?ep=device1";
//		final CoapClient client = new CoapClient(uri);
//		System.out.println("URI " + uri);
//		client.setEndpoint(endpoint);
//		client.post(VALID_REQUEST_PAYLOAD, 1);

		return new RegisterUplink(destination, endpoint, downlink);
	}

}

class ObjectResource extends ResourceBase {

	private final String value;
	private final LwM2mClient lwM2mClient;

	public ObjectResource(final LwM2mClient lwM2mClient, final int objectId) {
		super(Integer.toString(objectId));
		this.lwM2mClient = lwM2mClient;
		value = "THIS SHOULD HAVE TLV STUFF";
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		final OperationResponse read = lwM2mClient.downlink.read(Integer.parseInt(getName()));
		if(read.isSuccess()){
			System.out.println("Successful Read!");
			exchange.respond(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT, read.getPayload());
		}
		else{
			System.out.println("Failed Read!");
			exchange.respond(read.getResponseCode(), read.getPayload());
		}
	}

}
