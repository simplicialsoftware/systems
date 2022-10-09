package simplicial.software.models;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import simplicial.software.opengl.vertexformats.VertexPosition;
import simplicial.software.opengl.vertexformats.VertexPositionTexture;
import simplicial.software.utilities.math.geometry.Vector2;
import simplicial.software.utilities.math.geometry.Vector3;


public class Particle {
    public final static int TRAIL_SIZE = 100;
    public static final VertexPositionTexture[] VERTICES = new VertexPositionTexture[4];
    public static final FloatBuffer PARTICLE_VERTEX_BUFFER =
            ByteBuffer.allocateDirect(4 * VertexPositionTexture.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private static final double MAX_COORDINATE_VALUE = 10000;
    private static final double MIN_COORDINATE_VALUE = -10000;

    static {
        VERTICES[0] = new VertexPositionTexture(new Vector3(-.5, -0.5, 0), new Vector2(0, 0));
        VERTICES[1] = new VertexPositionTexture(new Vector3(0.5, -0.5, 0), new Vector2(1, 0));
        VERTICES[2] = new VertexPositionTexture(new Vector3(-0.5, 0.5, 0), new Vector2(0, 1));
        VERTICES[3] = new VertexPositionTexture(new Vector3(0.5, 0.5, 0), new Vector2(1, 1));
        for (int i = 0; i < VERTICES.length; i++) {
            PARTICLE_VERTEX_BUFFER.put(VERTICES[i].getFloats());
        }
        PARTICLE_VERTEX_BUFFER.rewind();
    }

    private final Lock trailLock = new ReentrantLock();
    private final Snapshot trailSnapshot = new Snapshot();
    private final VertexPosition[] trail;
    public double X;
    public double Y;
    public double Z;
    public double vX;
    public double vY;
    public double vZ;
    /**
     * Reads and writes are atomic for all variables declared volatile (including long and double
     * variables).
     * https://docs.oracle.com/javase/tutorial/essential/concurrency/atomic.html
     */
    public volatile double lastTrailPointUpdateTimeS = 0;
    public double lifetimeS = 0;
    private int trailIndex;
    private int trailLength = 0;
    private int trailUpdateCounter = 0;

    public Particle() {
        trail = new VertexPosition[TRAIL_SIZE];
        for (int i = 0; i < trail.length; i++) {
            trail[i] = new VertexPosition();
        }

        reset(0, 0, 0, 0, 0, 0, 0);

        trailSnapshot.trailIndex = trailIndex;
        trailSnapshot.trailLength = trailLength;
        trailSnapshot.trailUpdateCounter = trailUpdateCounter;
    }

    public void setPosition(double x, double y, double z, double vX, double vY, double vZ) {
        if (x > MAX_COORDINATE_VALUE)
            x = MAX_COORDINATE_VALUE;
        if (x < MIN_COORDINATE_VALUE)
            x = MIN_COORDINATE_VALUE;
        if (y > MAX_COORDINATE_VALUE)
            y = MAX_COORDINATE_VALUE;
        if (y < MIN_COORDINATE_VALUE)
            y = MIN_COORDINATE_VALUE;
        if (z > MAX_COORDINATE_VALUE)
            z = MAX_COORDINATE_VALUE;
        if (z < MIN_COORDINATE_VALUE)
            z = MIN_COORDINATE_VALUE;
        X = x;
        Y = y;
        Z = z;
        this.vX = vX;
        this.vY = vY;
        this.vZ = vZ;

        trail[trailIndex].pos.x = X;
        trail[trailIndex].pos.y = Y;
        trail[trailIndex].pos.z = Z;
    }

    public void updateTrail(double currentTime) {
        trailLock.lock();
        try {
            trailIndex++;
            if (trailIndex == trail.length) {
                trailIndex = 0;
                trail[trailIndex].pos.x = X;
                trail[trailIndex].pos.y = Y;
                trail[trailIndex].pos.z = Z;
                trailIndex = 1;
                trailUpdateCounter++;
            }
            trail[trailIndex].pos.x = X;
            trail[trailIndex].pos.y = Y;
            trail[trailIndex].pos.z = Z;
            trailUpdateCounter++;

            if (trailLength < TRAIL_SIZE)
                trailLength++;
            lastTrailPointUpdateTimeS = currentTime;
        } finally {
            trailLock.unlock();
        }
    }

    public Snapshot getTrailSnapshot() {
        trailLock.lock();
        try {
            if (trailUpdateCounter >= Particle.TRAIL_SIZE) {
                trailSnapshot.trailFloatBuffer.position(0);
                for (int i = 0; i < Particle.TRAIL_SIZE; i++)
                    trailSnapshot.trailFloatBuffer.put(trail[i].getFloats());
            } else {
                int start = Math.max(trailIndex + 1 - trailUpdateCounter, 0);
                int length = Math.min(trailUpdateCounter, Particle.TRAIL_SIZE - start);

                trailSnapshot.trailFloatBuffer.position(start * 3);
                for (int i = start; i < start + length; i++)
                    trailSnapshot.trailFloatBuffer.put(trail[i].getFloats());
                if (trailIndex + 1 < trailUpdateCounter) {
                    start = Particle.TRAIL_SIZE + trailIndex - trailUpdateCounter;
                    length = trailUpdateCounter - trailIndex;
                    trailSnapshot.trailFloatBuffer.position(start * 3);
                    for (int i = start; i < start + length; i++)
                        trailSnapshot.trailFloatBuffer.put(trail[i].getFloats());
                }
            }
            trailSnapshot.trailIndex = trailIndex;
            trailSnapshot.trailLength = trailLength;
            trailSnapshot.trailUpdateCounter = trailUpdateCounter;
            trailUpdateCounter = 1;
            return trailSnapshot;
        } finally {
            trailLock.unlock();
        }
    }

    public void reset(double x, double y, double z, double vX, double vY, double vZ,
                      double currentTime) {
        trailLock.lock();
        try {
            X = x;
            Y = y;
            Z = z;
            this.vX = vX;
            this.vY = vY;
            this.vZ = vZ;

            trailIndex = 0;

            trail[trailIndex].pos.x = X;
            trail[trailIndex].pos.y = Y;
            trail[trailIndex].pos.z = Z;

            trailIndex++;

            trail[trailIndex].pos.x = X;
            trail[trailIndex].pos.y = Y;
            trail[trailIndex].pos.z = Z;

            trailUpdateCounter = 2;
            trailLength = 1;
            lastTrailPointUpdateTimeS = currentTime;
        } finally {
            trailLock.unlock();
        }
    }

    public void forceFullTrailUpdate() {
        trailUpdateCounter = TRAIL_SIZE;
    }

    public static class Snapshot {
        public final FloatBuffer trailFloatBuffer =
                ByteBuffer.allocateDirect(TRAIL_SIZE * VertexPosition.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
        public int trailIndex;
        public int trailLength;
        public int trailUpdateCounter;
    }
}