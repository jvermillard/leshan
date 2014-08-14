package leshan.bootstrap;

import java.net.InetSocketAddress;

import leshan.server.lwm2m.LwM2mBootstrapServer;
import leshan.server.lwm2m.bootstrap.BootstrapStoreImpl;
import leshan.server.lwm2m.security.SecurityRegistry;

public class BootstrapMain {

    public static void main(String[] args) {

        BootstrapStoreImpl bsStore = new BootstrapStoreImpl();
        SecurityRegistry securityRegistry = new SecurityRegistry();
        // JV: testing bootstrap
        // BootstrapConfig bsConfig = new BootstrapConfig();
        // BootstrapConfig.ServerSecurity ss = new BootstrapConfig.ServerSecurity();
        // ss.bootstrapServer = true;
        // ss.publicKeyOrId = "Bleh".getBytes();
        // ss.secretKey = "S3cr3tm3".getBytes();
        // ss.securityMode = SecurityMode.NO_SEC;
        // ss.uri = "coaps://54.67.9.2";
        // ss.serverId = 1;
        //
        // bsConfig.security.put(0, ss);
        //
        // BootstrapConfig.ServerConfig sc = new BootstrapConfig.ServerConfig();
        // sc.binding = BindingMode.U;
        // sc.shortId = 1;
        // sc.lifetime = 36000;
        // bsConfig.servers.put(0, sc);
        //
        // bsStore.addConfig("testlwm2mclient", bsConfig);
        // use those ENV variables for specifying the interface to be bound for coap and coaps
        String iface = System.getenv("COAPIFACE");
        String ifaces = System.getenv("COAPSIFACE");

        LwM2mBootstrapServer bsServer;

        if (iface == null || iface.isEmpty() || ifaces == null || ifaces.isEmpty()) {
            bsServer = new LwM2mBootstrapServer(bsStore, securityRegistry);
        } else {
            String[] add = iface.split(":");
            String[] adds = ifaces.split(":");

            // user specified the iface to be bound
            bsServer = new LwM2mBootstrapServer(new InetSocketAddress(add[0], Integer.parseInt(add[1])),
                    new InetSocketAddress(adds[0], Integer.parseInt(adds[1])), bsStore, securityRegistry);
        }

        bsServer.start();

    }
}
