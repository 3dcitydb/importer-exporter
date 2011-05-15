/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 *
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.kml.database;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.media.j3d.GeometryArray;
import javax.vecmath.Point3d;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import oracle.ord.im.OrdImage;

import org.citygml.textureAtlasAPI.TextureAtlasGenerator;
import org.citygml.textureAtlasAPI.dataStructure.TexImage;
import org.citygml.textureAtlasAPI.dataStructure.TexImageInfo;
import org.citygml4j.model.citygml.appearance.X3DMaterial;
import org.collada._2005._11.colladaschema.Accessor;
import org.collada._2005._11.colladaschema.Asset;
import org.collada._2005._11.colladaschema.BindMaterial;
import org.collada._2005._11.colladaschema.COLLADA;
import org.collada._2005._11.colladaschema.CommonColorOrTextureType;
import org.collada._2005._11.colladaschema.CommonFloatOrParamType;
import org.collada._2005._11.colladaschema.CommonNewparamType;
import org.collada._2005._11.colladaschema.Effect;
import org.collada._2005._11.colladaschema.Extra;
import org.collada._2005._11.colladaschema.FloatArray;
import org.collada._2005._11.colladaschema.FxSampler2DCommon;
import org.collada._2005._11.colladaschema.FxSurfaceCommon;
import org.collada._2005._11.colladaschema.FxSurfaceInitFromCommon;
import org.collada._2005._11.colladaschema.Geometry;
import org.collada._2005._11.colladaschema.Image;
import org.collada._2005._11.colladaschema.InputLocal;
import org.collada._2005._11.colladaschema.InputLocalOffset;
import org.collada._2005._11.colladaschema.InstanceEffect;
import org.collada._2005._11.colladaschema.InstanceGeometry;
import org.collada._2005._11.colladaschema.InstanceMaterial;
import org.collada._2005._11.colladaschema.InstanceWithExtra;
import org.collada._2005._11.colladaschema.LibraryEffects;
import org.collada._2005._11.colladaschema.LibraryGeometries;
import org.collada._2005._11.colladaschema.LibraryImages;
import org.collada._2005._11.colladaschema.LibraryMaterials;
import org.collada._2005._11.colladaschema.LibraryVisualScenes;
import org.collada._2005._11.colladaschema.Material;
import org.collada._2005._11.colladaschema.Mesh;
import org.collada._2005._11.colladaschema.ObjectFactory;
import org.collada._2005._11.colladaschema.Param;
import org.collada._2005._11.colladaschema.ProfileCOMMON;
import org.collada._2005._11.colladaschema.Source;
import org.collada._2005._11.colladaschema.Technique;
import org.collada._2005._11.colladaschema.Triangles;
import org.collada._2005._11.colladaschema.UpAxisType;
import org.collada._2005._11.colladaschema.Vertices;
import org.collada._2005._11.colladaschema.VisualScene;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.j3d.utils.geometry.GeometryInfo;

import de.tub.citydb.api.log.LogLevelType;
import de.tub.citydb.api.log.Logger;

public class Building {
	
	private String id;
	private BigInteger vertexIdCounter = new BigInteger("-1");
	private VertexInfo firstVertexInfo = null;
	private VertexInfo lastVertexInfo = null;

	/** Tolerance after triangulation must be bigger than before triangulation since some points
	 * may deviate 0.00999999 before and 0.01000001 after. Using a single bigger tolerance value
	 * does not help since the effect repeats itself (0.01999999 vs. 0.0200001).
	 * 
	 * Tolerance after triangulation must not be much bigger than tolerance before, otherwise
	 * there is a risk of going up the wrong node tree when searching for a vertex
	 */
	private final static double TOLERANCE_BEFORE_TRIANGULATION = 0.015d; // this is very tolerant!!!
	private final static double TOLERANCE_AFTER_TRIANGULATION = 0.0150005d; // this is very tolerant!!!
	
	private final static String NO_TEXIMAGE = "default";

	private HashMap<Long, GeometryInfo> geometryInfos = new HashMap<Long, GeometryInfo>();
	// coordinates include texCoordinates, which geometryInfo does not
	// texCoordinates in geometryInfo would be float --> precision loss
	private NodeZ coordinateTree;
	
	// key is surfaceId, surfaceId is originally a Long, here we use an Object for compatibility with the textureAtlasAPI
	private HashMap<Object, String> texImageUris = new HashMap<Object, String>();
	// key is imageUri
	private HashMap<String, BufferedImage> texImages = new HashMap<String, BufferedImage>();
	// for images in unusual formats or wrapping textures. Most times it will be null.
	// key is imageUri
	private HashMap<String, OrdImage> texOrdImages = null;
	// key is surfaceId, surfaceId is originally a Long
	private HashMap<Long, X3DMaterial> x3dMaterials = null;
	
	private double originX;
	private double originY;
	private double originZ;

	private double zOffset;

	private double locationX;
	private double locationY;

	private boolean ignoreSurfaceOrientation = true;

	public void setId(String id) {
		this.id = id.replace(':', '_');
	}

	public String getId() {
		return id;
	}
/*
	public void setOriginX(double originX) {
		this.originX = originX;
	}
*/
	public double getOriginX() {
		return originX;
	}
/*
	public void setOriginY(double originY) {
		this.originY = originY;
	}
*/
	public double getOriginY() {
		return originY;
	}
/*
	public void setOriginZ(double originZ) {
		this.originZ = originZ;
	}
*/
	public double getOriginZ() {
		return originZ;
	}

	public void setZOffset(double zOffset) {
		this.zOffset = zOffset;
	}

	public double getZOffset() {
		return zOffset;
	}

	public void setLocationX(double locationX) {
		this.locationX = locationX;
	}

	public double getLocationX() {
		return locationX;
	}

	public void setLocationY(double locationY) {
		this.locationY = locationY;
	}

	public double getLocationY() {
		return locationY;
	}

	public void setIgnoreSurfaceOrientation(boolean ignoreSurfaceOrientation) {
		this.ignoreSurfaceOrientation = ignoreSurfaceOrientation;
	}

	public boolean isIgnoreSurfaceOrientation() {
		return ignoreSurfaceOrientation;
	}


	public COLLADA generateColladaTree() throws DatatypeConfigurationException{

		ObjectFactory colladaFactory = new ObjectFactory();

		// java and XML...
		DatatypeFactory df = DatatypeFactory.newInstance();
		XMLGregorianCalendar xmlGregorianCalendar = df.newXMLGregorianCalendar(new GregorianCalendar());
		xmlGregorianCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);

		COLLADA	collada = colladaFactory.createCOLLADA();
		collada.setVersion("1.4.1");
		// --------------------------- asset ---------------------------

		Asset asset = colladaFactory.createAsset();
		asset.setCreated(xmlGregorianCalendar);
		asset.setModified(xmlGregorianCalendar);
		Asset.Unit unit = colladaFactory.createAssetUnit();
		unit.setName("meters");
		unit.setMeter(1.0);
		asset.setUnit(unit);
		asset.setUpAxis(UpAxisType.Z_UP);
		Asset.Contributor contributor = colladaFactory.createAssetContributor();
		// System.getProperty("line.separator") produces weird effects here
		contributor.setAuthoringTool(this.getClass().getPackage().getImplementationTitle() + ", version " +
									 this.getClass().getPackage().getImplementationVersion() + "; " +
									 this.getClass().getPackage().getImplementationVendor());
		asset.getContributor().add(contributor);
		collada.setAsset(asset);

		LibraryImages libraryImages = colladaFactory.createLibraryImages();
		LibraryMaterials libraryMaterials = colladaFactory.createLibraryMaterials();
		LibraryEffects libraryEffects = colladaFactory.createLibraryEffects();
		LibraryGeometries libraryGeometries = colladaFactory.createLibraryGeometries();
		LibraryVisualScenes libraryVisualScenes = colladaFactory.createLibraryVisualScenes();
		
		// --------------------------- geometry (constant part) ---------------------------
		Geometry geometry = colladaFactory.createGeometry();
		geometry.setId("geometry0");
		
		Source positionSource = colladaFactory.createSource();
		positionSource.setId("geometry0-position");
		
		FloatArray positionArray = colladaFactory.createFloatArray();
		positionArray.setId("geometry0-position-array");
		List<Double> positionValues = positionArray.getValue();
		positionSource.setFloatArray(positionArray);
		
		Accessor positionAccessor = colladaFactory.createAccessor();
		positionAccessor.setSource("#" + positionArray.getId());
		positionAccessor.setStride(new BigInteger("3"));
        Param paramX = colladaFactory.createParam();
        paramX.setType("float");
        paramX.setName("X");
        Param paramY = colladaFactory.createParam();
        paramY.setType("float");
        paramY.setName("Y");
        Param paramZ = colladaFactory.createParam();
        paramZ.setType("float");
        paramZ.setName("Z");
		positionAccessor.getParam().add(paramX);
		positionAccessor.getParam().add(paramY);
		positionAccessor.getParam().add(paramZ);
		Source.TechniqueCommon positionTechnique = colladaFactory.createSourceTechniqueCommon();
		positionTechnique.setAccessor(positionAccessor);
		positionSource.setTechniqueCommon(positionTechnique);
		
		Source texCoordsSource = colladaFactory.createSource();
		texCoordsSource.setId("geometry0-texCoords");

		FloatArray texCoordsArray = colladaFactory.createFloatArray();
		texCoordsArray.setId("geometry0-texCoords-array");
		List<Double> texCoordsValues = texCoordsArray.getValue();
		texCoordsSource.setFloatArray(texCoordsArray);
		
		Accessor texCoordsAccessor = colladaFactory.createAccessor();
		texCoordsAccessor.setSource("#" + texCoordsArray.getId());
		texCoordsAccessor.setStride(new BigInteger("2"));
        Param paramS = colladaFactory.createParam();
        paramS.setType("float");
        paramS.setName("S");
        Param paramT = colladaFactory.createParam();
        paramT.setType("float");
        paramT.setName("T");
        texCoordsAccessor.getParam().add(paramS);
        texCoordsAccessor.getParam().add(paramT);
        Source.TechniqueCommon texCoordsTechnique = colladaFactory.createSourceTechniqueCommon();
		texCoordsTechnique.setAccessor(texCoordsAccessor);
		texCoordsSource.setTechniqueCommon(texCoordsTechnique);

		Vertices vertices = colladaFactory.createVertices();
		vertices.setId("geometry0-vertex");
		InputLocal input = colladaFactory.createInputLocal();
		input.setSemantic("POSITION");
		input.setSource("#" + positionSource.getId());
		vertices.getInput().add(input);
		
		Mesh mesh = colladaFactory.createMesh();
		mesh.getSource().add(positionSource);
		mesh.getSource().add(texCoordsSource);
		mesh.setVertices(vertices);
		geometry.setMesh(mesh);
		libraryGeometries.getGeometry().add(geometry);
		BigInteger texCoordsCounter = BigInteger.ZERO;

		// --------------------------- visual scenes ---------------------------
		VisualScene visualScene = colladaFactory.createVisualScene();
		visualScene.setId("Building_" + id);
		BindMaterial.TechniqueCommon techniqueCommon = colladaFactory.createBindMaterialTechniqueCommon();
		BindMaterial bindMaterial = colladaFactory.createBindMaterial();
		bindMaterial.setTechniqueCommon(techniqueCommon);
		InstanceGeometry instanceGeometry = colladaFactory.createInstanceGeometry();
		instanceGeometry.setUrl("#" + geometry.getId());
		instanceGeometry.setBindMaterial(bindMaterial);
		org.collada._2005._11.colladaschema.Node node = colladaFactory.createNode();
		node.getInstanceGeometry().add(instanceGeometry);
		visualScene.getNode().add(node);
		libraryVisualScenes.getVisualScene().add(visualScene);

		// --------------------------- now the variable part ---------------------------
		Triangles triangles = null;
		HashMap<String, Triangles> trianglesByTexImageName = new HashMap<String, Triangles>();
		
		// geometryInfos contains all surfaces, textured or not
		Set<Long> keySet = geometryInfos.keySet();
		Iterator<Long> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			Long surfaceId = iterator.next();
			String texImageName = texImageUris.get(surfaceId);
			X3DMaterial x3dMaterial = getX3dMaterial(surfaceId);
			boolean surfaceTextured = true;
			if (texImageName == null) {
				surfaceTextured = false;
				texImageName = (x3dMaterial != null) ?
							   buildNameFromX3dMaterial(x3dMaterial):
							   NO_TEXIMAGE; // <- should never happen
			}

			triangles = trianglesByTexImageName.get(texImageName);
			if (triangles == null) { // never worked on this image or material before

				// --------------------------- materials ---------------------------
				Material material = colladaFactory.createMaterial();
				material.setId(replaceExtensionWithSuffix(texImageName, "_mat"));
				InstanceEffect instanceEffect = colladaFactory.createInstanceEffect();
				instanceEffect.setUrl("#" + replaceExtensionWithSuffix(texImageName, "_eff"));
				material.setInstanceEffect(instanceEffect);
				libraryMaterials.getMaterial().add(material);

				// --------------------- effects common part 1 ---------------------
				Effect effect = colladaFactory.createEffect();
				effect.setId(replaceExtensionWithSuffix(texImageName, "_eff"));
				ProfileCOMMON profileCommon = colladaFactory.createProfileCOMMON();

				if (surfaceTextured) {
					// --------------------------- images ---------------------------
					Image image = colladaFactory.createImage();
					image.setId(replaceExtensionWithSuffix(texImageName, "_img"));
					image.setInitFrom(texImageName);
					libraryImages.getImage().add(image);

					// --------------------------- effects ---------------------------
					FxSurfaceInitFromCommon initFrom = colladaFactory.createFxSurfaceInitFromCommon();
					initFrom.setValue(image); // evtl. image.getId();
					FxSurfaceCommon surface = colladaFactory.createFxSurfaceCommon();
					surface.setType("2D"); // ColladaConstants.SURFACE_TYPE_2D
					surface.getInitFrom().add(initFrom);

					CommonNewparamType newParam1 = colladaFactory.createCommonNewparamType();
					newParam1.setSurface(surface);
					newParam1.setSid(replaceExtensionWithSuffix(texImageName, "_surface"));
					profileCommon.getImageOrNewparam().add(newParam1);

					FxSampler2DCommon sampler2D = colladaFactory.createFxSampler2DCommon();
					sampler2D.setSource(newParam1.getSid());
					CommonNewparamType newParam2 = colladaFactory.createCommonNewparamType();
					newParam2.setSampler2D(sampler2D);
					newParam2.setSid(replaceExtensionWithSuffix(texImageName, "_sampler"));
					profileCommon.getImageOrNewparam().add(newParam2);

					ProfileCOMMON.Technique profileCommonTechnique = colladaFactory.createProfileCOMMONTechnique();
					profileCommonTechnique.setSid("COMMON");
					ProfileCOMMON.Technique.Lambert lambert = colladaFactory.createProfileCOMMONTechniqueLambert();
					CommonColorOrTextureType.Texture texture = colladaFactory.createCommonColorOrTextureTypeTexture();
					texture.setTexture(newParam2.getSid());
					texture.setTexcoord("TEXCOORD"); // ColladaConstants.INPUT_SEMANTIC_TEXCOORD
					CommonColorOrTextureType ccott = colladaFactory.createCommonColorOrTextureType();
					ccott.setTexture(texture);
					lambert.setDiffuse(ccott);
					profileCommonTechnique.setLambert(lambert);
					profileCommon.setTechnique(profileCommonTechnique);
				}
				else {
					// --------------------------- effects ---------------------------
					ProfileCOMMON.Technique profileCommonTechnique = colladaFactory.createProfileCOMMONTechnique();
					profileCommonTechnique.setSid("COMMON");
					ProfileCOMMON.Technique.Lambert lambert = colladaFactory.createProfileCOMMONTechniqueLambert();

					CommonFloatOrParamType cfopt = colladaFactory.createCommonFloatOrParamType();
					CommonFloatOrParamType.Float cfoptf = colladaFactory.createCommonFloatOrParamTypeFloat();
					if (x3dMaterial.isSetShininess()) {
						cfoptf.setValue(x3dMaterial.getShininess());
						cfopt.setFloat(cfoptf);
						lambert.setReflectivity(cfopt);
					}

					if (x3dMaterial.isSetTransparency()) {
						cfopt = colladaFactory.createCommonFloatOrParamType();
						cfoptf = colladaFactory.createCommonFloatOrParamTypeFloat();
						cfoptf.setValue(x3dMaterial.getTransparency());
						cfopt.setFloat(cfoptf);
						lambert.setTransparency(cfopt);
					}

					if (x3dMaterial.isSetDiffuseColor()) {
						CommonColorOrTextureType.Color color = colladaFactory.createCommonColorOrTextureTypeColor();
						color.getValue().add(x3dMaterial.getDiffuseColor().getRed());
						color.getValue().add(x3dMaterial.getDiffuseColor().getGreen());
						color.getValue().add(x3dMaterial.getDiffuseColor().getBlue());
						color.getValue().add(1d); // alpha
						CommonColorOrTextureType ccott = colladaFactory.createCommonColorOrTextureType();
						ccott.setColor(color);
						lambert.setDiffuse(ccott);
					}

					if (x3dMaterial.isSetSpecularColor()) {
						CommonColorOrTextureType.Color color = colladaFactory.createCommonColorOrTextureTypeColor();
						color.getValue().add(x3dMaterial.getSpecularColor().getRed());
						color.getValue().add(x3dMaterial.getSpecularColor().getGreen());
						color.getValue().add(x3dMaterial.getSpecularColor().getBlue());
						color.getValue().add(1d); // alpha
						CommonColorOrTextureType ccott = colladaFactory.createCommonColorOrTextureType();
						ccott.setColor(color);
						lambert.setReflective(ccott);
					}

					if (x3dMaterial.isSetEmissiveColor()) {
						CommonColorOrTextureType.Color color = colladaFactory.createCommonColorOrTextureTypeColor();
						color.getValue().add(x3dMaterial.getEmissiveColor().getRed());
						color.getValue().add(x3dMaterial.getEmissiveColor().getGreen());
						color.getValue().add(x3dMaterial.getEmissiveColor().getBlue());
						color.getValue().add(1d); // alpha
						CommonColorOrTextureType ccott = colladaFactory.createCommonColorOrTextureType();
						ccott.setColor(color);
						lambert.setEmission(ccott);
					}

					profileCommonTechnique.setLambert(lambert);
					profileCommon.setTechnique(profileCommonTechnique);
				}

				// --------------------- effects common part 2 ---------------------
				Technique geTechnique = colladaFactory.createTechnique();
				geTechnique.setProfile("GOOGLEEARTH");

				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder = factory.newDocumentBuilder();
					Document document = docBuilder.newDocument();
					factory.setNamespaceAware(true);
					Element doubleSided = document.createElementNS("http://www.collada.org/2005/11/COLLADASchema", "double_sided");
					doubleSided.setTextContent(ignoreSurfaceOrientation ? "1": "0");
					geTechnique.getAny().add(doubleSided);
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}

				Extra extra = colladaFactory.createExtra();
				extra.getTechnique().add(geTechnique);
				profileCommon.getExtra().add(extra);

				effect.getFxProfileAbstract().add(colladaFactory.createProfileCOMMON(profileCommon));

				libraryEffects.getEffect().add(effect);

				// --------------------------- triangles ---------------------------
				triangles = colladaFactory.createTriangles();
				triangles.setMaterial(replaceExtensionWithSuffix(texImageName, "_tri"));
				InputLocalOffset inputV = colladaFactory.createInputLocalOffset();
				inputV.setSemantic("VERTEX"); // ColladaConstants.INPUT_SEMANTIC_VERTEX
				inputV.setSource("#" + vertices.getId());
				inputV.setOffset(BigInteger.ZERO);
				triangles.getInput().add(inputV);
				if (surfaceTextured) {
					InputLocalOffset inputT = colladaFactory.createInputLocalOffset();
					inputT.setSemantic("TEXCOORD"); // ColladaConstants.INPUT_SEMANTIC_TEXCOORD
					inputT.setSource("#" + texCoordsSource.getId());
					inputT.setOffset(BigInteger.ONE);
					triangles.getInput().add(inputT);
				}
				
				trianglesByTexImageName.put(texImageName, triangles);
			}
		
			// --------------------------- geometry (variable part) ---------------------------
			GeometryInfo ginfo = geometryInfos.get(surfaceId);
			ginfo.convertToIndexedTriangles();
			/*
				// the following seems to be buggy, so don't do it for now
				// generate normals, currently not used, but this is the recommended order
				NormalGenerator ng = new NormalGenerator();
				ng.generateNormals(ginfo);
				// stripify: merge triangles together into bigger triangles when possible
				Stripifier st = new Stripifier();
				st.stripify(ginfo);
			 */
			GeometryArray gArray = ginfo.getGeometryArray();
			Point3d coordPoint = new Point3d();
			for(int i = 0; i < gArray.getVertexCount(); i++){
				gArray.getCoordinate(i, coordPoint);

				VertexInfo vertexInfo = getVertexInfoForXYZ(coordPoint.x, coordPoint.y, coordPoint.z);
				if (vertexInfo == null || (surfaceTextured && vertexInfo.getTexCoords(surfaceId) == null)) {
					// no node or wrong node found
					// use best fit only in extreme cases (it is slow)
					if (surfaceTextured) {
						vertexInfo = getVertexInfoBestFitForXYZ(coordPoint.x, coordPoint.y, coordPoint.z, surfaceId);
					}
					else  {
						vertexInfo = getVertexInfoBestFitForXYZ(coordPoint.x, coordPoint.y, coordPoint.z);
					}
				}
				triangles.getP().add(vertexInfo.getVertexId());

				if (surfaceTextured) {
					TexCoords texCoords = vertexInfo.getTexCoords(surfaceId);
					if (texCoords != null) {
						// trying to save some texture points
						int indexOfT = texCoordsValues.indexOf(texCoords.getT()); 
						if (indexOfT > 0 && indexOfT%2 == 1 && // avoid coincidences
								texCoordsValues.get(indexOfT - 1).equals(texCoords.getS())) {
							triangles.getP().add(new BigInteger(String.valueOf((indexOfT - 1)/2)));
						}
						else {
							texCoordsValues.add(new Double(texCoords.getS()));
							texCoordsValues.add(new Double(texCoords.getT()));
							triangles.getP().add(texCoordsCounter);
							texCoordsCounter = texCoordsCounter.add(BigInteger.ONE);
							// no triangleCounter++ since it is BigInteger
						}
					}
					else { // should never happen
						triangles.getP().add(texCoordsCounter); // wrong data is better than triangles out of sync
						Logger.getInstance().log(LogLevelType.DEBUG, 
								"texCoords not found for (" + coordPoint.x + ", " + coordPoint.y + ", "
								+ coordPoint.z + "). TOLERANCE = " + TOLERANCE_AFTER_TRIANGULATION);
					}
				}
			}
		}

		VertexInfo vertexInfoIterator = firstVertexInfo;
		while (vertexInfoIterator != null) {
			positionValues.add(new Double(reducePrecisionForXorY(vertexInfoIterator.getX() - originX)));
			positionValues.add(new Double(reducePrecisionForXorY(vertexInfoIterator.getY() - originY)));
			positionValues.add(new Double(reducePrecisionForZ(vertexInfoIterator.getZ() - originZ)));
			vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
		} 
		positionArray.setCount(new BigInteger(String.valueOf(positionValues.size()))); // gotta love BigInteger!
		texCoordsArray.setCount(new BigInteger(String.valueOf(texCoordsValues.size())));
		positionAccessor.setCount(positionArray.getCount().divide(positionAccessor.getStride()));
		texCoordsAccessor.setCount(texCoordsArray.getCount().divide(texCoordsAccessor.getStride()));

		Set<String> trianglesKeySet = trianglesByTexImageName.keySet();
		Iterator<String> trianglesIterator = trianglesKeySet.iterator();
		while (trianglesIterator.hasNext()) {
			String texImageName = trianglesIterator.next();
			triangles = trianglesByTexImageName.get(texImageName);
			triangles.setCount(new BigInteger(String.valueOf(triangles.getP().size()/(3*triangles.getInput().size()))));
			if (texImageName.startsWith(NO_TEXIMAGE)) { // materials first, textures last
				mesh.getLinesOrLinestripsOrPolygons().add(0, triangles);
			}
			else {
				mesh.getLinesOrLinestripsOrPolygons().add(triangles);
			}
			InstanceMaterial instanceMaterial = colladaFactory.createInstanceMaterial();
			instanceMaterial.setSymbol(triangles.getMaterial());
			instanceMaterial.setTarget("#" + replaceExtensionWithSuffix(texImageName, "_mat"));
			techniqueCommon.getInstanceMaterial().add(instanceMaterial);
		}

		// this method's name is really like this...
		List<Object> libraries = collada.getLibraryAnimationsOrLibraryAnimationClipsOrLibraryCameras();
		
		if (!libraryImages.getImage().isEmpty()) { // there may be buildings with no textures at all
			libraries.add(libraryImages);
		}
		libraries.add(libraryMaterials);
		libraries.add(libraryEffects);
		libraries.add(libraryGeometries);
		libraries.add(libraryVisualScenes);
		
		InstanceWithExtra instanceWithExtra = colladaFactory.createInstanceWithExtra();
		instanceWithExtra.setUrl("#" + visualScene.getId());
		COLLADA.Scene scene = colladaFactory.createCOLLADAScene();
		scene.setInstanceVisualScene(instanceWithExtra);
		collada.setScene(scene);

		return collada;
	}
	
	private String replaceExtensionWithSuffix (String imageName, String suffix) {
		int indexOfExtension = imageName.lastIndexOf('.');
		if (indexOfExtension != -1) {
			imageName = imageName.substring(0, indexOfExtension);
		}
		return imageName + suffix;
	}
	
	protected HashMap<Object, String> getTexImageUris(){
		return texImageUris;
	}
	
	public void addGeometryInfo(long surfaceId, GeometryInfo geometryInfo){
		geometryInfos.put(new Long(surfaceId), geometryInfo);
	}

	public GeometryInfo getGeometryInfo(long surfaceId){
		return geometryInfos.get(new Long(surfaceId));
	}

	public void addX3dMaterial(long surfaceId, X3DMaterial x3dMaterial){
		if (x3dMaterial == null) return;
		if (x3dMaterial.isSetAmbientIntensity()
			|| x3dMaterial.isSetShininess()
			|| x3dMaterial.isSetTransparency()
			|| x3dMaterial.isSetDiffuseColor()
			|| x3dMaterial.isSetSpecularColor()
			|| x3dMaterial.isSetEmissiveColor()) {
			
			if (x3dMaterials == null) {
				x3dMaterials = new HashMap<Long, X3DMaterial>();
			}
			x3dMaterials.put(new Long(surfaceId), x3dMaterial);
		}
	}

	public X3DMaterial getX3dMaterial(long surfaceId) {
		X3DMaterial x3dMaterial = null;
		if (x3dMaterials != null) {
			x3dMaterial = x3dMaterials.get(new Long(surfaceId));
		}
		return x3dMaterial;
	}

	public void addTexImageUri(long surfaceId, String texImageUri){
		if (texImageUri != null) {
			texImageUris.put(new Long(surfaceId), texImageUri);
		}
	}

	public void addTexImage(String texImageUri, BufferedImage texImage){
		if (texImage != null) {
			texImages.put(texImageUri, texImage);
		}
	}

	public void removeTexImage(String texImageUri){
		texImages.remove(texImageUri);
	}

	public HashMap<String, BufferedImage> getTexImages(){
		return texImages;
	}

	protected BufferedImage getTexImage(String texImageUri){
		BufferedImage texImage = null;
		if (texImages != null) {
			texImage = texImages.get(texImageUri);
		}
		return texImage;
	}

	public void addTexOrdImage(String texImageUri, OrdImage texOrdImage){
		if (texOrdImage == null) {
			return;
		}
		if (texOrdImages == null) {
			texOrdImages = new HashMap<String, OrdImage>();
		}
		texOrdImages.put(texImageUri, texOrdImage);
	}

	public HashMap<String, OrdImage> getTexOrdImages(){
		return texOrdImages;
	}

	public OrdImage getTexOrdImage(String texImageUri){
		OrdImage texOrdImage = null;
		if (texOrdImages != null) {
			texOrdImage = texOrdImages.get(texImageUri);
		}
		return texOrdImage;
	}

	public void setVertexInfoForXYZ(long surfaceId, double x, double y, double z, TexCoords texCoordsForThisSurface){
		vertexIdCounter = vertexIdCounter.add(BigInteger.ONE);
		VertexInfo vertexInfo = new VertexInfo(vertexIdCounter, x, y, z);
		vertexInfo.addTexCoords(surfaceId, texCoordsForThisSurface);
		NodeZ nodeToInsert = new NodeZ(z, new NodeY(y, new NodeX(x, vertexInfo)));
		if (coordinateTree == null) {
			coordinateTree =  nodeToInsert;
			firstVertexInfo = vertexInfo;
			lastVertexInfo = vertexInfo;
		}
		else {
			insertNode(coordinateTree, nodeToInsert);
		}
	}

	protected VertexInfo getVertexInfoForXYZ(double x, double y, double z){
		NodeY rootY = (NodeY) getValue(z, coordinateTree);
		NodeX rootX = (NodeX) getValue(y, rootY);
		VertexInfo vertexInfo = (VertexInfo) getValue(x, rootX);
		return vertexInfo;
	}

	private void insertNode(Node currentBasis, Node nodeToInsert) {
		int compareKeysResult = compareKeys(nodeToInsert.key, currentBasis.key, TOLERANCE_BEFORE_TRIANGULATION);
		if (compareKeysResult > 0) {
			if (currentBasis.rightArc == null){
				currentBasis.setRightArc(nodeToInsert);
				linkCurrentVertexInfoToLastVertexInfo(nodeToInsert);
			}
			else {
				insertNode(currentBasis.rightArc, nodeToInsert);
			}
		}
		else if (compareKeysResult < 0) {
			if (currentBasis.leftArc == null){
				currentBasis.setLeftArc(nodeToInsert);
				linkCurrentVertexInfoToLastVertexInfo(nodeToInsert);
			}
			else {
				insertNode(currentBasis.leftArc, nodeToInsert);
			}
		}
		else {
			replaceOrAddValue(currentBasis, nodeToInsert);
		}
	}
	
	private Object getValue(double key, Node currentBasis) {
		if (currentBasis == null) {
			return null;
		}
		int compareKeysResult = compareKeys(key, currentBasis.key, TOLERANCE_AFTER_TRIANGULATION);
		if (compareKeysResult > 0) {
			return getValue(key, currentBasis.rightArc);
		}
		else if (compareKeysResult < 0) {
			return getValue(key, currentBasis.leftArc);
		}
		return currentBasis.value;
	}


    public VertexInfo getVertexInfoBestFitForXYZ(double x, double y, double z, long surfaceId) {
		VertexInfo result = null;
		VertexInfo vertexInfoIterator = firstVertexInfo;
    	double distancePow2 = Double.MAX_VALUE;
    	double currentDistancePow2;
    	while (vertexInfoIterator != null) {
    		if (vertexInfoIterator.getTexCoords(surfaceId) != null) {
    			currentDistancePow2 = Math.pow(x - vertexInfoIterator.getX(), 2) + 
    								  Math.pow(y - vertexInfoIterator.getY(), 2) +
    								  Math.pow(z - vertexInfoIterator.getZ(), 2);
    			if (currentDistancePow2 < distancePow2) {
    				distancePow2 = currentDistancePow2;
    				result = vertexInfoIterator;
    			}
    		}
			vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
		}
		if (result == null) {
			result = getVertexInfoBestFitForXYZ(x, y, z);
		}
		return result;
    }

    public VertexInfo getVertexInfoBestFitForXYZ(double x, double y, double z) {
		VertexInfo result = null;
		VertexInfo vertexInfoIterator = firstVertexInfo;
    	double distancePow2 = Double.MAX_VALUE;
    	double currentDistancePow2;
		while (vertexInfoIterator != null) {
			currentDistancePow2 = Math.pow(x - vertexInfoIterator.getX(), 2) + 
								  Math.pow(y - vertexInfoIterator.getY(), 2) +
								  Math.pow(z - vertexInfoIterator.getZ(), 2);
			if (currentDistancePow2 < distancePow2) {
				distancePow2 = currentDistancePow2;
				result = vertexInfoIterator;
			}
			vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
		}
		return result;
    }

	private void replaceOrAddValue(Node currentBasis, Node nodeToInsert) {
		if (nodeToInsert.value instanceof VertexInfo) {
			VertexInfo vertexInfoToInsert = (VertexInfo)nodeToInsert.value;
			if (currentBasis.value == null) { // no vertexInfo yet for this point
				currentBasis.value = nodeToInsert.value;
				linkCurrentVertexInfoToLastVertexInfo(vertexInfoToInsert);
			}
			else {
				vertexIdCounter = vertexIdCounter.subtract(BigInteger.ONE);
				((VertexInfo)currentBasis.value).addTexCoordsFrom(vertexInfoToInsert);
			}
		}
		else { // Node
			insertNode((Node)currentBasis.value, (Node)nodeToInsert.value);
		}
	}
	
	private void linkCurrentVertexInfoToLastVertexInfo (Node node) {
		while (!(node.value instanceof VertexInfo)) {
			node = (Node)node.value;
		}
		linkCurrentVertexInfoToLastVertexInfo((VertexInfo)node.value);
	}

	private void linkCurrentVertexInfoToLastVertexInfo (VertexInfo currentVertexInfo) {
		lastVertexInfo.setNextVertexInfo(currentVertexInfo);
		lastVertexInfo = currentVertexInfo;
	}

	private int compareKeys (double key1, double key2, double tolerance){
		int result = 0;
		if (Math.abs(key1 - key2) > tolerance) {
			result = key1 > key2 ? 1 : -1;
		}
		return result;
	}
	
	public void appendBuilding (Building buildingToAppend) {
		
		VertexInfo vertexInfoIterator = buildingToAppend.firstVertexInfo;
		while (vertexInfoIterator != null) {
			if (vertexInfoIterator.getAllTexCoords() == null) {
				this.setVertexInfoForXYZ(-1, // dummy
						 				 vertexInfoIterator.getX(),
						 				 vertexInfoIterator.getY(),
						 				 vertexInfoIterator.getZ(),
						 				 null);
			}
			else {
				Set<Long> keySet = vertexInfoIterator.getAllTexCoords().keySet();
				Iterator<Long> iterator = keySet.iterator();
				while (iterator.hasNext()) {
					Long surfaceId = iterator.next();
					this.setVertexInfoForXYZ(surfaceId,
											 vertexInfoIterator.getX(),
											 vertexInfoIterator.getY(),
											 vertexInfoIterator.getZ(),
											 vertexInfoIterator.getTexCoords(surfaceId));
				}
			}
			vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
		} 

		Set<Long> keySet = buildingToAppend.geometryInfos.keySet();
		Iterator<Long> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			Long surfaceId = iterator.next();
			this.addX3dMaterial(surfaceId, buildingToAppend.getX3dMaterial(surfaceId));
			String imageUri = buildingToAppend.texImageUris.get(surfaceId);
			this.addTexImageUri(surfaceId, imageUri);
			this.addTexImage(imageUri, buildingToAppend.getTexImage(imageUri));
			this.addTexOrdImage(imageUri, buildingToAppend.getTexOrdImage(imageUri));
			this.addGeometryInfo(surfaceId, buildingToAppend.geometryInfos.get(surfaceId));
		}
		
		// adapt id accordingly
		int indexOf_to_ = this.id.indexOf("_to_");
		String ownLowerLimit = "";
		String ownUpperLimit = "";
		if (indexOf_to_ != -1) { // already more than one building in here
			ownLowerLimit = this.id.substring(0, indexOf_to_);
			ownUpperLimit = this.id.substring(indexOf_to_ + 4);
		}
		else {
			ownLowerLimit = this.id;
			ownUpperLimit = ownLowerLimit;
		}
		
		int btaIndexOf_to_ = buildingToAppend.id.indexOf("_to_");
		String btaLowerLimit = "";
		String btaUpperLimit = "";
		if (btaIndexOf_to_ != -1) { // already more than one building in there
			btaLowerLimit = buildingToAppend.id.substring(0, btaIndexOf_to_);
			btaUpperLimit = buildingToAppend.id.substring(btaIndexOf_to_ + 4);
		}
		else {
			btaLowerLimit = buildingToAppend.id;
			btaUpperLimit = btaLowerLimit;
		}

		ownLowerLimit = ownLowerLimit.compareTo(btaLowerLimit)<0 ? ownLowerLimit: btaLowerLimit;
		ownUpperLimit = ownUpperLimit.compareTo(btaUpperLimit)>0 ? ownUpperLimit: btaUpperLimit;
		
		this.setId(String.valueOf(ownLowerLimit) + "_to_" + ownUpperLimit);
	}


	public void createTextureAtlas(int packingAlgorithm) throws SQLException, IOException {

		if (texImages.size() == 0 && texOrdImages == null) {
			// building has no textures at all or they are in an unknown image format 
			return;
		}
		
		switch (packingAlgorithm) {
			case -1:
				useInternalTAGenerator();
				break;
			default:
				useExternalTAGenerator(packingAlgorithm);
		}
	}

	private void useExternalTAGenerator(int packingAlgorithm) throws SQLException, IOException {

		TextureAtlasGenerator taGenerator = new TextureAtlasGenerator();
		TexImageInfo tiInfo = new TexImageInfo();
		tiInfo.setTexImageURIs(texImageUris);
		
		HashMap<String, TexImage> tiInfoImages = new HashMap<String, TexImage>();

		Set<String> texImagesSet = texImages.keySet();
		Iterator<String> texImagesIterator = texImagesSet.iterator();
		while (texImagesIterator.hasNext()) {
			String imageName = texImagesIterator.next();
			TexImage image = new TexImage(texImages.get(imageName));
			tiInfoImages.put(imageName, image);
		}

		if (texOrdImages != null) {
			texImagesSet = texOrdImages.keySet();
			texImagesIterator = texImagesSet.iterator();
			while (texImagesIterator.hasNext()) {
				String imageName = texImagesIterator.next();
				TexImage image = new TexImage(texOrdImages.get(imageName));
				tiInfoImages.put(imageName, image);
			}
		}
		
		tiInfo.setTexImages(tiInfoImages);
		
		// texture coordinates
		HashMap<Object, String> tiInfoCoords = new HashMap<Object, String>();

		Set<Object> sgIdSet = texImageUris.keySet();
		Iterator<Object> sgIdIterator = sgIdSet.iterator();
		while (sgIdIterator.hasNext()) {
			Long sgId = (Long) sgIdIterator.next();
			VertexInfo vertexInfoIterator = firstVertexInfo;
			while (vertexInfoIterator != null) {
				if (vertexInfoIterator.getAllTexCoords() != null &&
					vertexInfoIterator.getAllTexCoords().containsKey(sgId)) {
					double s = vertexInfoIterator.getTexCoords(sgId).getS();
					double t = vertexInfoIterator.getTexCoords(sgId).getT();
					String tiInfoCoordsForSgId = tiInfoCoords.get(sgId);
					tiInfoCoordsForSgId = (tiInfoCoordsForSgId == null) ?
											"" :
											tiInfoCoordsForSgId + " ";	
					tiInfoCoords.put(sgId, tiInfoCoordsForSgId + String.valueOf(s) + " " + String.valueOf(t));
				}
				vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
			}
		} 
		
		tiInfo.setTexCoordinates(tiInfoCoords);
		
		tiInfo = taGenerator.convert(tiInfo, packingAlgorithm);
		
		texImageUris = tiInfo.getTexImageURIs();
		tiInfoImages = tiInfo.getTexImages(); 
		tiInfoCoords = tiInfo.getTexCoordinates();
			
		texImages.clear();
		if (texOrdImages != null) {
			texOrdImages.clear();
		}
		
		texImagesSet = tiInfoImages.keySet();
		texImagesIterator = texImagesSet.iterator();
		while (texImagesIterator.hasNext()) {
			String texImageName = texImagesIterator.next();
			TexImage texImage = tiInfoImages.get(texImageName);
			if (texImage.getBufferedImage() != null) {
				texImages.put(texImageName, texImage.getBufferedImage());
			}
			else if (texImage.getOrdImage() != null) {
				if (texOrdImages == null) {
					texOrdImages = new HashMap<String, OrdImage>();
				}
				texOrdImages.put(texImageName, texImage.getOrdImage());
			}
		}
		
		sgIdIterator = sgIdSet.iterator();
		while (sgIdIterator.hasNext()) {
			Long sgId = (Long) sgIdIterator.next();
			StringTokenizer texCoordsTokenized = new StringTokenizer(tiInfoCoords.get(sgId), " ");
			VertexInfo vertexInfoIterator = firstVertexInfo;
			while (texCoordsTokenized.hasMoreElements() &&
				   vertexInfoIterator != null) {
				if (vertexInfoIterator.getAllTexCoords() != null && 
					vertexInfoIterator.getAllTexCoords().containsKey(sgId)) {
					vertexInfoIterator.getTexCoords(sgId).setS(Double.parseDouble(texCoordsTokenized.nextToken()));
					vertexInfoIterator.getTexCoords(sgId).setT(Double.parseDouble(texCoordsTokenized.nextToken()));
				}
				vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
			}
		} 
	}
	
	private void useInternalTAGenerator() throws SQLException, IOException {

		if (texImages.size() == 0) {
			// building has no textures at all or they are in an unknown image format 
			return;
		}
		// imageNamesOrderedByImageHeight
		ArrayList<String> inobih = new ArrayList<String>();

		int totalWidth = 0;
		// order images by height		
		Set<String> texImagesSet = texImages.keySet();
		Iterator<String> texImagesIterator = texImagesSet.iterator();
		while (texImagesIterator.hasNext()) {
			String imageName = texImagesIterator.next();
			BufferedImage imageToAdd = texImages.get(imageName);
			int index = 0;
			while (index < inobih.size() 
				   && texImages.get(inobih.get(index)).getHeight() > imageToAdd.getHeight()) {
				index++;
			}
			inobih.add(index, imageName);
			totalWidth = totalWidth + imageToAdd.getWidth();
		}
		
		// calculate size of texture atlas
		final int TEX_ATLAS_MAX_WIDTH = (int)(totalWidth/Math.sqrt(inobih.size()));
		int accumulatedWidth = 0;
		int maxWidth = 0;
		int maxHeightForRow = 0;
		int accumulatedHeight = 0;
		
		for (String imageName: inobih) {
			BufferedImage imageToAdd = texImages.get(imageName);
			if (accumulatedWidth + imageToAdd.getWidth() > TEX_ATLAS_MAX_WIDTH) { // new row
				maxWidth = Math.max(maxWidth, accumulatedWidth);
				accumulatedHeight = accumulatedHeight + maxHeightForRow;
				accumulatedWidth = 0;
				maxHeightForRow = 0;
			}
			maxHeightForRow = Math.max(maxHeightForRow, imageToAdd.getHeight());
			accumulatedWidth = accumulatedWidth + imageToAdd.getWidth();
		}
		maxWidth = Math.max(maxWidth, accumulatedWidth);
		accumulatedHeight = accumulatedHeight + maxHeightForRow; // add last row

		// check the first image as example, is it jpeg or png?
		int type = (texImages.get(inobih.get(0)).getTransparency() == Transparency.OPAQUE) ?
                	BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		// draw texture atlas
		BufferedImage textureAtlas = new BufferedImage(maxWidth, accumulatedHeight, type);
		Graphics2D g2d = textureAtlas.createGraphics();

		accumulatedWidth = 0;
		maxWidth = 0;
		maxHeightForRow = 0;
		accumulatedHeight = 0;
		HashMap<String, Point> imageOffset = new HashMap<String, Point>();
		
		for (String imageName: inobih) {
			BufferedImage imageToAdd = texImages.get(imageName);
			if (accumulatedWidth + imageToAdd.getWidth() > TEX_ATLAS_MAX_WIDTH) { // new row
				maxWidth = Math.max(maxWidth, accumulatedWidth);
				accumulatedHeight = accumulatedHeight + maxHeightForRow;
				accumulatedWidth = 0;
				maxHeightForRow = 0;
			}
			maxHeightForRow = Math.max(maxHeightForRow, imageToAdd.getHeight());
			Point offsetPoint = new Point (accumulatedWidth,
										   accumulatedHeight + maxHeightForRow - imageToAdd.getHeight());
			g2d.drawImage(imageToAdd, offsetPoint.x, offsetPoint.y, null);
			imageOffset.put(imageName, offsetPoint);
			accumulatedWidth = accumulatedWidth + imageToAdd.getWidth();
		}


		HashSet<Long> wrappedSurfacesSet = new HashSet<Long>();
		// transform texture coordinates
		VertexInfo vertexInfoIterator = firstVertexInfo;
		while (vertexInfoIterator != null) {
			if (vertexInfoIterator.getAllTexCoords() != null) {
				Set<Long> surfaceIdSet = vertexInfoIterator.getAllTexCoords().keySet();
				Iterator<Long> surfaceIdIterator = surfaceIdSet.iterator();
				while (surfaceIdIterator.hasNext()) {
					Long surfaceId = surfaceIdIterator.next();
					String imageName = texImageUris.get(surfaceId);
					BufferedImage texImage = texImages.get(imageName);

					if (texImage == null) { // wrapped textures or unknown format images are in texOrdImages
						wrappedSurfacesSet.add(surfaceId);
						continue;
					}
					
					double s = vertexInfoIterator.getTexCoords(surfaceId).getS();
					double t = vertexInfoIterator.getTexCoords(surfaceId).getT();
 					s = (imageOffset.get(imageName).x + (s * texImage.getWidth())) / textureAtlas.getWidth();
					// graphics2D coordinates start at the top left point
					// texture coordinates start at the bottom left point
					t = ((textureAtlas.getHeight() - imageOffset.get(imageName).y - texImage.getHeight()) + 
							t * texImage.getHeight()) / textureAtlas.getHeight();
					vertexInfoIterator.getTexCoords(surfaceId).setS(s);
					vertexInfoIterator.getTexCoords(surfaceId).setT(t);
				}
			}
			vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
		} 

		// redirect all non-wrapping, known-formatted texture images to texture atlas
		String textureAtlasName = "textureAtlas_" + getId() + 
								  inobih.get(0).substring(inobih.get(0).lastIndexOf('.'));
		
		Set<Object> surfaceIdSet = texImageUris.keySet();
		Iterator<Object> surfaceIdIterator = surfaceIdSet.iterator();
		while (surfaceIdIterator.hasNext()) {
			Object surfaceId = surfaceIdIterator.next();
			if (!wrappedSurfacesSet.contains(surfaceId)) {
				texImageUris.put(surfaceId, textureAtlasName);
			}
		}

		// remove all texture images included in texture atlas
		texImages.clear();
		texImages.put(textureAtlasName, textureAtlas);
		g2d.dispose();
	}
	
	
	public void resizeAllImagesByFactor (double factor) throws SQLException, IOException {
		if (texImages.size() == 0) { // building has no textures at all
			return;
		}

		Set<String> keySet = texImages.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			String imageName = iterator.next();
			BufferedImage imageToResize = texImages.get(imageName);
			if (imageToResize.getWidth()*factor < 1 || imageToResize.getHeight()*factor < 1) {
				continue;
			}
			BufferedImage resizedImage = getScaledInstance(imageToResize,
														   (int)(imageToResize.getWidth()*factor),
														   (int)(imageToResize.getHeight()*factor),
														   RenderingHints.VALUE_INTERPOLATION_BILINEAR,
														   true);
			texImages.put(imageName, resizedImage);
		}

	}


    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    private BufferedImage getScaledInstance(BufferedImage img,
                                           int targetWidth,
                                           int targetHeight,
                                           Object hint,
                                           boolean higherQuality) {
    	
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage)img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } 
        else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }
        
        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        }
        while (w != targetWidth || h != targetHeight);

        return ret;
    }

    private String buildNameFromX3dMaterial(X3DMaterial x3dMaterial) {
    	String name = NO_TEXIMAGE;
    	if (x3dMaterial.isSetAmbientIntensity()) { name = name + "_ai_" + x3dMaterial.getAmbientIntensity();}
    	if (x3dMaterial.isSetShininess()) { name = name + "_sh_" + x3dMaterial.getShininess();}
    	if (x3dMaterial.isSetTransparency()) { name = name + "_tr_" + x3dMaterial.getTransparency();}
    	if (x3dMaterial.isSetDiffuseColor()) { name = name + "_dc_r_" + x3dMaterial.getDiffuseColor().getRed()
    														  + "_g_" + x3dMaterial.getDiffuseColor().getGreen()
    														  + "_b_" + x3dMaterial.getDiffuseColor().getBlue();}
    	if (x3dMaterial.isSetSpecularColor()) { name = name + "_sc_r_" + x3dMaterial.getSpecularColor().getRed()
			  												   + "_g_" + x3dMaterial.getSpecularColor().getGreen()
			  												   + "_b_" + x3dMaterial.getSpecularColor().getBlue();}
    	if (x3dMaterial.isSetEmissiveColor()) { name = name + "_ec_r_" + x3dMaterial.getEmissiveColor().getRed()
			  												   + "_g_" + x3dMaterial.getEmissiveColor().getGreen()
			  												   + "_b_" + x3dMaterial.getEmissiveColor().getBlue();}
    	return name;
    }

    public List<Point3d> setOrigins() {
    	originZ = Double.MAX_VALUE;
		List<Point3d> coords = new ArrayList<Point3d>();
		VertexInfo vertexInfoIterator = firstVertexInfo;
		while (vertexInfoIterator != null) {
			if (vertexInfoIterator.getZ() < originZ) { // origin must be a point with the lowest z-coordinate
				originX = vertexInfoIterator.getX();
				originY = vertexInfoIterator.getY();
				originZ = vertexInfoIterator.getZ();
				coords.clear();
				Point3d point3d = new Point3d(originX, originY, originZ);
				coords.add(point3d);
			}
			if (vertexInfoIterator.getZ() == originZ) {
				Point3d point3d = new Point3d(vertexInfoIterator.getX(), vertexInfoIterator.getY(), vertexInfoIterator.getZ());
				coords.add(point3d);
			}
			vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
		}
		return coords;
    }
    
    public static double reducePrecisionForXorY (double originalValue) {
		double newValue = originalValue; // + 0.00000005d;
//		if (decimalDigits != 0) {
//			double factor = Math.pow(10, decimalDigits);
			double factor = Math.pow(10, 7);
			newValue = Math.rint(newValue*factor);
			newValue = newValue/factor;
//		}
		return newValue;
	}

	public static double reducePrecisionForZ (double originalValue) {
		double newValue = originalValue; // + 0.0005d;
//		if (decimalDigits != 0) {
//			double factor = Math.pow(10, decimalDigits);
			double factor = Math.pow(10, 4);
			newValue = Math.rint(newValue*factor);
			newValue = newValue/factor;
//		}
		return newValue;
	}


	protected class Node{
		double key;
		Object value;
		Node rightArc;
		Node leftArc;

		protected Node(double key, Object value){
			this.key = key;
			this.value = value;
		}
		
		protected void setLeftArc(Node leftArc) {
			this.leftArc = leftArc;
		}

		protected Node getLeftArc() {
			return leftArc;
		}

		protected void setRightArc (Node rightArc) {
			this.rightArc = rightArc;
		}

		protected Node getRightArc() {
			return rightArc;
		}

	}

	protected class NodeX extends Node{
		protected NodeX(double key, Object value){
			super(key, value);
		}
	}
	protected class NodeY extends Node{
		protected NodeY(double key, Object value){
			super(key, value);
		}
	}
	protected class NodeZ extends Node{
		protected NodeZ(double key, Object value){
			super(key, value);
		}
	}
	
}
