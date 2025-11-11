package namnt.vn.coffestore.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import namnt.vn.coffestore.R;

public class SplashActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable goNext = new Runnable() {
        @Override public void run() {
            // Sau 5 giây, chuyển sang màn Home (MenuActivity)
            startActivity(new Intent(SplashActivity.this, MenuActivity.class));
            finish();
            // Hiệu ứng mượt
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(goNext, 5000); // 5000ms = 5s
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(goNext);
        super.onDestroy();
    }
}
