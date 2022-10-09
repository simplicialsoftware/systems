package simplicial.software.opengl.shaders;

import android.content.Context;
import android.opengl.GLES20;

import simplicial.software.utilities.color.ColorRGB;
import simplicial.software.utilities.math.algebra.Matrix4;

public class LineShader extends Shader {
    public final int aPosition;

    private final int uColor;
    private final int uPV;

    public LineShader(Context context) {
        super(context, "line.vertex", "line.fragment");

        aPosition = getAttributeLocation("aPosition");

        uPV = getUniformLocation("uPV");
        uColor = getUniformLocation("uColor");
    }

    public void setPV(Matrix4 matrix) {
        GLES20.glUniformMatrix4fv(uPV, 1, false, matrix.getFloats(), 0);
    }

    public void setColor(ColorRGB color) {
        GLES20.glUniform3fv(uColor, 1, color.getFloatArray(), 0);
    }
}
