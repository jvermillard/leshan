package leshan.client.lwm2m;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.TreeSet;

import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.manage.ManageDownlink;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.resource.ClientObject;
import leshan.client.lwm2m.resource.ObjectResource;
import ch.ethz.inf.vs.californium.WebLink;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;
import ch.ethz.inf.vs.californium.server.Server;
import ch.ethz.inf.vs.californium.server.resources.Resource;

public class LwM2mClient {

	private final Server clientSideServer;
	private final String VALID_REQUEST_PAYLOAD = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";
	ManageDownlink downlink;

	public LwM2mClient(final ClientObject... objs) {
		this(new Server(), objs);
	}

	public LwM2mClient(final Server server, final ClientObject... objs) {
		if(objs == null || objs.length == 0){
			throw new IllegalArgumentException("LWM2M Clients must support minimum required Objects defined in the LWM2M Specification.");
		}
		clientSideServer = server;

		//		readResource = new ObjectResource(this, 1);
		for (final ClientObject obj : objs) {
			final Resource newResource = clientObjectToResource(obj);
			
			if(clientSideServer.getRoot().getChild(newResource.getName()) != null){
				throw new IllegalArgumentException("Trying to load Client Object of name '" + newResource.getName() + "' when one was already added.");
			}
			
			clientSideServer.add(newResource);
		}
	}

	private Resource clientObjectToResource(final ClientObject obj) {
		return new ObjectResource(obj);
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

		final Set<WebLink> objectsAndInstances = new TreeSet<WebLink>();
		for (final Resource resource : clientSideServer.getRoot().getChildren()) {
			objectsAndInstances.add(new WebLink(resource.getURI()));
		}
		return new RegisterUplink(destination, endpoint, downlink, objectsAndInstances );
	}

}
