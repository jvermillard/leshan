package leshan.server.lwm2m.codec;

import java.io.UnsupportedEncodingException;

import leshan.server.lwm2m.message.LwM2mMessage;
import leshan.server.lwm2m.message.RegisterMessage;
import leshan.server.lwm2m.message.RegisterMessage.BindingMode;

import org.apache.mina.coap.CoapCode;
import org.apache.mina.coap.CoapMessage;
import org.apache.mina.codec.StatelessProtocolDecoder;

public class LwM2mDecoder implements StatelessProtocolDecoder<CoapMessage, LwM2mMessage> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Void createDecoderState() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LwM2mMessage decode(CoapMessage input, Void context) {

        System.out.println("decoding coap message : " + input);

        try {
            // build LW-M2M message
            String[] uriPath = input.getUriPath();
            if (uriPath.length > 0) {

                switch (uriPath[0]) {
                case "rd":
                    switch (CoapCode.fromCode(input.getCode())) {
                    case POST:
                        // register

                        String endpoint = null;
                        Long lifetime = null;
                        String sms = null;
                        String lwVersion = null;
                        BindingMode binding = null;

                        for (String param : input.getUriQuery()) {
                            if (param.startsWith("ep=")) {
                                endpoint = param.substring(3);
                            } else if (param.startsWith("lt=")) {
                                lifetime = Long.valueOf(param.substring(3));
                            } else if (param.startsWith("sms=")) {
                                sms = param.substring(4);
                            } else if (param.startsWith("lwm2m=")) {
                                lwVersion = param.substring(6);
                            } else if (param.startsWith("b=")) {
                                binding = BindingMode.valueOf(param.substring(2));
                            }
                        }

                        // TODO CoRE Link Format (RFC6690)
                        String links = new String(input.getPayload(), "UTF-8");

                        return new RegisterMessage(endpoint, lifetime, lwVersion, binding, sms, links.split(","));
                    case PUT:
                        // update
                    case DELETE:
                        // unregister
                    default:
                        System.err.println("register operation non supported : " + input.getCode());
                    }
                    break;
                case "bs":
                    // bootstrap
                default:
                    System.err.println("coap uri path not supported : " + uriPath[0]);
                }
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishDecode(Void arg0) {
        // TODO Auto-generated method stub

    }

}
