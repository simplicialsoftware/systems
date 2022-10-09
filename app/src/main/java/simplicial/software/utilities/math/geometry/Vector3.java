package simplicial.software.utilities.math.geometry;

import java.io.Serializable;

public class Vector3 implements Serializable {
    public static final int SIZE = 3 * 4;
    private static final long serialVersionUID = 3;
    private final float[] floatArray = new float[3];
    public double x;
    public double y;
    public double z;

    public Vector3() {
        x = 0.0;
        y = 0.0;
        z = 0.0;
    }

    public Vector3(double ix, double iy, double iz) {
        x = ix;
        y = iy;
        z = iz;
    }

    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 add(Vector3 v) {
        return new Vector3(x + v.x, y + v.y, z + v.z);
    }

    public Vector3 add(Vector3 v, double s) {
        return new Vector3(x + v.x * s, y + v.y * s, z + v.z * s);
    }

    public float[] getFloatArray() {
        floatArray[0] = (float) x;
        floatArray[1] = (float) y;
        floatArray[2] = (float) z;
        return floatArray;
    }
}
