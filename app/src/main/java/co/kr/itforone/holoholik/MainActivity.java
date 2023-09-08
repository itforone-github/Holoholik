package co.kr.itforone.holoholik;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.snackbar.Snackbar;
import com.lukedeighton.wheelview.WheelView;
import com.lukedeighton.wheelview.adapter.WheelAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import co.kr.itforone.holoholik.databinding.ActivityMainBinding;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import util.BackPressCloseHandler;
import util.Common;
import util.NetworkCheckReceiver;
import util.PermissionCheck;

public class MainActivity extends AppCompatActivity {
    final int WEBREQUESTCODE = 1010;
    ActivityMainBinding dataBiding;
    //휠 메뉴 이미지들
    public int drawableArr[] = {
            R.drawable.main_icon01,
            R.drawable.main_icon06,
            R.drawable.main_icon03,
    };
    public int drawableArr2[] = {
            R.drawable.main_icon01,
            R.drawable.main_icon06,
            R.drawable.main_icon03,
    };
    //원메뉴 선택된 번호
    int selectedPosition=0;
    //리모컨 번호
    int remoconPosition = 0;
    Socket socket;
    public ConnectivityManager connectivityManager;
    boolean isMainBtnTouch=true;
    boolean isIndex = true;
    final int FILECHOOSER_NORMAL_REQ_CODE = 1200,FILECHOOSER_LOLLIPOP_REQ_CODE=1300;
    ValueCallback<Uri> filePathCallbackNormal;
    ValueCallback<Uri[]> filePathCallbackLollipop;
    Uri mCapturedImageURI;
    String firstUrl = "";
    //유튜브 아이디
    public static String youtubeId="";
    //인스타그램 url
    public static String instagramUrl="";
    //뒤로가기를 할때 쓰는 클래스
    private BackPressCloseHandler backPressCloseHandler;
    //youtube 터치
    boolean isYoutubeTouch=false;
    PermissionCheck permissionCheck;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent startIntent = new Intent(co.kr.itforone.holoholik.MainActivity.this, SplashActivity.class);
        startActivity(startIntent);
        //유튜브 아이디 가져오기
        if(!Common.getPref(co.kr.itforone.holoholik.MainActivity.this,"youtubeId","").equals("")) {
            youtubeId = Common.getPref(co.kr.itforone.holoholik.MainActivity.this, "youtubeId", "");
        }
        Log.d("youtubeId",youtubeId+"1");
        //유튜브 아이디 가져오기
        if(!Common.getPref(co.kr.itforone.holoholik.MainActivity.this,"instagramUrl","").equals("")) {
            instagramUrl = Common.getPref(co.kr.itforone.holoholik.MainActivity.this, "instagramUrl", "");
        }
        //소켓통신 연결시키기
        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        try{
            socket = IO.socket("http://14.48.175.184:8020");
        }catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.connect();


        //데이터 바인딩 연결하기
        dataBiding= DataBindingUtil.setContentView(this,R.layout.activity_main);
        dataBiding.setMainData(this);
        //뒤로가기 설정
        backPressCloseHandler = new BackPressCloseHandler(dataBiding.mainLayout,this);


        dataBiding.wheelView.setAdapter(new WheelAdapter() {
            @Override
            public Drawable getDrawable(int position) {
                for(int i=0;i<drawableArr.length;i++){
                    //getResources().getDrawable(drawableArr[i]).setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.MULTIPLY);
                }
                //0번째 휠 메뉴 이미지는 처음엔 노란색으로 변경하기
                //getResources().getDrawable(drawableArr[0]).setColorFilter(Color.parseColor("#d8001c"), PorterDuff.Mode.MULTIPLY);
                return getResources().getDrawable(drawableArr[position]);
            }
            @Override
            public int getCount() {
                return drawableArr.length;
            }
        });
        //휠메뉴가 해당각도에 들어왔을 때 이벤트
        dataBiding.wheelView.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectListener() {
            @Override
            public void onWheelItemSelected(WheelView parent, Drawable itemDrawable, int position) {
                Log.d("position",position+"선택");
                for(int i=0;i<drawableArr.length;i++){
                    //dataBiding.wheelView.getWheelItemDrawable(i).setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);;
                }

                //dataBiding.wheelView.getWheelItemDrawable(position).setColorFilter(Color.parseColor("#d8001c"), PorterDuff.Mode.MULTIPLY);;
                dataBiding.wheelImageView.setImageResource(drawableArr2[position]);
                selectedPosition=position;
                for(int i=0;i<drawableArr2.length;i++){
                    if(remoconPosition!=i) {
                        //getResources().getDrawable(drawableArr2[i]).setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    }
                }
            }
        });
        //휠메뉴 클릭이벤트
        dataBiding.wheelView.setOnWheelItemClickListener(new WheelView.OnWheelItemClickListener() {
            @Override
            public void onWheelItemClick(WheelView parent, int position, boolean isSelected) {
                Log.d("position",position+"터치");
                //네트워크 끊겼을 시
                if(NetworkCheckReceiver.isNetwork==false){
                    Snackbar.make(dataBiding.mainLayout,"Network Disconnected", Snackbar.LENGTH_SHORT).show();
                }else {
                    dataBiding.wheelImageView.setImageResource(drawableArr2[position]);
                    selectedPosition = position;
                    for(int i=0;i<drawableArr2.length;i++){
                        if(remoconPosition!=i) {
                            //getResources().getDrawable(drawableArr2[i]).setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                        }
                    }
                }
            }
        });


        //중앙에 있는 이미지 클릭시
        dataBiding.wheelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //네트워크 끊겼을 시
                if(NetworkCheckReceiver.isNetwork==false){
                    Snackbar.make(dataBiding.mainLayout,"Network Disconnected", Snackbar.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, Common.getPref(MainActivity.this, "ss_mb_id", ""), Toast.LENGTH_SHORT).show();
                    if (!Common.getPref(MainActivity.this, "ss_mb_id", "").equals("")) {
                        Log.d("socket", socket.toString());
                        //중복 터치 못 하게 막기 위한 if 문
                        //if (isMainBtnTouch) {
                        try {
                            JSONObject obj = new JSONObject();
                            /*if(Common.getPref(MainActivity.this,"SignageId","").equals("")||
                                    Common.getPref(MainActivity.this,"SignageId","").equals(null)){
                                Snackbar.make(dataBiding.mainLayout,"Signage DeviceID Register Or Connected.",Snackbar.LENGTH_SHORT).show();
                                return;
                            }*/
                            //obj.put("s_id", Common.getPref(MainActivity.this,"SignageId",""));
                            obj.put("mb_id",Common.getPref(MainActivity.this, "ss_mb_id", ""));
                            obj.put("s_id", Common.getPref(co.kr.itforone.holoholik.MainActivity.this, "s_id", ""));
                            Log.d("select-position", selectedPosition + "");
                            obj.put("data", selectedPosition + "");
                            Toast.makeText(MainActivity.this, ""+Common.getPref(co.kr.itforone.holoholik.MainActivity.this, "s_id", ""), Toast.LENGTH_SHORT).show();
                            //유튜브일 때
                            if (selectedPosition == 2) {
                                if (!youtubeId.equals("")) {
                                    obj.put("youtubeId", youtubeId.substring(youtubeId.lastIndexOf("/") + 1, youtubeId.length()));
                                    socket.emit("message", obj);
                                    dataBiding.volumeLayout.setVisibility(View.VISIBLE);//볼륨 버튼 레이아웃 보여주기
                                } else {
                                    Snackbar.make(dataBiding.mainLayout, "not Youtube", Snackbar.LENGTH_SHORT).show();
                                    dataBiding.volumeLayout.setVisibility(View.GONE);//볼륨 레이아웃 감추기
                                }
                                //인스타그램일때
                            } else if (selectedPosition == 3) {
                                isYoutubeTouch = false;
                                if (!instagramUrl.equals("")) {
                                    obj.put("instagramUrl", instagramUrl);
                                    socket.emit("message", obj);
                                    dataBiding.volumeLayout.setVisibility(View.VISIBLE);//볼륨 버튼 레이아웃 보여주기
                                } else {
                                    Snackbar.make(dataBiding.mainLayout, "not Instagram", Snackbar.LENGTH_SHORT).show();
                                    dataBiding.volumeLayout.setVisibility(View.GONE);//볼륨 레이아웃 감추기
                                }
                            } else if (selectedPosition == 4) {
                                obj.put("viewType", "");
                                socket.emit("message", obj);
                            } else {
                                isYoutubeTouch = false;
                                socket.emit("message", obj);
                                dataBiding.volumeLayout.setVisibility(View.GONE);//볼륨 레이아웃 감추기
                            }

                        } catch (JSONException e) {
                            Toast.makeText(co.kr.itforone.holoholik.MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        isMainBtnTouch = false;
                        mHandler.sendEmptyMessageDelayed(0, 3000);
                        Log.d("color", "color");

                        //dataBiding.wheelImageView.getDrawable().setColorFilter(Color.parseColor("#e3e300"), PorterDuff.Mode.MULTIPLY);
                        remoconPosition = selectedPosition;
                   /* } else {
                        Snackbar.make(dataBiding.mainLayout, "Touch in 3 seconds", Snackbar.LENGTH_SHORT).show();
                        dataBiding.volumeLayout.setVisibility(View.GONE);//볼륨 레이아웃 감추기
                    }*/
                    }else {
                        Snackbar.make(dataBiding.mainLayout,"You can use it after logging in.",Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
        //볼륨 높이기 버튼을 눌렀을 때 소켓통신
        dataBiding.volUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("s_id", Common.getPref(co.kr.itforone.holoholik.MainActivity.this,"s_id",""));
                    obj.put("data", "up");
                    socket.emit("message", obj);
                  //  Snackbar.make(dataBiding.mainLayout,"Play the video after adjusting the volume.", BaseTransientBottomBar.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //볼륨 낮추기 버튼을 눌렀을 때 소켓통신
        dataBiding.volDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject obj = new JSONObject();

                try {
                    obj.put("s_id", Common.getPref(co.kr.itforone.holoholik.MainActivity.this,"s_id",""));
                    obj.put("data", "down");
                    socket.emit("message", obj);
                    //Snackbar.make(dataBiding.mainLayout,"Play the video after adjusting the volume.", BaseTransientBottomBar.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        //상단 메뉴바 터치 이벤트
        dataBiding.drawMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //네트워크 연결이 끊겼을 시
                if(NetworkCheckReceiver.isNetwork==false){
                    Snackbar.make(dataBiding.mainLayout,"Network Disconnected", Snackbar.LENGTH_SHORT).show();
                }else {
                    dataBiding.drawerLayout.openDrawer(dataBiding.drawerMenuLayout);
                    dataBiding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                }
            }
        });
        //네트워크 체킹하기
        NetworkCheckReceiver receiver = new NetworkCheckReceiver(connectivityManager);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver,filter);
        Log.d("networkCheck",NetworkCheckReceiver.isNetwork+"");
        //웹뷰 세팅하기
        firstUrl=getString(R.string.domain)+"plugin/hash/basic/aos_menu.php";//웹뷰에 처음 실행할 것을 지정하는 url 주소입니다.
        Intent intent2 = getIntent();
        String action = intent2.getAction();
        String type = intent2.getType();
        Log.d("action",action);

        //공유하기로 들어왔을 때
        if(Intent.ACTION_SEND.equals(action)&&type!=null){
            if("text/plain".equals(type)){
                String sharedText = intent2.getStringExtra(Intent.EXTRA_TEXT);
                Log.d("shared",sharedText);
                if(0 < sharedText.indexOf("youtu")){
                    firstUrl+="?youtubeUrl="+sharedText;
                    this.youtubeId=sharedText;
                    dataBiding.drawerLayout.openDrawer(dataBiding.drawerMenuLayout);
                    dataBiding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                }else if(0 < sharedText.indexOf("instagram")){
                    firstUrl+="?instagramUrl="+sharedText;
                    dataBiding.drawerLayout.openDrawer(dataBiding.drawerMenuLayout);
                    dataBiding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                }else{

                }
            }
        }else if(Intent.ACTION_SEND.equals(action)&&type==null){
            Log.d("type1",type);
        }
        webViewSetting();
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        //드로우 메뉴가 열어져 있을 때는 닫을 수 있게
        if(dataBiding.drawerLayout.isDrawerOpen(dataBiding.drawerMenuLayout)){
            dataBiding.drawerLayout.closeDrawer(dataBiding.drawerMenuLayout);
        }else{
            backPressCloseHandler.onBackPressed();
        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // your code...

        }
    };
    //버튼 클릭시 3초동안 터치 못하게 하는 핸들러
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            isMainBtnTouch=true;
            return false;
        }
    });

    //웹뷰 세팅 메서드
    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR_MR1)
    public void webViewSetting() {
        //Common.setTOKEN(this);
        WebSettings setting = dataBiding.webView.getSettings();//웹뷰 세팅용
        if(Build.VERSION.SDK_INT >= 21) {
            dataBiding.webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
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
        setting.setTextZoom(100);
        setting.setSupportZoom(true);
        dataBiding.webView.setWebChromeClient(chrome);//웹에서 경고창이나 또는 컴펌창을 띄우기 위한 메서드
        dataBiding.webView.setWebViewClient(client);//웹페이지 관련된 메서드 페이지 이동할 때 또는 페이지가 로딩이 끝날 때 주로 쓰임
        setting.setUserAgentString("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Mobile Safari/537.36"+"/AGold");
        dataBiding.webView.addJavascriptInterface(new WebJavascriptEvent(), "Android");
        dataBiding.webView.loadUrl(firstUrl);

    }
    WebChromeClient chrome;
    {
        chrome = new WebChromeClient() {
            //새창 띄우기 여부
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                return false;
            }

            //경고창 띄우기
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(co.kr.itforone.holoholik.MainActivity.this)
                        .setMessage("\n" + message + "\n")
                        .setCancelable(false)
                        .setPositiveButton("확인",
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
                new AlertDialog.Builder(co.kr.itforone.holoholik.MainActivity.this)
                        .setMessage("\n" + message + "\n")
                        .setCancelable(false)
                        .setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("취소",
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
                AlertDialog.Builder builder = new AlertDialog.Builder(co.kr.itforone.holoholik.MainActivity.this);
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
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_NORMAL_REQ_CODE);
            }

            // For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }


            // For Android 5.0+\
            // SDK 21 이상부터 웹뷰에서 파일 첨부를 해주는 기능입니다.
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {


                //카메라 프로바이더로 이용해서 파일을 가져오는 방식입니다.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {// API 24 이상 일경우..
                    File imageStorageDir = new File(co.kr.itforone.holoholik.MainActivity.this.getFilesDir() + "/Pictures", "main_image");
                    if (!imageStorageDir.exists()) {
                        // Create AndroidExampleFolder at sdcard
                        imageStorageDir.mkdirs();
                    }
                    // Create camera captured image file path and name

                    //Toast.makeText(mainActivity.getApplicationContext(),imageStorageDir.toString(),Toast.LENGTH_LONG).show();
                    File file = new File(imageStorageDir, "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                    Uri providerURI = FileProvider.getUriForFile(co.kr.itforone.holoholik.MainActivity.this, co.kr.itforone.holoholik.MainActivity.this.getPackageName() + ".provider", file);
                    mCapturedImageURI = providerURI;

                } else {// API 24 미만 일경우..

                    File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "main_image");
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
                i.setType("image/*");

                // Create file chooser intent
                Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
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
                Intent intent = new Intent(co.kr.itforone.holoholik.MainActivity.this, co.kr.itforone.holoholik.WebActivity.class);
                intent.putExtra("firstUrl",url);
                startActivityForResult(intent,WEBREQUESTCODE);
                return true;
            }
            //페이지 로딩이 다 끝났을 때
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("url",url);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.getInstance().sync();
                } else {
                    CookieManager.getInstance().flush();
                }
                Log.d("mb_id", Common.getPref(co.kr.itforone.holoholik.MainActivity.this,"ss_mb_id",""));
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
                dataBiding.webView.loadUrl("javascript:getUrl('"+youtubeId+"','"+instagramUrl+"');");

            }
            //페이지 오류가 났을 때 6.0 이후에는 쓰이지 않음
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }
        };
    }
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
        public void drawerClose(){
            try{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dataBiding.drawerLayout.closeDrawer(dataBiding.drawerMenuLayout);
                        dataBiding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    }
                });

            }catch (Exception e){
                Log.d("error",e.toString());
            }
        }

        @JavascriptInterface
        public void saveUrl(String y,String i){
            try{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Common.savePref(co.kr.itforone.holoholik.MainActivity.this,"youtubeId",y);
                        co.kr.itforone.holoholik.MainActivity.youtubeId=y;
                        Common.savePref(co.kr.itforone.holoholik.MainActivity.this,"instagramUrl",i);
                        co.kr.itforone.holoholik.MainActivity.instagramUrl=i;
                        dataBiding.drawerLayout.closeDrawer(dataBiding.drawerMenuLayout);
                        Snackbar.make(dataBiding.mainLayout,"Has been Saved", Snackbar.LENGTH_SHORT).show();
                    }
                });

            }catch (Exception e){
                Log.d("error",e.toString());
            }
        }

        //사이니지 등록
        @JavascriptInterface
        public void setSignageDeviceId(){

            String signageId=Common.getPref(co.kr.itforone.holoholik.MainActivity.this,"SignageId","");
            AlertDialog.Builder builder=new AlertDialog.Builder(co.kr.itforone.holoholik.MainActivity.this);
            LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
            View view=inflater.inflate(R.layout.dialog_signage,null);
            EditText signageEdit = view.findViewById(R.id.signageEdit);
            signageEdit.setText(signageId);
            builder.setView(view);
            builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Common.savePref(co.kr.itforone.holoholik.MainActivity.this,"SignageId",signageEdit.getText().toString());


                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==WEBREQUESTCODE){
            if(resultCode==RESULT_OK){
                dataBiding.webView.reload();

            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        socket.connect();
        socket.open();
    }
    @Override
    protected void onPause() {
        super.onPause();
        socket.disconnect();
        socket.close();
    }
}