package namnt.vn.coffestore.ui.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import namnt.vn.coffestore.R;

public class ChatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.chatContainer, new namnt.vn.coffestore.ui.fragments.ChatDrawerFragment())
                .commitNow(); // Use commitNow() to ensure fragment is attached immediately
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvTitle = findViewById(R.id.tvTitle);
        btnBack.setOnClickListener(v -> onBackPressed());
        tvTitle.setText("Messages");
    }
}


