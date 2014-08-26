package leshan.client.lwm2m.factory;

import java.net.InetSocketAddress;

import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.manage.ManageDownlink;
import leshan.client.lwm2m.register.RegisterUplink;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

public class ClientFactory {

	public BootstrapUplink startBootstrap(final int port, final InetSocketAddress destination, final BootstrapDownlink downlink){
		final BootstrapUplink uplink = new BootstrapUplink(destination, new CoAPEndpoint(port), downlink);
		
		return uplink;
	}
	
	public RegisterUplink startRegistration(final int port, final InetSocketAddress destination, final ManageDownlink downlink){
		final RegisterUplink uplink = new RegisterUplink(destination, new CoAPEndpoint(port), downlink);
		
		return uplink;
	}
}
