package leshan.client.lwm2m.factory;

import java.net.InetSocketAddress;

import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.register.RegisterDownlink;
import leshan.client.lwm2m.register.RegisterUplink;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

public class ClientFactory {

	public BootstrapUplink startBootstrap(final InetSocketAddress destination, final BootstrapDownlink downlink){
		final BootstrapUplink uplink = new BootstrapUplink(new CoAPEndpoint(destination));
		return uplink;
	}
	
	public RegisterUplink startRegistration(final InetSocketAddress destination, final RegisterDownlink downlink){
		return null;
	}
}
