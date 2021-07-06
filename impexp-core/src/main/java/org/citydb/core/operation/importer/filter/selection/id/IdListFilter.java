package org.citydb.core.operation.importer.filter.selection.id;

import org.citydb.config.project.importer.ImportIdList;
import org.citydb.config.project.importer.ImportIdListMode;
import org.citydb.core.operation.common.csv.IdListException;
import org.citydb.core.operation.common.csv.IdListParser;
import org.citydb.core.query.filter.FilterException;
import org.citydb.util.log.Logger;
import org.citygml4j.model.gml.feature.AbstractFeature;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class IdListFilter {
    private final ImportIdListMode mode;
    private final Set<String> ids;

    public IdListFilter(ImportIdList importIdList) throws FilterException {
        if (importIdList == null) {
            throw new FilterException("Identifier list config must not be null.");
        }

        Logger.getInstance().debug("Loading identifier list into main memory...");
        ids = new HashSet<>();

        try (IdListParser parser = new IdListParser(importIdList)) {
            while (parser.hasNext()) {
                ids.add(parser.nextId());
            }
        } catch (IdListException e) {
            throw new FilterException("Failed to parse identifier list.", e);
        } catch (IOException e) {
            throw new FilterException("Failed to create identifier list parser.", e);
        }

        mode = importIdList.getMode();
    }

    public boolean isSatisfiedBy(AbstractFeature feature)  {
        return feature.isSetId() && (mode == ImportIdListMode.IMPORT) == ids.contains(feature.getId());
    }
}
