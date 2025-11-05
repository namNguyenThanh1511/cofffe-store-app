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

public class UserProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView ivAvatar;
    private TextView tvFullName, tvEmail, tvPhone, tvRole;

    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Views (không còn tvHeaderName)
        btnBack    = findViewById(R.id.btnBack);
        ivAvatar   = findViewById(R.id.ivAvatar);
        tvFullName = findViewById(R.id.tvFullName);
        tvEmail    = findViewById(R.id.tvEmail);
        tvPhone    = findViewById(R.id.tvPhone);
        tvRole     = findViewById(R.id.tvRole);

        // ViewModels
        ViewModelProvider.AndroidViewModelFactory factory =
                new ViewModelProvider.AndroidViewModelFactory(getApplication());
        authViewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);
        userViewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);

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

        // Gọi API
        String access = authViewModel.getAccessToken();
        if (access != null && !access.isEmpty()) {
            userViewModel.fetchProfile("Bearer " + access);
        } else {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            getOnBackPressedDispatcher().onBackPressed();
        }
    }

    private void bindUser(UserProfile u) {
        String name = u.getFullName() != null ? u.getFullName() : "";
        tvFullName.setText(name);
        tvEmail.setText(u.getEmail() != null ? u.getEmail() : "");
        tvPhone.setText(u.getPhoneNumber() != null ? u.getPhoneNumber() : "");
        tvRole.setText(u.getRole() != null ? u.getRole() : "");

        // Nếu sau này API trả URL ảnh, dùng Glide/Picasso để load vào ivAvatar.
        // Glide.with(this).load(u.getAvatarUrl()).placeholder(R.drawable.ic_avt_user).into(ivAvatar);
    }
}
