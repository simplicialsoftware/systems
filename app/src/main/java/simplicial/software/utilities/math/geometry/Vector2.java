package simplicial.software.utilities.math.geometry;

import java.io.Serializable;

public class Vector2 implements Serializable {
    public static final int SIZE = 4 * 2;
    private static final long serialVersionUID = 3;
    public final double x;
    public final double y;
    private final float[] floatArray = new float[2];

    public Vector2(double ix, double iy) {
        x = ix;
        y = iy;
    }

    public Vector2 add(Vector2 v) {
        return new Vector2(x + v.x, y + v.y);
    }

    public Vector2 add(Vector2 v, float s) {
        return new Vector2(x + v.x * s, y + v.y * s);
    }

    public float[] getFloatArray() {
        floatArray[0] = (float) x;
        floatArray[1] = (float) y;
        return floatArray;
    }
}
