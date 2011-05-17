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
package de.tub.citydb.db.exporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.AppearanceModule;
import org.citygml4j.model.citygml.appearance.GeoreferencedTexture;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.TexCoordGen;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.TextureAssociation;
import org.citygml4j.model.citygml.appearance.TextureCoordinates;
import org.citygml4j.model.citygml.appearance.WorldToTexture;
import org.citygml4j.model.citygml.appearance.X3DMaterial;

import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

public class DBTextureParam implements DBExporter {
	private final Logger LOG = Logger.getInstance();
	private final CityGMLFactory cityGMLFactory;
	private final Connection connection;

	private PreparedStatement psTextureParam;

	public DBTextureParam(Connection connection, CityGMLFactory cityGMLFactory) throws SQLException {
		this.cityGMLFactory = cityGMLFactory;
		this.connection = connection;

		init();
	}

	private void init() throws SQLException {
		psTextureParam = connection.prepareStatement("select tp.WORLD_TO_TEXTURE, tp.TEXTURE_COORDINATES, " +
		"sg.GMLID, sg.IS_REVERSE from TEXTUREPARAM tp inner join SURFACE_GEOMETRY sg on sg.ID=tp.SURFACE_GEOMETRY_ID where tp.SURFACE_DATA_ID=?");
	}

	public void read(AbstractSurfaceData surfaceData, long surfaceDataId, AppearanceModule app) throws SQLException {
		ResultSet rs = null;

		try {
			psTextureParam.setLong(1, surfaceDataId);
			rs = psTextureParam.executeQuery();

			while (rs.next()) {
				String worldToTexture = rs.getString("WORLD_TO_TEXTURE");
				String textureCoordinates = rs.getString("TEXTURE_COORDINATES");
				String target = rs.getString("GMLID");
				boolean isReverse = rs.getBoolean("IS_REVERSE");

				if (target == null || target.length() == 0)
					continue;
				else
					target = "#" + target;

				if (surfaceData.getCityGMLClass() == CityGMLClass.X3DMATERIAL) {
					X3DMaterial material = (X3DMaterial)surfaceData;
					material.addTarget(target);
				}

				else if (surfaceData.getCityGMLClass() == CityGMLClass.GEOREFERENCEDTEXTURE) {
					GeoreferencedTexture geoTex = (GeoreferencedTexture)surfaceData;
					geoTex.addTarget(target);
				}

				else if (surfaceData.getCityGMLClass() == CityGMLClass.PARAMETERIZEDTEXTURE) {
					ParameterizedTexture paraTex = (ParameterizedTexture)surfaceData;

					if (textureCoordinates != null) {
						TextureAssociation textureAssociation = cityGMLFactory.createTextureAssociation(app);
						textureAssociation.setUri(target);

						String[] rings = textureCoordinates.trim().split("\\s*;\\s*");
						if (rings != null && rings.length != 0) {
							TexCoordList texCoordList = cityGMLFactory.createTexCoordList(app);

							for (int i = 0; i < rings.length; i++) {
								String splitter = rings[i];

								if (splitter == null || splitter.length() == 0)
									continue;

								List<Double> coordsList = Util.string2double(splitter, "\\s+");
								if (coordsList != null && coordsList.size() != 0) {
									
									// reverse order of texture coordinates if necessary
									if (isReverse) {
										
										// check for even number of texture coordinates
										if ((coordsList.size() & 1) == 1) {
											coordsList.add(0.0);
											
											StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
													surfaceData.getCityGMLClass(), 
													surfaceData.getId()));
											
											msg.append(": Odd number of texture coordinates found. Adding 0.0 to fix this.");
											LOG.error(msg.toString());
										}
										
										List<Double> reverseList = new ArrayList<Double>(coordsList.size());
										for (int j = coordsList.size() - 2; j >= 0; j -= 2) {
											reverseList.add(coordsList.get(j));
											reverseList.add(coordsList.get(j + 1));
										}
										
										coordsList = reverseList;
									}
									
									TextureCoordinates texureCoordinates = cityGMLFactory.createTextureCoordinates(app);
									texureCoordinates.setValue(coordsList);
									texureCoordinates.setRing(target + '_' + i + '_');

									texCoordList.addTextureCoordinates(texureCoordinates);
								} else {
									// database entry incorrect
								}
							}

							textureAssociation.setTextureParameterization(texCoordList);
							paraTex.addTarget(textureAssociation);
						}
					}

					else if (worldToTexture != null) {
						TextureAssociation textureAssociation = cityGMLFactory.createTextureAssociation(app);
						textureAssociation.setUri(target);

						List<Double> m = Util.string2double(worldToTexture, "\\s+");
						if (m != null && m.size() >= 12) {
							Matrix matrix = new Matrix(3, 4);
							matrix.setMatrix(m.subList(0, 12));

							WorldToTexture worldToTextureMatrix = cityGMLFactory.createWorldToTexture(app);
							worldToTextureMatrix.setMatrix(matrix);

							TexCoordGen texCoordGen = cityGMLFactory.createTexCoordGen(app);
							texCoordGen.setWorldToTexture(worldToTextureMatrix);

							textureAssociation.setTextureParameterization(texCoordGen);
							paraTex.addTarget(textureAssociation);
						} else {
							// database entry incorrect
						}
					}
				}
			}
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psTextureParam.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.TEXTUREPARAM;
	}

}
