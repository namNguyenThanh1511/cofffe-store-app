package namnt.vn.coffestore.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;  // Cho Spinner Role nếu cần
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.auth.RegisterRequest;
import namnt.vn.coffestore.viewmodel.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private TextInputEditText etFullName, etEmail, etPhone, etPassword;
    private MaterialButton btnRegister;
    private Spinner spRole;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        etFullName = findViewById(R.id.et_fullname);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        spRole = findViewById(R.id.sp_role);
        btnRegister = findViewById(R.id.btn_register);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(Class<T> modelClass) {
                return (T) new AuthViewModel(getApplication());
            }
        }).get(AuthViewModel.class);

        // Setup Spinner for Role
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this,
                R.array.roles, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(roleAdapter);

        // Register button listener (FIX: Map String to int for role)
        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String roleStr = spRole.getSelectedItem().toString();  // "Customer" hoặc "Admin"

            // Validation
            if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.length() < 6) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ và đúng định dạng (mật khẩu ≥6 ký tự)", Toast.LENGTH_SHORT).show();
                return;
            }

            // FIX: Map String to int (giả sử Customer=1, Admin=2 – kiểm tra enum .NET nếu khác)
            int roleInt;
            switch (roleStr) {
                case  "Staff" :
                    roleInt = 1;
                    break;
                case "Customer":
                    roleInt = 2;
                    break;
                case "Admin":
                    roleInt = 0;
                    break;
                default:
                    Toast.makeText(this, "Vai trò không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
            }

            RegisterRequest request = new RegisterRequest(fullName, email, phone, password, roleInt);
            viewModel.register(request);
        });

        // Observe auth result
        viewModel.getAuthResult().observe(this, new Observer<AuthViewModel.AuthResult>() {
            @Override
            public void onChanged(AuthViewModel.AuthResult result) {
                if (result != null) {
                    if (result.isSuccess()) {
                        Toast.makeText(RegisterActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        Log.d(TAG, "RegisterActivity onCreate completed");
    }
}
