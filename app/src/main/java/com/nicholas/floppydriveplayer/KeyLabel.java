package com.nicholas.floppydriveplayer;

import android.opengl.GLES30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by Nicholas on 9/16/2016.
 */
public class KeyLabel {

    private int[] vao = new int[1];
    private int[] vbo = new int[1];
    private int[] ebo = new int[1];

    private static final int NUM_VERTICES = 4;
    private static final int VERTEX_POSITION_COMPONENTS = 3;
    private static final int VERTEX_TEXCOORD_COMPONENTS = 2;
    private static final int VERTEX_STRIDE = 4 * (VERTEX_POSITION_COMPONENTS + VERTEX_TEXCOORD_COMPONENTS);

    public float x, y, width, height, zIndex;
    private int textureId;

    public KeyLabel(float x, float y, float width, float height, float zIndex, int textureId) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.zIndex = zIndex;
        this.textureId = textureId;
        initialize();
    }

    private void initialize() {
        float[] vertexData = new float[] {
                x, y, zIndex,
                0, 0,
                x + width, y, zIndex,
                1, 0,
                x, y + height, zIndex,
                0, 1,
                x + width, y + height, zIndex,
                1, 1
        };

        int[] indexData = new int[] {
                0, 1, 3,
                0, 3, 2
        };

        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertexData).position(0);

        IntBuffer indexBuffer = ByteBuffer.allocateDirect(indexData.length * 4)
                .order(ByteOrder.nativeOrder()).asIntBuffer();
        indexBuffer.put(indexData).position(0);

        GLES30.glGenVertexArrays(1, vao, 0);
        GLES30.glGenBuffers(1, vbo, 0);
        GLES30.glGenBuffers(1, ebo, 0);

        GLES30.glBindVertexArray(vao[0]);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexData.length * 4, vertexBuffer,
                GLES30.GL_STATIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ebo[0]);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexData.length * 4, indexBuffer,
                GLES30.GL_STATIC_DRAW);

        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, VERTEX_STRIDE, 0);

        GLES30.glEnableVertexAttribArray(1);
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, VERTEX_STRIDE, VERTEX_POSITION_COMPONENTS * 4);

        GLES30.glBindVertexArray(0);
    }

    public void draw() {
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
        GLES30.glBindVertexArray(vao[0]);
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_UNSIGNED_INT, 0);
        GLES30.glBindVertexArray(0);
    }

    public void delete() {
        GLES30.glDeleteVertexArrays(1, vao, 0);
        GLES30.glDeleteBuffers(1, vbo, 0);
        GLES30.glDeleteBuffers(1, ebo, 0);
    }
}
