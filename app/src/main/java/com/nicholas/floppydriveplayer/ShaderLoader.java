package com.nicholas.floppydriveplayer;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Nicholas on 9/12/2016.
 */
public class ShaderLoader {

    private static String TAG = "ShaderLoader";

    public int compileShader(Context context, String fileName, int type) {
        String source = readFile(context, fileName);
        if (source == null) {
            Log.e(TAG, "Shader file could not be read.");
            return 0; //Indicate error.
        }

        int shader = GLES30.glCreateShader(type);
        Log.i(TAG, "Shader=" + shader);
        GLES30.glShaderSource(shader, source);
        int err = GLES30.glGetError();
        if (err != GLES30.GL_NO_ERROR) {
            Log.e(TAG, "Error with specifying shader source: ERR=" + err + ", SRC=" +  source);
        }
        GLES30.glCompileShader(shader);

        int[] compileStatus = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == GLES30.GL_FALSE) {
            Log.e(TAG, "Compilation failed in shader: \"" + fileName + "\"\n"
                    + GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);
            return 0; //Indicate error.
        }

        return shader;
    }

    public int linkShader(int vertexShader, int fragmentShader) {
        int program = GLES30.glCreateProgram();

        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        GLES30.glLinkProgram(program);

        int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == GLES30.GL_FALSE) {
            Log.e(TAG, "Linking failed in shader program: \"Program " + program + "\"\n"
                    + GLES30.glGetProgramInfoLog(program));
            GLES30.glDeleteProgram(program);
            return 0;
        }

        return program;
    }

    public String readFile(Context context, String fileName) {
        if (fileName == null) {
            Log.e(TAG, "File name was not provided, found \'null\' instead.");
            return null;
        }

        StringBuilder source = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName), "UTF-8"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                source.append(line + "\n");
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return source.toString();
    }
}
