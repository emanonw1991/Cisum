package com.vivo.emanon.cisum.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.vivo.emanon.cisum.constant.Constants;
import com.vivo.emanon.cisum.R;
import com.vivo.emanon.cisum.utils.HttpUtil;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 登陆Activity
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private AutoCompleteTextView actUsername;
    private EditText etPassword;

    private int fingerprintLimits = 5;

    private static SharedPreferences loginPrefs;
    private static SharedPreferences.Editor editor;

    private static String username;
    private static String password;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //接收登陆Message并进行处理
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        Toast.makeText(LoginActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                        if (!loginPrefs.contains(username)) {
                            editor.putString(username, "");
                            editor.apply();
                        }
                        setLogin(true);
                        break;
                    case 1:
                        Toast.makeText(LoginActivity.this, "用户名已被注册", Toast.LENGTH_SHORT).show();
                        setLogin(false);
                        break;
                    case 2:
                        Toast.makeText(LoginActivity.this, "登陆成功！", Toast.LENGTH_SHORT).show();
                        if (!loginPrefs.contains(username)) {
                            editor.putString(username, "");
                            editor.apply();
                        }
                        setLogin(true);
                        break;
                    case 3:
                        Toast.makeText(LoginActivity.this, "登陆失败， 请重试", Toast.LENGTH_SHORT)
                                .show();
                        setLogin(false);
                        break;
                }
            }
        };
        loginPrefs = getSharedPreferences("profiles", MODE_PRIVATE);
        editor = loginPrefs.edit();
        FingerprintManager fingerprintLogin = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        if (ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]
                    {Manifest.permission.USE_FINGERPRINT}, 1);
        } else {
            if (fingerprintLogin.isHardwareDetected() &&
                    fingerprintLogin.hasEnrolledFingerprints()) {
                fingerprintLogin.authenticate(null, null, 0, fingerLoginCallback, null);
            }
        }

        initViews();
    }

    private void initViews() {
        actUsername = (AutoCompleteTextView) findViewById(R.id.act_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        Button bLogin = (Button) findViewById(R.id.b_login);
        Button bRegister = (Button) findViewById(R.id.b_register);
        bLogin.setOnClickListener(this);
        bRegister.setOnClickListener(this);
        String[] profiles = loginPrefs.getAll().keySet().toArray(new String[0]);
        //AutoCompleteTextView的Adapter为一个String数组
        ArrayAdapter<String> adapter = new ArrayAdapter<>(LoginActivity.this,
                android.R.layout.simple_dropdown_item_1line, profiles);
        actUsername.setAdapter(adapter);
        actUsername.addTextChangedListener(new MyTextWatcher());
    }

    @Override
    public void onClick(View v) {
        username = actUsername.getText().toString();
        password = etPassword.getText().toString();
        switch (v.getId()) {
            case R.id.b_login:
                isLogin();
                break;
            case R.id.b_register:
                isRegister();
                break;
            default:
                break;
        }
    }

    /**
     * AutoCompleteTextView应当实现的方法
     */
    private static class MyTextWatcher implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }

    /**
     * 注册用户方法
     */
    private void isRegister() {
        String registerRequest = Constants.SERVER_HOST + "/web/Login?username=" + username +
                "&password=" + password + "&type=register";
        HttpUtil.sendOkHttpRequest(registerRequest, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String registerResponse = Objects.requireNonNull(response.body()).string();
                Message registerMessage = new Message();
                if (registerResponse.equals("register success")) {
                    registerMessage.what = 0;
                } else {
                    registerMessage.what = 1;
                }
                mHandler.sendMessage(registerMessage);
            }
        });
    }

    /**
     * 登陆事件方法
     */
    private void isLogin() {
        String loginRequest = Constants.SERVER_HOST + "/web/Login?username=" + username +
                "&password=" + password + "&type=login";
        HttpUtil.sendOkHttpRequest(loginRequest, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Message loginMessage = new Message();
                String loginResponse = Objects.requireNonNull(response.body()).string();
                Log.d("response", loginResponse);
                if (loginResponse.equals("login success")) {
                    loginMessage.what = 2;
                } else {
                    loginMessage.what = 3;
                }
                mHandler.sendMessage(loginMessage);
            }
        });
    }

    /**
     * 指纹验证回调方法
     */
    private final FingerprintManager.AuthenticationCallback fingerLoginCallback = new
            FingerprintManager.AuthenticationCallback() {
        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            Toast.makeText(LoginActivity.this, "指纹登陆成功！", Toast.LENGTH_SHORT).show();
            setLogin(true);
        }

        @Override
        public void onAuthenticationFailed() {
            if (fingerprintLimits > 1) {
                fingerprintLimits--;
                Toast.makeText(LoginActivity.this, "指纹验证失败，请重试（还剩" +
                                fingerprintLimits + "次）", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "指纹验证失败次数过多，请30秒后重试",
                        Toast.LENGTH_SHORT).show();
            }
            setLogin(false);
        }
    };

    /**
     * 设置登陆状态，若已登陆则不必再登陆
     * @param loginState 登陆状态，是否登陆
     */
    private void setLogin(boolean loginState) {
        SharedPreferences.Editor mEditor = PreferenceManager.getDefaultSharedPreferences(this)
                .edit();
        if (loginState) {
            mEditor.putBoolean("login", true);
            mEditor.apply();
            new Handler().postDelayed(() -> {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }, 500);
        } else {
            mEditor.putBoolean("login", false);
            mEditor.apply();
        }
    }

}
