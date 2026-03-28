//加工数据模型
package com.example.printerfeature.model;

public class ProcessLabelData {
    public final String processTypeKey;   // material / semi_finished
    public final String processingType;   // 初加工 / 精加工
    public final String processName;
    public final String name;
    public final String model;
    public final String spec;
    public final String num;
    public final String weight;
    public final String grade;
    public final String completedTime;
    public final String operatorId;
    public final String traceCode;

    public ProcessLabelData(String processTypeKey, String processingType, String processName,
                            String name, String model, String spec, String num, String weight,
                            String grade, String completedTime, String operatorId, String traceCode) {
        this.processTypeKey = processTypeKey;
        this.processingType = processingType;
        this.processName = processName;
        this.name = name;
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
