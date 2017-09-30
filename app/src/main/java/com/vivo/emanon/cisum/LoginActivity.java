package com.vivo.emanon.cisum;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private AutoCompleteTextView actUsername;
    private EditText etPassword;
    private ImageView ivPasswordVisibility;
    private CheckBox cbRememberPassword;
    private Button bLogin;

    private boolean registerFlag;
    private boolean loginFlag;

    private SharedPreferences loginPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();

        loginPrefs = getSharedPreferences("profiles", MODE_PRIVATE);
    }

    private void initViews() {
        actUsername = (AutoCompleteTextView) findViewById(R.id.act_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        ivPasswordVisibility = (ImageView) findViewById(R.id.iv_password_visibility);
        cbRememberPassword = (CheckBox) findViewById(R.id.cb_remember_password);
        bLogin = (Button) findViewById(R.id.b_login);
        ivPasswordVisibility.setOnClickListener(this);
        bLogin.setOnClickListener(this);
        cbRememberPassword.setChecked(false);
        String[] profiles = new String[loginPrefs.getAll().size()];
        profiles = loginPrefs.getAll().keySet().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(LoginActivity.this,
                android.R.layout.simple_dropdown_item_1line, profiles);
        actUsername.setAdapter(adapter);
        actUsername.addTextChangedListener(new MyTextWatcher());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_password_visibility:
                if (etPassword.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                    ivPasswordVisibility.setImageResource(R.mipmap.ic_launcher);
                    etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else if (etPassword.getInputType() == InputType.
                        TYPE_TEXT_VARIATION_VISIBLE_PASSWORD){
                    ivPasswordVisibility.setImageResource(R.mipmap.ic_launcher_round);
                    etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                break;
            case R.id.b_login:
                boolean rememberPassword = cbRememberPassword.isChecked();
                String username = actUsername.getText().toString();
                String password = etPassword.getText().toString();
                SharedPreferences.Editor editor = loginPrefs.edit();
                isRegister(username, password);
                if (registerFlag) {

                } else {

                }
                break;
            default:
                break;
        }
    }

    private class MyTextWatcher implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            etPassword.setText(loginPrefs.getString(actUsername.getText().toString(), ""));
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }

    //有问题
    private void isRegister(String username, String password) {
        String registerRequest = Constants.SERVER_HOST + "/web/Login?username=" + username +
                "&password=" + password + "&type=register";
        HttpUtil.sendOkHttpRequest(registerRequest, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String registerResponse = response.body().toString();
                if (registerResponse.equals("register success")) {
                    registerFlag = true;
                } else {
                    registerFlag = false;
                }
            }
        });
    }

    private void isLogin(String username, String password) {
        String loginRequest = Constants.SERVER_HOST + "/web/Login?username=" + username +
                "&password=" + password + "&type=login";
        HttpUtil.sendOkHttpRequest(loginRequest, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String loginResponse = response.body().toString();
                if (loginResponse.equals("login success")) {
                    loginFlag = true;
                } else {
                    loginFlag = false;
                }
            }
        });
    }
}
