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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für StyleMapType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="StyleMapType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractStyleSelectorType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Pair" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}StyleMapSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}StyleMapObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StyleMapType", propOrder = {
    "pair",
    "styleMapSimpleExtensionGroup",
    "styleMapObjectExtensionGroup"
})
public class StyleMapType
    extends AbstractStyleSelectorType
{

    @XmlElement(name = "Pair")
    protected List<PairType> pair;
    @XmlElement(name = "StyleMapSimpleExtensionGroup")
    protected List<Object> styleMapSimpleExtensionGroup;
    @XmlElement(name = "StyleMapObjectExtensionGroup")
    protected List<AbstractObjectType> styleMapObjectExtensionGroup;

    /**
     * Gets the value of the pair property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pair property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPair().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PairType }
     * 
     * 
     */
    public List<PairType> getPair() {
        if (pair == null) {
            pair = new ArrayList<PairType>();
        }
        return this.pair;
    }

    public boolean isSetPair() {
        return ((this.pair!= null)&&(!this.pair.isEmpty()));
    }

    public void unsetPair() {
        this.pair = null;
    }

    /**
     * Gets the value of the styleMapSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the styleMapSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStyleMapSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getStyleMapSimpleExtensionGroup() {
        if (styleMapSimpleExtensionGroup == null) {
            styleMapSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.styleMapSimpleExtensionGroup;
    }

    public boolean isSetStyleMapSimpleExtensionGroup() {
        return ((this.styleMapSimpleExtensionGroup!= null)&&(!this.styleMapSimpleExtensionGroup.isEmpty()));
    }

    public void unsetStyleMapSimpleExtensionGroup() {
        this.styleMapSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the styleMapObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the styleMapObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStyleMapObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getStyleMapObjectExtensionGroup() {
        if (styleMapObjectExtensionGroup == null) {
            styleMapObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.styleMapObjectExtensionGroup;
    }

    public boolean isSetStyleMapObjectExtensionGroup() {
        return ((this.styleMapObjectExtensionGroup!= null)&&(!this.styleMapObjectExtensionGroup.isEmpty()));
    }

    public void unsetStyleMapObjectExtensionGroup() {
        this.styleMapObjectExtensionGroup = null;
    }

    public void setPair(List<PairType> value) {
        this.pair = value;
    }

    public void setStyleMapSimpleExtensionGroup(List<Object> value) {
        this.styleMapSimpleExtensionGroup = value;
    }

    public void setStyleMapObjectExtensionGroup(List<AbstractObjectType> value) {
        this.styleMapObjectExtensionGroup = value;
    }

}
