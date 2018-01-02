package com.example.damei.dormitorychoose;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by damei on 18/1/1.
 */

public class Choose extends Activity implements View.OnClickListener{
    private String studentID = null; //学号
    private String gender = null; //性别
    private String vcode = null; //校验码
    private String status = null; //办理状态
    private String dormitory = null; //房间号
    private ImageView btn_choose_exit; //返回按钮
    private Button btn_choose_submit; //提交按钮

    private Spinner persons, buildings; //可选的办理人数及楼号
    //private TextView personsView, buildingsView;
    private static String personsStr, buildingsStr;
    private static final String[] personsArr = {"一人", "两人", "三人", "四人"}; //使用数组作为数据源
    private static final String[] buildingsArr = {"5号楼", "8号楼", "9号楼", "13号楼", "14号楼"};
    private ArrayAdapter<String> personsAdapter, buildingsAdapter;

    private EditText roomate1_stuid, roomate1_vcode, roomate2_stuid, roomate2_vcode, roomate3_sutid, roomate3_vcode;

    private static final int CHOOSE_SUCCESS = 1;
    private static final int CHOOSE_FAIL = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose); //加载布局

        //初始化
        btn_choose_exit = (ImageView) findViewById(R.id.choose_back);
        btn_choose_exit.setOnClickListener(this);
        btn_choose_submit = (Button) findViewById(R.id.choose_submit);
        btn_choose_submit.setOnClickListener(this);

        roomate1_stuid = (EditText) findViewById(R.id.roommate1_stuid);
        roomate1_vcode = (EditText) findViewById(R.id.roommate1_vcode);
        roomate2_stuid = (EditText) findViewById(R.id.roommate2_stuid);
        roomate2_vcode = (EditText) findViewById(R.id.roommate2_vcode);
        roomate3_sutid = (EditText) findViewById(R.id.roommate3_stuid);
        roomate3_vcode = (EditText) findViewById(R.id.roommate3_vcode);

        //用适配器给Spinner添加数据，实现下拉列表
        persons = (Spinner) findViewById(R.id.spinner_choose_persons);
        //personsView = (TextView) findViewById(R.id.choose_persons);
        personsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, personsArr); //将可选内容与ArrayAdapter连接起来
        personsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //设置下拉列表风格
        persons.setAdapter(personsAdapter); //将adapter添加到spinner中
        persons.setOnItemSelectedListener(new PersonsSelectedListener()); //添加spinner事件监听器
        persons.setVisibility(View.VISIBLE); //设置默认值

        buildings = (Spinner) findViewById(R.id.spinner_choose_bulidings);
        //buildingsView = (TextView) findViewById(R.id.choose_buildings);
        buildingsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, buildingsArr);
        buildingsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        buildings.setAdapter(buildingsAdapter);
        buildings.setOnItemSelectedListener(new BuildingsSelectedListener());
        buildings.setVisibility(View.VISIBLE);

        //获取学号和性别
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        studentID = bundle.getString("studentID");
        gender = bundle.getString("gender");
        vcode = bundle.getString("vcode");

        //System.out.println(personsStr);
        //System.out.println(buildingsStr);
        //Log.d("personsStr", personsStr);
        //Log.d("buildingsStr", buildingsStr);
    }

    class PersonsSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            //personsView.setText(personsArr[arg2]);
            personsStr = personsArr[arg2];
            //System.out.println(personsArr[arg2]);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    class BuildingsSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            //personsView.setText(buildingsArr[arg2]);
            buildingsStr = buildingsArr[arg2];
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }



    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.choose_back) { //退回到查询剩余床位界面
            Intent intent = new Intent(Choose.this, QueryRemainBeds.class);
            Bundle bundle = new Bundle();
            bundle.putString("studentID", studentID); //传递学号
            bundle.putString("gender",gender); //传递性别
            bundle.putString("vcode", vcode); //传递校验码
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
        if(view.getId() == R.id.choose_submit) {
            allocate();
            onChoose();
        }
    }

    public void allocate() {
        if(buildingsStr == "5号楼")
            dormitory = "5xxx";
        if(buildingsStr == "8号楼")
            dormitory = "8xxx";
        if(buildingsStr == "9号楼")
            dormitory = "9xxx";
        if(buildingsStr == "13号楼")
            dormitory = "13xx";
        if(buildingsStr == "14号楼")
            dormitory = "14xx";
    }
    public void onChoose() {
        final String address = "https://api.mysspku.com/index.php/V1/MobileCourse/SelectRoom";
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("persons", personsStr);
            jsonObject.put("buildings", buildingsStr);
            jsonObject.put("stuid", studentID);
            jsonObject.put("vcode", vcode);
            jsonObject.put("r1id", roomate1_stuid);
            jsonObject.put("r1code", roomate1_vcode);
            jsonObject.put("r2id", roomate2_stuid);
            jsonObject.put("r2code", roomate2_vcode);
            jsonObject.put("r3id", roomate3_sutid);
            jsonObject.put("r3code", roomate3_vcode);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpsURLConnection con = null;
                    try {
                        URL url = new URL(address);
                        trustAllHosts();
                        con = (HttpsURLConnection) url.openConnection();
                        con.setHostnameVerifier(DO_NOT_VERIFY);
                        con.setDoInput(true);
                        con.setDoOutput(true);
                        con.setUseCaches(false);
                        con.setRequestMethod("POST");
                        con.setRequestProperty("Content-Type","application/json");
                        con.connect();
                        con.setConnectTimeout(8000);
                        con.setReadTimeout(8000);

                        DataOutputStream out = new DataOutputStream(con.getOutputStream());
                        out.writeBytes(jsonObject.toString());
                        out.flush();
                        out.close();

                        InputStream in = con.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder response = new StringBuilder();
                        String str;
                        while ((str = reader.readLine()) != null) {
                            response.append(str);
                            Log.d("chooseInfo", str);
                        }
                        String responseStr = response.toString();
                        decodeChooseJson(responseStr); //解析返回的json数据
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (con != null) {
                            con.disconnect();
                        }
                    }
                }
            }).start();
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        // Android use X509 cert
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        } };
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public void decodeChooseJson(String str) {
        try{
            JSONObject jsonObject = new JSONObject(str);
            int errcode = jsonObject.getInt("errcode");
            Message msg = new Message();
            if(errcode == 0)
                msg.what = CHOOSE_SUCCESS;
            else
                msg.what = CHOOSE_FAIL;
            handler.sendMessage(msg);
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg){
            switch (msg.what){
                case CHOOSE_SUCCESS:
                    status = "已办理";
                    Intent intent = new Intent(Choose.this, MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("studentID", studentID); //传递学号
                    bundle.putString("gender",gender); //传递性别
                    bundle.putString("vcode", vcode); //传递校验码
                    bundle.putString("status", status); //传递办理状态
                    bundle.putString("dormitory", dormitory); //传递房间号
                    bundle.putString("building", buildingsStr); //传递宿舍楼号
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                    //Toast.makeText(Choose.this, "提交成功！", Toast.LENGTH_LONG).show();
                    break;
                case CHOOSE_FAIL:
                    Toast.makeText(Choose.this, "提交失败！", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };
}
