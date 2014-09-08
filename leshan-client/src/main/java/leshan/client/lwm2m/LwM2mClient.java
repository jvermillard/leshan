package leshan.client.lwm2m;

import java.net.InetSocketAddress;

import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.californium.ClientObject;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.client.lwm2m.resource.LinkFormattable;
import leshan.client.lwm2m.resource.LwM2mObjectDefinition;
import leshan.server.lwm2m.client.LinkObject;
import leshan.server.lwm2m.linkformat.LinkFormatParser;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;
import ch.ethz.inf.vs.californium.server.Server;
import ch.ethz.inf.vs.californium.server.resources.Resource;

public class LwM2mClient {

	private final Server clientSideServer;

	public LwM2mClient(final LwM2mObjectDefinition... defs) {
		this(new Server(), defs);
	}

	public LwM2mClient(final Server server, final LwM2mObjectDefinition... defs) {
		if(defs == null || defs.length == 0){
			throw new IllegalArgumentException("LWM2M Clients must support minimum required Objects defined in the LWM2M Specification.");
		}
		server.setMessageDeliverer(new LwM2mServerMessageDeliverer(server.getRoot()));
		clientSideServer = server;

		for (final LwM2mObjectDefinition def : defs) {
			if(clientSideServer.getRoot().getChild(Integer.toString(def.getId())) != null){
				throw new IllegalArgumentException("Trying to load Client Object of name '" + def.getId() + "' when one was already added.");
			}

			clientSideServer.add(new ClientObject(def.getId(), def.getDefinitions()));
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

	public RegisterUplink startRegistration(final int port, final InetSocketAddress destination){
		CoAPEndpoint endpoint = (CoAPEndpoint) clientSideServer.getEndpoint(port);
		if(endpoint == null){
			endpoint = new CoAPEndpoint(port);
		}

		clientSideServer.addEndpoint(endpoint);
		clientSideServer.start();

		return new RegisterUplink(destination, endpoint, this);
	}

	public LinkObject[] getObjectModel(final Integer...ids){
		if(ids.length > 3){
			throw new IllegalArgumentException("An Object Model Only Goes 3 levels deep:  Object ID/ObjectInstance ID/Resource ID");
		}

		if(ids.length == 0){
			final StringBuilder registrationMasterLinkObject = new StringBuilder();
			for(final Resource clientObject : clientSideServer.getRoot().getChildren()){
				if(clientObject instanceof LinkFormattable){
					registrationMasterLinkObject.append(((LinkFormattable) clientObject).asLinkFormat()).append(",");
				}
			}

			registrationMasterLinkObject.deleteCharAt(registrationMasterLinkObject.length() - 1);

			return LinkFormatParser.parse(registrationMasterLinkObject.toString().getBytes());
		}

		final Resource clientObject = clientSideServer.getRoot().getChild(Integer.toString(ids[0]));

		if(clientObject == null){
			return new LinkObject[]{};
		}
		else if(ids.length == 1){
			return LinkFormatParser.parse(((LinkFormattable) clientObject).asLinkFormat().getBytes());
		}

		final Resource clientObjectInstance = clientObject.getChild(Integer.toString(ids[1]));

		if(clientObjectInstance == null){
			return new LinkObject[]{};
		}
		else if(ids.length == 2){
			return LinkFormatParser.parse(((LinkFormattable) clientObjectInstance).asLinkFormat().getBytes());
		}

		final Resource clientResource = clientObjectInstance.getChild(Integer.toString(ids[2]));

		if(clientResource == null){
			return new LinkObject[]{};
		}

		return LinkFormatParser.parse(((LinkFormattable) clientResource).asLinkFormat().getBytes());
	}

}
