package simplicial.software.opengl.vertexformats;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import simplicial.software.utilities.math.geometry.Vector3;


public class VertexPosition {
    public final static int SIZE = Vector3.SIZE;

    public final Vector3 pos;

    final FloatBuffer buffer =
            ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

    public VertexPosition() {
        pos = new Vector3();
    }

    public FloatBuffer getFloats() {
        buffer.rewind();
        buffer.put(pos.getFloatArray());
        buffer.rewind();
        return buffer;
    }
}
