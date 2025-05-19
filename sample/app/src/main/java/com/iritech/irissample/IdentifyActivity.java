package com.iritech.irissample;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class IdentifyActivity extends AppCompatActivity {

    private TextView emailText;
    private TextView phoneText;
    private TextView nameText;
    private TextView idText;

    private ImageView imageAvatar;

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

            nameText.setText("Name: " + name);
            idText.setText("User ID: " + userId);
            phoneText.setText(phone);
            emailText.setText(email);
        }
        cursor.close();
    }
}