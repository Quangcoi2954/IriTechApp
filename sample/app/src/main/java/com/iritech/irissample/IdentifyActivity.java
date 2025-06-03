package com.iritech.irissample;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iritech.iris.LanguageHelper;

public class IdentifyActivity extends AppCompatActivity {

    private TextView emailText;
    private TextView phoneText;
    private TextView nameText;
    private TextView idText;

    private ImageView imageAvatar;

    private String language;

    @Override
    protected void attachBaseContext(Context newBase) {
        Context context = LanguageHelper.onAttach(newBase);
        super.attachBaseContext(context);
        language = LanguageHelper.getLanguage(context);
        Log.d("LanguageDebug", "EnrollActivity attachBaseContext - Language applied: " + LanguageHelper.getLanguage(context));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify);

        renderInforUser();

        Button btnOk = findViewById(R.id.bt_ok);
        btnOk.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void renderInforUser() {
        String userId = getIntent().getStringExtra("userId");

        emailText = findViewById(R.id.user_email);
        phoneText = findViewById(R.id.user_phone);
        nameText = findViewById(R.id.user_name);
        idText = findViewById(R.id.user_id);

        Intent intent = getIntent();
        Bitmap leftBitmap = intent.getParcelableExtra("leftRenderBm");
        Bitmap rightBitmap = intent.getParcelableExtra("rightRenderBm");
        Log.d("DEBUG_FAKE_IDENTIFY", "rightBm: " + rightBitmap);
        Log.d("DEBUG_FAKE_IDENTIFY", "leftBm: " + leftBitmap);

        ImageView leftImageView = findViewById(R.id.user_left_iris);
        ImageView rightImageView = findViewById(R.id.user_right_iris);

        if (leftBitmap != null) {
            leftImageView.setImageBitmap(leftBitmap);
        } else {
            leftImageView.setVisibility(View.GONE);
        }
        if (rightBitmap != null) {
            rightImageView.setImageBitmap(rightBitmap);
        } else {
            rightImageView.setVisibility(View.GONE);
        }

        EnrollActivity.DatabaseHelper dbHelper = new EnrollActivity.DatabaseHelper(this);

        byte[] avatarBytes = dbHelper.getUserAvatar(Integer.parseInt(userId));
        if (avatarBytes != null) {
            Bitmap avatarBitmap = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
            imageAvatar = findViewById(R.id.avartaImage);
            imageAvatar.setImageBitmap(avatarBitmap);
        }

        Cursor cursor = dbHelper.getUserById(Integer.parseInt(userId));
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            String email = cursor.getString(1);
            String phone = cursor.getString(2);

            if(language.equals("vi")) {
                nameText.setText("Tên: " + name);
                idText.setText("ID người dùng: " + userId);
            }
            else {
                nameText.setText("Name: " + name);
                idText.setText("User ID: " + userId);
            }
            phoneText.setText(phone);
            emailText.setText(email);
        }
        cursor.close();
    }
}