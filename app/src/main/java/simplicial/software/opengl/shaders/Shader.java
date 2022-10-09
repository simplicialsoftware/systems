package simplicial.software.opengl.shaders;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.IntBuffer;

public class Shader {
    private final int programID;

    public Shader(Context context, String vertexFilePath, String fragmentFilePath) {
        programID = GLES20.glCreateProgram();

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, loadSource(vertexFilePath, context));
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, loadSource(fragmentFilePath,
                context));

        GLES20.glAttachShader(programID, vertexShader);
        GLES20.glAttachShader(programID, fragmentShader);

        GLES20.glLinkProgram(programID);
        if (!getLinkStatus()) {
            String logText = GLES20.glGetProgramInfoLog(programID);
            Log.e(getClass().getName(), "Shader linking failed.\n" + logText);
        }

        GLES20.glDetachShader(programID, vertexShader);
        GLES20.glDetachShader(programID, fragmentShader);
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
    }

    public void use() {
        GLES20.glUseProgram(programID);
    }

    public int getUniformLocation(String name) {
        int location = GLES20.glGetUniformLocation(programID, name);
        if (location < 0) {
            Log.e(getClass().getName(), "Failed to locate uniform: " + name);
            return -1;
        }
        return location;
    }

    public int getAttributeLocation(String name) {
        int location = GLES20.glGetAttribLocation(programID, name);
        if (location < 0) {
            Log.e(getClass().getName(), "Failed to locate attribute: " + name);
            return -1;
        }
        return location;
    }

    public boolean getLinkStatus() {
        IntBuffer status = IntBuffer.allocate(1);
        GLES20.glGetProgramiv(programID, GLES20.GL_LINK_STATUS, status);
        return (status.get(0) == GLES20.GL_TRUE);
    }

    private int loadShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        if (GLES20.glGetError() != GLES20.GL_NO_ERROR)
            Log.e(getClass().getName(), GLES20.glGetShaderInfoLog(shader));
        return shader;
    }

    private String loadSource(String fileName, Context context) {
        StringBuilder source = new StringBuilder();

        try {
            InputStream stream = context.getResources().getAssets().open(fileName);
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(streamReader);
            String line;
            while ((line = reader.readLine()) != null)
                source.append(line);
        } catch (Exception e) {
            Log.e(getClass().getName(),
                    "Exception occured while loading program from assets: " + e.getMessage());
            return null;
        }

        return source.toString();
    }
}
