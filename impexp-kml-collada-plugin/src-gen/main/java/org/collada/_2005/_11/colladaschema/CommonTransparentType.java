//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package org.collada._2005._11.colladaschema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für common_transparent_type complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="common_transparent_type">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.collada.org/2005/11/COLLADASchema}common_color_or_texture_type">
 *       &lt;attribute name="opaque" type="{http://www.collada.org/2005/11/COLLADASchema}fx_opaque_enum" default="A_ONE" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "common_transparent_type")
public class CommonTransparentType
    extends CommonColorOrTextureType
{

    @XmlAttribute(name = "opaque")
    protected FxOpaqueEnum opaque;

    /**
     * Ruft den Wert der opaque-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FxOpaqueEnum }
     *     
     */
    public FxOpaqueEnum getOpaque() {
        if (opaque == null) {
            return FxOpaqueEnum.A_ONE;
        } else {
            return opaque;
        }
    }

    /**
     * Legt den Wert der opaque-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FxOpaqueEnum }
     *     
     */
    public void setOpaque(FxOpaqueEnum value) {
        this.opaque = value;
    }

    public boolean isSetOpaque() {
        return (this.opaque!= null);
    }

}
