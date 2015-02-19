package org.eclipse.leshan.client.resource;

import org.eclipse.leshan.core.request.CreateRequest;
import org.eclipse.leshan.core.request.DeleteRequest;
import org.eclipse.leshan.core.request.DiscoverRequest;
import org.eclipse.leshan.core.request.ExecuteRequest;
import org.eclipse.leshan.core.request.ObserveRequest;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.request.WriteAttributesRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.response.CreateResponse;
import org.eclipse.leshan.core.response.DiscoverResponse;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ValueResponse;

public interface LwM2mObjectEnabler {

    int getId();

    CreateResponse create(CreateRequest request);

    ValueResponse read(ReadRequest request);

    LwM2mResponse write(WriteRequest request);

    LwM2mResponse delete(DeleteRequest request);

    LwM2mResponse execute(ExecuteRequest request);

    LwM2mResponse writeAttributes(WriteAttributesRequest request);

    DiscoverResponse discover(DiscoverRequest request);

    ValueResponse observe(ObserveRequest request);

    void setNotifySender(NotifySender sender);
}
