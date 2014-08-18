package leshan.client.lwm2m.factory;

import leshan.client.lwm2m.bootstrap.BootstrapDownlink;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.register.RegisterDownlink;
import leshan.client.lwm2m.register.RegisterUplink;

public class ClientFactory {

	public BootstrapUplink startBootstrap(final BootstrapDownlink downlink){
		return null;
	}
	
	public RegisterUplink startRegistration(final RegisterDownlink downlink){
		return null;
	}
}
