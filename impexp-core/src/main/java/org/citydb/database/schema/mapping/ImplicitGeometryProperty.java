package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.citygml4j.model.module.citygml.CoreModule;

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
