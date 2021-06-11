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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_animations"/&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_animation_clips"/&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_cameras"/&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_controllers"/&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_geometries"/&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_effects"/&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_force_fields"/&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_images"/&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_lights"/&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_materials"/&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_nodes"/&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_physics_materials"/&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_physics_models"/&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_physics_scenes"/&gt;
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_visual_scenes"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="scene" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="instance_physics_scene" type="{http://www.collada.org/2005/11/COLLADASchema}InstanceWithExtra" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                   &lt;element name="instance_visual_scene" type="{http://www.collada.org/2005/11/COLLADASchema}InstanceWithExtra" minOccurs="0"/&gt;
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="version" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}VersionType" /&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}base"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "asset",
    "libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras",
    "scene",
    "extra"
})
@XmlRootElement(name = "COLLADA")
public class COLLADA {

    @XmlElement(required = true)
    protected Asset asset;
    @XmlElements({
        @XmlElement(name = "library_animations", type = LibraryAnimations.class),
        @XmlElement(name = "library_animation_clips", type = LibraryAnimationClips.class),
        @XmlElement(name = "library_cameras", type = LibraryCameras.class),
        @XmlElement(name = "library_controllers", type = LibraryControllers.class),
        @XmlElement(name = "library_geometries", type = LibraryGeometries.class),
        @XmlElement(name = "library_effects", type = LibraryEffects.class),
        @XmlElement(name = "library_force_fields", type = LibraryForceFields.class),
        @XmlElement(name = "library_images", type = LibraryImages.class),
        @XmlElement(name = "library_lights", type = LibraryLights.class),
        @XmlElement(name = "library_materials", type = LibraryMaterials.class),
        @XmlElement(name = "library_nodes", type = LibraryNodes.class),
        @XmlElement(name = "library_physics_materials", type = LibraryPhysicsMaterials.class),
        @XmlElement(name = "library_physics_models", type = LibraryPhysicsModels.class),
        @XmlElement(name = "library_physics_scenes", type = LibraryPhysicsScenes.class),
        @XmlElement(name = "library_visual_scenes", type = LibraryVisualScenes.class)
    })
    protected List<Object> libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras;
    protected COLLADA.Scene scene;
    protected List<Extra> extra;
    @XmlAttribute(name = "version", required = true)
    protected String version;
    @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlSchemaType(name = "anyURI")
    protected String base;

    /**
     * 
     * 						The COLLADA element must contain an asset element.
     * 						
     * 
     * @return
     *     possible object is
     *     {@link Asset }
     *     
     */
    public Asset getAsset() {
        return asset;
    }

    /**
     * Legt den Wert der asset-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Asset }
     *     
     */
    public void setAsset(Asset value) {
        this.asset = value;
    }

    public boolean isSetAsset() {
        return (this.asset!= null);
    }

    /**
     * Gets the value of the libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLibraryAnimationsOrLibraryAnimationClipsOrLibraryCameras().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LibraryAnimations }
     * {@link LibraryAnimationClips }
     * {@link LibraryCameras }
     * {@link LibraryControllers }
     * {@link LibraryGeometries }
     * {@link LibraryEffects }
     * {@link LibraryForceFields }
     * {@link LibraryImages }
     * {@link LibraryLights }
     * {@link LibraryMaterials }
     * {@link LibraryNodes }
     * {@link LibraryPhysicsMaterials }
     * {@link LibraryPhysicsModels }
     * {@link LibraryPhysicsScenes }
     * {@link LibraryVisualScenes }
     * 
     * 
     */
    public List<Object> getLibraryAnimationsOrLibraryAnimationClipsOrLibraryCameras() {
        if (libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras == null) {
            libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras = new ArrayList<Object>();
        }
        return this.libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras;
    }

    public boolean isSetLibraryAnimationsOrLibraryAnimationClipsOrLibraryCameras() {
        return ((this.libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras!= null)&&(!this.libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras.isEmpty()));
    }

    public void unsetLibraryAnimationsOrLibraryAnimationClipsOrLibraryCameras() {
        this.libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras = null;
    }

    /**
     * Ruft den Wert der scene-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link COLLADA.Scene }
     *     
     */
    public COLLADA.Scene getScene() {
        return scene;
    }

    /**
     * Legt den Wert der scene-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link COLLADA.Scene }
     *     
     */
    public void setScene(COLLADA.Scene value) {
        this.scene = value;
    }

    public boolean isSetScene() {
        return (this.scene!= null);
    }

    /**
     * 
     * 						The extra element may appear any number of times.
     * 						Gets the value of the extra property.
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

    /**
     * Ruft den Wert der version-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Legt den Wert der version-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    public boolean isSetVersion() {
        return (this.version!= null);
    }

    /**
     * 
     * 					The xml:base attribute allows you to define the base URI for this COLLADA document. See
     * 					http://www.w3.org/TR/xmlbase/ for more information.
     * 					
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBase() {
        return base;
    }

    /**
     * Legt den Wert der base-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBase(String value) {
        this.base = value;
    }

    public boolean isSetBase() {
        return (this.base!= null);
    }

    public void setLibraryAnimationsOrLibraryAnimationClipsOrLibraryCameras(List<Object> value) {
        this.libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras = value;
    }

    public void setExtra(List<Extra> value) {
        this.extra = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="instance_physics_scene" type="{http://www.collada.org/2005/11/COLLADASchema}InstanceWithExtra" maxOccurs="unbounded" minOccurs="0"/&gt;
     *         &lt;element name="instance_visual_scene" type="{http://www.collada.org/2005/11/COLLADASchema}InstanceWithExtra" minOccurs="0"/&gt;
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
    @XmlType(name = "", propOrder = {
        "instancePhysicsScene",
        "instanceVisualScene",
        "extra"
    })
    public static class Scene {

        @XmlElement(name = "instance_physics_scene")
        protected List<InstanceWithExtra> instancePhysicsScene;
        @XmlElement(name = "instance_visual_scene")
        protected InstanceWithExtra instanceVisualScene;
        protected List<Extra> extra;

        /**
         * Gets the value of the instancePhysicsScene property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the instancePhysicsScene property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getInstancePhysicsScene().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link InstanceWithExtra }
         * 
         * 
         */
        public List<InstanceWithExtra> getInstancePhysicsScene() {
            if (instancePhysicsScene == null) {
                instancePhysicsScene = new ArrayList<InstanceWithExtra>();
            }
            return this.instancePhysicsScene;
        }

        public boolean isSetInstancePhysicsScene() {
            return ((this.instancePhysicsScene!= null)&&(!this.instancePhysicsScene.isEmpty()));
        }

        public void unsetInstancePhysicsScene() {
            this.instancePhysicsScene = null;
        }

        /**
         * Ruft den Wert der instanceVisualScene-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link InstanceWithExtra }
         *     
         */
        public InstanceWithExtra getInstanceVisualScene() {
            return instanceVisualScene;
        }

        /**
         * Legt den Wert der instanceVisualScene-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link InstanceWithExtra }
         *     
         */
        public void setInstanceVisualScene(InstanceWithExtra value) {
            this.instanceVisualScene = value;
        }

        public boolean isSetInstanceVisualScene() {
            return (this.instanceVisualScene!= null);
        }

        /**
         * 
         * 									The extra element may appear any number of times.
         * 									Gets the value of the extra property.
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

        public void setInstancePhysicsScene(List<InstanceWithExtra> value) {
            this.instancePhysicsScene = value;
        }

        public void setExtra(List<Extra> value) {
            this.extra = value;
        }

    }

}
