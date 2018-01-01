package com.example.damei.dormitorychoose;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.BufferUnderflowException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by damei on 17/12/29.
 */

public class MainActivity extends Activity implements View.OnClickListener{
    private String studentID = null; //学号

    private ImageView btn_main_exit; //退出按钮

    private TextView stuID, stuName, stuGender, status, buildingNum, dormitoryNum, vcode; //定义相关控件对象

    private String stuid, stuname, stugender, Status, buildingnum, dormitorynum, Vcode; //相关信息

    private static final int UPDATE_STUINFO_SUCCESS = 1;
    private static final int UPDATE_STUINFO_FAIL = 0;
    private static final int GET_STUINFO_FAIL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main); //加载布局

        //初始化
        stuID = (TextView) findViewById(R.id.main_stuid);
        stuName = (TextView) findViewById(R.id.main_stuname);
        stuGender = (TextView) findViewById(R.id.main_gender);
        status = (TextView) findViewById(R.id.main_status);
        buildingNum = (TextView) findViewById(R.id.main_buildingnum);
        dormitoryNum = (TextView) findViewById(R.id.main_dormitorynum);
        vcode = (TextView) findViewById(R.id.main_vcode);

        btn_main_exit = (ImageView) findViewById(R.id.main_btn_back);
        btn_main_exit.setOnClickListener(this);

        //获取学号
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        studentID = bundle.getString("studentID");
        //studentID = intent.getStringExtra("studentID");
        //System.out.println(studentID);
        getStuInfoJson(studentID); //获取网络数据
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.main_btn_back) { //退回到登录界面
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        }
    }

    public void getStuInfoJson(String stuid) {
        final String address = "https://api.mysspku.com/index.php/V1/MobileCourse/getDetail?" + "stuid=" + stuid;
        System.out.println(address);
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
                    Log.d("StudentDomitoryInfo", responseStr); //显示返回的信息
                    decodeStuInfoJson(responseStr); //解析返回的json数据
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

    public void decodeStuInfoJson(String str) {
        try{
            JSONObject jsonObject = new JSONObject(str);
            int errcode = jsonObject.getInt("errcode");
            JSONObject jsonData = jsonObject.getJSONObject("data");
            if(errcode != 40001) { //学号不存在的返回信息
                stuid = jsonData.getString("studentid");
                stuname = jsonData.getString("name");
                stugender = jsonData.getString("gender");
                Vcode = jsonData.getString("vcode");
                if (Integer.parseInt(stuid) % 2 == 0) { //学号为偶数的学生办理成功
                    Status = "已办理";
                    buildingnum = jsonData.getString("building");
                    dormitorynum = jsonData.getString("room");
                } else {
                    Status = "未办理";
                    buildingnum = "未办理";
                    dormitorynum = "未办理";
                }
            }
            Message msg = new Message();
            if(errcode == 40001)
                msg.what = GET_STUINFO_FAIL;
            else if(errcode == 0)
                msg.what = UPDATE_STUINFO_SUCCESS;
            else
                msg.what = UPDATE_STUINFO_FAIL;
            handler.sendMessage(msg);
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //通过消息机制，将解析到的登录返回信息，通过消息发送给主线程，主线程接收消息后进行相应操作
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case UPDATE_STUINFO_SUCCESS: //能获取到学生的信息
                    stuID.setText(stuid);
                    stuName.setText(stuname);
                    stuGender.setText(stugender);
                    status.setText(Status);
                    buildingNum.setText(buildingnum);
                    dormitoryNum.setText(dormitorynum);
                    vcode.setText(Vcode);
                    break;
                case UPDATE_STUINFO_FAIL: //不能获取学生信息
                    Toast.makeText(MainActivity.this, "该学生目前无信息！", Toast.LENGTH_LONG).show();
                    break;
                case GET_STUINFO_FAIL: //不能获取学生信息
                    Toast.makeText(MainActivity.this, "该学号不存在！", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

}

