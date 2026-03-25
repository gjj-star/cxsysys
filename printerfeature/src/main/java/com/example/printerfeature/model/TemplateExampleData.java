//示例数据模型
package com.example.printerfeature.model;

public class TemplateExampleData {
    public final String processingType;
    public final String processName;
    public final String f1;
    public final String f2;
    public final String f3;
    public final String f4;
    public final String f5;
    public final String f6;
    public final String model;
    public final String spec;
    public final String num;
    public final String weight;
    public final String traceCode;

    public TemplateExampleData(String processingType, String processName, String f1, String f2, String f3,
                               String f4, String f5, String f6, String model, String spec,
                               String num, String weight, String traceCode) {
        this.processingType = processingType;
        this.processName = processName;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.f4 = f4;
        this.f5 = f5;
        this.f6 = f6;
        this.model = model;
        this.spec = spec;
        this.num = num;
        this.weight = weight;
        this.traceCode = traceCode;
    }
}
