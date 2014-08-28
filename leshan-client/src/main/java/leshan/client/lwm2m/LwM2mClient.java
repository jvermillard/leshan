package leshan.client.lwm2m;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.manage.ManageDownlink;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.resource.ClientObject;
import leshan.client.lwm2m.resource.LinkFormattable;
import leshan.server.lwm2m.client.LinkObject;
import leshan.server.lwm2m.linkformat.LinkFormatParser;
import ch.ethz.inf.vs.californium.WebLink;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;
import ch.ethz.inf.vs.californium.server.Server;
import ch.ethz.inf.vs.californium.server.resources.Resource;

public class LwM2mClient {

	private final Server clientSideServer;
	ManageDownlink downlink;

	public LwM2mClient(final ClientObject... objs) {
		this(new Server(), objs);
	}

	public LwM2mClient(final Server server, final ClientObject... objs) {
		if(objs == null || objs.length == 0){
			throw new IllegalArgumentException("LWM2M Clients must support minimum required Objects defined in the LWM2M Specification.");
		}
		server.setMessageDeliverer(new LwM2mServerMessageDeliverer(server.getRoot()));
		clientSideServer = server;

		//		readResource = new ObjectResource(this, 1);
		for (final ClientObject obj : objs) {
			if(clientSideServer.getRoot().getChild(obj.getName()) != null){
				throw new IllegalArgumentException("Trying to load Client Object of name '" + obj.getName() + "' when one was already added.");
			}

			clientSideServer.add(obj);
		}
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

	public LinkObject[] getObjectLinks(final int objectId) {
		final Resource clientObject = clientSideServer.getRoot().getChild(Integer.toString(objectId));
		
		if(clientObject == null){
			return new LinkObject[]{};
		}
		
		return LinkFormatParser.parse(((LinkFormattable) clientObject).asLinkFormat().getBytes());
	}

	public LinkObject[] getObjectLinks(final int objectId,
			final int objectInstanceId) {
		final Resource clientObject = clientSideServer.getRoot().getChild(Integer.toString(objectId));
		
		if(clientObject == null){
			return new LinkObject[]{};
		}
		
		final Resource clientObjectInstance = clientObject.getChild(Integer.toString(objectInstanceId));
		
		return LinkFormatParser.parse(((LinkFormattable) clientObjectInstance).asLinkFormat().getBytes());
	}

}
