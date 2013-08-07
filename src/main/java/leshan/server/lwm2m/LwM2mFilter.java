package leshan.server.lwm2m;

import java.io.UnsupportedEncodingException;

import leshan.server.lwm2m.message.client.DeregisterMessage;
import leshan.server.lwm2m.message.client.RegisterMessage;
import leshan.server.lwm2m.message.server.MessageEncoder;
import leshan.server.lwm2m.message.server.ServerMessage;
import leshan.server.lwm2m.session.BindingMode;

import org.apache.commons.lang.NotImplementedException;
import org.apache.mina.api.AbstractIoFilter;
import org.apache.mina.api.IoSession;
import org.apache.mina.coap.CoapCode;
import org.apache.mina.coap.CoapMessage;
import org.apache.mina.codec.ProtocolDecoderException;
import org.apache.mina.filterchain.ReadFilterChainController;
import org.apache.mina.filterchain.WriteFilterChainController;
import org.apache.mina.session.DefaultWriteRequest;
import org.apache.mina.session.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A filter in charge of encoding/decoding LW-M2M messages
 */
public class LwM2mFilter extends AbstractIoFilter {

    private static final Logger LOG = LoggerFactory.getLogger(LwM2mFilter.class);

    private MessageEncoder encoder = new LwM2mEncoder();

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(final IoSession session, final Object message,
            final ReadFilterChainController controller) {

        CoapMessage coapMessage = (CoapMessage) message;

        LOG.debug("decoding coap message : " + coapMessage);

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
                    case DELETE:
                        // unregister

                        // TODO multi level location ?
                        String registrationId = uriPath[1];
                        controller.callReadNextFilter(new DeregisterMessage(coapMessage.getId(), registrationId));
                        break;
                    case PUT:
                        // update

                    default:
                        throw new ProtocolDecoderException("register operation non supported : "
                                + coapMessage.getCode());
                    }
                    break;
                case "bs":
                    // bootstrap
                default:
                    throw new NotImplementedException("coap uri path not supported : " + uriPath[0]);
                }
            }

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new ProtocolDecoderException("invalid LW-M2M client message", e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageWriting(IoSession session, WriteRequest message, WriteFilterChainController controller) {

        LOG.debug("encoding message : " + message.getMessage());

        if (message.getMessage() != null && message.getMessage() instanceof ServerMessage) {
            CoapMessage response = ((ServerMessage) message.getMessage()).encode(encoder);

            controller.callWriteNextFilter(new DefaultWriteRequest(response));
        }

    }

}
