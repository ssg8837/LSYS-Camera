package com.example.administrator.lsys_camera.filter;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.example.administrator.lsys_camera.LSYSUtility;
import com.example.administrator.lsys_camera.R;

public class EdgeDetectionFilter extends Filter {
    private int program;

    public EdgeDetectionFilter(Context context) {
        super(context);

        // Build shaders
        program = LSYSUtility.buildProgram(context, R.raw.vertext, R.raw.edge_detection);
    }

    @Override
    public void onDraw(int cameraTexId, int canvasWidth, int canvasHeight) {
        setupShaderInputs(program,
                new int[]{canvasWidth, canvasHeight},
                new int[]{cameraTexId},
                new int[][]{});
        Log.e("selceted","edge");
    }
}