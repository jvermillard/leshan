package leshan.server.lwm2m;

import java.io.UnsupportedEncodingException;

import leshan.server.lwm2m.message.client.RegisterMessage;
import leshan.server.lwm2m.message.client.RegisterMessage.BindingMode;
import leshan.server.lwm2m.message.server.MessageEncoder;
import leshan.server.lwm2m.message.server.ServerMessage;

import org.apache.mina.api.AbstractIoFilter;
import org.apache.mina.api.IoSession;
import org.apache.mina.coap.CoapCode;
import org.apache.mina.coap.CoapMessage;
import org.apache.mina.filterchain.ReadFilterChainController;
import org.apache.mina.filterchain.WriteFilterChainController;
import org.apache.mina.session.DefaultWriteRequest;
import org.apache.mina.session.WriteRequest;

/**
 * A filter in charge of encoding/decoding LW-M2M messages
 */
public class LwM2mFilter extends AbstractIoFilter {

    private MessageEncoder encoder = new LwM2mEncoder();

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(final IoSession session, final Object message,
            final ReadFilterChainController controller) {

        CoapMessage coapMessage = (CoapMessage) message;

        System.out.println("decoding coap message : " + coapMessage);

        try {
            // build LW-M2M message
            String[] uriPath = coapMessage.getUriPath();
            if (uriPath.length > 0) {

                switch (uriPath[0]) {
                case "rd":
                    switch (CoapCode.fromCode(coapMessage.getCode())) {
                    case POST:
                        // register

                        String endpoint = null;
                        Long lifetime = null;
                        String sms = null;
                        String lwVersion = null;
                        BindingMode binding = null;

                        for (String param : coapMessage.getUriQuery()) {
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
                        String links = new String(coapMessage.getPayload(), "UTF-8");

                        controller.callReadNextFilter(new RegisterMessage(coapMessage.getId(), endpoint, lifetime,
                                lwVersion, binding, sms, links.split(",")));
                        break;
                    case PUT:
                        // update
                    case DELETE:
                        // unregister
                    default:
                        System.err.println("register operation non supported : " + coapMessage.getCode());
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

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageWriting(IoSession session, WriteRequest message, WriteFilterChainController controller) {

        if (message.getMessage() != null && message.getMessage() instanceof ServerMessage) {
            System.out.println("encoding LW-M2M message : " + message.getMessage());

            CoapMessage response = ((ServerMessage) message.getMessage()).encode(encoder);

            System.out.println("coap response : " + response);
            controller.callWriteNextFilter(new DefaultWriteRequest(response));
        }

    }

}
