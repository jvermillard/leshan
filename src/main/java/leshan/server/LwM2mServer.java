package leshan.server;

import java.nio.ByteBuffer;

import leshan.server.lwm2m.codec.LwM2mDecoder;
import leshan.server.lwm2m.codec.LwM2mEncoder;
import leshan.server.lwm2m.message.LwM2mMessage;

import org.apache.mina.api.IdleStatus;
import org.apache.mina.api.IoFilter;
import org.apache.mina.coap.CoapMessage;
import org.apache.mina.coap.codec.CoapDecoder;
import org.apache.mina.coap.codec.CoapEncoder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.bio.BioUdpServer;

public class LwM2mServer {

    /** IANA assigned UDP port for CoAP (so for LWM2M) */
    private final int port = 5683;

    private BioUdpServer server;

    public void start() {
        server = new BioUdpServer();

        // protocol filters
        IoFilter coapFilter = new ProtocolCodecFilter<CoapMessage, ByteBuffer, Void, Void>(new CoapEncoder(),
                new CoapDecoder());
        IoFilter lwM2mFilter = new ProtocolCodecFilter<LwM2mMessage, CoapMessage, Void, Void>(new LwM2mEncoder(),
                new LwM2mDecoder());

        server.setFilters(coapFilter, lwM2mFilter);

        // LW-M2M handler
        server.setIoHandler(new LwM2mHandler());

        // we kill sessions after 20 minutes of inactivity
        server.getSessionConfig().setIdleTimeInMillis(IdleStatus.READ_IDLE, 20 * 60 * 1_000);

        server.bind(port);

        System.out.println("LW-M2M server started on port " + port);
    }

    public static void main(String[] args) {
        new LwM2mServer().start();
    }

}
