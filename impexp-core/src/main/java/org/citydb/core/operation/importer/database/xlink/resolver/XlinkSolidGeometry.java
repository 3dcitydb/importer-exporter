/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.operation.importer.database.xlink.resolver;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.database.DatabaseType;
import org.citydb.core.operation.common.xlink.DBXlinkSolidGeometry;
import org.citydb.util.log.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XlinkSolidGeometry implements DBXlinkResolver {
    private final Logger log = Logger.getInstance();
    private final Connection connection;
    private final DBXlinkResolverManager manager;
    private final PreparedStatement psSelectSurfGeom;
    private final PreparedStatement psUpdateSurfGeom;
    private final int dbSrid;

    private int batchCounter;

    public XlinkSolidGeometry(Connection connection, DBXlinkResolverManager manager) throws SQLException {
        this.connection = connection;
        this.manager = manager;

        dbSrid = manager.getDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid();
        psSelectSurfGeom = connection.prepareStatement(manager.getDatabaseAdapter().getSQLAdapter().getHierarchicalGeometryQuery());
        String schema = manager.getDatabaseAdapter().getConnectionDetails().getSchema();

        StringBuilder stmt = new StringBuilder("update ").append(schema).append(".SURFACE_GEOMETRY set SOLID_GEOMETRY=");
        if (manager.getDatabaseAdapter().getDatabaseType() == DatabaseType.POSTGIS) {
            // the current PostGIS JDBC driver lacks support for geometry objects of type PolyhedralSurface
            // thus, we have to use the database function ST_GeomFromEWKT to insert such geometries
            // TODO: rework as soon as the JDBC driver supports PolyhedralSurface
            stmt.append("ST_GeomFromEWKT(?) ");
        } else
            stmt.append("? ");

        stmt.append("where ID=?");
        psUpdateSurfGeom = connection.prepareStatement(stmt.toString());
    }

    public boolean insert(DBXlinkSolidGeometry xlink) throws SQLException {
        GeometryNode root = read(xlink.getId());
        if (root == null) {
            log.error("Failed to read solid geometry with id '" + xlink.getId() + "'.");
            return false;
        }

        // get solids from SURFACE_GEOMETRY as GeometryObject instances
        List<GeometryObject> solids = new ArrayList<>();
        rebuildSolids(root, solids, new ArrayList<>());

        if (!solids.isEmpty()) {
            GeometryObject solidObj;

            if (!root.isComposite)
                solidObj = solids.get(0);
            else {
                GeometryObject[] tmp = new GeometryObject[solids.size()];

                int i = 0;
                for (GeometryObject solid : solids)
                    tmp[i++] = solid;

                solidObj = GeometryObject.createCompositeSolid(tmp, dbSrid);
            }

            if (solidObj != null) {
                psUpdateSurfGeom.setObject(1, manager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(solidObj, connection));
                psUpdateSurfGeom.setLong(2, xlink.getId());
                psUpdateSurfGeom.addBatch();
                if (++batchCounter == manager.getDatabaseAdapter().getMaxBatchSize())
                    manager.executeBatch(this);
            }
        }

        return true;
    }

    private void rebuildSolids(GeometryNode node, List<GeometryObject> solids, List<GeometryObject> surfaces) {
        if (node.geometry != null)
            surfaces.add(node.geometry);

        // visit child nodes depth-first
        for (GeometryNode childNode : node.childNodes)
            rebuildSolids(childNode, solids, surfaces);

        // rebuild solid geometry
        if (node.isSolid && !node.isComposite) {
            if (!surfaces.isEmpty()) {

                int nrOfRings = 0;
                for (GeometryObject surface : surfaces)
                    nrOfRings += surface.getNumElements();

                int[] exteriorRings = new int[surfaces.size()];
                double[][] coordinates = new double[nrOfRings][];

                int ringNo = 0;
                for (int i = 0; i < surfaces.size(); i++) {
                    GeometryObject surface = surfaces.get(i);
                    exteriorRings[i] = ringNo;
                    for (int j = 0; j < surface.getNumElements(); j++)
                        coordinates[ringNo + j] = surface.getCoordinates(j);

                    ringNo += surface.getNumElements();
                }

                solids.add(GeometryObject.createSolid(coordinates, exteriorRings, dbSrid));
                surfaces.clear();
            }
        }
    }

    private GeometryNode read(long rootId) throws SQLException {
        psSelectSurfGeom.setLong(1, rootId);

        try (ResultSet rs = psSelectSurfGeom.executeQuery()) {
            GeometryNode root = null;
            Map<Long, GeometryNode> parentMap = new HashMap<>();

            // rebuild geometry hierarchy
            while (rs.next()) {
                long id = rs.getLong("id");
                long parentId = rs.getLong("parent_id");

                // constructing a geometry node
                GeometryNode geomNode = new GeometryNode();
                geomNode.isSolid = rs.getBoolean("is_solid");
                geomNode.isComposite = rs.getBoolean("is_composite");

                GeometryObject geometry = null;
                Object object = rs.getObject("geometry");
                if (!rs.wasNull() && object != null)
                    geometry = manager.getDatabaseAdapter().getGeometryConverter().getPolygon(object);

                geomNode.geometry = geometry;

                if (root != null) {
                    GeometryNode parentNode = parentMap.get(parentId);
                    if (parentNode == null)
                        return null;

                    parentNode.childNodes.add(geomNode);
                } else
                    root = geomNode;

                // make this node the parent for the next hierarchy level
                if (geomNode.geometry == null)
                    parentMap.put(id, geomNode);
            }

            return root;
        }
    }

    private static class GeometryNode {
        private boolean isSolid;
        private boolean isComposite;
        private GeometryObject geometry;
        private final List<GeometryNode> childNodes = new ArrayList<>();
    }

    @Override
    public void executeBatch() throws SQLException {
        psUpdateSurfGeom.executeBatch();
        batchCounter = 0;
    }

    @Override
    public void close() throws SQLException {
        psSelectSurfGeom.close();
        psUpdateSurfGeom.close();
    }

    @Override
    public DBXlinkResolverEnum getDBXlinkResolverType() {
        return DBXlinkResolverEnum.SOLID_GEOMETRY;
    }
}
