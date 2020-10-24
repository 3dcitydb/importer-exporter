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
package org.citydb.citygml.importer.database.content;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.citydb.citygml.common.database.xlink.DBXlinkDeprecatedMaterial;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.config.Config;
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

public class TexturedSurfaceConverter {
	private final DBAppearance appearanceImporter;
	private final CityGMLImportManager importer;

	private Appearance appearanceWrapper = null;
	private long cityObjectId = 0;
	private String theme;

	public TexturedSurfaceConverter(DBAppearance appearanceImporter, Config config, CityGMLImportManager importer) throws SQLException {
		this.appearanceImporter = appearanceImporter;
		this.importer = importer;

		theme = config.getProject().getImportConfig().getAppearances().getThemeForTexturedSurface();
	}

	protected void convertTexturedSurface(_AbstractAppearance _appearance, AbstractSurface abstractSurface, long parentId, boolean isFront, String target) throws CityGMLImportException, SQLException {
		if (parentId != cityObjectId) {
			if (cityObjectId != 0 && appearanceWrapper != null)
				appearanceImporter.doImport(appearanceWrapper, cityObjectId, true);

			appearanceWrapper = new Appearance();
			appearanceWrapper.setTheme(theme);
			cityObjectId = parentId;
		}

		AbstractSurfaceData abstractSurfaceData = null;
		if (_appearance instanceof _Material)
			abstractSurfaceData = new X3DMaterial();
		else if (_appearance instanceof _SimpleTexture)
			abstractSurfaceData = new ParameterizedTexture();
		else {
			importer.logOrThrowErrorMessage(importer.getObjectSignature(_appearance) +
					": Failed to interpret textured surface.");
			return;
		}

		abstractSurfaceData.setIsFront(isFront);
		if (_appearance.isSetId() && _appearance.getId().length() != 0)
			abstractSurfaceData.setId(_appearance.getId());

		if (_appearance instanceof _Material) {
			X3DMaterial material = (X3DMaterial)abstractSurfaceData;
			_Material _material = (_Material)_appearance;

			if (_material.isSetShininess())
				material.setShininess(_material.getShininess());

			if (_material.isSetTransparency())
				material.setTransparency(_material.getTransparency());

			if (_material.isSetAmbientIntensity())
				material.setAmbientIntensity(_material.getAmbientIntensity());

			if (_material.isSetSpecularColor())
				material.setSpecularColor(new Color(
						_material.getSpecularColor().getRed(),
						_material.getSpecularColor().getGreen(),
						_material.getSpecularColor().getBlue()
						));

			if (_material.isSetDiffuseColor())
				material.setDiffuseColor(new Color(
						_material.getDiffuseColor().getRed(),
						_material.getDiffuseColor().getGreen(),
						_material.getDiffuseColor().getBlue()
						));

			if (_material.isSetEmissiveColor())
				material.setEmissiveColor(new Color(
						_material.getEmissiveColor().getRed(),
						_material.getEmissiveColor().getGreen(),
						_material.getEmissiveColor().getBlue()
						));

			// group material instances
			if (appearanceWrapper.isSetSurfaceDataMember()) {
				for (SurfaceDataProperty property : appearanceWrapper.getSurfaceDataMember()) {
					if (property.isSetSurfaceData() && property.getSurfaceData() instanceof X3DMaterial) {
						X3DMaterial candidate = (X3DMaterial)property.getSurfaceData();

						if (matches(material, candidate)) {
							candidate.addTarget(target);
							return;
						}								
					}
				}
			}

			material.addTarget(target);

		} else {
			ParameterizedTexture paraTex = (ParameterizedTexture)abstractSurfaceData;
			_SimpleTexture _simpleTex = (_SimpleTexture)_appearance;

			if (_simpleTex.isSetTextureMap())
				paraTex.setImageURI(_simpleTex.getTextureMap());

			if (_simpleTex.isSetTextureType())
				paraTex.setTextureType(TextureType.fromValue(_simpleTex.getTextureType().getValue()));

			if (_simpleTex.isSetRepeat() && _simpleTex.getRepeat())
				paraTex.setWrapMode(WrapMode.WRAP);

			if (_simpleTex.isSetTextureCoordinates()) {
				TextureAssociation texAss = new TextureAssociation();
				texAss.setUri(target);

				TexCoordList texCoordList = new TexCoordList();
				List<Double> _texCoords = _simpleTex.getTextureCoordinates();

				// interpret an inline polygon
				if (abstractSurface.getGMLClass() == GMLClass.POLYGON) {
					Polygon polygon = (Polygon)abstractSurface;

					if (polygon.isSetExterior()) {
						LinearRing exteriorLinearRing = (LinearRing)polygon.getExterior().getRing();

						if (exteriorLinearRing != null) {
							List<Double> points = exteriorLinearRing.toList3d();

							if (points != null && points.size() != 0) {
								// we need two texture coordinates per geometry point
								int noTexPoints = points.size() * 2 / 3;
								int index = _texCoords.size() >= noTexPoints ? noTexPoints : _texCoords.size();
								List<Double> texCoord = new ArrayList<>(_texCoords.subList(0, index));

								if (texCoord.size() > 0) {
									TextureCoordinates texCoords = new TextureCoordinates();
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
							List<Double> interiorPoints = interiorLinearRing.toList3d();

							if (interiorPoints == null || interiorPoints.size() == 0)
								continue;

							// we need two texture coordinates per geometry point
							int noTexPoints = interiorPoints.size() * 2 / 3;
							int index = _texCoords.size() >= noTexPoints ? noTexPoints : _texCoords.size();
							List<Double> texCoord = new ArrayList<>(_texCoords.subList(0, index));

							if (texCoord.size() > 0) {
								TextureCoordinates texCoords = new TextureCoordinates();
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
				if (appearanceWrapper.isSetSurfaceDataMember()) {
					for (SurfaceDataProperty property : appearanceWrapper.getSurfaceDataMember()) {
						if (property.isSetSurfaceData() && property.getSurfaceData() instanceof ParameterizedTexture) {
							ParameterizedTexture candidate = (ParameterizedTexture)property.getSurfaceData();

							if (matches(paraTex, candidate)) {
								candidate.addTarget(texAss);
								return;
							}								
						}
					}
				}

				paraTex.addTarget(texAss);
			}
		}

		SurfaceDataProperty surfaceDataProperty = new SurfaceDataProperty();
		surfaceDataProperty.setSurfaceData(abstractSurfaceData);
		appearanceWrapper.addSurfaceDataMember(surfaceDataProperty);
	}

	protected void convertTexturedSurfaceXlink(String href, long surfaceGeometryId, long parentId) throws CityGMLImportException, SQLException {
		Appearance appearance = new Appearance();
		appearance.setTheme(theme);

		long appearanceId = appearanceImporter.doImport(appearance, parentId, true);
		importer.propagateXlink(new DBXlinkDeprecatedMaterial(
				appearanceId,
				href,
				surfaceGeometryId));
	}

	protected void flush() throws CityGMLImportException, SQLException {
		if (appearanceWrapper != null && cityObjectId != 0)
			appearanceImporter.doImport(appearanceWrapper, cityObjectId, true);

		appearanceWrapper = null;
		cityObjectId = 0;
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
