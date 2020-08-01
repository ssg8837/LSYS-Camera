package com.example.administrator.lsys_camera;

/**
 * Created by sinhyeonjun on 2017. 3. 15..
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;

abstract class AbstractSticker {
    static float SQUARE_COORDS[];


    public static FloatBuffer VERTEX_BUF;
    static int ORIGIN_PROGRAM = 0;

    static  float ROATED_TEXTURE_COORDS[] = {
            1.0f, 1.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };


    Context context;



    public void release() {
        ORIGIN_PROGRAM = 0;
    }

}
