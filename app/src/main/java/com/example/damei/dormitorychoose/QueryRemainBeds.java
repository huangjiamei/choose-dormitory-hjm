package com.example.damei.dormitorychoose;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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

public class QueryRemainBeds extends Activity implements View.OnClickListener{
    private String studentID = null; //学号
    private String gender = null; //性别
    private String vcode = null; //校验码
    private ImageView btn_query_remain_beds_exit; //返回按钮
    private ImageView btn_query_remain_beds_choose; //选择宿舍按钮

    private static final int QUERY_REMAIN_BEDS_SUCCESS = 1;
    private static final int QUERY_REMAIN_BEDS_FAIL= 0;

    private TextView remain_five, remain_eight, remain_nine, remain_thirteen, remain_fourteen; //定义相关控件对象
    private String five, eight, nine, thirtheen, fourteen; //相关信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_remain_beds); //加载布局

        //初始化
        remain_five = (TextView) findViewById(R.id.query_remain_beds_five);
        remain_eight = (TextView) findViewById(R.id.query_remain_beds_eight);
        remain_nine = (TextView) findViewById(R.id.query_remain_beds_nine);
        remain_thirteen = (TextView) findViewById(R.id.query_remain_beds_thirteen);
        remain_fourteen = (TextView) findViewById(R.id.query_remain_beds_fourteen);

        btn_query_remain_beds_exit = (ImageView) findViewById(R.id.query_remain_beds_btn_back);
        btn_query_remain_beds_exit.setOnClickListener(this);
        btn_query_remain_beds_choose = (ImageView) findViewById(R.id.query_remain_beds_btn_choose);
        btn_query_remain_beds_choose.setOnClickListener(this);

        //获取学号和性别
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        studentID = bundle.getString("studentID");
        gender = bundle.getString("gender");
        vcode = bundle.getString("vcode");

        getRemainBeds(gender); //获取网络数据
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.query_remain_beds_btn_back) { //退回到个人信息界面
            Intent intent = new Intent(QueryRemainBeds.this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("studentID", studentID); //传递学号
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
        if(view.getId() == R.id.query_remain_beds_btn_choose) { //跳转到选宿舍界面
            Intent intent = new Intent(QueryRemainBeds.this, Choose.class);
            Bundle bundle = new Bundle();
            bundle.putString("studentID", studentID); //传递学号
            bundle.putString("gender", gender);
            bundle.putString("vcode", vcode);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

    public void getRemainBeds(String stugender) {
        String gender = null;
        if(stugender == "女")
            gender = "2";
        else
            gender = "1";
        final String address = "https://api.mysspku.com/index.php/V1/MobileCourse/getRoom?" + "gender=" + gender;
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpsURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    //解决java.security.cert.CertPathValidatorException: Trust anchor for certification path not found.
                    //忽略https证书验证
                    trustAllHosts();
                    connection = (HttpsURLConnection) url.openConnection();
                    connection.setHostnameVerifier(DO_NOT_VERIFY);
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = bufferedReader.readLine()) != null)
                        response.append(str);
                    String responseStr = response.toString();
                    Log.d("ReaminBedsInfo", responseStr); //显示返回的信息
                    decodeRemainBedsInfoJson(responseStr); //解析返回的json数据
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(connection != null)
                        connection.disconnect();
                }
            }
        }).start();
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

    public void decodeRemainBedsInfoJson(String str) {
        try{
            JSONObject jsonObject = new JSONObject(str);
            int errcode = jsonObject.getInt("errcode");
            JSONObject jsonData = jsonObject.getJSONObject("data");
            if(errcode == 0) {
                five = jsonData.getString("5");
                eight = jsonData.getString("8");
                nine = jsonData.getString("9");
                thirtheen = jsonData.getString("13");
                fourteen = jsonData.getString("14");
            }
            Message msg = new Message();
            if(errcode == 0)
                msg.what = QUERY_REMAIN_BEDS_SUCCESS;
            else
                msg.what = QUERY_REMAIN_BEDS_FAIL;
            handler.sendMessage(msg);
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //通过消息机制，将解析到的登录返回信息，通过消息发送给主线程，主线程接收消息后进行相应操作
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case QUERY_REMAIN_BEDS_SUCCESS: //能获取到学生的信息
                    remain_five.setText(five);
                    remain_eight.setText(eight);
                    remain_nine.setText(nine);
                    remain_thirteen.setText(thirtheen);
                    remain_fourteen.setText(fourteen);
                    break;
                case QUERY_REMAIN_BEDS_FAIL: //不能获取学生信息
                    Toast.makeText(QueryRemainBeds.this, "该学生目前无信息！", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

}
