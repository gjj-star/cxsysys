//大棚数据模型
package com.example.printerfeature.model;

public class GreenhouseLabelData {
    public final String greenhouseName;
    public final String selfCode;
    public final String plantationName;
    public final String area;
    public final String status;
    public final String owner;
    public final String traceCode;

    public GreenhouseLabelData(String greenhouseName, String selfCode, String plantationName, String area, String status, String owner, String traceCode) {
        this.greenhouseName = greenhouseName;
        this.selfCode = selfCode;
        this.plantationName = plantationName;
        this.area = area;
        this.status = status;
        this.owner = owner;
        this.traceCode = traceCode;
    }
}
