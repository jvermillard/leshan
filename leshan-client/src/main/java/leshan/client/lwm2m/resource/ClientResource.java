package leshan.client.lwm2m.resource;

import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leshan.client.lwm2m.operation.ClientObservation;
import leshan.client.lwm2m.operation.ClientObservationImpl;
import leshan.client.lwm2m.operation.Executable;
import leshan.client.lwm2m.operation.ExecuteResponse;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.client.lwm2m.operation.Readable;
import leshan.client.lwm2m.operation.Writable;
import leshan.client.lwm2m.operation.WriteResponse;
import leshan.server.lwm2m.message.ObserveRequest;
import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvType;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.network.Exchange;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

class ClientResource extends ResourceBase implements LinkFormattable, ClientObservable, Notifier{

	private static final int IS_OBSERVE = 0;
	private final Executable executable;
	private final Writable writable;
	private final Readable readable;
	private final Map<ClientObservable, String> observationTokens;

	public ClientResource(final int id, final Executable executable, final Writable writable, final Readable readable) {
		super(Integer.toString(id));
		setObservable(true);

		this.executable = executable;
		this.writable = writable;
		this.readable = readable;

		observationTokens = new ConcurrentHashMap<>();
	}

	public int getId() {
		return Integer.parseInt(getName());
	}

	public Tlv asTlv() {
		final ReadResponse response = readable.read();

		if(ResponseCode.isSuccess(response.getCode())){
			return new Tlv(TlvType.RESOURCE_VALUE, null, response.getValue(), getId());
		}
		else{
			return new Tlv(TlvType.RESOURCE_VALUE, null, new byte[0], getId());
		}
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
		if(isNotifyRead(exchange)){
			handleObserveNotifyRead(exchange);
		}
		else{
			handleNormalRead(exchange);
		}
		
	}

	private void handleNormalRead(final CoapExchange exchange) {
		final ReadResponse response = readable.read();

		if(ResponseCode.isSuccess(response.getCode())){
			exchange.respond(response.getCode(), response.getValue());
			handleObserve(exchange);
		}
		else{
			exchange.respond(response.getCode());
		}
	}

	private void handleObserveNotifyRead(final CoapExchange exchange) {
		final ReadResponse response = readable.read();
		if(ResponseCode.isSuccess(response.getCode())){
			exchange.respond(ResponseCode.CHANGED, response.getValue());
		}
		else{
			exchange.respond(response.getCode());
		}
	}

	private boolean isNotifyRead(final CoapExchange exchange) {
		for(final String t : observationTokens.values()){
			if(t.equals(exchange.advanced().getRequest().getTokenString())){
				return true;
			}
		}
		
		return false;
	}

	private void handleObserve(final CoapExchange exchange) {
		if(exchange.getRequestOptions().hasObserve() && exchange.getRequestOptions().getObserve() == IS_OBSERVE){
			createObservation(this, exchange);
		}
	}

	@Override
	public void createObservation(final ClientObservable observable, final CoapExchange exchange) {
		if(!observationTokens.containsKey(observable)){
			observationTokens.put(observable, exchange.advanced().getRequest().getTokenString());

			if(observationTokens.size() == 1){
				readable.observe(this);
			}
		}
		else{
			//Is there anything needed to be done if we already have this?
		}
	}

	@Override
	public void handlePUT(final CoapExchange exchange) {
		final WriteResponse writeResponse = writeValue(exchange.getRequestPayload());
		exchange.respond(writeResponse.getCode(), new byte[0]);
	}

	@Override
	public void handlePOST(final CoapExchange exchange) {
		final ExecuteResponse response = executable.execute(Integer.parseInt(getParent().getParent().getName()),
				Integer.parseInt(getParent().getName()),
				Integer.parseInt(getName()));
		exchange.respond(response.getCode());
	}

	public boolean isExecutable() {
		return executable != Executable.NOT_EXECUTABLE;
	}

	public boolean isWritable() {
		return writable != Writable.NOT_WRITABLE;
	}

	public boolean isReadable() {
		return readable != Readable.NOT_READABLE;
	}

	public void writeTlv(final Tlv tlv) {
		writeValue(tlv.getValue());
	}

	private WriteResponse writeValue(final byte[] value) {
		return writable.write(Integer.parseInt(getParent().getParent().getName()),
				Integer.parseInt(getParent().getName()),
				Integer.parseInt(getName()), value);
	}

	@Override
	public String asLinkFormat() {
		final StringBuilder linkFormat = LinkFormat.serializeResource(this).append(LinkFormat.serializeAttributes(getAttributes()));

		linkFormat.deleteCharAt(linkFormat.length() - 1);

		return linkFormat.toString();
	}

	@Override
	public void notify(final ReadResponse notification) {
		notifyObserverRelations();
	}

}