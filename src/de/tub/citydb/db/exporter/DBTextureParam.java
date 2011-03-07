package de.tub.citydb.db.exporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import de.tub.citydb.util.Util;
import de.tub.citygml4j.CityGMLFactory;
import de.tub.citygml4j.geometry.Matrix4;
import de.tub.citygml4j.model.citygml.CityGMLClass;
import de.tub.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import de.tub.citygml4j.model.citygml.appearance.AppearanceModule;
import de.tub.citygml4j.model.citygml.appearance.GeoreferencedTexture;
import de.tub.citygml4j.model.citygml.appearance.ParameterizedTexture;
import de.tub.citygml4j.model.citygml.appearance.TexCoordGen;
import de.tub.citygml4j.model.citygml.appearance.TexCoordList;
import de.tub.citygml4j.model.citygml.appearance.TextureAssociation;
import de.tub.citygml4j.model.citygml.appearance.TextureCoordinates;
import de.tub.citygml4j.model.citygml.appearance.WorldToTexture;
import de.tub.citygml4j.model.citygml.appearance.X3DMaterial;

public class DBTextureParam implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Connection connection;

	private PreparedStatement psTextureParam;
	private ResultSet rs;

	public DBTextureParam(Connection connection, CityGMLFactory cityGMLFactory, DBExporterManager dbExporterManager) throws SQLException {
		this.dbExporterManager = dbExporterManager;
		this.cityGMLFactory = cityGMLFactory;
		this.connection = connection;

		init();
	}

	private void init() throws SQLException {
		psTextureParam = connection.prepareStatement("select tp.WORLD_TO_TEXTURE, tp.TEXTURE_COORDINATES, " +
				"sg.GMLID from TEXTUREPARAM tp inner join SURFACE_GEOMETRY sg on sg.ID=tp.SURFACE_GEOMETRY_ID where tp.SURFACE_DATA_ID=?");
	}

	public void read(AbstractSurfaceData surfaceData, long surfaceDataId, AppearanceModule factory) throws SQLException {
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
					TextureAssociation textureAssociation = cityGMLFactory.createTextureAssociation(factory);
					textureAssociation.setUri(target);

					String[] rings = textureCoordinates.trim().split("\\s*;\\s*");
					if (rings != null && rings.length != 0) {
						TexCoordList texCoordList = cityGMLFactory.createTexCoordList(factory);

						for (int i = 0; i < rings.length; i++) {
							String splitter = rings[i];

							if (splitter == null || splitter.length() == 0)
								continue;

							List<Double> coordsList = Util.string2double(splitter, "\\s+");
							if (coordsList != null && coordsList.size() != 0) {
								TextureCoordinates texureCoordinates = cityGMLFactory.createTextureCoordinates(factory);
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
					TextureAssociation textureAssociation = cityGMLFactory.createTextureAssociation(factory);
					textureAssociation.setUri(target);

					List<Double> m = Util.string2double(worldToTexture, "\\s+");
					if (m != null && m.size() >= 12) {
						Matrix4 matrix = new Matrix4(
								m.get(0), m.get(1), m.get(2), m.get(3),
								m.get(4), m.get(5), m.get(6), m.get(7),
								m.get(8), m.get(9), m.get(10), m.get(11)
						);

						WorldToTexture worldToTextureMatrix = cityGMLFactory.createWorldToTexture(factory);
						worldToTextureMatrix.setTransformationMatrix3x4(matrix);

						TexCoordGen texCoordGen = cityGMLFactory.createTexCoordGen(factory);
						texCoordGen.setWorldToTexture(worldToTextureMatrix);

						textureAssociation.setTextureParameterization(texCoordGen);
						paraTex.addTarget(textureAssociation);
					} else {
						// database entry incorrect
					}
				}
			}
		}
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.TEXTUREPARAM;
	}

}
