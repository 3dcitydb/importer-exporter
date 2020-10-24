/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.modules.kml.database;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import net.opengis.kml._2.*;
import org.citydb.config.Config;
import org.citydb.config.geometry.ElementType;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.geometry.GeometryType;
import org.citydb.config.project.database.DatabaseConfig;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.global.LogLevel;
import org.citydb.config.project.kmlExporter.*;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.AbstractGeometryConverterAdapter;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.event.EventDispatcher;
import org.citydb.event.global.CounterEvent;
import org.citydb.event.global.CounterType;
import org.citydb.event.global.GeometryCounterEvent;
import org.citydb.log.Logger;
import org.citydb.modules.kml.datatype.TypeAttributeValueEnum;
import org.citydb.modules.kml.util.AffineTransformer;
import org.citydb.modules.kml.util.BalloonTemplateHandler;
import org.citydb.modules.kml.util.ElevationServiceHandler;
import org.citydb.query.Query;
import org.citydb.textureAtlas.TextureAtlasCreator;
import org.citydb.textureAtlas.image.ImageReader;
import org.citydb.textureAtlas.model.TextureImage;
import org.citydb.textureAtlas.model.TextureImagesInfo;
import org.citydb.util.Util;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.geometry.Point;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.Color;
import org.citygml4j.model.citygml.appearance.X3DMaterial;
import org.collada._2005._11.colladaschema.Accessor;
import org.collada._2005._11.colladaschema.Asset;
import org.collada._2005._11.colladaschema.BindMaterial;
import org.collada._2005._11.colladaschema.COLLADA;
import org.collada._2005._11.colladaschema.CommonColorOrTextureType;
import org.collada._2005._11.colladaschema.CommonFloatOrParamType;
import org.collada._2005._11.colladaschema.CommonNewparamType;
import org.collada._2005._11.colladaschema.CommonTransparentType;
import org.collada._2005._11.colladaschema.Effect;
import org.collada._2005._11.colladaschema.Extra;
import org.collada._2005._11.colladaschema.FloatArray;
import org.collada._2005._11.colladaschema.FxOpaqueEnum;
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

import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public abstract class KmlGenericObject {
	private final Logger log = Logger.getInstance();
	protected final int GEOMETRY_AMOUNT_WARNING = 10000;
	private final double TOLERANCE = Math.pow(10, -7);
	private final double PRECISION = Math.pow(10, 7);
	private final String NO_TEXIMAGE = "default";

	private HashMap<Long, SurfaceInfo> surfaceInfos = new HashMap<Long, SurfaceInfo>();
	private NodeZ coordinateTree;

	// key is surfaceId, surfaceId is originally a Long, here we use an Object for compatibility with the textureAtlasAPI
	private HashMap<Object, String> texImageUris = new HashMap<Object, String>();
	// key is imageUri
	private HashMap<String, TextureImage> texImages = new HashMap<String, TextureImage>();
	// for images in unusual formats or wrapping textures. Most times it will be null.
	// key is imageUri
	private HashMap<String, Long> unsupportedTexImageIds = null;
	// key is surfaceId, surfaceId is originally a Long
	private HashMap<Long, X3DMaterial> x3dMaterials = null;

	private long id;
	private String gmlId;
	private BigInteger vertexIdCounter = new BigInteger("-1");
	protected VertexInfo firstVertexInfo = null;
	private VertexInfo lastVertexInfo = null;

	// origin of the relative coordinates for the object
	private List<Point3d> origins = new ArrayList<Point3d>();
	private Point3d origin = new Point3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);

	// placemark location in WGS84
	private Point3d location = new Point3d();
	private double zOffset;
	private boolean ignoreSurfaceOrientation = true;

	protected Connection connection;
	protected Query query;
	protected KmlExporterManager kmlExporterManager;
	protected net.opengis.kml._2.ObjectFactory kmlFactory;
	protected AbstractDatabaseAdapter databaseAdapter;
	protected BlobExportAdapter textureExportAdapter;
	protected AbstractGeometryConverterAdapter geometryConverterAdapter;
	protected ElevationServiceHandler elevationServiceHandler;
	protected BalloonTemplateHandler balloonTemplateHandler;
	protected EventDispatcher eventDispatcher;
	protected Config config;

	protected int currentLod;
	protected DatabaseSrs dbSrs;
	protected X3DMaterial defaultX3dMaterial;
	protected Queries queries;

	private SimpleDateFormat dateFormatter;
	private final ImageReader imageReader;

	protected KmlGenericObject(Connection connection,
			Query query,
			KmlExporterManager kmlExporterManager,
			net.opengis.kml._2.ObjectFactory kmlFactory,
			AbstractDatabaseAdapter databaseAdapter,
			BlobExportAdapter textureExportAdapter,
			ElevationServiceHandler elevationServiceHandler,
			BalloonTemplateHandler balloonTemplateHandler,
			EventDispatcher eventDispatcher,
			Config config) {

		this.connection = connection;
		this.query = query;
		this.kmlExporterManager = kmlExporterManager;
		this.kmlFactory = kmlFactory;
		this.textureExportAdapter = textureExportAdapter;
		this.elevationServiceHandler = elevationServiceHandler;
		this.balloonTemplateHandler = balloonTemplateHandler;
		this.eventDispatcher = eventDispatcher;
		this.config = config;

		this.databaseAdapter = databaseAdapter;
		geometryConverterAdapter = databaseAdapter.getGeometryConverter();
		dbSrs = databaseAdapter.getConnectionMetaData().getReferenceSystem();

		dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

		defaultX3dMaterial = new X3DMaterial();
		defaultX3dMaterial.setAmbientIntensity(0.2d);
		defaultX3dMaterial.setShininess(0.2d);
		defaultX3dMaterial.setTransparency(0d);
		defaultX3dMaterial.setDiffuseColor(getX3dColorFromString("0.8 0.8 0.8"));
		defaultX3dMaterial.setSpecularColor(getX3dColorFromString("1.0 1.0 1.0"));
		defaultX3dMaterial.setEmissiveColor(getX3dColorFromString("0.0 0.0 0.0"));

		queries = new Queries(databaseAdapter, kmlExporterManager.getDatabaseAdapter().getConnectionDetails().getSchema());
		imageReader = new ImageReader();
	}

	public abstract void read(KmlSplittingResult work);
	public abstract String getStyleBasisName();
	public abstract ColladaOptions getColladaOptions();
	public abstract Balloon getBalloonSettings();
	protected abstract List<DisplayForm> getDisplayForms();


	protected BalloonTemplateHandler getBalloonTemplateHandler() {
		return balloonTemplateHandler;
	}

	protected void setBalloonTemplateHandler(BalloonTemplateHandler balloonTemplateHandler) {
		this.balloonTemplateHandler = balloonTemplateHandler;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId.replace(':', '_');
	}

	public String getGmlId() {
		return gmlId;
	}

	protected void updateOrigins(double x, double y, double z) {
		// update origin and list of lowest points
		if (z < origin.z) {
			origins.clear();
			origin.x = x;
			origin.y = y;
			origin.z = z;
			origins.add(origin);
		} else if (z == origin.z)
			origins.add(new Point3d(x, y, z));
	}

	protected Point3d getOrigin() {
		return origin;
	}

	protected List<Point3d> getOrigins() {
		return origins;
	}

	protected void setZOffset(double zOffset) {
		this.zOffset = zOffset;
	}

	protected double getZOffset() {
		return zOffset;
	}

	protected Point3d getLocation() {
		return location;
	}

	protected void setLocation(double x, double y, double z) {
		location.x = x;
		location.y = y;
		location.z = z;
	}

	protected void setIgnoreSurfaceOrientation(boolean ignoreSurfaceOrientation) {
		this.ignoreSurfaceOrientation = ignoreSurfaceOrientation;
	}

	protected boolean isIgnoreSurfaceOrientation() {
		return ignoreSurfaceOrientation;
	}

	protected void addSurfaceInfo(long surfaceId, SurfaceInfo surfaceInfo) {
		surfaceInfos.put(surfaceId, surfaceInfo);
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

		Source normalSource = colladaFactory.createSource();
		normalSource.setId("geometry0-normal");

		FloatArray normalArray = colladaFactory.createFloatArray();
		normalArray.setId("geometry0-normal-array");
		List<Double> normalValues = normalArray.getValue();
		normalSource.setFloatArray(normalArray);

		Accessor normalAccessor = colladaFactory.createAccessor();
		normalAccessor.setSource("#" + normalArray.getId());
		normalAccessor.setStride(new BigInteger("3"));
		normalAccessor.getParam().add(paramX);
		normalAccessor.getParam().add(paramY);
		normalAccessor.getParam().add(paramZ);
		Source.TechniqueCommon normalTechnique = colladaFactory.createSourceTechniqueCommon();
		normalTechnique.setAccessor(normalAccessor);
		normalSource.setTechniqueCommon(normalTechnique);

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
		mesh.setVertices(vertices);
		if (getColladaOptions().isGenerateSurfaceNormals())
			mesh.getSource().add(normalSource);
		if (!config.getProject().getKmlExportConfig().getAppearanceTheme().equals(KmlExportConfig.THEME_NONE))
			mesh.getSource().add(texCoordsSource);

		geometry.setMesh(mesh);
		libraryGeometries.getGeometry().add(geometry);
		BigInteger texCoordsCounter = BigInteger.ZERO;

		// --------------------------- visual scenes ---------------------------
		VisualScene visualScene = colladaFactory.createVisualScene();
		visualScene.setId("Building_" + gmlId);
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
		Set<Long> keySet = surfaceInfos.keySet();
		Iterator<Long> iterator = keySet.iterator();
		int normalIndexOffset = 0;

		while (iterator.hasNext()) {
			Long surfaceId = iterator.next();
			String texImageName = texImageUris.get(surfaceId);
			X3DMaterial x3dMaterial = getX3dMaterial(surfaceId);
			if (x3dMaterial == null)
				x3dMaterial = defaultX3dMaterial;

			boolean surfaceTextured = true;
			if (texImageName == null) {
				surfaceTextured = false;
				texImageName = buildNameFromX3dMaterial(x3dMaterial);
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

					// added for Highlighting in Cesium
					CommonColorOrTextureType.Color color = colladaFactory.createCommonColorOrTextureTypeColor();
					color.getValue().add(0d);
					color.getValue().add(0d);
					color.getValue().add(0d);
					color.getValue().add(1d); // alpha
					CommonColorOrTextureType ccott2 = colladaFactory.createCommonColorOrTextureType();
					ccott2.setColor(color);
					lambert.setEmission(ccott2);

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
						cfoptf.setValue(1.0-x3dMaterial.getTransparency());
						cfopt.setFloat(cfoptf);
						lambert.setTransparency(cfopt);
						CommonTransparentType transparent = colladaFactory.createCommonTransparentType();
						transparent.setOpaque(FxOpaqueEnum.A_ONE);
						CommonColorOrTextureType.Color color = colladaFactory.createCommonColorOrTextureTypeColor();
						color.getValue().add(1.0);
						color.getValue().add(1.0);
						color.getValue().add(1.0);
						color.getValue().add(1.0);
						transparent.setColor(color);
						lambert.setTransparent(transparent);
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
					else {
						// added for Highlighting in Cesium
						CommonColorOrTextureType.Color color = colladaFactory.createCommonColorOrTextureTypeColor();
						color.getValue().add(0d);
						color.getValue().add(0d);
						color.getValue().add(0d);
						color.getValue().add(1d); // alpha
						CommonColorOrTextureType ccott2 = colladaFactory.createCommonColorOrTextureType();
						ccott2.setColor(color);
						lambert.setEmission(ccott2);
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
					log.logStackTrace(e);
				}

				Extra extra = colladaFactory.createExtra();
				extra.getTechnique().add(geTechnique);
				profileCommon.getExtra().add(extra);

				effect.getFxProfileAbstract().add(colladaFactory.createProfileCOMMON(profileCommon));

				libraryEffects.getEffect().add(effect);

				// --------------------------- triangles ---------------------------
				int offset = 0;

				triangles = colladaFactory.createTriangles();
				triangles.setMaterial(replaceExtensionWithSuffix(texImageName, "_tri"));
				InputLocalOffset inputV = colladaFactory.createInputLocalOffset();
				inputV.setSemantic("VERTEX"); // ColladaConstants.INPUT_SEMANTIC_VERTEX
				inputV.setSource("#" + vertices.getId());
				inputV.setOffset(BigInteger.valueOf(offset++));
				triangles.getInput().add(inputV);

				if (getColladaOptions().isGenerateSurfaceNormals()) {
					InputLocalOffset inputN = colladaFactory.createInputLocalOffset();
					inputN.setSemantic("NORMAL"); // ColladaConstants.INPUT_NORMAL_VERTEX
					inputN.setSource("#" + normalSource.getId());
					inputN.setOffset(BigInteger.valueOf(offset++));
					triangles.getInput().add(inputN);
				}

				if (surfaceTextured) {
					InputLocalOffset inputT = colladaFactory.createInputLocalOffset();
					inputT.setSemantic("TEXCOORD"); // ColladaConstants.INPUT_SEMANTIC_TEXCOORD
					inputT.setSource("#" + texCoordsSource.getId());
					inputT.setOffset(BigInteger.valueOf(offset++));
					triangles.getInput().add(inputT);
				}

				trianglesByTexImageName.put(texImageName, triangles);
			}

			// --------------------------- geometry (variable part) ---------------------------
			SurfaceInfo surfaceInfo = surfaceInfos.get(surfaceId);
			List<VertexInfo> vertexInfos = surfaceInfo.getVertexInfos();
			double[] ordinatesArray = new double[vertexInfos.size() * 3];

			int count = 0;
			for (VertexInfo vertexInfo : vertexInfos) {
				ordinatesArray[count++] = vertexInfo.getX() - origin.x;
				ordinatesArray[count++] = vertexInfo.getY() - origin.y;
				ordinatesArray[count++] = vertexInfo.getZ() - origin.z;
			}

			GeometryInfo ginfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
			ginfo.setCoordinates(ordinatesArray);
			ginfo.setContourCounts(surfaceInfo.getRingCountAsArray());
			ginfo.setStripCounts(surfaceInfo.getVertexCount());
			int outerRingCount = ginfo.getStripCounts()[0];

			// triangulate the surface geometry
			ginfo.convertToIndexedTriangles();

			// generate normals
			NormalGenerator ng = new NormalGenerator();
			ng.generateNormals(ginfo);

			int[] indexes = ginfo.getCoordinateIndices();
			int[] normalIndexes = ginfo.getNormalIndices();
			Vector3f[] normals = ginfo.getNormals();

			// fix a reversed orientation of the triangulated surface 
			byte[] edges = {0, 1, 1, 2, 2, 0};
			boolean hasFound = false;
			boolean reverse = false;

			for (int i = 0; !hasFound && i < indexes.length; i += 3) {
				// skip degenerated triangles
				if (indexes[i] == indexes[i + 1] || indexes[i + 1] == indexes[i + 2] || indexes[i] == indexes[i + 2])
					continue;

				// find the first edge on the exterior ring
				for (int j = 0; j < edges.length; j += 2) {
					int first = i + edges[j];
					int second = i + edges[j + 1];

					if (indexes[first] < outerRingCount && indexes[second] < outerRingCount && Math.abs(indexes[first] - indexes[second]) == 1) {
						// ok, we found it. now check the order of the vertex indices
						hasFound = true;
						if (indexes[first] > indexes[second])
							reverse = true;

						break;
					}
				}
			}

			// reverse indexes and normals
			if (reverse) {
				ginfo.reverse();
				for (int i = 0; i < normals.length; i++)
					normals[i].negate();
			}

			// use vertex indices of the triangulation to populate
			// the vertex arrays in the collada file
			for (int i = 0; i < indexes.length; i++) {
				VertexInfo vertexInfo = vertexInfos.get(indexes[i]);
				triangles.getP().add(vertexInfo.getVertexId());

				if (getColladaOptions().isGenerateSurfaceNormals())
					triangles.getP().add(BigInteger.valueOf(normalIndexes[i] + normalIndexOffset));

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
							texCoordsValues.add(texCoords.getS());
							texCoordsValues.add(texCoords.getT());
							triangles.getP().add(texCoordsCounter);
							texCoordsCounter = texCoordsCounter.add(BigInteger.ONE);
						}
					}
					else { // should never happen
						triangles.getP().add(texCoordsCounter); // wrong data is better than triangles out of sync
						log.log(LogLevel.DEBUG,
								"texCoords not found for (" + vertexInfo.getX() + ", " + vertexInfo.getY() + ", "
										+ vertexInfo.getZ() + "). TOLERANCE = " + TOLERANCE);
					}
				}
			}

			for (Vector3f normal : normals) {
				normalValues.add(reducePrecisionForXorY((double) normal.x));
				normalValues.add(reducePrecisionForXorY((double) normal.y));
				normalValues.add(reducePrecisionForXorY((double) normal.z));
				normalIndexOffset++;
			}
		}

		VertexInfo vertexInfoIterator = firstVertexInfo;
		while (vertexInfoIterator != null) {
			positionValues.add(reducePrecisionForXorY((vertexInfoIterator.getX() - origin.x)));
			positionValues.add(reducePrecisionForXorY((vertexInfoIterator.getY() - origin.y)));
			positionValues.add(reducePrecisionForZ((vertexInfoIterator.getZ() - origin.z)));
			vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
		}

		positionArray.setCount(new BigInteger(String.valueOf(positionValues.size()))); // gotta love BigInteger!
		normalArray.setCount(new BigInteger(String.valueOf(normalValues.size()))); // gotta love BigInteger!
		texCoordsArray.setCount(new BigInteger(String.valueOf(texCoordsValues.size())));
		positionAccessor.setCount(positionArray.getCount().divide(positionAccessor.getStride()));
		normalAccessor.setCount(normalArray.getCount().divide(normalAccessor.getStride()));
		texCoordsAccessor.setCount(texCoordsArray.getCount().divide(texCoordsAccessor.getStride()));

		Set<String> trianglesKeySet = trianglesByTexImageName.keySet();
		Iterator<String> trianglesIterator = trianglesKeySet.iterator();
		while (trianglesIterator.hasNext()) {
			String texImageName = trianglesIterator.next();
			triangles = trianglesByTexImageName.get(texImageName);
			triangles.setCount(new BigInteger(String.valueOf(triangles.getP().size()/(3*triangles.getInput().size()))));

			// Triangles with textures must be first printed. Otherwise it will cause incorrect textures in glTF model
			if (!texImageName.startsWith(NO_TEXIMAGE)) {
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

	protected int getGeometryAmount(){
		return surfaceInfos.size();
	}

	protected void addX3dMaterial(long surfaceId, X3DMaterial x3dMaterial){
		if (x3dMaterial == null) return;
		if (x3dMaterial.isSetAmbientIntensity()
				|| x3dMaterial.isSetShininess()
				|| x3dMaterial.isSetTransparency()
				|| x3dMaterial.isSetDiffuseColor()
				|| x3dMaterial.isSetSpecularColor()
				|| x3dMaterial.isSetEmissiveColor()) {

			if (x3dMaterials == null)
				x3dMaterials = new HashMap<Long, X3DMaterial>();

			x3dMaterials.put(surfaceId, x3dMaterial);
		}
	}

	protected X3DMaterial getX3dMaterial(long surfaceId) {
		X3DMaterial x3dMaterial = null;
		if (x3dMaterials != null) {
			x3dMaterial = x3dMaterials.get(surfaceId);
		}
		return x3dMaterial;
	}

	protected void removeX3dMaterial(long surfaceId) {
		if (x3dMaterials != null)
			x3dMaterials.remove(surfaceId);
	}

	protected void addTexImageUri(long surfaceId, String texImageUri){
		if (texImageUri != null) {
			texImageUris.put(new Long(surfaceId), texImageUri);
		}
	}

	protected void addTexImage(String texImageUri, TextureImage texImage){
		if (texImage != null) {
			texImages.put(texImageUri, texImage);
		}
	}

	protected void removeTexImage(String texImageUri){
		texImages.remove(texImageUri);
	}

	public HashMap<String, TextureImage> getTexImages(){
		return texImages;
	}

	protected TextureImage getTexImage(String texImageUri){
		TextureImage texImage = null;
		if (texImages != null) {
			texImage = texImages.get(texImageUri);
		}
		return texImage;
	}

	protected void addUnsupportedTexImageId(String texImageUri, long surfaceDataId){
		if (surfaceDataId < 0) {
			return;
		}
		if (unsupportedTexImageIds == null) {
			unsupportedTexImageIds = new HashMap<String, Long>();
		}
		unsupportedTexImageIds.put(texImageUri, surfaceDataId);
	}

	public HashMap<String, Long> getUnsupportedTexImageIds(){
		return unsupportedTexImageIds;
	}

	protected long getUnsupportedTexImageId(String texImageUri){
		long surfaceDataId = -1;
		if (unsupportedTexImageIds != null) {
			Long tmp = unsupportedTexImageIds.get(texImageUri);
			if (tmp != null)
				surfaceDataId = tmp.longValue();
		}
		return surfaceDataId;
	}

	protected VertexInfo setVertexInfoForXYZ(long surfaceId, double x, double y, double z){
		vertexIdCounter = vertexIdCounter.add(BigInteger.ONE);
		VertexInfo vertexInfo = new VertexInfo(vertexIdCounter, x, y, z);
		NodeZ nodeToInsert = new NodeZ(z, new NodeY(y, new NodeX(x, vertexInfo)));
		if (coordinateTree == null) {
			coordinateTree =  nodeToInsert;
			firstVertexInfo = vertexInfo;
			lastVertexInfo = vertexInfo;
		}
		else {
			Node node = insertNode(coordinateTree, nodeToInsert);
			if (node.value instanceof VertexInfo)
				vertexInfo = (VertexInfo)node.value;
		}

		return vertexInfo;
	}

	private Node insertNode(Node currentBasis, Node nodeToInsert) {
		int compareKeysResult = compareKeys(nodeToInsert.key, currentBasis.key, TOLERANCE);
		if (compareKeysResult > 0) {
			if (currentBasis.rightArc == null){
				currentBasis.setRightArc(nodeToInsert);
				linkCurrentVertexInfoToLastVertexInfo(nodeToInsert);
				return nodeToInsert;
			}
			else {
				return insertNode(currentBasis.rightArc, nodeToInsert);
			}
		}
		else if (compareKeysResult < 0) {
			if (currentBasis.leftArc == null){
				currentBasis.setLeftArc(nodeToInsert);
				linkCurrentVertexInfoToLastVertexInfo(nodeToInsert);
				return nodeToInsert;
			}
			else {
				return insertNode(currentBasis.leftArc, nodeToInsert);
			}
		}
		else {
			return replaceOrAddValue(currentBasis, nodeToInsert);
		}
	}

	private Node replaceOrAddValue(Node currentBasis, Node nodeToInsert) {
		if (nodeToInsert.value instanceof VertexInfo) {
			VertexInfo vertexInfoToInsert = (VertexInfo)nodeToInsert.value;
			if (currentBasis.value == null) { // no vertexInfo yet for this point
				currentBasis.value = nodeToInsert.value;
				linkCurrentVertexInfoToLastVertexInfo(vertexInfoToInsert);
			} else
				vertexIdCounter = vertexIdCounter.subtract(BigInteger.ONE);

			return currentBasis;
		}
		else { // Node
			return insertNode((Node)currentBasis.value, (Node)nodeToInsert.value);
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

	public void appendObject (KmlGenericObject objectToAppend) {

		VertexInfo vertexInfoIterator = objectToAppend.firstVertexInfo;
		while (vertexInfoIterator != null) {
			if (vertexInfoIterator.getAllTexCoords() == null) {
				VertexInfo tmp = this.setVertexInfoForXYZ(-1, // dummy
						vertexInfoIterator.getX(),
						vertexInfoIterator.getY(),
						vertexInfoIterator.getZ());
				vertexInfoIterator.setVertexId(tmp.getVertexId());
			}
			else {
				Set<Long> keySet = vertexInfoIterator.getAllTexCoords().keySet();
				Iterator<Long> iterator = keySet.iterator();

				while (iterator.hasNext()) {
					Long surfaceId = iterator.next();
					VertexInfo tmp = this.setVertexInfoForXYZ(surfaceId,
							vertexInfoIterator.getX(),
							vertexInfoIterator.getY(),
							vertexInfoIterator.getZ());
					vertexInfoIterator.setVertexId(tmp.getVertexId());
					tmp.addTexCoords(surfaceId, vertexInfoIterator.getTexCoords(surfaceId));
				}
			}
			vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
		}

		Set<Long> keySet = objectToAppend.surfaceInfos.keySet();
		Iterator<Long> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			Long surfaceId = iterator.next();
			this.addX3dMaterial(surfaceId, objectToAppend.getX3dMaterial(surfaceId));
			String imageUri = objectToAppend.texImageUris.get(surfaceId);
			this.addTexImageUri(surfaceId, imageUri);
			this.addTexImage(imageUri, objectToAppend.getTexImage(imageUri));
			this.addUnsupportedTexImageId(imageUri, objectToAppend.getUnsupportedTexImageId(imageUri));
			this.surfaceInfos.put(surfaceId, objectToAppend.surfaceInfos.get(surfaceId));
		}

		// adapt id accordingly
		int indexOf_to_ = this.gmlId.indexOf("_to_");
		String ownLowerLimit = "";
		String ownUpperLimit = "";
		if (indexOf_to_ != -1) { // already more than one building in here
			ownLowerLimit = this.gmlId.substring(0, indexOf_to_);
			ownUpperLimit = this.gmlId.substring(indexOf_to_ + 4);
		}
		else {
			ownLowerLimit = this.gmlId;
			ownUpperLimit = ownLowerLimit;
		}

		int btaIndexOf_to_ = objectToAppend.gmlId.indexOf("_to_");
		String btaLowerLimit = "";
		String btaUpperLimit = "";
		if (btaIndexOf_to_ != -1) { // already more than one building in there
			btaLowerLimit = objectToAppend.gmlId.substring(0, btaIndexOf_to_);
			btaUpperLimit = objectToAppend.gmlId.substring(btaIndexOf_to_ + 4);
		}
		else {
			btaLowerLimit = objectToAppend.gmlId;
			btaUpperLimit = btaLowerLimit;
		}

		ownLowerLimit = ownLowerLimit.compareTo(btaLowerLimit)<0 ? ownLowerLimit: btaLowerLimit;
		ownUpperLimit = ownUpperLimit.compareTo(btaUpperLimit)>0 ? ownUpperLimit: btaUpperLimit;

		this.setGmlId(String.valueOf(ownLowerLimit) + "_to_" + ownUpperLimit);
	}

	// This function allows to crop large CityGML texture atlas into small texture images for the corresponding surface geometries
	public void cropImages () {
		HashMap<String, TextureImage> newTexImages = new HashMap<String, TextureImage>();
		Set<Object> sgIdSet = texImageUris.keySet();
		Iterator<Object> sgIdIterator = sgIdSet.iterator();

		// Crop texture image and calculate new texture coordinates for each surface geometry
		while (sgIdIterator.hasNext()) {
			Long sgId = (Long) sgIdIterator.next();

			// step 1: calculate maximal and minimal texture coordinates
			VertexInfo vertexInfoIterator = firstVertexInfo;
			double maxS = 0;
			double minS = Double.MAX_VALUE;
			double maxT = 0;
			double minT = Double.MAX_VALUE;
			while (vertexInfoIterator != null) {
				if (vertexInfoIterator.getAllTexCoords() != null && vertexInfoIterator.getAllTexCoords().containsKey(sgId)) {
					double s = vertexInfoIterator.getTexCoords(sgId).getS();
					double t = vertexInfoIterator.getTexCoords(sgId).getT();
					if (s > maxS) {
						maxS = s;
					}
					if (s < minS) {
						minS = s;
					}
					if (t > maxT) {
						maxT = t;
					}
					if (t < minT) {
						minT = t;
					}
				}
				vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
			}

			// step 2: crop images
			String texImageUri = texImageUris.get(sgId);
			TextureImage texImage = texImages.get(texImageUri);
			int imageWidth = texImage.getWidth();
			int imageHeight = texImage.getHeight();
			int startX = 0;
			int startY = 0;
			int endX = 0;
			int endY = 0;
			int croppedImageWidth = 0;
			int croppedImageHeight = 0;
			try {
				// According to the CityGML Encoding Standard, the lower left corner has been defined as the coordinate origin (0, 0) in the texture space
				// But in JAVA BufferedImage, the coordinate origin (0, 0) is located at the upper left corner of the image
				startX = (int) Math.floor(imageWidth * minS);
				startY = (int) Math.floor(imageHeight * (1 - maxT));
				endX = (int) Math.ceil(maxS * imageWidth);
				endY = (int) Math.ceil((1 - minT) * imageHeight);
				croppedImageWidth = endX - startX;
				croppedImageHeight = endY - startY;
				BufferedImage imageToCrop = texImage.getBufferedImage().getSubimage(startX, startY, croppedImageWidth, croppedImageHeight);
				String newImageUri = sgId + "_" + texImageUri;
				texImageUris.put(sgId, newImageUri);
				newTexImages.put(newImageUri, new TextureImage(imageToCrop));
			}
			catch (Exception e) {
				newTexImages.put(texImageUri, texImage);
				log.debug("City object '" + gmlId + "': " + "A texture coordinate lies outside the range [0, 1] for the texutre image '"  + texImageUri + "'; This image can therefore not be cropped" );
			}

			// step 3: update the vertex coordinates according to the cropped images
			vertexInfoIterator = firstVertexInfo;
			while (vertexInfoIterator != null) {
				if (vertexInfoIterator.getAllTexCoords() != null && vertexInfoIterator.getAllTexCoords().containsKey(sgId)) {
					double s = vertexInfoIterator.getTexCoords(sgId).getS();
					double t = vertexInfoIterator.getTexCoords(sgId).getT();
					double newS = (s * imageWidth - startX) / croppedImageWidth;
					double newT = (t * imageHeight - (imageHeight - endY)) / croppedImageHeight;
					vertexInfoIterator.getTexCoords(sgId).setS(newS);
					vertexInfoIterator.getTexCoords(sgId).setT(newT);
				}
				vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
			}
		}

		// step 4: update texture Images
		texImages = newTexImages;
	}

	public void createTextureAtlas(int packingAlgorithm, double imageScaleFactor, boolean pots) throws SQLException, IOException {

		if (texImages.size() < 2) {
			// building has not enough textures or they are in an unknown image format 
			return;
		}

		useExternalTAGenerator(packingAlgorithm, imageScaleFactor, pots);
	}

	private void useExternalTAGenerator(int packingAlgorithm, double scaleFactor, boolean pots) throws SQLException, IOException {

		TextureAtlasCreator taCreator = new TextureAtlasCreator();
		TextureImagesInfo tiInfo = new TextureImagesInfo();
		tiInfo.setTexImageURIs(texImageUris);
		tiInfo.setTexImages(texImages);

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

		taCreator.setUsePOTS(pots);
		taCreator.setScaleFactor(scaleFactor);

		// create texture atlases
		taCreator.convert(tiInfo, packingAlgorithm);

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

	public void resizeAllImagesByFactor (double factor) throws SQLException, IOException {
		if (texImages.size() == 0) { // building has no textures at all
			return;
		}

		Set<String> keySet = texImages.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			String imageName = iterator.next();
			BufferedImage imageToResize = texImages.get(imageName).getBufferedImage();
			if (imageToResize.getWidth()*factor < 1 || imageToResize.getHeight()*factor < 1) {
				continue;
			}
			BufferedImage resizedImage = getScaledInstance(imageToResize,
					(int)(imageToResize.getWidth()*factor),
					(int)(imageToResize.getHeight()*factor),
					RenderingHints.VALUE_INTERPOLATION_BILINEAR,
					true);
			texImages.put(imageName, new TextureImage(resizedImage));
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

	protected double reducePrecisionForXorY (double originalValue) {
		return Math.rint(originalValue * PRECISION) / PRECISION;
	}

	protected double reducePrecisionForZ (double originalValue) {
		return Math.rint(originalValue * PRECISION) / PRECISION;
	}

	protected List<PlacemarkType> createPlacemarksForFootprint(ResultSet rs, KmlSplittingResult work) throws SQLException {
		List<PlacemarkType> placemarkList = new ArrayList<PlacemarkType>();
		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setName(work.getGmlId());
		placemark.setId(config.getProject().getKmlExportConfig().getIdPrefixes().getPlacemarkFootprint() + placemark.getName());

		if (work.getDisplayForm().isHighlightingEnabled()) {
			placemark.setStyleUrl("#" + getStyleBasisName() + DisplayForm.FOOTPRINT_STR + "Style");
		}
		else {
			placemark.setStyleUrl("#" + getStyleBasisName() + DisplayForm.FOOTPRINT_STR + "Normal");
		}

		if (getBalloonSettings().isIncludeDescription()) {
			addBalloonContents(placemark, work.getId());
		}
		MultiGeometryType multiGeometry = kmlFactory.createMultiGeometryType();
		placemark.setAbstractGeometryGroup(kmlFactory.createMultiGeometry(multiGeometry));

		PolygonType polygon = null;
		while (rs.next()) {
			Object buildingGeometryObj = rs.getObject(1);

			if (!rs.wasNull() && buildingGeometryObj != null) {
				eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));

				GeometryObject unconvertedGeom = geometryConverterAdapter.getGeometry(buildingGeometryObj);
				if (unconvertedGeom == null || (unconvertedGeom.getGeometryType() != GeometryType.POLYGON && unconvertedGeom.getGeometryType() != GeometryType.MULTI_POLYGON))
					continue;

				// for implicit geometries, we need to apply the transformation matrix first
				rs.getLong(2);
				if (!rs.wasNull()) {
					AffineTransformer transformer = getAffineTransformer(rs, 3, 4);
					if (transformer != null)
						unconvertedGeom = transformer.applyTransformation(unconvertedGeom);
				}

				GeometryObject groundSurface = convertToWGS84(unconvertedGeom);

				int dim = groundSurface.getDimension();

				for (int i = 0; i < groundSurface.getNumElements(); i++) {
					LinearRingType linearRing = kmlFactory.createLinearRingType();
					BoundaryType boundary = kmlFactory.createBoundaryType();
					boundary.setLinearRing(linearRing);

					if (groundSurface.getElementType(i) == ElementType.EXTERIOR_LINEAR_RING) {
						polygon = kmlFactory.createPolygonType();
						polygon.setTessellate(true);
						polygon.setExtrude(false);
						polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.CLAMP_TO_GROUND));
						polygon.setOuterBoundaryIs(boundary);
						multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));
					} else if (polygon != null)
						polygon.getInnerBoundaryIs().add(boundary);

					// order points counter-clockwise
					double[] ordinatesArray = groundSurface.getCoordinates(i);
					for (int j = ordinatesArray.length - dim; j >= 0; j = j-dim)
						linearRing.getCoordinates().add(String.valueOf(ordinatesArray[j] + "," + ordinatesArray[j+1] + ",0"));
				}
			}
		}
		if (polygon != null) { // if there is at least some content
			placemarkList.add(placemark);
		}
		return placemarkList;
	}

	protected List<PlacemarkType> createPlacemarksForExtruded(ResultSet rs, KmlSplittingResult work, double measuredHeight, boolean reversePointOrder) throws SQLException {
		List<PlacemarkType> placemarkList = new ArrayList<PlacemarkType>();
		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setName(work.getGmlId());
		placemark.setId(config.getProject().getKmlExportConfig().getIdPrefixes().getPlacemarkExtruded() + placemark.getName());
		if (work.getDisplayForm().isHighlightingEnabled()) {
			placemark.setStyleUrl("#" + getStyleBasisName() + DisplayForm.EXTRUDED_STR + "Style");
		}
		else {
			placemark.setStyleUrl("#" + getStyleBasisName() + DisplayForm.EXTRUDED_STR + "Normal");
		}
		if (getBalloonSettings().isIncludeDescription()) {
			addBalloonContents(placemark, work.getId());
		}
		MultiGeometryType multiGeometry = kmlFactory.createMultiGeometryType();
		placemark.setAbstractGeometryGroup(kmlFactory.createMultiGeometry(multiGeometry));

		PolygonType polygon = null;
		while (rs.next()) {
			Object buildingGeometryObj = rs.getObject(1);

			if (!rs.wasNull() && buildingGeometryObj != null) {
				eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));

				GeometryObject unconvertedGeom = geometryConverterAdapter.getGeometry(buildingGeometryObj);
				if (unconvertedGeom == null || (unconvertedGeom.getGeometryType() != GeometryType.POLYGON && unconvertedGeom.getGeometryType() != GeometryType.MULTI_POLYGON))
					continue;

				// for implicit geometries, we need to apply the transformation matrix first
				rs.getLong(2);
				if (!rs.wasNull()) {
					AffineTransformer transformer = getAffineTransformer(rs, 3, 4);
					if (transformer != null)
						unconvertedGeom = transformer.applyTransformation(unconvertedGeom);
				}

				GeometryObject groundSurface = convertToWGS84(unconvertedGeom);

				int dim = groundSurface.getDimension();

				for (int i = 0; i < groundSurface.getNumElements(); i++) {
					LinearRingType linearRing = kmlFactory.createLinearRingType();
					BoundaryType boundary = kmlFactory.createBoundaryType();
					boundary.setLinearRing(linearRing);

					if (groundSurface.getElementType(i) == ElementType.EXTERIOR_LINEAR_RING) {
						polygon = kmlFactory.createPolygonType();
						polygon.setTessellate(true);
						polygon.setExtrude(true);
						polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
						polygon.setOuterBoundaryIs(boundary);
						multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));
					} else
						polygon.getInnerBoundaryIs().add(boundary);

					double[] ordinatesArray = groundSurface.getCoordinates(i);
					if (reversePointOrder) {
						for (int j = 0; j < ordinatesArray.length; j = j+dim)
							linearRing.getCoordinates().add(String.valueOf(ordinatesArray[j] + "," + ordinatesArray[j+1] + "," + measuredHeight));

					} else if (polygon != null)
						// order points counter-clockwise
						for (int j = ordinatesArray.length - dim; j >= 0; j = j-dim)
							linearRing.getCoordinates().add(String.valueOf(ordinatesArray[j] + "," + ordinatesArray[j+1] + "," + measuredHeight));
				}
			}
		}
		if (polygon != null) { // if there is at least some content
			placemarkList.add(placemark);
		}
		return placemarkList;
	}
	
	protected List<PlacemarkType> createPlacemarksForGeometry(ResultSet _rs, KmlSplittingResult work) throws SQLException {
		HashSet<String> exportedGmlIds = new HashSet<String>();
		HashMap<String, MultiGeometryType> multiGeometries = new HashMap<String, MultiGeometryType>();
		MultiGeometryType multiGeometry = null;
		PolygonType polygon = null;

		_rs.beforeFirst(); // return cursor to beginning
		double zOffset = getZOffsetFromConfigOrDB(work.getId());
		List<Point3d> lowestPointCandidates = getLowestPointsCoordinates(_rs, (zOffset == Double.MAX_VALUE));
		if (zOffset == Double.MAX_VALUE)
			zOffset = getZOffsetFromGEService(work.getId(), lowestPointCandidates);

		_rs.beforeFirst(); // return cursor to beginning

		while (_rs.next()) {
			AffineTransformer transformer = null;
			long rootId = _rs.getLong(1);

			if (_rs.wasNull()) {
				// get nested implicit geometry
				rootId = _rs.getLong(3);
				if (_rs.wasNull())
					continue;

				transformer = getAffineTransformer(_rs, 4, 5);
				if (transformer == null)
					continue;
			}

			// skip closure surfaces
			int surfaceTypeID = _rs.getInt("objectclass_id");
			if (surfaceTypeID != 0
					&& (Util.getCityGMLClass(surfaceTypeID) == CityGMLClass.BUILDING_CLOSURE_SURFACE
					|| Util.getCityGMLClass(surfaceTypeID) == CityGMLClass.BRIDGE_CLOSURE_SURFACE
					|| Util.getCityGMLClass(surfaceTypeID) == CityGMLClass.TUNNEL_CLOSURE_SURFACE))
				continue;

			PreparedStatement geometryQuery = null;
			ResultSet rs = null;

			try {
				String query = queries.getSurfaceGeometries(false, transformer != null);
				geometryQuery = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				for (int i = 1; i <= getParameterCount(query); i++)
					geometryQuery.setLong(i, rootId);

				rs = geometryQuery.executeQuery();

				double lowestZCoordinate = convertPointCoordinatesToWGS84(new double[] {
						lowestPointCandidates.get(0).x,
						lowestPointCandidates.get(0).y,
						lowestPointCandidates.get(0).z}) [2];

				rs.beforeFirst(); // return cursor to beginning

				while (rs.next()) {
					// skip duplicate geometries
					String gmlId = rs.getString("gmlid");
					boolean isXlink = rs.getBoolean("is_xlink");
					if (isXlink && gmlId != null && !exportedGmlIds.add(gmlId))
						continue;

					// skip entries without geometry
					Object buildingGeometryObj = rs.getObject(1);
					if (buildingGeometryObj == null)
						continue;

					GeometryObject unconvertedSurface = geometryConverterAdapter.getPolygon(buildingGeometryObj);
					if (unconvertedSurface == null || unconvertedSurface.getNumElements() == 0)
						continue;

					// for implicit geometries, we need to apply the transformation matrix first
					if (transformer != null)
						unconvertedSurface = transformer.applyTransformation(unconvertedSurface);

					GeometryObject surface = convertToWGS84(unconvertedSurface);
					unconvertedSurface = null;

					eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));

					polygon = kmlFactory.createPolygonType();
					switch (config.getProject().getKmlExportConfig().getAltitudeMode()) {
					case ABSOLUTE:
						polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
						break;
					case RELATIVE:
						polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
						break;
					case CLAMP_TO_GROUND:
						polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.CLAMP_TO_GROUND));
						break;
					}

					// in case that the Building, Bridge or Tunnel don't have thematic Surface, but normal LODxSurface, the "surfaceType" variable will be null.
					// in this case, the thematic surface e.g. WallSurface, RoofSurface can be determined by using a walk-around-way e.g. calculate the Normal-vector
					String surfaceType = null;
					if (surfaceTypeID != 0)
						surfaceType = TypeAttributeValueEnum.fromCityGMLClass(Util.getCityGMLClass(surfaceTypeID)).toString();

					// just in case surfaceType == null
					boolean probablyRoof = true;
					double nx = 0;
					double ny = 0;
					double nz = 0;

					for (int i = 0; i < surface.getNumElements(); i++) {
						LinearRingType linearRing = kmlFactory.createLinearRingType();
						BoundaryType boundary = kmlFactory.createBoundaryType();
						boundary.setLinearRing(linearRing);

						if (i == 0)
							polygon.setOuterBoundaryIs(boundary);
						else
							polygon.getInnerBoundaryIs().add(boundary);

						// order points clockwise
						double[] ordinatesArray = surface.getCoordinates(i);
						for (int j = 0; j < ordinatesArray.length; j = j+3) {
							linearRing.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArray[j]) + ","
									+ reducePrecisionForXorY(ordinatesArray[j+1]) + ","
									+ reducePrecisionForZ(ordinatesArray[j+2] + zOffset)));

							// not touching the ground
							probablyRoof = probablyRoof && (reducePrecisionForZ(ordinatesArray[j+2] - lowestZCoordinate) > 0);

							if (currentLod == 1) {
								// calculate normal
								int current = j;
								int next = j+3;
								if (next >= ordinatesArray.length) next = 0;
								nx = nx + ((ordinatesArray[current+1] - ordinatesArray[next+1]) * (ordinatesArray[current+2] + ordinatesArray[next+2]));
								ny = ny + ((ordinatesArray[current+2] - ordinatesArray[next+2]) * (ordinatesArray[current] + ordinatesArray[next]));
								nz = nz + ((ordinatesArray[current] - ordinatesArray[next]) * (ordinatesArray[current+1] + ordinatesArray[next+1]));
							}
						}
					}

					if (currentLod == 1) { // calculate normal
						double value = Math.sqrt(nx * nx + ny * ny + nz * nz);
						if (value == 0) { // not a surface, but a line
							continue;
						}
						nx = nx / value;
						ny = ny / value;
						nz = nz / value;
					}

					if (surfaceType == null) {
						if (work.getCityGMLClass() == CityGMLClass.BUILDING){
							surfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.BUILDING_WALL_SURFACE).toString();
							switch (currentLod) {
							case 1:
								if (probablyRoof && (nz > 0.999)) {
									surfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.BUILDING_ROOF_SURFACE).toString();
								}
								break;
							case 2:
								if (probablyRoof) {
									surfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.BUILDING_ROOF_SURFACE).toString();
								}
								break;
							}
						}
						else if (work.getCityGMLClass() == CityGMLClass.BRIDGE){
							surfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.BRIDGE_WALL_SURFACE).toString();
							/*							switch (currentLod) {
							case 1:
								if (probablyRoof && (nz > 0.999)) {
									surfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.BRIDGE_ROOF_SURFACE).toString();
								}
								break;
							case 2:
								if (probablyRoof) {
									surfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.BRIDGE_ROOF_SURFACE).toString();
								}
								break;
							}*/
						}
						else if (work.getCityGMLClass() == CityGMLClass.TUNNEL){
							surfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.TUNNEL_WALL_SURFACE).toString();
							/*							switch (currentLod) {
							case 1:
								if (probablyRoof && (nz > 0.999)) {
									surfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.TUNNEL_ROOF_SURFACE).toString();
								}
								break;
							case 2:
								if (probablyRoof) {
									surfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.TUNNEL_ROOF_SURFACE).toString();
								}
								break;
							}	*/
						}
					}

					multiGeometry = multiGeometries.get(surfaceType);
					if (multiGeometry == null) {
						multiGeometry = kmlFactory.createMultiGeometryType();
						multiGeometries.put(surfaceType, multiGeometry);
					}
					multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));
				}
			} catch (SQLException e) {
				log.error("SQL error while querying surface geometries: " + e.getMessage());
			} finally {
				if (rs != null)
					try { rs.close(); } catch (SQLException e) {}
				if (geometryQuery != null)
					try { geometryQuery.close(); } catch (SQLException e) {}
			}
		}

		List<PlacemarkType> placemarkList = new ArrayList<PlacemarkType>();
		Set<String> keySet = multiGeometries.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			String surfaceType = iterator.next();
			PlacemarkType placemark = kmlFactory.createPlacemarkType();
			if (work.getCityGMLClass() == CityGMLClass.BUILDING
					|| work.getCityGMLClass() == CityGMLClass.BRIDGE
					|| work.getCityGMLClass() == CityGMLClass.TUNNEL){
				placemark.setName(work.getGmlId() + "_" + surfaceType);
				placemark.setId(config.getProject().getKmlExportConfig().getIdPrefixes().getPlacemarkGeometry() + placemark.getName());
				placemark.setStyleUrl("#" + surfaceType + "Normal");
			}
			else{
				placemark.setName(work.getGmlId());
				placemark.setId(config.getProject().getKmlExportConfig().getIdPrefixes().getPlacemarkGeometry() + placemark.getName());
				placemark.setStyleUrl("#" + getStyleBasisName() + DisplayForm.GEOMETRY_STR + "Normal");
			}

			if (getBalloonSettings().isIncludeDescription() &&
					!work.getDisplayForm().isHighlightingEnabled()) { // avoid double description
				addBalloonContents(placemark, work.getId());
			}
			multiGeometry = multiGeometries.get(surfaceType);
			placemark.setAbstractGeometryGroup(kmlFactory.createMultiGeometry(multiGeometry));
			placemarkList.add(placemark);
		}

		return placemarkList;
	}

	protected List<PlacemarkType> createPlacemarksForPointOrCurve(ResultSet rs,
	                                                              KmlSplittingResult work,
	                                                              PointAndCurve pacSettings) throws SQLException {
		List<PlacemarkType> placemarkList= new ArrayList<>();

		double zOffset = getZOffsetFromConfigOrDB(work.getId());
		List<Point3d> lowestPointCandidates = getLowestPointsCoordinates(rs, null, (zOffset == Double.MAX_VALUE));
		rs.beforeFirst(); // return cursor to beginning
		if (zOffset == Double.MAX_VALUE) {
			zOffset = getZOffsetFromGEService(work.getId(), lowestPointCandidates);
		}
		while (rs.next()) {
			PlacemarkType placemark = kmlFactory.createPlacemarkType();
			Object buildingGeometryObj = rs.getObject(1);

			GeometryObject pointOrCurveGeometry = geometryConverterAdapter.getGeometry(buildingGeometryObj);
			// for implicit geometries, we need to apply the transformation matrix first
			rs.getLong(2);
			if (!rs.wasNull()) {
				AffineTransformer transformer = getAffineTransformer(rs, 3, 4);
				if (transformer != null)
					pointOrCurveGeometry = transformer.applyTransformation(pointOrCurveGeometry);
			}

			eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));
			MultiGeometryType multiGeometry =  kmlFactory.createMultiGeometryType();

			if (pointOrCurveGeometry.getGeometryType() == GeometryType.MULTI_POINT ||
					pointOrCurveGeometry.getGeometryType() == GeometryType.POINT) { // point

				for (int i = 0; i < pointOrCurveGeometry.getNumElements(); i++) {
					GeometryObject pointGeometry = GeometryObject.createPoint(pointOrCurveGeometry.getCoordinates(i), pointOrCurveGeometry.getDimension(), pointOrCurveGeometry.getSrid());
					JAXBElement<? extends AbstractGeometryType> pointGeometryElement = null;

					LineStringType lineString = kmlFactory.createLineStringType();
					PointType pointString = kmlFactory.createPointType();
					double[] ordinatesArray = convertPointCoordinatesToWGS84(pointGeometry.getCoordinates(0));
					double zOrdinate = ordinatesArray[2] + zOffset;

					if (pacSettings.getPointDisplayMode() == PointDisplayMode.CROSS_LINE){
						double[] ordinatesArrayTopLeft = new double[2];
						ordinatesArrayTopLeft[0] = pointGeometry.getCoordinates(0)[0] - 1;
						ordinatesArrayTopLeft[1] = pointGeometry.getCoordinates(0)[1] + 1;
						ordinatesArrayTopLeft = convertPointCoordinatesToWGS84(ordinatesArrayTopLeft);

						double[] ordinatesArrayBottomRight = new double[2];
						ordinatesArrayBottomRight[0] = pointGeometry.getCoordinates(0)[0] + 1;
						ordinatesArrayBottomRight[1] = pointGeometry.getCoordinates(0)[1] - 1;
						ordinatesArrayBottomRight = convertPointCoordinatesToWGS84(ordinatesArrayBottomRight);

						if (pacSettings.getCurveAltitudeMode() == AltitudeMode.CLAMP_TO_GROUND) {
							// tiny extrude above the ground
							zOrdinate = 0.1;
						}
						// draw an X
						lineString.getCoordinates().add(reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + ","
								+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
								+ reducePrecisionForZ(zOrdinate));

						lineString.getCoordinates().add(reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + ","
								+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
								+ reducePrecisionForZ(zOrdinate));

						lineString.getCoordinates().add(reducePrecisionForXorY(ordinatesArray[0]) + ","
								+ reducePrecisionForXorY(ordinatesArray[1]) + ","
								+ reducePrecisionForZ(zOrdinate));

						lineString.getCoordinates().add(reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + ","
								+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
								+ reducePrecisionForZ(zOrdinate));

						lineString.getCoordinates().add(reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + ","
								+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
								+ reducePrecisionForZ(zOrdinate));

						switch (pacSettings.getPointAltitudeMode()) {
							case ABSOLUTE:
								lineString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
								break;
							case RELATIVE:
							case CLAMP_TO_GROUND: // to make point geometry over curve geometry
								lineString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
								break;
						}
						pointGeometryElement = kmlFactory.createLineString(lineString);
					}
					else if (pacSettings.getPointDisplayMode() == PointDisplayMode.ICON) {
						pointString.getCoordinates().add(reducePrecisionForXorY(ordinatesArray[0]) + ","
								+ reducePrecisionForXorY(ordinatesArray[1]) + ","
								+ reducePrecisionForZ(zOrdinate));

						switch (pacSettings.getPointAltitudeMode()) {
							case ABSOLUTE:
								pointString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
								break;
							case RELATIVE:
								pointString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
								break;
							case CLAMP_TO_GROUND:
								pointString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.CLAMP_TO_GROUND));
								break;
						}
						pointGeometryElement = kmlFactory.createPoint(pointString);
					}
					else if (pacSettings.getPointDisplayMode() == PointDisplayMode.CUBE) {

						double sideLength = pacSettings.getPointCubeLengthOfSide();
						MultiGeometryType solid =  kmlFactory.createMultiGeometryType();
						double[] ordinatesArrayTopLeft = new double[2];
						ordinatesArrayTopLeft[0] = pointGeometry.getCoordinates(0)[0] - sideLength/2;
						ordinatesArrayTopLeft[1] = pointGeometry.getCoordinates(0)[1] + sideLength/2;
						ordinatesArrayTopLeft = convertPointCoordinatesToWGS84(ordinatesArrayTopLeft);

						double[] ordinatesArrayBottomRight = new double[2];
						ordinatesArrayBottomRight[0] = pointGeometry.getCoordinates(0)[0] + sideLength/2;
						ordinatesArrayBottomRight[1] = pointGeometry.getCoordinates(0)[1] - sideLength/2;
						ordinatesArrayBottomRight = convertPointCoordinatesToWGS84(ordinatesArrayBottomRight);

						if (pacSettings.getPointAltitudeMode() == AltitudeMode.CLAMP_TO_GROUND) {
							zOrdinate = 0.0;
						}

						String topLeftFootNode = reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + ","
								+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
								+ reducePrecisionForZ(zOrdinate);
						String bottomLeftFootNode = reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + ","
								+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
								+ reducePrecisionForZ(zOrdinate);
						String bottomRightFootNode = reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + ","
								+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
								+ reducePrecisionForZ(zOrdinate);
						String topRightFootNode = reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + ","
								+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
								+ reducePrecisionForZ(zOrdinate);
						String topLeftRoofNode = reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + ","
								+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
								+ reducePrecisionForZ(zOrdinate + sideLength);
						String bottomLeftRoofNode = reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + ","
								+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
								+ reducePrecisionForZ(zOrdinate + sideLength);
						String bottomRightRoofNode = reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + ","
								+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
								+ reducePrecisionForZ(zOrdinate + sideLength);
						String topRightRoofNode = reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + ","
								+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
								+ reducePrecisionForZ(zOrdinate + sideLength);

						LinearRingType LinearRingElement = kmlFactory.createLinearRingType();

						// bottom side
						LinearRingElement.getCoordinates().add(topLeftFootNode);
						LinearRingElement.getCoordinates().add(bottomLeftFootNode);
						LinearRingElement.getCoordinates().add(bottomRightFootNode);
						LinearRingElement.getCoordinates().add(topRightFootNode);
						LinearRingElement.getCoordinates().add(topLeftFootNode);
						BoundaryType boundary = kmlFactory.createBoundaryType();
						boundary.setLinearRing(LinearRingElement);
						PolygonType polygon = kmlFactory.createPolygonType();
						polygon.setOuterBoundaryIs(boundary);
						switch (pacSettings.getPointAltitudeMode()) {
							case ABSOLUTE:
								polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
								break;
							case RELATIVE:
							case CLAMP_TO_GROUND:
								polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
								break;
						}
						solid.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

						// top side
						LinearRingElement = kmlFactory.createLinearRingType();
						LinearRingElement.getCoordinates().add(topLeftRoofNode);
						LinearRingElement.getCoordinates().add(bottomLeftRoofNode);
						LinearRingElement.getCoordinates().add(bottomRightRoofNode);
						LinearRingElement.getCoordinates().add(topRightRoofNode);
						LinearRingElement.getCoordinates().add(topLeftRoofNode);
						boundary = kmlFactory.createBoundaryType();
						boundary.setLinearRing(LinearRingElement);
						polygon = kmlFactory.createPolygonType();
						polygon.setOuterBoundaryIs(boundary);
						switch (pacSettings.getPointAltitudeMode()) {
							case ABSOLUTE:
								polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
								break;
							case RELATIVE:
							case CLAMP_TO_GROUND:
								polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
								break;
						}
						solid.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

						// left side
						LinearRingElement = kmlFactory.createLinearRingType();
						LinearRingElement.getCoordinates().add(topLeftRoofNode);
						LinearRingElement.getCoordinates().add(topLeftFootNode);
						LinearRingElement.getCoordinates().add(bottomLeftFootNode);
						LinearRingElement.getCoordinates().add(bottomLeftRoofNode);
						LinearRingElement.getCoordinates().add(topLeftRoofNode);
						boundary = kmlFactory.createBoundaryType();
						boundary.setLinearRing(LinearRingElement);
						polygon = kmlFactory.createPolygonType();
						polygon.setOuterBoundaryIs(boundary);
						switch (pacSettings.getPointAltitudeMode()) {
							case ABSOLUTE:
								polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
								break;
							case RELATIVE:
							case CLAMP_TO_GROUND:
								polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
								break;
						}
						solid.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

						// right side
						LinearRingElement = kmlFactory.createLinearRingType();
						LinearRingElement.getCoordinates().add(topRightRoofNode);
						LinearRingElement.getCoordinates().add(bottomRightRoofNode);
						LinearRingElement.getCoordinates().add(bottomRightFootNode);
						LinearRingElement.getCoordinates().add(topRightFootNode);
						LinearRingElement.getCoordinates().add(topRightRoofNode);
						boundary = kmlFactory.createBoundaryType();
						boundary.setLinearRing(LinearRingElement);
						polygon = kmlFactory.createPolygonType();
						polygon.setOuterBoundaryIs(boundary);
						switch (pacSettings.getPointAltitudeMode()) {
							case ABSOLUTE:
								polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
								break;
							case RELATIVE:
							case CLAMP_TO_GROUND:
								polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
								break;
						}
						solid.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

						// front side
						LinearRingElement = kmlFactory.createLinearRingType();
						LinearRingElement.getCoordinates().add(topLeftRoofNode);
						LinearRingElement.getCoordinates().add(topRightRoofNode);
						LinearRingElement.getCoordinates().add(topRightFootNode);
						LinearRingElement.getCoordinates().add(topLeftFootNode);
						LinearRingElement.getCoordinates().add(topLeftRoofNode);
						boundary = kmlFactory.createBoundaryType();
						boundary.setLinearRing(LinearRingElement);
						polygon = kmlFactory.createPolygonType();
						polygon.setOuterBoundaryIs(boundary);
						switch (pacSettings.getPointAltitudeMode()) {
							case ABSOLUTE:
								polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
								break;
							case RELATIVE:
							case CLAMP_TO_GROUND:
								polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
								break;
						}
						solid.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

						// back side
						LinearRingElement = kmlFactory.createLinearRingType();
						LinearRingElement.getCoordinates().add(bottomLeftRoofNode);
						LinearRingElement.getCoordinates().add(bottomLeftFootNode);
						LinearRingElement.getCoordinates().add(bottomRightFootNode);
						LinearRingElement.getCoordinates().add(bottomRightRoofNode);
						LinearRingElement.getCoordinates().add(bottomLeftRoofNode);
						boundary = kmlFactory.createBoundaryType();
						boundary.setLinearRing(LinearRingElement);
						polygon = kmlFactory.createPolygonType();
						polygon.setOuterBoundaryIs(boundary);
						switch (pacSettings.getPointAltitudeMode()) {
							case ABSOLUTE:
								polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
								break;
							case RELATIVE:
							case CLAMP_TO_GROUND:
								polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
								break;
						}
						solid.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

						pointGeometryElement = kmlFactory.createMultiGeometry(solid);
					}

					if (pointGeometryElement != null)
						multiGeometry.getAbstractGeometryGroup().add(pointGeometryElement);
				}

				if (pacSettings.getPointDisplayMode() == PointDisplayMode.CROSS_LINE){
					if (pacSettings.isPointHighlightingEnabled() || pacSettings.isPointCubeHighlightingEnabled())
						placemark.setStyleUrl("#" + getStyleBasisName() + ADEObject.POINT + "Style");
					else
						placemark.setStyleUrl("#" + getStyleBasisName() + ADEObject.POINT + "Normal");
				} else if (pacSettings.getPointDisplayMode() == PointDisplayMode.ICON) {
					placemark.setStyleUrl("#" + getStyleBasisName() + ADEObject.POINT + "Normal");
				} else if (pacSettings.getPointDisplayMode() == PointDisplayMode.CUBE) {
					// setting Style references
					if (pacSettings.isPointCubeHighlightingEnabled())
						placemark.setStyleUrl("#" + getStyleBasisName() + ADEObject.POINT + "Style");
					else
						placemark.setStyleUrl("#" + getStyleBasisName() + ADEObject.POINT + "Normal");
				}
			}
			else if (pointOrCurveGeometry.getGeometryType() == GeometryType.MULTI_LINE_STRING ||
					pointOrCurveGeometry.getGeometryType() == GeometryType.LINE_STRING){ // curve
				pointOrCurveGeometry = convertToWGS84(pointOrCurveGeometry);
				for (int i = 0; i < pointOrCurveGeometry.getNumElements(); i++) {
					LineStringType lineString = kmlFactory.createLineStringType();
					double[] ordinatesArray = pointOrCurveGeometry.getCoordinates(i);
					for (int j = 0; j < ordinatesArray.length; j = j+3){
						lineString.getCoordinates().add(reducePrecisionForXorY(ordinatesArray[j]) + ","
								+ reducePrecisionForXorY(ordinatesArray[j + 1]) + ","
								+ reducePrecisionForZ(ordinatesArray[j + 2] + zOffset));
					}

					switch (pacSettings.getCurveAltitudeMode()) {
						case ABSOLUTE:
							lineString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
							break;
						case RELATIVE:
							lineString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
							break;
						case CLAMP_TO_GROUND:
							lineString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.CLAMP_TO_GROUND));
							break;
					}
					multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createLineString(lineString));
				}

				if (pacSettings.isCurveHighlightingEnabled())
					placemark.setStyleUrl("#" + getStyleBasisName() + ADEObject.CURVE + "Style");
				else
					placemark.setStyleUrl("#" + getStyleBasisName() + ADEObject.CURVE + "Normal");
			}

			if (getBalloonSettings().isIncludeDescription()) {
				addBalloonContents(placemark, work.getId());
			}

			placemark.setAbstractGeometryGroup(kmlFactory.createMultiGeometry(multiGeometry));
			placemark.setId(/* DisplayForm.GEOMETRY_PLACEMARK_ID + */ placemark.getName());
			placemark.setName(work.getGmlId());
			placemarkList.add(placemark);
		}

		return placemarkList;
	}

	protected X3DMaterial getX3dMaterialFromIntColor(int rgbColor) {
		java.awt.Color color = new java.awt.Color(rgbColor, true);
		double floatRed = color.getRed() / 255.0;
		double floatGreen = color.getGreen() / 255.0;
		double floatBlue = color.getBlue() / 255.0;
		double floatAlpha = color.getAlpha() / 255.0;
		X3DMaterial x3dMaterial =  new X3DMaterial();
		x3dMaterial.setAmbientIntensity(0.2d);
		x3dMaterial.setShininess(0.2d);
		x3dMaterial.setTransparency(Double.valueOf(1 - floatAlpha));
		x3dMaterial.setDiffuseColor(getX3dColorFromString(String.valueOf(floatRed) + " " + String.valueOf(floatGreen) + " " + String.valueOf(floatBlue)));
		x3dMaterial.setSpecularColor(getX3dColorFromString("1.0 1.0 1.0"));
		x3dMaterial.setEmissiveColor(getX3dColorFromString("0.0 0.0 0.0"));
		return x3dMaterial;
	}
	
	protected void fillGenericObjectForCollada(ResultSet _rs, boolean generateTextureAtlas) throws SQLException {
		HashSet<String> exportedGmlIds = new HashSet<String>();

		String selectedTheme = config.getProject().getKmlExportConfig().getAppearanceTheme();
		boolean exportAppearance = !selectedTheme.equals(KmlExportConfig.THEME_NONE);
		int texImageCounter = 0;

		DisplayForm colladaDisplayForm = null;
		for (DisplayForm displayForm: getDisplayForms()) {
			if (displayForm.getForm() == DisplayForm.COLLADA) {
				colladaDisplayForm = displayForm;
				break;
			}
		}

		X3DMaterial x3dWallMaterial = getX3dMaterialFromIntColor(colladaDisplayForm.getRgba0());
		X3DMaterial x3dRoofMaterial = null;
		if (colladaDisplayForm.isSetRgba2())
			x3dRoofMaterial = getX3dMaterialFromIntColor(colladaDisplayForm.getRgba2());

		HashMap<Long, Long> implicitIdMap = new HashMap<Long, Long>();

		while (_rs.next()) {
			AffineTransformer transformer = null;
			long rootId = _rs.getLong(1);

			if (_rs.wasNull()) {
				// get nested implicit geometry
				rootId = _rs.getLong(3);
				if (_rs.wasNull())
					continue;

				transformer = getAffineTransformer(_rs, 4, 5);
				if (transformer == null)
					continue;
			}

			boolean isImplicit = transformer != null;

			// skip closure surfaces
			int surfaceTypeID = _rs.getInt("objectclass_id");
			if (surfaceTypeID != 0
					&& (Util.getCityGMLClass(surfaceTypeID) == CityGMLClass.BUILDING_CLOSURE_SURFACE
					|| Util.getCityGMLClass(surfaceTypeID) == CityGMLClass.BRIDGE_CLOSURE_SURFACE
					|| Util.getCityGMLClass(surfaceTypeID) == CityGMLClass.TUNNEL_CLOSURE_SURFACE))
				continue;

			PreparedStatement geometryQuery = null;
			ResultSet rs = null;

			try {
				long surfaceId = 0;
				long previousSurfaceId = -1;
				SurfaceInfo surfaceInfo = null;

				String query = queries.getSurfaceGeometries(exportAppearance, isImplicit);
				geometryQuery = connection.prepareStatement(query);
				for (int i = 1; i <= getParameterCount(query); i++)
					geometryQuery.setLong(i, rootId);

				rs = geometryQuery.executeQuery();

				while (rs.next()) {
					surfaceId = rs.getLong("id");

					if (isImplicit) {
						// we generate unique ids for implicit geometries.
						// this is only required for grouping objects because
						// otherwise surfaces with the same id would be overwritten
						Long tmp = implicitIdMap.get(surfaceId);
						if (tmp == null) {
							tmp = kmlExporterManager.nextImplicitId();
							implicitIdMap.put(surfaceId, tmp);
						}

						surfaceId = tmp;
					}

					if (previousSurfaceId != surfaceId) {
						// skip duplicate geometries
						String gmlId = rs.getString("gmlid");
						boolean isXlink = rs.getBoolean("is_xlink");
						if (isXlink && gmlId != null && !exportedGmlIds.add(gmlId))
							continue;

						// build geometry
						Object surfaceObj = rs.getObject(1);
						surfaceInfo = null;

						// add surface geometry to internal vertex tree structure
						if (surfaceObj != null) {
							GeometryObject surface = geometryConverterAdapter.getPolygon(surfaceObj);
							if (isImplicit)
								surface = transformer.applyTransformation(surface);

							List<VertexInfo> vertexInfos = new ArrayList<VertexInfo>();
							int ringCount = surface.getNumElements();
							int[] vertexCount = new int[ringCount];

							for (int i = 0; i < surface.getNumElements(); i++) {
								double[] ordinatesArray = surface.getCoordinates(i);
								int vertices = 0;

								for (int j = 0; j < ordinatesArray.length - 3; j += 3) {
									// calculate origin and list of lowest points
									updateOrigins(ordinatesArray[j], ordinatesArray[j + 1], ordinatesArray[j + 2]);

									// get or create node in vertex info tree
									VertexInfo vertexInfo = setVertexInfoForXYZ(surfaceId,
											ordinatesArray[j],
											ordinatesArray[j+1],
											ordinatesArray[j+2]);

									vertexInfos.add(vertexInfo);
									vertices++;
								}

								vertexCount[i] = vertices;
							}

							surfaceInfo = new SurfaceInfo(ringCount, vertexCount, vertexInfos);
							addSurfaceInfo(surfaceId, surfaceInfo);
							eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));
						}

						previousSurfaceId = surfaceId;
					}

					// handle appearance
					if (selectedTheme.equals(KmlExportConfig.THEME_NONE)) {
						if (surfaceInfo != null && getX3dMaterial(surfaceId) == null) {
							if (x3dRoofMaterial != null && surfaceTypeID != 0 &&
									(Util.getCityGMLClass(surfaceTypeID) == CityGMLClass.BUILDING_ROOF_SURFACE ||
											Util.getCityGMLClass(surfaceTypeID) == CityGMLClass.BRIDGE_ROOF_SURFACE ||
											Util.getCityGMLClass(surfaceTypeID) == CityGMLClass.TUNNEL_ROOF_SURFACE)) {
								addX3dMaterial(surfaceId, x3dRoofMaterial);
							}
							else {
								addX3dMaterial(surfaceId, x3dWallMaterial);
							}
						}
					} else {
						// skip if we have already assigned a texture
						if (texImageUris.get(surfaceId) != null)
							continue;

						String theme = rs.getString("theme");

						long parentId = rs.getLong(3);
						if (isImplicit) {
							// since we generate unique ids for implicit geometries,
							// we need to retrieve the parentId from the internal map
							Long tmp = implicitIdMap.get(parentId);
							if (tmp != null)
								parentId = tmp;
						}

						if (selectedTheme.equalsIgnoreCase(theme)) {
							long textureImageId = rs.getLong("tex_image_id");
							String texImageUri = rs.getString("tex_image_uri");
							Object texCoordsObject = rs.getObject("texture_coordinates");
							boolean hasTexture = false;

							// textures have priority
							if (texImageUri != null && texImageUri.trim().length() != 0 && texCoordsObject != null && surfaceInfo != null) {
								GeometryObject texCoordsGeometry = geometryConverterAdapter.getPolygon(texCoordsObject);
								texImageUri = texImageUri.replaceAll(" ", "_"); //replace spaces with underscores
								hasTexture = true;

								// handle texture image
								int fileSeparatorIndex = Math.max(texImageUri.lastIndexOf("\\"), texImageUri.lastIndexOf("/"));
								texImageUri = "_" + texImageUri.substring(fileSeparatorIndex + 1); // for example: _tex4712047.jpeg

								if ((getUnsupportedTexImageId(texImageUri) == -1) && (getTexImage(texImageUri) == null)) {
									byte[] imageBytes = textureExportAdapter.getInByteArray(textureImageId);
									if (imageBytes != null) {
										imageReader.setSupportRGB(generateTextureAtlas);

										try {
											TextureImage texImage = imageReader.read(new ByteArrayInputStream(imageBytes));
											if (texImage != null)
												addTexImage(texImageUri, texImage);
											else
												addUnsupportedTexImageId(texImageUri, textureImageId);
										} catch (IOException ioe) {
											// unsupported image format
											addUnsupportedTexImageId(texImageUri, textureImageId);
										}

										if (++texImageCounter == 20) {
											eventDispatcher.triggerEvent(new CounterEvent(CounterType.TEXTURE_IMAGE, texImageCounter, this));
											texImageCounter = 0;
										}
									} else
										hasTexture = false;
								}

								if (hasTexture) {
									// assign texture coordinates to vertices within internal data structure
									Iterator<VertexInfo> iter = surfaceInfo.getVertexInfos().iterator();
									for (int i = 0; i < texCoordsGeometry.getNumElements(); i++) {
										double[] texCoords = texCoordsGeometry.getCoordinates(i);
										for (int j = 0; iter.hasNext() && j < texCoords.length - 2; j += 2)
											iter.next().addTexCoords(surfaceId, new TexCoords(texCoords[j], texCoords[j + 1]));
									}

									addTexImageUri(surfaceId, texImageUri);
									removeX3dMaterial(surfaceId);
								}
							}

							// only use material if there is no texture
							else {
								X3DMaterial x3dMaterial = new X3DMaterial();
								fillX3dMaterialValues(x3dMaterial, rs);
								addX3dMaterial(surfaceId, x3dMaterial);
							}
						}

						// appearance theme does not match selected theme
						else if (surfaceInfo != null && getX3dMaterial(surfaceId) == null) {
							if (getX3dMaterial(parentId) != null) // material for parent surface known
								addX3dMaterial(surfaceId, getX3dMaterial(parentId));
							else if (getX3dMaterial(rootId) != null) // material for root surface known
								addX3dMaterial(surfaceId, getX3dMaterial(rootId));
							else
								addX3dMaterial(surfaceId, x3dWallMaterial);
						}
					}
				}
			} catch (SQLException e) {
				log.error("SQL error while querying surface geometries: " + e.getMessage());
			} finally {
				if (rs != null)
					try { rs.close(); } catch (SQLException e) {}
				if (geometryQuery != null)
					try { geometryQuery.close(); } catch (SQLException e) {}
			}
		}

		eventDispatcher.triggerEvent(new CounterEvent(CounterType.TEXTURE_IMAGE, texImageCounter, this));
	}

	public PlacemarkType createPlacemarkForColladaModel() throws SQLException {
		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setName(getGmlId());
		placemark.setId(config.getProject().getKmlExportConfig().getIdPrefixes().getPlacemarkCollada() + placemark.getName());

		DisplayForm colladaDisplayForm = null;
		for (DisplayForm displayForm: getDisplayForms()) {
			if (displayForm.getForm() == DisplayForm.COLLADA) {
				colladaDisplayForm = displayForm;
				break;
			}
		}

		if (getBalloonSettings().isIncludeDescription()
				&& !colladaDisplayForm.isHighlightingEnabled()) { // avoid double description

			ColladaOptions colladaOptions = getColladaOptions();
			if (!colladaOptions.isGroupObjects() || colladaOptions.getGroupSize() == 1) {
				addBalloonContents(placemark, getId());
			}
		}

		ModelType model = kmlFactory.createModelType();
		LocationType location = kmlFactory.createLocationType();

		switch (config.getProject().getKmlExportConfig().getAltitudeMode()) {
		case ABSOLUTE:
			model.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
			break;
		case RELATIVE:
			model.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
			break;
		case CLAMP_TO_GROUND:
			model.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.CLAMP_TO_GROUND));
			break;
		}

		location.setLatitude(this.location.y);
		location.setLongitude(this.location.x);
		location.setAltitude(this.location.z + reducePrecisionForZ(getZOffset()));
		model.setLocation(location);

		// correct heading value
		double lat1 = Math.toRadians(this.location.y);
		double[] dummy = convertPointCoordinatesToWGS84(new double[] {origin.x, origin.y - 20, origin.z});
		double lat2 = Math.toRadians(dummy[1]);
		double dLon = Math.toRadians(dummy[0] - this.location.x);
		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1)*Math.sin(lat2) - Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
		double bearing = Math.toDegrees(Math.atan2(y, x));
		bearing = (bearing + 180) % 360;

		OrientationType orientation = kmlFactory.createOrientationType();
		orientation.setHeading(reducePrecisionForZ(bearing));
		model.setOrientation(orientation);

		LinkType link = kmlFactory.createLinkType();
		if (config.getProject().getKmlExportConfig().isOneFilePerObject() &&
				!config.getProject().getKmlExportConfig().isExportAsKmz() &&
				query.isSetTiling())
		{
			link.setHref(getGmlId() + ".dae");
		}
		else {
			// File.separator would be wrong here, it MUST be "/"
			link.setHref(getId() + "/" + getGmlId() + ".dae");
		}
		model.setLink(link);

		placemark.setAbstractGeometryGroup(kmlFactory.createModel(model));
		return placemark;
	}
	
	protected List<PlacemarkType> createPlacemarksForHighlighting(ResultSet _rs, KmlSplittingResult work) throws SQLException {
		HashSet<String> exportedGmlIds = new HashSet<String>();

		List<PlacemarkType> placemarkList= new ArrayList<PlacemarkType>();
		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setStyleUrl("#" + getStyleBasisName() + work.getDisplayForm().getName() + "Style");
		placemark.setName(work.getGmlId());
		placemark.setId(config.getProject().getKmlExportConfig().getIdPrefixes().getPlacemarkHighlight() + placemark.getName());
		placemarkList.add(placemark);

		if (getBalloonSettings().isIncludeDescription()) {
			addBalloonContents(placemark, work.getId());
		}

		MultiGeometryType multiGeometry =  kmlFactory.createMultiGeometryType();
		placemark.setAbstractGeometryGroup(kmlFactory.createMultiGeometry(multiGeometry));

		double hlDistance = work.getDisplayForm().getHighlightingDistance();

		_rs.beforeFirst(); // return cursor to beginning
		double zOffset = getZOffsetFromConfigOrDB(work.getId());
		if (zOffset == Double.MAX_VALUE) {
			List<Point3d> lowestPointCandidates = getLowestPointsCoordinates(_rs, (zOffset == Double.MAX_VALUE));
			zOffset = getZOffsetFromGEService(work.getId(), lowestPointCandidates);
		}

		_rs.beforeFirst(); // return cursor to beginning
		while (_rs.next()) {
			AffineTransformer transformer = null;
			long rootId = _rs.getLong(1);

			if (_rs.wasNull()) {
				// get nested implicit geometry
				rootId = _rs.getLong(3);
				if (_rs.wasNull())
					continue;

				transformer = getAffineTransformer(_rs, 4, 5);
				if (transformer == null)
					continue;
			}

			PreparedStatement geometryQuery = null;
			ResultSet rs = null;

			try {
				String query = queries.getSurfaceGeometries(false, transformer != null);
				geometryQuery = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				for (int i = 1; i <= getParameterCount(query); i++)
					geometryQuery.setLong(i, rootId);

				rs = geometryQuery.executeQuery();

				rs.beforeFirst(); // return cursor to beginning

				while (rs.next()) {
					// skip duplicate geometries
					String gmlId = rs.getString("gmlid");
					boolean isXlink = rs.getBoolean("is_xlink");
					if (isXlink && gmlId != null && !exportedGmlIds.add(gmlId))
						continue;

					// skip entries without geometry
					Object unconvertedObj = rs.getObject(1);
					if (unconvertedObj == null)
						continue;

					GeometryObject unconvertedSurface = geometryConverterAdapter.getPolygon(unconvertedObj);
					if (unconvertedSurface == null || unconvertedSurface.getNumElements() == 0)
						continue;

					// for implicit geometries, we need to apply the transformation matrix first
					if (transformer != null)
						unconvertedSurface = transformer.applyTransformation(unconvertedSurface);

					double[] ordinatesArray = unconvertedSurface.getCoordinates(0);
					double nx = 0;
					double ny = 0;
					double nz = 0;

					for (int current = 0; current < ordinatesArray.length - 3; current = current+3) {
						int next = current+3;
						if (next >= ordinatesArray.length - 3) next = 0;
						nx = nx + ((ordinatesArray[current+1] - ordinatesArray[next+1]) * (ordinatesArray[current+2] + ordinatesArray[next+2]));
						ny = ny + ((ordinatesArray[current+2] - ordinatesArray[next+2]) * (ordinatesArray[current] + ordinatesArray[next]));
						nz = nz + ((ordinatesArray[current] - ordinatesArray[next]) * (ordinatesArray[current+1] + ordinatesArray[next+1]));
					}

					double value = Math.sqrt(nx * nx + ny * ny + nz * nz);
					if (value == 0) { // not a surface, but a line
						continue;
					}
					nx = nx / value;
					ny = ny / value;
					nz = nz / value;

					for (int i = 0; i < unconvertedSurface.getNumElements(); i++) {
						ordinatesArray = unconvertedSurface.getCoordinates(i);
						for (int j = 0; j < ordinatesArray.length; j = j + 3) {
							// coordinates = coordinates + hlDistance * (dot product of normal vector and unity vector)
							ordinatesArray[j] = ordinatesArray[j] + hlDistance * nx;
							ordinatesArray[j+1] = ordinatesArray[j+1] + hlDistance * ny;
							ordinatesArray[j+2] = ordinatesArray[j+2] + zOffset + hlDistance * nz;
						}
					}

					// now convert to WGS84
					GeometryObject surface = convertToWGS84(unconvertedSurface);
					unconvertedSurface = null;

					PolygonType polygon = kmlFactory.createPolygonType();
					switch (config.getProject().getKmlExportConfig().getAltitudeMode()) {
					case ABSOLUTE:
						polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
						break;
					case RELATIVE:
						polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
						break;
					case CLAMP_TO_GROUND:
						polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.CLAMP_TO_GROUND));
						break;
					}
					multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

					for (int i = 0; i < surface.getNumElements(); i++) {
						LinearRingType linearRing = kmlFactory.createLinearRingType();
						BoundaryType boundary = kmlFactory.createBoundaryType();
						boundary.setLinearRing(linearRing);

						if (i == 0)
							polygon.setOuterBoundaryIs(boundary);
						else
							polygon.getInnerBoundaryIs().add(boundary);

						// order points clockwise
						ordinatesArray = surface.getCoordinates(i);
						for (int j = 0; j < ordinatesArray.length; j = j+3)
							linearRing.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArray[j]) + ","
									+ reducePrecisionForXorY(ordinatesArray[j+1]) + ","
									+ reducePrecisionForZ(ordinatesArray[j+2])));
					}
				}
			} catch (SQLException e) {
				log.error("SQL error while querying surface geometries: " + e.getMessage());
			} finally {
				if (rs != null)
					try { rs.close(); } catch (SQLException e) {}
				if (geometryQuery != null)
					try { geometryQuery.close(); } catch (SQLException e) {}
			}
		}

		return placemarkList;
	}

	protected AffineTransformer getAffineTransformer(ResultSet rs, int referencePoint, int transformationMatrix) throws SQLException {
		GeometryObject obj = geometryConverterAdapter.getPoint(rs.getObject(referencePoint));
		String transformationString = rs.getString(transformationMatrix);
		if (obj != null && transformationString != null) {
			double[] ordinatesArray = obj.getCoordinates(0);
			Point point = new Point(ordinatesArray[0], ordinatesArray[1], ordinatesArray[2]);
			List<Double> m = Util.string2double(transformationString, "\\s+");
			if (m != null && m.size() >= 16)
				return new AffineTransformer(new Matrix(m.subList(0, 16), 4), point, databaseAdapter.getConnectionMetaData().getReferenceSystem().getSrid());
		}

		return null;
	}

	private String getBalloonContentFromGenericAttribute(long id) {

		String balloonContent = null;
		String genericAttribName = "Balloon_Content";
		PreparedStatement selectQuery = null;
		ResultSet rs = null;

		try {
			// look for the value in the DB
			selectQuery = connection.prepareStatement(queries.getStringAttributeById());
			selectQuery.setLong(1, id);
			selectQuery.setString(2, genericAttribName);
			rs = selectQuery.executeQuery();
			if (rs.next()) {
				balloonContent = rs.getString(1);
			}
		}
		catch (Exception e) {}
		finally {
			try {
				if (rs != null) rs.close();
				if (selectQuery != null) selectQuery.close();
			}
			catch (Exception e2) {}
		}
		return balloonContent;
	}

	protected void addBalloonContents(PlacemarkType placemark, long id) {
		String schemaName = kmlExporterManager.getDatabaseAdapter().getConnectionDetails().getSchema();
		try {
			switch (getBalloonSettings().getBalloonContentMode()) {
			case GEN_ATTRIB:
				String balloonTemplate = getBalloonContentFromGenericAttribute(id);
				if (balloonTemplate != null) {
					if (getBalloonTemplateHandler() == null) { // just in case
						setBalloonTemplateHandler(new BalloonTemplateHandler(balloonTemplate, databaseAdapter));
					}
					placemark.setDescription(getBalloonTemplateHandler().getBalloonContent(balloonTemplate, id, currentLod, connection, schemaName));
				}
				break;
			case GEN_ATTRIB_AND_FILE:
				balloonTemplate = getBalloonContentFromGenericAttribute(id);
				if (balloonTemplate != null) {
					placemark.setDescription(getBalloonTemplateHandler().getBalloonContent(balloonTemplate, id, currentLod, connection, schemaName));
					break;
				}
			case FILE :
				if (getBalloonTemplateHandler() != null) {
					getBalloonTemplateHandler().getBalloonContent(id, currentLod, connection, schemaName);
					placemark.setDescription(getBalloonTemplateHandler().getBalloonContent(id, currentLod, connection, schemaName));
				}
				break;
			}
		}
		catch (Exception e) { } // invalid balloons are silently discarded
	}

	protected void fillX3dMaterialValues (X3DMaterial x3dMaterial, ResultSet rs) throws SQLException {

		double ambientIntensity = rs.getDouble("x3d_ambient_intensity");
		if (!rs.wasNull()) {
			x3dMaterial.setAmbientIntensity(ambientIntensity);
		}
		double shininess = rs.getDouble("x3d_shininess");
		if (!rs.wasNull()) {
			x3dMaterial.setShininess(shininess);
		}
		double transparency = rs.getDouble("x3d_transparency");
		if (!rs.wasNull()) {
			x3dMaterial.setTransparency(transparency);
		}
		Color color = getX3dColorFromString(rs.getString("x3d_diffuse_color"));
		if (color != null) {
			x3dMaterial.setDiffuseColor(color);
		}
		color = getX3dColorFromString(rs.getString("x3d_specular_color"));
		if (color != null) {
			x3dMaterial.setSpecularColor(color);
		}
		color = getX3dColorFromString(rs.getString("x3d_emissive_color"));
		if (color != null) {
			x3dMaterial.setEmissiveColor(color);
		}
		x3dMaterial.setIsSmooth(rs.getInt("x3d_is_smooth") == 1);
	}

	private Color getX3dColorFromString(String colorString) {
		Color color = null;
		if (colorString != null) {
			List<Double> colorList = Util.string2double(colorString, "\\s+");

			if (colorList != null && colorList.size() >= 3) {
				color = new Color(colorList.get(0), colorList.get(1), colorList.get(2));
			}
		}
		return color;
	}

	protected double getZOffsetFromConfigOrDB (long id) {

		double zOffset = Double.MAX_VALUE;

		switch (config.getProject().getKmlExportConfig().getAltitudeOffsetMode()) {
		case NO_OFFSET:
			zOffset = 0;
			break;
		case CONSTANT:
			zOffset = config.getProject().getKmlExportConfig().getAltitudeOffsetValue();
			break;
		case BOTTOM_ZERO:
			zOffset = Double.MAX_VALUE;
			break;
		case GENERIC_ATTRIBUTE:
			PreparedStatement selectQuery = null;
			ResultSet rs = null;
			String genericAttribName = "GE_LoD" + currentLod + "_zOffset";
			try {
				// first look for the value in the DB
				selectQuery = connection.prepareStatement(queries.getStringAttributeById());
				selectQuery.setLong(1, id);
				selectQuery.setString(2, genericAttribName);
				rs = selectQuery.executeQuery();
				if (rs.next()) {
					String strVal = rs.getString(1);
					if (strVal != null) { // use value in DB
						StringTokenizer attributeTokenized = new StringTokenizer(strVal, "|");
						attributeTokenized.nextToken(); // skip mode
						zOffset = Double.parseDouble(attributeTokenized.nextToken());
					}
				}
			}
			catch (Exception e) {}
			finally {
				try {
					if (rs != null) rs.close();
					if (selectQuery != null) selectQuery.close();
				}
				catch (Exception e2) {}
			}
		}

		return zOffset;
	}

	protected double getZOffsetFromGEService (long id, List<Point3d> candidates) {

		double zOffset = 0;

		if (config.getProject().getKmlExportConfig().getAltitudeOffsetMode() == AltitudeOffsetMode.BOTTOM_ZERO) {
			try {
				// convert candidate points to WGS84
				double[] coords = new double[candidates.size()*3];
				int index = 0;
				for (Point3d point3d: candidates) {
					coords[index++] = point3d.x;
					coords[index++] = point3d.y;
					coords[index++] = point3d.z;
				}

				if (candidates.size() == 1) {
					coords = convertPointCoordinatesToWGS84(coords);
				} else {
					GeometryObject geomObj = convertToWGS84(GeometryObject.createCurve(coords, 3, dbSrs.getSrid()));
					coords = geomObj.getCoordinates(0);
				}

				double minElevation = Double.MAX_VALUE;
				for (int i = 0; i < coords.length; i = i + 3) {
					if (minElevation > coords[i+2]) {
						minElevation = coords[i+2];
					}
				}
				zOffset = 0 - minElevation;
			}
			catch (Exception e) {}
		}
		else if (config.getProject().getKmlExportConfig().isCallGElevationService()) { // allowed to query
			PreparedStatement insertQuery = null;
			ResultSet rs = null;

			try {
				// convert candidate points to WGS84
				double[] coords = new double[candidates.size()*3];
				int index = 0;
				for (Point3d point3d: candidates) {
					coords[index++] = point3d.x;
					coords[index++] = point3d.y;
					coords[index++] = point3d.z;
				}

				if (candidates.size() == 1) {
					coords = convertPointCoordinatesToWGS84(coords);
				} else {
					GeometryObject geomObj = convertToWGS84(GeometryObject.createCurve(coords, 3, dbSrs.getSrid()));
					coords = geomObj.getCoordinates(0);
				}

				log.info("Getting zOffset from Google's elevation API for " + getGmlId() + " with " + candidates.size() + " points.");
				zOffset = elevationServiceHandler.getZOffset(coords);

				// save result in DB for next time
				String genericAttribName = "GE_LoD" + currentLod + "_zOffset";
				insertQuery = connection.prepareStatement(queries.insertGEOffset());
				insertQuery.setString(1, genericAttribName);
				String strVal = "Auto|" + zOffset + "|" + dateFormatter.format(new Date(System.currentTimeMillis()));
				insertQuery.setString(2, strVal);
				insertQuery.setLong(3, id);
				rs = insertQuery.executeQuery();
			}
			catch (Exception e) {}
			finally {
				try {
					if (rs != null) rs.close();
					if (insertQuery != null) insertQuery.close();
				}
				catch (Exception e2) {}
			}
		}

		return zOffset;
	}

	protected List<Point3d> getLowestPointsCoordinates(ResultSet _rs, boolean willCallGEService) throws SQLException {
		List<Point3d> result = new ArrayList<Point3d>();
		while (_rs.next()) {
			AffineTransformer transformer = null;
			long rootId = _rs.getLong(1);

			if (_rs.wasNull()) {
				// get nested implicit geometry
				rootId = _rs.getLong(3);
				if (_rs.wasNull())
					continue;

				transformer = getAffineTransformer(_rs, 4, 5);
				if (transformer == null)
					continue;
			}

			// skip closure surfaces
			int surfaceTypeID = _rs.getInt("objectclass_id");
			if (surfaceTypeID != 0
					&& (Util.getCityGMLClass(surfaceTypeID) == CityGMLClass.BUILDING_CLOSURE_SURFACE
					|| Util.getCityGMLClass(surfaceTypeID) == CityGMLClass.BRIDGE_CLOSURE_SURFACE
					|| Util.getCityGMLClass(surfaceTypeID) == CityGMLClass.TUNNEL_CLOSURE_SURFACE))
				continue;

			PreparedStatement geometryQuery = null;
			String query = queries.getSurfaceGeometries(false, transformer != null);

			try {
				geometryQuery = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				for (int i = 1; i <= getParameterCount(query); i++)
					geometryQuery.setLong(i, rootId);
				ResultSet rs = geometryQuery.executeQuery();
				result.addAll(getLowestPointsCoordinates(rs, transformer, willCallGEService));
			} finally {
				if (geometryQuery != null)
					geometryQuery.close();
			}
		}
		return result;
	}

	protected List<Point3d> getLowestPointsCoordinates(ResultSet rs, AffineTransformer transformer, boolean willCallGEService) throws SQLException {
		double currentlyLowestZCoordinate = Double.MAX_VALUE;
		List<Point3d> coords = new ArrayList<Point3d>();

		while (rs.next()) {
			Object buildingGeometryObj = rs.getObject(1);

			if (buildingGeometryObj != null) {
				GeometryObject geometry = geometryConverterAdapter.getGeometry(rs.getObject(1));
				if (transformer != null)
					geometry = transformer.applyTransformation(geometry);

				// we are only interested in the z coordinate
				for (int i = 0; i < geometry.getNumElements(); i++) {
					double[] ordinatesArray = geometry.getCoordinates(i);

					for (int j = 2; j < ordinatesArray.length; j = j+3) {
						if (ordinatesArray[j] < currentlyLowestZCoordinate) {
							coords.clear();
							Point3d point3d = new Point3d(ordinatesArray[j-2], ordinatesArray[j-1], ordinatesArray[j]);
							coords.add(point3d);
							currentlyLowestZCoordinate = point3d.z;
						}
						if (willCallGEService && ordinatesArray[j] == currentlyLowestZCoordinate) {
							Point3d point3d = new Point3d(ordinatesArray[j-2], ordinatesArray[j-1], ordinatesArray[j]);
							if (!coords.contains(point3d)) {
								coords.add(point3d);
							}
						}
					}
				}
			}
		}

		return coords;
	}

	protected double[] convertPointCoordinatesToWGS84(double[] coords) throws SQLException {
		double[] pointCoords = null;
		GeometryObject convertedPointGeom = null;

		// this is a nasty hack for Oracle. In Oracle, transforming a single point to WGS84 does not change
		// its z-value, whereas transforming a series of vertices does affect their z-value
		switch (databaseAdapter.getDatabaseType()) {
		case ORACLE:
			convertedPointGeom = convertToWGS84(GeometryObject.createCurve(coords, coords.length, dbSrs.getSrid()));
			break;
		case POSTGIS:
			convertedPointGeom = convertToWGS84(GeometryObject.createPoint(coords, coords.length, dbSrs.getSrid()));
			break;
		}

		if (convertedPointGeom != null)
			pointCoords = convertedPointGeom.getCoordinates(0);

		return pointCoords;
	}

	protected GeometryObject convertToWGS84(GeometryObject geomObj) throws SQLException {
		GeometryObject convertedGeomObj = null;
		try {
			DatabaseSrs targetSrs = dbSrs.is3D() ? databaseAdapter.getUtil().getWGS843D() : DatabaseConfig.PREDEFINED_SRS.get(DatabaseConfig.PredefinedSrsName.WGS84_2D);
			convertedGeomObj = databaseAdapter.getUtil().transform(geomObj, targetSrs);
		} catch (SQLException e) {
			log.warn("SQL exception when converting geometry to WGS84: " + e.getMessage());
			throw e;
		}

		if (config.getProject().getKmlExportConfig().isUseOriginalZCoords() && geomObj.getDimension() == 3) {
			double[][] originalCoords = geomObj.getCoordinates();
			double[][] convertedCoords = convertedGeomObj.getCoordinates();

			for (int i = 0; i < originalCoords.length; i++) {
				for (int j = 2; j < originalCoords[i].length; j += 3)
					convertedCoords[i][j] = originalCoords[i][j];
			}
		}

		return convertedGeomObj;
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

	protected int getParameterCount(String query) {
		// due to a bug in the Oracle JDBC driver, we cannot use getParameterMetaData().getParameterCount() to
		// get the number of parameters in a prepared statement having a long query string...
		int parameters = 0;
		for (int i = 0; i < query.length(); i++)
			if (query.charAt(i) == '?')
				parameters++;

		return parameters;
	}

}
