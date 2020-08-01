package com.example.administrator.lsys_camera;


import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class StickerBound extends AbstractSticker {



    public static FloatBuffer VERTEX_BUF, TEXTURE_COORD_BUF;
    static int ORIGIN_PROGRAM = 0;

    private static final int BUF_ACTIVE_TEX_UNIT = GLES20.GL_TEXTURE8;
    private static FrameBuffer CAMERA_RENDER_BUF;

    private static FloatBuffer ROATED_TEXTURE_COORD_BUF;
    int[] textTexId;
    int[] stickerTexId;
    Context context;
    StickerSize stickerSize;

    public StickerBound(Context context, FloatBuffer vertexBuf, FloatBuffer roatedTextureCoordBuf)
    {
        textTexId = new int[1];
        stickerTexId = new int[1];

        VERTEX_BUF=vertexBuf;
        ROATED_TEXTURE_COORD_BUF=roatedTextureCoordBuf;

        stickerSize=new StickerSize(context,vertexBuf,roatedTextureCoordBuf);

        ORIGIN_PROGRAM = LSYSUtility.buildProgram(context, R.raw.vertext, R.raw.original);

        this.context = context;


    }


    void draw(int canvasWidth, int canvasHeight)
    {
        Bitmap textBitmap;

        GLES20.glUseProgram(ORIGIN_PROGRAM);

        // Use shaders

            stickerTexId[0] = LSYSUtility.loadStickerTexture(context, R.drawable.stickerbound);

            int vPositionLocation = GLES20.glGetAttribLocation(ORIGIN_PROGRAM, "vPosition");
            GLES20.glEnableVertexAttribArray(vPositionLocation);
            GLES20.glVertexAttribPointer(vPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, VERTEX_BUF);


            int vTexCoordLocation = GLES20.glGetAttribLocation(ORIGIN_PROGRAM, "vTexCoord");
            GLES20.glEnableVertexAttribArray(vTexCoordLocation);
            GLES20.glVertexAttribPointer(vTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, ROATED_TEXTURE_COORD_BUF);

            // Render to texture

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            GLES20.glDeleteTextures(1, stickerTexId, 0);
        stickerSize.draw(canvasWidth,canvasHeight);

    }


    int check(float[] SQUARE_COORDS)
    {
        float touchposX=MainActivity.touchPosX;
        float touchposY=MainActivity.touchPosY;
        MainActivity.textureviewTouch=false;
        if(MainActivity.checked)
        {
            return stickerSize.check(touchposX,touchposY);
        }

        if(SQUARE_COORDS[0]>touchposX && SQUARE_COORDS[2]<touchposX&&SQUARE_COORDS[1]<touchposY&&SQUARE_COORDS[5]>touchposY)
        {
            MainActivity.checked=true;
            return 1;//move 기능 1
        }
        return 0;
        //변화를 주지 않을 경우 0

    }
    void moveBound( float[] SQUARE_COORDS)
    {
        VERTEX_BUF = ByteBuffer.allocateDirect(SQUARE_COORDS.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        VERTEX_BUF.put(SQUARE_COORDS);

        VERTEX_BUF.position(0);
        stickerSize.move(SQUARE_COORDS[0],SQUARE_COORDS[1]);

    }




}
