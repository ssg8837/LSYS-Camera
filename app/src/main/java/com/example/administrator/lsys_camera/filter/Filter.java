package com.example.administrator.lsys_camera.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.support.annotation.CallSuper;

import com.example.administrator.lsys_camera.Processing;

import java.nio.FloatBuffer;

public abstract class Filter {

    final long START_TIME = System.currentTimeMillis();
    int iFrame = 0;
    Context context;

    public Filter(Context context) {
        this.context =context;
    }

        @CallSuper
        public void onAttach() {
            iFrame = 0;
        }



    final public void AdaptFilter(int originTexId, int canvasWidth, int canvasHeight) {
        // TODO move?
        // Use shaders

        onDraw(originTexId, canvasWidth, canvasHeight);

        iFrame++;
    }

        abstract void onDraw(int cameraTexId, int canvasWidth, int canvasHeight);

        void setupShaderInputs(int program, int[] iResolution, int[] iChannels, int[][] iChannelResolutions) {  // CAMERA_RENDER_BUF.getTexId() <-> iChannels
            setupShaderInputs(program, Processing.VERTEX_BUF, Processing.TEXTURE_COORD_BUF, iResolution, iChannels, iChannelResolutions);
        }

        void setupShaderInputs(int program, FloatBuffer vertex, FloatBuffer textureCoord, int[] iResolution, int[] iChannels, int[][] iChannelResolutions) {
            GLES20.glUseProgram(program);

            int iResolutionLocation = GLES20.glGetUniformLocation(program, "iResolution");
            GLES20.glUniform3fv(iResolutionLocation, 1,
                    FloatBuffer.wrap(new float[]{(float) iResolution[0], (float) iResolution[1], 1.0f}));

            float time = ((float) (System.currentTimeMillis() - START_TIME)) / 1000.0f;
            int iGlobalTimeLocation = GLES20.glGetUniformLocation(program, "iGlobalTime");
            GLES20.glUniform1f(iGlobalTimeLocation, time);

            int iFrameLocation = GLES20.glGetUniformLocation(program, "iFrame");
            GLES20.glUniform1i(iFrameLocation, iFrame);

            int vPositionLocation = GLES20.glGetAttribLocation(program, "vPosition");
            GLES20.glEnableVertexAttribArray(vPositionLocation);
            GLES20.glVertexAttribPointer(vPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, vertex);

            int vTexCoordLocation = GLES20.glGetAttribLocation(program, "vTexCoord");
            GLES20.glEnableVertexAttribArray(vTexCoordLocation);
            GLES20.glVertexAttribPointer(vTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, textureCoord);

            // Log.e("gggggg",""+iChannels.length);
            for (int i = 0; i < iChannels.length; i++) {
                int sTextureLocation = GLES20.glGetUniformLocation(program, "iChannel" + i);
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, iChannels[i]);
                GLES20.glUniform1i(sTextureLocation, i);
            }
           // Log.e("gggggg",""+iChannelResolutions.length);
            float _iChannelResolutions[] = new float[iChannelResolutions.length * 3];

            for (int i = 0; i < iChannelResolutions.length; i++) {
                _iChannelResolutions[i * 3] = iChannelResolutions[i][0];
                _iChannelResolutions[i * 3 + 1] = iChannelResolutions[i][1];
                _iChannelResolutions[i * 3 + 2] = 1.0f;
            }

            int iChannelResolutionLocation = GLES20.glGetUniformLocation(program, "iChannelResolution");
            GLES20.glUniform3fv(iChannelResolutionLocation,
                    _iChannelResolutions.length, FloatBuffer.wrap(_iChannelResolutions));
        }



}
