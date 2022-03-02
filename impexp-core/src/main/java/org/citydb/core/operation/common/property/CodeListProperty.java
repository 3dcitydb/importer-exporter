package org.citydb.core.operation.common.property;

public class CodeListProperty extends StringProperty {
    private Integer codeList;

    @Override
    public PropertyType getType() {
        return PropertyType.CODE_LIST;
    }

    public int getCodeList() {
        return codeList;
    }

    public void setCodeList(Integer codeList) {
        this.codeList = codeList;
    }

    public boolean isSetCodeList() {
        return codeList != null;
    }
}
