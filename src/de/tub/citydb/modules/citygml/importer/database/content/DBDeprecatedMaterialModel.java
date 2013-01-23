/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
package de.tub.citydb.modules.citygml.importer.database.content;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.citygml4j.impl.citygml.appearance.AppearanceImpl;
import org.citygml4j.impl.citygml.appearance.ColorImpl;
import org.citygml4j.impl.citygml.appearance.ParameterizedTextureImpl;
import org.citygml4j.impl.citygml.appearance.SurfaceDataPropertyImpl;
import org.citygml4j.impl.citygml.appearance.TexCoordListImpl;
import org.citygml4j.impl.citygml.appearance.TextureAssociationImpl;
import org.citygml4j.impl.citygml.appearance.TextureCoordinatesImpl;
import org.citygml4j.impl.citygml.appearance.X3DMaterialImpl;
import org.citygml4j.impl.gml.geometry.primitives.LinearRingImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.Color;
import org.citygml4j.model.citygml.appearance.ColorPlusOpacity;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.SurfaceDataProperty;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.TextureAssociation;
import org.citygml4j.model.citygml.appearance.TextureCoordinates;
import org.citygml4j.model.citygml.appearance.TextureType;
import org.citygml4j.model.citygml.appearance.WrapMode;
import org.citygml4j.model.citygml.appearance.X3DMaterial;
import org.citygml4j.model.citygml.texturedsurface._AbstractAppearance;
import org.citygml4j.model.citygml.texturedsurface._Material;
import org.citygml4j.model.citygml.texturedsurface._SimpleTexture;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.primitives.AbstractRingProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.Polygon;

import de.tub.citydb.config.Config;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkDeprecatedMaterial;

public class DBDeprecatedMaterialModel implements DBImporter {
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private Appearance appearance = null;
	private long lastId = 0;
	private DBAppearance appearanceImporter;
	private String theme;

	public DBDeprecatedMaterialModel(Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		theme = config.getProject().getImporter().getAppearances().getThemeForTexturedSurface();
		appearanceImporter = (DBAppearance)dbImporterManager.getDBImporter(DBImporterEnum.APPEARANCE);
	}

	public boolean insert(_AbstractAppearance _appearance, AbstractSurface abstractSurface, long parentId, boolean isFront, String target) throws SQLException {
		if (parentId != lastId) {
			if (lastId != 0 && appearance != null)
				appearanceImporter.insert(appearance, CityGMLClass.ABSTRACT_CITY_OBJECT, lastId);

			appearance = new AppearanceImpl();
			appearance.setTheme(theme);
			lastId = parentId;
		}

		AbstractSurfaceData abstractSurfaceData;
		CityGMLClass type = _appearance.getCityGMLClass();

		switch (type) {
		case _MATERIAL:
			abstractSurfaceData =new X3DMaterialImpl();
			break;
		case _SIMPLE_TEXTURE:
			abstractSurfaceData = new ParameterizedTextureImpl();
			break;
		default:
			return false;
		}

		abstractSurfaceData.setIsFront(isFront);

		if (type == CityGMLClass._MATERIAL) {
			X3DMaterial material = (X3DMaterial)abstractSurfaceData;
			_Material _material = (_Material)_appearance;

			if (_material.isSetShininess())
				material.setShininess(_material.getShininess());

			if (_material.isSetTransparency())
				material.setTransparency(_material.getTransparency());

			if (_material.isSetAmbientIntensity())
				material.setAmbientIntensity(_material.getAmbientIntensity());

			if (_material.isSetSpecularColor())
				material.setSpecularColor(new ColorImpl(
						_material.getSpecularColor().getRed(),
						_material.getSpecularColor().getGreen(),
						_material.getSpecularColor().getBlue()
				));

			if (_material.isSetDiffuseColor())
				material.setDiffuseColor(new ColorImpl(
						_material.getDiffuseColor().getRed(),
						_material.getDiffuseColor().getGreen(),
						_material.getDiffuseColor().getBlue()
				));

			if (_material.isSetEmissiveColor())
				material.setEmissiveColor(new ColorImpl(
						_material.getEmissiveColor().getRed(),
						_material.getEmissiveColor().getGreen(),
						_material.getEmissiveColor().getBlue()
				));

			// group material instances
			if (appearance.isSetSurfaceDataMember()) {
				for (SurfaceDataProperty property : appearance.getSurfaceDataMember()) {
					if (property.isSetSurfaceData() && property.getSurfaceData() instanceof X3DMaterial) {
						X3DMaterial candidate = (X3DMaterial)property.getSurfaceData();

						if (matches(material, candidate)) {
							candidate.addTarget(target);
							return true;
						}								
					}
				}
			}

			material.addTarget(target);

		} else if (type == CityGMLClass._SIMPLE_TEXTURE) {
			ParameterizedTexture paraTex = (ParameterizedTexture)abstractSurfaceData;
			_SimpleTexture _simpleTex = (_SimpleTexture)_appearance;

			if (_simpleTex.isSetTextureMap())
				paraTex.setImageURI(_simpleTex.getTextureMap());

			if (_simpleTex.isSetTextureType())
				paraTex.setTextureType(TextureType.fromValue(_simpleTex.getTextureType().getValue()));

			if (_simpleTex.isSetRepeat() && _simpleTex.getRepeat())
				paraTex.setWrapMode(WrapMode.WRAP);

			if (_simpleTex.isSetTextureCoordinates()) {
				TextureAssociation texAss = new TextureAssociationImpl();
				texAss.setUri(target);

				TexCoordList texCoordList = new TexCoordListImpl();
				List<Double> _texCoords = _simpleTex.getTextureCoordinates();

				// interpret an inline polygon
				if (abstractSurface.getGMLClass() == GMLClass.POLYGON) {
					Polygon polygon = (Polygon)abstractSurface;

					if (polygon.isSetExterior()) {
						LinearRing exteriorLinearRing = (LinearRing)polygon.getExterior().getRing();

						if (exteriorLinearRing != null) {
							List<Double> points = ((LinearRingImpl)exteriorLinearRing).toList3d();

							if (points != null && points.size() != 0) {
								// we need two texture coordinates per geometry point
								int noTexPoints = points.size() * 2 / 3;
								int index = _texCoords.size() >= noTexPoints ? noTexPoints : _texCoords.size();
								List<Double> texCoord = new ArrayList<Double>(_texCoords.subList(0, index));

								if (texCoord.size() > 0) {
									TextureCoordinates texCoords = new TextureCoordinatesImpl();
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
							List<Double> interiorPoints = ((LinearRingImpl)interiorLinearRing).toList3d();

							if (interiorPoints == null || interiorPoints.size() == 0)
								continue;

							// we need two texture coordinates per geometry point
							int noTexPoints = interiorPoints.size() * 2 / 3;
							int index = _texCoords.size() >= noTexPoints ? noTexPoints : _texCoords.size();
							List<Double> texCoord = new ArrayList<Double>(_texCoords.subList(0, index));

							if (texCoord.size() > 0) {
								TextureCoordinates texCoords = new TextureCoordinatesImpl();
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
				if (appearance.isSetSurfaceDataMember()) {
					for (SurfaceDataProperty property : appearance.getSurfaceDataMember()) {
						if (property.isSetSurfaceData() && property.getSurfaceData() instanceof ParameterizedTexture) {
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

		if (_appearance.isSetId() && _appearance.getId().length() != 0)
			abstractSurfaceData.setId(_appearance.getId());

		SurfaceDataProperty surfaceDataProperty = new SurfaceDataPropertyImpl();
		surfaceDataProperty.setSurfaceData(abstractSurfaceData);
		appearance.addSurfaceDataMember(surfaceDataProperty);

		return true;
	}

	public boolean insertXlink(String href, long surfaceGeometryId, long parentId) throws SQLException {
		Appearance appearance = new AppearanceImpl();
		appearance.setTheme(theme);

		long appearanceId = appearanceImporter.insert(appearance, CityGMLClass.ABSTRACT_CITY_OBJECT, parentId);
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
			appearanceImporter.insert(appearance, CityGMLClass.ABSTRACT_CITY_OBJECT, lastId);

		appearance = null;
		lastId = 0;
	}

	@Override
	public void close() throws SQLException {
		// nothing to do here
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.DEPRECATED_MATERIAL_MODEL;
	}

	private boolean matches(ParameterizedTexture first, ParameterizedTexture second) {
		Boolean firstFront = first.getIsFront();
		Boolean secondFront = second.getIsFront();

		if ((firstFront == null && secondFront != null) ||
				(secondFront == null && firstFront != null))
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
