//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package net.opengis.kml._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für NetworkLinkType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="NetworkLinkType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractFeatureType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}refreshVisibility" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}flyToView" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}Url" minOccurs="0"/&gt;
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}Link" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}NetworkLinkSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}NetworkLinkObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NetworkLinkType", propOrder = {
    "rest"
})
public class NetworkLinkType
    extends AbstractFeatureType
{

    @XmlElementRefs({
        @XmlElementRef(name = "refreshVisibility", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "flyToView", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "Url", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "Link", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "NetworkLinkSimpleExtensionGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "NetworkLinkObjectExtensionGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> rest;

    /**
     * Ruft das restliche Contentmodell ab. 
     * 
     * <p>
     * Sie rufen diese "catch-all"-Eigenschaft aus folgendem Grund ab: 
     * Der Feldname "Link" wird von zwei verschiedenen Teilen eines Schemas verwendet. Siehe: 
     * Zeile 744 von file:/C:/devel/java/impexp/impexp-kml-collada-plugin/resources/jaxb/kml/2.2/ogckml22.xsd
     * Zeile 312 von file:/C:/devel/java/impexp/impexp-kml-collada-plugin/resources/jaxb/kml/2.2/ogckml22.xsd
     * <p>
     * Um diese Eigenschaft zu entfernen, wenden Sie eine Eigenschaftenanpassung für eine
     * der beiden folgenden Deklarationen an, um deren Namen zu ändern: 
     * Gets the value of the rest property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rest property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link LinkType }{@code >}
     * {@link JAXBElement }{@code <}{@link LinkType }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractObjectType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getRest() {
        if (rest == null) {
            rest = new ArrayList<JAXBElement<?>>();
        }
        return this.rest;
    }

    public void setRest(List<JAXBElement<?>> value) {
        this.rest = value;
    }

}
