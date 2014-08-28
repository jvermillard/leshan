
package leshan.client.lwm2m.object.xsd;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the leshan.client.lwm2m.object.xsd package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: leshan.client.lwm2m.object.xsd
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link LWM2M }
     * 
     */
    public LWM2M createLWM2M() {
        return new LWM2M();
    }

    /**
     * Create an instance of {@link Object }
     * 
     */
    public Object createLWM2MObject() {
        return new Object();
    }

    /**
     * Create an instance of {@link Resources }
     * 
     */
    public Resources createLWM2MObjectResources() {
        return new Resources();
    }

    /**
     * Create an instance of {@link Item }
     * 
     */
    public Item createLWM2MObjectResourcesItem() {
        return new Item();
    }

}
