package com.example.printerfeature;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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

import com.google.android.material.textfield.TextInputLayout;
import com.gengcon.www.jcprintersdk.callback.PrintCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends ComponentActivity {

    private TextView tvStatus, tvDataCount;
    private ProgressDialog loadingDialog;

    // UI 控件
    private EditText etF1, etF2, etF3, etF4, etF5, etF6, etProcessName, etTraceCode, etSpec, etNum, etModel, etWeight;
    private TextInputLayout tilF1, tilF2, tilF3, tilF4, tilF5, tilF6, tilProcessName, tilModel, tilSpec, tilNum, tilWeight, tilProcessingType;
    private LinearLayout layoutModelSpec, layoutNumWeight;
    private AutoCompleteTextView spinnerTemplate, spinnerProcessingType;
    private Button btnPrint;
    private ImageButton btnBack;
    private TextView tvToolbarTitle;
    private Button btnExample;

    public static final String TEMP_MM = "苗木二维码";
    public static final String TEMP_CJG = "加工二维码"; // 原：初加工二维码
    public static final String TEMP_CP = "产成品二维码";
    public static final String TEMP_DP = "大棚二维码";
    public static final String TEMP_MC = "苗床二维码";
    public static final String TEMP_DK = "地块二维码";

    private static final String TYPE_INITIAL = "初加工";
    private static final String TYPE_DEEP = "精加工";

    // 数据存储类
    private static class LabelData {
        String template;
        String processingType; // 初加工 or 精加工
        String processName;
        String f1, f2, f3, f4, f5, f6, traceCode;

        LabelData(String temp, String processingType, String process, String f1, String f2, String f3, String f4, String f5, String f6, String tc) {
            this.template = temp;
            this.processingType = processingType;
            this.processName = process;
            this.f1 = f1; this.f2 = f2; this.f3 = f3;
            this.f4 = f4; this.f5 = f5; this.f6 = f6;
            this.traceCode = tc;
        }
    }

    private List<LabelData> dataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 适配沉浸式状态栏（根项目风格）
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(Color.WHITE);

        setContentView(R.layout.activity_main);

        btnBack = findViewById(R.id.btnBack);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        btnExample = findViewById(R.id.btnExample);
        tvStatus = findViewById(R.id.tvStatus);
        tvDataCount = findViewById(R.id.tvDataCount);
        Button btnConnect = findViewById(R.id.btnConnect);
        Button btnAddData = findViewById(R.id.btnAddData);
        Button btnClearData = findViewById(R.id.btnClearData);
        btnPrint = findViewById(R.id.btnPrint);

        // 初始化输入框和布局容器
        tilProcessingType = findViewById(R.id.tilProcessingType);
        spinnerProcessingType = findViewById(R.id.spinnerProcessingType);
        tilProcessName = findViewById(R.id.tilProcessName);
        etProcessName = findViewById(R.id.etProcessName);
        tilF1 = findViewById(R.id.tilF1); etF1 = findViewById(R.id.etF1);
        tilF2 = findViewById(R.id.tilF2); etF2 = findViewById(R.id.etF2);
        tilF3 = findViewById(R.id.tilF3); etF3 = findViewById(R.id.etF3);
        tilF4 = findViewById(R.id.tilF4); etF4 = findViewById(R.id.etF4);
        tilF5 = findViewById(R.id.tilF5); etF5 = findViewById(R.id.etF5);
        tilF6 = findViewById(R.id.tilF6); etF6 = findViewById(R.id.etF6);
        etTraceCode = findViewById(R.id.etTraceCode);
        spinnerTemplate = findViewById(R.id.spinnerTemplate);

        // 初始化组合字段布局
        layoutModelSpec = findViewById(R.id.layoutModelSpec);
        tilModel = findViewById(R.id.tilModel);
        etModel = findViewById(R.id.etModel);
        tilSpec = findViewById(R.id.tilSpec);
        etSpec = findViewById(R.id.etSpec);
        
        layoutNumWeight = findViewById(R.id.layoutNumWeight);
        tilNum = findViewById(R.id.tilNum);
        etNum = findViewById(R.id.etNum);
        tilWeight = findViewById(R.id.tilWeight);
        etWeight = findViewById(R.id.etWeight);

        // 默认点击 etF6 弹出日期选择器
        etF6.setFocusable(false);
        etF6.setOnClickListener(v -> showDatePicker());

        // 设置下拉框选项
        String[] templates = {TEMP_MM, TEMP_CJG, TEMP_CP, TEMP_DP, TEMP_MC, TEMP_DK};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, templates);
        spinnerTemplate.setAdapter(adapter);
        spinnerTemplate.setOnItemClickListener((parent, view, position, id) -> {
            updateUIByTemplate(templates[position]);
        });

        // 初始化加工类型下拉框
        String[] processingTypes = {TYPE_INITIAL, TYPE_DEEP};
        ArrayAdapter<String> processingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, processingTypes);
        spinnerProcessingType.setAdapter(processingAdapter);
        spinnerProcessingType.setText(TYPE_INITIAL, false);

        btnBack.setOnClickListener(v -> finish());
        
        btnConnect.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, DeviceListActivity.class));
        });

        btnAddData.setOnClickListener(v -> addData());

        btnClearData.setOnClickListener(v -> {
            dataList.clear();
            updateDataUI();
            Toast.makeText(this, "数据已清空", Toast.LENGTH_SHORT).show();
        });

        btnPrint.setOnClickListener(v -> {
            if (dataList.isEmpty()) {
                Toast.makeText(this, "请先录入数据", Toast.LENGTH_SHORT).show();
                return;
            }
            startPrint();
        });

        // 示例数据按钮点击事件
        btnExample.setOnClickListener(v -> fillExampleData());

        // 根据 Intent 传入的模板名称进行初始化
        String targetTemplate = getIntent().getStringExtra("target_template");
        if (targetTemplate == null) targetTemplate = TEMP_MM;
        
        spinnerTemplate.setText(targetTemplate, false);
        updateUIByTemplate(targetTemplate);
        tvToolbarTitle.setText(targetTemplate + "打印");
        
        updateDataUI();
    }

    /**
     * 在此处填入各个模板的示例数据
     */
    private void fillExampleData() {
        String currentTemplate = spinnerTemplate.getText().toString();
        
        // 清空当前输入
        etF1.setText(""); etF2.setText(""); etF3.setText(""); 
        etF4.setText(""); etF5.setText(""); etF6.setText("");
        etProcessName.setText(""); etModel.setText(""); etSpec.setText("");
        etNum.setText(""); etWeight.setText(""); etTraceCode.setText("");

        if (TEMP_MM.equals(currentTemplate)) {
            // TODO: 填写 苗木二维码 示例数据
            etF1.setText("金丝油（奇楠）");
            etF2.setText("2");
            etF3.setText("嫁接");
            etF4.setText("DK-456");
            etF5.setText("2012-A005");
            etF6.setText("2024-05-20");
            etTraceCode.setText("DDDDDDEEEEEEEEE-AAA-BBBB-CCCCCCCC-YYMMDD-GG");
        } else if (TEMP_CJG.equals(currentTemplate)) {
            // TODO: 填写 加工二维码 示例数据
            spinnerProcessingType.setText(TYPE_INITIAL, false);
            etProcessName.setText("初步清理");
            etF1.setText("沉香片");
            etModel.setText("CX-1234");
            etSpec.setText("5×2");
            etNum.setText("10");
            etWeight.setText("5g");
            etF4.setText("一级");
            etF5.setText("2024-05-21 14:00:00");
            etF6.setText("OP-08");
            etTraceCode.setText("DDDDDDEEEEEEEEE-AAA-BCCCCCCCC-YYMMDD-BB-GG-SS");
        } else if (TEMP_CP.equals(currentTemplate)) {
            // TODO: 填写 产成品二维码 示例数据
            etF1.setText("极品沉香线香");
            etModel.setText("CX-20");
            etSpec.setText("20支");
            etNum.setText("50");
            etWeight.setText("1.5kg");
            etF4.setText("特级");
            etF5.setText("2024-05-22 10:30:00");
            etF6.setText("OP-12");
            etTraceCode.setText("DDDDDDEEEEEEEEE-AAA-PCCCCCCCC-YYMMDD-BB-GG-FFF");
        } else if (TEMP_DP.equals(currentTemplate)) {
            // TODO: 填写 大棚二维码 示例数据
            etF1.setText("DP-12345");
            etF2.setText("ZZY-123");
            etF3.setText("500亩");
            etF4.setText("张三");
            etTraceCode.setText("DDDDDDEEEEEEEEE-PPPPPP-AAAAAA");
        } else if (TEMP_MC.equals(currentTemplate)) {
            // TODO: 填写 苗床二维码 示例数据
            etF1.setText("MC-12345");
            etF2.setText("DP-123");
            etF3.setText("ZZY-123");
            etF4.setText("李四");
            etTraceCode.setText("DDDDDDEEEEEEEEE-PPPPPP-AAAAAA-SSSSSS");
        } else if (TEMP_DK.equals(currentTemplate)) {
            // TODO: 填写 地块二维码 示例数据
            etF1.setText("DK-108");
            etF2.setText("ZZY-123");
            etModel.setText("100");
            etSpec.setText("50");
            etF4.setText("50亩");
            etF5.setText("王五");
            etTraceCode.setText("DDDDDDEEEEEEEEE-PPPPPP-FFFFFF");
        }
        
        Toast.makeText(this, "已填入示例数据", Toast.LENGTH_SHORT).show();
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    etF6.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showDateTimePicker(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    final String date = String.format(Locale.getDefault(), "%d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    
                    int hour = c.get(Calendar.HOUR_OF_DAY);
                    int minute = c.get(Calendar.MINUTE);
                    
                    TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                            (view1, hourOfDay, minute1) -> {
                                String dateTime = date + " " + String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute1);
                                editText.setText(dateTime);
                            }, hour, minute, true);
                    timePickerDialog.show();
                }, year, month, day);
        datePickerDialog.show();
    }

    private void updateUIByTemplate(String template) {
        // 重置所有可见性
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
        
        if (TEMP_MM.equals(template)) {
            tilF1.setHint("品种");
            tilF2.setHint("代数");
            tilF3.setHint("育苗方法");
            tilF4.setHint("地块");
            tilF5.setHint("母树");
            tilF6.setHint("定植日期");
            
            etF5.setFocusableInTouchMode(true);
            etF5.setOnClickListener(null);
            etF6.setFocusable(false);
            etF6.setOnClickListener(v -> showDatePicker());
        } else if (TEMP_CJG.equals(template)) {
            tilProcessingType.setVisibility(View.VISIBLE);
            tilProcessName.setVisibility(View.VISIBLE);
            tilF1.setHint("名称");
            tilF2.setVisibility(View.GONE); 
            layoutModelSpec.setVisibility(View.VISIBLE);
            tilModel.setHint("型号");
            tilSpec.setHint("规格");
            tilF3.setVisibility(View.GONE); 
            layoutNumWeight.setVisibility(View.VISIBLE);
            tilNum.setHint("数量");
            tilWeight.setHint("重量");
            tilF4.setHint("等级");
            tilF5.setHint("完工时间");
            tilF6.setHint("操作员ID");
            
            etF5.setFocusable(false);
            etF5.setOnClickListener(v -> showDateTimePicker(etF5));
            etF6.setFocusableInTouchMode(true);
            etF6.setOnClickListener(null);
        } else if (TEMP_CP.equals(template)) {
            tilF1.setHint("产成品名称");
            tilF2.setVisibility(View.GONE);
            layoutModelSpec.setVisibility(View.VISIBLE);
            tilModel.setHint("型号");
            tilSpec.setHint("规格");
            tilF3.setVisibility(View.GONE);
            layoutNumWeight.setVisibility(View.VISIBLE);
            tilNum.setHint("数量");
            tilWeight.setHint("重量");
            tilF4.setHint("等级");
            tilF5.setHint("完工时间");
            tilF6.setHint("操作员ID");

            etF5.setFocusable(false);
            etF5.setOnClickListener(v -> showDateTimePicker(etF5));
            etF6.setFocusableInTouchMode(true);
            etF6.setOnClickListener(null);
        } else if (TEMP_DP.equals(template)) {
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
        } else if (TEMP_MC.equals(template)) {
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
        } else if (TEMP_DK.equals(template)) {
            tilF1.setHint("自编码");
            tilF2.setHint("种植园");
            tilF3.setVisibility(View.GONE); // 隐藏单一F3，显示长宽组合
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
    }

    private void addData() {
        String temp = spinnerTemplate.getText().toString();
        String processingType = spinnerProcessingType.getText().toString();
        String process = etProcessName.getText().toString();
        String f1 = etF1.getText().toString();
        
        String f2, f3;
        if (TEMP_MM.equals(temp) || TEMP_DP.equals(temp) || TEMP_MC.equals(temp)) {
            f2 = etF2.getText().toString();
            f3 = etF3.getText().toString();
        } else if (TEMP_DK.equals(temp)) {
            f2 = etF2.getText().toString();
            f3 = etModel.getText().toString() + " × " + etSpec.getText().toString();
        } else {
            // 加工/产成品
            f2 = etModel.getText().toString() + " / " + etSpec.getText().toString();
            f3 = etNum.getText().toString() + " / " + etWeight.getText().toString();
        }

        String f4 = etF4.getText().toString();
        String f5 = etF5.getText().toString();
        String f6 = etF6.getText().toString();
        String tc = etTraceCode.getText().toString();

        // 校验逻辑
        if (TEMP_DP.equals(temp) || TEMP_MC.equals(temp)) {
            if (f1.isEmpty() || f2.isEmpty() || f3.isEmpty() || f4.isEmpty()) {
                Toast.makeText(this, "请填写所有必要字段", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (TEMP_DK.equals(temp)) {
            if (f1.isEmpty() || f2.isEmpty() || etModel.getText().toString().isEmpty() || etSpec.getText().toString().isEmpty() || f4.isEmpty() || f5.isEmpty()) {
                Toast.makeText(this, "请填写所有必要字段", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (f1.isEmpty() || f2.trim().equals("/") || f3.trim().equals("/") || f4.isEmpty() || f5.isEmpty() || f6.isEmpty()){
                Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        dataList.add(new LabelData(temp, processingType, process, f1, f2, f3, f4, f5, f6, tc));
        updateDataUI();
        Toast.makeText(this, "已录入第 " + dataList.size() + " 组数据", Toast.LENGTH_SHORT).show();
    }

    private void updateDataUI() {
        btnPrint.setText("确认打印 (" + dataList.size() + ")");
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void refreshStatus() {
        String name = MyApplication.printerName;
        tvStatus.setText(name);
        if (name.equals("未连接")) {
            tvStatus.setTextColor(Color.RED);
        } else {
            tvStatus.setTextColor(Color.parseColor("#4CAF50")); // 适配根项目 AgGreenPrimary
        }
    }

    private int generatedCount = 0;

    private void startPrint() {
        generatedCount = 0;
        int totalCount = dataList.size();
        showLoading("正在准备批量打印 " + totalCount + " 张...");

        PrintCallback printCallback = new PrintCallback() {
            @Override
            public void onProgress(int pageIndex, int quantityIndex, HashMap hashMap) {
                if (pageIndex == totalCount) {
                    MyApplication.api.endPrintJob();
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(MainActivity.this, "全部打印完成", Toast.LENGTH_SHORT).show();
                        dataList.clear();
                        updateDataUI();
                    });
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
                String name = MyApplication.printerName;
                runOnUiThread(() -> {
                    hideLoading();
                    if (name.equals("未连接")) {
                        Toast.makeText(MainActivity.this, "请先连接打印机", Toast.LENGTH_SHORT).show();
                        return;
                    };
                    Toast.makeText(MainActivity.this, "打印出错:" + errorCode, Toast.LENGTH_SHORT).show();
                });
            }

            @Override public void onError(int i) {}
            @Override public void onCancelJob(boolean i) {}
        };

        MyApplication.api.setTotalPrintQuantity(totalCount);
        MyApplication.api.startPrintJob(3, 1, 1, printCallback);
    }

    private void commitPrintData(LabelData data) {
        float w = 70, h = 50;
        MyApplication.api.drawEmptyLabel(w, h, 0, new ArrayList<>());
        // 外边框（加粗）
        float margin = 1.5f;
        //上
        MyApplication.api.drawLabelLine(margin, margin, 70 - margin * 2, 0.5f, 0, 1, new float[]{});
        //下
        MyApplication.api.drawLabelLine(margin, 50 - margin, 70 - margin * 2, 0.5f, 0, 1, new float[]{});
        // 左
        MyApplication.api.drawLabelLine(margin, margin, 0.5f, 50 - margin * 2, 0, 1, new float[]{});
        // 右
        MyApplication.api.drawLabelLine(70 - margin, margin, 0.5f, 50 - margin * 2, 0, 1, new float[]{});
        
        // 标题 居中
        String title;
        if (TEMP_MM.equals(data.template)) title = "沉香溯源标签【苗木】";
        else if (TEMP_CJG.equals(data.template)) title = "沉香溯源标签【" + data.processingType + "-(" + data.processName + ")】";
        else if (TEMP_CP.equals(data.template)) title = "沉香溯源标签【产成品】";
        else if (TEMP_DP.equals(data.template)) title = "沉香溯源标签【大棚】";
        else if (TEMP_MC.equals(data.template)) title = "沉香溯源标签【苗床】";
        else title = "沉香溯源标签【地块】";
        
        MyApplication.api.drawLabelText(0, 2, 70, 7, title, "", 5.5f, 0, 1, 1, 6, 0, 1, new boolean[]{true, false, false, false});
        MyApplication.api.drawLabelLine(margin,margin+8f,67,0.5f,0,1,new float[]{});

        float tableTop = 8+margin;
        float tableHeight = 32;
        
        // 两条竖线位置
        float x1 = margin+15;           
        float x2 = margin+15 + 26;      
        
        if (TEMP_DP.equals(data.template) || TEMP_MC.equals(data.template)) {
            // 4行模板 (大棚/苗床)
            float rowH = tableHeight / 4f;
            MyApplication.api.drawLabelLine(x1, tableTop, 0.5f, tableHeight, 0, 1, new float[]{});
            MyApplication.api.drawLabelLine(x2, tableTop, 0.5f, tableHeight, 0, 1, new float[]{});
            // 3行横线
            for (int i = 1; i <= 3; i++) {
                MyApplication.api.drawLabelLine(margin, tableTop + i * rowH, x2-1, 0.4f, 0, 1, new float[]{});
            }
            
            String[] labels;
            if (TEMP_DP.equals(data.template)) {
                labels = new String[]{"自编码", "种植园", "面积", "负责人"};
            } else {
                labels = new String[]{"自编码", "大棚", "种植园", "负责人"};
            }
            
            String[] values = {data.f1, data.f2, data.f3, data.f4};
            for (int i = 0; i < 4; i++) {
                float y = tableTop + i * rowH ;
                MyApplication.api.drawLabelText(margin+1, y, 14, rowH, labels[i], "", 3.5f, 0, 0, 1, 6, 0, 1, new boolean[]{false, false, false, false});
                MyApplication.api.drawLabelText(x1+1, y, 25, rowH, values[i], "", 3.0f, 0, 0, 1, 6, 0, 1, new boolean[]{false, false, false, false});
            }
        } else if (TEMP_DK.equals(data.template)) {
            // 5行模板 (地块)
            float rowH = tableHeight / 5f;
            MyApplication.api.drawLabelLine(x1, tableTop, 0.5f, tableHeight, 0, 1, new float[]{});
            MyApplication.api.drawLabelLine(x2, tableTop, 0.5f, tableHeight, 0, 1, new float[]{});
            // 4行横线
            for (int i = 1; i <= 4; i++) {
                MyApplication.api.drawLabelLine(margin, tableTop + i * rowH, x2-1, 0.4f, 0, 1, new float[]{});
            }
            String[] labels = {"自编码", "种植园", "长×宽", "面积", "负责人"};
            String[] values = {data.f1, data.f2, data.f3, data.f4, data.f5};
            for (int i = 0; i < 5; i++) {
                float y = tableTop + i * rowH ;
                MyApplication.api.drawLabelText(margin+1, y, 14, rowH, labels[i], "", 3.5f, 0, 0, 1, 6, 0, 1, new boolean[]{false, false, false, false});
                MyApplication.api.drawLabelText(x1+1, y, 25, rowH, values[i], "", 3.0f, 0, 0, 1, 6, 0, 1, new boolean[]{false, false, false, false});
            }
        } else {
            // 6行模板 (苗木/加工/产成品)
            float rowH = tableHeight / 6f;
            MyApplication.api.drawLabelLine(x1, tableTop, 0.5f, tableHeight, 0, 1, new float[]{});
            MyApplication.api.drawLabelLine(x2, tableTop, 0.5f, tableHeight, 0, 1, new float[]{});
            // 5行横线
            for (int i = 1; i <= 5; i++) {
                MyApplication.api.drawLabelLine(margin, tableTop + i * rowH, x2-1, 0.4f, 0, 1, new float[]{});
            }
            String[] labels;
            if (TEMP_MM.equals(data.template)) {
                labels = new String[]{"品种", "代数", "育苗方法", "地块", "母树", "定植日期"};
            } else if (TEMP_CJG.equals(data.template)) {
                labels = new String[]{"名称", "型号/规格", "数量/重量", "等级", "完工时间", "操作员ID"};
            } else {
                labels = new String[]{"分类名称", "型号/规格", "数量/重量", "等级", "完工时间", "操作员ID"};
            }
            String[] values = {data.f1, data.f2, data.f3, data.f4, data.f5, data.f6};

            for (int i = 0; i < 6; i++) {
                float y = tableTop + i * rowH ;
                MyApplication.api.drawLabelText(margin+1, y, 14, rowH, labels[i], "", 3.5f, 0, 0, 1, 6, 0, 1, new boolean[]{false, false, false, false});
                MyApplication.api.drawLabelText(x1+1, y, 25, rowH, values[i], "", 3.0f, 0, 0, 1, 6, 0, 1, new boolean[]{false, false, false, false});
            }
        }

        //二维码绘制
        MyApplication.api.drawLabelQrCode(x2+2, tableTop+5, 22, 22, data.traceCode, 31, 0);
        //底部编码
        MyApplication.api.drawLabelLine(margin, 40+margin, 67, 0.5f, 0, 1, new float[]{});
        MyApplication.api.drawLabelText(margin+1, 39+margin, 68, 8, data.traceCode, "", 2.8f, 0, 1, 1, 6, 0, 1, new boolean[]{false, false, false, false});

        byte[] jsonByte = MyApplication.api.generateLabelJson();
        String jsonStr = new String(jsonByte, java.nio.charset.StandardCharsets.UTF_8);
        String printerInfo = "{\"printerImageProcessingInfo\":{\"orientation\":0,\"margin\":[0,0,0,0],\"printQuantity\":1,\"width\":70,\"height\":50},\"epc\":\"\"}";
        List<String> dList = new ArrayList<>(); dList.add(jsonStr);
        List<String> iList = new ArrayList<>(); iList.add(printerInfo);
        MyApplication.api.commitData(dList, iList);
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
