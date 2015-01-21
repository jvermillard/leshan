/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Zebra Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.client.request;

import java.util.Map;

import org.eclipse.leshan.client.request.identifier.ClientIdentifier;

public class UpdateRequest extends AbstractRegisteredLwM2mClientRequest implements LwM2mContentRequest {

    private final Map<String, String> updatedParameters;

    public UpdateRequest(final ClientIdentifier clientIdentifier, final Map<String, String> updatedParameters) {
        super(clientIdentifier);
        this.updatedParameters = updatedParameters;
    }

    public UpdateRequest(final ClientIdentifier clientIdentifier, final long timeout,
            final Map<String, String> updatedParameters) {
        super(clientIdentifier, timeout);
        this.updatedParameters = updatedParameters;
    }

    @Override
    public Map<String, String> getClientParameters() {
        return updatedParameters;
    }

    @Override
    public void accept(final LwM2mClientRequestVisitor visitor) {
        visitor.visit(this);
    }
}
