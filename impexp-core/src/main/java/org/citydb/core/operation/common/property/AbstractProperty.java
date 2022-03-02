package org.citydb.core.operation.common.property;

public abstract class AbstractProperty {
    protected String name;
    protected String namespace;
    protected String dataType;
    protected int indexNumber;
    protected Object value;

    public abstract PropertyType getType();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public int getIndexNumber() {
        return indexNumber;
    }

    public void setIndexNumber(int indexNumber) {
        this.indexNumber = indexNumber;
    }

    public boolean isSetValue() {
        return value != null;
    }
}
