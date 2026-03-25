//苗木数据模型
package com.example.printerfeature.model;

public class PlantData {
    public final String variety;
    public final String generation;
    public final String cultivationMethod;
    public final String blockName;
    public final String motherTree;
    public final String plantedDate;
    public final String traceCode;

    public PlantData(String variety, String generation, String cultivationMethod, String blockName,
                       String motherTree, String plantedDate, String traceCode) {
        this.variety = variety;
        this.generation = generation;
        this.cultivationMethod = cultivationMethod;
        this.blockName = blockName;
        this.motherTree = motherTree;
        this.plantedDate = plantedDate;
        this.traceCode = traceCode;
    }
}
