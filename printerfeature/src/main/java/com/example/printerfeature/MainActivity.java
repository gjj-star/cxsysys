//优化：不再负责具体打印指令，只管理UI框架、页面切换、权限等
//2026.5.1 删除加工二维码（临时数据名称为TEMP_CJG）和产成品二维码（TEMP_CP）
package com.example.printerfeature;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.example.printerfeature.data.LabelTemplates;
import com.example.printerfeature.data.MockLabelRepository;
import com.example.printerfeature.data.PrinterApiClient;
import com.example.printerfeature.data.PrinterApiModels;
import com.example.printerfeature.data.PrinterApiService;
import com.example.printerfeature.model.FieldLabelData;
import com.example.printerfeature.model.GreenhouseLabelData;
import com.example.printerfeature.model.LabelData;
import com.example.printerfeature.model.PlantBlockData;
import com.example.printerfeature.model.PlantData;
import com.example.printerfeature.model.PlantationData;
import com.example.printerfeature.model.SeedbedLabelData;
import com.example.printerfeature.model.TemplateExampleData;
import com.example.printerfeature.printing.LabelPrintManager;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends ComponentActivity {

    public static final String TEMP_MM = LabelTemplates.TEMP_MM;
//    public static final String TEMP_CJG = LabelTemplates.TEMP_CJG;
//    public static final String TEMP_CP = LabelTemplates.TEMP_CP;
    public static final String TEMP_DP = LabelTemplates.TEMP_DP;
    public static final String TEMP_MC = LabelTemplates.TEMP_MC;
    public static final String TEMP_DK = LabelTemplates.TEMP_DK;
    private static final String EXTRA_PRINT_SOURCE = "print_source";
    private static final String PRINT_SOURCE_PLANTING_ENTRY = "planting_entry";
    private static final String EXTRA_ENTRY_FIELD_CODE = "entry_field_code";
    private static final String EXTRA_ENTRY_PLANTING_DATE = "entry_planting_date";
    private static final String EXTRA_ENTRY_RECORD_TIME = "entry_record_time";
    private static final String EXTRA_ENTRY_PLANT_COUNT = "entry_plant_count";
    private static final String EXTRA_ENTRY_SUBSPECIES = "entry_subspecies";
    private static final String EXTRA_ENTRY_GENERATION = "entry_generation";
    private static final String EXTRA_ENTRY_GENERATION_WAY = "entry_generation_way";
    private static final String EXTRA_ENTRY_MOTHER_TREE_SELF_CODE = "entry_mother_tree_self_code";
    private static final String MSG_CONNECT_PRINTER = "请连接打印机";

    private TextView tvStatus;
    private TextView tvDataCount;
    private TextView tvToolbarTitle;
    private TextView tvBatchFilterTitle;
    private TextView tvPlantBlockName;
    private TextView tvPlantBlockCode;
    private TextView tvPlantBlockLocation;
    private TextView tvPlantBlockStatus;
    private TextView tvPlantBlockOwner;
    private TextView tvPlantCountLabel;
    private TextView tvPlantCount;

    private EditText etF1;
    private EditText etF2;
    private EditText etF3;
    private EditText etF4;
    private EditText etF5;
    private EditText etF6;
    private EditText etProcessName;
    private EditText etTraceCode;
    private EditText etSpec;
    private EditText etNum;
    private EditText etModel;
    private EditText etWeight;
    private EditText etPlantDate;

    private TextInputLayout tilF1;
    private TextInputLayout tilF2;
    private TextInputLayout tilF3;
    private TextInputLayout tilF4;
    private TextInputLayout tilF5;
    private TextInputLayout tilF6;
    private TextInputLayout tilProcessName;
    private TextInputLayout tilModel;
    private TextInputLayout tilSpec;
    private TextInputLayout tilNum;
    private TextInputLayout tilWeight;
    private TextInputLayout tilProcessingType;
    private TextInputLayout tilPlantBlock;
    private TextInputLayout tilPlantDate;

    private LinearLayout layoutModelSpec;
    private LinearLayout layoutNumWeight;
    private LinearLayout layoutPlantSummary;
    private LinearLayout layoutQrPreviewList;
    private TextView tvQrPreviewMore;

    private AutoCompleteTextView spinnerTemplate;
    private AutoCompleteTextView spinnerProcessingType;
    private AutoCompleteTextView spinnerPlantBlock;

    private Button btnPrint;
    private Button btnExample;
    private Button btnResetPlantFilters;

    private View cardManualForm;
    private View cardPlantBatch;

    private ProgressDialog loadingDialog;
    private final LabelPrintManager printManager = new LabelPrintManager();
    private PrinterApiService printerApiService;

    private final List<LabelData> dataList = new ArrayList<>();
    private List<PlantBlockData> plantBlocks = new ArrayList<>();
    private List<PlantationData> plantations = new ArrayList<>();
    private List<GreenhouseLabelData> greenhouses = new ArrayList<>();
    private boolean plantBlocksLoaded = false;
    private boolean plantationsLoaded = false;
    private boolean greenhousesLoaded = false;
    private boolean plantBlocksLoading = false;
    private boolean plantationsLoading = false;
    private boolean greenhousesLoading = false;
    private PlantBlockData selectedPlantBlock;
    private PlantationData selectedPlantation;
    private GreenhouseLabelData selectedGreenhouse;
    private String selectedPlantDate = "";
    private String selectedProcessTypeKey = "";
    private String selectedProcessDate = "";
    private String selectedProductDate = "";
    private boolean isPlantingEntryPrintMode = false;
    private String plantingEntryFieldCode = "";
    private String plantingEntryDate = "";
    private String plantingEntryRecordTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupStatusBar();
        setContentView(R.layout.activity_main);
        printerApiService = PrinterApiClient.service(getApplicationContext());

        bindViews();
        setupStaticInputs();
        setupTemplateSelectors();
        setupActions();

        String targetTemplate = getIntent().getStringExtra("target_template");
        if (targetTemplate == null) targetTemplate = TEMP_MM;

        spinnerTemplate.setText(targetTemplate, false);
        updateUIByTemplate(targetTemplate);
        consumePlantingEntryPrintIntentIfNeeded();
        tvToolbarTitle.setText(targetTemplate + "打印");
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPrinterStatus();
    }

    private void setupStatusBar() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(Color.WHITE);
    }

    private void bindViews() {
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvStatus = findViewById(R.id.tvStatus);
        tvDataCount = findViewById(R.id.tvDataCount);
        tvBatchFilterTitle = findViewById(R.id.tvBatchFilterTitle);
        tvPlantBlockName = findViewById(R.id.tvPlantBlockName);
        tvPlantBlockCode = findViewById(R.id.tvPlantBlockCode);
        tvPlantBlockLocation = findViewById(R.id.tvPlantBlockLocation);
        tvPlantBlockStatus = findViewById(R.id.tvPlantBlockStatus);
        tvPlantBlockOwner = findViewById(R.id.tvPlantBlockOwner);
        tvPlantCountLabel = findViewById(R.id.tvPlantCountLabel);
        tvPlantCount = findViewById(R.id.tvPlantCount);

        etProcessName = findViewById(R.id.etProcessName);
        etF1 = findViewById(R.id.etF1);
        etF2 = findViewById(R.id.etF2);
        etF3 = findViewById(R.id.etF3);
        etF4 = findViewById(R.id.etF4);
        etF5 = findViewById(R.id.etF5);
        etF6 = findViewById(R.id.etF6);
        etTraceCode = findViewById(R.id.etTraceCode);
        etModel = findViewById(R.id.etModel);
        etSpec = findViewById(R.id.etSpec);
        etNum = findViewById(R.id.etNum);
        etWeight = findViewById(R.id.etWeight);
        etPlantDate = findViewById(R.id.etPlantDate);

        tilProcessingType = findViewById(R.id.tilProcessingType);
        tilProcessName = findViewById(R.id.tilProcessName);
        tilF1 = findViewById(R.id.tilF1);
        tilF2 = findViewById(R.id.tilF2);
        tilF3 = findViewById(R.id.tilF3);
        tilF4 = findViewById(R.id.tilF4);
        tilF5 = findViewById(R.id.tilF5);
        tilF6 = findViewById(R.id.tilF6);
        tilModel = findViewById(R.id.tilModel);
        tilSpec = findViewById(R.id.tilSpec);
        tilNum = findViewById(R.id.tilNum);
        tilWeight = findViewById(R.id.tilWeight);
        tilPlantBlock = findViewById(R.id.tilPlantBlock);
        tilPlantDate = findViewById(R.id.tilPlantDate);

        layoutModelSpec = findViewById(R.id.layoutModelSpec);
        layoutNumWeight = findViewById(R.id.layoutNumWeight);
        layoutPlantSummary = findViewById(R.id.layoutPlantSummary);
        layoutQrPreviewList = findViewById(R.id.layoutQrPreviewList);
        tvQrPreviewMore = findViewById(R.id.tvQrPreviewMore);

        spinnerTemplate = findViewById(R.id.spinnerTemplate);
        spinnerProcessingType = findViewById(R.id.spinnerProcessingType);
        spinnerPlantBlock = findViewById(R.id.spinnerPlantBlock);

        btnExample = findViewById(R.id.btnExample);
        btnPrint = findViewById(R.id.btnPrint);
        btnResetPlantFilters = findViewById(R.id.btnResetPlantFilters);

        cardManualForm = findViewById(R.id.cardManualForm);
        cardPlantBatch = findViewById(R.id.cardPlantBatch);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupStaticInputs() {
        ArrayAdapter<String> templateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, LabelTemplates.allTemplates());
        spinnerTemplate.setAdapter(templateAdapter);

        ArrayAdapter<String> processingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, LabelTemplates.processingTypes());
        spinnerProcessingType.setAdapter(processingAdapter);
        spinnerProcessingType.setText(LabelTemplates.TYPE_INITIAL, false);

        etF6.setFocusable(false);
        etF6.setOnClickListener(v -> showDatePicker(etF6));

        etPlantDate.setInputType(InputType.TYPE_NULL);
        etPlantDate.setOnClickListener(v -> {
            if (TEMP_MM.equals(currentTemplate())) {
                showPlantDatePicker();
            }
//            } else if (TEMP_CJG.equals(currentTemplate())) {
//                showProcessDatePicker();
//            } else if (TEMP_CP.equals(currentTemplate())) {
//                showProductDatePicker();
//            }
        });

        plantBlocks = new ArrayList<>();
        plantations = new ArrayList<>();
        greenhouses = new ArrayList<>();
    }

    private void setupTemplateSelectors() {
        spinnerTemplate.setOnItemClickListener((parent, view, position, id) -> {
            String template = LabelTemplates.allTemplates()[position];
            updateUIByTemplate(template);
            tvToolbarTitle.setText(template + "打印");
        });
    }

    private void ensurePlantationsLoaded() {
        if (plantationsLoaded) {
            setupPlantationSelector();
            return;
        }
        if (plantationsLoading) return;
        loadPlantations();
    }

    private void ensureFieldsLoaded() {
        if (plantBlocksLoaded) {
            setupPlantBlockSelector();
            return;
        }
        if (plantBlocksLoading) return;
        loadFields();
    }

    private void ensureGreenhousesLoaded() {
        if (greenhousesLoaded) {
            setupGreenhouseSelector();
            return;
        }
        if (greenhousesLoading) return;
        loadGreenhouses();
    }

    private void loadPlantations() {
        plantationsLoading = true;
        printerApiService.getPlantations().enqueue(new Callback<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantationOption>>>() {
            @Override
            public void onResponse(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantationOption>>> call,
                                   Response<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantationOption>>> response) {
                plantationsLoading = false;
                PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantationOption>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccessful() || body.data == null) {
                    toastApiError("种植园列表", body);
                    return;
                }
                List<PlantationData> next = new ArrayList<>();
                for (PrinterApiModels.PlantationOption item : body.data) {
                    next.add(new PlantationData(
                            item.plantationId,
                            safe(item.plantationName, ""),
                            safe(item.plantationCode, ""),
                            "",
                            "",
                            "",
                            new ArrayList<>()
                    ));
                }
                plantations = next;
                plantationsLoaded = true;
                if (TEMP_DK.equals(currentTemplate()) || TEMP_DP.equals(currentTemplate())) {
                    setupPlantationSelector();
                }
            }

            @Override
            public void onFailure(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantationOption>>> call, Throwable t) {
                plantationsLoading = false;
                toast("种植园列表加载失败：" + t.getMessage());
            }
        });
    }

    private void loadFields() {
        plantBlocksLoading = true;
        printerApiService.getFields().enqueue(new Callback<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldOption>>>() {
            @Override
            public void onResponse(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldOption>>> call,
                                   Response<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldOption>>> response) {
                plantBlocksLoading = false;
                PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldOption>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccessful() || body.data == null) {
                    toastApiError("地块列表", body);
                    return;
                }
                List<PlantBlockData> next = new ArrayList<>();
                for (PrinterApiModels.FieldOption item : body.data) {
                    next.add(new PlantBlockData(
                            item.fieldId,
                            safe(item.fieldCode, ""),
                            safe(item.fieldCode, ""),
                            buildFieldLocation(item),
                            safe(item.fieldStatus, ""),
                            new ArrayList<>()
                    ));
                }
                plantBlocks = next;
                plantBlocksLoaded = true;
                if (TEMP_MM.equals(currentTemplate())) {
                    setupPlantBlockSelector();
                }
            }

            @Override
            public void onFailure(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldOption>>> call, Throwable t) {
                plantBlocksLoading = false;
                toast("地块列表加载失败：" + t.getMessage());
            }
        });
    }

    private void loadGreenhouses() {
        greenhousesLoading = true;
        printerApiService.getGreenhouses().enqueue(new Callback<PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhouseOption>>>() {
            @Override
            public void onResponse(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhouseOption>>> call,
                                   Response<PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhouseOption>>> response) {
                greenhousesLoading = false;
                PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhouseOption>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccessful() || body.data == null) {
                    toastApiError("大棚列表", body);
                    return;
                }
                List<GreenhouseLabelData> next = new ArrayList<>();
                for (PrinterApiModels.GreenhouseOption item : body.data) {
                    String code = item.displayCode();
                    next.add(new GreenhouseLabelData(
                            item.greenhouseId,
                            code,
                            code,
                            "",
                            "",
                            "",
                            "",
                            ""
                    ));
                }
                greenhouses = next;
                greenhousesLoaded = true;
                if (TEMP_MC.equals(currentTemplate())) {
                    setupGreenhouseSelector();
                }
            }

            @Override
            public void onFailure(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhouseOption>>> call, Throwable t) {
                greenhousesLoading = false;
                toast("大棚列表加载失败：" + t.getMessage());
            }
        });
    }

    private String buildFieldLocation(PrinterApiModels.FieldOption item) {
        List<String> parts = new ArrayList<>();
        if (item.soilType != null && !item.soilType.trim().isEmpty()) {
            parts.add("土壤：" + item.soilType.trim());
        }
        if (item.fieldArea != null && !item.fieldArea.trim().isEmpty()) {
            parts.add("面积：" + item.fieldArea.trim());
        }
        if (parts.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) builder.append("，");
            builder.append(parts.get(i));
        }
        return builder.toString();
    }

    private void toastApiError(String name, PrinterApiModels.ApiResponse<?> body) {
        String message = body == null || body.message == null || body.message.trim().isEmpty()
                ? "接口返回异常"
                : body.message;
        toast(name + "加载失败：" + message);
    }

    private void showPrintDataErrorDialog(PrinterApiModels.ApiResponse<?> body, Response<?> response) {
        String message;
        if (body != null && body.message != null && !body.message.trim().isEmpty()) {
            message = body.message.trim();
        } else if (response != null && !response.message().trim().isEmpty()) {
            message = response.message().trim();
        } else {
            message = "接口返回异常";
        }
        new AlertDialog.Builder(this)
                .setTitle("获取打印数据失败")
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }

    private void setupActions() {
        Button btnConnect = findViewById(R.id.btnConnect);
        Button btnAddData = findViewById(R.id.btnAddData);
        Button btnClearData = findViewById(R.id.btnClearData);

        btnConnect.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DeviceListActivity.class)));
        btnAddData.setOnClickListener(v -> addData());
        btnClearData.setOnClickListener(v -> clearCurrentData());
        btnResetPlantFilters.setOnClickListener(v -> {
            if (TEMP_DK.equals(currentTemplate())) {
                resetFieldFilters();
            } else if (TEMP_DP.equals(currentTemplate())) {
                resetGreenhouseFilters();
            } else if (TEMP_MC.equals(currentTemplate())) {
                resetSeedbedFilters();
//            } else if (TEMP_CJG.equals(currentTemplate())) {
//                resetProcessFilters();
//            } else if (TEMP_CP.equals(currentTemplate())) {
//                resetProductFilters();
            } else {
                resetPlantFilters();
            }
        });
        btnPrint.setOnClickListener(v -> printCurrentData());
        btnExample.setOnClickListener(v -> onExampleAction());
    }

    private void consumePlantingEntryPrintIntentIfNeeded() {
        Intent intent = getIntent();
        if (intent == null) return;
        if (!PRINT_SOURCE_PLANTING_ENTRY.equals(intent.getStringExtra(EXTRA_PRINT_SOURCE))) return;
        if (!TEMP_MM.equals(currentTemplate())) return;

        int plantCount = intent.getIntExtra(EXTRA_ENTRY_PLANT_COUNT, 0);
        if (plantCount <= 0) {
            Toast.makeText(this, "录入批次数据无效，请重新提交", Toast.LENGTH_SHORT).show();
            return;
        }

        String fieldCode = safe(intent.getStringExtra(EXTRA_ENTRY_FIELD_CODE), "");
        String plantingDate = safe(intent.getStringExtra(EXTRA_ENTRY_PLANTING_DATE), "");
        String entryRecordTime = safe(intent.getStringExtra(EXTRA_ENTRY_RECORD_TIME), "");
        if (fieldCode.isEmpty() || plantingDate.isEmpty()) {
            Toast.makeText(this, "录入批次缺少地块编码或定植日期，无法获取打印数据", Toast.LENGTH_SHORT).show();
            return;
        }

        isPlantingEntryPrintMode = true;
        plantingEntryFieldCode = fieldCode;
        plantingEntryDate = plantingDate;
        plantingEntryRecordTime = entryRecordTime;
        selectedPlantBlock = null;
        selectedPlantDate = "";
        tvBatchFilterTitle.setText("本次录入批次打印");
        tilPlantBlock.setVisibility(View.GONE);
        tilPlantDate.setVisibility(View.GONE);
        btnResetPlantFilters.setVisibility(View.GONE);

        tvPlantBlockCode.setText("定植地块：" + fieldCode);
        tvPlantBlockLocation.setText("录入时间：" + (entryRecordTime.isEmpty() ? "未填写" : entryRecordTime));

        dataList.clear();
        updateDataUI();
        tvDataCount.setText("正在加载本次录入的苗木标签...");
        loadPlantingEntryPrintLabels(fieldCode, plantingDate, plantCount);
    }

    private void loadPlantingEntryPrintLabels(String fieldCode, String plantingDate, int plantCount) {
        showLoading("正在匹配地块信息...");
        printerApiService.getFields().enqueue(new Callback<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldOption>>>() {
            @Override
            public void onResponse(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldOption>>> call,
                                   Response<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldOption>>> response) {
                PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldOption>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccessful() || body.data == null) {
                    hideLoading();
                    showPrintDataErrorDialog(body, response);
                    tvDataCount.setText("地块信息加载失败，请稍后重试");
                    return;
                }

                PrinterApiModels.FieldOption matchedField = findFieldOptionByCode(body.data, fieldCode);
                if (matchedField == null || matchedField.fieldId <= 0) {
                    hideLoading();
                    tvDataCount.setText("未找到地块“" + fieldCode + "”，无法获取本次录入标签");
                    Toast.makeText(MainActivity.this, "未在地块列表中找到对应地块", Toast.LENGTH_SHORT).show();
                    return;
                }

                selectedPlantBlock = new PlantBlockData(
                        matchedField.fieldId,
                        safe(matchedField.fieldCode, fieldCode),
                        safe(matchedField.fieldCode, fieldCode),
                        buildFieldLocation(matchedField),
                        safe(matchedField.fieldStatus, ""),
                        new ArrayList<>()
                );
                loadPlantingEntryPlantPrintData(matchedField.fieldId, plantingDate, plantCount);
            }

            @Override
            public void onFailure(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldOption>>> call, Throwable t) {
                hideLoading();
                tvDataCount.setText("地块信息加载失败，请稍后重试");
                toast("地块列表加载失败：" + t.getMessage());
            }
        });
    }

    private void loadPlantingEntryPlantPrintData(int fieldId, String plantingDate, int plantCount) {
        showLoading("正在加载本次录入的苗木标签...");
        printerApiService.getPlantPrintData(fieldId, plantingDate)
                .enqueue(new Callback<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>>>() {
                    @Override
                    public void onResponse(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>>> call,
                                           Response<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>>> response) {
                        hideLoading();
                        PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccessful() || body.data == null) {
                            showPrintDataErrorDialog(body, response);
                            updateDataUI();
                            return;
                        }

                        List<PrinterApiModels.PlantPrintData> latestItems = latestPlantPrintData(body.data, plantCount);
                        dataList.clear();
                        for (PrinterApiModels.PlantPrintData item : latestItems) {
                            dataList.add(item.toLabelData());
                        }
                        updateDataUI();
                        if (dataList.size() < plantCount) {
                            Toast.makeText(MainActivity.this, "接口仅返回 " + dataList.size() + " 条可打印苗木标签", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "已加载本次录入的 " + dataList.size() + " 株苗木标签", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>>> call, Throwable t) {
                        hideLoading();
                        updateDataUI();
                        toast("苗木标签数据加载失败：" + t.getMessage());
                    }
                });
    }

    private PrinterApiModels.FieldOption findFieldOptionByCode(List<PrinterApiModels.FieldOption> fields, String fieldCode) {
        String target = normalizeCode(fieldCode);
        for (PrinterApiModels.FieldOption field : fields) {
            if (target.equals(normalizeCode(field.fieldCode)) || target.equals(normalizeCode(field.fieldQrcode))) {
                return field;
            }
        }
        return null;
    }

    private List<PrinterApiModels.PlantPrintData> latestPlantPrintData(List<PrinterApiModels.PlantPrintData> source, int plantCount) {
        List<PrinterApiModels.PlantPrintData> sorted = new ArrayList<>(source);
        Collections.sort(sorted, new Comparator<PrinterApiModels.PlantPrintData>() {
            @Override
            public int compare(PrinterApiModels.PlantPrintData left, PrinterApiModels.PlantPrintData right) {
                return Integer.compare(right.plantId, left.plantId);
            }
        });
        if (sorted.size() > plantCount) {
            sorted = new ArrayList<>(sorted.subList(0, plantCount));
        }
        Collections.sort(sorted, new Comparator<PrinterApiModels.PlantPrintData>() {
            @Override
            public int compare(PrinterApiModels.PlantPrintData left, PrinterApiModels.PlantPrintData right) {
                return Integer.compare(left.plantId, right.plantId);
            }
        });
        return sorted;
    }

    private String normalizeCode(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private LabelData findLoadedLabelByTraceCode(String template, String traceCode) {
        for (LabelData item : dataList) {
            if (template.equals(item.template) && traceCode.equals(item.traceCode)) {
                return item;
            }
        }
        return null;
    }

    private LabelData findLoadedLabelBySelfCode(String template, String selfCode) {
        for (LabelData item : dataList) {
            if (template.equals(item.template) && selfCode.equals(item.f1)) {
                return item;
            }
        }
        return null;
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private boolean isPrinterConnected() {
        return !"未连接".equals(printerSDK.printerName);
    }

    private boolean ensurePrinterConnected() {
        if (isPrinterConnected()) return true;
        toast(MSG_CONNECT_PRINTER);
        return false;
    }

    private void clearCurrentData() {
        dataList.clear();
        updateDataUI();
        Toast.makeText(this, "数据已清空", Toast.LENGTH_SHORT).show();
    }

    private void printCurrentData() {
        if (dataList.isEmpty()) {
            String template = currentTemplate();
            String message;
            if (TEMP_MM.equals(template)) {
                message = "请先选择地块或补打一棵苗木";
            } else if (TEMP_DK.equals(template)) {
                message = "请先选择种植园或补打一块地块";
            } else if (TEMP_DP.equals(template)) {
                message = "请先选择种植园或补打一座大棚";
            } else if (TEMP_MC.equals(template)) {
                message = "请先选择大棚或补打一个苗床";
//            } else if (TEMP_CJG.equals(template)) {
//                message = "请先选择加工类型和完工日期，或补打一个加工标签";
//            } else if (TEMP_CP.equals(template)) {
//                message = "请先选择完工日期或补打一个产成品";
            } else {
                message = "请先录入数据";
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading("正在准备批量打印 " + dataList.size() + " 张...");
        printManager.startPrint(dataList, new LabelPrintManager.PrintJobListener() {
            @Override
            public void onCompleted() {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(MainActivity.this, "全部打印完成", Toast.LENGTH_SHORT).show();
                    if (TEMP_MM.equals(currentTemplate()) && selectedPlantBlock != null) {
                        applyPlantFilters(false);
                    } else if (TEMP_DK.equals(currentTemplate()) && selectedPlantation != null) {
                        applyFieldFilters(false);
                    } else if (TEMP_DP.equals(currentTemplate()) && selectedPlantation != null) {
                        applyGreenhouseFilters(false);
                    } else if (TEMP_MC.equals(currentTemplate()) && selectedGreenhouse != null) {
                        applySeedbedFilters(false);
//                    } else if (TEMP_CJG.equals(currentTemplate()) && !selectedProcessTypeKey.isEmpty() && !selectedProcessDate.isEmpty()) {
//                        applyProcessFilters(false);
//                    } else if (TEMP_CP.equals(currentTemplate()) && !selectedProductDate.isEmpty()) {
//                        applyProductFilters(false);
                    } else {
                        dataList.clear();
                        updateDataUI();
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                runOnUiThread(() -> {
                    hideLoading();
                    if (!isPrinterConnected()) {
                        toast(MSG_CONNECT_PRINTER);
                        return;
                    }
                    toast("打印出错:" + errorCode);
                });
            }
        });
    }

    private void onExampleAction() {
        String template = currentTemplate();
        switch (template) {
            case TEMP_MM:
                if (!ensurePrinterConnected()) return;
                showManualPlantDialog();
                return;
            case TEMP_DK:
                if (!ensurePrinterConnected()) return;
                showManualFieldDialog();
                return;
            case TEMP_DP:
                if (!ensurePrinterConnected()) return;
                showManualGreenhouseDialog();
                return;
            case TEMP_MC:
                if (!ensurePrinterConnected()) return;
                showManualSeedbedDialog();
                return;
//            case TEMP_CJG:
//                if (!ensurePrinterConnected()) return;
//                showManualProcessDialog();
//                return;
//            case TEMP_CP:
//                if (!ensurePrinterConnected()) return;
//                showManualProductDialog();
//                return;
            default:
                fillExampleData();
        }
    }

    private void fillExampleData() {
        clearManualInputs();

        if (TEMP_MM.equals(currentTemplate())) {
            if (!plantBlocks.isEmpty()) {
                configurePlantBatchUI();
                spinnerPlantBlock.setText(plantBlocks.get(0).name, false);
                selectedPlantBlock = plantBlocks.get(0);
                applyPlantFilters(true);
            }
            return;
        }
        if (TEMP_DK.equals(currentTemplate())) {
            if (!plantations.isEmpty()) {
                configureFieldBatchUI();
                spinnerPlantBlock.setText(plantations.get(0).name, false);
                selectedPlantation = plantations.get(0);
                applyFieldFilters(true);
            }
            return;
        }
        if (TEMP_DP.equals(currentTemplate())) {
            if (!plantations.isEmpty()) {
                configureGreenhouseBatchUI();
                spinnerPlantBlock.setText(plantations.get(0).name, false);
                selectedPlantation = plantations.get(0);
                applyGreenhouseFilters(true);
            }
            return;
        }
        if (TEMP_MC.equals(currentTemplate())) {
            if (!greenhouses.isEmpty()) {
                configureSeedbedBatchUI();
                spinnerPlantBlock.setText(greenhouses.get(0).greenhouseName + " (" + greenhouses.get(0).selfCode + ")", false);
                selectedGreenhouse = greenhouses.get(0);
                applySeedbedFilters(true);
            }
            return;
        }
//        if (TEMP_CJG.equals(currentTemplate())) {
//            configureProcessBatchUI();
//            selectedProcessTypeKey = MockLabelRepository.PROCESS_TYPE_MATERIAL;
//            spinnerPlantBlock.setText("初加工 (material)", false);
//            selectedProcessDate = "2024-05-21";
//            etPlantDate.setText(selectedProcessDate);
//            applyProcessFilters(true);
//            return;
//        }
//        if (TEMP_CP.equals(currentTemplate())) {
//            configureProductBatchUI();
//            selectedProductDate = "2024-05-22";
//            etPlantDate.setText(selectedProductDate);
//            applyProductFilters(true);
//            return;
//        }

        TemplateExampleData exampleData = MockLabelRepository.getTemplateExample(currentTemplate());
        if (exampleData == null) return;

        spinnerProcessingType.setText(exampleData.processingType, false);
        etProcessName.setText(exampleData.processName);
        etF1.setText(exampleData.f1);
        etF2.setText(exampleData.f2);
        etF3.setText(exampleData.f3);
        etF4.setText(exampleData.f4);
        etF5.setText(exampleData.f5);
        etF6.setText(exampleData.f6);
        etModel.setText(exampleData.model);
        etSpec.setText(exampleData.spec);
        etNum.setText(exampleData.num);
        etWeight.setText(exampleData.weight);
        etTraceCode.setText(exampleData.traceCode);
        Toast.makeText(this, "已填入示例数据", Toast.LENGTH_SHORT).show();
    }

    private void clearManualInputs() {
        etF1.setText("");
        etF2.setText("");
        etF3.setText("");
        etF4.setText("");
        etF5.setText("");
        etF6.setText("");
        etProcessName.setText("");
        etModel.setText("");
        etSpec.setText("");
        etNum.setText("");
        etWeight.setText("");
        etTraceCode.setText("");
    }

    private void updateUIByTemplate(String template) {
        dataList.clear();
        isPlantingEntryPrintMode = false;
        plantingEntryFieldCode = "";
        plantingEntryDate = "";
        plantingEntryRecordTime = "";
        resetCommonVisibility();

        if (TEMP_MM.equals(template)) {
            cardManualForm.setVisibility(View.GONE);
            cardPlantBatch.setVisibility(View.VISIBLE);
            btnExample.setText("补打单棵");
            configurePlantBatchUI();
            resetPlantFilters();
//        } else if (TEMP_CJG.equals(template)) {
//            cardManualForm.setVisibility(View.GONE);
//            cardPlantBatch.setVisibility(View.VISIBLE);
//            btnExample.setText("补打单条");
//            configureProcessBatchUI();
//            resetProcessFilters();
//        } else if (TEMP_CP.equals(template)) {
//            cardManualForm.setVisibility(View.GONE);
//            cardPlantBatch.setVisibility(View.VISIBLE);
//            btnExample.setText("补打单品");
//            configureProductBatchUI();
//            resetProductFilters();
        } else if (TEMP_DP.equals(template)) {
            cardManualForm.setVisibility(View.GONE);
            cardPlantBatch.setVisibility(View.VISIBLE);
            btnExample.setText("补打单棚");
            configureGreenhouseBatchUI();
            resetGreenhouseFilters();
        } else if (TEMP_MC.equals(template)) {
            cardManualForm.setVisibility(View.GONE);
            cardPlantBatch.setVisibility(View.VISIBLE);
            btnExample.setText("补打单床");
            configureSeedbedBatchUI();
            resetSeedbedFilters();
        } else if (TEMP_DK.equals(template)) {
            cardManualForm.setVisibility(View.GONE);
            cardPlantBatch.setVisibility(View.VISIBLE);
            btnExample.setText("补打单块");
            configureFieldBatchUI();
            resetFieldFilters();
        }

        updateDataUI();
    }

    private void resetCommonVisibility() {
        tilF1.setVisibility(View.VISIBLE);
        tilF2.setVisibility(View.VISIBLE);
        tilF3.setVisibility(View.VISIBLE);
        tilF4.setVisibility(View.VISIBLE);
        tilF5.setVisibility(View.VISIBLE);
        tilF6.setVisibility(View.VISIBLE);
        tilProcessingType.setVisibility(View.GONE);
        tilProcessName.setVisibility(View.GONE);
        layoutModelSpec.setVisibility(View.GONE);
        layoutNumWeight.setVisibility(View.GONE);
        cardManualForm.setVisibility(View.VISIBLE);
        cardPlantBatch.setVisibility(View.GONE);
        btnExample.setVisibility(View.VISIBLE);
        btnExample.setText("示例数据");
        tilPlantBlock.setVisibility(View.VISIBLE);
        layoutPlantSummary.setVisibility(View.VISIBLE);
        tvDataCount.setText("");
    }

/*    private void showProcessingTemplate() {
        tilProcessingType.setVisibility(View.VISIBLE);
        tilProcessName.setVisibility(View.VISIBLE);
        tilF1.setHint("名称");
        tilF2.setVisibility(View.GONE);
        tilF3.setVisibility(View.GONE);
        layoutModelSpec.setVisibility(View.VISIBLE);
        layoutNumWeight.setVisibility(View.VISIBLE);
        tilModel.setHint("型号");
        tilSpec.setHint("规格");
        tilNum.setHint("数量");
        tilWeight.setHint("重量");
        tilF4.setHint("等级");
        tilF5.setHint("完工时间");
        tilF6.setHint("操作员ID");
        etF5.setFocusable(false);
        etF5.setOnClickListener(v -> showDateTimePicker(etF5));
        etF6.setFocusableInTouchMode(true);
        etF6.setOnClickListener(null);
    }*/

    private void showProductTemplate() {
        tilF1.setHint("产成品名称");
        tilF2.setVisibility(View.GONE);
        tilF3.setVisibility(View.GONE);
        layoutModelSpec.setVisibility(View.VISIBLE);
        layoutNumWeight.setVisibility(View.VISIBLE);
        tilModel.setHint("型号");
        tilSpec.setHint("规格");
        tilNum.setHint("数量");
        tilWeight.setHint("重量");
        tilF4.setHint("等级");
        tilF5.setHint("完工时间");
        tilF6.setHint("操作员ID");
        etF5.setFocusable(false);
        etF5.setOnClickListener(v -> showDateTimePicker(etF5));
        etF6.setFocusableInTouchMode(true);
        etF6.setOnClickListener(null);
    }

    private void showGreenhouseTemplate() {
        tilF1.setHint("自编码");
        tilF2.setHint("种植园");
        tilF3.setHint("面积");
        tilF4.setHint("负责人");
        tilF5.setVisibility(View.GONE);
        tilF6.setVisibility(View.GONE);
        etF5.setFocusableInTouchMode(true);
        etF5.setOnClickListener(null);
        etF6.setFocusableInTouchMode(true);
        etF6.setOnClickListener(null);
    }

    private void showSeedbedTemplate() {
        tilF1.setHint("自编码");
        tilF2.setHint("大棚");
        tilF3.setHint("种植园");
        tilF4.setHint("负责人");
        tilF5.setVisibility(View.GONE);
        tilF6.setVisibility(View.GONE);
        etF5.setFocusableInTouchMode(true);
        etF5.setOnClickListener(null);
        etF6.setFocusableInTouchMode(true);
        etF6.setOnClickListener(null);
    }

    private void showFieldTemplate() {
        tilF1.setHint("自编码");
        tilF2.setHint("种植园");
        tilF3.setVisibility(View.GONE);
        layoutModelSpec.setVisibility(View.VISIBLE);
        tilModel.setHint("长");
        tilSpec.setHint("宽");
        tilF4.setHint("面积");
        tilF5.setHint("负责人");
        tilF6.setVisibility(View.GONE);
        etF5.setFocusableInTouchMode(true);
        etF5.setOnClickListener(null);
        etF6.setFocusableInTouchMode(true);
        etF6.setOnClickListener(null);
    }

    private void configurePlantBatchUI() {
        hideBatchDetailRows();
        tvBatchFilterTitle.setText("筛选苗木所在地块");
        tilPlantBlock.setVisibility(View.VISIBLE);
        tilPlantBlock.setHint("选择地块");
        tilPlantDate.setVisibility(View.VISIBLE);
        tilPlantDate.setHint("定植日期");
        layoutPlantSummary.setVisibility(View.VISIBLE);
        tvPlantCountLabel.setText("待打印苗木标签");
        ensureFieldsLoaded();
    }

    private void configureFieldBatchUI() {
        hideBatchDetailRows();
        tvBatchFilterTitle.setText("筛选地块所在种植园");
        tilPlantBlock.setVisibility(View.VISIBLE);
        tilPlantBlock.setHint("选择种植园");
        tilPlantDate.setVisibility(View.GONE);
        layoutPlantSummary.setVisibility(View.VISIBLE);
        tvPlantCountLabel.setText("待打印地块标签");
        ensurePlantationsLoaded();
    }

    private void configureGreenhouseBatchUI() {
        hideBatchDetailRows();
        tvBatchFilterTitle.setText("筛选大棚所在种植园");
        tilPlantBlock.setVisibility(View.VISIBLE);
        tilPlantBlock.setHint("选择种植园");
        tilPlantDate.setVisibility(View.GONE);
        layoutPlantSummary.setVisibility(View.VISIBLE);
        tvPlantCountLabel.setText("待打印大棚标签");
        ensurePlantationsLoaded();
    }

    private void configureSeedbedBatchUI() {
        hideBatchDetailRows();
        tvBatchFilterTitle.setText("筛选苗床所在大棚");
        tilPlantBlock.setVisibility(View.VISIBLE);
        tilPlantBlock.setHint("选择大棚");
        tilPlantDate.setVisibility(View.GONE);
        layoutPlantSummary.setVisibility(View.VISIBLE);
        tvPlantCountLabel.setText("待打印苗床标签");
        ensureGreenhousesLoaded();
    }

/*    private void configureProductBatchUI() {
        showAllSummaryRows();
        tvBatchFilterTitle.setText("筛选产成品完工时间");
        tilPlantBlock.setVisibility(View.GONE);
        tilPlantDate.setVisibility(View.VISIBLE);
        tilPlantDate.setHint("完工日期");
        layoutPlantSummary.setVisibility(View.VISIBLE);
        tvPlantCountLabel.setText("待打印产成品标签");
        tvPlantBlockName.setVisibility(View.GONE);
        tvPlantBlockLocation.setVisibility(View.GONE);
        tvPlantBlockStatus.setVisibility(View.GONE);
        tvPlantBlockOwner.setVisibility(View.GONE);
        updateProductSummaryPlaceholder();
    }

    private void configureProcessBatchUI() {
        showAllSummaryRows();
        tvBatchFilterTitle.setText("筛选加工完工标签");
        tilPlantBlock.setVisibility(View.VISIBLE);
        tilPlantBlock.setHint("选择类型");
        tilPlantDate.setVisibility(View.VISIBLE);
        tilPlantDate.setHint("完工日期");
        layoutPlantSummary.setVisibility(View.VISIBLE);
        tvPlantCountLabel.setText("待打印加工标签");
        tvPlantBlockStatus.setVisibility(View.GONE);
        tvPlantBlockOwner.setVisibility(View.GONE);
        updateProcessSummaryPlaceholder();
        setupProcessTypeSelector();
    }*/

    private void showAllSummaryRows() {
        tvPlantBlockName.setVisibility(View.VISIBLE);
        tvPlantBlockCode.setVisibility(View.VISIBLE);
        tvPlantBlockLocation.setVisibility(View.VISIBLE);
        tvPlantBlockStatus.setVisibility(View.VISIBLE);
        tvPlantBlockOwner.setVisibility(View.VISIBLE);
    }

    private void hideBatchDetailRows() {
        tvPlantBlockName.setVisibility(View.GONE);
        tvPlantBlockCode.setVisibility(View.GONE);
        tvPlantBlockLocation.setVisibility(View.GONE);
        tvPlantBlockStatus.setVisibility(View.GONE);
        tvPlantBlockOwner.setVisibility(View.GONE);
    }

    private void addData() {
        String template = currentTemplate();
        String f1 = etF1.getText().toString();
        String f2 = resolveF2(template);
        String f3 = resolveF3(template);
        String f4 = etF4.getText().toString();
        String f5 = etF5.getText().toString();
        String f6 = etF6.getText().toString();

        if (!validateManualInput(template, f1, f2, f3, f4, f5, f6)) {
            return;
        }

        dataList.add(new LabelData(
                template,
                spinnerProcessingType.getText().toString(),
                etProcessName.getText().toString(),
                f1, f2, f3, f4, f5, f6,
                etTraceCode.getText().toString()
        ));
        updateDataUI();
        Toast.makeText(this, "已录入第 " + dataList.size() + " 组数据", Toast.LENGTH_SHORT).show();
    }

    private String resolveF2(String template) {
        if (TEMP_DK.equals(template)) {
            return etF2.getText().toString();
        }
//        if (TEMP_CP.equals(template) || TEMP_CJG.equals(template)) {
//            return etModel.getText().toString() + " / " + etSpec.getText().toString();
//        }
        return etF2.getText().toString();
    }

    private String resolveF3(String template) {
        if (TEMP_DK.equals(template)) {
            return etModel.getText().toString() + " × " + etSpec.getText().toString();
        }
//        if (TEMP_CP.equals(template) || TEMP_CJG.equals(template)) {
//            return etNum.getText().toString() + " / " + etWeight.getText().toString();
//        }
        return etF3.getText().toString();
    }

    private boolean validateManualInput(String template, String f1, String f2, String f3, String f4, String f5, String f6) {
        if (TEMP_DP.equals(template) || TEMP_MC.equals(template)) {
            if (f1.isEmpty() || f2.isEmpty() || f3.isEmpty() || f4.isEmpty()) {
                Toast.makeText(this, "请填写所有必要字段", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        if (TEMP_DK.equals(template)) {
            if (f1.isEmpty() || f2.isEmpty() || etModel.getText().toString().isEmpty() || etSpec.getText().toString().isEmpty() || f4.isEmpty() || f5.isEmpty()) {
                Toast.makeText(this, "请填写所有必要字段", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        if (f1.isEmpty() || f2.trim().equals("/") || f3.trim().equals("/") || f4.isEmpty() || f5.isEmpty() || f6.isEmpty()) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setupGreenhouseSelector() {
        List<String> names = new ArrayList<>();
        for (GreenhouseLabelData greenhouse : greenhouses) {
            names.add(greenhouse.selfCode);
        }
        ArrayAdapter<String> greenhouseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        spinnerPlantBlock.setAdapter(greenhouseAdapter);
        spinnerPlantBlock.setKeyListener(null);
        spinnerPlantBlock.setFocusable(false);
        spinnerPlantBlock.setCursorVisible(false);
        spinnerPlantBlock.setOnClickListener(v -> spinnerPlantBlock.showDropDown());
        spinnerPlantBlock.setOnItemClickListener((parent, view, position, id) -> {
            selectedGreenhouse = greenhouses.get(position);
            applySeedbedFilters(true);
        });
        layoutPlantSummary.setVisibility(View.VISIBLE);
        updateSeedbedSummaryPlaceholder();
    }

/*    private void setupProcessTypeSelector() {
        List<String> types = new ArrayList<>();
        types.add("初加工 (material)");
        types.add("精加工 (semi_finished)");
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, types);
        spinnerPlantBlock.setAdapter(typeAdapter);
        spinnerPlantBlock.setKeyListener(null);
        spinnerPlantBlock.setFocusable(false);
        spinnerPlantBlock.setCursorVisible(false);
        spinnerPlantBlock.setOnClickListener(v -> spinnerPlantBlock.showDropDown());
        spinnerPlantBlock.setOnItemClickListener((parent, view, position, id) -> {
            selectedProcessTypeKey = position == 0
                    ? MockLabelRepository.PROCESS_TYPE_MATERIAL
                    : MockLabelRepository.PROCESS_TYPE_SEMI_FINISHED;
            if (!selectedProcessDate.isEmpty()) {
                applyProcessFilters(true);
            }
        });
    }*/

    private void setupPlantBlockSelector() {
        List<String> names = new ArrayList<>();
        for (PlantBlockData block : plantBlocks) {
            names.add(block.selfCode);
        }
        ArrayAdapter<String> blockAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        spinnerPlantBlock.setAdapter(blockAdapter);
        spinnerPlantBlock.setKeyListener(null);
        spinnerPlantBlock.setFocusable(false);
        spinnerPlantBlock.setCursorVisible(false);
        spinnerPlantBlock.setOnClickListener(v -> spinnerPlantBlock.showDropDown());
        spinnerPlantBlock.setOnItemClickListener((parent, view, position, id) -> {
            selectedPlantBlock = plantBlocks.get(position);
            applyPlantFilters(true);
        });
        layoutPlantSummary.setVisibility(View.VISIBLE);
        updatePlantSummaryPlaceholder();
    }

    private void setupPlantationSelector() {
        List<String> names = new ArrayList<>();
        for (PlantationData plantation : plantations) {
            names.add(plantation.name + "（" + plantation.selfCode + "）");
        }
        ArrayAdapter<String> plantationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        spinnerPlantBlock.setAdapter(plantationAdapter);
        spinnerPlantBlock.setKeyListener(null);
        spinnerPlantBlock.setFocusable(false);
        spinnerPlantBlock.setCursorVisible(false);
        spinnerPlantBlock.setOnClickListener(v -> spinnerPlantBlock.showDropDown());
        spinnerPlantBlock.setOnItemClickListener((parent, view, position, id) -> {
            selectedPlantation = plantations.get(position);
            if (TEMP_DP.equals(currentTemplate())) {
                applyGreenhouseFilters(true);
            } else {
                applyFieldFilters(true);
            }
        });
        layoutPlantSummary.setVisibility(View.VISIBLE);
        if (TEMP_DP.equals(currentTemplate())) {
            updateGreenhouseSummaryPlaceholder();
        } else {
            updateFieldSummaryPlaceholder();
        }
    }

    private void applyPlantFilters(boolean showToast) {
        dataList.clear();
        if (selectedPlantBlock == null) {
            updatePlantSummaryPlaceholder();
            updateDataUI();
            return;
        }

        bindPlantBlockSummary(selectedPlantBlock);
        if (selectedPlantDate.isEmpty()) {
            updateDataUI();
            tvDataCount.setText("请选择地块和定植日期后加载苗木标签");
            if (showToast) toast("请选择定植日期");
            return;
        }
        if (selectedPlantBlock.id <= 0) {
            updateDataUI();
            toast("地块数据缺少接口ID，无法获取打印数据");
            return;
        }

        showLoading("正在加载苗木标签数据...");
        printerApiService.getPlantPrintData(selectedPlantBlock.id, selectedPlantDate)
                .enqueue(new Callback<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>>>() {
                    @Override
                    public void onResponse(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>>> call,
                                           Response<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>>> response) {
                        hideLoading();
                        PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccessful() || body.data == null) {
                            showPrintDataErrorDialog(body, response);
                            updateDataUI();
                            return;
                        }
                        dataList.clear();
                        for (PrinterApiModels.PlantPrintData item : body.data) {
                            dataList.add(item.toLabelData());
                        }
                        updateDataUI();
                        if (showToast) {
                            Toast.makeText(MainActivity.this, "已按定植日期筛选到 " + dataList.size() + " 棵苗木", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>>> call, Throwable t) {
                        hideLoading();
                        updateDataUI();
                        toast("苗木标签数据加载失败：" + t.getMessage());
                    }
                });
    }

    private void bindPlantBlockSummary(PlantBlockData block) {
        tvPlantBlockName.setText("地块名称：" + block.name);
        tvPlantBlockCode.setText("自编码：" + block.selfCode);
        tvPlantBlockLocation.setText("位置：" + block.location);
        tvPlantBlockStatus.setText("状态：" + block.status);
        tvPlantBlockStatus.setTextColor(getStatusColor(block.status));
        tvPlantBlockOwner.setText("负责人：按地块配置");
        tvPlantBlockOwner.setTextColor(Color.parseColor("#555555"));
    }

    private void applyFieldFilters(boolean showToast) {
        dataList.clear();
        if (selectedPlantation == null) {
            updateFieldSummaryPlaceholder();
            updateDataUI();
            return;
        }
        if (selectedPlantation.id <= 0) {
            updateDataUI();
            toast("种植园数据缺少接口ID，无法获取打印数据");
            return;
        }

        bindPlantationSummary(selectedPlantation);
        showLoading("正在加载地块标签数据...");
        printerApiService.getFieldPrintData(selectedPlantation.id)
                .enqueue(new Callback<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldPrintData>>>() {
                    @Override
                    public void onResponse(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldPrintData>>> call,
                                           Response<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldPrintData>>> response) {
                        hideLoading();
                        PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldPrintData>> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccessful() || body.data == null) {
                            showPrintDataErrorDialog(body, response);
                            updateDataUI();
                            return;
                        }
                        dataList.clear();
                        for (PrinterApiModels.FieldPrintData item : body.data) {
                            dataList.add(item.toLabelData());
                        }
                        updateDataUI();
                        if (showToast) {
                            Toast.makeText(MainActivity.this, "已载入“" + selectedPlantation.name + "”的地块标签", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldPrintData>>> call, Throwable t) {
                        hideLoading();
                        updateDataUI();
                        toast("地块标签数据加载失败：" + t.getMessage());
                    }
                });
    }

    private void bindPlantationSummary(PlantationData plantation) {
        tvPlantBlockName.setText("种植园：" + plantation.name);
        tvPlantBlockCode.setText("自编码：" + plantation.selfCode);
        tvPlantBlockLocation.setText("总面积：" + plantation.totalArea);
        tvPlantBlockStatus.setText("状态：" + plantation.status);
        tvPlantBlockStatus.setTextColor(getStatusColor(plantation.status));
        tvPlantBlockOwner.setText("负责人：" + plantation.owner);
        tvPlantBlockOwner.setTextColor(Color.parseColor("#555555"));
    }

    private void bindGreenhouseSummary(GreenhouseLabelData greenhouse) {
        tvPlantBlockName.setText("大棚：" + greenhouse.greenhouseName);
        tvPlantBlockCode.setText("自编码：" + greenhouse.selfCode);
        tvPlantBlockLocation.setText("种植园：" + greenhouse.plantationName + "，面积：" + greenhouse.area);
        tvPlantBlockStatus.setText("状态：" + greenhouse.status);
        tvPlantBlockStatus.setTextColor(getStatusColor(greenhouse.status));
        tvPlantBlockOwner.setText("负责人：" + greenhouse.owner);
        tvPlantBlockOwner.setTextColor(Color.parseColor("#555555"));
    }

    private void applyGreenhouseFilters(boolean showToast) {
        dataList.clear();
        if (selectedPlantation == null) {
            updateGreenhouseSummaryPlaceholder();
            updateDataUI();
            return;
        }
        if (selectedPlantation.id <= 0) {
            updateDataUI();
            toast("种植园数据缺少接口ID，无法获取打印数据");
            return;
        }

        bindPlantationSummary(selectedPlantation);
        showLoading("正在加载大棚标签数据...");
        printerApiService.getGreenhousePrintData(selectedPlantation.id)
                .enqueue(new Callback<PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhousePrintData>>>() {
                    @Override
                    public void onResponse(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhousePrintData>>> call,
                                           Response<PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhousePrintData>>> response) {
                        hideLoading();
                        PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhousePrintData>> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccessful() || body.data == null) {
                            showPrintDataErrorDialog(body, response);
                            updateDataUI();
                            return;
                        }
                        dataList.clear();
                        for (PrinterApiModels.GreenhousePrintData item : body.data) {
                            dataList.add(item.toLabelData());
                        }
                        updateDataUI();
                        if (showToast) {
                            Toast.makeText(MainActivity.this, "已载入“" + selectedPlantation.name + "”的大棚标签", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhousePrintData>>> call, Throwable t) {
                        hideLoading();
                        updateDataUI();
                        toast("大棚标签数据加载失败：" + t.getMessage());
                    }
                });
    }

    private void applySeedbedFilters(boolean showToast) {
        dataList.clear();
        if (selectedGreenhouse == null) {
            updateSeedbedSummaryPlaceholder();
            updateDataUI();
            return;
        }
        if (selectedGreenhouse.id <= 0) {
            updateDataUI();
            toast("大棚数据缺少接口ID，无法获取打印数据");
            return;
        }

        bindGreenhouseSummary(selectedGreenhouse);
        showLoading("正在加载苗床标签数据...");
        printerApiService.getSeedbedPrintData(selectedGreenhouse.id)
                .enqueue(new Callback<PrinterApiModels.ApiResponse<List<PrinterApiModels.SeedbedPrintData>>>() {
                    @Override
                    public void onResponse(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.SeedbedPrintData>>> call,
                                           Response<PrinterApiModels.ApiResponse<List<PrinterApiModels.SeedbedPrintData>>> response) {
                        hideLoading();
                        PrinterApiModels.ApiResponse<List<PrinterApiModels.SeedbedPrintData>> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccessful() || body.data == null) {
                            showPrintDataErrorDialog(body, response);
                            updateDataUI();
                            return;
                        }
                        dataList.clear();
                        for (PrinterApiModels.SeedbedPrintData item : body.data) {
                            dataList.add(item.toLabelData());
                        }
                        updateDataUI();
                        if (showToast) {
                            Toast.makeText(MainActivity.this, "已载入“" + selectedGreenhouse.selfCode + "”的苗床标签", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.SeedbedPrintData>>> call, Throwable t) {
                        hideLoading();
                        updateDataUI();
                        toast("苗床标签数据加载失败：" + t.getMessage());
                    }
                });
    }

/*    private void applyProductFilters(boolean showToast) {
        dataList.clear();
        if (selectedProductDate.isEmpty()) {
            updateProductSummaryPlaceholder();
            updateDataUI();
            return;
        }
        dataList.addAll(MockLabelRepository.findProductLabelsByDate(selectedProductDate));
        bindProductSummary();
        updateDataUI();
        if (showToast) {
            Toast.makeText(this, "已按完工日期筛选到 " + dataList.size() + " 个产成品标签", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyProcessFilters(boolean showToast) {
        dataList.clear();
        if (selectedProcessTypeKey.isEmpty() || selectedProcessDate.isEmpty()) {
            updateProcessSummaryPlaceholder();
            updateDataUI();
            return;
        }
        dataList.addAll(MockLabelRepository.findProcessLabelsByTypeAndDate(selectedProcessTypeKey, selectedProcessDate));
        bindProcessSummary();
        updateDataUI();
        if (showToast) {
            Toast.makeText(this, "已按类型和完工日期筛选到 " + dataList.size() + " 条加工标签", Toast.LENGTH_SHORT).show();
        }
    }*/

    private void resetPlantFilters() {
        isPlantingEntryPrintMode = false;
        plantingEntryFieldCode = "";
        plantingEntryDate = "";
        plantingEntryRecordTime = "";
        selectedPlantBlock = null;
        selectedPlantDate = "";
        dataList.clear();
        spinnerPlantBlock.setText("", false);
        etPlantDate.setText("");
        tilPlantBlock.setVisibility(View.VISIBLE);
        tilPlantDate.setVisibility(View.VISIBLE);
        btnResetPlantFilters.setVisibility(View.VISIBLE);
        updatePlantSummaryPlaceholder();
        updateDataUI();
    }

    private void updatePlantSummaryPlaceholder() {
        tvPlantBlockName.setText("地块名称：待选择");
        tvPlantBlockCode.setText("自编码：待选择");
        tvPlantBlockLocation.setText("位置：待选择");
        tvPlantBlockStatus.setText("状态：待选择");
        tvPlantBlockStatus.setTextColor(Color.parseColor("#999999"));
        tvPlantBlockOwner.setText("负责人：待选择");
        tvPlantBlockOwner.setTextColor(Color.parseColor("#999999"));
        tvPlantCount.setText("0");
        tvDataCount.setText("请选择地块，并可按定植日期进一步筛选");
    }

    private void resetFieldFilters() {
        selectedPlantation = null;
        dataList.clear();
        spinnerPlantBlock.setText("", false);
        etPlantDate.setText("");
        updateFieldSummaryPlaceholder();
        updateDataUI();
    }

    private void updateFieldSummaryPlaceholder() {
        tvPlantBlockName.setText("种植园：待选择");
        tvPlantBlockCode.setText("自编码：待选择");
        tvPlantBlockLocation.setText("总面积：待选择");
        tvPlantBlockStatus.setText("状态：待选择");
        tvPlantBlockStatus.setTextColor(Color.parseColor("#999999"));
        tvPlantBlockOwner.setText("负责人：待选择");
        tvPlantBlockOwner.setTextColor(Color.parseColor("#999999"));
        tvPlantCount.setText("0");
        tvDataCount.setText("请选择种植园，批量打印该种植园下所有地块标签");
    }

    private void resetGreenhouseFilters() {
        selectedPlantation = null;
        dataList.clear();
        spinnerPlantBlock.setText("", false);
        etPlantDate.setText("");
        updateGreenhouseSummaryPlaceholder();
        updateDataUI();
    }

    private void updateGreenhouseSummaryPlaceholder() {
        tvPlantBlockName.setText("种植园：待选择");
        tvPlantBlockCode.setText("自编码：待选择");
        tvPlantBlockLocation.setText("总面积：待选择");
        tvPlantBlockStatus.setText("状态：待选择");
        tvPlantBlockStatus.setTextColor(Color.parseColor("#999999"));
        tvPlantBlockOwner.setText("负责人：待选择");
        tvPlantBlockOwner.setTextColor(Color.parseColor("#999999"));
        tvPlantCount.setText("0");
        tvDataCount.setText("请选择种植园，批量打印该种植园下所有大棚标签");
    }

    private void resetSeedbedFilters() {
        selectedGreenhouse = null;
        dataList.clear();
        spinnerPlantBlock.setText("", false);
        etPlantDate.setText("");
        updateSeedbedSummaryPlaceholder();
        updateDataUI();
    }

    private void updateSeedbedSummaryPlaceholder() {
        tvPlantBlockName.setText("大棚：待选择");
        tvPlantBlockCode.setText("自编码：待选择");
        tvPlantBlockLocation.setText("种植园/面积：待选择");
        tvPlantBlockStatus.setText("状态：待选择");
        tvPlantBlockStatus.setTextColor(Color.parseColor("#999999"));
        tvPlantBlockOwner.setText("负责人：待选择");
        tvPlantBlockOwner.setTextColor(Color.parseColor("#999999"));
        tvPlantCount.setText("0");
        tvDataCount.setText("请选择大棚，批量打印该大棚下所有苗床标签");
    }

/*    private void resetProductFilters() {
        selectedProductDate = "";
        dataList.clear();
        etPlantDate.setText("");
        updateProductSummaryPlaceholder();
        tvDataCount.setText("请选择完工日期，批量打印该日期下所有产成品标签");
        updateDataUI();
    }*/

    private void resetProcessFilters() {
        selectedProcessTypeKey = "";
        selectedProcessDate = "";
        dataList.clear();
        spinnerPlantBlock.setText("", false);
        etPlantDate.setText("");
        updateProcessSummaryPlaceholder();
        tvDataCount.setText("请先选择加工类型，再选择完工日期");
        updateDataUI();
    }

    private void updateDataUI() {
        btnPrint.setText("确认打印 (" + dataList.size() + ")");
        updateQrPreviewList();
        if (TEMP_MM.equals(currentTemplate()) && isPlantingEntryPrintMode) {
            String recordTimeLine = plantingEntryRecordTime.isEmpty() ? "录入时间：未填写" : "录入时间：" + plantingEntryRecordTime;
            tvDataCount.setText("当前将打印本次录入的 " + dataList.size() + " 张苗木标签\n" + recordTimeLine);
            tvPlantCount.setText(String.valueOf(dataList.size()));
        } else if (TEMP_MM.equals(currentTemplate()) && selectedPlantBlock != null) {
            String dateSuffix = selectedPlantDate.isEmpty() ? "" : "，定植日期：" + selectedPlantDate;
            tvDataCount.setText("当前将打印地块“" + selectedPlantBlock.name + "”中的 " + dataList.size() + " 张苗木标签" + dateSuffix);
            tvPlantCount.setText(String.valueOf(dataList.size()));
        } else if (TEMP_DK.equals(currentTemplate()) && selectedPlantation != null) {
            tvDataCount.setText("当前将打印种植园“" + selectedPlantation.name + "”中的 " + dataList.size() + " 张地块标签");
            tvPlantCount.setText(String.valueOf(dataList.size()));
        } else if (TEMP_DP.equals(currentTemplate()) && selectedPlantation != null) {
            tvDataCount.setText("当前将打印种植园“" + selectedPlantation.name + "”中的 " + dataList.size() + " 张大棚标签");
            tvPlantCount.setText(String.valueOf(dataList.size()));
        } else if (TEMP_MC.equals(currentTemplate()) && selectedGreenhouse != null) {
            tvDataCount.setText("当前将打印大棚“" + selectedGreenhouse.greenhouseName + "”中的 " + dataList.size() + " 张苗床标签");
            tvPlantCount.setText(String.valueOf(dataList.size()));
//        } else if (TEMP_CJG.equals(currentTemplate()) && !selectedProcessTypeKey.isEmpty() && !selectedProcessDate.isEmpty()) {
//            tvDataCount.setText("当前将打印" + getProcessTypeDisplayName(selectedProcessTypeKey) + "在 " + selectedProcessDate + " 完工的 " + dataList.size() + " 张加工标签");
//            tvPlantCount.setText(String.valueOf(dataList.size()));
//        } else if (TEMP_CP.equals(currentTemplate()) && !selectedProductDate.isEmpty()) {
//            tvDataCount.setText("当前将打印完工日期为 " + selectedProductDate + " 的 " + dataList.size() + " 张产成品标签");
//            tvPlantCount.setText(String.valueOf(dataList.size()));
        } else if (!TEMP_MM.equals(currentTemplate()) && !TEMP_DK.equals(currentTemplate()) && !TEMP_DP.equals(currentTemplate()) && !TEMP_MC.equals(currentTemplate())) {
            tvDataCount.setText(dataList.isEmpty() ? "" : "已准备 " + dataList.size() + " 张标签");
        }
    }

    private void updateQrPreviewList() {
        if (layoutQrPreviewList == null || tvQrPreviewMore == null) {
            return;
        }
        layoutQrPreviewList.removeAllViews();
        tvQrPreviewMore.setVisibility(View.GONE);
        if (!isBatchPreviewTemplate()) {
            layoutQrPreviewList.setVisibility(View.GONE);
            return;
        }

        layoutQrPreviewList.setVisibility(View.VISIBLE);
        if (dataList.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("暂无待打印二维码");
            emptyView.setTextColor(Color.parseColor("#888888"));
            emptyView.setTextSize(13);
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setPadding(dp(12), dp(12), dp(12), dp(12));
            layoutQrPreviewList.addView(emptyView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            return;
        }

        int displayCount = Math.min(dataList.size(), 10);
        for (int i = 0; i < displayCount; i++) {
            layoutQrPreviewList.addView(createQrPreviewRow(i, dataList.get(i)));
        }
        if (dataList.size() > displayCount) {
            tvQrPreviewMore.setText("仅显示前10个二维码，后续 " + (dataList.size() - displayCount) + " 个标签已省略显示");
            tvQrPreviewMore.setVisibility(View.VISIBLE);
        }
    }

    private boolean isBatchPreviewTemplate() {
        String template = currentTemplate();
        return TEMP_MM.equals(template) || TEMP_DK.equals(template) || TEMP_DP.equals(template) || TEMP_MC.equals(template);
    }

    private View createQrPreviewRow(int index, LabelData item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(9), dp(10), dp(9));
        row.setBackgroundColor(index % 2 == 0 ? Color.WHITE : Color.parseColor("#F3F8F4"));

        TextView orderView = new TextView(this);
        orderView.setText(String.format(Locale.getDefault(), "#%02d", index + 1));
        orderView.setTextColor(Color.parseColor("#2E7D32"));
        orderView.setTextSize(13);
        orderView.setTypeface(null, Typeface.BOLD);

        TextView codeView = new TextView(this);
        codeView.setText(resolveQrPreviewCode(item));
        codeView.setTextColor(Color.parseColor("#333333"));
        codeView.setTextSize(13);
        codeView.setSingleLine(true);
        codeView.setEllipsize(TextUtils.TruncateAt.END);

        LinearLayout.LayoutParams orderParams = new LinearLayout.LayoutParams(dp(48), LinearLayout.LayoutParams.WRAP_CONTENT);
        row.addView(orderView, orderParams);
        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(codeView, codeParams);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.topMargin = index == 0 ? 0 : dp(1);
        row.setLayoutParams(rowParams);
        return row;
    }

    private String resolveQrPreviewCode(LabelData item) {
        if (item == null) {
            return "";
        }
        if (item.traceCode != null && !item.traceCode.trim().isEmpty()) {
            return item.traceCode;
        }
        return item.f1 == null ? "" : item.f1;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

/*    private void bindProductSummary() {
        tvPlantBlockCode.setText("完工日期：" + selectedProductDate);
    }*/

    private void updateProductSummaryPlaceholder() {
        tvPlantBlockCode.setText("完工日期：待选择");
        tvPlantCount.setText("0");
    }

    private void bindProcessSummary() {
        tvPlantBlockName.setText("标签类型：加工");
        tvPlantBlockCode.setText("加工类型：" + getProcessTypeDisplayName(selectedProcessTypeKey));
        tvPlantBlockLocation.setText("完工日期：" + selectedProcessDate);
    }

    private void updateProcessSummaryPlaceholder() {
        tvPlantBlockName.setText("标签类型：加工");
        tvPlantBlockCode.setText("加工类型：待选择");
        tvPlantBlockLocation.setText("完工日期：待选择");
        tvPlantCount.setText("0");
    }

    private String getProcessTypeDisplayName(String typeKey) {
        if (MockLabelRepository.PROCESS_TYPE_MATERIAL.equals(typeKey)) {
            return LabelTemplates.TYPE_INITIAL;
        }
        if (MockLabelRepository.PROCESS_TYPE_SEMI_FINISHED.equals(typeKey)) {
            return LabelTemplates.TYPE_DEEP;
        }
        return "未知类型";
    }

    private void showManualPlantDialog() {
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, padding / 2, padding, 0);

        EditText input = new EditText(this);
        input.setHint("请输入苗木二维码");
        input.setSingleLine();
        container.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("补打单个苗木标签")
                .setMessage("输入苗木二维码后，将校验并补打单个标签。")
                .setView(container)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button confirmButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            confirmButton.setOnClickListener(v -> {
                String manualCode = input.getText().toString().trim();
                if (manualCode.isEmpty()) {
                    Toast.makeText(this, "请输入苗木二维码", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                fetchManualPlantPrintData(manualCode);
            });
        });

        dialog.show();
    }

    private void showManualFieldDialog() {
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, padding / 2, padding, 0);

        EditText input = new EditText(this);
        input.setHint("请输入地块自编码");
        input.setSingleLine();
        container.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("补打单个地块标签")
                .setMessage("输入地块自编码后，将校验并补打单个标签。")
                .setView(container)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button confirmButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            confirmButton.setOnClickListener(v -> {
                String selfCode = input.getText().toString().trim();
                if (selfCode.isEmpty()) {
                    Toast.makeText(this, "请输入地块自编码", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                fetchManualFieldPrintData(selfCode);
            });
        });

        dialog.show();
    }

    private void showManualGreenhouseDialog() {
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, padding / 2, padding, 0);

        EditText input = new EditText(this);
        input.setHint("请输入大棚自编码");
        input.setSingleLine();
        container.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("补打单个大棚标签")
                .setMessage("输入大棚自编码后，将校验并补打单个标签。")
                .setView(container)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button confirmButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            confirmButton.setOnClickListener(v -> {
                String selfCode = input.getText().toString().trim();
                if (selfCode.isEmpty()) {
                    Toast.makeText(this, "请输入大棚自编码", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                fetchManualGreenhousePrintData(selfCode);
            });
        });

        dialog.show();
    }

    private void showManualSeedbedDialog() {
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, padding / 2, padding, 0);

        EditText input = new EditText(this);
        input.setHint("请输入苗床自编码");
        input.setSingleLine();
        container.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("补打单个苗床标签")
                .setMessage("输入苗床自编码后，将校验并补打单个标签。")
                .setView(container)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button confirmButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            confirmButton.setOnClickListener(v -> {
                String selfCode = input.getText().toString().trim();
                if (selfCode.isEmpty()) {
                    Toast.makeText(this, "请输入苗床自编码", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                fetchManualSeedbedPrintData(selfCode);
            });
        });

        dialog.show();
    }

    private void fetchManualPlantPrintData(String plantQrcode) {
        showLoading("正在获取苗木补打数据...");
        printerApiService.getPlantPrintData(plantQrcode)
                .enqueue(new Callback<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>>>() {
                    @Override
                    public void onResponse(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>>> call,
                                           Response<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>>> response) {
                        hideLoading();
                        PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccessful() || body.data == null) {
                            showPrintDataErrorDialog(body, response);
                            return;
                        }
                        PrinterApiModels.PlantPrintData matchedItem = findPlantPrintDataByQrcode(body.data, plantQrcode);
                        if (matchedItem == null) {
                            toast("未找到该苗木二维码的打印数据");
                            return;
                        }
                        LabelData label = matchedItem.toLabelData();
                        showReprintConfirmDialog("苗木", label, buildPlantConfirmMessage(label));
                    }

                    @Override
                    public void onFailure(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>>> call, Throwable t) {
                        hideLoading();
                        toast("苗木补打数据获取失败：" + t.getMessage());
                    }
                });
    }

    private void fetchManualFieldPrintData(String fieldCode) {
        showLoading("正在获取地块补打数据...");
        printerApiService.getFieldPrintData(fieldCode)
                .enqueue(new Callback<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldPrintData>>>() {
                    @Override
                    public void onResponse(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldPrintData>>> call,
                                           Response<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldPrintData>>> response) {
                        hideLoading();
                        PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldPrintData>> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccessful() || body.data == null) {
                            showPrintDataErrorDialog(body, response);
                            return;
                        }
                        PrinterApiModels.FieldPrintData matchedItem = findFieldPrintDataByCode(body.data, fieldCode);
                        if (matchedItem == null) {
                            toast("未找到该地块自编码的打印数据");
                            return;
                        }
                        LabelData label = matchedItem.toLabelData();
                        showReprintConfirmDialog("地块", label, buildFieldConfirmMessage(label));
                    }

                    @Override
                    public void onFailure(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldPrintData>>> call, Throwable t) {
                        hideLoading();
                        toast("地块补打数据获取失败：" + t.getMessage());
                    }
                });
    }

    private void fetchManualGreenhousePrintData(String greenhouseCode) {
        showLoading("正在获取大棚补打数据...");
        printerApiService.getGreenhousePrintData(greenhouseCode)
                .enqueue(new Callback<PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhousePrintData>>>() {
                    @Override
                    public void onResponse(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhousePrintData>>> call,
                                           Response<PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhousePrintData>>> response) {
                        hideLoading();
                        PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhousePrintData>> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccessful() || body.data == null) {
                            showPrintDataErrorDialog(body, response);
                            return;
                        }
                        PrinterApiModels.GreenhousePrintData matchedItem = findGreenhousePrintDataByCode(body.data, greenhouseCode);
                        if (matchedItem == null) {
                            toast("未找到该大棚自编码的打印数据");
                            return;
                        }
                        LabelData label = matchedItem.toLabelData();
                        showReprintConfirmDialog("大棚", label, buildGreenhouseConfirmMessage(label));
                    }

                    @Override
                    public void onFailure(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhousePrintData>>> call, Throwable t) {
                        hideLoading();
                        toast("大棚补打数据获取失败：" + t.getMessage());
                    }
                });
    }

    private void fetchManualSeedbedPrintData(String seedbedCode) {
        showLoading("正在获取苗床补打数据...");
        printerApiService.getSeedbedPrintData(seedbedCode)
                .enqueue(new Callback<PrinterApiModels.ApiResponse<List<PrinterApiModels.SeedbedPrintData>>>() {
                    @Override
                    public void onResponse(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.SeedbedPrintData>>> call,
                                           Response<PrinterApiModels.ApiResponse<List<PrinterApiModels.SeedbedPrintData>>> response) {
                        hideLoading();
                        PrinterApiModels.ApiResponse<List<PrinterApiModels.SeedbedPrintData>> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccessful() || body.data == null) {
                            showPrintDataErrorDialog(body, response);
                            return;
                        }
                        PrinterApiModels.SeedbedPrintData matchedItem = findSeedbedPrintDataByCode(body.data, seedbedCode);
                        if (matchedItem == null) {
                            toast("未找到该苗床自编码的打印数据");
                            return;
                        }
                        LabelData label = matchedItem.toLabelData();
                        showReprintConfirmDialog("苗床", label, buildSeedbedConfirmMessage(label));
                    }

                    @Override
                    public void onFailure(Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.SeedbedPrintData>>> call, Throwable t) {
                        hideLoading();
                        toast("苗床补打数据获取失败：" + t.getMessage());
                    }
                });
    }

    private void showReprintConfirmDialog(String labelType, LabelData label, String message) {
        new AlertDialog.Builder(this)
                .setTitle("确认补打" + labelType + "标签")
                .setMessage(message)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> {
                    dataList.clear();
                    dataList.add(label);
                    updateDataUI();
                    tvDataCount.setText("已准备补打 1 张" + labelType + "标签，二维码：" + resolveQrPreviewCode(label));
                    tvPlantCount.setText("1");
                    printCurrentData();
                })
                .show();
    }

    private PrinterApiModels.PlantPrintData findPlantPrintDataByQrcode(List<PrinterApiModels.PlantPrintData> items, String plantQrcode) {
        String target = normalizeCode(plantQrcode);
        for (PrinterApiModels.PlantPrintData item : items) {
            if (target.equals(normalizeCode(item.plantQrcode))) {
                return item;
            }
        }
        return null;
    }

    private PrinterApiModels.FieldPrintData findFieldPrintDataByCode(List<PrinterApiModels.FieldPrintData> items, String fieldCode) {
        String target = normalizeCode(fieldCode);
        for (PrinterApiModels.FieldPrintData item : items) {
            if (target.equals(normalizeCode(item.fieldCode))) {
                return item;
            }
        }
        return null;
    }

    private PrinterApiModels.GreenhousePrintData findGreenhousePrintDataByCode(List<PrinterApiModels.GreenhousePrintData> items, String greenhouseCode) {
        String target = normalizeCode(greenhouseCode);
        for (PrinterApiModels.GreenhousePrintData item : items) {
            if (target.equals(normalizeCode(item.greenhouseCode))) {
                return item;
            }
        }
        return null;
    }

    private PrinterApiModels.SeedbedPrintData findSeedbedPrintDataByCode(List<PrinterApiModels.SeedbedPrintData> items, String seedbedCode) {
        String target = normalizeCode(seedbedCode);
        for (PrinterApiModels.SeedbedPrintData item : items) {
            if (target.equals(normalizeCode(item.seedbedCode))) {
                return item;
            }
        }
        return null;
    }

    private String buildPlantConfirmMessage(LabelData label) {
        return "品种：" + label.f1
                + "\n代数：" + label.f2
                + "\n育苗方法：" + label.f3
                + "\n所属地块：" + label.f4
                + "\n定植日期：" + label.f6
                + "\n二维码：" + label.traceCode;
    }

    private String buildFieldConfirmMessage(LabelData label) {
        return "地块自编码：" + label.f1
                + "\n所属种植园：" + label.f2
                + "\n长宽：" + label.f3
                + "\n面积：" + label.f4
                + "\n负责人：" + label.f5
                + "\n二维码：" + label.traceCode;
    }

    private String buildGreenhouseConfirmMessage(LabelData label) {
        return "大棚自编码：" + label.f1
                + "\n所属种植园：" + label.f2
                + "\n面积：" + label.f3
                + "\n负责人：" + label.f4
                + "\n二维码：" + label.traceCode;
    }

    private String buildSeedbedConfirmMessage(LabelData label) {
        return "苗床自编码：" + label.f1
                + "\n所属大棚：" + label.f2
                + "\n所属种植园：" + label.f3
                + "\n负责人：" + label.f4
                + "\n二维码：" + label.traceCode;
    }

/*    private void showManualProductDialog() {
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, padding / 2, padding, 0);

        EditText input = new EditText(this);
        input.setHint("请输入产成品二维码");
        input.setSingleLine();
        container.addView(input);

        TextView validationText = new TextView(this);
        validationText.setPadding(0, padding, 0, 0);
        validationText.setTextColor(Color.parseColor("#2E7D32"));
        container.addView(validationText);

        TextView infoText = new TextView(this);
        infoText.setPadding(0, padding / 2, 0, 0);
        infoText.setVisibility(View.GONE);
        container.addView(infoText);

        final LabelData[] validatedLabel = new LabelData[1];
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("补打单个产成品标签")
                .setMessage("先校验产成品二维码，再确认信息后打印。")
                .setView(container)
                .setNeutralButton("校验二维码", null)
                .setNegativeButton("取消", null)
                .setPositiveButton("确认打印", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button validateButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            Button printButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            printButton.setEnabled(false);

            validateButton.setOnClickListener(v -> {
                String traceCode = input.getText().toString().trim();
                if (traceCode.isEmpty()) {
                    Toast.makeText(this, "请输入产成品二维码", Toast.LENGTH_SHORT).show();
                    return;
                }

                LabelData label = MockLabelRepository.findProductLabelByTraceCode(traceCode);
                if (label == null) {
                    label = new LabelData(TEMP_CP, "", "", "沉香礼盒", "LB-01 / 礼盒装", "20 / 2.0kg", "精品", "2024-05-24 10:00:00", "OP-01", traceCode);
                }

                validatedLabel[0] = label;
                validationText.setText("二维码校验结果：正确");
                infoText.setText("名称：" + label.f1 + "\n型号/规格：" + label.f2 + "\n数量/重量：" + label.f3 + "\n等级：" + label.f4 + "\n完工时间：" + label.f5);
                infoText.setTextColor(Color.parseColor("#2E7D32"));
                infoText.setVisibility(View.VISIBLE);
                printButton.setEnabled(true);
            });

            printButton.setOnClickListener(v -> {
                if (validatedLabel[0] == null) {
                    Toast.makeText(this, "请先校验产成品二维码", Toast.LENGTH_SHORT).show();
                    return;
                }
                dataList.clear();
                dataList.add(validatedLabel[0]);
                updateDataUI();
                tvDataCount.setText("已准备补打 1 张产成品标签，二维码：" + validatedLabel[0].traceCode);
                tvPlantCount.setText("1");
                printCurrentData();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showManualProcessDialog() {
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, padding / 2, padding, 0);

        EditText input = new EditText(this);
        input.setHint("请输入加工二维码");
        input.setSingleLine();
        container.addView(input);

        TextView validationText = new TextView(this);
        validationText.setPadding(0, padding, 0, 0);
        validationText.setTextColor(Color.parseColor("#2E7D32"));
        container.addView(validationText);

        TextView infoText = new TextView(this);
        infoText.setPadding(0, padding / 2, 0, 0);
        infoText.setVisibility(View.GONE);
        container.addView(infoText);

        final LabelData[] validatedLabel = new LabelData[1];
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("补打单个加工标签")
                .setMessage("先选择加工类型，再校验对应二维码。")
                .setView(container)
                .setNeutralButton("校验二维码", null)
                .setNegativeButton("取消", null)
                .setPositiveButton("确认打印", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button validateButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            Button printButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            printButton.setEnabled(false);

            validateButton.setOnClickListener(v -> {
                if (selectedProcessTypeKey.isEmpty()) {
                    Toast.makeText(this, "请先选择加工类型", Toast.LENGTH_SHORT).show();
                    return;
                }
                String traceCode = input.getText().toString().trim();
                if (traceCode.isEmpty()) {
                    Toast.makeText(this, "请输入加工二维码", Toast.LENGTH_SHORT).show();
                    return;
                }

                LabelData label = MockLabelRepository.findProcessLabelByTypeAndTraceCode(selectedProcessTypeKey, traceCode);
                if (label == null) {
                    String typeName = MockLabelRepository.PROCESS_TYPE_MATERIAL.equals(selectedProcessTypeKey) ? LabelTemplates.TYPE_INITIAL : LabelTemplates.TYPE_DEEP;
                    String processName = MockLabelRepository.PROCESS_TYPE_MATERIAL.equals(selectedProcessTypeKey) ? "初步清理" : "精制提纯";
                    label = new LabelData(TEMP_CJG, typeName, processName,
                            "沉香片", "CX-1234 / 5×2", "10 / 5g", "一级",
                            "2024-05-21 14:00:00", "OP-08", traceCode);
                }

                validatedLabel[0] = label;
                validationText.setText("二维码校验结果：正确");
                infoText.setText("类型：" + label.processingType + "(" + label.processName + ")" + "\n名称：" + label.f1 + "\n等级：" + label.f4 + "\n完工时间：" + label.f5);
                infoText.setTextColor(Color.parseColor("#2E7D32"));
                infoText.setVisibility(View.VISIBLE);
                printButton.setEnabled(true);
            });

            printButton.setOnClickListener(v -> {
                if (validatedLabel[0] == null) {
                    Toast.makeText(this, "请先校验加工二维码", Toast.LENGTH_SHORT).show();
                    return;
                }
                dataList.clear();
                dataList.add(validatedLabel[0]);
                updateDataUI();
                tvDataCount.setText("已准备补打 1 张加工标签，二维码：" + validatedLabel[0].traceCode);
                tvPlantCount.setText("1");
                printCurrentData();
                dialog.dismiss();
            });
        });

        dialog.show();
    }*/

    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                    target.setText(date);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showDateTimePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                    TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                            (view1, hourOfDay, minute) -> target.setText(date + " " + String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute)),
                            c.get(Calendar.HOUR_OF_DAY),
                            c.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showPlantDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    selectedPlantDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                    etPlantDate.setText(selectedPlantDate);
                    applyPlantFilters(true);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

/*    private void showProductDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    selectedProductDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                    etPlantDate.setText(selectedProductDate);
                    applyProductFilters(true);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }*/

/*    private void showProcessDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    selectedProcessDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                    etPlantDate.setText(selectedProcessDate);
                    applyProcessFilters(true);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }*/

    private int getStatusColor(String status) {
        if (status == null) return Color.parseColor("#999999");
        if (status.contains("正常") || status.contains("良好")) return Color.parseColor("#2E7D32");
        if (status.contains("待")) return Color.parseColor("#EF6C00");
        return Color.parseColor("#666666");
    }

    private void refreshPrinterStatus() {
        String name = printerSDK.printerName;
        tvStatus.setText(name);
        tvStatus.setTextColor("未连接".equals(name) ? Color.RED : Color.parseColor("#4CAF50"));
    }

    private String currentTemplate() {
        return spinnerTemplate.getText().toString();
    }

    private void showLoading(String message) {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(this);
            loadingDialog.setCancelable(false);
            loadingDialog.setCanceledOnTouchOutside(false);
        }
        loadingDialog.setMessage(message);
        loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
