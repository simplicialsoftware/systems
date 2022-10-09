package simplicial.software.opengl.shaders;

import android.content.Context;
import android.opengl.GLES20;

import simplicial.software.utilities.color.ColorRGB;
import simplicial.software.utilities.math.algebra.Matrix4;

public class PointShader extends Shader {
    public final int aPosition;
    public final int aTexCoords;

    private final int uPVM;
    private final int uColor;

    public PointShader(Context context) {
        super(context, "point.vertex", "point.fragment");

        aPosition = getAttributeLocation("aPosition");
        aTexCoords = getAttributeLocation("aTexCoords");

        uPVM = getUniformLocation("uPVM");
        uColor = getUniformLocation("uColor");
    }

    public void setPVM(Matrix4 matrix) {
        GLES20.glUniformMatrix4fv(uPVM, 1, false, matrix.getFloats(), 0);
    }

    public void setColor(ColorRGB color) {
        GLES20.glUniform3fv(uColor, 1, color.getFloatArray(), 0);
    }
}
