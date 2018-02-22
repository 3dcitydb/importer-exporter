//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package org.collada._2005._11.colladaschema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 * 			Defines a set of texturing commands that will be converted into multitexturing operations using glTexEnv in regular and combiner mode.
 * 			
 * 
 * <p>Java-Klasse für gles_texture_pipeline complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="gles_texture_pipeline">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="texcombiner" type="{http://www.collada.org/2005/11/COLLADASchema}gles_texcombiner_command_type"/>
 *         &lt;element name="texenv" type="{http://www.collada.org/2005/11/COLLADASchema}gles_texenv_command_type"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra"/>
 *       &lt;/choice>
 *       &lt;attribute name="sid" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gles_texture_pipeline", propOrder = {
    "texcombinerOrTexenvOrExtra"
})
public class GlesTexturePipeline {

    @XmlElements({
        @XmlElement(name = "texcombiner", type = GlesTexcombinerCommandType.class),
        @XmlElement(name = "texenv", type = GlesTexenvCommandType.class),
        @XmlElement(name = "extra", type = Extra.class)
    })
    protected List<Object> texcombinerOrTexenvOrExtra;
    @XmlAttribute(name = "sid")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String sid;

    /**
     * Gets the value of the texcombinerOrTexenvOrExtra property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the texcombinerOrTexenvOrExtra property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTexcombinerOrTexenvOrExtra().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GlesTexcombinerCommandType }
     * {@link GlesTexenvCommandType }
     * {@link Extra }
     * 
     * 
     */
    public List<Object> getTexcombinerOrTexenvOrExtra() {
        if (texcombinerOrTexenvOrExtra == null) {
            texcombinerOrTexenvOrExtra = new ArrayList<Object>();
        }
        return this.texcombinerOrTexenvOrExtra;
    }

    public boolean isSetTexcombinerOrTexenvOrExtra() {
        return ((this.texcombinerOrTexenvOrExtra!= null)&&(!this.texcombinerOrTexenvOrExtra.isEmpty()));
    }

    public void unsetTexcombinerOrTexenvOrExtra() {
        this.texcombinerOrTexenvOrExtra = null;
    }

    /**
     * Ruft den Wert der sid-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSid() {
        return sid;
    }

    /**
     * Legt den Wert der sid-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSid(String value) {
        this.sid = value;
    }

    public boolean isSetSid() {
        return (this.sid!= null);
    }

    public void setTexcombinerOrTexenvOrExtra(List<Object> value) {
        this.texcombinerOrTexenvOrExtra = value;
    }

}
