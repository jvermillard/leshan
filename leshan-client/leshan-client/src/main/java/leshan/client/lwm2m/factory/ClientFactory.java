package leshan.client.lwm2m.factory;

import leshan.client.lwm2m.BootstrapDownlink;
import leshan.client.lwm2m.BootstrapUplink;
import leshan.client.lwm2m.RegisterDownlink;
import leshan.client.lwm2m.RegisterUplink;

public class ClientFactory {

	public BootstrapUplink startBootstrap(final BootstrapDownlink downlink){
		return null;
	}
	
	public RegisterUplink startRegistration(final RegisterDownlink downlink){
		return null;
	}
}
