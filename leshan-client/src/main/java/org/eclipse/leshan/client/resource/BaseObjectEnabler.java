package org.eclipse.leshan.client.resource;

import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.objectspec.ObjectSpec;
import org.eclipse.leshan.core.objectspec.ResourceSpec;
import org.eclipse.leshan.core.objectspec.Resources;
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

public class BaseObjectEnabler implements LwM2mObjectEnabler {

    int id;
    private NotifySender notifySender;

    public BaseObjectEnabler(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public final CreateResponse create(CreateRequest request) {
        // we can not create new instance on single object
        ObjectSpec objectSpec = Resources.getObjectSpec(request.getPath().getObjectId());
        if (objectSpec != null && !objectSpec.multiple) {
            return new CreateResponse(ResponseCode.METHOD_NOT_ALLOWED);
        }

        // TODO we could do a validation of request.getObjectInstance() by comparing with resourceSpec information.

        return doCreate(request);
    }

    protected CreateResponse doCreate(CreateRequest request) {
        return new CreateResponse(ResponseCode.BAD_REQUEST);
    }

    @Override
    public final ValueResponse read(ReadRequest request) {
        LwM2mPath path = request.getPath();

        // check if the resource is readable
        if (path.isResource()) {
            ResourceSpec resourceSpec = Resources.getResourceSpec(path.getObjectId(), path.getResourceId());
            if (resourceSpec != null && !resourceSpec.operations.isReadable()) {
                return new ValueResponse(ResponseCode.METHOD_NOT_ALLOWED);
            }
        }

        return doRead(request);

        // TODO we could do a validation of response.getContent by comparing with the spec.
    }

    protected ValueResponse doRead(ReadRequest request) {
        return new ValueResponse(ResponseCode.BAD_REQUEST);
    }

    @Override
    public final LwM2mResponse write(WriteRequest request) {
        LwM2mPath path = request.getPath();

        // check if the resource is writable
        if (path.isResource()) {
            ResourceSpec resourceSpec = Resources.getResourceSpec(path.getObjectId(), path.getResourceId());
            if (resourceSpec != null && !resourceSpec.operations.isWritable()) {
                return new LwM2mResponse(ResponseCode.METHOD_NOT_ALLOWED);
            }
        }

        // TODO we could do a validation of request.getNode() by comparing with resourceSpec information

        return doWrite(request);
    }

    protected LwM2mResponse doWrite(WriteRequest request) {
        return new LwM2mResponse(ResponseCode.BAD_REQUEST);
    }

    @Override
    public final LwM2mResponse delete(DeleteRequest request) {
        // we can not create new instance on single object
        ObjectSpec objectSpec = Resources.getObjectSpec(request.getPath().getObjectId());
        if (objectSpec != null && !objectSpec.multiple) {
            return new CreateResponse(ResponseCode.METHOD_NOT_ALLOWED);
        }

        return doDelete(request);
    }

    protected LwM2mResponse doDelete(DeleteRequest request) {
        return new LwM2mResponse(ResponseCode.BAD_REQUEST);
    }

    @Override
    public final LwM2mResponse execute(ExecuteRequest request) {
        LwM2mPath path = request.getPath();

        // only resource could be executed
        if (!path.isResource()) {
            return new LwM2mResponse(ResponseCode.BAD_REQUEST);
        }

        // check if the resource is writable
        ResourceSpec resourceSpec = Resources.getResourceSpec(path.getObjectId(), path.getResourceId());
        if (resourceSpec != null && !resourceSpec.operations.isExecutable()) {
            return new LwM2mResponse(ResponseCode.METHOD_NOT_ALLOWED);
        }

        return doExecute(request);
    }

    protected LwM2mResponse doExecute(ExecuteRequest request) {
        return new LwM2mResponse(ResponseCode.BAD_REQUEST);
    }

    @Override
    public LwM2mResponse writeAttributes(WriteAttributesRequest request) {
        // TODO should be implemented here to be available for all object enabler
        return new LwM2mResponse(ResponseCode.BAD_REQUEST);
    }

    @Override
    public DiscoverResponse discover(DiscoverRequest request) {
        // TODO should be implemented here to be available for all object enabler
        return new DiscoverResponse(ResponseCode.BAD_REQUEST);
    }

    @Override
    public ValueResponse observe(ObserveRequest request) {
        return this.read(new ReadRequest(request.getPath().toString()));
    }

    @Override
    public void setNotifySender(NotifySender sender) {
        notifySender = sender;
    }

    public NotifySender getNotifySender() {
        return notifySender;
    }
}
