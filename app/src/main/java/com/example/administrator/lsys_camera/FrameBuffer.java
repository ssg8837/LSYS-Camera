package com.example.administrator.lsys_camera;

import android.opengl.GLES20;

import javax.microedition.khronos.opengles.GL10;

public class FrameBuffer {
    private int texId = 0;
    private int activeTexUnit = 0;
    private int renderBufferId = 0; // 깊이버퍼
    private int frameBufferId = 0;  // 텍스쳐버퍼

    private int width, height;

    public FrameBuffer(int width, int height, int activeTexUnit) {
        this.width = width;
        this.height = height;
        this.activeTexUnit = activeTexUnit;
        int[] genbuf = new int[1];

        // Generate and bind 2d texture
        GLES20.glActiveTexture(activeTexUnit);
        texId = LSYSUtility.genTexture(GLES20.GL_TEXTURE_2D);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        // Generate frame buffer
        GLES20.glGenFramebuffers(1, genbuf, 0);
        frameBufferId = genbuf[0];

        // Bind frame buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);

        // Generate render buffer
        GLES20.glGenRenderbuffers(1, genbuf, 0);
        renderBufferId = genbuf[0];

        // Bind render buffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBufferId);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderBufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texId, 0);

        unbind();
    }

    // 우리만의 버퍼에넣은 텍스쳐의 아이디 획득
    public int getTexId() {
        return texId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // 프레임버퍼를 만들면 그 버퍼를 설정 (이걸사용하겠다)
    public void bind() {
        GLES20.glViewport(0, 0, width, height);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
    }

    public void unbind() {
        // 0은 기본값. unbind시 기본으로 돌려야하므로 0으로 설정
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }
}
