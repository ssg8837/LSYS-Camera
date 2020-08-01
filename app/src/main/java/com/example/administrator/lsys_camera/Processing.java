package com.example.administrator.lsys_camera;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.example.administrator.lsys_camera.filter.Filter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_BLEND;

public class Processing {

    static final float SQUARE_COORDS[] = {
            1.0f, -1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, 1.0f,
    };
    static final float TEXTURE_COORDS[] = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
    };
    public static FloatBuffer VERTEX_BUF, TEXTURE_COORD_BUF;
    static int ORIGIN_PROGRAM = 0;

    private static final int BUF_ACTIVE_TEX_UNIT = GLES20.GL_TEXTURE8;
    private FrameBuffer CAMERA_RENDER_BUF;

    private static final float ROATED_TEXTURE_COORDS[] = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
    };
    private static FloatBuffer ROATED_TEXTURE_COORD_BUF;

    public Processing(Context context)
    {
        // Setup default Buffers
        if (VERTEX_BUF == null) {
            VERTEX_BUF = ByteBuffer.allocateDirect(SQUARE_COORDS.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            VERTEX_BUF.put(SQUARE_COORDS);
            VERTEX_BUF.position(0);
        }

        if (TEXTURE_COORD_BUF == null) {
            TEXTURE_COORD_BUF = ByteBuffer.allocateDirect(TEXTURE_COORDS.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            TEXTURE_COORD_BUF.put(TEXTURE_COORDS);
            TEXTURE_COORD_BUF.position(0);
        }

        if (ROATED_TEXTURE_COORD_BUF == null) {
            ROATED_TEXTURE_COORD_BUF = ByteBuffer.allocateDirect(ROATED_TEXTURE_COORDS.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            ROATED_TEXTURE_COORD_BUF.put(ROATED_TEXTURE_COORDS);
            ROATED_TEXTURE_COORD_BUF.position(0);
        }
        if (ORIGIN_PROGRAM == 0) {
            ORIGIN_PROGRAM = LSYSUtility.buildProgram(context, R.raw.vertext, R.raw.original_rtt);
        }
    }


     public void draw(int cameraTexId, int canvasWidth, int canvasHeight, Filter selectedFilter, Context context) //이미지 변수 추가예정?
    {
        int texId = CameraBufferToFrameBuffer(cameraTexId, canvasWidth, canvasHeight);
        if(texId == -1)
            return;
        selectedFilter.AdaptFilter(texId, canvasWidth, canvasHeight);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);



    }

    private  int CameraBufferToFrameBuffer(int cameraTexId, int canvasWidth, int canvasHeight) {
        // TODO move?
        // Create camera render buffer
        if (CAMERA_RENDER_BUF == null ||
                CAMERA_RENDER_BUF.getWidth() != canvasWidth ||
                CAMERA_RENDER_BUF.getHeight() != canvasHeight) {
            CAMERA_RENDER_BUF = new FrameBuffer(canvasWidth, canvasHeight, BUF_ACTIVE_TEX_UNIT);
        }

        // Use shaders
        GLES20.glUseProgram(ORIGIN_PROGRAM);

        int iChannel0Location = GLES20.glGetUniformLocation(ORIGIN_PROGRAM, "iChannel0");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexId);
        GLES20.glUniform1i(iChannel0Location, 0);

        int vPositionLocation = GLES20.glGetAttribLocation(ORIGIN_PROGRAM, "vPosition");
        GLES20.glEnableVertexAttribArray(vPositionLocation);
        GLES20.glVertexAttribPointer(vPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, VERTEX_BUF);

        int vTexCoordLocation = GLES20.glGetAttribLocation(ORIGIN_PROGRAM, "vTexCoord");
        GLES20.glEnableVertexAttribArray(vTexCoordLocation);
        GLES20.glVertexAttribPointer(vTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, ROATED_TEXTURE_COORD_BUF);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Render to texture
        if(CAMERA_RENDER_BUF != null) {
            CAMERA_RENDER_BUF.bind();
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            CAMERA_RENDER_BUF.unbind();
        }
        else {
            Log.e("CAMERA_RENDER_BUF" ,"is null!");
            return -1;
        }

        return CAMERA_RENDER_BUF.getTexId();
    }

    // 썼던 리소스 초기화
    public  void release() {

        ORIGIN_PROGRAM = 0;
        CAMERA_RENDER_BUF = null;
    }

    // 전면카메라로 open하면 상이 거꾸로 맺히기때문에 수정하기 위해
    public static void SetRotateScreen(int num)
    {
        switch (num)
        {
            // 후면카메라 기준 (평소상태)
            case 0:
                ROATED_TEXTURE_COORDS[0]= 1.0f;
                ROATED_TEXTURE_COORDS[1]= 0.0f;
                ROATED_TEXTURE_COORDS[2]= 1.0f;
                ROATED_TEXTURE_COORDS[3]= 1.0f;
                ROATED_TEXTURE_COORDS[4]= 0.0f;
                ROATED_TEXTURE_COORDS[5]= 0.0f;
                ROATED_TEXTURE_COORDS[6]= 0.0f;
                ROATED_TEXTURE_COORDS[7]= 1.0f;
                ROATED_TEXTURE_COORD_BUF.put(ROATED_TEXTURE_COORDS);
                ROATED_TEXTURE_COORD_BUF.position(0);
                break;

            // 임시 (기능추가시를 위함)
            case 1:
                break;

            // 후면카메라 기준 상하,좌우 회전함. 전면카메라가 올바르게 보이게하기 위함
            case 2:
                ROATED_TEXTURE_COORDS[0]= 0.0f;
                ROATED_TEXTURE_COORDS[1]= 0.0f;
                ROATED_TEXTURE_COORDS[2]= 0.0f;
                ROATED_TEXTURE_COORDS[3]= 1.0f;
                ROATED_TEXTURE_COORDS[4]= 1.0f;
                ROATED_TEXTURE_COORDS[5]= 0.0f;
                ROATED_TEXTURE_COORDS[6]= 1.0f;
                ROATED_TEXTURE_COORDS[7]= 1.0f;
                ROATED_TEXTURE_COORD_BUF.put(ROATED_TEXTURE_COORDS);
                ROATED_TEXTURE_COORD_BUF.position(0);
                break;

            // 임시 (기능추가시를 위함)
            case 3:
                break;
            default:
                break;
        }

    }


}
