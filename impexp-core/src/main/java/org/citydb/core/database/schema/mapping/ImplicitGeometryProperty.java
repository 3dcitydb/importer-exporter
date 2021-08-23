/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
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
package org.citydb.core.database.schema.mapping;

import org.citygml4j.model.module.citygml.CoreModule;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "implicitGeometryProperty")
public class ImplicitGeometryProperty extends AbstractRefTypeProperty<ObjectType> {
	@XmlAttribute(required = true)
	protected int lod = -1;

	@XmlTransient
    protected ObjectType objectType;
	
	protected ImplicitGeometryProperty() {
	}
    
    public ImplicitGeometryProperty(String path, int lod, AppSchema schema) {
    	super(path, schema);
    	
    	if (lod < 0 || lod > 4)
			throw new IllegalArgumentException("LOD level of an implicit representation property must be between 0 and 4.");
    	
    	this.lod = lod;
    	populate();
    }

	@Override
	public void setJoin(Join join) {
		// nothing to do here...
	}

	@Override
	public void setJoin(JoinTable join) {
		// nothing to do here...
	}

	@Override
	public ObjectType getType() {
        return objectType;
    }

	@Override
    public boolean isSetType() {
        return objectType != null;
    }
	
	@Override
	public void setType(ObjectType type) {
		// nothing to do here...
	}

	public int getLod() {
		return lod;
	}

	private final void populate() {
		objectType = new ObjectType();
		objectType.path = MappingConstants.IMPLICIT_GEOMETRY_PATH;
		objectType.table = MappingConstants.IMPLICIT_GEOMETRY_TABLE;
		objectType.objectClassId = MappingConstants.IMPLICIT_GEOMETRY_OBJECTCLASS_ID;
		objectType.schemaMapping = new SchemaMapping();
		
		// add app schema
		AppSchema schema = new AppSchema();
		schema.namespaces.add(new Namespace(CoreModule.v2_0_0.getNamespaceURI(), CityGMLContext.CITYGML_2_0));
		schema.setXMLPrefix(CoreModule.v2_0_0.getNamespacePrefix());
		objectType.schema = schema;
		
		// join implicit_geometry table
		join = new Join();
		((Join)join).table = MappingConstants.IMPLICIT_GEOMETRY_TABLE;
		((Join)join).fromColumn = "lod" + lod + "_implicit_rep_id";
		((Join)join).toColumn = "id";
		((Join)join).toRole = TableRole.PARENT;
		
		// join source table
		ReverseJoin join = new ReverseJoin();
		
		SimpleAttribute mimeType = new SimpleAttribute();
		mimeType.path = "mimeType";
		mimeType.column = "mime_type";
		mimeType.type = SimpleType.STRING;
		mimeType.schema = objectType.schema;
		objectType.addProperty(mimeType);
		
		SimpleAttribute transformationMatrix = new SimpleAttribute();
		transformationMatrix.path = "transformationMatrix";
		transformationMatrix.column = "lod" + lod + "_implicit_transformation";
		transformationMatrix.type = SimpleType.STRING;
		transformationMatrix.schema = objectType.schema;
		transformationMatrix.join = join;
		objectType.addProperty(transformationMatrix);
		
		SimpleAttribute libraryObject = new SimpleAttribute();
		libraryObject.path = "libraryObject";
		libraryObject.column = "reference_to_library";
		libraryObject.type = SimpleType.STRING;
		libraryObject.schema = objectType.schema;
		objectType.addProperty(libraryObject);
		
		GeometryProperty relativeGMLGeometry = new GeometryProperty();
		relativeGMLGeometry.path = "relativeGMLGeometry";
		relativeGMLGeometry.refColumn = "relative_brep_id";
		relativeGMLGeometry.type = GeometryType.ABSTRACT_GEOMETRY;
		relativeGMLGeometry.schema = objectType.schema;
		objectType.addProperty(relativeGMLGeometry);
		
		GeometryProperty referencePoint = new GeometryProperty();
		referencePoint.path = "referencePoint";
		referencePoint.refColumn = "lod" + lod + "_implicit_ref_point";
		referencePoint.type = GeometryType.POINT;
		referencePoint.schema = objectType.schema;
		referencePoint.join = join;
		objectType.addProperty(referencePoint);		
	}
    
    @Override
	public PathElementType getElementType() {
		return PathElementType.IMPLICIT_GEOMETRY_PROPERTY;
	}

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		populate();
		super.validate(schemaMapping, parent);
		
		if (lod < 0 || lod > 4)
			throw new SchemaMappingException("LOD level of an implicit representation property must be between 0 and 4.");
	}
    
}
