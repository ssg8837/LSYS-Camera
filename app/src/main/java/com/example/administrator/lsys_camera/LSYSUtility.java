package com.example.administrator.lsys_camera;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.TextureView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;

public class LSYSUtility {

    // 사진이미지를 이용한 텍스쳐생성
    public static int genTexture(int textureType) {
        int[] texId = new int[1];
        GLES20.glGenTextures(1, texId, 0);
        GLES20.glBindTexture(textureType, texId[0]);

        // Set texture default draw parameters
        if (textureType == GLES11Ext.GL_TEXTURE_EXTERNAL_OES) {
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        } else {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
        }

        return texId[0];
    }

    // 텍스쳐 불러오기
    public static int loadTexture(final Context context, final int resourceId, int[] size) {
        final int texId = genTexture(GLES20.GL_TEXTURE_2D);

        if (texId != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling
            options.inJustDecodeBounds = true;

            // Just decode bounds
            BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Set return size
            size[0] = options.outWidth;
            size[1] = options.outHeight;

            // Decode
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Load the bitmap into the bound texture.

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        return texId;
    }

    public static int loadStickerTexture(final Context context, final int resourceId) {

        int texId = genTexture(GLES20.GL_TEXTURE_2D);


        if (texId != 0) {
            Bitmap bitmap;
            BitmapDrawable drawable = (BitmapDrawable) context.getResources().getDrawable(resourceId); // 배열 형식으로 스티커들 미리 올려놓기로 바꾸기 혹은 Renderer의 맵형식으로

            bitmap = drawable.getBitmap();

            // Load the bitmap into the bound texture.
            GLES20.glEnable (GL_BLEND);
            GLES20.glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);

            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);


            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        }

        return texId;
    }

    // GLES20부터는 Shader를 프로그램을 이용해서 사용하게됨 (vertex, fragment.. 등이 들어감)
    // Shader를 사용하는 프로그램 빌드
    public static int buildProgram(Context context, int vertexSourceRawId, int fragmentSourceRawId) {
        return buildProgram(getStringFromRaw(context, vertexSourceRawId),
                getStringFromRaw(context, fragmentSourceRawId));
    }

    public static int buildProgram(String vertexSource, String fragmentSource) {
        final int vertexShader = buildShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        final int fragmentShader = buildShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == 0) {
            return 0;
        }

        final int program = GLES20.glCreateProgram();
        if (program == 0) {
            return 0;
        }

        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        return program;
    }

    public static int buildShader(int type, String shaderSource) {
        final int shader = GLES20.glCreateShader(type);
        if (shader == 0) {
            return 0;
        }

        GLES20.glShaderSource(shader, shaderSource);
        GLES20.glCompileShader(shader);

        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            Log.e("LSYSUtility", GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    // Raw 데이터를 String으로 얻어옴
    private static String getStringFromRaw(Context context, int id) {
        String str;
        try {
            Resources r = context.getResources();
            InputStream is = r.openRawResource(id);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int i = is.read();
            while (i != -1) {
                baos.write(i);
                i = is.read();
            }

            str = baos.toString();
            is.close();
        } catch (IOException e) {
            str = "";
        }

        return str;
    }

    // textureView 이미지 저장
    public static void Save(Context context, TextureView textureView)
    {
        Thread svaeThread = new Thread(new ImageSaver(context,textureView));
        svaeThread.start();
    }
}
