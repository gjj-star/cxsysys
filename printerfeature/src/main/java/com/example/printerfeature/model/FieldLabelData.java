//地块数据模型（用于地块二维码）
package com.example.printerfeature.model;

public class FieldLabelData {
    public final String fieldName;
    public final String selfCode;
    public final String size;
    public final String area;
    public final String owner;
    public final String traceCode;

    public FieldLabelData(String fieldName, String selfCode, String size, String area, String owner, String traceCode) {
        this.fieldName = fieldName;
        this.selfCode = selfCode;
        this.size = size;
        this.area = area;
        this.owner = owner;
        this.traceCode = traceCode;
    }
}
