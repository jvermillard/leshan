package leshan.connector.californium.server;

import leshan.server.lwm2m.request.CoapResponseCode;

import org.apache.commons.lang.Validate;
import org.eclipse.californium.core.coap.CoAP;

public class CaliforniumResponseCode extends CoapResponseCode {

	@Override
	public ResponseCode fromCoapCode(final int code) {
		Validate.notNull(code);

        if (code == CoAP.ResponseCode.CREATED.value) {
            return CoapResponseCode.ResponseCode.CREATED;
        } else if (code == CoAP.ResponseCode.DELETED.value) {
            return CoapResponseCode.ResponseCode.DELETED;
        } else if (code == CoAP.ResponseCode.CHANGED.value) {
            return CoapResponseCode.ResponseCode.CHANGED;
        } else if (code == CoAP.ResponseCode.CONTENT.value) {
            return CoapResponseCode.ResponseCode.CONTENT;
        } else if (code == CoAP.ResponseCode.BAD_REQUEST.value) {
            return CoapResponseCode.ResponseCode.BAD_REQUEST;
        } else if (code == CoAP.ResponseCode.UNAUTHORIZED.value) {
            return CoapResponseCode.ResponseCode.UNAUTHORIZED;
        } else if (code == CoAP.ResponseCode.NOT_FOUND.value) {
            return CoapResponseCode.ResponseCode.NOT_FOUND;
        } else if (code == CoAP.ResponseCode.METHOD_NOT_ALLOWED.value) {
            return CoapResponseCode.ResponseCode.METHOD_NOT_ALLOWED;
        } else if (code == 137) {
            return CoapResponseCode.ResponseCode.CONFLICT;
        } else {
            throw new IllegalArgumentException("Invalid CoAP code for LWM2M response: " + code);
        }
	}

}
