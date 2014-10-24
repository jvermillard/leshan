/*
 * Copyright (c) 2013, Sierra Wireless,
 * Copyright (c) 2014, Zebra Technologies,
 * 
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package leshan.client;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.observe.ObserveManager;
import org.eclipse.californium.core.observe.ObserveRelation;
import org.eclipse.californium.core.observe.ObservingEndpoint;
import org.eclipse.californium.core.server.MessageDeliverer;
import org.eclipse.californium.core.server.resources.Resource;

public class LwM2mServerMessageDeliverer implements MessageDeliverer {

    private final static Logger LOGGER = Logger.getLogger(LwM2mServerMessageDeliverer.class.getCanonicalName());

    /* The root of all resources */
    private final Resource root;

    /* The manager of the observe mechanism for this server */
    private final ObserveManager observeManager = new ObserveManager();

    /**
     * Constructs a default message deliverer that delivers requests to the resources rooted at the specified root.
     */
    public LwM2mServerMessageDeliverer(final Resource root) {
        this.root = root;
    }

    @Override
    public void deliverRequest(final Exchange exchange) {
        final Request request = exchange.getRequest();
        final List<String> path = request.getOptions().getURIPaths();
        final Code code = request.getCode();
        final Resource resource = findResource(path, code);
        if (resource != null) {
            checkForObserveOption(exchange, resource);

            // Get the executor and let it process the request
            final Executor executor = resource.getExecutor();
            if (executor != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        resource.handleRequest(exchange);
                    }
                });
            } else {
                resource.handleRequest(exchange);
            }
        } else {
            LOGGER.info("Did not find resource " + path.toString());
            exchange.sendResponse(new Response(ResponseCode.NOT_FOUND));
        }
    }

    /**
     * Checks whether an observe relationship has to be established or canceled. This is done here to have a
     * server-global observeManager that holds the set of remote endpoints for all resources. This global knowledge is
     * required for efficient orphan handling.
     *
     * @param exchange the exchange of the current request
     * @param resource the target resource
     * @param path the path to the resource
     */
    private void checkForObserveOption(final Exchange exchange, final Resource resource) {
        final Request request = exchange.getRequest();
        if (request.getCode() != Code.GET) {
            return;
        }

        final InetSocketAddress source = new InetSocketAddress(request.getSource(), request.getSourcePort());

        if (request.getOptions().hasObserve() && resource.isObservable()) {

            if (request.getOptions().getObserve() == 0) {
                // Requests wants to observe and resource allows it :-)
                LOGGER.info("Initiate an observe relation between " + request.getSource() + ":"
                        + request.getSourcePort() + " and resource " + resource.getURI());
                final ObservingEndpoint remote = observeManager.findObservingEndpoint(source);
                final ObserveRelation relation = new ObserveRelation(remote, resource, exchange);
                remote.addObserveRelation(relation);
                exchange.setRelation(relation);
                // all that's left is to add the relation to the resource which
                // the resource must do itself if the response is successful
            } else if (request.getOptions().getObserve() == 1) {
                final ObserveRelation relation = observeManager.getRelation(source, request.getToken());
                if (relation != null) {
                    relation.cancel();
                }
            }
        }
    }

    /**
     * Searches in the resource tree for the specified path. A parent resource may accept requests to subresources,
     * e.g., to allow addresses with wildcards like <code>coap://example.com:5683/devices/*</code>
     *
     * @param list the path as list of resource names
     * @return the resource or null if not found
     */
    public Resource findResource(final List<String> list, final Code code) {
        final Resource result = searchResourceTree(list);
        if (result == null && shouldDeliverAbsenteeToParent(list, code)) {
            return searchResourceTree(list.subList(0, list.size() - 1));
        }
        return result;
    }

    private Resource searchResourceTree(final List<String> list) {
        final LinkedList<String> path = new LinkedList<String>(list);
        Resource current = root;
        while (!path.isEmpty() && current != null) {
            final String name = path.removeFirst();
            current = current.getChild(name);
        }
        return current;
    }

    private boolean shouldDeliverAbsenteeToParent(final List<String> list, final Code code) {
        return code == Code.POST && list.size() == 2;
    }

    @Override
    public void deliverResponse(final Exchange exchange, final Response response) {
        if (response == null) {
            throw new NullPointerException();
        }
        if (exchange == null) {
            throw new NullPointerException();
        }
        if (exchange.getRequest() == null) {
            throw new NullPointerException();
        }
        exchange.getRequest().setResponse(response);
    }
}
