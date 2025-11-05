package namnt.vn.coffestore.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.UserProfile;
import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.network.ApiService;
import namnt.vn.coffestore.network.RetrofitClient;
import namnt.vn.coffestore.viewmodel.AuthViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private ImageView btnBack;
    private TextView tvProfileName, tvProfileEmail, tvProfilePhone, tvProfileRole;
    
    private ApiService apiService;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        initAuthViewModel();
        setupClickListeners();
        loadUserProfile();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileRole = findViewById(R.id.tvProfileRole);
    }

    private void initAuthViewModel() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadUserProfile() {
        String accessToken = authViewModel.getAccessToken();
        if (accessToken.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String bearerToken = "Bearer " + accessToken;
        Call<ApiResponse<UserProfile>> call = apiService.getUserProfile(bearerToken);
        call.enqueue(new Callback<ApiResponse<UserProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfile>> call, Response<ApiResponse<UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserProfile profile = response.body().getData();
                    if (profile != null) {
                        displayUserProfile(profile);
                        Log.d(TAG, "User profile loaded successfully");
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to load user profile");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserProfile>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading user profile: " + t.getMessage());
            }
        });
    }

    private void displayUserProfile(UserProfile profile) {
        // Display full name
        if (profile.getFullName() != null && !profile.getFullName().isEmpty()) {
            tvProfileName.setText(profile.getFullName());
        } else {
            tvProfileName.setText("Chưa có tên");
        }

        // Display email
        if (profile.getEmail() != null && !profile.getEmail().isEmpty()) {
            tvProfileEmail.setText(profile.getEmail());
        } else {
            tvProfileEmail.setText("Chưa có email");
        }

        // Display phone number
        if (profile.getPhoneNumber() != null && !profile.getPhoneNumber().isEmpty()) {
            tvProfilePhone.setText(profile.getPhoneNumber());
        } else {
            tvProfilePhone.setText("Chưa có số điện thoại");
        }

        // Display role (convert to Vietnamese)
        String role = profile.getRole();
        String roleDisplay;
        if (role != null) {
            switch (role.toLowerCase()) {
                case "customer":
                    roleDisplay = "Khách hàng";
                    break;
                case "admin":
                    roleDisplay = "Quản trị viên";
                    break;
                case "staff":
                    roleDisplay = "Nhân viên";
                    break;
                default:
                    roleDisplay = role;
            }
            tvProfileRole.setText(roleDisplay);
        } else {
            tvProfileRole.setText("Chưa có vai trò");
        }
    }
}
