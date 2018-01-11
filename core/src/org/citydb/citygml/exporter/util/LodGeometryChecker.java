package org.citydb.citygml.exporter.util;

import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.LodRepresentation;
import org.citygml4j.util.walker.FeatureWalker;

public class LodGeometryChecker extends FeatureWalker {
	private final SchemaMapping schemaMapping;
	private final LodWalker lodWalker;

	public LodGeometryChecker(SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;
		lodWalker = new LodWalker();
	}

	public boolean satisfiesLodFilter(AbstractCityObject cityObject) {
		if (cityObject.getLodRepresentation().hasRepresentations())
			return true;

		lodWalker.reset(cityObject);
		cityObject.accept(lodWalker);

		if (!lodWalker.foundGeometry) {
			FeatureType featureType = schemaMapping.getFeatureType(Util.getObjectClassId(cityObject.getClass()));
			if (!featureType.hasLodProperties())
				return true;
		}

		return lodWalker.foundGeometry;
	}

	private final class LodWalker extends FeatureWalker {
		private AbstractCityObject root;
		private boolean foundGeometry = false;

		@Override
		public void visit(AbstractCityObject cityObject) {
			if (cityObject != root) {
				LodRepresentation lodRepresentation = cityObject.getLodRepresentation();
				if (lodRepresentation.hasRepresentations()) {
					foundGeometry = true;
					shouldWalk = false;
				}
			}
		}

		public void reset(AbstractCityObject cityObject) {
			super.reset();
			root = cityObject;
			foundGeometry = false;
		}
	}
}
