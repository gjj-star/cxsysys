//优化：不再负责具体打印指令，只管理UI框架、页面切换、权限等
package com.example.printerfeature;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
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
import java.util.List;
import java.util.Locale;

public class MainActivity extends ComponentActivity {

    public static final String TEMP_MM = LabelTemplates.TEMP_MM;
    public static final String TEMP_CJG = LabelTemplates.TEMP_CJG;
    public static final String TEMP_CP = LabelTemplates.TEMP_CP;
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

    private final List<LabelData> dataList = new ArrayList<>();
    private List<PlantBlockData> plantBlocks = new ArrayList<>();
    private List<PlantationData> plantations = new ArrayList<>();
    private List<GreenhouseLabelData> greenhouses = new ArrayList<>();
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
            } else if (TEMP_CJG.equals(currentTemplate())) {
                showProcessDatePicker();
            } else if (TEMP_CP.equals(currentTemplate())) {
                showProductDatePicker();
            }
        });

        plantBlocks = MockLabelRepository.getPlantBlocks();
        plantations = MockLabelRepository.getPlantations();
        greenhouses = MockLabelRepository.getAllGreenhouses();
    }

    private void setupTemplateSelectors() {
        spinnerTemplate.setOnItemClickListener((parent, view, position, id) -> {
            String template = LabelTemplates.allTemplates()[position];
            updateUIByTemplate(template);
            tvToolbarTitle.setText(template + "打印");
        });
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
            } else if (TEMP_CJG.equals(currentTemplate())) {
                resetProcessFilters();
            } else if (TEMP_CP.equals(currentTemplate())) {
                resetProductFilters();
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

        String fieldCode = safe(intent.getStringExtra(EXTRA_ENTRY_FIELD_CODE), "未填写地块编码");
        String plantingDate = safe(intent.getStringExtra(EXTRA_ENTRY_PLANTING_DATE), "");
        String entryRecordTime = safe(intent.getStringExtra(EXTRA_ENTRY_RECORD_TIME), "");
        String subspecies = safe(intent.getStringExtra(EXTRA_ENTRY_SUBSPECIES), "未知品种");
        String generation = safe(intent.getStringExtra(EXTRA_ENTRY_GENERATION), "1");
        String generationWay = safe(intent.getStringExtra(EXTRA_ENTRY_GENERATION_WAY), "嫁接");
        String motherTreeCode = safe(intent.getStringExtra(EXTRA_ENTRY_MOTHER_TREE_SELF_CODE), "-");

        dataList.clear();
        for (int i = 1; i <= plantCount; i++) {
            dataList.add(new LabelData(
                    TEMP_MM,
                    "",
                    "",
                    subspecies,
                    generation,
                    generationWay,
                    fieldCode,
                    motherTreeCode,
                    plantingDate,
                    buildEntryTraceCode(plantingDate, i)
            ));
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


        updateDataUI();
        Toast.makeText(this, "已加载本次录入的 " + plantCount + " 株苗木标签", Toast.LENGTH_SHORT).show();
    }

    private String buildEntryTraceCode(String plantingDate, int index) {
        String datePart = plantingDate == null || plantingDate.isEmpty()
                ? "00000000"
                : plantingDate.replace("-", "");
        return String.format(Locale.getDefault(), "MM-ENTRY-%s-%03d", datePart, index);
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
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
            } else if (TEMP_CJG.equals(template)) {
                message = "请先选择加工类型和完工日期，或补打一个加工标签";
            } else if (TEMP_CP.equals(template)) {
                message = "请先选择完工日期或补打一个产成品";
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
                    } else if (TEMP_CJG.equals(currentTemplate()) && !selectedProcessTypeKey.isEmpty() && !selectedProcessDate.isEmpty()) {
                        applyProcessFilters(false);
                    } else if (TEMP_CP.equals(currentTemplate()) && !selectedProductDate.isEmpty()) {
                        applyProductFilters(false);
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
            case TEMP_CJG:
                if (!ensurePrinterConnected()) return;
                showManualProcessDialog();
                return;
            case TEMP_CP:
                if (!ensurePrinterConnected()) return;
                showManualProductDialog();
                return;
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
        if (TEMP_CJG.equals(currentTemplate())) {
            configureProcessBatchUI();
            selectedProcessTypeKey = MockLabelRepository.PROCESS_TYPE_MATERIAL;
            spinnerPlantBlock.setText("初加工 (material)", false);
            selectedProcessDate = "2024-05-21";
            etPlantDate.setText(selectedProcessDate);
            applyProcessFilters(true);
            return;
        }
        if (TEMP_CP.equals(currentTemplate())) {
            configureProductBatchUI();
            selectedProductDate = "2024-05-22";
            etPlantDate.setText(selectedProductDate);
            applyProductFilters(true);
            return;
        }

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
        } else if (TEMP_CJG.equals(template)) {
            cardManualForm.setVisibility(View.GONE);
            cardPlantBatch.setVisibility(View.VISIBLE);
            btnExample.setText("补打单条");
            configureProcessBatchUI();
            resetProcessFilters();
        } else if (TEMP_CP.equals(template)) {
            cardManualForm.setVisibility(View.GONE);
            cardPlantBatch.setVisibility(View.VISIBLE);
            btnExample.setText("补打单品");
            configureProductBatchUI();
            resetProductFilters();
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

    private void showProcessingTemplate() {
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
    }

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
        tvBatchFilterTitle.setText("筛选苗木所在地块");
        tilPlantBlock.setVisibility(View.VISIBLE);
        tilPlantBlock.setHint("选择地块");
        tilPlantDate.setVisibility(View.VISIBLE);
        tilPlantDate.setHint("定植日期");
        layoutPlantSummary.setVisibility(View.VISIBLE);
        tvPlantCountLabel.setText("待打印苗木标签");
        setupPlantBlockSelector();
    }

    private void configureFieldBatchUI() {
        tvBatchFilterTitle.setText("筛选地块所在种植园");
        tilPlantBlock.setVisibility(View.VISIBLE);
        tilPlantBlock.setHint("选择种植园");
        tilPlantDate.setVisibility(View.GONE);
        layoutPlantSummary.setVisibility(View.VISIBLE);
        tvPlantCountLabel.setText("待打印地块标签");
        setupPlantationSelector();
    }

    private void configureGreenhouseBatchUI() {
        tvBatchFilterTitle.setText("筛选大棚所在种植园");
        tilPlantBlock.setVisibility(View.VISIBLE);
        tilPlantBlock.setHint("选择种植园");
        tilPlantDate.setVisibility(View.GONE);
        layoutPlantSummary.setVisibility(View.VISIBLE);
        tvPlantCountLabel.setText("待打印大棚标签");
        setupPlantationSelector();
    }

    private void configureSeedbedBatchUI() {
        tvBatchFilterTitle.setText("筛选苗床所在大棚");
        tilPlantBlock.setVisibility(View.VISIBLE);
        tilPlantBlock.setHint("选择大棚");
        tilPlantDate.setVisibility(View.GONE);
        layoutPlantSummary.setVisibility(View.VISIBLE);
        tvPlantCountLabel.setText("待打印苗床标签");
        setupGreenhouseSelector();
    }

    private void configureProductBatchUI() {
        tvBatchFilterTitle.setText("筛选产成品完工时间");
        tilPlantBlock.setVisibility(View.GONE);
        tilPlantDate.setVisibility(View.VISIBLE);
        tilPlantDate.setHint("完工日期");
        layoutPlantSummary.setVisibility(View.GONE);
    }

    private void configureProcessBatchUI() {
        tvBatchFilterTitle.setText("筛选加工完工标签");
        tilPlantBlock.setVisibility(View.VISIBLE);
        tilPlantBlock.setHint("选择类型");
        tilPlantDate.setVisibility(View.VISIBLE);
        tilPlantDate.setHint("完工日期");
        layoutPlantSummary.setVisibility(View.GONE);
        setupProcessTypeSelector();
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
        if (TEMP_CP.equals(template) || TEMP_CJG.equals(template)) {
            return etModel.getText().toString() + " / " + etSpec.getText().toString();
        }
        return etF2.getText().toString();
    }

    private String resolveF3(String template) {
        if (TEMP_DK.equals(template)) {
            return etModel.getText().toString() + " × " + etSpec.getText().toString();
        }
        if (TEMP_CP.equals(template) || TEMP_CJG.equals(template)) {
            return etNum.getText().toString() + " / " + etWeight.getText().toString();
        }
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
            names.add(greenhouse.greenhouseName + " (" + greenhouse.selfCode + ")");
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

    private void setupProcessTypeSelector() {
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
    }

    private void setupPlantBlockSelector() {
        List<String> names = new ArrayList<>();
        for (PlantBlockData block : plantBlocks) {
            names.add(block.name);
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
            names.add(plantation.name);
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
        for (PlantData plant : selectedPlantBlock.plants) {
            if (selectedPlantDate.isEmpty() || selectedPlantDate.equals(plant.plantedDate)) {
                dataList.add(MockLabelRepository.toPlantLabel(plant));
            }
        }
        updateDataUI();

        if (showToast) {
            String message = selectedPlantDate.isEmpty()
                    ? "已载入“" + selectedPlantBlock.name + "”的示例苗木标签"
                    : "已按定植日期筛选到 " + dataList.size() + " 棵苗木";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
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

        bindPlantationSummary(selectedPlantation);
        for (FieldLabelData field : selectedPlantation.fields) {
            dataList.add(MockLabelRepository.toFieldLabel(selectedPlantation.name, field));
        }
        updateDataUI();

        if (showToast) {
            Toast.makeText(this, "已载入“" + selectedPlantation.name + "”的地块示例标签", Toast.LENGTH_SHORT).show();
        }
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

        bindPlantationSummary(selectedPlantation);
        List<GreenhouseLabelData> greenhouses = MockLabelRepository.getGreenhousesByPlantationName(selectedPlantation.name);
        for (GreenhouseLabelData greenhouse : greenhouses) {
            dataList.add(MockLabelRepository.toGreenhouseLabel(selectedPlantation.name, greenhouse));
        }
        updateDataUI();

        if (showToast) {
            Toast.makeText(this, "已载入“" + selectedPlantation.name + "”的大棚示例标签", Toast.LENGTH_SHORT).show();
        }
    }

    private void applySeedbedFilters(boolean showToast) {
        dataList.clear();
        if (selectedGreenhouse == null) {
            updateSeedbedSummaryPlaceholder();
            updateDataUI();
            return;
        }

        bindGreenhouseSummary(selectedGreenhouse);
        List<SeedbedLabelData> seedbeds = MockLabelRepository.getSeedbedsByGreenhouseSelfCode(selectedGreenhouse.selfCode);
        for (SeedbedLabelData seedbed : seedbeds) {
            dataList.add(MockLabelRepository.toSeedbedLabel(selectedGreenhouse, seedbed));
        }
        updateDataUI();

        if (showToast) {
            Toast.makeText(this, "已载入“" + selectedGreenhouse.greenhouseName + "”的苗床示例标签", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyProductFilters(boolean showToast) {
        dataList.clear();
        if (selectedProductDate.isEmpty()) {
            updateDataUI();
            return;
        }
        dataList.addAll(MockLabelRepository.findProductLabelsByDate(selectedProductDate));
        updateDataUI();
        if (showToast) {
            Toast.makeText(this, "已按完工日期筛选到 " + dataList.size() + " 个产成品标签", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyProcessFilters(boolean showToast) {
        dataList.clear();
        if (selectedProcessTypeKey.isEmpty() || selectedProcessDate.isEmpty()) {
            updateDataUI();
            return;
        }
        dataList.addAll(MockLabelRepository.findProcessLabelsByTypeAndDate(selectedProcessTypeKey, selectedProcessDate));
        updateDataUI();
        if (showToast) {
            Toast.makeText(this, "已按类型和完工日期筛选到 " + dataList.size() + " 条加工标签", Toast.LENGTH_SHORT).show();
        }
    }

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
        tvPlantBlockStatus.setText("状态：-");
        tvPlantBlockStatus.setTextColor(Color.parseColor("#999999"));
        tvPlantBlockOwner.setText("负责人：-");
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

    private void resetProductFilters() {
        selectedProductDate = "";
        dataList.clear();
        etPlantDate.setText("");
        tvDataCount.setText("请选择完工日期，批量打印该日期下所有产成品标签");
        updateDataUI();
    }

    private void resetProcessFilters() {
        selectedProcessTypeKey = "";
        selectedProcessDate = "";
        dataList.clear();
        spinnerPlantBlock.setText("", false);
        etPlantDate.setText("");
        tvDataCount.setText("请先选择加工类型，再选择完工日期");
        updateDataUI();
    }

    private void updateDataUI() {
        btnPrint.setText("确认打印 (" + dataList.size() + ")");
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
        } else if (TEMP_CJG.equals(currentTemplate()) && !selectedProcessTypeKey.isEmpty() && !selectedProcessDate.isEmpty()) {
            tvDataCount.setText("待打印加工标签：" + dataList.size());
        } else if (TEMP_CP.equals(currentTemplate()) && !selectedProductDate.isEmpty()) {
            tvDataCount.setText("待打印产成品标签：" + dataList.size());
        } else if (!TEMP_MM.equals(currentTemplate()) && !TEMP_DK.equals(currentTemplate()) && !TEMP_DP.equals(currentTemplate()) && !TEMP_MC.equals(currentTemplate()) && !TEMP_CP.equals(currentTemplate()) && !TEMP_CJG.equals(currentTemplate())) {
            tvDataCount.setText(dataList.isEmpty() ? "" : "已准备 " + dataList.size() + " 张标签");
        }
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
                .setTitle("补打单个苗木标签")
                .setMessage("先校验苗木二维码，再确认苗木信息后打印。")
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
                String manualCode = input.getText().toString().trim();
                if (manualCode.isEmpty()) {
                    Toast.makeText(this, "请输入苗木二维码", Toast.LENGTH_SHORT).show();
                    return;
                }

                LabelData label = MockLabelRepository.findPlantLabelByTraceCode(plantBlocks, manualCode);
                String status = selectedPlantBlock != null ? selectedPlantBlock.status : "正常养护";
                if (label == null) {
                    String blockName = selectedPlantBlock != null ? selectedPlantBlock.name : "一号示范地块";
                    label = new LabelData(TEMP_MM, "", "", "金丝油", "2代", "嫁接", blockName, "MS-REPRINT-001", "2024-06-18", manualCode);
                } else if (selectedPlantBlock == null) {
                    PlantBlockData block = MockLabelRepository.findPlantBlockByName(plantBlocks, label.f4);
                    status = block != null ? block.status : "正常养护";
                }

                validatedLabel[0] = label;
                validationText.setText("二维码校验结果：正确");
                infoText.setText("品种：" + label.f1 + "\n代数：" + label.f2 + "\n所属地块：" + label.f4 + "\n状态：" + status);
                infoText.setTextColor(getStatusColor(status));
                infoText.setVisibility(View.VISIBLE);
                printButton.setEnabled(true);
            });

            printButton.setOnClickListener(v -> {
                if (validatedLabel[0] == null) {
                    Toast.makeText(this, "请先校验苗木二维码", Toast.LENGTH_SHORT).show();
                    return;
                }
                dataList.clear();
                dataList.add(validatedLabel[0]);
                updateDataUI();
                tvDataCount.setText("已准备补打 1 张苗木标签，二维码：" + validatedLabel[0].traceCode);
                tvPlantCount.setText("1");
                printCurrentData();
                dialog.dismiss();
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
                .setTitle("补打单个地块标签")
                .setMessage("先校验地块自编码，再确认地块信息后打印。")
                .setView(container)
                .setNeutralButton("校验自编码", null)
                .setNegativeButton("取消", null)
                .setPositiveButton("确认打印", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button validateButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            Button printButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            printButton.setEnabled(false);

            validateButton.setOnClickListener(v -> {
                String selfCode = input.getText().toString().trim();
                if (selfCode.isEmpty()) {
                    Toast.makeText(this, "请输入地块自编码", Toast.LENGTH_SHORT).show();
                    return;
                }

                LabelData label = MockLabelRepository.findFieldLabelBySelfCode(plantations, selfCode);
                PlantationData plantation = selectedPlantation;
                String status = plantation != null ? plantation.status : "正常运营";
                if (label == null) {
                    String plantationName = plantation != null ? plantation.name : "东山一号种植园";
                    String owner = plantation != null ? plantation.owner : "陈大海";
                    label = new LabelData(TEMP_DK, "", "", selfCode, plantationName, "100m × 40m", "4.0亩", owner, "", "DK-REPRINT-" + selfCode);
                } else if (plantation == null) {
                    plantation = MockLabelRepository.findPlantationByName(plantations, label.f2);
                    status = plantation != null ? plantation.status : "正常运营";
                }

                validatedLabel[0] = label;
                validationText.setText("自编码校验结果：正确");
                infoText.setText("地块自编码：" + label.f1 + "\n所属种植园：" + label.f2 + "\n面积：" + label.f4 + "\n负责人：" + label.f5 + "\n状态：" + status);
                infoText.setTextColor(getStatusColor(status));
                infoText.setVisibility(View.VISIBLE);
                printButton.setEnabled(true);
            });

            printButton.setOnClickListener(v -> {
                if (validatedLabel[0] == null) {
                    Toast.makeText(this, "请先校验地块自编码", Toast.LENGTH_SHORT).show();
                    return;
                }
                dataList.clear();
                dataList.add(validatedLabel[0]);
                updateDataUI();
                tvDataCount.setText("已准备补打 1 张地块标签，自编码：" + validatedLabel[0].f1);
                tvPlantCount.setText("1");
                printCurrentData();
                dialog.dismiss();
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
                .setTitle("补打单个大棚标签")
                .setMessage("先校验大棚自编码，再确认大棚信息后打印。")
                .setView(container)
                .setNeutralButton("校验自编码", null)
                .setNegativeButton("取消", null)
                .setPositiveButton("确认打印", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button validateButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            Button printButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            printButton.setEnabled(false);

            validateButton.setOnClickListener(v -> {
                String selfCode = input.getText().toString().trim();
                if (selfCode.isEmpty()) {
                    Toast.makeText(this, "请输入大棚自编码", Toast.LENGTH_SHORT).show();
                    return;
                }

                LabelData label = MockLabelRepository.findGreenhouseLabelBySelfCode(plantations, selfCode);
                PlantationData plantation = selectedPlantation;
                String status = plantation != null ? plantation.status : "正常运营";
                if (label == null) {
                    String plantationName = plantation != null ? plantation.name : "东山一号种植园";
                    String owner = plantation != null ? plantation.owner : "陈大海";
                    label = new LabelData(TEMP_DP, "", "", selfCode, plantationName, "2.0亩", owner, "", "", "GH-REPRINT-" + selfCode);
                } else if (plantation == null) {
                    plantation = MockLabelRepository.findPlantationByName(plantations, label.f2);
                    status = plantation != null ? plantation.status : "正常运营";
                }

                validatedLabel[0] = label;
                validationText.setText("自编码校验结果：正确");
                infoText.setText("大棚自编码：" + label.f1 + "\n所属种植园：" + label.f2 + "\n面积：" + label.f3 + "\n负责人：" + label.f4 + "\n状态：" + status);
                infoText.setTextColor(getStatusColor(status));
                infoText.setVisibility(View.VISIBLE);
                printButton.setEnabled(true);
            });

            printButton.setOnClickListener(v -> {
                if (validatedLabel[0] == null) {
                    Toast.makeText(this, "请先校验大棚自编码", Toast.LENGTH_SHORT).show();
                    return;
                }
                dataList.clear();
                dataList.add(validatedLabel[0]);
                updateDataUI();
                tvDataCount.setText("已准备补打 1 张大棚标签，自编码：" + validatedLabel[0].f1);
                tvPlantCount.setText("1");
                printCurrentData();
                dialog.dismiss();
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
                .setTitle("补打单个苗床标签")
                .setMessage("先校验苗床自编码，再确认苗床信息后打印。")
                .setView(container)
                .setNeutralButton("校验自编码", null)
                .setNegativeButton("取消", null)
                .setPositiveButton("确认打印", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button validateButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            Button printButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            printButton.setEnabled(false);

            validateButton.setOnClickListener(v -> {
                String selfCode = input.getText().toString().trim();
                if (selfCode.isEmpty()) {
                    Toast.makeText(this, "请输入苗床自编码", Toast.LENGTH_SHORT).show();
                    return;
                }

                LabelData label = MockLabelRepository.findSeedbedLabelBySelfCode(greenhouses, selfCode);
                GreenhouseLabelData greenhouse = selectedGreenhouse;
                String status = greenhouse != null ? greenhouse.status : "正常运营";
                if (label == null) {
                    String greenhouseCode = greenhouse != null ? greenhouse.selfCode : "GH-201";
                    String plantationName = greenhouse != null ? greenhouse.plantationName : "东山一号种植园";
                    String owner = greenhouse != null ? greenhouse.owner : "陈大海";
                    label = new LabelData(TEMP_MC, "", "", selfCode, greenhouseCode, plantationName, owner, "", "", "MC-REPRINT-" + selfCode);
                } else if (greenhouse == null) {
                    for (GreenhouseLabelData item : greenhouses) {
                        if (item.selfCode.equals(label.f2)) {
                            greenhouse = item;
                            break;
                        }
                    }
                    status = greenhouse != null ? greenhouse.status : "正常运营";
                }

                validatedLabel[0] = label;
                validationText.setText("自编码校验结果：正确");
                infoText.setText("苗床自编码：" + label.f1 + "\n所属大棚：" + label.f2 + "\n所属种植园：" + label.f3 + "\n负责人：" + label.f4 + "\n状态：" + status);
                infoText.setTextColor(getStatusColor(status));
                infoText.setVisibility(View.VISIBLE);
                printButton.setEnabled(true);
            });

            printButton.setOnClickListener(v -> {
                if (validatedLabel[0] == null) {
                    Toast.makeText(this, "请先校验苗床自编码", Toast.LENGTH_SHORT).show();
                    return;
                }
                dataList.clear();
                dataList.add(validatedLabel[0]);
                updateDataUI();
                tvDataCount.setText("已准备补打 1 张苗床标签，自编码：" + validatedLabel[0].f1);
                tvPlantCount.setText("1");
                printCurrentData();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showManualProductDialog() {
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
                printCurrentData();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

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

    private void showProductDatePicker() {
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
    }

    private void showProcessDatePicker() {
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
    }

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
