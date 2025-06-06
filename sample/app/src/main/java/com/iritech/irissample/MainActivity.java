package com.iritech.irissample;

import static com.iritech.irissample.EnrollActivity.*;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.iritech.android.widget.alertdialog.BestImageDialog;
import com.iritech.android.widget.alertdialog.SettingDialog;
import com.iritech.android.widget.alertdialog.RegisterLicenseDialog;
import com.iritech.iris.CaptureActivity;
import com.iritech.iris.Constants;
import com.iritech.iris.IriController;
import com.iritech.iris.LanguageHelper;
import com.iritech.iris.LicenseInfo;
import com.iritech.iris.Utilities;
import com.iritech.mqel704.GemResult;
import com.iritech.mqel704.ImageData;
import com.iritech.mqel704.ImageFormat;
import com.iritech.mqel704.ImageKind;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int PERMISSIONS_REQUEST_CAMERA = 2;
    private static final int PERMISSIONS_READ_PHONE_STATE = 3;
    private static final int PERMISSIONS_ACCESS_NETWORK_STATE = 4;

//    String enrollImgPath = (Environment.getExternalStorageDirectory().toString() + File.separator
//            + "iritech" + File.separator + "enroll");
//    String verifyImgPath = (Environment.getExternalStorageDirectory().toString() + File.separator
//            + "iritech" + File.separator + "verify");

    String enrollImgPath = "/sdcard" + File.separator
            + "iritech" + File.separator + "enroll";
    String verifyImgPath = "/sdcard" + File.separator
            + "iritech" + File.separator + "verify";

    String identifyImgPath = "/sdcard" + File.separator
            + "iritech" + File.separator + "identify";

    String mUserId;
    private int REQUEST_CODE_IDENTIFY = 1111;
    private int REQUEST_CODE_CAPTURE = 1112;
    private int REQUEST_CODE_ENROLL = 1113;
    private int REQUEST_CODE_VERIFY = 1114;
    private int REQUEST_CODE_UNENROLL = 1115;
    private EditText editUserId;
    PowerManager.WakeLock wl;

    private int mResultCode;

    private String mAction;

    private int mRequestCode;

    DatabaseHelper dbHelper = new DatabaseHelper(this);

    private boolean isTestMode = false;

    private String language;

    @Override
    protected void attachBaseContext(Context newBase) {
        // Gọi LanguageHelper.onAttach để áp dụng ngôn ngữ
        // Giả sử bạn đã có MyAplication.java gọi onAttach,
        // thì dòng này trong Activity vẫn hữu ích để đảm bảo Activity này cũng được cập nhật
        // nếu nó được tạo lại một cách độc lập.
        Context context = LanguageHelper.onAttach(newBase);
        super.attachBaseContext(context);
        language = LanguageHelper.getLanguage(context);
        Log.d("LanguageDebug", "MainActivity attachBaseContext - Language applied: " + LanguageHelper.getLanguage(context));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("LanguageDebug", "MainActivity onCreate - Current language: " + LanguageHelper.getLanguage(this));

        editUserId = findViewById(R.id.edit_user_id);

        CaptureActivity.setUSBActivity(this);

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            String version = pInfo.versionName;
            TextView tvVersion = findViewById(R.id.tv_version);
            tvVersion.setText("Version: " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Button btnEnroll = findViewById(R.id.bt_enroll);
        btnEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAction = Constants.ACTION_ENROLL;
                mRequestCode = REQUEST_CODE_ENROLL;
                startCaptureActivity(Constants.ACTION_ENROLL, REQUEST_CODE_ENROLL);
            }
        });

        Button btnVerify = findViewById(R.id.bt_verify);
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAction = Constants.ACTION_VERIFY;
                mRequestCode = REQUEST_CODE_VERIFY;
                startCaptureActivity(Constants.ACTION_VERIFY, REQUEST_CODE_VERIFY);

            }
        });

        Button btnUnenroll = findViewById(R.id.bt_unenroll);
        btnUnenroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAction = Constants.ACTION_UNENROLL;
                mRequestCode = REQUEST_CODE_UNENROLL;
                startCaptureActivity(Constants.ACTION_UNENROLL, REQUEST_CODE_UNENROLL);
            }
        });

        Button btnIdentify = findViewById(R.id.bt_identify);
        btnIdentify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAction = Constants.ACTION_IDENTIFY;
                mRequestCode = REQUEST_CODE_IDENTIFY;
                startCaptureActivity(Constants.ACTION_IDENTIFY, REQUEST_CODE_IDENTIFY);
            }
        });

        Button btnCapture = findViewById(R.id.bt_capture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAction = Constants.ACTION_CAPTURE;
                mRequestCode = REQUEST_CODE_CAPTURE;
                startCaptureActivity(Constants.ACTION_CAPTURE, REQUEST_CODE_CAPTURE);
            }
        });

        //checkStoragePermission();

        // Avoid app auto change screen to sleep
        writeWakeLock();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "UbioXiris:MainActivity");
        wl.acquire();

        //Load and request permission from /sdcard/iritech/irissample.json
        loadConfigs();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkPermissions(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mResultCode == GemResult.IDDK_UVC_DEVICE_ACCESS_DENIED) {
            startCaptureActivity(mAction, mRequestCode);
        }
    }

    private void loadConfigs()
    {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("/sdcard/iritech/irissample.json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            JSONObject jsonObject = new JSONObject(obj.toString());
            JSONArray permissionList = jsonObject.getJSONArray("permissions");
            for (int i = 0; i < permissionList.length(); i++) {
                JSONObject permission = permissionList.getJSONObject(i);
                String permissionName = permission.getString("name");
                int permissionCode = permission.getInt("code");
                if (ContextCompat.checkSelfPermission(this, permissionName) !=
                        PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    ActivityCompat.requestPermissions(this, new String[]{permissionName},
                            permissionCode);
                }
            }
        } catch (Exception var) {
            var.printStackTrace();
        }
    }

    private void startCaptureActivity(String actionType, int requestCode) {
        if (!checkCameraPermission()) {
            return;
        }
        mUserId = editUserId.getText().toString().trim();

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userId", mUserId);
        editor.apply();

        if (mUserId.isEmpty() && (actionType.equals(Constants.ACTION_VERIFY)
                || actionType.equals(Constants.ACTION_ENROLL))) {
            //Toast.makeText(this, "Please input User Id", Toast.LENGTH_SHORT).show();
            BestImageDialog dialog = new BestImageDialog(this);
            dialog.setTitle("Information!");
            dialog.show();
            dialog.setBestImages(null, null, null);
            if(language.equals("vi")) {
                dialog.setMessage("  Vui lòng nhập User ID  ");
            }
            else {
                dialog.setMessage("  Please input User ID  ");
            }

            return;
        }

        if((actionType.equals(Constants.ACTION_ENROLL))) {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            if (dbHelper.isUserIdExists(mUserId)) {
                if(language.equals("vi"))
                {
                    showWarningDialog("User ID đã tồn tại!");

                }
                else {
                    showWarningDialog("User ID already exists!");
                }
                return;
            }
        }

        if((actionType.equals(Constants.ACTION_VERIFY))) { // || (actionType.equals(Constants.ACTION_UNENROLL))) {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            if (!dbHelper.isUserIdExists(mUserId)) {
                if(language.equals("vi")) {
                    showWarningDialog("User ID không tồn tại!");
                }
                else {
                    showWarningDialog("User ID does not exist!");
                }
                return;
            }
        }

        Intent intent = new Intent(getApplicationContext(), CaptureActivity.class);
        intent.setAction(actionType);
        intent.putExtra(Constants.EXTRA_USER_ID, mUserId);

        LicenseInfo licInfo = LicenseInfo.getInstance();
        if (!licInfo.isInitialized()){
            licInfo.initialize(this);
        }
        if (!licInfo.isLicenseExisted()){
            RegisterLicenseDialog regLicenseDlg = RegisterLicenseDialog.newInstance();
            regLicenseDlg.setActivityAfterRegistered(intent, requestCode);
            regLicenseDlg.show(getSupportFragmentManager(), null);
        }
        else{
            startActivityForResult(intent, requestCode);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            int resultCodeExt = data.getIntExtra(Constants.EXTRA_RESULT_CODE, -1);
            mResultCode = resultCodeExt;
        }

        if (mResultCode != GemResult.IDDK_UVC_DEVICE_ACCESS_DENIED){
            if (resultCode == RESULT_OK) {
                processResult(requestCode, data);
            } else {
                Toast.makeText(getApplicationContext(), "Capture activity failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateImgPath() {
        enrollImgPath += File.separator + mUserId;
        verifyImgPath += File.separator + mUserId;
        identifyImgPath += File.separator + mUserId;

        File enrollDirFile = new File(enrollImgPath);
        if (!enrollDirFile.exists()) {
            boolean wasSuccessful = enrollDirFile.mkdirs();
            if (wasSuccessful) {
                Log.d("MyApp", "Directory created: " + enrollImgPath);
            } else {
                Log.e("MyApp", "Failed to create directory: " + enrollImgPath);
            }
        }

        File verifyDirFile = new File(verifyImgPath);
        if (!verifyDirFile.exists()) {
            boolean wasSuccessful = verifyDirFile.mkdirs();
            if (wasSuccessful) {
                Log.d("MyApp", "Directory created: " + verifyImgPath);
            } else {
                Log.e("MyApp", "Failed to create directory: " + verifyImgPath);
            }
        }

        File identifyDirFile = new File(identifyImgPath);
        if (!identifyDirFile.exists()) {
            boolean wasSuccessful = identifyDirFile.mkdirs();
            if (wasSuccessful) {
                Log.d("MyApp", "Directory created: " + identifyImgPath);
            }
            else {
                Log.e("MyApp", "Failed to create directory: " + identifyImgPath);
            }
        }
    }

    private void processResult(int requestCode, Intent data) {

        Bitmap leftRenderBm = null;
        Bitmap rightRenderBm = null;

        updateImgPath();

        if (requestCode == REQUEST_CODE_CAPTURE) {
            processCaptureResult(data, verifyImgPath, mUserId, "best");
        }
        else if (requestCode == REQUEST_CODE_ENROLL) {
            processCaptureResult(data, enrollImgPath, mUserId, "best");

            //TEST

//            Intent intent = new Intent(MainActivity.this, EnrollActivity.class);
//            intent.putExtra("userId", mUserId);
//            intent.putExtra("leftRenderBm", leftRenderBm);
//            intent.putExtra("rightRenderBm", rightRenderBm);
//            startActivity(intent);

            boolean matchingResult = data.getBooleanExtra(Constants.EXTRA_MATCHING_RESULT, false);
            Log.d("DEBUG_ENROLL", "matchingResult: " + matchingResult);
            if (matchingResult) {
                if(language.equals("vi")) {
                    showWarningDialog("Đăng ký bị trùng lặp!");
                }
                else {
                    showWarningDialog("Enroll Duplicate!");
                }
            }
            else {
                Pair<Bitmap, Bitmap> pair = processCaptureRender(this, enrollImgPath, "best");
                leftRenderBm = pair.first;
                Log.d("DEBUG_ENROLL", "leftRenderBm: " + leftRenderBm);
                rightRenderBm = pair.second;
                Log.d("DEBUG_ENROLL", "rightRenderBm: " + rightRenderBm);

                if(leftRenderBm != null && rightRenderBm != null) {
                    //THUC TE
                    Intent intent = new Intent(MainActivity.this, EnrollActivity.class);
                    intent.putExtra("userId", mUserId);
                    intent.putExtra("leftRenderBm", leftRenderBm);
                    intent.putExtra("rightRenderBm", rightRenderBm);
                    startActivity(intent);
                }
            }
        }
        else {
            Bitmap leftBm = null;
            Bitmap rightBm = null;
            Bitmap unknownBm = null;
            int resultCode = data.getIntExtra(Constants.EXTRA_RESULT_CODE, -1);
            String msg = data.getStringExtra(Constants.EXTRA_RESULT_MSG);
            msg += (resultCode != GemResult.IDDK_OK ? " (" + resultCode + ")" : "");
            if (requestCode == REQUEST_CODE_VERIFY) {

                //TEST

//                Pair<Bitmap, Bitmap> pair = processCaptureRender(this, verifyImgPath, "best");
//                leftRenderBm = pair.first;
//                Log.d("DEBUG_VERIFY", "leftRenderBm: " + leftRenderBm);
//                rightRenderBm = pair.second;
//                Log.d("DEBUG_VERIFY", "rightRenderBm: " + rightRenderBm);
//
//                Intent intent = new Intent(MainActivity.this, VerifyActivity.class);
//                intent.putExtra("userId", mUserId);
//                intent.putExtra("leftRenderBm", leftRenderBm);
//                intent.putExtra("rightRenderBm", rightRenderBm);
//                startActivity(intent);

                if (resultCode == 0) {
                    processCaptureResult(data, verifyImgPath, mUserId, "best");

                    boolean matchingResult = data.getBooleanExtra(Constants.EXTRA_MATCHING_RESULT, false);
                    if (matchingResult) {
                        if(language.equals("vi")) {
                            msg = "Xác nhận thành công !";
                        }
                        else {
                            msg = "Verify Successfully !";
                        }

                        //THUC TE

                        Pair<Bitmap, Bitmap> pair = processCaptureRender(this, verifyImgPath, "best");
                        leftRenderBm = pair.first;
                        Log.d("DEBUG_VERIFY", "leftRenderBm: " + leftRenderBm);
                        rightRenderBm = pair.second;
                        Log.d("DEBUG_VERIFY", "rightRenderBm: " + rightRenderBm);

                        Intent intent = new Intent(MainActivity.this, VerifyActivity.class);
                        intent.putExtra("userId", mUserId);
                        intent.putExtra("leftRenderBm", leftRenderBm);
                        intent.putExtra("rightRenderBm", rightRenderBm);
                        startActivity(intent);

                    } else {
                        if(language.equals("vi")) {
                            msg = "Xác nhận thất bại! Không khớp";
                        }
                        else {
                            msg = "Verify failed! Not matched";
                        }
                    }
                }
            }
            else if (requestCode == REQUEST_CODE_IDENTIFY) {

                //TEST
//                Pair<Bitmap, Bitmap> pair = processCaptureRender(this, identifyImgPath, "best");
//                leftRenderBm = pair.first;
//                rightRenderBm = pair.second;
//                Intent intent = new Intent(MainActivity.this, IdentifyActivity.class);
//                intent.putExtra("userId", "2");
//                intent.putExtra("leftRenderBm", leftRenderBm);
//                Log.d("DEBUG_IDENTIFY", "leftRenderBm: " + leftRenderBm);
//                intent.putExtra("rightRenderBm", rightRenderBm);
//                Log.d("DEBUG_IDENTIFY", "rightRenderBm: " + rightRenderBm);
//                startActivity(intent);

                if (resultCode == 0)
                {
                    processCaptureResult(data, identifyImgPath, mUserId, "best");

                    int resultCount = data.getIntExtra(Constants.EXTRA_MATCHING_COUNT, 0);
                    String resultItems = data.getStringExtra(Constants.EXTRA_MATCHING_ITEMS);

                    if(language.equals("vi")) {
                        msg = "Đã tìm thấy " + resultCount + " người dùng: " + resultItems;
                    }
                    else {
                        msg = "Matched with " + resultCount + " user(s): " + resultItems;
                    }

                    String id = "";

                    if (resultItems != null && !resultItems.isEmpty()) {
                        String[] parts = resultItems.split(",");
                        String idPart = parts[0]; // "ID: 123"
                        id = idPart.replace("ID:", "").trim(); // "123"

                        Log.d("MainActivity", "ID: " + id);
                    }

                    //THUC TE

                    Pair<Bitmap, Bitmap> pair = processCaptureRender(this, identifyImgPath, "best");
                    leftRenderBm = pair.first;
                    rightRenderBm = pair.second;
                    Intent intent = new Intent(MainActivity.this, IdentifyActivity.class);
                    intent.putExtra("userId", id);
                    intent.putExtra("leftRenderBm", leftRenderBm);
                    Log.d("DEBUG_IDENTIFY", "leftRenderBm: " + leftRenderBm);
                    intent.putExtra("rightRenderBm", rightRenderBm);
                    Log.d("DEBUG_IDENTIFY", "rightRenderBm: " + rightRenderBm);
                    startActivity(intent);
                }
            }
            else {
                if(dbHelper.isUserIdExists(mUserId)) {
                    dbHelper.deleteUserById(Integer.parseInt(mUserId));
                    if(language.equals("vi")) {
                        msg = "UNENROLL thành công!";
                    }
                    else {
                        msg = "UNENROLL successfully!";
                    }
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
                else {
                    dbHelper.deleteAllUsers();
                    if(language.equals("vi")) {
                        msg = "UNENROLL thành công!";
                    }
                    else {
                        msg = "UNENROLL successfully!";
                    }
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }

            BestImageDialog dialog = new BestImageDialog(this);
            dialog.setTitle("Information!");
            dialog.show();
            dialog.setBestImages(leftBm, rightBm, unknownBm);
            dialog.setMessage(msg);
        }
    }

    public Pair<Bitmap, Bitmap> processCaptureRender(Context context, String userFolderPath, String filePurposeToSearch) {
        // Kiểm tra context và đầu vào
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        if (isTestMode) {
            Bitmap left = null;
            Bitmap right = null;

            try {
                // Tải tài nguyên
                left = BitmapFactory.decodeResource(context.getResources(), R.drawable.fake_left);
                Log.d("DEBUG_FAKE", "left: " + left);
                right = BitmapFactory.decodeResource(context.getResources(), R.drawable.fake_right);
                Log.d("DEBUG_FAKE", "right: " + right);

                if (left == null || right == null) {
                    throw new IllegalStateException("Failed to load one or both test images");
                }

                // Convert dp to pixels
                DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                int targetWidthPx = 180;
                int targetHeightPx = 150;

                // Nén ảnh
                Bitmap compressedLeft = Bitmap.createScaledBitmap(left, targetWidthPx, targetHeightPx, true);
                Log.d("DEBUG_FAKE", "compressedLeft:" + compressedLeft);
                Bitmap compressedRight = Bitmap.createScaledBitmap(right, targetWidthPx, targetHeightPx, true);
                Log.d("DEBUG_FAKE", "compressedRight:" + compressedRight);

                // Giải phóng Bitmap gốc để tránh rò rỉ bộ nhớ
                left.recycle();
                right.recycle();

                return new Pair<>(compressedLeft, compressedRight);
            } catch (Exception e) {
                // Xử lý lỗi rõ ràng
                throw new RuntimeException("Error processing test images: " + e.getMessage(), e);
            }
        }
        else {

            List<File> leftBmpFiles = findBmpFiles(userFolderPath, filePurposeToSearch, "L");
            Log.d("FileSearch", "Found " + leftBmpFiles.size() + " left BMP files.");

            Bitmap latestLeftBitmap = null;

            if (!leftBmpFiles.isEmpty()) {
                // Sắp xếp theo ngày sửa đổi, tệp mới nhất ở đầu
                Collections.sort(leftBmpFiles, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return Long.compare(f2.lastModified(), f1.lastModified());
                    }
                });

                File latestLeftBmp = leftBmpFiles.get(0);
                Log.d("FileSearch", "Latest left BMP: " + latestLeftBmp.getAbsolutePath());
                latestLeftBitmap = loadBitmapFromFile(latestLeftBmp.getAbsolutePath());
            }

            List<File> rightBmpFiles = findBmpFiles(userFolderPath, filePurposeToSearch, "R");
            Log.d("FileSearch", "Found " + rightBmpFiles.size() + " right BMP files.");

            Bitmap latestRightBitmap = null;

            if (!rightBmpFiles.isEmpty()) {
                // Sắp xếp theo ngày sửa đổi, tệp mới nhất ở đầu
                Collections.sort(rightBmpFiles, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return Long.compare(f2.lastModified(), f1.lastModified());
                    }
                });

                File latestRightBmp = rightBmpFiles.get(0);
                Log.d("FileSearch", "Latest right BMP: " + latestRightBmp.getAbsolutePath());
                latestRightBitmap = loadBitmapFromFile(latestRightBmp.getAbsolutePath());
            }

            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int targetWidthPx = 180;
            int targetHeightPx = 150;

            Bitmap compressedLeft = null;
            Bitmap compressedRight = null;

            // Nén ảnh
            if (latestLeftBitmap != null) {
                try {
                    compressedLeft = Bitmap.createScaledBitmap(latestLeftBitmap, targetWidthPx, targetHeightPx, true);
                    Log.d("RealMode", "Compressed Left: " + compressedLeft);
                } finally {
                    latestLeftBitmap.recycle();
                }
            }

            if (latestRightBitmap != null) {
                try {
                    compressedRight = Bitmap.createScaledBitmap(latestRightBitmap, targetWidthPx, targetHeightPx, true);
                    Log.d("RealMode", "Compressed Right: " + compressedRight);
                } finally {
                    latestRightBitmap.recycle();
                }
            }

            return new Pair<>(compressedLeft, compressedRight);
        }
    }

    public Bitmap loadBitmapFromFile(String filePath) {
        File imageFile = new File(filePath);
        if (!imageFile.exists() || !imageFile.isFile()) {
            Log.e("ImageLoader", "BMP File not found or is not a file: " + filePath);
            return null;
        }

        try {
            // Tùy chọn này hữu ích nếu bạn muốn kiểm soát cấu hình của Bitmap
            // BitmapFactory.Options options = new BitmapFactory.Options();
            // options.inPreferredConfig = Bitmap.Config.ARGB_8888; // Ví dụ
            // return BitmapFactory.decodeFile(filePath, options);

            return BitmapFactory.decodeFile(filePath);
        } catch (OutOfMemoryError e) {
            Log.e("ImageLoader", "OutOfMemoryError loading BMP: " + filePath, e);
            // Cân nhắc giảm kích thước ảnh hoặc xử lý lỗi bộ nhớ
            return null;
        } catch (Exception e) {
            Log.e("ImageLoader", "Error loading BMP: " + filePath, e);
            return null;
        }
    }

    private List<File> findBmpFiles(String directoryPath, final String filePurpose, final String sideIndicator) {
        File directory = new File(directoryPath);
        List<File> foundFiles = new ArrayList<>();

        if (!directory.exists() || !directory.isDirectory()) {
            Log.e("FileFinder", "Directory not found or is not a directory: " + directoryPath);
            return foundFiles; // Trả về danh sách rỗng
        }

        // Tạo một bộ lọc tên tệp
        FilenameFilter bmpFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean matchesPurpose = name.contains(filePurpose);
                boolean matchesSide = name.contains(filePurpose + sideIndicator);
                boolean isBmp = name.toLowerCase().endsWith(".bmp");

                return matchesPurpose && matchesSide && isBmp;
            }
        };

        File[] files = directory.listFiles(bmpFilter);

        if (files != null) {
            Collections.addAll(foundFiles, files);
        }

        return foundFiles;
    }

    private void processCaptureResult(Intent data, String folderPath, String prefix, String filePurpose
    ) {
        String imageExt = ".bmp";
        String templateExt = ".tpl";
        Bitmap leftBm = null;
        Bitmap rightBm = null;
        Bitmap unknownBm = null;

        int resultCode = data.getIntExtra(Constants.EXTRA_RESULT_CODE, 0);
        String resultMsg = data.getStringExtra(Constants.EXTRA_RESULT_MSG);

        if (resultCode == 0) {
            // Capture success
            if(language.equals("vi")) {
                resultMsg = "Thành công! dữ liệu hình ảnh được lưu tại: \n" + folderPath;
            }
            else {
                resultMsg = "Successfully! Captured data is saved at: \n" + folderPath;
            }
            byte[] leftTemplateBuffer = data.getByteArrayExtra(Constants.EXTRA_LEFT_TEMPLATE);
            byte[] rightTemplateBuffer = data.getByteArrayExtra(Constants.EXTRA_RIGHT_TEMPLATE);

            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            int second = c.get(Calendar.SECOND);
            prefix = prefix + String.format(
                    Locale.US, "_%02d%02d%02d_%02d%02d%02d_",
                    year, month, day, hour, minute, second
            );

            saveFile(folderPath, prefix, filePurpose + "L" + templateExt, leftTemplateBuffer);
            saveFile(folderPath, prefix, filePurpose + "R" + templateExt, rightTemplateBuffer);

            // Get best images if needed from IriController
            // Get best image only after capture successfully, if no capture yet error will be returned
            ImageData leftImage = new ImageData();
            ImageData rightImage = new ImageData();
            ImageData unknownImage = new ImageData();

            byte[] bmpLeft = null;
            byte[] bmpRight = null;
            byte[] bmpUnknown = null;

            CaptureActivity.getResultImages(leftImage, rightImage, unknownImage);
            if (leftImage.getData() != null) {
                bmpLeft = Utilities.convertRawImageToBitmap(leftImage.getData(), leftImage.getWidth(), leftImage.getHeight());
                saveFile(folderPath, prefix, filePurpose + "L" + imageExt, bmpLeft);
            }

            if (rightImage.getData() != null) {
                bmpRight = Utilities.convertRawImageToBitmap(rightImage.getData(), rightImage.getWidth(), rightImage.getHeight());
                saveFile(folderPath, prefix, filePurpose + "R" + imageExt, bmpRight);
            }

            if (unknownImage.getData() != null) {
                bmpUnknown = Utilities.convertRawImageToBitmap(unknownImage.getData(), unknownImage.getWidth(), unknownImage.getHeight());
                saveFile(folderPath, prefix, filePurpose + "U" + imageExt, bmpUnknown);
            }

            // save left image kind 7_35, MONO_JPEG2000
            ImageData leftImageJp2 = new ImageData(ImageKind.IDDK_IKIND_K7_3_5, ImageFormat.IDDK_IFORMAT_MONO_JPEG2000, 0, 0, null);
            leftImageJp2.setCompressParams(1,100);
            ImageData rightImageJp2 = new ImageData(ImageKind.IDDK_IKIND_K7_3_5, ImageFormat.IDDK_IFORMAT_MONO_JPEG2000, 0, 0, null);
            rightImageJp2.setCompressParams(1,100);
            ImageData unknownImageJp2 = new ImageData(ImageKind.IDDK_IKIND_K7_3_5, ImageFormat.IDDK_IFORMAT_MONO_JPEG2000, 0, 0, null);
            unknownImageJp2.setCompressParams(1,100);
            CaptureActivity.getResultImages(leftImageJp2, rightImageJp2, unknownImageJp2);
            imageExt = ".jp2";
            String imagesKindString = "MONO_JPEG2000";
            String imageFormatString = "IDDK_IKIND_K7_35";
            if (leftImageJp2.getData() != null) {
                saveFile(folderPath, prefix, filePurpose + "L_" + imagesKindString + imageFormatString + imageExt, leftImageJp2.getData());
            }

            if (rightImageJp2.getData() != null) {
                saveFile(folderPath, prefix, filePurpose + "R_" + imagesKindString + imageFormatString + imageExt, rightImageJp2.getData());
            }

            if (unknownImageJp2.getData() != null) {
                saveFile(folderPath, prefix, filePurpose + "U_" + imagesKindString + imageFormatString + imageExt, unknownImageJp2.getData());
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            if (bmpLeft != null) {
                leftBm = BitmapFactory.decodeByteArray(bmpLeft, 0, bmpLeft.length, options);
            }

            if (bmpRight != null) {
                rightBm = BitmapFactory.decodeByteArray(bmpRight, 0, bmpRight.length, options);
            }

            if (bmpUnknown != null) {
                unknownBm = BitmapFactory.decodeByteArray(bmpUnknown, 0, bmpUnknown.length, options);
            }

        } else {
            resultMsg += " (" + resultCode + ")";
        }

        BestImageDialog dialog = new BestImageDialog(this);
        dialog.setTitle("Information!");
        dialog.show();
        dialog.setBestImages(leftBm, rightBm, unknownBm);
        dialog.setMessage(resultMsg);
    }

    private void saveFile(String filePath, String prefix, String fileName,
                          byte[] byteArray) {
        if (byteArray == null) {
            return;
        }
        String fileFullname = prefix + fileName;
        String filePathName = filePath + File.separator + fileFullname;
        File folder = new File(filePath);
        File file = new File(filePathName);

        try {
            if (!folder.exists()) {
                folder.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream bos = new FileOutputStream(file);

            bos.write(byteArray);
            bos.flush();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Information!")
                        .setMessage("Please allow write to external storage permission.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                            }
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            // Permission has already been granted
            initFolder();
        }
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Information!")
                        .setMessage("Please allow camera permission.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        PERMISSIONS_REQUEST_CAMERA);
                            }
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_CAMERA);
            }
            return false;
        }
        return true;
    }

    private void initFolder() {
        File enrollFolder = new File(enrollImgPath);
        if (!enrollFolder.exists()) {
            enrollFolder.mkdirs();
        }

        File verifyFolder = new File(verifyImgPath);
        if (!verifyFolder.exists()) {
            verifyFolder.mkdirs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
//            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    initFolder();
//                } else {
//                    checkStoragePermission();
//                }
//                break;
            case PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    checkCameraPermission();
                }
                break;
            case PERMISSIONS_READ_PHONE_STATE:
                checkStorage(this);
                break;
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initFolder();
                    checkNetWork(this);
                } else {
                    checkStoragePermission();
                }
                break;
        }
    }

    public static void writeWakeLock() {

        String path = "/sys/power/wake_lock";
        BufferedWriter bw = null;	// default buffer size = 8192
        try {
            bw = new BufferedWriter(new FileWriter(path));
            bw.write("dont_sleep");
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            Log.d("writeWakeLock", "IOException : " + e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    Log.d("writeWakeLock", "IOException : " + e);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            SettingDialog dialog = new SettingDialog(this);
            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static void checkPermissions(final Context context) {
        // Read phone state permission
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)context,
                    Manifest.permission.READ_PHONE_STATE)) {
                new AlertDialog.Builder(context)
                        .setTitle("Information!")
                        .setMessage("Read phone state permission needs to be allowed.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions((Activity)context,
                                        new String[]{Manifest.permission.READ_PHONE_STATE},
                                        PERMISSIONS_READ_PHONE_STATE);
                            }
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions((Activity)context,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        PERMISSIONS_READ_PHONE_STATE);
            }
        }
        else
        {
            checkStorage(context);
        }
    }

    private static void checkStorage(final Context context)
    {
        String TAG = "";
        // Write external storage permission
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(context)
                        .setTitle("Information!")
                        .setMessage("Access external storage permission needs to be allowed.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions((Activity)context,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                            }
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions((Activity)context,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
        else
        {
            checkNetWork(context);
        }
    }

    private static void checkNetWork(final Context context)
    {
        String TAG="";
        // Access network state permission
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)context,
                    Manifest.permission.ACCESS_NETWORK_STATE)) {
                new AlertDialog.Builder(context)
                        .setTitle("Information!")
                        .setMessage("Access network state permission needs to be allowed.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions((Activity)context,
                                        new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                                        PERMISSIONS_ACCESS_NETWORK_STATE);
                            }
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions((Activity)context,
                        new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                        PERMISSIONS_ACCESS_NETWORK_STATE);
            }
        }
    }
}
