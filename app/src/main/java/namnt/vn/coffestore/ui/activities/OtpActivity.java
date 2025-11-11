package namnt.vn.coffestore.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.auth.RegisterRequest;
import namnt.vn.coffestore.utils.NotificationHelper;
import namnt.vn.coffestore.viewmodel.AuthViewModel;

public class OtpActivity extends AppCompatActivity {

    private ImageView ivBack;
    private EditText et1, et2, et3, et4, et5, et6;
    private Button btnVerify;
    private TextView tvHelp, tvResendCountdown;

    private AuthViewModel authViewModel;

    // Keys dùng chung với RegisterV2Activity (local test)
    private static final String PREFS_NAME = "coffeestore_prefs";
    private static final String PREF_KEY_EXPECTED_OTP = "expected_otp";
    private static final String PREF_KEY_OTP_EXPIRES = "expected_otp_expires_ms";
    private static final String PREF_KEY_PASSWORD_TEMP = "password_temp";

    private String expectedOtp;
    private long expectedExpiresAt;

    // Đếm ngược resend
    private CountDownTimer resendTimer;
    private static final long RESEND_COOLDOWN_MS = 60_000L; // 60s

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        bindViews();
        setupClicks();
        addOtpTextWatchers();
        updateVerifyEnabled(); // khởi tạo trạng thái nút

        // ViewModel init (giống RegisterActivity cũ)
        authViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new AuthViewModel(getApplication());
            }
        }).get(AuthViewModel.class);

        // Observe kết quả đăng ký
        authViewModel.getAuthResult().observe(this, result -> {
            if (result == null) return;
            if (result.isSuccess() && result.getTokenResponse() == null) {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else if (!result.isSuccess()) {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Đọc OTP local
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        expectedOtp = prefs.getString(PREF_KEY_EXPECTED_OTP, null);
        expectedExpiresAt = prefs.getLong(PREF_KEY_OTP_EXPIRES, 0L);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // bắt đầu đếm ngược cho phần resend
        startResendCountdown(RESEND_COOLDOWN_MS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (resendTimer != null) resendTimer.cancel();
    }

    // ------------------ UI binding ------------------
    private void bindViews() {
        ivBack = findViewById(R.id.ivBack);
        et1 = findViewById(R.id.et1);
        et2 = findViewById(R.id.et2);
        et3 = findViewById(R.id.et3);
        et4 = findViewById(R.id.et4);
        et5 = findViewById(R.id.et5);
        et6 = findViewById(R.id.et6);
        btnVerify = findViewById(R.id.btnVerify);
        tvHelp = findViewById(R.id.tvHelp);
        tvResendCountdown = findViewById(R.id.tvResendCountdown);
    }

    // ------------------ Click handlers ------------------
    private void setupClicks() {
        ivBack.setOnClickListener(v -> finish());

        btnVerify.setOnClickListener(v -> {
            String input = collectOtpInput();
            if (input.length() != 6) {
                Toast.makeText(this, "Vui lòng nhập đủ 6 chữ số", Toast.LENGTH_SHORT).show();
                return;
            }

            long now = System.currentTimeMillis();
            if (expectedExpiresAt > 0 && now > expectedExpiresAt) {
                Toast.makeText(this, getString(R.string.otp_expired), Toast.LENGTH_SHORT).show();
                return;
            }

            if (expectedOtp == null) {
                Toast.makeText(this, "Chưa có OTP local. Hãy thử gửi lại từ Register.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!input.equals(expectedOtp)) {
                Toast.makeText(this, getString(R.string.otp_mismatch), Toast.LENGTH_SHORT).show();
                return;
            }

            // OTP đúng -> gọi register API (giống RegisterActivity cũ)
            String fullName = getIntent().getStringExtra("name");
            String email    = getIntent().getStringExtra("email");
            String phone    = getIntent().getStringExtra("phone");

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String password = prefs.getString(PREF_KEY_PASSWORD_TEMP, "");

            // role: giữ mặc định 2 (không hiển thị ở UI)
            RegisterRequest request = new RegisterRequest(fullName, email, phone, password, 0);
            authViewModel.register(request);

            // Dọn dữ liệu tạm
            prefs.edit()
                    .remove(PREF_KEY_EXPECTED_OTP)
                    .remove(PREF_KEY_OTP_EXPIRES)
                    .remove(PREF_KEY_PASSWORD_TEMP)
                    .apply();
        });

        // Click "Didn't receive?" hoặc text đếm ngược để resend khi được phép
        var resendClick = (android.view.View.OnClickListener) v -> {
            boolean canResend = tvResendCountdown.getTag() instanceof Boolean
                    && (Boolean) tvResendCountdown.getTag();
            if (canResend) {
                resendOtpLocal();
            } else {
                Toast.makeText(this, "Đợi hết thời gian đếm ngược để gửi lại", Toast.LENGTH_SHORT).show();
            }
        };
        tvHelp.setOnClickListener(resendClick);
        tvResendCountdown.setOnClickListener(resendClick);
    }

    // ------------------ OTP input helpers ------------------
    private void addOtpTextWatchers() {
        TextWatcher tw = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                moveFocusForward();   // tự chuyển focus khi nhập
                updateVerifyEnabled(); // bật/tắt nút Verify
            }
        };
        et1.addTextChangedListener(tw);
        et2.addTextChangedListener(tw);
        et3.addTextChangedListener(tw);
        et4.addTextChangedListener(tw);
        et5.addTextChangedListener(tw);
        et6.addTextChangedListener(tw);
    }

    private void moveFocusForward() {
        if (et1.getText().length() < 1) { et1.requestFocus(); return; }
        if (et2.getText().length() < 1) { et2.requestFocus(); return; }
        if (et3.getText().length() < 1) { et3.requestFocus(); return; }
        if (et4.getText().length() < 1) { et4.requestFocus(); return; }
        if (et5.getText().length() < 1) { et5.requestFocus(); return; }
        et6.requestFocus();
    }

    private void updateVerifyEnabled() {
        boolean ok = collectOtpInput().length() == 6;
        btnVerify.setEnabled(ok);
        btnVerify.setAlpha(ok ? 1f : 0.5f);
    }

    private String collectOtpInput() {
        return (safe(et1) + safe(et2) + safe(et3) + safe(et4) + safe(et5) + safe(et6)).trim();
    }

    private String safe(EditText et) {
        return et.getText() == null ? "" : et.getText().toString();
    }

    // ------------------ Resend & countdown ------------------
    private void startResendCountdown(long millis) {
        if (resendTimer != null) resendTimer.cancel();
        tvResendCountdown.setTag(false); // chưa cho phép resend

        resendTimer = new CountDownTimer(millis, 1000) {
            @Override public void onTick(long ms) {
                long s = TimeUnit.MILLISECONDS.toSeconds(ms);
                tvResendCountdown.setText(getString(R.string.otp_resend_in_s, s));
            }
            @Override public void onFinish() {
                tvResendCountdown.setText(getString(R.string.otp_resend_now));
                tvResendCountdown.setTag(true); // cho phép resend
            }
        };
        resendTimer.start();
    }

    private void resendOtpLocal() {
        // tạo OTP mới + lưu + gửi noti
        String otp = String.format(Locale.getDefault(), "%06d", new Random().nextInt(1_000_000));
        long expiresAtMs = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(PREF_KEY_EXPECTED_OTP, otp)
                .putLong(PREF_KEY_OTP_EXPIRES, expiresAtMs)
                .apply();

        String identifier = getIntent().getStringExtra("identifier");
        NotificationHelper.showOtpNotification(this, otp, expiresAtMs, identifier);

        Toast.makeText(this, getString(R.string.otp_resend_triggered), Toast.LENGTH_SHORT).show();

        // reset countdown
        startResendCountdown(RESEND_COOLDOWN_MS);
    }
}
