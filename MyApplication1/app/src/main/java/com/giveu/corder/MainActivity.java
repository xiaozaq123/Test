package com.giveu.corder;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.giveu.facelive.GenerateSign;
import com.giveu.facelive.HttpRequestCallBack;
import com.giveu.facelive.HttpRequestManager;
import com.google.gson.Gson;
import com.megvii.meglive_sdk.listener.DetectCallback;
import com.megvii.meglive_sdk.listener.PreCallback;
import com.megvii.meglive_sdk.manager.MegLiveManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

import static android.os.Build.VERSION_CODES.M;


/**
 * 使用说明
 * 使用前请商家申请自己的apiKey 和 secret
 * 在使用无源对比是需要补全比对所需要的底库图片
 * 在使用有源比对时需要补全身份号和姓名
 * demo中所包含的两个网络请求和签名建议商家放在自己服务器端去做，确保自己的apikey和secret安全
 * 网络请求中只包含了必选字段 商家需要根据自己需要完善 详细说明参考API文档
 */

public class MainActivity extends Activity implements View.OnClickListener, DetectCallback, PreCallback {
    private static final int ACTION_YY = 1;
    private static final int ACTION_WY = 2;
    private static final int FMP_YY = 3;
    private static final int FMP_WY = 4;
    private Button bt_action_yy, bt_action_wy, bt_fmp_yy, bt_fmp_wy;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int EXTERNAL_STORAGE_REQ_WRITE_EXTERNAL_STORAGE_CODE = 101;
    private ProgressDialog mProgressDialog;
    private MegLiveManager megLiveManager;
    private static final String GET_BIZTOKEN_URL = "https://api.megvii.com/faceid/v3/sdk/get_biz_token";
    private static final String VERIFY_URL = "https://api.megvii.com/faceid/v3/sdk/verify";
    private static final String API_KEY = "A7mQNO43bGNz_s3SDjWK59AAaGuESEKW";
    private static final String SECRET = "E7jlWJnz0qOoz9BqRKhrVr-q1APyhoEN";
    private String sign = "";
    private static final String SIGN_VERSION = "hmac_sha1";
    private byte[] imageRef;//底库图片
    private int buttonType;//1

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        init();

    }

    private void init() {
        megLiveManager = MegLiveManager.getInstance();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);


        bt_action_yy = (Button) findViewById(R.id.bt_action_yy);
        bt_action_yy.setOnClickListener(this);
        bt_action_wy = (Button) findViewById(R.id.bt_action_wy);
        bt_action_wy.setOnClickListener(this);
        bt_fmp_yy = (Button) findViewById(R.id.bt_fmp_yy);
        bt_fmp_yy.setOnClickListener(this);
        bt_fmp_wy = (Button) findViewById(R.id.bt_fmp_wy);
        bt_fmp_wy.setOnClickListener(this);

        long currtTime = System.currentTimeMillis() / 1000;
        long expireTime = (System.currentTimeMillis() + 60 * 60 * 100) / 1000;
        sign = GenerateSign.appSign(API_KEY, SECRET, currtTime, expireTime);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.bt_action_yy) {
            Uri uri = Uri.parse("http://fastdfs.dafysz.cn//group2/M00/77/46/CgsMMFvcdMOATeEiAAIN9iS6Yl8749.pdf");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
//            buttonType = ACTION_YY;
//            requestCameraPerm();
//            final WebView webView = new WebView(this);
//            webView.setWebViewClient(new WebViewClient() {
//                @Override
//                public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                    webView.loadUrl("http://fastdfs.dafysz.cn//group2/M00/77/46/CgsMMFvcdMOATeEiAAIN9iS6Yl8749.pdf");
//                    return true;
//                }
//            });
        }
//        else if (i == com.giveu.facelive.R.id.bt_action_wy) {
//            buttonType = ACTION_WY;
//            requestCameraPerm();
//
//        }
//        if (i == com.giveu.facelive.R.id.bt_fmp_yy) {
//            buttonType = FMP_YY;
//            requestCameraPerm();
//        }
// else if (i == com.giveu.facelive.R.id.bt_fmp_wy) {
//            buttonType = FMP_WY;
//            requestCameraPerm();
//
//        }
    }

    private void requestCameraPerm() {
        if (android.os.Build.VERSION.SDK_INT >= M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                //进行权限请求
                requestPermissions(
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                requestWriteExternalPerm();
            }
        } else {
            beginDetect(buttonType);
        }
    }

    private void requestWriteExternalPerm() {
        if (android.os.Build.VERSION.SDK_INT >= M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //进行权限请求
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_REQ_WRITE_EXTERNAL_STORAGE_CODE);
            } else {
                beginDetect(buttonType);
            }
        } else {
            beginDetect(buttonType);
        }
    }


    private void beginDetect(int type) {
        if (type == ACTION_YY) {
            getBizToken("meglive", 1, "贺英俊", "430781199510100034", "", null);
        }
//        else if (type == ACTION_WY) {
//            getBizToken("meglive", 0, "贺英俊", "430781199510100034", UUID.randomUUID().toString(), imageRef);
//        }
//        if (type == FMP_YY) {
//            getBizToken("still", 1, "贺英俊", "430781199510100034", "", null);
//        }
// else if (type == FMP_WY) {
//            getBizToken("still", 0, "贺英俊", "430781199510100034", UUID.randomUUID().toString(), imageRef);
//        }
    }

    private static byte[] bitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imgBytes = baos.toByteArray();
        return imgBytes;
    }

    private void getBizToken(String livenessType, int comparisonType, String idcardName, String idcardNum, String uuid, byte[] image) {
        mProgressDialog.show();
        HttpRequestManager.getInstance().getBizToken(this, GET_BIZTOKEN_URL, sign, SIGN_VERSION, livenessType, comparisonType, idcardName, idcardNum, uuid, image, new HttpRequestCallBack() {

            @Override
            public void onSuccess(String responseBody) {
                try {
                    JSONObject json = new JSONObject(responseBody);
                    String bizToken = json.optString("biz_token");
                    megLiveManager.preDetect(MainActivity.this, bizToken, MainActivity.this);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, byte[] responseBody) {
                Log.w("result", "result: "+new String(responseBody));
            }
        });
    }

    @Override
    public void onDetectFinish(String token, int errorCode, String errorMessage, String data) {
        if (errorCode == 1000) {
            verify(token, data.getBytes());
        }
    }

    @Override
    public void onPreStart() {
        showDialogDismiss();
    }

    @Override
    public void onPreFinish(String token, int errorCode, String errorMessage) {
        progressDialogDismiss();
        if (errorCode == 1000) {
            megLiveManager.startDetect(this);
        }
    }

    private final String cache_root = getSDcardPath();
    //缓存图片目录，用后可调用deleteAllFile()方法删除目录
    public final String TEMP_IMAGE = cache_root + "/giveU/TEMP_IMAGE";

    public static boolean isHasSdcard() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public String getSDcardPath() {
        String mDir = "";
        try {
            //                    /data/data/com.dfppm.giveu/cache
            mDir = BaseApplication.getInstance().getApplicationContext().getCacheDir().getAbsolutePath();
            if (isHasSdcard()) {
//                /storage/emulated/0/Android/data/com.giveu.corder/cache
                try {
                    String externalCacheDir = BaseApplication.getInstance().getApplicationContext().getExternalCacheDir().getAbsolutePath();
                    if (!TextUtils.isEmpty(externalCacheDir)) {
                        mDir = externalCacheDir;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mDir;
    }

    public static void deleteAllFile(File file) {
        if (file != null && file.exists()) {
            if (file.isFile()) {
                file.delete();
                return;
            }
            if (file.isDirectory()) {
                File[] childFile = file.listFiles();
                if (childFile == null || childFile.length == 0) {
                    file.delete();
                    return;
                }
                for (File f : childFile) {
                    deleteAllFile(f);
                }
                file.delete();
            }
        }
    }

    public static File getDirFile(String path) {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }

        return f;
    }

    private void verify(String token, byte[] data) {
        showDialogDismiss();
        HttpRequestManager.getInstance().verify(this, VERIFY_URL, sign, SIGN_VERSION, token, data, new HttpRequestCallBack() {
            @Override
            public void onSuccess(String responseBody) {
                Log.w("result", responseBody);
                String filePath = TEMP_IMAGE;
                deleteAllFile(new File(TEMP_IMAGE));
                getDirFile(TEMP_IMAGE);
                String fileName = System.currentTimeMillis() + "facelive.png";
                Gson gson = new Gson();
                FaceLiveResponse faceLiveResponse = gson.fromJson(responseBody, FaceLiveResponse.class);
                saveToImgByStr(faceLiveResponse.images.image_best, filePath, fileName);
                progressDialogDismiss();

            }

            @Override
            public void onFailure(int statusCode, byte[] responseBody) {
                Log.w("result", new String(responseBody));
                progressDialogDismiss();
            }
        });
    }

    private void progressDialogDismiss() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    private void showDialogDismiss() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.show();
                }
            }
        });

    }

    /**
     * 将接收的字符串转换成图片保存
     *
     * @param imgStr  二进制流转换的字符串
     * @param imgPath 图片的保存路径
     * @param imgName 图片的名称
     * @return 1：保存正常
     * 0：保存失败
     */
    public static int saveToImgByStr(String imgStr, String imgPath, String imgName) {

        int stateInt = 1;
        if (imgStr != null && imgStr.length() > 0) {
            try {
                // 将字符串转换成二进制，用于显示图片
                // 将上面生成的图片格式字符串 imgStr，还原成图片显示
                byte[] imgByte = Base64.decode(imgStr, Base64.DEFAULT);
                InputStream in = new ByteArrayInputStream(imgByte);

                File file = new File(imgPath, imgName);//可以是任何图片格式.jpg,.png等
                FileOutputStream fos = new FileOutputStream(file);

                byte[] b = new byte[1024];
                int nRead = 0;
                while ((nRead = in.read(b)) != -1) {
                    fos.write(b, 0, nRead);
                }
                fos.flush();
                fos.close();
                in.close();

            } catch (Exception e) {
                stateInt = 0;
                e.printStackTrace();
            } finally {
            }
        }
        return stateInt;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length < 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //拒绝了权限申请
            } else {
                requestWriteExternalPerm();
            }
        } else if (requestCode == EXTERNAL_STORAGE_REQ_WRITE_EXTERNAL_STORAGE_CODE) {
            if (grantResults.length < 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //拒绝了权限申请
            } else {
                beginDetect(buttonType);
            }
        }
    }
}
