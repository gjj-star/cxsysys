//示例数据的数据仓库
package com.example.printerfeature.data;

import com.example.printerfeature.model.LabelData;
import com.example.printerfeature.model.FieldLabelData;
import com.example.printerfeature.model.PlantBlockData;
import com.example.printerfeature.model.PlantData;
import com.example.printerfeature.model.PlantationData;
import com.example.printerfeature.model.TemplateExampleData;

import java.util.ArrayList;
import java.util.List;

public final class MockLabelRepository {
    private MockLabelRepository() {}

    public static List<PlantBlockData> getPlantBlocks() {
        List<PlantBlockData> blocks = new ArrayList<>();

        List<PlantData> blockA = new ArrayList<>();
        blockA.add(new PlantData("金丝油", "2代", "嫁接", "一号示范地块", "MS-2012-A005", "2024-05-20", "MM-EX-A01"));
        blockA.add(new PlantData("白木香", "3代", "扦插", "一号示范地块", "MS-2013-B011", "2024-05-22", "MM-EX-A02"));
        blockA.add(new PlantData("奇楠", "1代", "高压", "一号示范地块", "MS-2011-C003", "2024-05-25", "MM-EX-A03"));
        blocks.add(new PlantBlockData("一号示范地块", "DK-101", "A区东侧缓坡", "正常养护", blockA));

        List<PlantData> blockB = new ArrayList<>();
        blockB.add(new PlantData("金丝油", "2代", "嫁接", "二号育苗地块", "MS-2014-D006", "2024-06-02", "MM-EX-B01"));
        blockB.add(new PlantData("蜜香", "2代", "扦插", "二号育苗地块", "MS-2014-D009", "2024-06-04", "MM-EX-B02"));
        blockB.add(new PlantData("白木香", "4代", "实生", "二号育苗地块", "MS-2010-A021", "2024-06-06", "MM-EX-B03"));
        blocks.add(new PlantBlockData("二号育苗地块", "DK-102", "B区连栋棚南侧", "待复核", blockB));

        List<PlantData> blockC = new ArrayList<>();
        blockC.add(new PlantData("奇楠", "1代", "嫁接", "三号保育地块", "MS-2015-F002", "2024-06-10", "MM-EX-C01"));
        blockC.add(new PlantData("白木香", "3代", "扦插", "三号保育地块", "MS-2016-F008", "2024-06-12", "MM-EX-C02"));
        blockC.add(new PlantData("金丝油", "2代", "高压", "三号保育地块", "MS-2017-H015", "2024-06-14", "MM-EX-C03"));
        blocks.add(new PlantBlockData("三号保育地块", "DK-103", "C区西北角", "生长良好", blockC));

        return blocks;
    }

    public static List<PlantationData> getPlantations() {
        List<PlantationData> plantations = new ArrayList<>();

        List<FieldLabelData> fieldsA = new ArrayList<>();
        fieldsA.add(new FieldLabelData("A区-01号地块", "DK-201", "120m × 45m", "5.4亩", "陈大海", "DK-EX-A01"));
        fieldsA.add(new FieldLabelData("A区-02号地块", "DK-202", "100m × 40m", "4.0亩", "陈大海", "DK-EX-A02"));
        fieldsA.add(new FieldLabelData("A区-03号地块", "DK-203", "90m × 35m", "3.2亩", "陈大海", "DK-EX-A03"));
        plantations.add(new PlantationData("东山一号种植园", "ZZY-301", "12.6亩", "正常运营", "陈大海", fieldsA));

        List<FieldLabelData> fieldsB = new ArrayList<>();
        fieldsB.add(new FieldLabelData("B区-01号地块", "DK-311", "80m × 36m", "2.9亩", "李芳", "DK-EX-B01"));
        fieldsB.add(new FieldLabelData("B区-02号地块", "DK-312", "110m × 42m", "4.6亩", "李芳", "DK-EX-B02"));
        fieldsB.add(new FieldLabelData("B区-03号地块", "DK-313", "95m × 38m", "3.6亩", "李芳", "DK-EX-B03"));
        plantations.add(new PlantationData("南岭智慧种植园", "ZZY-302", "11.1亩", "待巡检", "李芳", fieldsB));

        List<FieldLabelData> fieldsC = new ArrayList<>();
        fieldsC.add(new FieldLabelData("C区-01号地块", "DK-321", "130m × 50m", "6.5亩", "王志强", "DK-EX-C01"));
        fieldsC.add(new FieldLabelData("C区-02号地块", "DK-322", "100m × 45m", "4.5亩", "王志强", "DK-EX-C02"));
        fieldsC.add(new FieldLabelData("C区-03号地块", "DK-323", "85m × 40m", "3.4亩", "王志强", "DK-EX-C03"));
        plantations.add(new PlantationData("西湾生态种植园", "ZZY-303", "14.4亩", "生长良好", "王志强", fieldsC));

        return plantations;
    }

    public static TemplateExampleData getTemplateExample(String template) {
        if (LabelTemplates.TEMP_CJG.equals(template)) {
            return new TemplateExampleData(
                    LabelTemplates.TYPE_INITIAL, "初步清理", "沉香片", "", "", "一级", "2024-05-21 14:00:00", "OP-08",
                    "CX-1234", "5×2", "10", "5g", "DDDDDDEEEEEEEEE-AAA-BCCCCCCCC-YYMMDD-BB-GG-SS"
            );
        }
        if (LabelTemplates.TEMP_CP.equals(template)) {
            return new TemplateExampleData(
                    "", "", "极品沉香线香", "", "", "特级", "2024-05-22 10:30:00", "OP-12",
                    "CX-20", "20支", "50", "1.5kg", "DDDDDDEEEEEEEEE-AAA-PCCCCCCCC-YYMMDD-BB-GG-FFF"
            );
        }
        if (LabelTemplates.TEMP_DP.equals(template)) {
            return new TemplateExampleData(
                    "", "", "DP-12345", "ZZY-123", "500亩", "张三", "", "",
                    "", "", "", "", "DDDDDDEEEEEEEEE-PPPPPP-AAAAAA"
            );
        }
        if (LabelTemplates.TEMP_MC.equals(template)) {
            return new TemplateExampleData(
                    "", "", "MC-12345", "DP-123", "ZZY-123", "李四", "", "",
                    "", "", "", "", "DDDDDDEEEEEEEEE-PPPPPP-AAAAAA-SSSSSS"
            );
        }
        if (LabelTemplates.TEMP_DK.equals(template)) {
            return new TemplateExampleData(
                    "", "", "DK-108", "ZZY-123", "", "50亩", "王五", "",
                    "100", "50", "", "", "DDDDDDEEEEEEEEE-PPPPPP-FFFFFF"
            );
        }
        return null;
    }

    public static LabelData toPlantLabel(PlantData plant) {
        return new LabelData(
                LabelTemplates.TEMP_MM,
                "",
                "",
                plant.variety,
                plant.generation,
                plant.cultivationMethod,
                plant.blockName,
                plant.motherTree,
                plant.plantedDate,
                plant.traceCode
        );
    }

    public static LabelData findPlantLabelByTraceCode(List<PlantBlockData> blocks, String traceCode) {
        for (PlantBlockData block : blocks) {
            for (PlantData plant : block.plants) {
                if (traceCode.equalsIgnoreCase(plant.traceCode)) {
                    return toPlantLabel(plant);
                }
            }
        }
        return null;
    }

    public static LabelData toFieldLabel(String plantationName, FieldLabelData field) {
        return new LabelData(
                LabelTemplates.TEMP_DK,
                "",
                "",
                field.selfCode,
                plantationName,
                field.size,
                field.area,
                field.owner,
                "",
                field.traceCode
        );
    }

    public static LabelData findFieldLabelBySelfCode(List<PlantationData> plantations, String selfCode) {
        for (PlantationData plantation : plantations) {
            for (FieldLabelData field : plantation.fields) {
                if (selfCode.equalsIgnoreCase(field.selfCode)) {
                    return toFieldLabel(plantation.name, field);
                }
            }
        }
        return null;
    }

    public static PlantationData findPlantationByName(List<PlantationData> plantations, String name) {
        for (PlantationData plantation : plantations) {
            if (plantation.name.equals(name)) {
                return plantation;
            }
        }
        return null;
    }

    public static PlantBlockData findPlantBlockByName(List<PlantBlockData> blocks, String blockName) {
        for (PlantBlockData block : blocks) {
            if (block.name.equals(blockName)) {
                return block;
            }
        }
        return null;
    }
}
