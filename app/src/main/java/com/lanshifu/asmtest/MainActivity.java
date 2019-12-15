package com.lanshifu.asmtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

//插桩检测耗时
//https://github.com/zhengcx/MethodTraceMan/blob/master/README.md

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testSleep();
    }

    private void testSleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
