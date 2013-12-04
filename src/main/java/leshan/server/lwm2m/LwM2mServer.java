package leshan.server.lwm2m;

import java.nio.ByteBuffer;

import leshan.server.lwm2m.session.SessionRegistry;
import leshan.server.servlet.ApiServlet;
import leshan.server.servlet.EventServlet;

import org.apache.mina.api.IdleStatus;
import org.apache.mina.api.IoFilter;
import org.apache.mina.coap.CoapMessage;
import org.apache.mina.coap.codec.CoapDecoder;
import org.apache.mina.coap.codec.CoapEncoder;
import org.apache.mina.coap.retry.CoapRetryFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.bio.BioUdpServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LwM2mServer {

    private static final Logger LOG = LoggerFactory.getLogger(LwM2mServer.class);

    /** IANA assigned UDP port for CoAP (so for LWM2M) */
    private final int port = 5684;

    private BioUdpServer server;

    private SessionRegistry registry = new SessionRegistry();

    public void start() {
        server = new BioUdpServer();

        IoFilter coapFilter = new ProtocolCodecFilter<CoapMessage, ByteBuffer, Void, Void>(new CoapEncoder(),
                new CoapDecoder());

        CoapRetryFilter retryFilter = new CoapRetryFilter();

        IoFilter lwM2mFilter = new LwM2mFilter();

        LwM2mRequestFilter requestFilter = new LwM2mRequestFilter();

        server.setFilters(coapFilter, retryFilter, lwM2mFilter, requestFilter);

        // LW-M2M handler
        server.setIoHandler(new LwM2mHandler(registry));

        // we kill sessions after 20 minutes of inactivity (default)
        server.getSessionConfig().setIdleTimeInMillis(IdleStatus.READ_IDLE, 20 * 60 * 1_000);

        server.setReuseAddress(true);
        server.bind(port);

        LOG.info("LW-M2M server started on port " + port);

        // now prepare and start jetty
        String webappDirLocation = "src/main/webapp/";

        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        Server server = new Server(Integer.valueOf(webPort));
        WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);
        root.setParentLoaderPriority(true);

        ServletHolder apiServletHolder = new ServletHolder(new ApiServlet(registry, requestFilter));
        root.addServlet(apiServletHolder, "/api/*");

        server.setHandler(root);

        ServletHolder eventServletHolder = new ServletHolder(new EventServlet(registry));
        root.addServlet(eventServletHolder, "/event/*");

        server.setHandler(root);

        try {
            server.start();
        } catch (Exception e) {
            LOG.error("jetty error", e);
        }
    }

    public static void main(String[] args) {
        new LwM2mServer().start();
    }

}
