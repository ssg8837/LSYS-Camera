package com.example.administrator.lsys_camera;


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

public class StickerPro extends AbstractSticker {


    public boolean moveOn=false;
    public boolean sizeOn=false;

    StickerBound stickerBound;
    static float size =0.2f;

    //중심점
    float posX=0.0f;
    float posY=0.0f;

    float SQUARE_COORDS[] = {
            0.2f, -0.2f,
            -0.2f, -0.2f,
            0.2f, 0.2f,
            -0.2f, 0.2f,
    };

    static float BEFORE_MOVE_SQUARE_COORDS[]= {
            0.2f, -0.2f,
            -0.2f, -0.2f,
            0.2f, 0.2f,
            -0.2f, 0.2f,
    };//돌리기 전의 코드
    public static FloatBuffer VERTEX_BUF;
    static int ORIGIN_PROGRAM = 0;



    public static FloatBuffer ROATED_TEXTURE_COORD_BUF;
    int[] textTexId;
    int[] stickerTexId;

    public StickerPro(Context context)
    {
        textTexId = new int[1];
        stickerTexId = new int[1];
        // Setup default Buffers
        if (VERTEX_BUF == null) {
            VERTEX_BUF = ByteBuffer.allocateDirect(SQUARE_COORDS.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            VERTEX_BUF.put(SQUARE_COORDS);
            VERTEX_BUF.position(0);
        }


        if (ROATED_TEXTURE_COORD_BUF == null) {
            ROATED_TEXTURE_COORD_BUF = ByteBuffer.allocateDirect(ROATED_TEXTURE_COORDS.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            ROATED_TEXTURE_COORD_BUF.put(ROATED_TEXTURE_COORDS);
            ROATED_TEXTURE_COORD_BUF.position(0);
        }


        ORIGIN_PROGRAM = LSYSUtility.buildProgram(context, R.raw.vertext, R.raw.original);

        this.context = context;
        stickerBound= new StickerBound(context, VERTEX_BUF, ROATED_TEXTURE_COORD_BUF);


    }


    void draw(int canvasWidth, int canvasHeight, boolean touched)
    {
        Bitmap textBitmap;

        GLES20.glUseProgram(ORIGIN_PROGRAM);
        if(touched) {

            switch (stickerBound.check(SQUARE_COORDS))
            {
                case 1:
                {
                    moveOn=true;
                    sizeOn=false;
                    break;
                }
                case 2://size and Rotate
                {
                    moveOn=false;
                    sizeOn=true;
                    break;
                }
            }
            if(moveOn)
            {
                move();
            }
            if(sizeOn)
            {
                setSize();
            }
        }
        /*
        */
        // Use shaders


        if(MainActivity.checked) {
            stickerBound.draw(canvasWidth, canvasHeight);

        }
        if(MainActivity.textOn) {
            textBitmap= Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(textBitmap);
            textBitmap.eraseColor(0);

            Paint pnt = new Paint();
            pnt.setTextSize(32);
            pnt.setAntiAlias(true);
            pnt.setARGB(0xff, 0xff, 0xff, 0);
            canvas.drawText("Hello", 8, 64, pnt);

            GLES20.glEnable (GL_BLEND);
            GLES20.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glGenTextures(1,textTexId,0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0); //필요 있음?
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textTexId[0]);

            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textBitmap, 0);
            textBitmap.recycle();

            int vPositionLocation = GLES20.glGetAttribLocation(ORIGIN_PROGRAM, "vPosition");
            GLES20.glEnableVertexAttribArray(vPositionLocation);
            GLES20.glVertexAttribPointer(vPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, VERTEX_BUF);


            int vTexCoordLocation = GLES20.glGetAttribLocation(ORIGIN_PROGRAM, "vTexCoord");
            GLES20.glEnableVertexAttribArray(vTexCoordLocation);
            GLES20.glVertexAttribPointer(vTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, ROATED_TEXTURE_COORD_BUF);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            GLES20.glDeleteTextures(1, textTexId, 0);

        }
        else
        {

            stickerTexId[0] = LSYSUtility.loadStickerTexture(context,R.drawable.smile);

            int vPositionLocation = GLES20.glGetAttribLocation(ORIGIN_PROGRAM, "vPosition");
            GLES20.glEnableVertexAttribArray(vPositionLocation);
            GLES20.glVertexAttribPointer(vPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, VERTEX_BUF);


            int vTexCoordLocation = GLES20.glGetAttribLocation(ORIGIN_PROGRAM, "vTexCoord");
            GLES20.glEnableVertexAttribArray(vTexCoordLocation);
            GLES20.glVertexAttribPointer(vTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, ROATED_TEXTURE_COORD_BUF);

            // Render to texture

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            GLES20.glDeleteTextures(1, stickerTexId, 0);

        }
    }

    void move()
    {

        float tempX=MainActivity.touchPosX;
        float tempY=MainActivity.touchPosY;
        if(SQUARE_COORDS[0]>tempX && SQUARE_COORDS[2]<tempX&&SQUARE_COORDS[1]<tempY&&SQUARE_COORDS[5]>tempY) {
            posX = MainActivity.touchPosX; // virtual center coord
            posY = MainActivity.touchPosY;
            upload();
        }

    }

    void setSize()//일단 우측 하단을 기준으로 함.
    {
        //touchX,Y는 유저의 손가락 위치를 나타내는 변수이며 기본적으로 우측 하단을 기준으로 한다.
        float touchposX=MainActivity.touchPosX; //touch value
        float touchposY=MainActivity.touchPosY;
        //정사각형의 중심점 을 원점으로 옮겼을시의 가상의 touchX,Y값
        float virtualX;
        float virtualY;

        float cos;
        float sin;

        //square= 중심점부터 손가락위치까지의 거리의 제곱 값
        float square;


            virtualX=touchposX-posX; // vector a
            virtualY=touchposY-posY; // vector a
            square=virtualX*virtualX+virtualY*virtualY; // vector a size^2

            if(square!=0) {
                size = (float) Math.sqrt(square / 2); // vector a가 사각형의 중점으로부터 우측 꼭지점으로 가는 벡터,
                // vector b는 크기가 a이고 방향이 (1/sqrt(2),-1/sqrt(2))인 벡터

                cos = (size * virtualX - size*virtualY) / square;
                sin = (float) Math.sqrt(1 - (cos * cos));

                if((virtualX+virtualY)<0)//가상의 touchpos(X,Y)가 y=-x의 왼쪽아래일경우
                    sin=-sin;//sin의 값을 음수로 설정.

                if(size<0.1f)
                    size=0.1f;
                calcSize(0,cos,sin);
                calcSize(1,cos,sin);
                calcSize(2,cos,sin);
                calcSize(3,cos,sin);


                upload();

        }

    }

    void calcSize(int number,float cos, float sin)
    {
        int x=number*2;
        int y=1+x;
        float virtualX=-size;
        float virtualY=size;

        if(number==0)
        {
            virtualX=size;
            virtualY=-size;
        }
        if(number==1)
        {

            virtualY=-size;
        }
        if(number==2)
        {
            virtualX=size;
        }


        float rotateX=virtualX*cos-virtualY*sin;
        float rotateY=virtualX*sin+virtualY*cos;

        BEFORE_MOVE_SQUARE_COORDS[x]=rotateX;
        BEFORE_MOVE_SQUARE_COORDS[y]=rotateY;


    }


    void upload()
    {
        for(int i=0;i<4;i++)
        {
            SQUARE_COORDS[i*2]=BEFORE_MOVE_SQUARE_COORDS[i*2]+posX;
            SQUARE_COORDS[i*2+1]=BEFORE_MOVE_SQUARE_COORDS[i*2+1]+posY;
        }

        VERTEX_BUF = ByteBuffer.allocateDirect(SQUARE_COORDS.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        VERTEX_BUF.put(SQUARE_COORDS);

        stickerBound.moveBound(SQUARE_COORDS);
        VERTEX_BUF.position(0);

    }


}
