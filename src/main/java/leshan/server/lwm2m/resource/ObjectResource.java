package leshan.server.lwm2m.resource;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class ObjectResource extends ResourceBase {

    public ObjectResource(String name) {
        super(name);
    }

    /**
     * Append the Link Format representation of this object to the given list.
     */
    public void appendLinks(Collection<String> links, String parentObject) {
        StringBuilder builder = new StringBuilder();
        if (this.getChildren().isEmpty()) {
            builder.append("</");
            if (StringUtils.isNotBlank(parentObject)) {
                builder.append(parentObject + "/");
            }
            builder.append(this.getName()).append(">");
            links.add(builder.toString());
        } else {
            for (Resource child : this.getChildren()) {
                ((ObjectResource) child).appendLinks(links, this.getName());
            }
        }
    }

}
