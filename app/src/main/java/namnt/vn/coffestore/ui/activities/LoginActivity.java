package namnt.vn.coffestore.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.auth.LoginRequest;
import namnt.vn.coffestore.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin, btnRegister;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // MUST be FIRST!
        try {
            setContentView(R.layout.activity_login);

            // Initialize views AFTER setContentView
            etUsername = findViewById(R.id.ernameet_us);
            etPassword = findViewById(R.id.et_password);
            btnLogin = findViewById(R.id.btn_login);
            btnRegister = findViewById(R.id.btn_register);



            // Initialize ViewModel with custom Factory for AndroidViewModel
            viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
                @NonNull
                @Override
                public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                    return (T) new AuthViewModel(getApplication());
                }
            }).get(AuthViewModel.class);

            // Login button listener
            btnLogin.setOnClickListener(v -> {
                String keyLogin = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if (!keyLogin.isEmpty() && !password.isEmpty()) {
                    LoginRequest request = new LoginRequest(keyLogin, password);
                    viewModel.login(request);
                } else {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                }
            });

            // Ensure inputs are interactive and focused initially
            etUsername.setEnabled(true);
            etUsername.setFocusable(true);
            etUsername.setFocusableInTouchMode(true);
            etPassword.setEnabled(true);
            etPassword.setFocusable(true);
            etPassword.setFocusableInTouchMode(true);
            etUsername.requestFocus();
            getWindow().setSoftInputMode(
                    android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            );

            // Register button listener (was missing)
            btnRegister.setOnClickListener(v -> {
                startActivity(new Intent(this, RegisterV2Activity.class));
            });

            // Observe auth result (fix: use Observer<AuthResult> and null check)
            viewModel.getAuthResult().observe(this, new Observer<AuthViewModel.AuthResult>() {
                @Override
                public void onChanged(AuthViewModel.AuthResult result) {
                    if (result != null) {
                        if (result.isSuccess()) {
                            Toast.makeText(LoginActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                            if (result.getTokenResponse() != null && viewModel.isTokenValid()) {
                                Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            // Check if already logged in
            if (!viewModel.getAccessToken().isEmpty() && viewModel.isTokenValid()) {
                startActivity(new Intent(this, MenuActivity.class));
                finish();
            }

            Log.d(TAG, "LoginActivity onCreate completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}