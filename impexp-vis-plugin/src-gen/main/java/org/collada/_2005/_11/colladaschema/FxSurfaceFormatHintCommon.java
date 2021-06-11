//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package org.collada._2005._11.colladaschema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * If the exact format cannot be resolve via other methods then the format_hint will describe the important features of the format so that the application may select a compatable or close format
 * 
 * <p>Java-Klasse für fx_surface_format_hint_common complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="fx_surface_format_hint_common"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="channels" type="{http://www.collada.org/2005/11/COLLADASchema}fx_surface_format_hint_channels_enum"/&gt;
 *         &lt;element name="range" type="{http://www.collada.org/2005/11/COLLADASchema}fx_surface_format_hint_range_enum"/&gt;
 *         &lt;element name="precision" type="{http://www.collada.org/2005/11/COLLADASchema}fx_surface_format_hint_precision_enum" minOccurs="0"/&gt;
 *         &lt;element name="option" type="{http://www.collada.org/2005/11/COLLADASchema}fx_surface_format_hint_option_enum" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fx_surface_format_hint_common", propOrder = {
    "channels",
    "range",
    "precision",
    "option",
    "extra"
})
public class FxSurfaceFormatHintCommon {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected FxSurfaceFormatHintChannelsEnum channels;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected FxSurfaceFormatHintRangeEnum range;
    @XmlSchemaType(name = "string")
    protected FxSurfaceFormatHintPrecisionEnum precision;
    @XmlSchemaType(name = "string")
    protected List<FxSurfaceFormatHintOptionEnum> option;
    protected List<Extra> extra;

    /**
     * Ruft den Wert der channels-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSurfaceFormatHintChannelsEnum }
     *     
     */
    public FxSurfaceFormatHintChannelsEnum getChannels() {
        return channels;
    }

    /**
     * Legt den Wert der channels-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSurfaceFormatHintChannelsEnum }
     *     
     */
    public void setChannels(FxSurfaceFormatHintChannelsEnum value) {
        this.channels = value;
    }

    public boolean isSetChannels() {
        return (this.channels!= null);
    }

    /**
     * Ruft den Wert der range-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSurfaceFormatHintRangeEnum }
     *     
     */
    public FxSurfaceFormatHintRangeEnum getRange() {
        return range;
    }

    /**
     * Legt den Wert der range-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSurfaceFormatHintRangeEnum }
     *     
     */
    public void setRange(FxSurfaceFormatHintRangeEnum value) {
        this.range = value;
    }

    public boolean isSetRange() {
        return (this.range!= null);
    }

    /**
     * Ruft den Wert der precision-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxSurfaceFormatHintPrecisionEnum }
     *     
     */
    public FxSurfaceFormatHintPrecisionEnum getPrecision() {
        return precision;
    }

    /**
     * Legt den Wert der precision-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxSurfaceFormatHintPrecisionEnum }
     *     
     */
    public void setPrecision(FxSurfaceFormatHintPrecisionEnum value) {
        this.precision = value;
    }

    public boolean isSetPrecision() {
        return (this.precision!= null);
    }

    /**
     * Gets the value of the option property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the option property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOption().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FxSurfaceFormatHintOptionEnum }
     * 
     * 
     */
    public List<FxSurfaceFormatHintOptionEnum> getOption() {
        if (option == null) {
            option = new ArrayList<FxSurfaceFormatHintOptionEnum>();
        }
        return this.option;
    }

    public boolean isSetOption() {
        return ((this.option!= null)&&(!this.option.isEmpty()));
    }

    public void unsetOption() {
        this.option = null;
    }

    /**
     * Gets the value of the extra property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the extra property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExtra().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Extra }
     * 
     * 
     */
    public List<Extra> getExtra() {
        if (extra == null) {
            extra = new ArrayList<Extra>();
        }
        return this.extra;
    }

    public boolean isSetExtra() {
        return ((this.extra!= null)&&(!this.extra.isEmpty()));
    }

    public void unsetExtra() {
        this.extra = null;
    }

    public void setOption(List<FxSurfaceFormatHintOptionEnum> value) {
        this.option = value;
    }

    public void setExtra(List<Extra> value) {
        this.extra = value;
    }

}
