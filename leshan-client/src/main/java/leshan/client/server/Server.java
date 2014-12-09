package leshan.client.server;

import java.net.InetSocketAddress;

/**
 * A LW-M2M server as perceived from the client.
 */
public class Server {

	private final InetSocketAddress serverAddress;
	private final int timeoutMs;
	private final InetSocketAddress clientAddress;
	
	public Server(final InetSocketAddress serverAddress, final int timeoutMs, final InetSocketAddress clientAddress) {
		this.serverAddress = serverAddress;
		this.timeoutMs = timeoutMs;
		this.clientAddress = clientAddress;
	}
	
	public InetSocketAddress getServerAddress() {
		return serverAddress;
	}
	
	public int getTimeoutMs() {
		return timeoutMs;
	}

	public InetSocketAddress getClientAddress() {
		return clientAddress;
	}

}
