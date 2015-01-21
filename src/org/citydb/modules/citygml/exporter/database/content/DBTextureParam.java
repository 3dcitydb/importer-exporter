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
package org.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.modules.citygml.exporter.util.GlobalAppearanceResolver.SurfaceDataTarget;
import org.citydb.modules.citygml.exporter.util.GlobalAppearanceResolver.SurfaceGeometryTarget;
import org.citydb.util.Util;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.GeoreferencedTexture;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.TexCoordGen;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.TextureAssociation;
import org.citygml4j.model.citygml.appearance.TextureCoordinates;
import org.citygml4j.model.citygml.appearance.WorldToTexture;
import org.citygml4j.model.citygml.appearance.X3DMaterial;

public class DBTextureParam implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final DBExporterEnum type;
	private final Connection connection;

	private PreparedStatement psTextureParam;
	private int maxItems;

	public DBTextureParam(DBExporterEnum type, Connection connection, DBExporterManager dbExporterManager) throws SQLException {
		if (type != DBExporterEnum.LOCAL_APPEARANCE_TEXTUREPARAM && type != DBExporterEnum.GLOBAL_APPEARANCE_TEXTUREPARAM)
			throw new IllegalArgumentException("Only type " + DBExporterEnum.LOCAL_APPEARANCE_TEXTUREPARAM + " is allowed.");

		this.type = type;
		this.connection = connection;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		maxItems = dbExporterManager.getDatabaseAdapter().getSQLAdapter().getMaximumNumberOfItemsForInOperator();
		
		if (type == DBExporterEnum.LOCAL_APPEARANCE_TEXTUREPARAM) {
			StringBuilder query = new StringBuilder()
			.append("select tp.WORLD_TO_TEXTURE, tp.TEXTURE_COORDINATES, sg.GMLID, sg.IS_REVERSE from TEXTUREPARAM tp ")
			.append("inner join SURFACE_GEOMETRY sg on sg.ID=tp.SURFACE_GEOMETRY_ID ")
			.append("where tp.SURFACE_DATA_ID=?");
			psTextureParam = connection.prepareStatement(query.toString());
		}
	}

	public boolean read(AbstractSurfaceData surfaceData, long surfaceDataId) throws SQLException {
		ResultSet rs = null;

		try {
			psTextureParam.setLong(1, surfaceDataId);
			rs = psTextureParam.executeQuery();

			while (rs.next()) {
				String worldToTexture = rs.getString(1);
				Object texCoordsObj = rs.getObject(2);
				String target = rs.getString(3);
				boolean isReverse = rs.getBoolean(4);

				fillTextureParam(surfaceData, worldToTexture, texCoordsObj, target, isReverse);
			}

			return surfaceData.hasLocalProperty("hasTargets");
		} finally {
			if (rs != null)
				rs.close();
		}
	}
	
	public boolean read(AbstractSurfaceData surfaceData, long surfaceDataId, SurfaceDataTarget target) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuilder query = new StringBuilder()
			.append("select WORLD_TO_TEXTURE, TEXTURE_COORDINATES, SURFACE_GEOMETRY_ID from TEXTUREPARAM ")
			.append("where SURFACE_DATA_ID=").append(surfaceDataId).append(" and ")
			.append(Util.buildInOperator(target.getSurfaceGeometryIds(), "SURFACE_GEOMETRY_ID", "or", maxItems));
			
			stmt = connection.prepareStatement(query.toString());
			rs = stmt.executeQuery();

			while (rs.next()) {
				String worldToTexture = rs.getString(1);
				Object texCoordsObj = rs.getObject(2);
				long surfaceGeometryId = rs.getLong(3);				
				SurfaceGeometryTarget geometry = target.getSurfaceGeometryTarget(surfaceGeometryId);
				
				fillTextureParam(surfaceData, worldToTexture, texCoordsObj, geometry.getGmlId(), geometry.isReverse());
			}
			
			return surfaceData.hasLocalProperty("hasTargets");
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					//
				}
			}

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					//
				}
			}
		}
	}

	private void fillTextureParam(AbstractSurfaceData surfaceData, String worldToTexture, Object texCoordsObj, String target, boolean isReverse) throws SQLException {
		if (target == null || target.length() == 0)
			return;
		else
			target = "#" + target;

		if (surfaceData.getCityGMLClass() == CityGMLClass.X3D_MATERIAL) {
			X3DMaterial material = (X3DMaterial)surfaceData;
			material.addTarget(target);
		}

		else if (surfaceData.getCityGMLClass() == CityGMLClass.GEOREFERENCED_TEXTURE) {
			GeoreferencedTexture geoTex = (GeoreferencedTexture)surfaceData;
			geoTex.addTarget(target);
		}

		else if (surfaceData.getCityGMLClass() == CityGMLClass.PARAMETERIZED_TEXTURE) {
			ParameterizedTexture paraTex = (ParameterizedTexture)surfaceData;

			if (texCoordsObj != null) {
				GeometryObject texCoords = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getPolygon(texCoordsObj);
				if (texCoords != null && texCoords.getDimension() == 2) {
					TextureAssociation textureAssociation = new TextureAssociation();
					textureAssociation.setUri(target);
					TexCoordList texCoordList = new TexCoordList();

					for (int i = 0; i < texCoords.getNumElements(); i++) {
						double[] coordinates = texCoords.getCoordinates(i);

						// reverse order of texture coordinates if necessary
						if (isReverse) {
							for (int lower = 0, upper = coordinates.length - 2; lower < upper; lower += 2, upper -= 2) {
								double x = coordinates[lower];
								double y = coordinates[lower + 1];
								coordinates[lower] = coordinates[upper];
								coordinates[lower + 1] = coordinates[upper + 1];
								coordinates[upper] = x;
								coordinates[upper + 1] = y;
							}
						}

						List<Double> value = new ArrayList<Double>(coordinates.length);
						for (double coordinate : coordinates)
							value.add(coordinate);

						TextureCoordinates texureCoordinates = new TextureCoordinates();
						texureCoordinates.setValue(value);
						texureCoordinates.setRing(target + '_' + i + '_');

						texCoordList.addTextureCoordinates(texureCoordinates);
					}

					textureAssociation.setTextureParameterization(texCoordList);
					paraTex.addTarget(textureAssociation);
				} else {
					// database entry incorrect
				}
			}

			else if (worldToTexture != null) {
				TextureAssociation textureAssociation = new TextureAssociation();
				textureAssociation.setUri(target);

				List<Double> m = Util.string2double(worldToTexture, "\\s+");
				if (m != null && m.size() >= 12) {
					Matrix matrix = new Matrix(3, 4);
					matrix.setMatrix(m.subList(0, 12));

					WorldToTexture worldToTextureMatrix = new WorldToTexture();
					worldToTextureMatrix.setMatrix(matrix);

					TexCoordGen texCoordGen = new TexCoordGen();
					texCoordGen.setWorldToTexture(worldToTextureMatrix);

					textureAssociation.setTextureParameterization(texCoordGen);
					paraTex.addTarget(textureAssociation);
				} else {
					// database entry incorrect
				}
			}
		}

		surfaceData.setLocalProperty("hasTargets", true);
	}

	@Override
	public void close() throws SQLException {
		if (psTextureParam != null)
			psTextureParam.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return type;
	}

}
