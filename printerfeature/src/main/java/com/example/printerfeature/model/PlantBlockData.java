//地块数据模型
package com.example.printerfeature.model;

import java.util.List;

public class PlantBlockData {
    public final String name;
    public final String selfCode;
    public final String location;
    public final String status;
    public final List<PlantData> plants;

    public PlantBlockData(String name, String selfCode, String location, String status, List<PlantData> plants) {
        this.name = name;
        this.selfCode = selfCode;
        this.location = location;
        this.status = status;
        this.plants = plants;
    }
}
