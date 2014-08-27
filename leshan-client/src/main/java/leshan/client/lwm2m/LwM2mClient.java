package leshan.client.lwm2m;

import java.net.InetSocketAddress;

import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.manage.ManageDownlink;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.register.RegisterUplink;
import ch.ethz.inf.vs.californium.CoapClient;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;
import ch.ethz.inf.vs.californium.server.Server;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class LwM2mClient {

	private final Server clientSideServer;
	private final String VALID_REQUEST_PAYLOAD = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";

	public LwM2mClient() {
		clientSideServer = new Server();

		final Resource readResource = new ObjectResource(1);
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

		final String uri = "coap://" + destination.getHostString() + ":" + destination.getPort() + "/rd?ep=device1";
		final CoapClient client = new CoapClient(uri);
		System.out.println("URI " + uri);
		client.setEndpoint(endpoint);
		client.post(VALID_REQUEST_PAYLOAD, 1);

		return new RegisterUplink(destination, endpoint, downlink);
	}

}

class ObjectResource extends ResourceBase {

	private final String value;

	public ObjectResource(final int objectId) {
		super(Integer.toString(objectId));
		value = "THIS SHOULD HAVE TLV STUFF";
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		exchange.respond(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT, value);
	}

}
