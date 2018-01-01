package com.example.damei.dormitorychoose;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by damei on 18/1/1.
 */

public class Choose extends Activity implements View.OnClickListener{
    private String studentID = null; //学号
    private String gender = null; //性别
    private ImageView btn_choose_exit; //返回按钮
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose); //加载布局

        //初始化
        btn_choose_exit = (ImageView) findViewById(R.id.choose_back);
        btn_choose_exit.setOnClickListener(this);

        //获取学号和性别
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        studentID = bundle.getString("studentID");
        gender = bundle.getString("gender");
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.choose_back) { //退回到查询剩余床位界面
            Intent intent = new Intent(Choose.this, QueryRemainBeds.class);
            Bundle bundle = new Bundle();
            bundle.putString("studentID", studentID); //传递学号
            bundle.putString("gender",gender);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

}
