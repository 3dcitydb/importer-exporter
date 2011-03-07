package de.tub.citydb.db.exporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

import de.tub.citydb.util.Util;

public class DBTextureParam implements DBExporter {
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
		"sg.GMLID from TEXTUREPARAM tp inner join SURFACE_GEOMETRY sg on sg.ID=tp.SURFACE_GEOMETRY_ID where tp.SURFACE_DATA_ID=?");
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
									TextureCoordinates texureCoordinates = cityGMLFactory.createTextureCoordinates(app);
									texureCoordinates.setValue(coordsList);
									texureCoordinates.setRing(target + "_" + i);

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
