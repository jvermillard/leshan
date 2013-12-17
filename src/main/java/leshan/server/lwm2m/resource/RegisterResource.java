package leshan.server.lwm2m.resource;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;

import leshan.server.lwm2m.client.BindingMode;
import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.client.ClientRegistry;
import leshan.server.lwm2m.client.RegistryListener;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.CoAP.Type;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Exchange;
import ch.ethz.inf.vs.californium.observe.ObserveRelation;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;
import ch.ethz.inf.vs.californium.server.resources.ResourceObserver;

/**
 * A CoAP {@link Resource} in charge of handling clients registration requests.
 * <p>
 * This resource is the entry point of the Resource Directory ("/rd"). Each new client is added to the resource tree as
 * a {@link ClientResource} (as a child of this node).
 * </p>
 * <p>
 * This class implements the {@link ClientRegistry} interface and provides simple methods to access the list of
 * registered LW-M2M clients.
 * </p>
 */
public class RegisterResource extends ResourceBase implements ClientRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterResource.class);

    public RegisterResource() {
        super("rd");
        getAttributes().addResourceType("core.rd");
    }

    @Override
    public void handlePOST(Exchange exchange) {
        Request request = exchange.getRequest();

        LOG.debug("POST received : {}", request);

        if (Type.CON.equals(request.getType())) {

            try {
                // register
                String registrationId = RegisterResource.createRegistrationId();

                String endpoint = null;
                Long lifetime = null;
                String smsNumber = null;
                String lwVersion = null;
                BindingMode binding = null;

                for (String param : request.getOptions().getURIQueries()) {
                    if (param.startsWith("ep=")) {
                        endpoint = param.substring(3);
                    } else if (param.startsWith("lt=")) {
                        lifetime = Long.valueOf(param.substring(3));
                    } else if (param.startsWith("sms=")) {
                        smsNumber = param.substring(4);
                    } else if (param.startsWith("lwm2m=")) {
                        lwVersion = param.substring(6);
                    } else if (param.startsWith("b=")) {
                        binding = BindingMode.valueOf(param.substring(2));
                    }
                }

                // TODO endpoint uniqueness ?

                ClientResource client = new ClientResource(registrationId, endpoint, request.getSource(),
                        request.getSourcePort(), lwVersion, lifetime, smsNumber, binding);

                // object links
                String[] objectLinks = new String(request.getPayload(), "UTF-8").split(",");
                addObjectResources(client, objectLinks);

                this.add(client);
                LOG.info("New registered client: {}", client);

                Response response = new Response(ResponseCode.CREATED);
                response.getOptions().addLocationPath(client.getURI());
                exchange.respond(response);

            } catch (UnsupportedEncodingException e) {
                LOG.error("Invalid registration request", e);
                exchange.respond(ResponseCode.BAD_REQUEST);
            }

        } else {
            exchange.respond(ResponseCode.BAD_REQUEST);
        }
    }

    private void addObjectResources(Resource client, String[] objectLinks) {
        LOG.debug("Available objects for client {}: {}", client.getName(), objectLinks);

        for (String link : objectLinks) {

            // String valid = StringUtils.substringBetween(link.trim(), "<", ">");
            // HACK for liblwm2m client
            String valid = link.trim();

            // TODO rt and ct parameters

            if (valid != null) {
                Resource current = client;
                for (String objectName : valid.split("/")) {
                    Resource child = current.getChild(objectName);
                    if (child == null) {
                        child = new ObjectResource(objectName);
                        current.add(child);
                        LOG.debug("New object resource created: {}", child.getName());
                    }
                    current = child;
                }
            }
        }
    }

    private static String createRegistrationId() {
        return RandomStringUtils.random(10, true, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client get(String endpoint) {
        for (Resource client : this.getChildren()) {
            Client c = (Client) client;
            if (c.getEndpoint().equals(endpoint)) {
                return c;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Client> allClients() {
        Collection<Client> clients = new ArrayList<>();
        for (Resource client : this.getChildren()) {
            clients.add((Client) client);
        }
        return clients;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(final RegistryListener listener) {
        this.addObserver(new ResourceObserver() {

            @Override
            public void addedChild(Resource child) {
                listener.registered((Client) child);
            }

            @Override
            public void removedChild(Resource child) {
                listener.unregistered((Client) child);
            }

            @Override
            public void removedObserveRelation(ObserveRelation relation) {
            }

            @Override
            public void changedPath(String old) {
            }

            @Override
            public void changedName(String old) {
            }

            @Override
            public void addedObserveRelation(ObserveRelation relation) {
            }
        });
    }

}
