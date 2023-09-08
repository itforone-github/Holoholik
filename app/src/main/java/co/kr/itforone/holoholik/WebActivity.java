package co.kr.itforone.holoholik;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;

import co.kr.itforone.holoholik.databinding.ActivityWebBinding;
import util.BackPressCloseHandler;
import util.Common;
import util.PermissionCheck;

public class WebActivity extends AppCompatActivity {
    ActivityWebBinding binding;
    private BackPressCloseHandler backPressCloseHandler;
    boolean isIndex = true;
    final int FILECHOOSER_NORMAL_REQ_CODE = 1200,FILECHOOSER_LOLLIPOP_REQ_CODE=1300;
    ValueCallback<Uri> filePathCallbackNormal;
    ValueCallback<Uri[]> filePathCallbackLollipop;
    Uri mCapturedImageURI;
    String firstUrl = "";
    PermissionCheck permissionCheck;
    boolean isGallery=false;
    boolean isVideo = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_web);
        binding.setWebData(this);
        //화면을 계속 켜짐
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 웹뷰를 실행할 때 메모리 누수가 심하지 않게 설정하는 것
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        Intent intent = getIntent();
        firstUrl=intent.getExtras().getString("firstUrl");
        //푸시가 있을 때는 아래에 있는 소스가 실행이 됩니다.
        /* cookie */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }

        webViewSetting();
    }
    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR_MR1)
    public void webViewSetting() {
        //Common.setTOKEN(this);
        WebSettings setting = binding.webView.getSettings();//웹뷰 세팅용
        if(Build.VERSION.SDK_INT >= 21) {
            binding.webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        setting.setAllowFileAccess(true);//웹에서 파일 접근 여부
        setting.setAppCacheEnabled(true);//캐쉬 사용여부
        setting.setGeolocationEnabled(true);//위치 정보 사용여부
        setting.setDatabaseEnabled(true);//HTML5에서 db 사용여부
        setting.setDomStorageEnabled(true);//HTML5에서 DOM 사용여부
        setting.setCacheMode(WebSettings.LOAD_DEFAULT);//캐시 사용모드 LOAD_NO_CACHE는 캐시를 사용않는다는 뜻
        setting.setJavaScriptEnabled(true);//자바스크립트 사용여부
        setting.setSupportMultipleWindows(false);//윈도우 창 여러개를 사용할 것인지의 여부 무조건 false로 하는 게 좋음
        setting.setUseWideViewPort(true);//웹에서 view port 사용여부
        // 웹뷰에 동영상을 바로 실행시키기 위함.
        setting.setMediaPlaybackRequiresUserGesture(false);


        setting.setTextZoom(100);
        setting.setSupportZoom(true);
        binding.webView.setWebChromeClient(chrome);//웹에서 경고창이나 또는 컴펌창을 띄우기 위한 메서드
        binding.webView.setWebViewClient(client);//웹페이지 관련된 메서드 페이지 이동할 때 또는 페이지가 로딩이 끝날 때 주로 쓰임
        setting.setUserAgentString("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Mobile Safari/537.36"+"/Holoholik");
        binding.webView.addJavascriptInterface(new WebJavascriptEvent(), "Android");
        binding.webView.loadUrl(firstUrl);
    }

    WebChromeClient chrome;
    {
        chrome = new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {


                super.onShowCustomView(view, callback);
            }

            //새창 띄우기 여부
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                return false;
            }

            //경고창 띄우기
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(co.kr.itforone.holoholik.WebActivity.this)
                        .setMessage("\n" + message + "\n")
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.confirm();
                                    }
                                }).create().show();
                return true;
            }

            //컴펌 띄우기
            @Override
            public boolean onJsConfirm(WebView view, String url, String message,
                                       final JsResult result) {
                new AlertDialog.Builder(co.kr.itforone.holoholik.WebActivity.this)
                        .setMessage("\n" + message + "\n")
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("CANCEL",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.cancel();
                                    }
                                }).create().show();
                return true;
            }

            //현재 위치 정보 사용여부 묻기
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                // Should implement this function.
                final String myOrigin = origin;
                final GeolocationPermissions.Callback myCallback = callback;
                AlertDialog.Builder builder = new AlertDialog.Builder(co.kr.itforone.holoholik.WebActivity.this);
                builder.setTitle("Request message");
                builder.setMessage("Allow current location?");
                builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        myCallback.invoke(myOrigin, true, false);
                    }

                });
                builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        myCallback.invoke(myOrigin, false, false);
                    }

                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                filePathCallbackNormal = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_NORMAL_REQ_CODE);
            }

            // For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }


            // For Android 5.0+\
            // SDK 21 이상부터 웹뷰에서 파일 첨부를 해주는 기능입니다.
            @RequiresApi(api = Build.VERSION_CODES.M)
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    String permisions[] = {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
                    permissionCheck = new PermissionCheck(co.kr.itforone.holoholik.WebActivity.this);
                    permissionCheck.setPermission(permisions,"When attaching cameras and files, permission setting is mandatory.");
                    return false;
                }


                //카메라 프로바이더로 이용해서 파일을 가져오는 방식입니다.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {// API 24 이상 일경우..
                    File imageStorageDir = new File(co.kr.itforone.holoholik.WebActivity.this.getFilesDir() + "/Pictures", "holo_photo");
                    if (!imageStorageDir.exists()) {
                        // Create AndroidExampleFolder at sdcard
                        imageStorageDir.mkdirs();
                    }
                    // Create camera captured image file path and name

                    //Toast.makeText(mainActivity.getApplicationContext(),imageStorageDir.toString(),Toast.LENGTH_LONG).show();
                    File file = new File(imageStorageDir, "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                    Uri providerURI = FileProvider.getUriForFile(co.kr.itforone.holoholik.WebActivity.this, co.kr.itforone.holoholik.WebActivity.this.getPackageName() + ".provider", file);
                    mCapturedImageURI = providerURI;

                } else {// API 24 미만 일경우..

                    File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "holo_photo");
                    if (!imageStorageDir.exists()) {
                        // Create AndroidExampleFolder at sdcard
                        imageStorageDir.mkdirs();
                    }
                    // Create camera captured image file path and name
                    File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                    mCapturedImageURI = Uri.fromFile(file);
                }
                if (filePathCallbackLollipop != null) {
//                    filePathCallbackLollipop.onReceiveValue(null);
                    filePathCallbackLollipop = null;
                }
                filePathCallbackLollipop = filePathCallback;
                Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                if(isVideo==false) {
                    i.setType("image/*");
                }else{
                    i.setType("video/*");
                }

                // Create file chooser intent
                Intent chooserIntent = Intent.createChooser(i, "File Chooser");
                // Set camera intent to file chooser
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});

                // On select image call onActivityResult method of activity
                startActivityForResult(chooserIntent, FILECHOOSER_LOLLIPOP_REQ_CODE);
                return true;

            }

        };
    }

    WebViewClient client;
    {
        client = new WebViewClient() {



            //페이지 로딩중일 때 (마시멜로) 6.0 이후에는 쓰지 않음
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Log.d("url",url);
                if (url.equals(getString(R.string.url)) || url.equals(getString(R.string.domain))) {
                    isIndex=true;
                } else {
                    isIndex=false;
                }
                if(url.startsWith("tel:")){
                    Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(tel);
                    return true;
                }
                if (url.startsWith("intent:")) {

                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                        if (existPackage != null) {
                            startActivity(intent);
                        } else {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                            marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                            startActivity(marketIntent);
                        }
                        return true;
                    } catch (Exception e) {
                        Log.d("error1",e.toString());
                        e.printStackTrace();
                    }
                }
                return false;
            }
            //페이지 로딩이 다 끝났을 때
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                binding.webLayout.setRefreshing(false);

                Log.d("URLTAG",url);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.getInstance().sync();
                } else {
                    CookieManager.getInstance().flush();
                }
                Log.d("mb_id", Common.getPref(co.kr.itforone.holoholik.WebActivity.this,"ss_mb_id",""));
                //로그인할 때
                if(url.startsWith(getString(R.string.domain)+"bbs/login.php")||url.startsWith(getString(R.string.domain)+"bbs/register_form.php")){
                    //view.loadUrl("javascript:fcmKey('"+ Common.TOKEN+"')");
                }

                if (url.equals(getString(R.string.url)) || url.equals(getString(R.string.domain))) {
                    isIndex=true;
                } else {
                    isIndex=false;
                }
                Log.d("isIndex",isIndex+"");
                binding.webLayout.setRefreshing(false);
                //메인 화면이 아닌 페이지는 새로고침을 할 수 있고
                if(isIndex==false) {
                    binding.webLayout.setEnabled(true);
                    binding.webLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            binding.webView.clearCache(true);
                            binding.webView.reload();
                            binding.webLayout.setRefreshing(false);
                        }
                    });
                    //메인화면이면은 새로고침을 할 수 없습니다.
                }else{
                    binding.webLayout.setEnabled(false);
                }
            }
            //페이지 오류가 났을 때 6.0 이후에는 쓰이지 않음
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }
        };
    }
    //다시 들어왔을 때
    @Override
    protected void onResume() {
        super.onResume();
        /* cookie */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }




        Log.d("newtork","onResume");


        //netCheck.networkCheck();
    }
    //홈버튼 눌러서 바탕화면 나갔을 때
    @Override
    protected void onPause() {
        super.onPause();

        /* cookie */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().stopSync();
        }

    }
    //뒤로가기를 눌렀을 때
    public void onBackPressed() {
        //웹뷰에서 히스토리가 남아있으면 뒤로가기 함
        Log.d("isIndex",isIndex+"");

        if(binding.webView.canGoBack()) {
            binding.webView.goBack();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //여기서부터 카메라와 갤러리에서 가져오는 값이 조금씩 달라집니다.
        Uri[] results = null;
        if(resultCode==RESULT_OK) {
            //롤리팝 이전 버전 소스는 일단 뺐습니다.
            if(requestCode == FILECHOOSER_LOLLIPOP_REQ_CODE){
                Uri[] result = new Uri[0];
                Log.d("filePath1", mCapturedImageURI.toString());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {


                    if (resultCode == RESULT_OK) {
                        result = (intent == null) ? new Uri[]{mCapturedImageURI} : WebChromeClient.FileChooserParams.parseResult(resultCode, intent);
                    }
                    if(isVideo) {
                        filePathCallbackLollipop.onReceiveValue(result);
                        isVideo=false;
                    }else {
                        //크롭 액티비티로 이동
                        if (!isGallery) {
                            CropImage.activity(result[0])
                                    .setGuidelines(CropImageView.Guidelines.ON)//가이드라인을 보여줄 것인지 여부
                                    .setAspectRatio(1, 1)//가로 세로 1:1로 자르기 기능 * 1:1 4:3 16:9로 정해져 있어요
                                    .start(this);
                        } else {
                            CropImage.activity(result[0])
                                    .setGuidelines(CropImageView.Guidelines.ON)//가이드라인을 보여줄 것인지 여부
                                    .setAspectRatio(1, 1)//가로 세로 1:1로 자르기 기능 * 1:1 4:3 16:9로 정해져 있어요
                                    .setCropShape(CropImageView.CropShape.OVAL)//사각형과 동그라미를 선택할 수 있어요 OVAL은 동그라미 RECTAGLE은 사각형이예요 안 넣으면 사각형이예요
                                    .start(this);
                        }
                    }


                }


            }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

                CropImage.ActivityResult result = CropImage.getActivityResult(intent);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    Uri[] result2 = new Uri[0];
                    result2 =  new Uri[]{resultUri} ;

                    Log.d("image-uri", resultUri.toString());
                    filePathCallbackLollipop.onReceiveValue(result2);
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }

        }else{
            try {
                if (filePathCallbackLollipop != null) {
                    filePathCallbackLollipop.onReceiveValue(null);
                    filePathCallbackLollipop = null;
                }
            }catch (Exception e){

            }
        }
    }

    //로그인 로그아웃
    class WebJavascriptEvent{
        @JavascriptInterface
        public void setLogin(String mb_id){
            Log.d("login","로그인");
            Common.savePref(getApplicationContext(),"ss_mb_id",mb_id);
        }
        @JavascriptInterface
        public void setLogout(){
            Log.d("logout","로그아웃");
            Common.savePref(getApplicationContext(),"ss_mb_id","");
        }
        @JavascriptInterface
        public void doShare(String url){
            Intent sharedIntent = new Intent();
            sharedIntent.setAction(Intent.ACTION_SEND);
            sharedIntent.setType("text/plain");
            sharedIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            sharedIntent.putExtra(Intent.EXTRA_TEXT, url);
            Intent chooser = Intent.createChooser(sharedIntent, "공유");
            startActivity(chooser);
        }
        @JavascriptInterface
        public void ActivityFinish(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.putExtra("result","ok");
                    setResult(RESULT_OK,intent);
                   finish();
                }
            });
        }

        @JavascriptInterface
        public void ActivityFinish(String mb_id){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("login", mb_id);
                    Common.savePref(getApplicationContext(), "ss_mb_id", mb_id);
                    Intent intent = new Intent();
                    intent.putExtra("result","ok");
                    setResult(RESULT_OK,intent);
                    finish();
                }
            });
        }
        @JavascriptInterface
        public void saveDevice(String device){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(co.kr.itforone.holoholik.WebActivity.this, device, Toast.LENGTH_SHORT).show();
                    Common.savePref(co.kr.itforone.holoholik.WebActivity.this,"s_id",device);
                    JSONObject obj=new JSONObject();
                    /*try {
                        obj.put("s_id",device);
                        obj.put("data","connect");
                        socket.emit("message",obj);
                    }catch (JSONException e){
                    }*/

                    Intent intent = new Intent();
                    intent.putExtra("result","ok");
                    intent.putExtra("connect","ok");
                    setResult(RESULT_OK,intent);
                    finish();
                }
            });
        }
        @JavascriptInterface
        public void setIsGallery(String bool){
            isGallery=Boolean.parseBoolean(bool);
        }
        @JavascriptInterface
        public void setIsVideo() {
            isVideo=true;
        }
        @JavascriptInterface
        public void setIsNoVideo() {
            isVideo=false;
        }
    }
}