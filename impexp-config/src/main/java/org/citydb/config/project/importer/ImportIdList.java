package org.citydb.config.project.importer;

import org.citydb.config.project.common.IdColumnType;
import org.citydb.config.project.common.IdList;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ImportIdListType", propOrder = {})
public class ImportIdList extends IdList {
    private ImportIdListMode mode = ImportIdListMode.IMPORT;

    @Override
    public ImportIdList withDefaultCommentCharacter(Character commentCharacter) {
        return (ImportIdList) super.withDefaultCommentCharacter(commentCharacter);
    }

    public ImportIdListMode getMode() {
        return mode != null ? mode : ImportIdListMode.IMPORT;
    }

    public void setMode(ImportIdListMode mode) {
        this.mode = mode;
    }

    @Override
    public IdColumnType getIdColumnType() {
        return IdColumnType.RESOURCE_ID;
    }

    @Override
    public void setIdColumnType(IdColumnType idColumnType) {
        super.setIdColumnType(IdColumnType.RESOURCE_ID);
    }
}
