package simplicial.software.utilities.math.geometry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Segment {
    public final Vector3 p1;
    public final Vector3 p2;
    private final FloatBuffer vertexBuffer =
            ByteBuffer.allocateDirect(2 * Vector3.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

    public Segment() {
        p1 = new Vector3();
        p2 = new Vector3();
    }

    public void set(double x1, double y1, double z1, double x2, double y2, double z2) {
        p1.set(x1, y1, z1);
        p2.set(x2, y2, z2);
    }

    public FloatBuffer getFloats() {
        vertexBuffer.rewind();
        vertexBuffer.put(p1.getFloatArray());
        vertexBuffer.put(p2.getFloatArray());
        vertexBuffer.rewind();
        return vertexBuffer;
    }
}