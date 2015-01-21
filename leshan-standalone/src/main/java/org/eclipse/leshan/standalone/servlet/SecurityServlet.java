/*
 * Copyright (c) 2013, Sierra Wireless
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
package org.eclipse.leshan.standalone.servlet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PublicKey;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.leshan.server.security.NonUniqueSecurityInfoException;
import org.eclipse.leshan.server.security.SecurityInfo;
import org.eclipse.leshan.server.security.SecurityRegistry;
import org.eclipse.leshan.standalone.servlet.json.SecurityDeserializer;
import org.eclipse.leshan.standalone.servlet.json.SecuritySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * Service HTTP REST API calls for security information.
 */
public class SecurityServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityServlet.class);

    private static final long serialVersionUID = 1L;

    private final SecurityRegistry registry;

    private final Gson gsonSer;
    private final Gson gsonDes;

    public SecurityServlet(SecurityRegistry registry) {
        this.registry = registry;

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(SecurityInfo.class, new SecuritySerializer());
        this.gsonSer = builder.create();

        builder = new GsonBuilder();
        builder.registerTypeAdapter(SecurityInfo.class, new SecurityDeserializer());
        this.gsonDes = builder.create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] path = StringUtils.split(req.getPathInfo(), '/');

        if (path.length != 1 && "clients".equals(path[0])) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            SecurityInfo info = gsonDes.fromJson(new InputStreamReader(req.getInputStream()), SecurityInfo.class);
            LOG.debug("New security info for end-point {}: {}", info.getEndpoint(), info);

            registry.add(info);

            resp.setStatus(HttpServletResponse.SC_OK);

        } catch (NonUniqueSecurityInfoException e) {
            LOG.warn("Non unique security info: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().append(e.getMessage()).flush();
        } catch (JsonParseException e) {
            LOG.warn("Could not parse request body", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().append("Invalid request body").flush();
        } catch (RuntimeException e) {
            LOG.warn("unexpected error for request " + req.getPathInfo(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] path = StringUtils.split(req.getPathInfo(), '/');

        if (path.length != 1) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if ("clients".equals(path[0])) {
            Collection<SecurityInfo> infos = this.registry.getAll();

            String json = this.gsonSer.toJson(infos);
            resp.setContentType("application/json");
            resp.getOutputStream().write(json.getBytes("UTF-8"));
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        if ("server".equals(path[0])) {
            PublicKey publicKey = this.registry.getServerPublicKey();
            String json = this.gsonSer.toJson(SecurityInfo.newRawPublicKeyInfo("leshan", publicKey));
            resp.setContentType("application/json");
            resp.getOutputStream().write(json.getBytes("UTF-8"));
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] path = StringUtils.split(req.getPathInfo(), '/');

        if (path.length != 2 && !"clients".equals(path[0])) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String endpoint = path[1];

        LOG.debug("Removing security info for end-point {}", endpoint);
        if (this.registry.remove(endpoint) != null) {
            resp.sendError(HttpServletResponse.SC_OK);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
