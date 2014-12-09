package leshan.client.californium.impl;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;

import leshan.LinkObject;
import leshan.client.request.AbstractLwM2mClientRequest;
import leshan.client.request.AbstractRegisteredLwM2mClientRequest;
import leshan.client.request.DeregisterRequest;
import leshan.client.request.LwM2mClientRequestVisitor;
import leshan.client.request.LwM2mContentRequest;
import leshan.client.request.RegisterRequest;
import leshan.client.request.UpdateRequest;
import leshan.client.util.LinkFormatUtils;

import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.Endpoint;

public class CoapClientRequestBuilder implements LwM2mClientRequestVisitor {
	private Request coapRequest;

	private Endpoint coapEndpoint;

	private boolean parametersValid = false;

	private final InetSocketAddress serverAddress;

	private long timeout;

	private final LinkObject[] clientObjectModel;

	public CoapClientRequestBuilder(final InetSocketAddress serverAddress, final LinkObject... clientObjectModel) {
		this.serverAddress = serverAddress;
		this.clientObjectModel = clientObjectModel;
	}

	@Override
	public void visit(final RegisterRequest request) {
		if(!areParametersValid(request.getClientParameters())){
			return;
		}
		coapRequest = Request.newPost();
		buildRequestSettings(request);
		
		coapRequest.getOptions().addURIPath("rd");
		coapRequest.getOptions().addURIQuery("ep=" + request.getClientEndpointIdentifier());
		buildRequestContent(request);
		
		parametersValid = true;
	}
	
	@Override
	public void visit(final UpdateRequest request) {
		if(!areParametersValid(request.getClientParameters())){
			return;
		}
		coapRequest = Request.newPut();
		buildRequestSettings(request);
		
		buildLocationPath(request);
		buildRequestContent(request);
		
		parametersValid = true;
		
	}

	@Override
	public void visit(final DeregisterRequest request) {
		coapRequest = Request.newDelete();
		buildRequestSettings(request);
		
		buildLocationPath(request);
		
		parametersValid = true;
		
	}
	
	public Request getRequest() {
		return coapRequest;
	}

	public Endpoint getEndpoint() {
		return coapEndpoint;
	}
	
	public boolean areParametersValid() {
		return parametersValid;
	}
	
	public long getTimeout() {
		return timeout;
	}
	
	private void buildLocationPath(final AbstractRegisteredLwM2mClientRequest request) {
		final String[] locationPaths = request.getClientLocation().split("/");
		for(final String location : locationPaths){
			if(location.length() != 0){
				coapRequest.getOptions().addURIPath(location);
			}
		}
	}
	
	private void buildRequestSettings(final AbstractLwM2mClientRequest request) {
		timeout = request.getTimeout();
		coapRequest.setDestination(serverAddress.getAddress());
		coapRequest.setDestinationPort(serverAddress.getPort());
	}
	
	private void buildRequestContent(final LwM2mContentRequest request) {
		for(final Entry<String, String> entry : request.getClientParameters().entrySet()){
			coapRequest.getOptions().addURIQuery(entry.getKey() + "=" + entry.getValue());
		}
		
		final String payload = LinkFormatUtils.payloadize(clientObjectModel);
		coapRequest.setPayload(payload);
	}
	
	private boolean areParametersValid(final Map<String, String> parameters) {
        for (final Map.Entry<String, String> p : parameters.entrySet()) {
            switch (p.getKey()) {
            case "lt":
                break;
            case "lwm2m":
                break;
            case "sms":
                return false;
            case "b":
                if (!isBindingValid(p.getValue())) {
                    return false;
                }
                break;
            default:
                return false;
            }
        }

        return true;
    }

    private boolean isBindingValid(final String value) {
        if (value.equals("U")) {
            return true;
        }

        return false;
    }
}
