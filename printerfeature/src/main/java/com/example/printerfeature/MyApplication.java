package com.example.printerfeature;

import com.gengcon.www.jcprintersdk.JCPrintApi;
import com.gengcon.www.jcprintersdk.callback.Callback;

public class MyApplication extends android.app.Application {
    public static MyApplication instance;
    public static JCPrintApi api;
    public static String printerName = "未连接";

    @Override
    public void onCreate(){
        super.onCreate();
        instance = this;
        // 1. 初始化全局回调
        Callback callback = new Callback() {
            @Override
            public void onConnectSuccess(String address, int type) {
                // 连接成功回调
                if (printerName.equals("未连接")) {
                    printerName = "已连接设备";
                }
            }
            @Override
            public void onDisConnect() {
                // 断开连接回调
                // 断开连接时，恢复状态
                printerName = "未连接";
            }
            // ... 需要重写其他接口方法（如 onElectricityChange, onPaperStatus 等），保持空实现即可
            @Override
            public void onElectricityChange(int powerLevel) {}
            @Override
            public void onCoverStatus(int coverStatus) {}
            @Override
            public void onPaperStatus(int paperStatus) {}
            @Override
            public void onRfidReadStatus(int rfidReadStatus) {}
            @Override
            public void onRibbonRfidReadStatus(int ribbonRfidReadStatus) {}
            @Override
            public void onRibbonStatus(int ribbonStatus) {}
            @Override
            public void onFirmErrors() {}
        };

        // 2. 获取 API 实例
        api = JCPrintApi.getInstance(callback);
        // 3. 初始化 SDK
        api.initSdk(this);
        // 4. 初始化图像库
        api.initDefaultImageLibrarySettings("", "");
    }
}
