package com.iritech.irissample;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide; // Thêm import cho Glide
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnrollActivity extends AppCompatActivity {

    private EditText editUserName;
    private EditText editUserPhone;
    private EditText editUserEmail;
    private static final int REQUEST_CODE_GALLERY = 100;
    private static final int REQUEST_CODE_CAMERA = 101;
    private ImageView imageAvatar;
    private Uri imageUri;
    private Bitmap bitmap;
    private String userId;
    DatabaseHelper dbHelper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);

        editUserName = findViewById(R.id.edit_name);
        editUserPhone = findViewById(R.id.edit_phone);
        editUserEmail = findViewById(R.id.edit_email);

        userId = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("userId", null);

        Button btnSave = findViewById(R.id.bt_save);

        Intent intent = getIntent();
        Bitmap leftBitmap = intent.getParcelableExtra("leftRenderBm");
        Bitmap rightBitmap = intent.getParcelableExtra("rightRenderBm");
        Log.d("DEBUG_FAKE_ENROLL", "rightBm: " + rightBitmap);
        Log.d("DEBUG_FAKE_ENROLL", "leftBm: " + leftBitmap);

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

        if (dbHelper.isUserIdExists(userId)) {
            renderInforUser(userId, editUserEmail, editUserPhone, editUserName);
        }

        String originalName = editUserName.getText().toString().trim();
        String originalEmail = editUserEmail.getText().toString().trim();
        String originalPhone = editUserPhone.getText().toString().trim();

        byte[] avatarBytes = dbHelper.getUserAvatar(Integer.parseInt(userId));
        if (avatarBytes != null) {
            bitmap = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
        }

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkChanges(originalName, originalEmail, originalPhone, bitmap, null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editUserName.addTextChangedListener(watcher);
        editUserEmail.addTextChangedListener(watcher);
        editUserPhone.addTextChangedListener(watcher);

        if(!dbHelper.isUserIdExists(userId)) {
            btnSave.setEnabled(true);
            btnSave.setAlpha(1.0f);
        }
        else {
            btnSave.setEnabled(false);
            btnSave.setAlpha(0.5f);
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editUserName.getText().toString().trim();
                String phone = editUserPhone.getText().toString().trim();
                String email = editUserEmail.getText().toString().trim();

                if(name.isEmpty() || phone.isEmpty() || email.isEmpty() || bitmap == null) {
                    showWarningDialog("Please enter the full information before saving!");
                } else {
                    //getApplicationContext().deleteDatabase("UserData.db");
                    if(dbHelper.isUserIdExists(userId)) {
                        dbHelper.deleteUserById(Integer.parseInt(userId));
                    }

                    byte[] avatar = imageToByteArray(bitmap);
                    dbHelper.insertUser(userId, name, email, phone, avatar);
                    showSuccessDialog("Save successfully!");
                }
            }
        });

        Button btnCancle = findViewById(R.id.bt_cancel);
        btnCancle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(EnrollActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button btnChooseOption = findViewById(R.id.bt_choose_image);
        btnChooseOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog("Choose your options");
            }
        });
    }

    private void checkChanges(String originalName, String originalEmail, String originalPhone, Bitmap bitmap, Bitmap currentBitmap) {
        String currentName = editUserName.getText().toString().trim();
        String currentEmail = editUserEmail.getText().toString().trim();
        String currentPhone = editUserPhone.getText().toString().trim();

        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        boolean changed = !currentName.equals(originalName) ||
                !currentEmail.equals(originalEmail) ||
                !currentPhone.equals(originalPhone) ||
                (bitmap != null && currentBitmap != null && !bitmap.sameAs(currentBitmap));

        Button btnSave = findViewById(R.id.bt_save);
        btnSave.setEnabled(changed);

        if(changed) {
            btnSave.setAlpha(1.0f);
        }
        else {
            btnSave.setAlpha(0.5f);
        }

        boolean isDupliactePhone = dbHelper.isUserPhoneExists(currentPhone);
        boolean isDupliacteEmail = dbHelper.isUserEmailExists(currentEmail);
        boolean isInvalidEmail = currentEmail.matches(emailPattern);
        if(editUserPhone.isFocused())
        {
            if(!dbHelper.isUserIdExists(userId))
            {
                if(isDupliactePhone) {
                    editUserPhone.setError("Phone number already exists");
                    btnSave.setEnabled(isDupliactePhone);
                    btnSave.setAlpha(0.5f);
                }
            }
            else
            {
                if(!currentPhone.equals(originalPhone)) {
                    if (isDupliactePhone) {
                        editUserPhone.setError("Phone number already exists");
                        btnSave.setEnabled(isDupliactePhone);
                        btnSave.setAlpha(0.5f);
                    }
                }
            }
        }
        else if (editUserEmail.isFocused())
        {
            if(!dbHelper.isUserIdExists(userId)) {
                if (!isInvalidEmail) {
                    editUserEmail.setError("Invalid email format");
                    btnSave.setEnabled(isInvalidEmail);
                    btnSave.setAlpha(0.5f);
                } else if (isDupliacteEmail) {
                    editUserEmail.setError("Email already exists");
                    btnSave.setEnabled(isDupliacteEmail);
                    btnSave.setAlpha(0.5f);
                }
            }
            else
            {
                if(!currentEmail.equals(originalEmail)) {
                    if (!isInvalidEmail) {
                        editUserEmail.setError("Invalid email format");
                        btnSave.setEnabled(isInvalidEmail);
                        btnSave.setAlpha(0.5f);
                    } else if (isDupliacteEmail) {
                        editUserEmail.setError("Email already exists");
                        btnSave.setEnabled(isDupliacteEmail);
                        btnSave.setAlpha(0.5f);
                    }
                }
            }
        } else {
            btnSave.setAlpha(1.0f);
        }
    }

    private void renderInforUser(String userId, EditText editUserEmail, EditText editUserPhone, EditText editUserName) {

        DatabaseHelper dbHelper = new DatabaseHelper(this);

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

            editUserName.setText(name);
            editUserPhone.setText(phone);
            editUserEmail.setText(email);
        }
        cursor.close();
    }
    private void showSuccessDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_successful, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView txtMessage = dialogView.findViewById(R.id.txtMessage);
        txtMessage.setText(message);

        dialog.setCancelable(false);
        dialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dialog.dismiss();

            Intent intent = new Intent(EnrollActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 1500);
    }

    private void showWarningDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_warning, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView txtMessage = dialogView.findViewById(R.id.txtMessage);
        Button btnOK = dialogView.findViewById(R.id.btnOK);

        txtMessage.setText(message);
        btnOK.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showOptionsDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView txtMessage = dialogView.findViewById(R.id.txtMessage);
        txtMessage.setText(message);

        Button btnFromDevice = dialogView.findViewById(R.id.btn_from_device);
        Button btnTakeAPicture = dialogView.findViewById(R.id.btn_take_a_picture);

        imageAvatar = findViewById(R.id.avartaImage);

        btnFromDevice.setOnClickListener(v -> {
            openGallery();
            dialog.dismiss();
        });

        btnTakeAPicture.setOnClickListener(v -> {
            openCamera();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Tạo URI tạm để lưu ảnh
        imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", createImageFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    private File createImageFile() {
        String fileName = "avatar_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, fileName + ".jpg");
    }

    // Hàm sửa đổi: onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_GALLERY && data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    // Sử dụng Glide để tải ảnh từ gallery
                    Glide.with(this)
                            .asBitmap()
                            .load(selectedImage)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    bitmap = resource;
                                    imageAvatar.setImageBitmap(bitmap);
                                    Log.d("EnrollActivity", "Bitmap loaded from gallery: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                                    checkChanges(null, null, null, null, bitmap);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                    // Không cần xử lý trong trường hợp này
                                }

                                @Override
                                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                    Log.e("EnrollActivity", "Failed to load image from gallery");
                                    Toast.makeText(EnrollActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Log.e("EnrollActivity", "Selected image URI is null");
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_CODE_CAMERA) {
                if (imageUri != null) {
                    // Sử dụng Glide để tải ảnh từ camera
                    Glide.with(this)
                            .asBitmap()
                            .load(imageUri)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    bitmap = resource;
                                    imageAvatar.setImageBitmap(bitmap);
                                    Log.d("EnrollActivity", "Bitmap loaded from camera: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                                    checkChanges(null, null, null, null, bitmap);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                    // Không cần xử lý trong trường hợp này
                                }

                                @Override
                                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                    Log.e("EnrollActivity", "Failed to load image from camera");
                                    Toast.makeText(EnrollActivity.this, "Failed to load camera image", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Log.e("EnrollActivity", "Camera image URI is null");
                    Toast.makeText(this, "No camera image captured", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Hàm không thay đổi: imageToByteArray
    public byte[] imageToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "UserData.db";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_NAME = "users";
        private static final String COL_ID = "id";
        private static final String COL_NAME = "name";
        private static final String COL_EMAIL = "email";
        private static final String COL_PHONE = "phone";
        private static final String COL_AVATAR = "avatar";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " TEXT, " +
                    COL_NAME + " TEXT, " +
                    COL_EMAIL + " TEXT, " +
                    COL_PHONE + " TEXT," +
                    COL_AVATAR + " BLOB)";
            db.execSQL(createTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

        public void deleteUserById(int userId) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM users WHERE id = ?", new Object[]{userId});
            db.close();
        }

        public void deleteAllUsers() {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("users", null, null);  // Xóa toàn bộ dòng trong bảng users
            db.close();
        }

        public boolean insertUser(String id, String name, String email, String phone, byte[] avatar) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_ID, id);
            values.put(COL_NAME, name);
            values.put(COL_EMAIL, email);
            values.put(COL_PHONE, phone);
            values.put(COL_AVATAR, avatar);

            long result = db.insert(TABLE_NAME, null, values);
            return result != -1; // true nếu thành công
        }

        public List<String> getAllUsers() {
            List<String> users = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM users", null);
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                    String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                    byte[] avatar = cursor.getBlob(cursor.getColumnIndexOrThrow("avatar"));
                    users.add(id + " | " + name + " | " + email + " | " + phone + " | " + Arrays.toString(avatar));
                } while (cursor.moveToNext());
            }
            cursor.close();
            return users;
        }

        public boolean isUserIdExists(String id) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id FROM users WHERE id = ?", new String[]{id});
            boolean exists = cursor.moveToFirst();
            cursor.close();
            return exists;
        }

        public boolean isUserPhoneExists(String phone) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT phone FROM users WHERE phone = ?", new String[]{phone});
            boolean exists = cursor.moveToFirst();
            cursor.close();
            return exists;
        }

        public boolean isUserEmailExists(String email) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT email FROM users WHERE email = ?", new String[]{email});
            boolean exists = cursor.moveToFirst();
            cursor.close();
            return exists;
        }

        public Cursor getUserById(int userId) {
            SQLiteDatabase db = this.getReadableDatabase();
            return db.rawQuery("SELECT name, email, phone FROM users WHERE id = ?", new String[]{String.valueOf(userId)});
        }

        public byte[] getUserAvatar(int userId) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT avatar FROM users WHERE id = ?", new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst()) {
                byte[] avatarBytes = cursor.getBlob(0);
                cursor.close();
                return avatarBytes;
            }
            cursor.close();
            return null;
        }
    }
}