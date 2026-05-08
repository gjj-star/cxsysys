package com.example.printerfeature.data;

import com.example.printerfeature.model.LabelData;
import com.google.gson.annotations.SerializedName;

public final class PrinterApiModels {
    private PrinterApiModels() {}

    public static class ApiResponse<T> {
        public int code;
        public String message;
        public T data;
        public Boolean hasMore;

        public boolean isSuccessful() {
            return code == 0 || code == 200;
        }
    }

    public static class PlantationOption {
        @SerializedName("plantation_id")
        public int plantationId;
        @SerializedName("plantation_name")
        public String plantationName;
        @SerializedName("plantation_code")
        public String plantationCode;

        public String displayName() {
            return value(plantationName) + "（" + value(plantationCode) + "）";
        }
    }

    public static class FieldOption {
        @SerializedName("field_id")
        public int fieldId;
        @SerializedName("field_code")
        public String fieldCode;
        @SerializedName("field_qrcode")
        public String fieldQrcode;
        @SerializedName("soil_type")
        public String soilType;
        @SerializedName("field_area")
        public String fieldArea;
        @SerializedName("field_status")
        public String fieldStatus;
    }

    public static class GreenhouseOption {
        @SerializedName(value = "greenhouse_id", alternate = {"id"})
        public int greenhouseId;
        @SerializedName(value = "greenhouse_name", alternate = {"name"})
        public String greenhouseName;
        @SerializedName("greenhouse_code")
        public String greenhouseCode;

        public String displayCode() {
            if (greenhouseCode != null && !greenhouseCode.trim().isEmpty()) return greenhouseCode.trim();
            return value(greenhouseName);
        }
    }

    public static class FieldPrintData {
        @SerializedName("field_id")
        public int fieldId;
        @SerializedName("field_code")
        public String fieldCode;
        @SerializedName("plantation_name")
        public String plantationName;
        @SerializedName("plantation_code")
        public String plantationCode;
        @SerializedName("field_length")
        public String fieldLength;
        @SerializedName("field_width")
        public String fieldWidth;
        @SerializedName("field_area")
        public String fieldArea;
        @SerializedName("manager_name")
        public String managerName;
        @SerializedName("field_qrcode")
        public String fieldQrcode;

        public LabelData toLabelData() {
            return new LabelData(
                    LabelTemplates.TEMP_DK,
                    "",
                    "",
                    value(fieldCode),
                    value(plantationName),
                    value(fieldLength) + " × " + value(fieldWidth),
                    value(fieldArea),
                    value(managerName),
                    "",
                    value(fieldQrcode)
            );
        }
    }

    public static class GreenhousePrintData {
        @SerializedName("greenhouse_id")
        public int greenhouseId;
        @SerializedName("greenhouse_code")
        public String greenhouseCode;
        @SerializedName("plantation_name")
        public String plantationName;
        @SerializedName("plantation_code")
        public String plantationCode;
        @SerializedName("greenhouse_area")
        public String greenhouseArea;
        @SerializedName("manager_name")
        public String managerName;
        @SerializedName("greenhouse_qrcode")
        public String greenhouseQrcode;

        public LabelData toLabelData() {
            return new LabelData(
                    LabelTemplates.TEMP_DP,
                    "",
                    "",
                    value(greenhouseCode),
                    value(plantationName),
                    value(greenhouseArea),
                    value(managerName),
                    "",
                    "",
                    value(greenhouseQrcode)
            );
        }
    }

    public static class SeedbedPrintData {
        @SerializedName("seedbed_id")
        public int seedbedId;
        @SerializedName("seedbed_code")
        public String seedbedCode;
        @SerializedName("greenhouse_code")
        public String greenhouseCode;
        @SerializedName("plantation_name")
        public String plantationName;
        @SerializedName("plantation_code")
        public String plantationCode;
        @SerializedName("manager_name")
        public String managerName;
        @SerializedName("seedbed_qrcode")
        public String seedbedQrcode;

        public LabelData toLabelData() {
            return new LabelData(
                    LabelTemplates.TEMP_MC,
                    "",
                    "",
                    value(seedbedCode),
                    value(greenhouseCode),
                    value(plantationName),
                    value(managerName),
                    "",
                    "",
                    value(seedbedQrcode)
            );
        }
    }

    public static class PlantPrintData {
        @SerializedName("plant_id")
        public int plantId;
        @SerializedName("subspecies_name")
        public String subspeciesName;
        public String generation;
        @SerializedName("plant_method")
        public String plantMethod;
        @SerializedName("field_code")
        public String fieldCode;
        @SerializedName("mothertree_qrcode")
        public String mothertreeQrcode;
        @SerializedName("plant_date")
        public String plantDate;
        @SerializedName("plant_qrcode")
        public String plantQrcode;

        public LabelData toLabelData() {
            return new LabelData(
                    LabelTemplates.TEMP_MM,
                    "",
                    "",
                    value(subspeciesName),
                    value(generation),
                    value(plantMethod),
                    value(fieldCode),
                    value(mothertreeQrcode),
                    value(plantDate),
                    value(plantQrcode)
            );
        }
    }

    private static String value(String text) {
        return text == null ? "" : text;
    }
}
