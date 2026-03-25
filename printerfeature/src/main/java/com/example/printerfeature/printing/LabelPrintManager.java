//标签模板，打印业务核心
package com.example.printerfeature.printing;

import com.example.printerfeature.printerSDK;
import com.example.printerfeature.data.LabelTemplates;
import com.example.printerfeature.model.LabelData;
import com.gengcon.www.jcprintersdk.callback.PrintCallback;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LabelPrintManager {
    public interface PrintJobListener {
        void onCompleted();
        void onError(int errorCode);
    }

    private int generatedCount = 0;

    public void startPrint(List<LabelData> dataList, PrintJobListener listener) {
        generatedCount = 0;
        int totalCount = dataList.size();

        PrintCallback printCallback = new PrintCallback() {
            @Override
            public void onProgress(int pageIndex, int quantityIndex, HashMap hashMap) {
                if (pageIndex == totalCount) {
                    printerSDK.api.endPrintJob();
                    listener.onCompleted();
                }
            }

            @Override
            public void onBufferFree(int pageIndex, int bufferSize) {
                if (generatedCount >= totalCount) return;
                commitPrintData(dataList.get(generatedCount));
                generatedCount++;
            }

            @Override
            public void onError(int errorCode, int printState) {
                listener.onError(errorCode);
            }

            @Override public void onError(int i) {}
            @Override public void onCancelJob(boolean i) {}
        };

        printerSDK.api.setTotalPrintQuantity(totalCount);
        printerSDK.api.startPrintJob(3, 1, 1, printCallback);
    }

    private void commitPrintData(LabelData data) {
        float w = 70, h = 50;
        printerSDK.api.drawEmptyLabel(w, h, 0, new ArrayList<>());
        float margin = 1.5f;

        printerSDK.api.drawLabelLine(margin, margin, 70 - margin * 2, 0.5f, 0, 1, new float[]{});
        printerSDK.api.drawLabelLine(margin, 50 - margin, 70 - margin * 2, 0.5f, 0, 1, new float[]{});
        printerSDK.api.drawLabelLine(margin, margin, 0.5f, 50 - margin * 2, 0, 1, new float[]{});
        printerSDK.api.drawLabelLine(70 - margin, margin, 0.5f, 50 - margin * 2, 0, 1, new float[]{});

        String title = resolveTitle(data);
        printerSDK.api.drawLabelText(0, 2, 70, 7, title, "", 5.5f, 0, 1, 1, 6, 0, 1, new boolean[]{true, false, false, false});
        printerSDK.api.drawLabelLine(margin, margin + 8f, 67, 0.5f, 0, 1, new float[]{});

        float tableTop = 8 + margin;
        float tableHeight = 32;
        float x1 = margin + 15;
        float x2 = margin + 41;

        if (LabelTemplates.TEMP_DP.equals(data.template) || LabelTemplates.TEMP_MC.equals(data.template)) {
            drawFourRowTable(data, margin, tableTop, tableHeight, x1, x2);
        } else if (LabelTemplates.TEMP_DK.equals(data.template)) {
            drawFiveRowTable(data, margin, tableTop, tableHeight, x1, x2);
        } else {
            drawSixRowTable(data, margin, tableTop, tableHeight, x1, x2);
        }

        printerSDK.api.drawLabelQrCode(x2 + 2, tableTop + 5, 22, 22, data.traceCode, 31, 0);
        printerSDK.api.drawLabelLine(margin, 40 + margin, 67, 0.5f, 0, 1, new float[]{});
        printerSDK.api.drawLabelText(margin + 1, 39 + margin, 68, 8, data.traceCode, "", 2.8f, 0, 1, 1, 6, 0, 1, new boolean[]{false, false, false, false});

        byte[] jsonByte = printerSDK.api.generateLabelJson();
        String jsonStr = new String(jsonByte, StandardCharsets.UTF_8);
        String printerInfo = "{\"printerImageProcessingInfo\":{\"orientation\":0,\"margin\":[0,0,0,0],\"printQuantity\":1,\"width\":70,\"height\":50},\"epc\":\"\"}";
        List<String> dList = new ArrayList<>();
        dList.add(jsonStr);
        List<String> iList = new ArrayList<>();
        iList.add(printerInfo);
        printerSDK.api.commitData(dList, iList);
    }

    private String resolveTitle(LabelData data) {
        if (LabelTemplates.TEMP_MM.equals(data.template)) return "沉香溯源标签【苗木】";
        if (LabelTemplates.TEMP_CJG.equals(data.template)) return "沉香溯源标签【" + data.processingType + "-(" + data.processName + ")】";
        if (LabelTemplates.TEMP_CP.equals(data.template)) return "沉香溯源标签【产成品】";
        if (LabelTemplates.TEMP_DP.equals(data.template)) return "沉香溯源标签【大棚】";
        if (LabelTemplates.TEMP_MC.equals(data.template)) return "沉香溯源标签【苗床】";
        return "沉香溯源标签【地块】";
    }

    private void drawFourRowTable(LabelData data, float margin, float tableTop, float tableHeight, float x1, float x2) {
        float rowH = tableHeight / 4f;
        printerSDK.api.drawLabelLine(x1, tableTop, 0.5f, tableHeight, 0, 1, new float[]{});
        printerSDK.api.drawLabelLine(x2, tableTop, 0.5f, tableHeight, 0, 1, new float[]{});
        for (int i = 1; i <= 3; i++) {
            printerSDK.api.drawLabelLine(margin, tableTop + i * rowH, x2 - 1, 0.4f, 0, 1, new float[]{});
        }

        String[] labels = LabelTemplates.TEMP_DP.equals(data.template)
                ? new String[]{"自编码", "种植园", "面积", "负责人"}
                : new String[]{"自编码", "大棚", "种植园", "负责人"};
        String[] values = {data.f1, data.f2, data.f3, data.f4};
        drawTableRows(labels, values, margin, tableTop, rowH, x1);
    }

    private void drawFiveRowTable(LabelData data, float margin, float tableTop, float tableHeight, float x1, float x2) {
        float rowH = tableHeight / 5f;
        printerSDK.api.drawLabelLine(x1, tableTop, 0.5f, tableHeight, 0, 1, new float[]{});
        printerSDK.api.drawLabelLine(x2, tableTop, 0.5f, tableHeight, 0, 1, new float[]{});
        for (int i = 1; i <= 4; i++) {
            printerSDK.api.drawLabelLine(margin, tableTop + i * rowH, x2 - 1, 0.4f, 0, 1, new float[]{});
        }

        String[] labels = {"自编码", "种植园", "长×宽", "面积", "负责人"};
        String[] values = {data.f1, data.f2, data.f3, data.f4, data.f5};
        drawTableRows(labels, values, margin, tableTop, rowH, x1);
    }

    private void drawSixRowTable(LabelData data, float margin, float tableTop, float tableHeight, float x1, float x2) {
        float rowH = tableHeight / 6f;
        printerSDK.api.drawLabelLine(x1, tableTop, 0.5f, tableHeight, 0, 1, new float[]{});
        printerSDK.api.drawLabelLine(x2, tableTop, 0.5f, tableHeight, 0, 1, new float[]{});
        for (int i = 1; i <= 5; i++) {
            printerSDK.api.drawLabelLine(margin, tableTop + i * rowH, x2 - 1, 0.4f, 0, 1, new float[]{});
        }

        String[] labels;
        if (LabelTemplates.TEMP_MM.equals(data.template)) {
            labels = new String[]{"品种", "代数", "育苗方法", "地块", "母树", "定植日期"};
        } else if (LabelTemplates.TEMP_CJG.equals(data.template)) {
            labels = new String[]{"名称", "型号/规格", "数量/重量", "等级", "完工时间", "操作员ID"};
        } else {
            labels = new String[]{"分类名称", "型号/规格", "数量/重量", "等级", "完工时间", "操作员ID"};
        }
        String[] values = {data.f1, data.f2, data.f3, data.f4, data.f5, data.f6};
        drawTableRows(labels, values, margin, tableTop, rowH, x1);
    }

    private void drawTableRows(String[] labels, String[] values, float margin, float tableTop, float rowH, float x1) {
        for (int i = 0; i < labels.length; i++) {
            float y = tableTop + i * rowH;
            printerSDK.api.drawLabelText(margin + 1, y, 14, rowH, labels[i], "", 3.5f, 0, 0, 1, 6, 0, 1, new boolean[]{false, false, false, false});
            printerSDK.api.drawLabelText(x1 + 1, y, 25, rowH, values[i], "", 3.0f, 0, 0, 1, 6, 0, 1, new boolean[]{false, false, false, false});
        }
    }
}
