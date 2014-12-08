package leshan.client.request;

import java.net.InetSocketAddress;
import java.util.Map;

public class RegisterRequest {

	private final int clientPort;
	private final InetSocketAddress serverAddress;
	private final String clientEndpoint;
	private final Map<String, String> clientParamters;
	private final int timeoutMs;

	public RegisterRequest(final int clientPort, final String endpoint, final Map<String, String> clientParameters, final InetSocketAddress serverAddress, final int timeoutMs) {
		this.clientPort = clientPort;
		this.clientEndpoint = endpoint;
		this.clientParamters = clientParameters;
		this.serverAddress = serverAddress;
		this.timeoutMs = timeoutMs;
		
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

}
