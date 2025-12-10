package com.yaned.final_2025merino;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yaned.final_2025merino.api.ApiRepository;
import com.yaned.final_2025merino.api.dto.LoginResponse;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.input_username);
        passwordInput = findViewById(R.id.input_password);
        Button loginButton = findViewById(R.id.btn_login);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user = usernameInput.getText().toString().trim();
                final String pass = passwordInput.getText().toString().trim();

                if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pass)) {
                    Toast.makeText(LoginActivity.this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Ejecutar login en background
                new Thread(() -> {
                    try {
                        ApiRepository api = new ApiRepository();
                        LoginResponse resp = api.login(user, pass);
                        runOnUiThread(() -> {
                            if (resp != null && resp.success) {
                                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                intent.putExtra("username", resp.nombre);
                                startActivity(intent);
                                finish();
                            } else {
                                String m = (resp != null && resp.msg != null) ? resp.msg : "Credenciales inválidas";
                                Toast.makeText(LoginActivity.this, m, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }).start();
            }
        });
    }
}
