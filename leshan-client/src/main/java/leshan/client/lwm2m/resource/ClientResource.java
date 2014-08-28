package leshan.client.lwm2m.resource;

import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvType;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

class ClientResource extends ResourceBase {

	private final ExecuteListener executeListener;
	private final WriteListener writeListener;
	private final ReadListener readListener;

	public ClientResource(final int id, final ExecuteListener executeListener, final WriteListener writeListener, final ReadListener readListener) {
		super(Integer.toString(id));
		this.executeListener = executeListener;
		this.writeListener = writeListener;
		this.readListener = readListener;
	}

	public int getId() {
		return Integer.parseInt(getName());
	}

	public Tlv asTlv() {
		return new Tlv(TlvType.RESOURCE_VALUE, null, readListener.read(), getId());
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		exchange.respond(ResponseCode.CONTENT, readListener.read());
	}

	@Override
	public void handlePUT(final CoapExchange exchange) {
		final WriteResponse writeResponse = writeValue(exchange.getRequestPayload());
		exchange.respond(writeResponse.getCode(), new byte[0]);
	}

	@Override
	public void handlePOST(final CoapExchange exchange) {
		executeListener.execute(Integer.parseInt(getParent().getParent().getName()),
				Integer.parseInt(getParent().getName()),
				Integer.parseInt(getName()));
		exchange.respond(ResponseCode.CHANGED);
	}

	public boolean isExecutable() {
		return executeListener != ExecuteListener.DUMMY;
	}

	public boolean isWritable() {
		return writeListener != WriteListener.DUMMY;
	}

	public boolean isReadable() {
		return readListener != ReadListener.DUMMY;
	}

	public void writeTlv(final Tlv tlv) {
		writeValue(tlv.getValue());
	}

	private WriteResponse writeValue(final byte[] value) {
		return writeListener.write(Integer.parseInt(getParent().getParent().getName()),
				Integer.parseInt(getParent().getName()),
				Integer.parseInt(getName()), value);
	}

}