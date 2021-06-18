package com.example.frontend;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

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
    private String UriLogin = "http://10.28.213.89:9999/login";

    private int cl = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // 选择上传文件按钮
        Button btn = (Button) this.findViewById(R.id.upload);
        // 注册上传文件按钮监听回调函数
        btn.setOnClickListener(v -> onFileSelectBtnSelect(v));
        //pathShow = (TextView) this.findViewById(R.id.download_access);
        // 下载
        Button btn_download = (Button) this.findViewById(R.id.download);
        btn_download.setOnClickListener(v->download(v));

    }


    public void login()
    {

        LayoutInflater layoutInf=LayoutInflater.from(MainActivity.this);
        View loginView=layoutInf.inflate(R.layout.login, null);
//一定要通过loginView.findViewById()来取得控件
        final EditText etUserName=(EditText)loginView.findViewById(R.id.et_user_name);
        final EditText etPwd=(EditText)loginView.findViewById(R.id.et_password);
        AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("用户登录");
//对dialog添加视图
        dialog.setView(loginView);
        dialog.setPositiveButton("登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder().
                        setType(MultipartBody.FORM).
                        addFormDataPart("user_id",etUserName.getText().toString() ).
                        addFormDataPart("password",etPwd.getText().toString()).
                        build();
                Request request = new Request.Builder().url(UriLogin).post(requestBody).build();
                String text="登陆成功！\n用户名:"+etUserName.getText();
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
//创建并显示
        dialog.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }
    boolean issChecked = false;
    boolean isbChecked = false;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.darkmode:
                boolean isdChecked = !item.isChecked();
                item.setChecked(isdChecked);
                View bk = (View) this.findViewById(R.id.layout);
                if(cl == 0) {
                    ColorDrawable grey = new ColorDrawable(0xFF3A3A3A);
                    bk.setBackground(grey);
                }
                else{
                    ColorDrawable grey = new ColorDrawable(0xFFFFFF);
                    bk.setBackground(grey);
                }
                cl=cl^1;
                break;
            case R.id.login:
                login();
                break;
            case R.id.server:
                issChecked = !item.isChecked();
                item.setChecked(issChecked);
                break;
            case R.id.bluetooth:
                isbChecked = !item.isChecked();
                item.setChecked(isbChecked);
                break;
            default:
        }
        return true;
    }

    public void download(View v){
        if(issChecked == false ){
            return;
        }
        Toast.makeText(MainActivity.this, "已下载到/sdcard/Download", Toast.LENGTH_SHORT).show();

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


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ).start();



//        Toast.makeText(MainActivity.this, download_access, Toast.LENGTH_SHORT).show();
    }
    // 选择文件回调
    protected void onFileSelectBtnSelect(View v) {
        if(issChecked == false  & isbChecked == false ){
            return;
        }
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
//            pathShow.setText(img_path);
            Toast.makeText(MainActivity.this, this.path, Toast.LENGTH_SHORT).show();
        }
    }

    private void ConfirmDialog() {
        if(issChecked == false ){
            return;
        }
        new android.app.AlertDialog.Builder(this)
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
        if(issChecked == false & isbChecked == false){
            return;
        }
        new Thread((Runnable) () -> {
            System.out.println("**********************************************************");
            // 先获取验证码
            EditText editText = (EditText) findViewById(R.id.download_access);
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