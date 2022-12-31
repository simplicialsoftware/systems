package simplicial.software.views;

import static android.content.Context.WINDOW_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import simplicial.software.Systems.R;
import simplicial.software.models.AppState;
import simplicial.software.models.Particle;
import simplicial.software.models.Settings;
import simplicial.software.models.Simulator;
import simplicial.software.opengl.shaders.LineShader;
import simplicial.software.opengl.shaders.PointShader;
import simplicial.software.opengl.vertexformats.VertexPosition;
import simplicial.software.opengl.vertexformats.VertexPositionTexture;
import simplicial.software.utilities.android.InputController;
import simplicial.software.utilities.color.ColorRGB;
import simplicial.software.utilities.math.algebra.Matrix4;
import simplicial.software.utilities.math.geometry.Segment;
import simplicial.software.utilities.math.geometry.Vector3;
import simplicial.software.utilities.string.StringFormatter;

public class SimulationView extends GLSurfaceView implements Renderer {
    private static final ColorRGB X_AXIS_COLOR = new ColorRGB(1, 0, 0);
    private static final ColorRGB Y_AXIS_COLOR = new ColorRGB(0, 1, 0);
    private static final ColorRGB Z_AXIS_COLOR = new ColorRGB(0.117f, 0.549f, 1);
    private static final ColorRGB[] PARTICLE_COLORS = new ColorRGB[]{new ColorRGB(1, 1, 0),
            new ColorRGB(0, 1, 1), new ColorRGB(1, 0, 1)};
    private static final ColorRGB[] TRAIL_COLORS = new ColorRGB[]{new ColorRGB(1, 0.5f, 0),
            new ColorRGB(0, 0.5f, 1), new ColorRGB(1, 0, 0.5f)};
    private static final double ROTATION_SPEED = 4;
    private static final double ZOOM_SPEED = 1 / 500.0;

    final InputController inputController = new InputController(1, 1);
    private final Matrix4 view = new Matrix4();
    private final Matrix4 projection = new Matrix4();
    private final Matrix4 model = new Matrix4();
    private final Matrix4 projectionView = new Matrix4();
    private final Matrix4 projectionViewModel = new Matrix4();
    private final Vector3 translation = new Vector3();
    private final Segment segment = new Segment();
    private final Point windowSize = new Point();
    private final int[] particleVBOs = new int[1];
    private final int[] trailVBOs = new int[Simulator.MAX_PARTICLE_COUNT];
    private PointShader pointShader;
    private LineShader lineShader;
    private float width = 0;
    private float height = 0;
    private float aspect = 1;

    private ElapsedTimeRunnable elapsedTimeRunnable = null;
    private float yRotation = (float) (Math.PI / 4.0);
    private float xRotation = (float) (Math.PI / 4.0);
    private float cameraDistanceFromOrigin = 1;

    public SimulationView(Context context) {
        super(context);
        init();
    }

    public SimulationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Log.d("Extensions:", GLES20.glGetString(GLES20.GL_EXTENSIONS));

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glBlendFuncSeparate(GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA, GLES20.GL_SRC_ALPHA,
                GLES20.GL_DST_ALPHA);
        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD);

        float[] lineWidthRange = new float[2];
        GLES20.glGetFloatv(GLES20.GL_ALIASED_LINE_WIDTH_RANGE, lineWidthRange, 0);
        Simulator.INSTANCE.setLineWidthRange(lineWidthRange[0], lineWidthRange[1]);

        pointShader = new PointShader(getContext());
        lineShader = new LineShader(getContext());

        setRenderMode(RENDERMODE_CONTINUOUSLY);

        WindowManager windowManager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Rect bounds = windowManager.getCurrentWindowMetrics().getBounds();
            windowSize.x = bounds.width();
            windowSize.y = bounds.height();
        } else {
            getWindowSize(windowManager, windowSize);
        }

        GLES20.glGenBuffers(trailVBOs.length, trailVBOs, 0);
        for (int i = 0; i < trailVBOs.length; i++) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, trailVBOs[i]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, VertexPosition.SIZE * Particle.TRAIL_SIZE
                    , null, GLES20.GL_DYNAMIC_DRAW);
        }

        GLES20.glGenBuffers(1, particleVBOs, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, particleVBOs[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, VertexPositionTexture.SIZE * 4,
                Particle.PARTICLE_VERTEX_BUFFER, GLES20.GL_DYNAMIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    @SuppressWarnings({"deprecation", "RedundantSuppression"})
    private static void getWindowSize(WindowManager windowManager, Point windowSize) {
        windowManager.getDefaultDisplay().getSize(windowSize);
    }

    public void init() {
        setEGLContextClientVersion(2);
        setRenderer(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        Activity activity = (Activity) getContext();
        if (elapsedTimeRunnable == null)
            elapsedTimeRunnable =
                    new ElapsedTimeRunnable(activity.findViewById(R.id.tvElapsedTime));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = inputController.update(event);
        PointF p1 = inputController.getPointer1();
        if (p1 != null) {
            PointF p2 = inputController.getPointer2();
            if (p2 != null) {
                cameraDistanceFromOrigin -= (inputController.getPointerDistanceChange()) * ZOOM_SPEED;
                if (cameraDistanceFromOrigin > 10)
                    cameraDistanceFromOrigin = 10;
                if (cameraDistanceFromOrigin < 1)
                    cameraDistanceFromOrigin = 1;

            } else {
                yRotation -= ROTATION_SPEED * (inputController.getPointer1Movement().x) / (float) windowSize.x;
                xRotation += ROTATION_SPEED * (inputController.getPointer1Movement().y) / (float) windowSize.y;
                if (xRotation < -Math.PI / 2.0 + 0.01)
                    xRotation = (float) (-Math.PI / 2.0 + 0.01);
                if (xRotation > Math.PI / 2.0 - .01)
                    xRotation = (float) (Math.PI / 2.0 - 0.01);
            }
        }

        if (handled)
            return true;
        return super.onTouchEvent(event);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // GLES20.glDepthMask(true);
        GLES20.glLineWidth((float) AppState.INSTANCE.currentConfiguration.lineWidth);

        float xAxisLength;
        float yAxisLength;
        float zAxisLength;
        if (AppState.INSTANCE.currentConfiguration.simulationDimensions == Settings.SIMULATION_DIMENSIONS_2D) {
            Simulator.INSTANCE.setBoundaries(cameraDistanceFromOrigin * aspect,
                    cameraDistanceFromOrigin, 0);
            projection.setOrtho(-cameraDistanceFromOrigin * aspect,
                    cameraDistanceFromOrigin * aspect, -cameraDistanceFromOrigin,
                    cameraDistanceFromOrigin,
                    .01f, 200);
            view.setLookAt(0, 0, cameraDistanceFromOrigin, 0, 0, 0, 0, 1, 0);
            xAxisLength = width;
            yAxisLength = height;
            zAxisLength = 0;
        } else {
            Simulator.INSTANCE.setBoundaries(1, 1, 1);
            projection.setPerspective(90, width / height, .01f, 200);
            view.setLookAt((float) (Math.cos(xRotation) * Math.sin(yRotation) * cameraDistanceFromOrigin),
                    (float) (Math.sin(xRotation) * cameraDistanceFromOrigin),
                    (float) (Math.cos(xRotation) * Math.cos(yRotation) * cameraDistanceFromOrigin),
                    0, 0, 0, 0, 1, 0);
            xAxisLength = 1;
            yAxisLength = 1;
            zAxisLength = 1;
        }
        projectionView.set(projection);
        projectionView.rightMultiply(view.getFloats());

        pointShader.use();
        pointShader.setColor(PARTICLE_COLORS[AppState.INSTANCE.currentConfiguration.colorScheme.ordinal()]);

        lineShader.use();
        lineShader.setColor(TRAIL_COLORS[AppState.INSTANCE.currentConfiguration.colorScheme.ordinal()]);

        lineShader.setPV(projectionView);

        // Draw the axes
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        segment.set(-xAxisLength, 0, 0, xAxisLength, 0, 0);
        lineShader.setColor(X_AXIS_COLOR);
        GLES20.glVertexAttribPointer(lineShader.aPosition, 3, GLES20.GL_FLOAT, false,
                VertexPosition.SIZE, segment.getFloats());
        GLES20.glEnableVertexAttribArray(lineShader.aPosition);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

        segment.set(0, -yAxisLength, 0, 0, yAxisLength, 0);
        lineShader.setColor(Y_AXIS_COLOR);
        GLES20.glVertexAttribPointer(lineShader.aPosition, 3, GLES20.GL_FLOAT, false,
                VertexPosition.SIZE, segment.getFloats());
        GLES20.glEnableVertexAttribArray(lineShader.aPosition);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

        if (AppState.INSTANCE.currentConfiguration.simulationDimensions == Settings.SIMULATION_DIMENSIONS_3D) {
            segment.set(0, 0, -zAxisLength, 0, 0, zAxisLength);
            lineShader.setColor(Z_AXIS_COLOR);
            GLES20.glVertexAttribPointer(lineShader.aPosition, 3, GLES20.GL_FLOAT,
                    false, VertexPosition.SIZE, segment.getFloats());
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
        }

        // GLES20.glDepthMask(false);

        Simulator.INSTANCE.particleLock.lock();
        try {
            lineShader.use();
            lineShader.setColor(TRAIL_COLORS[AppState.INSTANCE.currentConfiguration.colorScheme.ordinal()]);

            Particle[] particles = Simulator.INSTANCE.getParticles();

            // Draw trails
            for (int i = 0; i < particles.length; i++) {
                Particle.Snapshot snapshot = particles[i].getTrailSnapshot();

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, trailVBOs[i]);
                if (snapshot.trailUpdateCounter >= Particle.TRAIL_SIZE) {
                    snapshot.trailFloatBuffer.position(0);
                    GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0,
                            Particle.TRAIL_SIZE * VertexPosition.SIZE, snapshot.trailFloatBuffer);
                } else {
                    int start = Math.max(snapshot.trailIndex + 1 - snapshot.trailUpdateCounter, 0);
                    int length = Math.min(snapshot.trailUpdateCounter, Particle.TRAIL_SIZE - start);
                    snapshot.trailFloatBuffer.position(start * 3);
                    GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, start * VertexPosition.SIZE,
                            length * VertexPosition.SIZE, snapshot.trailFloatBuffer);
                    if (snapshot.trailIndex + 1 < snapshot.trailUpdateCounter) {
                        start = Particle.TRAIL_SIZE + snapshot.trailIndex - snapshot.trailUpdateCounter;
                        length = snapshot.trailUpdateCounter - snapshot.trailIndex;
                        snapshot.trailFloatBuffer.position(start * 3);
                        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,
                                start * VertexPosition.SIZE, length * VertexPosition.SIZE,
                                snapshot.trailFloatBuffer);
                    }
                }

                GLES20.glVertexAttribPointer(lineShader.aPosition, 3, GLES20.GL_FLOAT
                        , false, VertexPosition.SIZE, 0);
                GLES20.glEnableVertexAttribArray(lineShader.aPosition);
                // GLES20.glDrawArrays(GLES20.GL_POINTS, 0, Particle.TrailSize);
                GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, snapshot.trailIndex + 1);
                if (snapshot.trailLength >= 99 && snapshot.trailIndex < 99)
                    GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, snapshot.trailIndex + 1,
                            Particle.TRAIL_SIZE - (snapshot.trailIndex + 1));
            }

            // Draw particles
            pointShader.use();

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, particleVBOs[0]);
            GLES20.glVertexAttribPointer(pointShader.aPosition, 3, GLES20.GL_FLOAT,
                    false, VertexPositionTexture.SIZE,
                    VertexPositionTexture.POSITION_OFFSET);
            GLES20.glEnableVertexAttribArray(pointShader.aPosition);
            GLES20.glVertexAttribPointer(pointShader.aTexCoords, 2, GLES20.GL_FLOAT,
                    false, VertexPositionTexture.SIZE,
                    VertexPositionTexture.TEXTURE_OFFSET);
            GLES20.glEnableVertexAttribArray(pointShader.aTexCoords);

            GLES20.glEnable(GLES20.GL_BLEND);

            for (int i = 0; i < particles.length; i++) {
                model.setIdentity();
                translation.set(particles[i].X, particles[i].Y, particles[i].Z);
                model.translate(translation);
                model.scale(cameraDistanceFromOrigin / 40);
                if (AppState.INSTANCE.currentConfiguration.simulationDimensions == Settings.SIMULATION_DIMENSIONS_3D) {
                    model.rotate((float) Math.toDegrees(xRotation), (float) -Math.cos(yRotation),
                            0, (float) Math.sin(yRotation));
                    model.rotate((float) Math.toDegrees(yRotation), 0, 1, 0);
                }
                projectionViewModel.set(projectionView);
                projectionViewModel.rightMultiply(model.getFloats());

                pointShader.setPVM(projectionViewModel);

                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            }

        } finally {
            Simulator.INSTANCE.particleLock.unlock();
        }

        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        ((Activity) getContext()).runOnUiThread(elapsedTimeRunnable);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        this.width = width;
        this.height = height;
        aspect = (float) width / (float) height;
        GLES20.glViewport(0, 0, width, height);
    }

    private static class ElapsedTimeRunnable implements Runnable {
        private final char[] text = "t= 0000.00".toCharArray();

        private final TextView textViewTime;

        private ElapsedTimeRunnable(@NonNull TextView textViewTime) {
            this.textViewTime = textViewTime;
        }

        @Override
        public void run() {
            if (AppState.INSTANCE.currentConfiguration.simulationMode == Settings.SIMULATION_MODE_MATRIX) {
                if (textViewTime.getVisibility() == VISIBLE)
                    textViewTime.setVisibility(GONE);
            } else {
                if (textViewTime.getVisibility() == GONE)
                    textViewTime.setVisibility(VISIBLE);
                double time = Simulator.INSTANCE.getElapsedTimeS();
                StringFormatter.fillStringWithPositiveDoubleFast(text, 3, Math.abs(time), 4, 2);
                if (time < 0)
                    text[2] = '-';
                else text[2] = ' ';
                textViewTime.setText(text, 0, text.length);
            }
        }
    }
}
