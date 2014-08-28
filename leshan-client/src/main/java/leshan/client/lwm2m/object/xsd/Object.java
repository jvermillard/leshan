package leshan.client.lwm2m.object.xsd;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Description1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ObjectID" type="{http://www.w3.org/2001/XMLSchema}unsignedByte"/>
 *         &lt;element name="ObjectURN" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MultipleInstances">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Multiple"/>
 *               &lt;enumeration value="Single"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Mandatory">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Mandatory"/>
 *               &lt;enumeration value="Optional"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Resources">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Item" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="Operations">
 *                               &lt;simpleType>
 *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                   &lt;enumeration value="R"/>
 *                                   &lt;enumeration value="W"/>
 *                                   &lt;enumeration value="RW"/>
 *                                   &lt;enumeration value="E"/>
 *                                   &lt;enumeration value="RE"/>
 *                                   &lt;enumeration value="WE"/>
 *                                   &lt;enumeration value="RWE"/>
 *                                   &lt;enumeration value=""/>
 *                                 &lt;/restriction>
 *                               &lt;/simpleType>
 *                             &lt;/element>
 *                             &lt;element name="MultipleInstances">
 *                               &lt;simpleType>
 *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                   &lt;enumeration value="Multiple"/>
 *                                   &lt;enumeration value="Single"/>
 *                                 &lt;/restriction>
 *                               &lt;/simpleType>
 *                             &lt;/element>
 *                             &lt;element name="Mandatory">
 *                               &lt;simpleType>
 *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                   &lt;enumeration value="Mandatory"/>
 *                                   &lt;enumeration value="Optional"/>
 *                                 &lt;/restriction>
 *                               &lt;/simpleType>
 *                             &lt;/element>
 *                             &lt;element name="Type">
 *                               &lt;simpleType>
 *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                   &lt;enumeration value="String"/>
 *                                   &lt;enumeration value="Integer"/>
 *                                   &lt;enumeration value="Float"/>
 *                                   &lt;enumeration value="Boolean"/>
 *                                   &lt;enumeration value="Opaque"/>
 *                                   &lt;enumeration value="Time"/>
 *                                   &lt;enumeration value="Integer"/>
 *                                   &lt;enumeration value=""/>
 *                                 &lt;/restriction>
 *                               &lt;/simpleType>
 *                             &lt;/element>
 *                             &lt;element name="RangeEnumeration" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="Units" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                           &lt;/sequence>
 *                           &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Description2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ObjectType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "description1",
    "objectID",
    "objectURN",
    "multipleInstances",
    "mandatory",
    "resources",
    "description2"
})
public class Object {

    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "Description1", required = true)
    protected String description1;
    @XmlElement(name = "ObjectID")
    @XmlSchemaType(name = "unsignedByte")
    protected short objectID;
    @XmlElement(name = "ObjectURN", required = true)
    protected String objectURN;
    @XmlElement(name = "MultipleInstances", required = true)
    protected String multipleInstances;
    @XmlElement(name = "Mandatory", required = true)
    protected String mandatory;
    @XmlElement(name = "Resources", required = true)
    protected Resources resources;
    @XmlElement(name = "Description2", required = true)
    protected String description2;
    @XmlAttribute(name = "ObjectType", required = true)
    protected String objectType;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the description1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription1() {
        return description1;
    }

    /**
     * Sets the value of the description1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription1(String value) {
        this.description1 = value;
    }

    /**
     * Gets the value of the objectID property.
     * 
     */
    public short getObjectID() {
        return objectID;
    }

    /**
     * Sets the value of the objectID property.
     * 
     */
    public void setObjectID(short value) {
        this.objectID = value;
    }

    /**
     * Gets the value of the objectURN property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectURN() {
        return objectURN;
    }

    /**
     * Sets the value of the objectURN property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectURN(String value) {
        this.objectURN = value;
    }

    /**
     * Gets the value of the multipleInstances property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMultipleInstances() {
        return multipleInstances;
    }

    /**
     * Sets the value of the multipleInstances property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMultipleInstances(String value) {
        this.multipleInstances = value;
    }

    /**
     * Gets the value of the mandatory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMandatory() {
        return mandatory;
    }

    /**
     * Sets the value of the mandatory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMandatory(String value) {
        this.mandatory = value;
    }

    /**
     * Gets the value of the resources property.
     * 
     * @return
     *     possible object is
     *     {@link Resources }
     *     
     */
    public Resources getResources() {
        return resources;
    }

    /**
     * Sets the value of the resources property.
     * 
     * @param value
     *     allowed object is
     *     {@link Resources }
     *     
     */
    public void setResources(Resources value) {
        this.resources = value;
    }

    /**
     * Gets the value of the description2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription2() {
        return description2;
    }

    /**
     * Sets the value of the description2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription2(String value) {
        this.description2 = value;
    }

    /**
     * Gets the value of the objectType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Sets the value of the objectType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectType(String value) {
        this.objectType = value;
    }

}