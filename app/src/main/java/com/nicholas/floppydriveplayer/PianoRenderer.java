package com.nicholas.floppydriveplayer;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.opengl.EGLConfig;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import java.nio.ByteBuffer;

/**
 * Created by Nicholas on 9/6/2016.
 */
public class PianoRenderer extends GLTextureViewRenderer implements SeekBar.OnSeekBarChangeListener, View.OnTouchListener, BluetoothSession.BluetoothSessionListener {

    public static final int NOTE_A = 1;
    public static final int NOTE_B = 2;
    public static final int NOTE_C = 3;
    public static final int NOTE_D = 4;
    public static final int NOTE_E = 5;
    public static final int NOTE_F = 6;
    public static final int NOTE_G = 7;

    private static final String TAG = "PianoRenderer";

    private static final int KEYS_PER_OCTAVE = 12;
    private static final int WHITE_KEYS_PER_OCTAVE = 7;
    private static final int BLACK_KEYS_PER_OCTAVE = 5;

    private final Context context;
    private final int numOctaves;

    private float whiteKeyWidth;
    private float whiteKeyHeight;
    private float blackKeyWidth;
    private float blackKeyHeight;
    private int width;
    private int height;
    private PianoKey[] keys;
    private int shaderProgram;

    private int[] textures = new int[7];
    private int[] labelTextures = new int[7];

    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];

    private int progress = 0;
    private float translateX = 0.0f;

    private float touchX = 0.0f, touchY = 0.0f;
    private boolean touching = false;
    private int previousPressedIndex = -1;

    private BluetoothSession session = null;

    public PianoRenderer(Context context, int numOctaves) {
        this.context = context;
        this.numOctaves = numOctaves;
    }

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i("LifeCycle", "onSurfaceCreated");

        String versionString = GLES30.glGetString(GLES30.GL_VERSION);
        Log.i(TAG, "OpenGL Version String: " + versionString);

        //Only include C6 from the 6th octave.
        keys = new PianoKey[KEYS_PER_OCTAVE * (numOctaves - 1) + 1];

        ShaderLoader loader = new ShaderLoader();
        int vertexShader = loader.compileShader(context, "default.vert", GLES30.GL_VERTEX_SHADER);
        int fragmentShader = loader.compileShader(context, "default.frag", GLES30.GL_FRAGMENT_SHADER);
        if (vertexShader == 0) {
            throw new RuntimeException("Cannot compile vertex shader.");
        } else if (fragmentShader == 0) {
            throw new RuntimeException("Cannot compile fragment shader.");
        }

        shaderProgram = loader.linkShader(vertexShader, fragmentShader);
        if (shaderProgram == 0) {
            throw new RuntimeException("Cannot link shader program.");
        }
        //Flag the shaders for deletion (they are currently attached to a shader program).
        GLES30.glDeleteShader(vertexShader);
        GLES30.glDeleteShader(fragmentShader);

        GLES30.glClearColor(0.4f, 0.4f, 0.4f, 1.0f);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);

        //Load textures.
        textures[0] = TextureHelper.loadTextureFromResource(context, R.drawable.key_w);
        textures[1] = TextureHelper.loadTextureFromResource(context, R.drawable.key_w0_pressed);
        textures[2] = TextureHelper.loadTextureFromResource(context, R.drawable.key_w1_pressed);
        textures[3] = TextureHelper.loadTextureFromResource(context, R.drawable.key_w2_pressed);
        textures[4] = TextureHelper.loadTextureFromResource(context, R.drawable.key_b);
        textures[5] = TextureHelper.loadTextureFromResource(context, R.drawable.key_b_pressed);
        textures[6] = TextureHelper.loadTextureFromResource(context, R.drawable.key_w3_pressed);

        labelTextures[0] = TextureHelper.loadTextureFromResource(context, R.drawable.c0);
        labelTextures[1] = TextureHelper.loadTextureFromResource(context, R.drawable.c1);
        labelTextures[2] = TextureHelper.loadTextureFromResource(context, R.drawable.c2);
        labelTextures[3] = TextureHelper.loadTextureFromResource(context, R.drawable.c3);
        labelTextures[4] = TextureHelper.loadTextureFromResource(context, R.drawable.c4);
        labelTextures[5] = TextureHelper.loadTextureFromResource(context, R.drawable.c5);
        labelTextures[6] = TextureHelper.loadTextureFromResource(context, R.drawable.c6);
    }

    public void update() {
        translateX = -(progress * whiteKeyWidth);

        //Change which key is pressed (if applicable).
        for (int index = 0; index < keys.length; index++) {
            if (index < keys.length - 1) {
                if (index == 0) {
                    //If index is the first.
                    boolean touchingNext = (keys[index + 1].insideBounds(touchX - translateX, touchY) && touching);
                    if (touchingNext && keys[index + 1].keyType == 1) {
                        //If touching the next key (and it is black), then it has priority.
                        //Do not indicate this key is pressed.
                        keys[index].setPressed(false);
                    } else if (keys[index].insideBounds(touchX - translateX, touchY) && touching) {
                        //Not touching a higher priority key.
                        keys[index].setPressed(true);
                    } else {
                        keys[index].setPressed(false);
                    }
                } else {
                    //If index is not the last index, nor the first.

                    boolean touchingPrevious = (keys[index - 1].insideBounds(touchX - translateX, touchY) && touching);
                    if (touchingPrevious && keys[index - 1].keyType == 1) {
                        //If touching the previous key (and it is black), then it has priority.
                        //Do not indicate this key is pressed.
                        keys[index].setPressed(false);
                        continue;
                    }

                    boolean touchingNext = (keys[index + 1].insideBounds(touchX - translateX, touchY) && touching);
                    if (touchingNext && keys[index + 1].keyType == 1) {
                        //If touching the next key (and it is black), then it has priority.
                        //Do not indicate this key is pressed.
                        keys[index].setPressed(false);
                    } else if (keys[index].insideBounds(touchX - translateX, touchY) && touching) {
                        //Not touching a higher priority key.
                        keys[index].setPressed(true);
                    } else {
                        keys[index].setPressed(false);
                    }
                }
            }
            else {
                //This is the last index.
                boolean touchingPrevious = (keys[index - 1].insideBounds(touchX - translateX, touchY) && touching);
                if (touchingPrevious && keys[index - 1].keyType == 1) {
                    //If touching the previous key (and it is black), then it has priority.
                    //Do not indicate this key is pressed.
                    keys[index].setPressed(false);
                    continue;
                }

                if (keys[index].insideBounds(touchX - translateX, touchY) && touching) {
                    keys[index].setPressed(true);
                } else {
                    keys[index].setPressed(false);
                }
            }

            if (keys[index].isPressed()) {
                //Only start new sound if a different key is pressed.
                if (previousPressedIndex != index) {
                    //Stop all sounds, then play new note.
                    mute();
                    playNote(index);
                }
                previousPressedIndex = index;
            }
        }
    }

    @Override
    public void onDrawFrame() {
        update();

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(viewMatrix, 0, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        GLES30.glUseProgram(shaderProgram);
        for (int index = 0; index < keys.length; index++) {
            if (keys[index] != null) {
                Matrix.setIdentityM(modelMatrix, 0);
                Matrix.translateM(modelMatrix, 0, translateX, 0.0f, 0.0f);
                GLES30.glUniformMatrix4fv(GLES30.glGetUniformLocation(shaderProgram, "model"), 1, false, modelMatrix, 0);
                GLES30.glUniformMatrix4fv(GLES30.glGetUniformLocation(shaderProgram, "view"), 1, false, viewMatrix, 0);
                GLES30.glUniformMatrix4fv(GLES30.glGetUniformLocation(shaderProgram, "projection"), 1, false, projectionMatrix, 0);

                keys[index].draw();

                if (keys[index].getLabel() != null) {
                    keys[index].getLabel().draw();
                }
            }
        }
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i("LifeCycle", "onSurfaceChanged (width=" + width + ", height=" + height + ")");
        this.width = width;
        this.height = height;

        float whiteKeysPerPage = 10.0f; //Originally was 7.0f (for a whole octave)
        whiteKeyWidth = width / whiteKeysPerPage;
        whiteKeyHeight = height;
        blackKeyWidth = whiteKeyWidth * 0.62f;
        blackKeyHeight = height * 0.669f;

        for (int index = 0; index < keys.length; index++) {
            if (isWhiteKey(index % KEYS_PER_OCTAVE)) {
                int whiteIndex = getWhitePositionIndex(index % KEYS_PER_OCTAVE);
                float whitePosition = ((index / KEYS_PER_OCTAVE * WHITE_KEYS_PER_OCTAVE) + whiteIndex) * whiteKeyWidth;
                int pressedTexture = getWhiteKeyPressedTexture(whiteIndex);

                //If this is the last key, we need to use a different pressed texture.
                if (index == keys.length - 1)
                {
                    //Specifically, the last key needs to be the depressed white key without black on it.
                    //This should be key_w3_pressed
                    pressedTexture = textures[6];
                }

                keys[index] = new PianoKey(whitePosition, 0, whiteKeyWidth, whiteKeyHeight, -1.0f, textures[0], pressedTexture, 0);
                if (whiteIndex == 0) {
                    //This is a C note, create a label.
                    keys[index].createLabel(labelTextures[index / KEYS_PER_OCTAVE]);
                }
            } else {
                int blackIndex = getBlackPositionIndex(index % KEYS_PER_OCTAVE);
                float blackPosition = ((index / KEYS_PER_OCTAVE * WHITE_KEYS_PER_OCTAVE) + blackIndex) * whiteKeyWidth + blackKeyWidth;
                keys[index] = new PianoKey(blackPosition, height - blackKeyHeight, blackKeyWidth, blackKeyHeight, 0.0f, textures[4], textures[5], 1);
            }
        }

        GLES30.glViewport(0, 0, width, height);
        Matrix.orthoM(projectionMatrix, 0, 0, width, 0, height, 0, 10.0f);
    }

    @Override
    public void onSurfaceDestroyed() {
        Log.i("LifeCycle", "onSurfaceDestroyed");
        //Delete shader program and the attached vertex and fragment shaders.
        GLES30.glDeleteProgram(shaderProgram);

        //Delete the piano key data from the GPU memory.
        for (int index = 0; index < keys.length; index++) {
            keys[index].delete();
        }

        //Delete the textures.
        GLES30.glDeleteTextures(textures.length, textures, 0);
        GLES30.glDeleteTextures(labelTextures.length, labelTextures, 0);
    }

    private boolean isWhiteKey(int index) {
        switch (index) {
            case 0:
            case 2:
            case 4:
            case 5:
            case 7:
            case 9:
            case 11:
                return true;
            default:
                return false;
        }
    }

    private int getWhitePositionIndex(int index) {
        switch (index) {
            case 0:
                return 0;
            case 2:
                return 1;
            case 4:
                return 2;
            case 5:
                return 3;
            case 7:
                return 4;
            case 9:
                return 5;
            case 11:
                return 6;
            default:
                throw new IllegalArgumentException("Invalid value for index: " + index);
        }
    }

    private int getWhiteKeyPressedTexture(int positionIndex) {
        switch (positionIndex) {
            case 0:
            case 3:
                return textures[1];
            case 2:
            case 6:
                return textures[2];
            case 1:
            case 4:
            case 5:
                return textures[3];
            default:
                return -1;
        }
    }

    private int getBlackPositionIndex(int index) {
        switch(index) {
            case 1:
                return 0;
            case 3:
                return 1;
            case 6:
                return 3;
            case 8:
                return 4;
            case 10:
                return 5;
            default:
                throw new IllegalArgumentException("Invalid value for index: " + index);
        }
    }

    private int getOctave(int index) {
        return index / 12;
    }

    /**
     * Notification that the progress level has changed. Clients can use the fromUser parameter
     * to distinguish user-initiated changes from those that occurred programmatically.
     *
     * @param seekBar  The SeekBar whose progress has changed
     * @param progress The current progress level. This will be in the range 0..max where max
     *                 was set by {@link ProgressBar#setMax(int)}. (The default value for max is 100.)
     * @param fromUser True if the progress change was initiated by the user.
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        this.progress = progress;
        Log.i(TAG, "onProgressChanged progress=" + progress +  ", fromUser=" + fromUser);

        //Key positions might have changed, invalidate the renderer.
        requestRender();
    }

    /**
     * Notification that the user has started a touch gesture. Clients may want to use this
     * to disable advancing the seekbar.
     *
     * @param seekBar The SeekBar in which the touch gesture began
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    /**
     * Notification that the user has finished a touch gesture. Clients may want to use this
     * to re-enable advancing the seekbar.
     *
     * @param seekBar The SeekBar in which the touch gesture began
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touchX = event.getX();
        touchY = height - event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touching = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            //Stop all current sounds.
            mute();
            previousPressedIndex = -1;
            touching = false;
        }

        requestRender();
        return true;
    }

    @Override
    public void onBluetoothSessionFailed(BluetoothSession session, BluetoothDevice device) {
        this.session = null;
    }

    @Override
    public void onBluetoothSessionStarted(BluetoothSession session, BluetoothDevice device) {
        this.session = session;
    }

    @Override
    public void onBluetoothSessionEnded(BluetoothSession session, BluetoothDevice device, boolean fromUser) {
        this.session = null;
    }

    public void playNote(int index) {
        if (session == null) return;

        int note = 0;
        int sharp = 0;
        switch (index % 12) {
            case 0:
                note = NOTE_C; //C
                break;
            case 1:
                note = NOTE_C; //C#
                sharp = 1;
                break;
            case 2:
                note = NOTE_D; //D
                break;
            case 3:
                note = NOTE_D; //D#
                sharp = 1;
                break;
            case 4:
                note = NOTE_E; //E
                break;
            case 5:
                note = NOTE_F; //F
                break;
            case 6:
                note = NOTE_F; //F#
                sharp = 1;
                break;
            case 7:
                note = NOTE_G; //G
                break;
            case 8:
                note = NOTE_G; //G#
                sharp = 1;
                break;
            case 9:
                note = NOTE_A; //A;
                break;
            case 10:
                note = NOTE_A; //A#
                sharp = 1;
                break;
            case 11:
                note = NOTE_B; //B
                break;
        }

        //Android app isn't concerned with channel in real-time mode.
        int channel = 0;

        //Android app isn't concerned with exact timing of note in real-time mode.
        //Just make sure we don't set it to 0 (which mutes).
        int length = 0x1F;

        short command = 0;
        command |= (short)(0x0007 & note);
        command |= (short)(0x0008 & (sharp << 3));
        command |= (short)(0x0070 & (getOctave(index) << 4));
        command |= (short)(0x0380 & (channel << 7));
        command |= (short)(0x7C00 & (length << 10));
        Log.i(TAG, String.format("Index=%d, Note=%d, Sharp=%d, Octave=%d, Channel=%d, Length=%d", index, note, sharp, getOctave(index), channel, length));

        ByteBuffer b = ByteBuffer.allocate(2);
        b.putShort(command);

        session.write(b.array());
    }

    public void mute() {
        if (session == null) return;

        short command = (short)0x8000;

        ByteBuffer b = ByteBuffer.allocate(2);
        b.putShort(command);

        session.write(b.array());
    }
}
