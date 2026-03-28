//示例数据的数据仓库
package com.example.printerfeature.data;

import com.example.printerfeature.model.LabelData;
import com.example.printerfeature.model.FieldLabelData;
import com.example.printerfeature.model.GreenhouseLabelData;
import com.example.printerfeature.model.PlantBlockData;
import com.example.printerfeature.model.PlantData;
import com.example.printerfeature.model.PlantationData;
import com.example.printerfeature.model.ProcessLabelData;
import com.example.printerfeature.model.ProductLabelData;
import com.example.printerfeature.model.SeedbedLabelData;
import com.example.printerfeature.model.TemplateExampleData;

import java.util.ArrayList;
import java.util.List;

public final class MockLabelRepository {
    private MockLabelRepository() {}
    public static final String PROCESS_TYPE_MATERIAL = "material";
    public static final String PROCESS_TYPE_SEMI_FINISHED = "semi_finished";

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

    public static List<ProcessLabelData> getProcessLabels() {
        List<ProcessLabelData> list = new ArrayList<>();
        list.add(new ProcessLabelData(PROCESS_TYPE_MATERIAL, LabelTemplates.TYPE_INITIAL, "初步清理",
                "沉香片", "CX-1234", "5×2", "10", "5g", "一级", "2024-05-21 14:00:00", "OP-08", "CJG-M-2101"));
        list.add(new ProcessLabelData(PROCESS_TYPE_MATERIAL, LabelTemplates.TYPE_INITIAL, "烘干分拣",
                "沉香丝", "CX-1266", "3×2", "12", "4g", "一级", "2024-05-21 16:20:00", "OP-11", "CJG-M-2102"));
        list.add(new ProcessLabelData(PROCESS_TYPE_SEMI_FINISHED, LabelTemplates.TYPE_DEEP, "精制提纯",
                "沉香半成品粉", "SF-201", "40g", "30", "1.2kg", "特级", "2024-05-22 10:10:00", "OP-03", "CJG-S-2201"));
        list.add(new ProcessLabelData(PROCESS_TYPE_SEMI_FINISHED, LabelTemplates.TYPE_DEEP, "压制成型",
                "沉香半成品片", "SF-302", "25片", "20", "1.8kg", "精品", "2024-05-22 13:40:00", "OP-05", "CJG-S-2202"));
        return list;
    }

    public static LabelData toProcessLabel(ProcessLabelData item) {
        return new LabelData(
                LabelTemplates.TEMP_CJG,
                item.processingType,
                item.processName,
                item.name,
                item.model + " / " + item.spec,
                item.num + " / " + item.weight,
                item.grade,
                item.completedTime,
                item.operatorId,
                item.traceCode
        );
    }

    public static List<LabelData> findProcessLabelsByTypeAndDate(String typeKey, String date) {
        List<LabelData> labels = new ArrayList<>();
        for (ProcessLabelData item : getProcessLabels()) {
            if (typeKey.equals(item.processTypeKey) && item.completedTime.startsWith(date)) {
                labels.add(toProcessLabel(item));
            }
        }
        if (labels.isEmpty()) {
            String processingType = PROCESS_TYPE_MATERIAL.equals(typeKey) ? LabelTemplates.TYPE_INITIAL : LabelTemplates.TYPE_DEEP;
            String processName = PROCESS_TYPE_MATERIAL.equals(typeKey) ? "初步清理" : "精制提纯";
            labels.add(toProcessLabel(new ProcessLabelData(
                    typeKey, processingType, processName,
                    PROCESS_TYPE_MATERIAL.equals(typeKey) ? "沉香片" : "沉香半成品粉",
                    PROCESS_TYPE_MATERIAL.equals(typeKey) ? "CX-1234" : "SF-201",
                    PROCESS_TYPE_MATERIAL.equals(typeKey) ? "5×2" : "40g",
                    "10", PROCESS_TYPE_MATERIAL.equals(typeKey) ? "5g" : "1.2kg",
                    "一级", date + " 10:00:00", "OP-08",
                    "CJG-TEST-" + typeKey + "-" + date.replace("-", "") + "-01"
            )));
            labels.add(toProcessLabel(new ProcessLabelData(
                    typeKey, processingType, processName,
                    PROCESS_TYPE_MATERIAL.equals(typeKey) ? "沉香丝" : "沉香半成品片",
                    PROCESS_TYPE_MATERIAL.equals(typeKey) ? "CX-1266" : "SF-302",
                    PROCESS_TYPE_MATERIAL.equals(typeKey) ? "3×2" : "25片",
                    "20", PROCESS_TYPE_MATERIAL.equals(typeKey) ? "6g" : "1.6kg",
                    "精品", date + " 15:20:00", "OP-12",
                    "CJG-TEST-" + typeKey + "-" + date.replace("-", "") + "-02"
            )));
        }
        return labels;
    }

    public static LabelData findProcessLabelByTypeAndTraceCode(String typeKey, String traceCode) {
        for (ProcessLabelData item : getProcessLabels()) {
            if (typeKey.equals(item.processTypeKey) && traceCode.equalsIgnoreCase(item.traceCode)) {
                return toProcessLabel(item);
            }
        }
        return null;
    }

    public static List<ProductLabelData> getProductLabels() {
        List<ProductLabelData> products = new ArrayList<>();
        products.add(new ProductLabelData("极品沉香线香", "CX-20", "20支", "50", "1.5kg", "特级", "2024-05-22 10:30:00", "OP-12", "CP-EX-2201"));
        products.add(new ProductLabelData("商务沉香盘香", "PX-10", "10片", "80", "2.0kg", "一级", "2024-05-22 15:40:00", "OP-09", "CP-EX-2202"));
        products.add(new ProductLabelData("礼盒沉香粉", "FH-05", "50g", "120", "6.0kg", "特级", "2024-05-23 09:20:00", "OP-15", "CP-EX-2301"));
        products.add(new ProductLabelData("家用沉香片", "XP-08", "8片", "60", "2.8kg", "一级", "2024-05-23 14:10:00", "OP-18", "CP-EX-2302"));
        products.add(new ProductLabelData("沉香礼品套装", "TZ-01", "1套", "30", "3.2kg", "精品", "2024-05-24 11:00:00", "OP-07", "CP-EX-2401"));
        return products;
    }

    public static LabelData toProductLabel(ProductLabelData product) {
        return new LabelData(
                LabelTemplates.TEMP_CP,
                "",
                "",
                product.productName,
                product.model + " / " + product.spec,
                product.num + " / " + product.weight,
                product.grade,
                product.completedTime,
                product.operatorId,
                product.traceCode
        );
    }

    public static List<LabelData> findProductLabelsByDate(String date) {
        List<LabelData> labels = new ArrayList<>();
        for (ProductLabelData product : getProductLabels()) {
            if (product.completedTime.startsWith(date)) {
                labels.add(toProductLabel(product));
            }
        }
        if (labels.isEmpty()) {
            // 便于联调测试：任意日期都可得到当日可打印的产成品标签
            labels.add(toProductLabel(new ProductLabelData(
                    "极品沉香线香",
                    "CX-20",
                    "20支",
                    "50",
                    "1.5kg",
                    "特级",
                    date + " 10:30:00",
                    "OP-12",
                    "CP-TEST-" + date.replace("-", "") + "-01"
            )));
            labels.add(toProductLabel(new ProductLabelData(
                    "商务沉香盘香",
                    "PX-10",
                    "10片",
                    "80",
                    "2.0kg",
                    "一级",
                    date + " 15:40:00",
                    "OP-09",
                    "CP-TEST-" + date.replace("-", "") + "-02"
            )));
        }
        return labels;
    }

    public static LabelData findProductLabelByTraceCode(String traceCode) {
        for (ProductLabelData product : getProductLabels()) {
            if (traceCode.equalsIgnoreCase(product.traceCode)) {
                return toProductLabel(product);
            }
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

    public static List<GreenhouseLabelData> getGreenhousesByPlantationName(String plantationName) {
        List<GreenhouseLabelData> greenhouses = new ArrayList<>();
        if ("东山一号种植园".equals(plantationName)) {
            greenhouses.add(new GreenhouseLabelData("东山-连栋1号棚", "GH-201", "东山一号种植园", "2.1亩", "正常运营", "陈大海", "GH-EX-A01"));
            greenhouses.add(new GreenhouseLabelData("东山-连栋2号棚", "GH-202", "东山一号种植园", "1.8亩", "正常运营", "陈大海", "GH-EX-A02"));
            greenhouses.add(new GreenhouseLabelData("东山-拱棚3号棚", "GH-203", "东山一号种植园", "1.6亩", "待巡检", "陈大海", "GH-EX-A03"));
            return greenhouses;
        }
        if ("南岭智慧种植园".equals(plantationName)) {
            greenhouses.add(new GreenhouseLabelData("南岭-智能1号棚", "GH-311", "南岭智慧种植园", "1.9亩", "正常运营", "李芳", "GH-EX-B01"));
            greenhouses.add(new GreenhouseLabelData("南岭-智能2号棚", "GH-312", "南岭智慧种植园", "2.2亩", "正常运营", "李芳", "GH-EX-B02"));
            greenhouses.add(new GreenhouseLabelData("南岭-保育3号棚", "GH-313", "南岭智慧种植园", "1.5亩", "待巡检", "李芳", "GH-EX-B03"));
            return greenhouses;
        }
        if ("西湾生态种植园".equals(plantationName)) {
            greenhouses.add(new GreenhouseLabelData("西湾-生态1号棚", "GH-321", "西湾生态种植园", "2.4亩", "生长良好", "王志强", "GH-EX-C01"));
            greenhouses.add(new GreenhouseLabelData("西湾-生态2号棚", "GH-322", "西湾生态种植园", "2.0亩", "生长良好", "王志强", "GH-EX-C02"));
            greenhouses.add(new GreenhouseLabelData("西湾-生态3号棚", "GH-323", "西湾生态种植园", "1.7亩", "待巡检", "王志强", "GH-EX-C03"));
        }
        return greenhouses;
    }

    public static List<GreenhouseLabelData> getAllGreenhouses() {
        List<GreenhouseLabelData> all = new ArrayList<>();
        all.addAll(getGreenhousesByPlantationName("东山一号种植园"));
        all.addAll(getGreenhousesByPlantationName("南岭智慧种植园"));
        all.addAll(getGreenhousesByPlantationName("西湾生态种植园"));
        return all;
    }

    public static LabelData toGreenhouseLabel(String plantationName, GreenhouseLabelData greenhouse) {
        return new LabelData(
                LabelTemplates.TEMP_DP,
                "",
                "",
                greenhouse.selfCode,
                plantationName,
                greenhouse.area,
                greenhouse.owner,
                "",
                "",
                greenhouse.traceCode
        );
    }

    public static List<SeedbedLabelData> getSeedbedsByGreenhouseSelfCode(String greenhouseSelfCode) {
        List<SeedbedLabelData> seedbeds = new ArrayList<>();
        if ("GH-201".equals(greenhouseSelfCode)) {
            seedbeds.add(new SeedbedLabelData("东山1号棚-A苗床", "MC-201-A", "MC-EX-201A"));
            seedbeds.add(new SeedbedLabelData("东山1号棚-B苗床", "MC-201-B", "MC-EX-201B"));
            seedbeds.add(new SeedbedLabelData("东山1号棚-C苗床", "MC-201-C", "MC-EX-201C"));
            return seedbeds;
        }
        if ("GH-202".equals(greenhouseSelfCode)) {
            seedbeds.add(new SeedbedLabelData("东山2号棚-A苗床", "MC-202-A", "MC-EX-202A"));
            seedbeds.add(new SeedbedLabelData("东山2号棚-B苗床", "MC-202-B", "MC-EX-202B"));
            seedbeds.add(new SeedbedLabelData("东山2号棚-C苗床", "MC-202-C", "MC-EX-202C"));
            return seedbeds;
        }
        if ("GH-203".equals(greenhouseSelfCode)) {
            seedbeds.add(new SeedbedLabelData("东山3号棚-A苗床", "MC-203-A", "MC-EX-203A"));
            seedbeds.add(new SeedbedLabelData("东山3号棚-B苗床", "MC-203-B", "MC-EX-203B"));
            return seedbeds;
        }
        if ("GH-311".equals(greenhouseSelfCode)) {
            seedbeds.add(new SeedbedLabelData("南岭1号棚-A苗床", "MC-311-A", "MC-EX-311A"));
            seedbeds.add(new SeedbedLabelData("南岭1号棚-B苗床", "MC-311-B", "MC-EX-311B"));
            return seedbeds;
        }
        if ("GH-312".equals(greenhouseSelfCode)) {
            seedbeds.add(new SeedbedLabelData("南岭2号棚-A苗床", "MC-312-A", "MC-EX-312A"));
            seedbeds.add(new SeedbedLabelData("南岭2号棚-B苗床", "MC-312-B", "MC-EX-312B"));
            seedbeds.add(new SeedbedLabelData("南岭2号棚-C苗床", "MC-312-C", "MC-EX-312C"));
            return seedbeds;
        }
        if ("GH-313".equals(greenhouseSelfCode)) {
            seedbeds.add(new SeedbedLabelData("南岭3号棚-A苗床", "MC-313-A", "MC-EX-313A"));
            return seedbeds;
        }
        if ("GH-321".equals(greenhouseSelfCode)) {
            seedbeds.add(new SeedbedLabelData("西湾1号棚-A苗床", "MC-321-A", "MC-EX-321A"));
            seedbeds.add(new SeedbedLabelData("西湾1号棚-B苗床", "MC-321-B", "MC-EX-321B"));
            return seedbeds;
        }
        if ("GH-322".equals(greenhouseSelfCode)) {
            seedbeds.add(new SeedbedLabelData("西湾2号棚-A苗床", "MC-322-A", "MC-EX-322A"));
            seedbeds.add(new SeedbedLabelData("西湾2号棚-B苗床", "MC-322-B", "MC-EX-322B"));
            return seedbeds;
        }
        if ("GH-323".equals(greenhouseSelfCode)) {
            seedbeds.add(new SeedbedLabelData("西湾3号棚-A苗床", "MC-323-A", "MC-EX-323A"));
            seedbeds.add(new SeedbedLabelData("西湾3号棚-B苗床", "MC-323-B", "MC-EX-323B"));
            return seedbeds;
        }
        return seedbeds;
    }

    public static LabelData toSeedbedLabel(GreenhouseLabelData greenhouse, SeedbedLabelData seedbed) {
        return new LabelData(
                LabelTemplates.TEMP_MC,
                "",
                "",
                seedbed.selfCode,
                greenhouse.selfCode,
                greenhouse.plantationName,
                greenhouse.owner,
                "",
                "",
                seedbed.traceCode
        );
    }

    public static LabelData findSeedbedLabelBySelfCode(List<GreenhouseLabelData> greenhouses, String selfCode) {
        for (GreenhouseLabelData greenhouse : greenhouses) {
            List<SeedbedLabelData> seedbeds = getSeedbedsByGreenhouseSelfCode(greenhouse.selfCode);
            for (SeedbedLabelData seedbed : seedbeds) {
                if (selfCode.equalsIgnoreCase(seedbed.selfCode)) {
                    return toSeedbedLabel(greenhouse, seedbed);
                }
            }
        }
        return null;
    }

    public static LabelData findGreenhouseLabelBySelfCode(List<PlantationData> plantations, String selfCode) {
        for (PlantationData plantation : plantations) {
            List<GreenhouseLabelData> greenhouses = getGreenhousesByPlantationName(plantation.name);
            for (GreenhouseLabelData greenhouse : greenhouses) {
                if (selfCode.equalsIgnoreCase(greenhouse.selfCode)) {
                    return toGreenhouseLabel(plantation.name, greenhouse);
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
