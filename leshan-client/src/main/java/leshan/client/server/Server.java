package leshan.client.server;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * A LW-M2M server as perceived from the client.
 */
public class Server {

	private final int clientPort;
	private final InetSocketAddress serverAddress;
	private final String clientEndpoint;
	private final Map<String, String> clientParamters;
	private final int timeoutMs;
	private final InetSocketAddress clientAddress;
	private final String clientLocation;
	
	public Server(final String clientEndpoint, final int clientPort, final Map<String, String> clientParameters, final InetSocketAddress serverAddress, final int timeoutMs, final InetSocketAddress clientAddress) {
		this(clientEndpoint, clientPort, clientParameters, serverAddress, timeoutMs, clientAddress, null);
		
	}
	
	public Server(final String clientEndpoint, final int clientPort, final Map<String, String> clientParameters, final InetSocketAddress serverAddress, final int timeoutMs, final InetSocketAddress clientAddress, final String clientLocation) {
		this.clientPort = clientPort;
		this.clientEndpoint = clientEndpoint;
		this.clientParamters = clientParameters;
		this.serverAddress = serverAddress;
		this.timeoutMs = timeoutMs;
		this.clientAddress = clientAddress;
		this.clientLocation = clientLocation;
	}
	
	public int getClientPort() {
		return clientPort;
	}
	
	public String getClientEndpoint() {
		return clientEndpoint;
	}
	
	public Map<String, String> getClientParamters() {
		return clientParamters;
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
