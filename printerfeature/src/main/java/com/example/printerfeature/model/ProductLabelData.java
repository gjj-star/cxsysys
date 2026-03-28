//产成品数据模型
package com.example.printerfeature.model;

public class ProductLabelData {
    public final String productName;
    public final String model;
    public final String spec;
    public final String num;
    public final String weight;
    public final String grade;
    public final String completedTime;
    public final String operatorId;
    public final String traceCode;

    public ProductLabelData(String productName, String model, String spec, String num, String weight,
                            String grade, String completedTime, String operatorId, String traceCode) {
        this.productName = productName;
        this.model = model;
        this.spec = spec;
        this.num = num;
        this.weight = weight;
        this.grade = grade;
        this.completedTime = completedTime;
        this.operatorId = operatorId;
        this.traceCode = traceCode;
    }
}
