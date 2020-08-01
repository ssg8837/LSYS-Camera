package com.example.administrator.lsys_camera;

import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class OrientationListener implements SensorEventListener {

    private boolean mInitialized = false;
    public static boolean mAutoFocus = true;
    private float mLastX;
    private float mLastY;
    private float mLastZ;
    private Camera camera;

    public OrientationListener(Camera camera) {
        this.camera = camera;
    }

    public void onSensorChanged(SensorEvent event) {  // 방향 센서 값이 바뀔때마다 호출됨
        float x = event.values[0]; //x 좌표
        float y = event.values[1]; //y 좌표
        float z = event.values[2]; //z 좌표
        if (!mInitialized) { // 초기화
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mInitialized = true;
        }
        float deltaX = Math.abs(mLastX - x);
        float deltaY = Math.abs(mLastY - y);
        float deltaZ = Math.abs(mLastZ - z);

        mLastX = x;
        mLastY = y;
        mLastZ = z;

        if (deltaX > 2 && mAutoFocus) { //AUTOFOCUS (while it is not autofocusing)
            mAutoFocus = false;
            setCameraFocus(myAutoFocusCallback); //오토포커스 호출
            return;
        }
        if (deltaY > 2 && mAutoFocus) { //AUTOFOCUS (while it is not autofocusing)
            mAutoFocus = false;
            setCameraFocus(myAutoFocusCallback);
            return;
        }
        if (deltaZ > 2 && mAutoFocus) { //AUTOFOCUS (while it is not autofocusing) */
            mAutoFocus = false;
            setCameraFocus(myAutoFocusCallback);
            return;
        }
    }

    // 가속도센서
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void SetCamera(Camera camera)
    {
        this.camera = camera;
    }

    public void setCameraFocus(Camera.AutoFocusCallback autoFocus) {
        if(!MainActivity.pushChangeBtn) {
            if (camera.getParameters().getFocusMode().equals(camera.getParameters().FOCUS_MODE_AUTO) ||
                    camera.getParameters().getFocusMode().equals(camera.getParameters().FOCUS_MODE_MACRO)) { // 오토포커스 모드가 있다면
                camera.autoFocus(autoFocus);
            }
        }
    }

    // this is the autofocus call back
    public Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback() {

        public void onAutoFocus(boolean autoFocusSuccess, Camera arg1) {
            /*
            try {
                Thread.currentThread().sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
            mAutoFocus = true;
        }
    };
}