package com.example.airshare;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {
    String path = "/storage/sdcard/Download/Screenshot_1623772342.png";

    TextView pathShow;
    String fileName = "aaa.png";
    // 文件服务器
    private String Url = "http://10.28.213.89:9999/upload";
    private String UrlD = "http://10.28.213.89:9999/download";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 选择上传文件按钮
        Button btn = (Button) this.findViewById(R.id.file_select_btn);
        // 注册上传文件按钮监听回调函数
        btn.setOnClickListener(v -> onFileSelectBtnSelect(v));
        pathShow = (TextView) this.findViewById(R.id.path_show_textview);
        // 下载
        Button btn_download = (Button) this.findViewById(R.id.file_download_btn);
        btn_download.setOnClickListener(v->download(v));

    }
    public void download(View v){
        new Thread((Runnable) () -> {
            System.out.println("**********************************************************");
            // 获取下载码
            EditText da = (EditText) findViewById(R.id.download_access);
            String download_access = da.getText().toString();


            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder().
                    setType(MultipartBody.FORM).
                    addFormDataPart("access", download_access).
                    build();
            Request request = new Request.Builder().url(UrlD).post(requestBody).build();
            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                BufferedInputStream bis = new BufferedInputStream(responseBody.byteStream());
//                File file = new File("/sdcard/Download", "download.png");
//                FileOutputStream fos = new FileOutputStream(file);

                Toast.makeText(MainActivity.this, "已下载到/sdcard/Download", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ).start();



//        Toast.makeText(MainActivity.this, download_access, Toast.LENGTH_SHORT).show();
    }
    // 选择文件回调
    protected void onFileSelectBtnSelect(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
            int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualimagecursor.moveToFirst();
            String img_path = actualimagecursor.getString(actual_image_column_index);
//            this.path = img_path;
            this.ConfirmDialog();
            pathShow.setText(img_path);
            Toast.makeText(MainActivity.this, this.path, Toast.LENGTH_SHORT).show();
        }
    }

    private void ConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("警告")
                .setMessage("严禁上传违法违规文件(包括但不限于:内容涉及低俗、色情、政治敏感、翻墙、暴力、恶意软件、外挂等文件)")
                .setNeutralButton("同意并继续", (dialog, which) -> {
                    upload();
                    Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("放弃上传", (dialog, which) -> {
                    this.path = "";
                    return;
                })
                .show();
    }

    // 上传函数
    private void upload() {
        new Thread((Runnable) () -> {
            System.out.println("**********************************************************");
            // 先获取验证码
            EditText editText = (EditText) findViewById(R.id.access_editText);
            String access = editText.getText().toString();

            System.out.println(path);
            OkHttpClient client = new OkHttpClient();
            File file = new File(path);
            RequestBody fileBody = RequestBody.create(MediaType.parse(""), file);
            RequestBody requestBody = new MultipartBody.Builder().
                    setType(MultipartBody.FORM).
                    addFormDataPart("access", access).
                    addFormDataPart("file", fileName, fileBody).build();
            Request request = new Request.Builder().url(Url).post(requestBody).build();
            try {
                Response response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ).start();
    }


}