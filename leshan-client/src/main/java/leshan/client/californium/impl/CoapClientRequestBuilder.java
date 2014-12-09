package leshan.client.californium.impl;

import java.util.Map;

import leshan.client.request.DeregisterRequest;
import leshan.client.request.LwM2mClientRequestVisitor;
import leshan.client.request.RegisterRequest;
import leshan.client.util.LinkFormatUtils;

import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.Endpoint;

public class CoapClientRequestBuilder implements LwM2mClientRequestVisitor {
	private Request coapRequest;

	private Endpoint coapEndpoint;

	private boolean parametersValid = false;

	@Override
	public void visit(final RegisterRequest request) {
		if(!areParametersValid(request.getClientParameters())){
			return;
		}
		coapRequest = Request.newPost();
		coapRequest.setDestination(request.getServer().getServerAddress().getAddress());
		coapRequest.setDestinationPort(request.getServer().getServerAddress().getPort());
		
		coapRequest.getOptions().addURIPath("rd");
		coapRequest.getOptions().addURIQuery("ep=" + request.getClientEndpoint());

		final String payload = LinkFormatUtils.payloadize(request.getObjectModel());
		coapRequest.setPayload(payload);
		
		parametersValid = true;
	}

	@Override
	public void visit(final DeregisterRequest request) {
		coapRequest = Request.newDelete();
		coapRequest.setDestination(request.getServer().getServerAddress().getAddress());
		coapRequest.setDestinationPort(request.getServer().getServerAddress().getPort());
		final String[] locationPaths = request.getClientLocation().split("/");
		for(final String location : locationPaths){
			if(location.length() != 0){
				coapRequest.getOptions().addURIPath(location);
			}
		}
		
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
