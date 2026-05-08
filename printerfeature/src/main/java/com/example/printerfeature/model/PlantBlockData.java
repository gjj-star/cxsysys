//地块数据模型（用于苗木二维码）
package com.example.printerfeature.model;

import java.util.List;

public class PlantBlockData {
    public final int id;
    public final String name;
    public final String selfCode;
    public final String location;
    public final String status;
    public final List<PlantData> plants;

    public PlantBlockData(String name, String selfCode, String location, String status, List<PlantData> plants) {
        this(0, name, selfCode, location, status, plants);
    }

    public PlantBlockData(int id, String name, String selfCode, String location, String status, List<PlantData> plants) {
        this.id = id;
        this.name = name;
        this.selfCode = selfCode;
        this.location = location;
        this.status = status;
        this.plants = plants;
    }
}
