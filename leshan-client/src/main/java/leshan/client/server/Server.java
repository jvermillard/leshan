package leshan.client.server;

import java.net.InetSocketAddress;

/**
 * A LW-M2M server as perceived from the client.
 */
public class Server {

	private final InetSocketAddress serverAddress;
	private final String clientEndpoint;
	private final int timeoutMs;
	private final InetSocketAddress clientAddress;
	private final String clientLocation;
	
	public Server(final String clientEndpoint, final InetSocketAddress serverAddress, final int timeoutMs, final InetSocketAddress clientAddress) {
		this(clientEndpoint, serverAddress, timeoutMs, clientAddress, null);
		
	}
	
	public Server(final String clientEndpoint, final InetSocketAddress serverAddress, final int timeoutMs, final InetSocketAddress clientAddress, final String clientLocation) {
		this.clientEndpoint = clientEndpoint;
		this.serverAddress = serverAddress;
		this.timeoutMs = timeoutMs;
		this.clientAddress = clientAddress;
		this.clientLocation = clientLocation;
	}
	
	public String getClientEndpoint() {
		return clientEndpoint;
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
	
	public String getLocation() {
		return clientLocation;
	}

}
