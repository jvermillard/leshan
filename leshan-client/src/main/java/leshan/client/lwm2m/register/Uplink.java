package leshan.client.lwm2m.register;

import java.io.IOException;

import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

public abstract class Uplink {

	public final void checkStarted(final CoAPEndpoint endpoint) {
		if(!endpoint.isStarted()) {
			try {
				endpoint.start();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

}
