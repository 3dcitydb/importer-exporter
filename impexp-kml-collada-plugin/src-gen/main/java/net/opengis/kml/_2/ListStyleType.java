//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package net.opengis.kml._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für ListStyleType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ListStyleType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractSubStyleType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}listItemType" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}bgColor" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ItemIcon" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}maxSnippetLines" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ListStyleSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ListStyleObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListStyleType", propOrder = {
    "listItemType",
    "bgColor",
    "itemIcon",
    "maxSnippetLines",
    "listStyleSimpleExtensionGroup",
    "listStyleObjectExtensionGroup"
})
public class ListStyleType
    extends AbstractSubStyleType
{

    @XmlElement(defaultValue = "check")
    @XmlSchemaType(name = "string")
    protected ListItemTypeEnumType listItemType;
    @XmlElement(type = String.class, defaultValue = "ffffffff")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    @XmlSchemaType(name = "hexBinary")
    protected byte[] bgColor;
    @XmlElement(name = "ItemIcon")
    protected List<ItemIconType> itemIcon;
    @XmlElement(defaultValue = "2")
    protected Integer maxSnippetLines;
    @XmlElement(name = "ListStyleSimpleExtensionGroup")
    protected List<Object> listStyleSimpleExtensionGroup;
    @XmlElement(name = "ListStyleObjectExtensionGroup")
    protected List<AbstractObjectType> listStyleObjectExtensionGroup;

    /**
     * Ruft den Wert der listItemType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ListItemTypeEnumType }
     *     
     */
    public ListItemTypeEnumType getListItemType() {
        return listItemType;
    }

    /**
     * Legt den Wert der listItemType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ListItemTypeEnumType }
     *     
     */
    public void setListItemType(ListItemTypeEnumType value) {
        this.listItemType = value;
    }

    public boolean isSetListItemType() {
        return (this.listItemType!= null);
    }

    /**
     * Ruft den Wert der bgColor-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getBgColor() {
        return bgColor;
    }

    /**
     * Legt den Wert der bgColor-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBgColor(byte[] value) {
        this.bgColor = value;
    }

    public boolean isSetBgColor() {
        return (this.bgColor!= null);
    }

    /**
     * Gets the value of the itemIcon property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the itemIcon property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItemIcon().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ItemIconType }
     * 
     * 
     */
    public List<ItemIconType> getItemIcon() {
        if (itemIcon == null) {
            itemIcon = new ArrayList<ItemIconType>();
        }
        return this.itemIcon;
    }

    public boolean isSetItemIcon() {
        return ((this.itemIcon!= null)&&(!this.itemIcon.isEmpty()));
    }

    public void unsetItemIcon() {
        this.itemIcon = null;
    }

    /**
     * Ruft den Wert der maxSnippetLines-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxSnippetLines() {
        return maxSnippetLines;
    }

    /**
     * Legt den Wert der maxSnippetLines-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxSnippetLines(Integer value) {
        this.maxSnippetLines = value;
    }

    public boolean isSetMaxSnippetLines() {
        return (this.maxSnippetLines!= null);
    }

    /**
     * Gets the value of the listStyleSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the listStyleSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getListStyleSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getListStyleSimpleExtensionGroup() {
        if (listStyleSimpleExtensionGroup == null) {
            listStyleSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.listStyleSimpleExtensionGroup;
    }

    public boolean isSetListStyleSimpleExtensionGroup() {
        return ((this.listStyleSimpleExtensionGroup!= null)&&(!this.listStyleSimpleExtensionGroup.isEmpty()));
    }

    public void unsetListStyleSimpleExtensionGroup() {
        this.listStyleSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the listStyleObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the listStyleObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getListStyleObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getListStyleObjectExtensionGroup() {
        if (listStyleObjectExtensionGroup == null) {
            listStyleObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.listStyleObjectExtensionGroup;
    }

    public boolean isSetListStyleObjectExtensionGroup() {
        return ((this.listStyleObjectExtensionGroup!= null)&&(!this.listStyleObjectExtensionGroup.isEmpty()));
    }

    public void unsetListStyleObjectExtensionGroup() {
        this.listStyleObjectExtensionGroup = null;
    }

    public void setItemIcon(List<ItemIconType> value) {
        this.itemIcon = value;
    }

    public void setListStyleSimpleExtensionGroup(List<Object> value) {
        this.listStyleSimpleExtensionGroup = value;
    }

    public void setListStyleObjectExtensionGroup(List<AbstractObjectType> value) {
        this.listStyleObjectExtensionGroup = value;
    }

}
