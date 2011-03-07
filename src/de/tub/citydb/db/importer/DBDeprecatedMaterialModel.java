package de.tub.citydb.db.importer;

import java.sql.SQLException;
import java.util.List;

import de.tub.citydb.config.Config;
import de.tub.citydb.db.xlink.DBXlinkDeprecatedMaterial;
import de.tub.citygml4j.CityGMLFactory;
import de.tub.citygml4j.implementation.gml._3_1_1.LinearRingImpl;
import de.tub.citygml4j.model.citygml.CityGMLClass;
import de.tub.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import de.tub.citygml4j.model.citygml.appearance.Appearance;
import de.tub.citygml4j.model.citygml.appearance.AppearanceModule;
import de.tub.citygml4j.model.citygml.appearance.Color;
import de.tub.citygml4j.model.citygml.appearance.ColorPlusOpacity;
import de.tub.citygml4j.model.citygml.appearance.ParameterizedTexture;
import de.tub.citygml4j.model.citygml.appearance.SurfaceDataProperty;
import de.tub.citygml4j.model.citygml.appearance.TexCoordList;
import de.tub.citygml4j.model.citygml.appearance.TextureAssociation;
import de.tub.citygml4j.model.citygml.appearance.TextureCoordinates;
import de.tub.citygml4j.model.citygml.appearance.X3DMaterial;
import de.tub.citygml4j.model.citygml.texturedsurface._Appearance;
import de.tub.citygml4j.model.citygml.texturedsurface._Material;
import de.tub.citygml4j.model.citygml.texturedsurface._SimpleTexture;
import de.tub.citygml4j.model.gml.AbstractRingProperty;
import de.tub.citygml4j.model.gml.AbstractSurface;
import de.tub.citygml4j.model.gml.GMLClass;
import de.tub.citygml4j.model.gml.LinearRing;
import de.tub.citygml4j.model.gml.Polygon;

public class DBDeprecatedMaterialModel implements DBImporter {
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private Appearance appearance = null;
	private long lastId = 0;
	private DBAppearance appearanceImporter;
	private String theme;

	public DBDeprecatedMaterialModel(CityGMLFactory cityGMLFactory, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		theme = config.getProject().getImporter().getAppearances().getThemeForTexturedSurface();
		appearanceImporter = (DBAppearance)dbImporterManager.getDBImporter(DBImporterEnum.APPEARANCE);
	}

	public boolean insert(_Appearance _appearance, AbstractSurface abstractSurface, long parentId, boolean isFront, String target) throws SQLException {
		AppearanceModule appFactory = _appearance.getCityGMLModule().getAppearanceDependency();

		if (parentId != lastId) {
			if (lastId != 0 && appearance != null)
				appearanceImporter.insert(appearance, CityGMLClass.CITYOBJECT, lastId);

			appearance = cityGMLFactory.createAppearance(appFactory);
			appearance.setTheme(theme);
			lastId = parentId;
		}

		AbstractSurfaceData abstractSurfaceData = null;
		CityGMLClass type = _appearance.getCityGMLClass();

		switch (type) {
		case _MATERIAL:
			abstractSurfaceData = cityGMLFactory.createX3DMaterial(appFactory);
			break;
		case _SIMPLETEXTURE:
			abstractSurfaceData = cityGMLFactory.createParameterizedTexture(appFactory);
			break;
		}

		abstractSurfaceData.setIsFront(isFront);

		if (type == CityGMLClass._MATERIAL) {
			X3DMaterial material = (X3DMaterial)abstractSurfaceData;
			_Material _material = (_Material)_appearance;

			if (_material.getShininess() != null)
				material.setShininess(_material.getShininess());

			if (_material.getTransparency() != null)
				material.setTransparency(_material.getTransparency());

			if (_material.getAmbientIntensity() != null)
				material.setAmbientIntensity(_material.getAmbientIntensity());

			if (_material.getSpecularColor() != null)
				material.setSpecularColor(cityGMLFactory.createColor(
						_material.getSpecularColor().getRed(),
						_material.getSpecularColor().getGreen(),
						_material.getSpecularColor().getBlue(),
						appFactory
				));

			if (_material.getDiffuseColor() != null)
				material.setDiffuseColor(cityGMLFactory.createColor(
						_material.getDiffuseColor().getRed(),
						_material.getDiffuseColor().getGreen(),
						_material.getDiffuseColor().getBlue(),
						appFactory
				));

			if (_material.getEmissiveColor() != null)
				material.setEmissiveColor(cityGMLFactory.createColor(
						_material.getEmissiveColor().getRed(),
						_material.getEmissiveColor().getGreen(),
						_material.getEmissiveColor().getBlue(),
						appFactory
				));

			// group material instances
			if (appearance.getSurfaceDataMember() != null) {
				for (SurfaceDataProperty property : appearance.getSurfaceDataMember()) {
					if (property.getSurfaceData() != null && property.getSurfaceData() instanceof X3DMaterial) {
						X3DMaterial candidate = (X3DMaterial)property.getSurfaceData();

						if (matches(material, candidate)) {
							candidate.addTarget(target);
							return true;
						}								
					}
				}
			}
			
			material.addTarget(target);
		} else if (type == CityGMLClass._SIMPLETEXTURE) {
			ParameterizedTexture paraTex = (ParameterizedTexture)abstractSurfaceData;
			_SimpleTexture _simpleTex = (_SimpleTexture)_appearance;

			if (_simpleTex.getTextureMap() != null)
				paraTex.setImageURI(_simpleTex.getTextureMap());

			if (_simpleTex.getTextureType() != null)
				paraTex.setTextureType(cityGMLFactory.createTextureType(_simpleTex.getTextureType().getValue(), appFactory));

			if (_simpleTex.getRepeat() != null && _simpleTex.getRepeat())
				paraTex.setWrapMode(cityGMLFactory.createWrapMode("wrap", appFactory));

			if (_simpleTex.getTextureCoordinates() != null) {
				TextureAssociation texAss = cityGMLFactory.createTextureAssociation(appFactory);
				texAss.setUri(target);

				TexCoordList texCoordList = cityGMLFactory.createTexCoordList(appFactory);
				List<Double> _texCoords = _simpleTex.getTextureCoordinates();

				// interpret an inline polygon
				if (abstractSurface.getGMLClass() == GMLClass.POLYGON) {
					Polygon polygon = (Polygon)abstractSurface;

					if (polygon.getExterior() != null) {
						LinearRing exteriorLinearRing = (LinearRing)polygon.getExterior().getRing();
						if (exteriorLinearRing != null) {
							List<Double> points = ((LinearRingImpl)exteriorLinearRing).toList();

							if (points != null && points.size() != 0) {
								// we need two texture coordinates per geometry point
								int noTexPoints = points.size() * 2 / 3;
								int index = _texCoords.size() >= noTexPoints ? noTexPoints : _texCoords.size();
								List<Double> texCoord = _texCoords.subList(0, index);

								if (texCoord.size() > 0) {
									TextureCoordinates texCoords = cityGMLFactory.createTextureCoordinates(appFactory);
									texCoords.setValue(texCoord);
									texCoords.setRing(exteriorLinearRing.getId());
									texCoordList.addTextureCoordinates(texCoords);
								}

								_texCoords.subList(0, index).clear();
							}
						}
					}

					if (polygon.getInterior() != null) {
						List<AbstractRingProperty> abstractRingPropertyList = polygon.getInterior();
						for (AbstractRingProperty abstractRingProperty : abstractRingPropertyList) {
							LinearRing interiorLinearRing = (LinearRing)abstractRingProperty.getRing();
							List<Double> interiorPoints = ((LinearRingImpl)interiorLinearRing).toList();

							if (interiorPoints == null || interiorPoints.size() == 0)
								continue;

							// we need two texture coordinates per geometry point
							int noTexPoints = interiorPoints.size() * 2 / 3;
							int index = _texCoords.size() >= noTexPoints ? noTexPoints : _texCoords.size();
							List<Double> texCoord = _texCoords.subList(0, index);

							if (texCoord.size() > 0) {
								TextureCoordinates texCoords = cityGMLFactory.createTextureCoordinates(appFactory);
								texCoords.setValue(texCoord);
								texCoords.setRing(interiorLinearRing.getId());
								texCoordList.addTextureCoordinates(texCoords);
							}

							_texCoords.subList(0, index).clear();
						}
					}

				}

				texAss.setTextureParameterization(texCoordList);

				// group texture instances
				if (appearance.getSurfaceDataMember() != null) {
					for (SurfaceDataProperty property : appearance.getSurfaceDataMember()) {
						if (property.getSurfaceData() != null && property.getSurfaceData() instanceof ParameterizedTexture) {
							ParameterizedTexture candidate = (ParameterizedTexture)property.getSurfaceData();

							if (matches(paraTex, candidate)) {
								candidate.addTarget(texAss);
								return true;
							}								
						}
					}
				}
				
				paraTex.addTarget(texAss);
			}
		}

		if (abstractSurfaceData != null) {
			if (_appearance.getId() != null && _appearance.getId().length() != 0)
				abstractSurfaceData.setId(_appearance.getId());
		
			SurfaceDataProperty surfaceDataProperty = cityGMLFactory.createSurfaceDataProperty(appFactory);
			surfaceDataProperty.setSurfaceData(abstractSurfaceData);
			appearance.addSurfaceDataMember(surfaceDataProperty);

			return true;
		}

		return false;
	}

	public boolean insertXlink(String href, long surfaceGeometryId, long parentId, AppearanceModule appFactory) throws SQLException {
		Appearance appearance = cityGMLFactory.createAppearance(appFactory);
		appearance.setTheme(theme);

		long appearanceId = appearanceImporter.insert(appearance, CityGMLClass.CITYOBJECT, parentId);
		if (appearanceId != 0) {
			DBXlinkDeprecatedMaterial xlink = new DBXlinkDeprecatedMaterial(
					appearanceId,
					href,
					surfaceGeometryId
			);

			dbImporterManager.propagateXlink(xlink);
			return true;
		}

		return false;
	}

	@Override
	public void executeBatch() throws SQLException {
		if (appearance != null && lastId != 0)
			appearanceImporter.insert(appearance, CityGMLClass.CITYOBJECT, lastId);

		appearance = null;
		lastId = 0;
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.DEPRECATED_MATERIAL_MODEL;
	}

	private boolean matches(ParameterizedTexture first, ParameterizedTexture second) {
		Boolean firstFront = first.getIsFront();
		Boolean secondFront = second.getIsFront();
		
		if ((firstFront == null && secondFront != null) ||
				(secondFront== null && firstFront != null))
			return false;
		
		if (firstFront != null && secondFront != null &&
				!firstFront.equals(secondFront))
			return false;
				
		if (!first.getImageURI().equals(second.getImageURI()))
			return false;

		if (first.getTextureType() != second.getTextureType())
			return false;

		if (first.getWrapMode() != second.getWrapMode())
			return false;

		ColorPlusOpacity firstBorder = first.getBorderColor();
		ColorPlusOpacity secondBorder = second.getBorderColor();

		if ((firstBorder == null && secondBorder != null) ||
				(secondBorder == null && firstBorder != null))
			return false;

		if (firstBorder != null && secondBorder != null && !firstBorder.equals(secondBorder))
			return false;

		return true;
	}
	
	private boolean matches(X3DMaterial first, X3DMaterial second) {
		int i = 0;
		Boolean firstBoolean = null; 
		Boolean secondBoolean = null; 
		
		do {			
			switch (i) {
			case 0:
				firstBoolean = first.getIsFront();
				secondBoolean = second.getIsFront();
				break;
			case 1:
				firstBoolean = first.getIsSmooth();
				secondBoolean = second.getIsSmooth();
				break;
			}
			
			if ((firstBoolean == null && secondBoolean != null) ||
					(secondBoolean== null && firstBoolean != null))
				return false;
			
			if (firstBoolean != null && secondBoolean != null && 
					!firstBoolean.equals(secondBoolean))
				return false;
			
		} while (i++ < 1);
		
		i = 0;
		Double firstDouble = null;
		Double secondDouble = null;
		
		do {						
			switch (i) {
			case 0:
				firstDouble = first.getShininess();
				secondDouble = second.getShininess();
				break;
			case 1:
				firstDouble = first.getTransparency();
				secondDouble = second.getTransparency();
				break;
			case 2:
				firstDouble = first.getAmbientIntensity();
				secondDouble = second.getAmbientIntensity();
				break;
			}
			
			if ((firstDouble == null && secondDouble != null) ||
					(secondDouble== null && firstDouble != null))
				return false;
			
			if (firstDouble != null && secondDouble != null && 
					firstDouble.doubleValue() != secondDouble.doubleValue())
				return false;
			
		} while (i++ < 2);
		
		i = 0;
		Color firstColor = null; 
		Color secondColor = null; 
		
		do {						
			switch (i) {
			case 0:
				firstColor = first.getSpecularColor();
				secondColor = second.getSpecularColor();
				break;
			case 1:
				firstColor = first.getDiffuseColor();
				secondColor = second.getDiffuseColor();
				break;
			case 2:
				firstColor = first.getEmissiveColor();
				secondColor = second.getEmissiveColor();
				break;
			}
			
			if ((firstColor == null && secondColor != null) ||
					(secondColor== null && firstColor != null))
				return false;
			
			if (firstColor != null && secondColor != null && !firstColor.equals(secondColor))
				return false;
			
		} while (i++ < 2);
		
		return true;
	}
}
