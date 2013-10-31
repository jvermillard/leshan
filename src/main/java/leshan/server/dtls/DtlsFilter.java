package leshan.server.dtls;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.mina.api.AbstractIoFilter;
import org.apache.mina.api.IoSession;
import org.apache.mina.filterchain.ReadFilterChainController;
import org.apache.mina.filterchain.WriteFilterChainController;
import org.apache.mina.session.AttributeKey;
import org.apache.mina.session.DefaultWriteRequest;
import org.apache.mina.session.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.EndpointAddress;
import ch.ethz.inf.vs.californium.dtls.ApplicationMessage;
import ch.ethz.inf.vs.californium.dtls.ClientHandshaker;
import ch.ethz.inf.vs.californium.dtls.ClientHello;
import ch.ethz.inf.vs.californium.dtls.ContentType;
import ch.ethz.inf.vs.californium.dtls.DTLSFlight;
import ch.ethz.inf.vs.californium.dtls.DTLSMessage;
import ch.ethz.inf.vs.californium.dtls.DTLSSession;
import ch.ethz.inf.vs.californium.dtls.FragmentedHandshakeMessage;
import ch.ethz.inf.vs.californium.dtls.HandshakeMessage;
import ch.ethz.inf.vs.californium.dtls.Handshaker;
import ch.ethz.inf.vs.californium.dtls.Record;
import ch.ethz.inf.vs.californium.dtls.ResumingClientHandshaker;
import ch.ethz.inf.vs.californium.dtls.ResumingServerHandshaker;
import ch.ethz.inf.vs.californium.dtls.ServerHandshaker;
import ch.ethz.inf.vs.californium.util.ByteArrayUtils;
import ch.ethz.inf.vs.californium.util.Properties;

/**
 * Implements a DTLS handshaker and pass the application data thru the rest of the chain.
 */
public class DtlsFilter extends AbstractIoFilter {

    private static final Logger LOG = LoggerFactory.getLogger(DtlsFilter.class);

    private static final AttributeKey<DTLSSession> DTLS_SESSION = AttributeKey.createKey(DTLSSession.class,
            "DTLS_SESSION");

    private static final AttributeKey<Handshaker> HANDSHAKER = AttributeKey.createKey(Handshaker.class, "HANDSHAKER");

    /** Storing flights according to peer-addresses. */
    private Map<String, DTLSFlight> flights = new HashMap<String, DTLSFlight>();

    /** The timer daemon to schedule retransmissions. */
    private Timer timer = new Timer(true); // run as daemon

    @Override
    public void messageReceived(IoSession iosession, Object omessage, ReadFilterChainController controller) {
        if (!(omessage instanceof ByteBuffer)) {
            LOG.debug("skipping message : {} not a ByteBuffer", omessage);
            // skip 
            controller.callReadNextFilter(omessage);
            return;
        }
        long timestamp = System.nanoTime();

        ByteBuffer bbMsg = (ByteBuffer) omessage;

        byte[] data = new byte[bbMsg.remaining()];
        bbMsg.get(data);

        DTLSSession dtlsSession = iosession.getAttribute(DTLS_SESSION);
        Handshaker handshaker = iosession.getAttribute(HANDSHAKER);
        InetSocketAddress inet = (InetSocketAddress) iosession.getRemoteAddress();
        EndpointAddress peerAddress = new EndpointAddress(inet.getAddress(), inet.getPort());

        List<Record> records = Record.fromByteArray(data);

        for (Record record : records) {
            record.setSession(dtlsSession);
            byte[] msg = null;

            ContentType contentType = record.getType();
            DTLSFlight flight = null;
            switch (contentType) {
            case APPLICATION_DATA:
                if (dtlsSession == null) {
                    // There is no session available, so no application data
                    // should be received, discard it
                    LOG.info("Discarded unexpected application data message from {}", iosession);
                    return;
                }

                // at this point, the current handshaker is not needed
                // anymore, remove it
                iosession.removeAttribute(HANDSHAKER);

                ApplicationMessage applicationData = (ApplicationMessage) record.getFragment();
                msg = applicationData.getData();
                break;

            case ALERT:
            case CHANGE_CIPHER_SPEC:
            case HANDSHAKE:
                if (handshaker == null) {
                    /*
                     * A handshake message received, but no handshaker
                     * available: this must mean that we either received
                     * a HelloRequest (from server) or a ClientHello
                     * (from client) => initialize appropriate
                     * handshaker type
                     */

                    HandshakeMessage message = (HandshakeMessage) record.getFragment();

                    switch (message.getMessageType()) {
                    case HELLO_REQUEST:
                        // client side
                        if (dtlsSession == null) {
                            // create new session
                            dtlsSession = new DTLSSession(true);
                            // store session according to peer address
                            iosession.setAttribute(DTLS_SESSION, dtlsSession);

                            LOG.trace("Client: Created new session with peer: {}", iosession);
                        }
                        handshaker = new ClientHandshaker(peerAddress, null, dtlsSession);
                        iosession.setAttribute(HANDSHAKER, handshaker);
                        break;
                    case CLIENT_HELLO:
                        /*
                         * Server side: server received a client hello:
                         * check first if client wants to resume a
                         * session (message must contain session
                         * identifier) and then check if particular
                         * session still available, otherwise conduct
                         * full handshake with fresh session.
                         */
                        if (!(message instanceof FragmentedHandshakeMessage)) {
                            // check if session identifier set
                            ClientHello clientHello = (ClientHello) message;
                            dtlsSession = iosession.getAttribute(DTLS_SESSION);
                        }

                        if (dtlsSession == null) {
                            // create new session
                            dtlsSession = new DTLSSession(false);
                            // store session according to peer address
                            iosession.setAttribute(DTLS_SESSION, dtlsSession);

                            LOG.info("Server: Created new session with peer: {}", iosession);

                            handshaker = new ServerHandshaker(peerAddress, dtlsSession);
                        } else {
                            handshaker = new ResumingServerHandshaker(peerAddress, dtlsSession);
                        }
                        iosession.setAttribute(HANDSHAKER, handshaker);
                        break;

                    default:
                        LOG.error("Received unexpected first handshake message from {} :\n {}", iosession,
                                message.toString());
                        break;
                    }
                }

                flight = handshaker.processMessage(record);

                break;

            default:
                LOG.error("Received unknown DTLS record from {} :\n{}", iosession, ByteArrayUtils.toHexString(data));
                break;
            }

            if (flight != null) {
                cancelPreviousFlight(peerAddress);

                flight.setPeerAddress(peerAddress);
                flight.setSession(dtlsSession);

                if (flight.isRetransmissionNeeded()) {
                    flights.put(peerAddress.toString(), flight);
                    scheduleRetransmission(flight, iosession);
                }

                for (ByteBuffer buff : sendFlight(flight)) {
                    controller.callWriteMessageForRead(buff);
                }
            }

            if (msg != null) {
                controller.callReadNextFilter(ByteBuffer.wrap(msg));
            }
        }
    }

    @Override
    public void messageWriting(IoSession iosession, WriteRequest writeRq, WriteFilterChainController controller) {

        ByteBuffer bbMsg = (ByteBuffer) writeRq.getMessage();
        byte[] message = new byte[bbMsg.remaining()];
        bbMsg.get(message);

        // remember when this message was sent for the first time
        // set timestamp only once in order
        // to handle retransmissions correctly
        /* if (message.getTimestamp() == -1) {
             message.setTimestamp(System.nanoTime());
         }*/
        InetSocketAddress inet = (InetSocketAddress) iosession.getRemoteAddress();
        EndpointAddress peerAddress = new EndpointAddress(inet.getAddress(), inet.getPort());

        DTLSSession session = iosession.getAttribute(DTLS_SESSION);

        /*
         * When the DTLS layer receives a message from an upper layer, there is
         * either a already a DTLS session available with the peer or a new
         * handshake must be executed. If a session is available and active, the
         * message will be encrypted and send to the peer, otherwise a short
         * handshake will be initiated.
         */
        Record encryptedMessage = null;
        Handshaker handshaker = null;

        if (session == null) {
            // no session with endpoint available, create new empty session,
            // start fresh handshake
            session = new DTLSSession(true);
            iosession.setAttribute(DTLS_SESSION, session);

            handshaker = new ClientHandshaker(peerAddress, message, session);
        } else {

            if (session.isActive()) {
                // session to peer is active, send encrypted message
                DTLSMessage fragment = new ApplicationMessage(message);
                encryptedMessage = new Record(ContentType.APPLICATION_DATA, session.getWriteEpoch(),
                        session.getSequenceNumber(), fragment, session);

                /*} else if (message.getRetransmissioned() > 0) {
                    // TODO when message retransmitted from TransactionLayer: what to do?
                    return;*/
            } else {
                // try resuming session
                handshaker = new ResumingClientHandshaker(peerAddress, message, session);
            }
        }

        DTLSFlight flight = new DTLSFlight();
        // the CoAP message can not be encrypted since no session with peer
        // available, start DTLS handshake protocol
        if (handshaker != null) {
            // get starting handshake message
            iosession.setAttribute(HANDSHAKER, handshaker);
            flight = handshaker.getStartHandshakeMessage();
            flights.put(peerAddress.toString(), flight);
            scheduleRetransmission(flight, iosession);
        }

        // the CoAP message has been encrypted and can be sent to the peer
        if (encryptedMessage != null) {
            flight.addMessage(encryptedMessage);
        }

        flight.setPeerAddress(peerAddress);
        flight.setSession(session);
        for (ByteBuffer buf : sendFlight(flight)) {
            controller.callWriteNextFilter(new DefaultWriteRequest(buf));
        }
    }

    private void scheduleRetransmission(DTLSFlight flight, IoSession iosession) {

        // cancel existing schedule (if any)
        if (flight.getRetransmitTask() != null) {
            flight.getRetransmitTask().cancel();
        }

        // create new retransmission task
        flight.setRetransmitTask(new RetransmitTask(flight, iosession));

        // calculate timeout using exponential back-off
        if (flight.getTimeout() == 0) {
            // use initial timeout
            flight.setTimeout(initialTimeout());
        } else {
            // double timeout
            flight.incrementTimeout();
        }

        // schedule retransmission task
        timer.schedule(flight.getRetransmitTask(), flight.getTimeout());
    }

    private List<ByteBuffer> sendFlight(DTLSFlight flight) {
        byte[] payload = new byte[] {};
        // the overhead for the record header (13 bytes) and the handshake
        // header (12 bytes) is 25 bytes
        int maxPayloadSize = Properties.std.getInt("MAX_FRAGMENT_LENGTH") + 25;

        // put as many records into one datagram as allowed by the block size
        List<ByteBuffer> datagrams = new ArrayList<ByteBuffer>();

        for (Record record : flight.getMessages()) {
            if (flight.getTries() > 0) {
                // adjust the record sequence number
                int epoch = record.getEpoch();
                record.setSequenceNumber(flight.getSession().getSequenceNumber(epoch));
            }

            byte[] recordBytes = record.toByteArray();
            if (payload.length + recordBytes.length > maxPayloadSize) {
                // can't add the next record, send current payload as datagram
                ByteBuffer data = ByteBuffer.allocate(payload.length);
                data.put(payload);
                data.flip();
                datagrams.add(data);
            }

            // retrieve payload
            payload = ByteArrayUtils.concatenate(payload, recordBytes);
        }
        ByteBuffer data = ByteBuffer.allocate(payload.length);
        data.put(payload);
        data.flip();
        datagrams.add(data);
        return datagrams;
    }

    /**
     * Cancels the retransmission timer of the previous flight (if available).
     * 
     * @param peerAddress
     *            the peer's address.
     */
    private void cancelPreviousFlight(EndpointAddress peerAddress) {
        DTLSFlight previousFlight = flights.get(peerAddress.toString());
        if (previousFlight != null) {
            previousFlight.getRetransmitTask().cancel();
            previousFlight.setRetransmitTask(null);
            flights.remove(peerAddress.toString());
        }
    }

    /**
     * Utility class to handle timeouts.
     */
    private class RetransmitTask extends TimerTask {

        private DTLSFlight flight;

        private IoSession iosession;

        RetransmitTask(DTLSFlight flight, IoSession iosession) {
            this.flight = flight;
            this.iosession = iosession;
        }

        @Override
        public void run() {
            handleTimeout(flight, iosession);
        }
    }

    private void handleTimeout(DTLSFlight flight, IoSession iosession) {

        final int max = Properties.std.getInt("MAX_RETRANSMIT");

        // check if limit of retransmissions reached
        if (flight.getTries() < max) {

            flight.incrementTries();

            for (ByteBuffer buff : sendFlight(flight)) {
                iosession.write(buff);
            }

            // schedule next retransmission
            scheduleRetransmission(flight, iosession);

        } else {
            LOG.info("Maximum retransmissions reached.");
        }
    }

    private int initialTimeout() {
        return Properties.std.getInt("RETRANSMISSION_TIMEOUT");
    }

}
