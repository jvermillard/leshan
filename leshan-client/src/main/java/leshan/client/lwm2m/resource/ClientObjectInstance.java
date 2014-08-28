package leshan.client.lwm2m.resource;

import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT;
import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.DELETED;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvEncoder;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

class ClientObjectInstance extends ResourceBase implements LinkFormattable{

	public ClientObjectInstance(final int instanceID, final Map<Integer, ClientResource> resources) {
		super(Integer.toString(instanceID));
		for(final Map.Entry<Integer, ClientResource> entry : resources.entrySet()){
			add(entry.getValue());
		}
	}

	public int getInstanceId() {
		return Integer.parseInt(getName());
	}

	public Tlv[] asTlvArray() {
		final List<Tlv> tlvs = new ArrayList<>();
		for (final Resource res : getChildren()) {
			final ClientResource resource = (ClientResource) res;
			if (resource.isReadable()) {
				tlvs.add(resource.asTlv());
			}
		}
		final Tlv[] tlvArray = tlvs.toArray(new Tlv[0]);
		return tlvArray;
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_LINK_FORMAT){
			handleDiscover(exchange);
		}
		else{
			handleRead(exchange);
		}
	}

	private void handleDiscover(final CoapExchange exchange) {
		exchange.respond(CONTENT, asLinkFormat());
	}

	private void handleRead(final CoapExchange exchange) {
		final Tlv[] tlvArray = asTlvArray();
		exchange.respond(CONTENT, TlvEncoder.encode(tlvArray).array());
	}
	
	@Override
	public void handleDELETE(final CoapExchange exchange) {
		getParent().remove(this);
		
		exchange.respond(DELETED);
	}

	@Override
	public String asLinkFormat() {
		final StringBuilder linkFormat = LinkFormat.serializeResource(this).append(LinkFormat.serializeAttributes(getAttributes()));
		for(final Resource child : getChildren()){
			linkFormat.append(LinkFormat.serializeResource(child));
		}
		linkFormat.deleteCharAt(linkFormat.length() - 1);
		
		return linkFormat.toString();
	}

}