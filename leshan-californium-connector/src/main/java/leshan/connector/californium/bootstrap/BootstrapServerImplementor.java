package leshan.connector.californium.bootstrap;

import leshan.server.lwm2m.bootstrap.BootstrapStore;
import leshan.server.lwm2m.impl.bridge.bootstrap.BootstrapImplementor;
import leshan.server.lwm2m.security.SecurityStore;

import org.eclipse.californium.core.CoapServer;


public class BootstrapServerImplementor implements BootstrapImplementor {
	
	private CoapServer coapServer;
	private BootstrapStore bootstrapStore;
	private SecurityStore secutiryStore;

	public BootstrapServerImplementor(CoapServer coapServer, BootstrapStore bootstrapStore, SecurityStore secutiryStore) {
			this.coapServer = coapServer;
			this.bootstrapStore = bootstrapStore;
			this.secutiryStore = secutiryStore;
	}

	@Override
	public SecurityStore getSecurityStore() {
		return secutiryStore;
	}

	@Override
	public BootstrapStore getBootstrapStore() {
		return bootstrapStore;
	}

	@Override
	public void start() {
		coapServer.start();
	}

	@Override
	public void stop() {
		coapServer.stop();
	}

	@Override
	public void destroy() {
		coapServer.destroy();
	}

}
