package simplicial.software.opengl.vertexformats;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import simplicial.software.utilities.math.geometry.Vector2;
import simplicial.software.utilities.math.geometry.Vector3;


public class VertexPositionTexture {
    public final static int SIZE = Vector3.SIZE + Vector2.SIZE;
    public static final int POSITION_OFFSET = 0;
    public final static int TEXTURE_OFFSET = POSITION_OFFSET + Vector3.SIZE;

    public final Vector3 Position;
    public final Vector2 TextureCoords;

    final FloatBuffer buffer =
            ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

    public VertexPositionTexture(Vector3 position, Vector2 textureCoords) {
        this.Position = position;
        this.TextureCoords = textureCoords;
    }

    public FloatBuffer getFloats() {
        buffer.rewind();
        buffer.put(Position.getFloatArray());
        buffer.put(TextureCoords.getFloatArray());
        buffer.rewind();
        return buffer;
    }
}
