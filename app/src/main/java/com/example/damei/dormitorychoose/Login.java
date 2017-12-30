package com.example.damei.dormitorychoose;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.damei.util.NetUtil;

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
 * Created by damei on 17/12/29.
 */

public class Login extends Activity implements View.OnClickListener{
    private Button btn_login_submit; //登录按钮
    private EditText txt_login_usr; //用户名
    private EditText txt_login_pwd; //密码

    private static final int LOGIN_SUCCESS_MESSAGE = 0;
    private static final int LOGIN_FAIL_MESSAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); //加载布局

        btn_login_submit = (Button) findViewById(R.id.login_submit);
        btn_login_submit.setOnClickListener(this); //为提交按钮添加单击事件

        txt_login_usr = (EditText) findViewById(R.id.login_usr);
        txt_login_pwd = (EditText) findViewById(R.id.login_pwd);

    }

    //单击事件
    @Override
    public void onClick(View view) {
        //单击事件调用登录函数
        if(view.getId() == R.id.login_submit) {
            //调用getNetworkState()方法检查网络状态
            if(NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE)
                onLogin();
            else
                Toast.makeText(Login.this, "网络挂了！请先连接网络！", Toast.LENGTH_LONG).show();
        }
    }

    public void onLogin() {
        String username = txt_login_usr.getText().toString();
        String password = txt_login_pwd.getText().toString();
        if(username.equals("") || password.equals("")) {
            Toast.makeText(Login.this, "用户名和密码不能为空！", Toast.LENGTH_LONG).show();
            return;
        }
        System.out.println("123");

        //获取网络数据
        final String address = "https://api.mysspku.com/index.php/V1/MobileCourse/Login?"
                + "username=" + username + "&password=" + password;
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
                    decodeLoginJson(responseStr); //解析返回的json数据
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

    //解析JSON数据
    public void decodeLoginJson(String jsonData) {
        try{
            JSONObject jsonObject = new JSONObject(jsonData);
            int errcode = jsonObject.getInt("errcode");
            Message msg = new Message();
            if(errcode == 0)
                msg.what = LOGIN_SUCCESS_MESSAGE;
            else
                msg.what = LOGIN_FAIL_MESSAGE;
            handler.sendMessage(msg);
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //通过消息机制，将解析到的登录返回信息，通过消息发送给主线程，主线程接收消息后进行相应操作
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case LOGIN_SUCCESS_MESSAGE: //登录成功，则跳转到主界面，即查询个人信息界面
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case LOGIN_FAIL_MESSAGE:
                    Toast.makeText(Login.this, "用户名或密码错误！", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

}
