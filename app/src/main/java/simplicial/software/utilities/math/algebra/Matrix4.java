package simplicial.software.utilities.math.algebra;

import android.opengl.Matrix;

import simplicial.software.utilities.math.geometry.Vector3;

public class Matrix4 {
    public final float[] values = new float[16];
    // These arrays are to avoid memory allocation in operations.
    private final float[] operationResult = new float[16];
    private final float[] multiplicationResult = new float[16];

    public Matrix4() {
        Matrix.setIdentityM(values, 0);
    }

    public void rightMultiply(float[] rhs) {
        Matrix.setIdentityM(multiplicationResult, 0);
        Matrix.multiplyMM(multiplicationResult, 0, values, 0, rhs, 0);
        System.arraycopy(multiplicationResult, 0, values, 0, 16);
    }

    public void translate(double x, double y, double z) {
        translate((float) x, (float) y, (float) z);
    }

    public void translate(float x, float y, float z) {
        Matrix.setIdentityM(operationResult, 0);
        Matrix.translateM(operationResult, 0, x, y, z);
        rightMultiply(operationResult);
    }

    public void translate(Vector3 translation) {
        translate(translation.x, translation.y, translation.z);
    }

    public void scale(float x, float y, float z) {
        Matrix.setIdentityM(operationResult, 0);
        Matrix.scaleM(operationResult, 0, x, y, z);
        rightMultiply(operationResult);
    }

    public void rotate(float angleDegrees, float x, float y, float z) {
        Matrix.setIdentityM(operationResult, 0);
        Matrix.rotateM(operationResult, 0, angleDegrees, x, y, z);
        rightMultiply(operationResult);
    }

    public final float[] getFloats() {
        return values;
    }

    public void setIdentity() {
        Matrix.setIdentityM(values, 0);
    }

    public void setPerspective(float fov, float aspect, float near, float far) {
        Matrix.perspectiveM(values, 0, fov, aspect, near, far);
    }

    public void setLookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY,
                          float centerZ, float upX, float upY, float upZ) {
        Matrix.setLookAtM(values, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    }

    public void setOrtho(float left, float right, float bottom, float top, float near, float far) {
        Matrix.orthoM(values, 0, left, right, bottom, top, near, far);
    }

    public void set(Matrix4 matrix) {
        System.arraycopy(matrix.values, 0, values, 0, 16);
    }

    public void scale(float s) {
        scale(s, s, s);
    }
}
