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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für FolderType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="FolderType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractContainerType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractFeatureGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}FolderSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}FolderObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FolderType", propOrder = {
    "abstractFeatureGroup",
    "folderSimpleExtensionGroup",
    "folderObjectExtensionGroup"
})
public class FolderType
    extends AbstractContainerType
{

    @XmlElementRef(name = "AbstractFeatureGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected List<JAXBElement<? extends AbstractFeatureType>> abstractFeatureGroup;
    @XmlElement(name = "FolderSimpleExtensionGroup")
    protected List<Object> folderSimpleExtensionGroup;
    @XmlElement(name = "FolderObjectExtensionGroup")
    protected List<AbstractObjectType> folderObjectExtensionGroup;

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
     * {@link JAXBElement }{@code <}{@link FolderType }{@code >}
     * {@link JAXBElement }{@code <}{@link DocumentType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractContainerType }{@code >}
     * {@link JAXBElement }{@code <}{@link NetworkLinkType }{@code >}
     * {@link JAXBElement }{@code <}{@link PlacemarkType }{@code >}
     * {@link JAXBElement }{@code <}{@link ScreenOverlayType }{@code >}
     * {@link JAXBElement }{@code <}{@link PhotoOverlayType }{@code >}
     * {@link JAXBElement }{@code <}{@link GroundOverlayType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractOverlayType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractFeatureType }{@code >}
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
     * Gets the value of the folderSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the folderSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFolderSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getFolderSimpleExtensionGroup() {
        if (folderSimpleExtensionGroup == null) {
            folderSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.folderSimpleExtensionGroup;
    }

    public boolean isSetFolderSimpleExtensionGroup() {
        return ((this.folderSimpleExtensionGroup!= null)&&(!this.folderSimpleExtensionGroup.isEmpty()));
    }

    public void unsetFolderSimpleExtensionGroup() {
        this.folderSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the folderObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the folderObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFolderObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getFolderObjectExtensionGroup() {
        if (folderObjectExtensionGroup == null) {
            folderObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.folderObjectExtensionGroup;
    }

    public boolean isSetFolderObjectExtensionGroup() {
        return ((this.folderObjectExtensionGroup!= null)&&(!this.folderObjectExtensionGroup.isEmpty()));
    }

    public void unsetFolderObjectExtensionGroup() {
        this.folderObjectExtensionGroup = null;
    }

    public void setAbstractFeatureGroup(List<JAXBElement<? extends AbstractFeatureType>> value) {
        this.abstractFeatureGroup = value;
    }

    public void setFolderSimpleExtensionGroup(List<Object> value) {
        this.folderSimpleExtensionGroup = value;
    }

    public void setFolderObjectExtensionGroup(List<AbstractObjectType> value) {
        this.folderObjectExtensionGroup = value;
    }

}
