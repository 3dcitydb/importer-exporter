package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "joinTable", propOrder = {
		"join",
		"inverseJoin"
})
public class JoinTable extends AbstractJoin {
	@XmlAttribute(required = true)
	protected String table;
	@XmlElement(required = true)
	protected Join join;
	@XmlElement(required = true)
	protected Join inverseJoin;

	protected JoinTable() {	
	}

	public JoinTable(String table) {
		this.table = table;
	}

	public Join[] getJoins() {
		return new Join[]{join, inverseJoin};
	}

	public boolean isSetJoin() {
		return join != null;
	}

	public Join getJoin() {
		return join;
	}

	public void setJoin(Join join) {
		this.join = join;
	}

	public boolean isSetInverseJoin() {
		return inverseJoin != null;
	}

	public Join getInverseJoin() {
		return inverseJoin;
	}

	public void setInverseJoin(Join inverseJoin) {
		this.inverseJoin = inverseJoin;
	}

	public String getTable() {
		return table;
	}

	public boolean isSetTable() {
		return table != null && !table.isEmpty();
	}

	public void setTable(String table) {
		this.table = table;
	}

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent, Object transitiveParent) throws SchemaMappingException {
		if (!isSetJoin() || !isSetInverseJoin())
			throw new SchemaMappingException("A join table requires both a join and an inverse join."); 

		if (!(parent instanceof AbstractTypeProperty<?>))
			throw new SchemaMappingException("The parent of a join table must be a type property.");

		if (!(transitiveParent instanceof AbstractType<?>) && !(transitiveParent instanceof PropertyInjection))
			throw new SchemaMappingException("The transitive parent of a join table must be a type or a property injection.");

		join.validate(schemaMapping, transitiveParent, null);
		inverseJoin.validate(schemaMapping, parent, null);
	}

}
