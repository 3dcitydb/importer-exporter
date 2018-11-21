/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2018
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
package org.citydb.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.citydb.citygml.common.database.cache.CacheTable;
import org.citydb.citygml.exporter.util.AttributeValueSplitter;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.GeoreferencedTexture;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.TexCoordGen;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.TextureAssociation;
import org.citygml4j.model.citygml.appearance.TextureCoordinates;
import org.citygml4j.model.citygml.appearance.WorldToTexture;
import org.citygml4j.model.citygml.appearance.X3DMaterial;

import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;

public class DBTextureParam implements DBExporter {
	private final CityGMLExportManager exporter;

	private PreparedStatement ps;
	private AttributeValueSplitter valueSplitter;

	public DBTextureParam(boolean isGlobal, Connection connection, CacheTable cacheTable, Config config, CityGMLExportManager exporter) throws SQLException {
		this.exporter = exporter;
		
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		Table textureParam = new Table(TableEnum.TEXTUREPARAM.getName(), schema);
		Table surfaceGeometry = new Table(TableEnum.SURFACE_GEOMETRY.getName(), schema);
		Select select = new Select().addProjection(textureParam.getColumn("world_to_texture"), textureParam.getColumn("texture_coordinates"),
				surfaceGeometry.getColumn("gmlid"), surfaceGeometry.getColumn("is_reverse "))
				.addSelection(ComparisonFactory.equalTo(textureParam.getColumn("surface_data_id"), new PlaceHolder<>()));
		if (!isGlobal) {
			select.addJoin(JoinFactory.inner(surfaceGeometry, "id", ComparisonName.EQUAL_TO, textureParam.getColumn("surface_geometry_id")));
			ps = connection.prepareStatement(select.toString());
		} else {
			Table tmp = new Table(cacheTable.getTableName());
			select.addJoin(JoinFactory.inner(tmp, "id", ComparisonName.EQUAL_TO, textureParam.getColumn("surface_geometry_id")))
			.addJoin(JoinFactory.inner(surfaceGeometry, "id", ComparisonName.EQUAL_TO, tmp.getColumn("id")));
			ps = cacheTable.getConnection().prepareStatement(select.toString());
		}
		
		valueSplitter = exporter.getAttributeValueSplitter();
	}

	protected boolean doExport(AbstractSurfaceData surfaceData, long surfaceDataId) throws SQLException {
		ps.setLong(1, surfaceDataId);

		try (ResultSet rs = ps.executeQuery()) {
			if (!rs.next())
				return false;

			do {
				String target = rs.getString(3);
				if (target == null || target.length() == 0)
					continue;
				
				target = "#" + target;

				if (surfaceData instanceof X3DMaterial) {
					((X3DMaterial)surfaceData).addTarget(target);
				}

				else if (surfaceData instanceof GeoreferencedTexture) {
					((GeoreferencedTexture)surfaceData).addTarget(target);
				}
				
				else if (surfaceData instanceof ParameterizedTexture) {
					ParameterizedTexture parameterizedTexture = (ParameterizedTexture)surfaceData;

					String worldToTexture = rs.getString(1);
					Object texCoordsObj = rs.getObject(2);

					if (texCoordsObj != null) {
						GeometryObject texCoords = exporter.getDatabaseAdapter().getGeometryConverter().getPolygon(texCoordsObj);
						if (texCoords != null && texCoords.getDimension() == 2) {
							TextureAssociation textureAssociation = new TextureAssociation();
							textureAssociation.setUri(target);
							TexCoordList texCoordList = new TexCoordList();

							for (int i = 0; i < texCoords.getNumElements(); i++) {
								double[] coordinates = texCoords.getCoordinates(i);

								// reverse order of texture coordinates if necessary
								boolean isReverse = rs.getBoolean(4);
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
							parameterizedTexture.addTarget(textureAssociation);
						}
					}

					else if (worldToTexture != null) {
						TextureAssociation textureAssociation = new TextureAssociation();
						textureAssociation.setUri(target);

						List<Double> m = valueSplitter.splitDoubleList(worldToTexture);
						if (m.size() >= 12) {
							Matrix matrix = new Matrix(3, 4);
							matrix.setMatrix(m.subList(0, 12));

							WorldToTexture worldToTextureMatrix = new WorldToTexture();
							worldToTextureMatrix.setMatrix(matrix);

							TexCoordGen texCoordGen = new TexCoordGen();
							texCoordGen.setWorldToTexture(worldToTextureMatrix);

							textureAssociation.setTextureParameterization(texCoordGen);
							parameterizedTexture.addTarget(textureAssociation);
						}
					}
				}
			} while (rs.next());

			return true;
		}
	}
	
	@Override
	public void close() throws SQLException {
		ps.close();
	}

}
