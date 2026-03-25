//定义模板名称和业务类型，为系统提供标准引用
package com.example.printerfeature.data;

public final class LabelTemplates {
    private LabelTemplates() {}

    public static final String TEMP_MM = "苗木二维码";
    public static final String TEMP_CJG = "加工二维码";
    public static final String TEMP_CP = "产成品二维码";
    public static final String TEMP_DP = "大棚二维码";
    public static final String TEMP_MC = "苗床二维码";
    public static final String TEMP_DK = "地块二维码";

    public static final String TYPE_INITIAL = "初加工";
    public static final String TYPE_DEEP = "精加工";

    public static String[] allTemplates() {
        return new String[]{TEMP_MM, TEMP_CJG, TEMP_CP, TEMP_DP, TEMP_MC, TEMP_DK};
    }

    public static String[] processingTypes() {
        return new String[]{TYPE_INITIAL, TYPE_DEEP};
    }
}
