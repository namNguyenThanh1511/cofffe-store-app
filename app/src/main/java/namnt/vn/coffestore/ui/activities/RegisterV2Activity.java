package namnt.vn.coffestore.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.utils.NotificationHelper;

public class RegisterV2Activity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPhone, etPassword;
    private MaterialButton btnRegister;
    private ImageButton btnBack;

    // Local store keys (chỉ dùng test)
    private static final String PREFS_NAME = "coffeestore_prefs";
    private static final String PREF_KEY_EXPECTED_OTP = "expected_otp";
    private static final String PREF_KEY_OTP_EXPIRES = "expected_otp_expires_ms";
    private static final String PREF_KEY_PASSWORD_TEMP = "password_temp";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_v2);

        etFullName = findViewById(R.id.et_fullname);
        etEmail    = findViewById(R.id.et_email);
        etPhone    = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);
        btnBack     = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            String fullName = safe(etFullName);
            String email    = safe(etEmail);
            String phone    = safe(etPhone);
            String password = safe(etPassword);

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email)
                    || TextUtils.isEmpty(phone) || password.length() < 6) {
                Toast.makeText(this, getString(R.string.rv2_err_required), Toast.LENGTH_SHORT).show();
                return;
            }

            // 1) Sinh OTP 6 số local
            String otp = String.format(Locale.getDefault(), "%06d", new Random().nextInt(1_000_000));

            // 2) Hạn OTP 5 phút
            long expiresAtMs = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);

            // 3) Lưu local để OtpActivity đọc
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit()
                    .putString(PREF_KEY_EXPECTED_OTP, otp)
                    .putLong(PREF_KEY_OTP_EXPIRES, expiresAtMs)
                    .putString(PREF_KEY_PASSWORD_TEMP, password)
                    .apply();

            // 4) Gửi notification cục bộ (dùng helper hiện có)
            NotificationHelper.showOtpNotification(
                    this,
                    otp,
                    expiresAtMs,
                    !TextUtils.isEmpty(email) ? email : phone
            );

            // 5) Điều hướng sang OTP
            Intent it = new Intent(this, OtpActivity.class);
            it.putExtra("name", fullName);
            it.putExtra("email", email);
            it.putExtra("phone", phone);
            it.putExtra("identifier", !TextUtils.isEmpty(email) ? email : phone);
            startActivity(it);
        });
    }

    private String safe(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
