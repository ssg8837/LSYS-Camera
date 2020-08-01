package com.example.administrator.lsys_camera;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.TextureView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public  class ImageSaver implements Runnable {

    // The JPEG image
    private  Bitmap imageBitmap;

    private  File mFile;
    private  String mPath;
    private  TextureView textureView;

    // private  Bitmap bitmap;
    private static final String FileName = "LSYS";
    private Context context;
    private Handler mHandler = new Handler();

    public ImageSaver(Context context, TextureView textureView) {
        this.context = context;
        this.textureView = textureView;
    }

    // 저장하여 갤러리에 반영시키기
    public static void addImageToGallery(final String filePath, final Context context) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    @Override
    public void run() {
        imageBitmap = textureView.getBitmap();
        Date date = new Date();

        // miliSecond까지 해야 사진이 겹치지않는다
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_hh_mm_ss_SSS");
        String timeStr = dateFormat.format(date);
        String dirStr = Environment.getExternalStorageDirectory().toString() + "/" + FileName;

        File dir = new File(dirStr);

        //해당 디렉토리의 존재여부를 확인
        if(!dir.exists()){
            //없다면 생성
            dir.mkdirs();
        }

        mPath =  dirStr +"/"+ timeStr + ".jpg";
        OutputStream fileOutputStream = null;

        try {
            mFile = new File(mPath);
            fileOutputStream = new FileOutputStream(mFile);

            // 비트맵을 파일스트림을이용하여 JPEG 형태로 보냄
            imageBitmap.compress(Bitmap.CompressFormat.JPEG,99, fileOutputStream);

            fileOutputStream.flush();
            fileOutputStream.close();

            addImageToGallery(mFile.toString(),context);

            // 저장이 되어야 다음촬영실행
            MainActivity.doingCapture =false;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mHandler.post(new Runnable() {
                // 찍힌게 완료 됐을 때
                @Override
                public void run() {
                    Toast.makeText(context.getApplicationContext(), "complete", Toast.LENGTH_SHORT).show();
                }
            });

            if (null != fileOutputStream) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
