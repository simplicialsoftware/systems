package simplicial.software.utilities.color;

public class ColorRGB {
    public static final int SIZE = 4 * 4;
    public final float r;
    public final float g;
    public final float b;
    private final float[] floatArray = new float[3];

    public ColorRGB(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public float[] getFloatArray() {
        floatArray[0] = r;
        floatArray[1] = g;
        floatArray[2] = b;
        return floatArray;
    }
}
