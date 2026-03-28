//种植园数据模型（用于地块二维码）
package com.example.printerfeature.model;

import java.util.List;

public class PlantationData {
    public final String name;
    public final String selfCode;
    public final String totalArea;
    public final String status;
    public final String owner;
    public final List<FieldLabelData> fields;

    public PlantationData(String name, String selfCode, String totalArea, String status, String owner, List<FieldLabelData> fields) {
        this.name = name;
        this.selfCode = selfCode;
        this.totalArea = totalArea;
        this.status = status;
        this.owner = owner;
        this.fields = fields;
    }
}
