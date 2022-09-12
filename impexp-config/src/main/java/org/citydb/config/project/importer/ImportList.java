package org.citydb.config.project.importer;

import org.citydb.config.project.common.IdColumnType;
import org.citydb.config.project.common.IdList;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ImportListType")
public class ImportList extends IdList {
    private ImportListMode mode = ImportListMode.IMPORT;

    @Override
    public ImportList withDefaultCommentCharacter(Character commentCharacter) {
        return (ImportList) super.withDefaultCommentCharacter(commentCharacter);
    }

    public ImportListMode getMode() {
        return mode != null ? mode : ImportListMode.IMPORT;
    }

    public void setMode(ImportListMode mode) {
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
