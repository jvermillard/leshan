package leshan.server;

import leshan.server.lwm2m.CoapServer;
import leshan.server.servlet.ApiServlet;
import leshan.server.servlet.EventServlet;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LwM2mServer {

    private static final Logger LOG = LoggerFactory.getLogger(LwM2mServer.class);

    public void start() {

        // LWM2M server
        CoapServer lwServer = new CoapServer();
        lwServer.start();

        // now prepare and start jetty
        String webappDirLocation = "src/main/webapp/";

        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(Integer.valueOf(webPort));
        WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);
        root.setParentLoaderPriority(true);

        ServletHolder apiServletHolder = new ServletHolder(new ApiServlet(lwServer.getRequestHandler(),
                lwServer.getClientRegistry()));
        root.addServlet(apiServletHolder, "/api/*");

        ServletHolder eventServletHolder = new ServletHolder(new EventServlet(lwServer.getClientRegistry()));
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
