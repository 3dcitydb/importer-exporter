//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package net.opengis.kml._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für DocumentType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="DocumentType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractContainerType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Schema" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractFeatureGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}DocumentSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}DocumentObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DocumentType", propOrder = {
    "schema",
    "abstractFeatureGroup",
    "documentSimpleExtensionGroup",
    "documentObjectExtensionGroup"
})
public class DocumentType
    extends AbstractContainerType
{

    @XmlElement(name = "Schema")
    protected List<SchemaType> schema;
    @XmlElementRef(name = "AbstractFeatureGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected List<JAXBElement<? extends AbstractFeatureType>> abstractFeatureGroup;
    @XmlElement(name = "DocumentSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> documentSimpleExtensionGroup;
    @XmlElement(name = "DocumentObjectExtensionGroup")
    protected List<AbstractObjectType> documentObjectExtensionGroup;

    /**
     * Gets the value of the schema property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the schema property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSchema().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SchemaType }
     * 
     * 
     */
    public List<SchemaType> getSchema() {
        if (schema == null) {
            schema = new ArrayList<SchemaType>();
        }
        return this.schema;
    }

    public boolean isSetSchema() {
        return ((this.schema!= null)&&(!this.schema.isEmpty()));
    }

    public void unsetSchema() {
        this.schema = null;
    }

    /**
     * Gets the value of the abstractFeatureGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractFeatureGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractFeatureGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link AbstractContainerType }{@code >}
     * {@link JAXBElement }{@code <}{@link NetworkLinkType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractOverlayType }{@code >}
     * {@link JAXBElement }{@code <}{@link ScreenOverlayType }{@code >}
     * {@link JAXBElement }{@code <}{@link GroundOverlayType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractFeatureType }{@code >}
     * {@link JAXBElement }{@code <}{@link PhotoOverlayType }{@code >}
     * {@link JAXBElement }{@code <}{@link DocumentType }{@code >}
     * {@link JAXBElement }{@code <}{@link FolderType }{@code >}
     * {@link JAXBElement }{@code <}{@link PlacemarkType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends AbstractFeatureType>> getAbstractFeatureGroup() {
        if (abstractFeatureGroup == null) {
            abstractFeatureGroup = new ArrayList<JAXBElement<? extends AbstractFeatureType>>();
        }
        return this.abstractFeatureGroup;
    }

    public boolean isSetAbstractFeatureGroup() {
        return ((this.abstractFeatureGroup!= null)&&(!this.abstractFeatureGroup.isEmpty()));
    }

    public void unsetAbstractFeatureGroup() {
        this.abstractFeatureGroup = null;
    }

    /**
     * Gets the value of the documentSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the documentSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDocumentSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getDocumentSimpleExtensionGroup() {
        if (documentSimpleExtensionGroup == null) {
            documentSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.documentSimpleExtensionGroup;
    }

    public boolean isSetDocumentSimpleExtensionGroup() {
        return ((this.documentSimpleExtensionGroup!= null)&&(!this.documentSimpleExtensionGroup.isEmpty()));
    }

    public void unsetDocumentSimpleExtensionGroup() {
        this.documentSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the documentObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the documentObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDocumentObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getDocumentObjectExtensionGroup() {
        if (documentObjectExtensionGroup == null) {
            documentObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.documentObjectExtensionGroup;
    }

    public boolean isSetDocumentObjectExtensionGroup() {
        return ((this.documentObjectExtensionGroup!= null)&&(!this.documentObjectExtensionGroup.isEmpty()));
    }

    public void unsetDocumentObjectExtensionGroup() {
        this.documentObjectExtensionGroup = null;
    }

    public void setSchema(List<SchemaType> value) {
        this.schema = value;
    }

    public void setAbstractFeatureGroup(List<JAXBElement<? extends AbstractFeatureType>> value) {
        this.abstractFeatureGroup = value;
    }

    public void setDocumentSimpleExtensionGroup(List<Object> value) {
        this.documentSimpleExtensionGroup = value;
    }

    public void setDocumentObjectExtensionGroup(List<AbstractObjectType> value) {
        this.documentObjectExtensionGroup = value;
    }

}
