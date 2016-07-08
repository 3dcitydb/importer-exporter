/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.modules.citygml.common.database.cache.CacheTable;
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
	private final CacheTable tempTable;

	private PreparedStatement psTextureParam;

	public DBTextureParam(DBExporterEnum type, Connection connection, CacheTable tempTable, DBExporterManager dbExporterManager) throws SQLException {
		if (type != DBExporterEnum.LOCAL_APPEARANCE_TEXTUREPARAM && type != DBExporterEnum.GLOBAL_APPEARANCE_TEXTUREPARAM)
			throw new IllegalArgumentException("Only type " + DBExporterEnum.LOCAL_APPEARANCE_TEXTUREPARAM + " is allowed.");

		this.type = type;
		this.connection = connection;
		this.tempTable = tempTable;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {		
		if (type == DBExporterEnum.LOCAL_APPEARANCE_TEXTUREPARAM) {
			StringBuilder query = new StringBuilder()
			.append("select tp.WORLD_TO_TEXTURE, tp.TEXTURE_COORDINATES, sg.GMLID, sg.IS_REVERSE from TEXTUREPARAM tp ")
			.append("inner join SURFACE_GEOMETRY sg on sg.ID=tp.SURFACE_GEOMETRY_ID ")
			.append("where tp.SURFACE_DATA_ID=?");
			psTextureParam = connection.prepareStatement(query.toString());
		} else {
			StringBuilder query = new StringBuilder()
			.append("select tp.WORLD_TO_TEXTURE, tp.TEXTURE_COORDINATES, sg.GMLID, sg.IS_REVERSE from TEXTUREPARAM tp ")
			.append("inner join " + tempTable.getTableName() + " tmp on tmp.ID = tp.SURFACE_GEOMETRY_ID ")
			.append("inner join SURFACE_GEOMETRY sg on sg.ID=tmp.ID ")
			.append("where tp.SURFACE_DATA_ID=?");
			psTextureParam = tempTable.getConnection().prepareStatement(query.toString());			
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
			
			return rs.isAfterLast();
		} finally {
			if (rs != null)
				rs.close();
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
