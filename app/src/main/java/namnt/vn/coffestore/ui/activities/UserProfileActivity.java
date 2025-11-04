package namnt.vn.coffestore.ui.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.auth.UserProfile;
import namnt.vn.coffestore.viewmodel.AuthViewModel;
import namnt.vn.coffestore.viewmodel.UserViewModel;
import namnt.vn.coffestore.data.repository.UserRepository;

public class UserProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView ivAvatar;
    private TextView tvHeaderName, tvFullName, tvEmail, tvPhone, tvRole;

    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Views
        btnBack     = findViewById(R.id.btnBack);
        ivAvatar    = findViewById(R.id.ivAvatar);
        tvHeaderName= findViewById(R.id.tvHeaderName);
        tvFullName  = findViewById(R.id.tvFullName);
        tvEmail     = findViewById(R.id.tvEmail);
        tvPhone     = findViewById(R.id.tvPhone);
        tvRole      = findViewById(R.id.tvRole);

        // ViewModels
        authViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication()))
                .get(AuthViewModel.class);
        userViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication()))
                .get(UserViewModel.class);

        // Back
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Observe
        userViewModel.getProfileResult().observe(this, result -> {
            if (result == null) return;

            if (result.success && result.data != null) {
                bindUser(result.data);
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });

        // Gọi API (truyền Bearer token thủ công)
        String access = authViewModel.getAccessToken();
        if (access != null && !access.isEmpty()) {
            String bearer = "Bearer " + access;
            userViewModel.fetchProfile(bearer);
        } else {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            getOnBackPressedDispatcher().onBackPressed();
        }
    }

    private void bindUser(UserProfile u) {
        String name = u.getFullName() != null ? u.getFullName() : "";
        tvHeaderName.setText(name);
        tvFullName.setText(name);
        tvEmail.setText(u.getEmail() != null ? u.getEmail() : "");
        tvPhone.setText(u.getPhoneNumber() != null ? u.getPhoneNumber() : "");
        tvRole.setText(u.getRole() != null ? u.getRole() : "");
        // TODO: ivAvatar: khi API trả URL ảnh -> dùng Glide để load
    }
}
