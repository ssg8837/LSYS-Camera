package com.example.administrator.lsys_camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

public class MainActivity extends AppCompatActivity implements Animation.AnimationListener {

    // Renderer 객체, EGL에 그림을 그려줌
    private Renderer renderer;

    // TextureView, 실시간 프리뷰보여줌, OpenGL에의해 그려지는 그림을 여기에 그림
    private TextureView textureView;

    // AutoFocusing 객체
    private Camera.AutoFocusCallback saveAutoFocus;

    // Renderer에서 쓰임, 예전에 테스트용으로넣은것 (추후삭제가능)
    public static int mainControl = 1;

    // Save오토포커스의 스타트함수에서 쓰임 (플래시를꺼줌)
    private Bitmap bitmap;

    // 메뉴구성 관련
    private Animation mAnimation;
    private View mSlideMenu;

    // Request code for runtime permissions
    private final int REQUEST_CODE_STORAGE_PERMS = 321;

    // 동기화를위한 flag 변수들
    public static boolean pushChangeBtn = false;    // false 일때가 후면카메라
    public static boolean doingCapture = false;     // 촬영중이 오래걸리므로 그때를위한 flag
    public static boolean pushFlashBtn = false;     // false 일때가 Off

    public static boolean textOn = false;
    public static boolean stickerOn = false;

    //스티커 체크용
    public static boolean textureviewTouch=false;
    public static boolean checked=false;
    public static float touchPosX;//터치 위치
    public static float touchPosY;

    public static float stickerPosX;//스티커 위치
    public static float stickerPosY;

    // 이미지버튼, 타이머가 현재 없는 상태
    private ImageView btChange;
    private ImageView btFlash;

    // 타이머 관련
    private int timerTime = 0; // 타이머 시간 값
    private int timerText;
    private  Thread timerThread;
    private Handler mHandler = new Handler();

    //줌 관련
    private double touchDistance; // 양손가락으로 터치할때 그 간격

    // 카메라 권한을 갖고있는지 확인
    // API 23 이상부터는 프로그램 실행중에 권한을 요청받음. (최초1회)
    private boolean hasPermissions() {
        int res = 0;
        // list all permissions which you want to check are granted or not.
        String[] permissions = new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        for (String perms : permissions){
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)){
                // it return false because your app dosen't have permissions.
                return false;
            }

        }
        // it return true, your app has permissions.
        return true;
    }

    // 필수권한요청, 권한요청 물어보기
    private void requestNecessaryPermissions() {
        // make array of permissions which you want to ask from user.
        String[] permissions = new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // have arry for permissions to requestPermissions method.
            // and also send unique Request code.
            // 원래 권한을 요청하는 함수
            requestPermissions(permissions, REQUEST_CODE_STORAGE_PERMS);
        }
    }

    /* when user grant or deny permission then your app will check in
      onRequestPermissionsReqult about user's response. */
    // 권한요청 승인/거절 을 눌렀을때 상황
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grandResults) {
        // this boolean will tell us that user granted permission or not.
        boolean allowed = true;
        switch (requestCode) {
            case REQUEST_CODE_STORAGE_PERMS:
                for (int res : grandResults) {
                    // if user granted all required permissions then 'allowed' will return true.
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                // if user denied then 'allowed' return false
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                allowed = false;
                break;
        }

        // allowed 가 true인것은 사용자가 승인을 누른 상태
        if (allowed) {
            // if user granted permissions then do your work.
            Start();
        }
        else {
            // else give any custom waring message.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    Toast.makeText(MainActivity.this, "Camera Permissions denied", Toast.LENGTH_SHORT).show();
                }
                else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(MainActivity.this, "Storage Permissions denied", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            if (!hasPermissions()) {
                // your app doesn't have permissions, ask for them.
                requestNecessaryPermissions();
            } else {
                // your app already have permissions allowed.
                // do what you want.
                //startCamera();
                // renderer = new Renderer(this);
                Start();
            }
        }
        else {
            // 예외처리, 카메라가 아예 없는 디바이스의 경우임
            Toast.makeText(MainActivity.this, "Camera not supported", Toast.LENGTH_LONG).show();
        }
    }

    // 카메라 타이머
    class Timer extends Thread
    {
        public void run()
        {
            if(timerTime != 0) {
                try {
                    // 5,4,3,2,1 를 나오게함. toast가 딜레이가 있으므로 다른것으로 수정이필요함 (애니메이션같은걸로)
                    for(timerText=timerTime; timerText>0;timerText--) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), " "+timerText, Toast.LENGTH_SHORT).show();
                            }
                        });
                        //타이머 시간동안 쉬기
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(timerText == 0) {
                // 0일때 해당 함수실행 -> 0이면 촬영을한다
                // 0이 아닐땐 촬영자체가 안되게됨
                Camera camera = renderer.getCamera();
                if (pushFlashBtn)
                    renderer.FlashOn();
                camera.autoFocus(saveAutoFocus); //save
            }
        }
    }

    void TimerExit()
    {
        // 타이머스레드 예외처리, 타이머를 끄는 함수
        if (timerThread != null && timerThread.isAlive()) { // 딴거 하다  다시 사진 찍을때 새로 쓰레드 생성
            timerThread.interrupt();
        }
        doingCapture = false;
    }

    private void handleZoom(MotionEvent event, Camera camera){// Camera.Parameters params) {
        Camera.Parameters params = camera.getParameters();
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        double newTouchDistance = getFingerSpacing(event);
        if(Math.abs(newTouchDistance - touchDistance) > 5 ) { //미세한 움직임은 무시
            if (newTouchDistance > touchDistance) {
                //zoom in
                if (zoom < maxZoom)
                    zoom++;
            } else if (newTouchDistance < touchDistance) {
                //zoom out
                if (zoom > 0)
                    zoom--;
            }
        }
        touchDistance = newTouchDistance;
        params.setZoom(zoom);
        camera.setParameters(params);
    }

    public void handleFocus(MotionEvent event, Camera camera){// Camera.Parameters params) {
        Camera.Parameters params = camera.getParameters();
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    // currently set to auto-focus on single touch
                }
            });
        }
    }

    /** Determine the space between the first two fingers */
    private double getFingerSpacing(MotionEvent event) {
        // ...
        double x = event.getX(0) - event.getX(1);
        double y = event.getY(0) - event.getY(1);

        return Math.sqrt(x * x + y * y);
    }

    // 카메라를 실행
    public void Start()
    {
        // 아랫줄은 상태바(배터리, 시간 표시)를 없애줌
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // saveAutoFocus 콜백함수 등록
        // save는 autoFocus 안에 있다
        saveAutoFocus = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                renderer.FlashOff();  //플래시 꺼준다
                bitmap = textureView.getBitmap();
                LSYSUtility.Save(MainActivity.this,textureView);
            }
        };

        setContentView(R.layout.activity_main);

        // Renderer 생성
        renderer = new Renderer(this);

        // 카메라 출력해주는 textureView
        textureView = (TextureView) findViewById(R.id.textureView);

        // 나중에 test할것 (삭제해도 ok?)
        assert textureView != null;

        // renderer를 textureView에 연결
        textureView.setSurfaceTextureListener(renderer);


        textureView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                textureviewTouch=true;
                return true;
            }
        });

        // textureView를 클릭하는 상황. (나중에 수정 예정)
        textureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Camera camera = renderer.getCamera();
                // Get the pointer ID
                Camera.Parameters params = camera.getParameters();
                int action = event.getAction();


                if (event.getPointerCount() > 1) { // 현재 PointerCount가 1보다 클때. 즉, 손가락이 2개가 터치되어있을때

                    switch (action &  MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:


                            //   touchPosX=(event.getX()-(textureView.getWidth()/2))/textureView.getWidth()*2;
                            //   touchPosY=-(event.getY()-(textureView.getHeight()/2))/textureView.getHeight()*2;
                            //   textureviewTouch=true;
                            //   Log.e("down" +event.getX(),"down");
                            Log.e("down" ,"down");
                            break;

                        case MotionEvent.ACTION_POINTER_DOWN:
                            touchDistance = getFingerSpacing(event);
                            Log.e("ACTION_POINTER_DOWN" ,"ACTION_POINTER_DOWN");
                            break;

                        case MotionEvent.ACTION_MOVE:
                            if(params.isZoomSupported()) //줌을 지원하면
                            {
                                camera.cancelAutoFocus(); // cancelAutoFocus
                                handleZoom(event, camera);
                            }
                        /*
                        if(checked) {
                            touchPosX = (event.getX() - (textureView.getWidth() / 2)) / textureView.getWidth() * 2;
                            touchPosY = -(event.getY() - (textureView.getHeight() / 2)) / textureView.getHeight() * 2;
                        }
                        */
                            break;

                        case MotionEvent.ACTION_UP:
                        /*
                        checked=false;
                        Log.e("up "+ +event.getY(),"up");
                        */
                        case MotionEvent.ACTION_CANCEL:


                            break;
                    }
                } else {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // renderer.setSelectedFilter(R.id.filter0);
                            // renderer.AutoFocusing();
                            touchPosX=(event.getX()-(textureView.getWidth()/2))/textureView.getWidth()*2;
                            touchPosY=-(event.getY()-(textureView.getHeight()/2))/textureView.getHeight()*2;

                            break;
                        case MotionEvent.ACTION_MOVE:
                            if(checked) {
                                touchPosX = (event.getX() - (textureView.getWidth() / 2)) / textureView.getWidth() * 2;
                                touchPosY = -(event.getY() - (textureView.getHeight() / 2)) / textureView.getHeight() * 2;
                            }
                            break;

                        case MotionEvent.ACTION_UP:

                            break;
                        case MotionEvent.ACTION_CANCEL:
                            // renderer.setSelectedFilter(filterId);
                            //   renderer.AutoFocusing();

                            break;
                    }
                }
                return false;
            }
        });

        // 핸드폰에 맞춰줌
        textureView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                renderer.onSurfaceTextureSizeChanged(null, v.getWidth(), v.getHeight());
            }
        });

        // 슬라이드 메뉴 구현부
        mSlideMenu = findViewById(R.id.id_submenu_filter);
        mSlideMenu.setVisibility(View.GONE);

        // 필터 아이콘 클릭
        ImageView btSlideUp = (ImageView) findViewById(R.id.id_icon_filter);
        Glide.with(this).load(R.drawable.icon_filter).into(btSlideUp);
        btSlideUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.push_up);
                mAnimation.setAnimationListener(MainActivity.this);
                mSlideMenu.setVisibility(View.VISIBLE);
                mSlideMenu.startAnimation(mAnimation);
            }
        });

        ImageView btSlideOut = (ImageView)findViewById(R.id.id_icon_backspace);
        Glide.with(this).load(R.drawable.icon_backspace).into(btSlideOut);
        btSlideOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.push_down);
                mAnimation.setAnimationListener(MainActivity.this);
                mSlideMenu.startAnimation(mAnimation);
                mSlideMenu.setVisibility(View.GONE);
            }
        });

        // 카메라 전/후방 전환
        btChange = (ImageView)findViewById(R.id.id_icon_change);
        Glide.with(this).load(R.drawable.icon_change).into(btChange);
        btChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(!pushChangeBtn) {
                    pushChangeBtn = true;
                    btChange.setEnabled(false);
                    TimerExit(); //TimerThread 예외처리
                }
            }
        });

        // 플래시 on
        btFlash = (ImageView)findViewById(R.id.id_icon_flash);
        Glide.with(this).load(R.drawable.icon_flash).into(btFlash);
        btFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(!doingCapture) {
                    pushFlashBtn = !pushFlashBtn;
                    if(pushFlashBtn)
                        Toast.makeText(getApplicationContext(), "Flash on", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), "Flash off", Toast.LENGTH_SHORT).show();
                   // btFlash.setEnabled(false);
                }
            }
        });

        // 타이머
        ImageView btTimer = (ImageView)findViewById(R.id.id_icon_timer);
        Glide.with(this).load(R.drawable.icon_timer).into(btTimer);
        btTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                switch (timerTime)
                {
                    case 0:
                        timerTime =3;
                        Toast.makeText(getApplicationContext(), "3초 설정", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        timerTime =5;
                        Toast.makeText(getApplicationContext(), "5초 설정", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        timerTime =10;
                        Toast.makeText(getApplicationContext(), "10초 설정", Toast.LENGTH_SHORT).show();
                        break;
                    case 10:
                        timerTime =0;
                        Toast.makeText(getApplicationContext(), "Timer off", Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        });

        //Button filter1 = (Button)findViewById(R.id.id_filter1);
        ImageView filter1Button = (ImageView)findViewById(R.id.id_filter1);
        Glide.with(this).load(R.drawable.filter01).into(filter1Button);
        filter1Button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                renderer.setSelectedFilter(R.id.filter1);
            }
        });

        ImageView filter2Button = (ImageView)findViewById(R.id.id_filter2);
        Glide.with(this).load(R.drawable.filter02).into(filter2Button);
        filter2Button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                renderer.setSelectedFilter(R.id.filter2);
            }
        });

        ImageView filter3Button = (ImageView)findViewById(R.id.id_filter3);
        Glide.with(this).load(R.drawable.filter03).into(filter3Button);
        filter3Button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                renderer.setSelectedFilter(R.id.filter3);
            }
        });

        ImageView filter4Button = (ImageView)findViewById(R.id.id_filter4);
        Glide.with(this).load(R.drawable.filter04).into(filter4Button);
        filter4Button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                renderer.setSelectedFilter(R.id.filter4);
            }
        });

        ImageView filter5Button = (ImageView)findViewById(R.id.id_filter5);
        Glide.with(this).load(R.drawable.filter05).into(filter5Button);
        filter5Button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                renderer.setSelectedFilter(R.id.filter5);
            }
        });

        ImageView filter6Button = (ImageView)findViewById(R.id.id_filter6);
        Glide.with(this).load(R.drawable.filter06).into(filter6Button);
        filter6Button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                renderer.setSelectedFilter(R.id.filter6);
            }
        });

        ImageView filter7Button = (ImageView)findViewById(R.id.id_filter7);
        Glide.with(this).load(R.drawable.filter07).into(filter7Button);
        filter7Button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                renderer.setSelectedFilter(R.id.filter7);
            }
        });

        ImageView filter8Button = (ImageView)findViewById(R.id.id_filter8);
        Glide.with(this).load(R.drawable.filter08).into(filter8Button);
        filter8Button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                renderer.setSelectedFilter(R.id.filter8);
            }
        });

        ImageView filter9Button = (ImageView)findViewById(R.id.id_filter9);
        Glide.with(this).load(R.drawable.filter09).into(filter9Button);
        filter9Button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                renderer.setSelectedFilter(R.id.filter9);
            }
        });

        ImageView filter10Button = (ImageView)findViewById(R.id.id_filter10);
        Glide.with(this).load(R.drawable.filter10).into(filter10Button);
        filter10Button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                renderer.setSelectedFilter(R.id.filter10);
            }
        });


        /*
        Button filter3 = (Button)findViewById(R.id.id_filter3);
        filter3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                renderer.setSelectedFilter(R.id.filter3);
            }
        });
        */
        // Filter Select ImageButton

        // 사진촬영
        ImageView capture = (ImageView)findViewById(R.id.id_icon_circle);
        Glide.with(this).load(R.drawable.icon_circle).into(capture);
        capture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(!doingCapture) {
                    doingCapture =true;
                    timerThread = new Timer();
                    timerThread.start();
                }
            }
        });

        // 텍스트스티커
        ImageView textSticker =(ImageView)findViewById(R.id.id_icon_text);
        Glide.with(this).load(R.drawable.icon_text).into(textSticker);
        textSticker.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(!textOn)
                {
                    stickerOn=false;
                    textOn=true;
                    //stickerOn=true;
                }
                else
                {
                    textOn=false;
                    // stickerOn=false;
                }
            }
        });
        // 이미지스티커
        ImageView imageSticker =(ImageView)findViewById(R.id.id_icon_image);
        Glide.with(this).load(R.drawable.icon_image).into(imageSticker);
        imageSticker.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(!stickerOn)
                {
                    textOn =false;
                    // StickerPro.SetSticker();
                    stickerOn=true;

                }
                else
                {
                    stickerOn=false;
                }
            }
        });

        ImageView collageButton = (ImageView)findViewById(R.id.id_icon_collage);
        Glide.with(this).load(R.drawable.icon_collage).into(collageButton);

    }

    @Override
    public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub
    }
}
