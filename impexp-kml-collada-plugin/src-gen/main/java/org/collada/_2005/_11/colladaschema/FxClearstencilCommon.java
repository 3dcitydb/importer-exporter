//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.11.18 um 03:45:53 PM CET 
//


package org.collada._2005._11.colladaschema;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java-Klasse für fx_clearstencil_common complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="fx_clearstencil_common"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;byte"&gt;
 *       &lt;attribute name="index" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" default="0" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fx_clearstencil_common", propOrder = {
    "value"
})
public class FxClearstencilCommon {

    @XmlValue
    protected byte value;
    @XmlAttribute(name = "index")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger index;

    /**
     * Ruft den Wert der value-Eigenschaft ab.
     * 
     */
    public byte getValue() {
        return value;
    }

    /**
     * Legt den Wert der value-Eigenschaft fest.
     * 
     */
    public void setValue(byte value) {
        this.value = value;
    }

    public boolean isSetValue() {
        return true;
    }

    /**
     * Ruft den Wert der index-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getIndex() {
        if (index == null) {
            return new BigInteger("0");
        } else {
            return index;
        }
    }

    /**
     * Legt den Wert der index-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setIndex(BigInteger value) {
        this.index = value;
    }

    public boolean isSetIndex() {
        return (this.index!= null);
    }

}
