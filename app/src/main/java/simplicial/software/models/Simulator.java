package simplicial.software.models;

import android.nfc.FormatException;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import simplicial.software.utilities.expressions.ExpressionTree;
import simplicial.software.utilities.expressions.Variable;

public class Simulator {
    public static final int MAX_PARTICLE_COUNT = 500;
    public static final Simulator INSTANCE = new Simulator();
    private static final String VALID_SYMBOLS = "x y z x' y' z' t + - * / ^ . (  ) [0-9] sin cos " +
            "asin acos abs";
    public final Lock particleLock = new ReentrantLock();
    private final Random random = new Random();
    private final Lock pauseResumeLock = new ReentrantLock();
    private final List<String> validSymbols = new ArrayList<>();
    private final List<Variable> variables = new ArrayList<>();
    private Particle[] particles = new Particle[0];
    private double elapsedTimeS = 0;
    private double realElapsedTimeS = 0;
    private Thread simulationThread;
    private volatile boolean simulationRunning = false;
    private double xBoundaryHalfWidth = .5;
    private double yBoundaryHalfWidth = .5;
    private double zBoundaryHalfWidth = .5;
    private double minLineWidth = 1;
    private double maxLineWidth = 1;
    private ExpressionTree dxdtTree = new ExpressionTree();
    private ExpressionTree dydtTree = new ExpressionTree();
    private ExpressionTree dzdtTree = new ExpressionTree();
    private double particleRepositionQuota = 0;
    private boolean resetParticlesFlag = false;

    private Simulator() {
        validSymbols.add("x");
        validSymbols.add("y");
        validSymbols.add("z");
        validSymbols.add("x'");
        validSymbols.add("y'");
        validSymbols.add("z'");
        validSymbols.add("t");

        variables.add(new Variable("x", 0.0));
        variables.add(new Variable("y", 0.0));
        variables.add(new Variable("z", 0.0));
        variables.add(new Variable("x'", 0.0));
        variables.add(new Variable("y'", 0.0));
        variables.add(new Variable("z'", 0.0));
        variables.add(new Variable("t", 0.0));
    }

    public void initializeParticles() {
        particleLock.lock();
        try {
            particles = new Particle[AppState.INSTANCE.currentConfiguration.particleCount];
            for (int i = 0; i < AppState.INSTANCE.currentConfiguration.particleCount; i++) {
                particles[i] = new Particle();
                particles[i].lifetimeS =
                        (AppState.INSTANCE.currentConfiguration.particleDuration * ((double) i / (double) AppState.INSTANCE.currentConfiguration
                        .particleCount));
                repositionParticle(particles[i], 0);
            }
        } finally {
            particleLock.unlock();
        }
    }

    public void setDefaultSettings() {
        AppState.INSTANCE.currentConfiguration.randomizeVelocities = false;
        AppState.INSTANCE.currentConfiguration.timeScale = 1;
        AppState.INSTANCE.currentConfiguration.updateRate = 60;
        AppState.INSTANCE.currentConfiguration.particleDuration = 3;
        AppState.INSTANCE.currentConfiguration.lineWidth = 1;
        setParticleCount(100);
    }


    public void setDxdt(String dxdt) throws FormatException {
        try {
            ExpressionTree newTree = new ExpressionTree();
            newTree.parse(dxdt, validSymbols);
            dxdtTree = newTree;
            AppState.INSTANCE.currentConfiguration.dxdt = dxdt;
        } catch (InvalidParameterException e) {
            throw new FormatException(e.getMessage() + "\r\nValidSymbols: " + VALID_SYMBOLS);
        } catch (Exception e) {
            throw new FormatException();
        }
    }


    public void setDydt(String dydt) throws FormatException {
        try {
            ExpressionTree newTree = new ExpressionTree();
            newTree.parse(dydt, validSymbols);
            dydtTree = newTree;
            AppState.INSTANCE.currentConfiguration.dydt = dydt;
        } catch (InvalidParameterException e) {
            throw new FormatException(e.getMessage() + "\r\nValidSymbols: " + VALID_SYMBOLS);
        } catch (Exception e) {
            throw new FormatException();
        }
    }

    public void setDzdt(String dzdt) throws FormatException {
        try {
            ExpressionTree newTree = new ExpressionTree();
            newTree.parse(dzdt, validSymbols);
            dzdtTree = newTree;
            AppState.INSTANCE.currentConfiguration.dzdt = dzdt;
        } catch (InvalidParameterException e) {
            throw new FormatException(e.getMessage() + "\r\nValidSymbols: " + VALID_SYMBOLS);
        } catch (Exception e) {
            throw new FormatException();
        }
    }

    public void setParticleCount(int count) {
        boolean simulationWasRunning = isSimulationRunning();
        pauseSimulation();

        particleLock.lock();
        try {
            AppState.INSTANCE.currentConfiguration.particleCount = count;
            initializeParticles();
        } finally {
            particleLock.unlock();
        }

        if (simulationWasRunning)
            resumeSimulation();
    }

    public void resetParticles() {
        resetParticlesFlag = true;
    }

    public void setSimulationMode(int simulationMode) {
        AppState.INSTANCE.currentConfiguration.simulationMode = simulationMode;
        resetElapsedTimeS();
    }

    public void setLineWidth(double lineWidth) {
        AppState.INSTANCE.currentConfiguration.lineWidth = clampLineWidth(lineWidth);
    }

    public void setLineWidthRange(double min, double max) {
        minLineWidth = min;
        maxLineWidth = max;


        AppState.INSTANCE.currentConfiguration.lineWidth =
                clampLineWidth(AppState.INSTANCE.currentConfiguration.lineWidth);
    }

    public double getLineWidthMin() {
        return minLineWidth;
    }

    public double getLineWidthMax() {
        return maxLineWidth;
    }

    private double clampLineWidth(double lineWidth) {
        if (lineWidth < minLineWidth)
            return minLineWidth;
        return Math.min(lineWidth, maxLineWidth);
    }

    public void resumeSimulation() {
        pauseResumeLock.lock();
        try {
            if (isSimulationRunning())
                return;
            try {
                setDxdt(AppState.INSTANCE.currentConfiguration.dxdt);
                setDydt(AppState.INSTANCE.currentConfiguration.dydt);
                setDzdt(AppState.INSTANCE.currentConfiguration.dzdt);
            } catch (FormatException e) {
                try {
                    setDxdt("");
                    setDydt("");
                    setDzdt("");
                } catch (FormatException e1) {
                    //Do nothing.
                }
            }
            realElapsedTimeS = 0;
            particleRepositionQuota = 0;

            for (Particle particle : particles) {
                particle.lastTrailPointUpdateTimeS = 0;
                particle.forceFullTrailUpdate();
            }

            simulationThread = new Thread(this::executeFrame);
            simulationThread.start();
            simulationRunning = true;
        } finally {
            pauseResumeLock.unlock();
        }
    }

    public void pauseSimulation() {
        pauseResumeLock.lock();
        try {
            if (simulationThread == null)
                return;

            simulationThread.interrupt();
            try {
                simulationThread.join(1000);
            } catch (InterruptedException e) {
                // Do nothing.
            }
            simulationThread = null;
        } finally {
            pauseResumeLock.unlock();
        }
    }

    private void executeFrame() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                long frameStart = System.nanoTime();
                double dt = 1.0 / AppState.INSTANCE.currentConfiguration.updateRate;

                update(dt);

                long sleepTime =
                        (long) ((1000000000L * dt - (System.nanoTime() - frameStart)) / 1000000L);
                if (sleepTime > 0)
                    //noinspection BusyWait
                    Thread.sleep(sleepTime);

//                while (System.nanoTime() - frameStart < 1000000000L * dt) { }
            }
        } catch (InterruptedException e) {
            // Do nothing.
        }
        simulationRunning = false;
    }

    public void update(double dt) {
        double realDtS = dt;
        realElapsedTimeS += realDtS;

        dt *= AppState.INSTANCE.currentConfiguration.timeScale;
        elapsedTimeS += dt;

        for (Particle particle : particles) {
            if (resetParticlesFlag)
                repositionParticle(particle, realElapsedTimeS);

            particle.lifetimeS += realDtS;
            double originalX = particle.X;
            double originalY = particle.Y;
            double originalZ = particle.Z;
            double originalVX = particle.vX;
            double originalVY = particle.vY;
            double originalVZ = particle.vZ;
            double newX = originalX;
            double newY = originalY;
            double newZ = originalZ;
            double newVX = originalVX;
            double newVY = originalVY;
            double newVZ = originalVZ;
            if (AppState.INSTANCE.currentConfiguration.simulationMode == Settings.SIMULATION_MODE_EXPRESSION) {
                if (AppState.INSTANCE.currentConfiguration.simulationOrder == Settings.SIMULATION_ORDER_1ST) {
                    variables.get(0).value = originalX;
                    variables.get(1).value = originalY;
                    variables.get(2).value = originalZ;
                    variables.get(6).value = elapsedTimeS;

                    newX = originalX + dxdtTree.evaluate(variables) * dt;
                    newY = originalY + dydtTree.evaluate(variables) * dt;
                    if (AppState.INSTANCE.currentConfiguration.simulationDimensions == Settings.SIMULATION_DIMENSIONS_3D)
                        newZ = originalZ + dzdtTree.evaluate(variables) * dt;
                } else if (AppState.INSTANCE.currentConfiguration.simulationOrder == Settings.SIMULATION_ORDER_2ND) {
                    variables.get(0).value = originalX;
                    variables.get(1).value = originalY;
                    variables.get(2).value = originalZ;
                    variables.get(3).value = originalVX;
                    variables.get(4).value = originalVY;
                    variables.get(5).value = originalVZ;
                    variables.get(6).value = elapsedTimeS;

                    newVX = originalVX + dxdtTree.evaluate(variables) * dt;
                    newX = originalX + newVX * dt;
                    newVY = originalVY + dydtTree.evaluate(variables) * dt;
                    newY = originalY + newVY * dt;
                    if (AppState.INSTANCE.currentConfiguration.simulationDimensions == Settings.SIMULATION_DIMENSIONS_3D) {
                        newVZ = originalVZ + dzdtTree.evaluate(variables) * dt;
                        newZ = originalZ + newVZ * dt;
                    }
                }
            } else if (AppState.INSTANCE.currentConfiguration.simulationMode == Settings.SIMULATION_MODE_MATRIX) {
                if (AppState.INSTANCE.currentConfiguration.simulationOrder == Settings.SIMULATION_ORDER_1ST) {
                    newX = originalX + (originalX * AppState.INSTANCE.currentConfiguration.m[0][0] + originalY * AppState.INSTANCE.currentConfiguration.m[0][1] + originalZ *
                            AppState.INSTANCE.currentConfiguration.m[0][2]) * dt;
                    newY = originalY + (originalX * AppState.INSTANCE.currentConfiguration.m[1][0] + originalY * AppState.INSTANCE.currentConfiguration.m[1][1] + originalZ *
                            AppState.INSTANCE.currentConfiguration.m[1][2]) * dt;
                    if (AppState.INSTANCE.currentConfiguration.simulationDimensions == Settings.SIMULATION_DIMENSIONS_3D)
                        newZ = originalZ + (originalX * AppState.INSTANCE.currentConfiguration.m[2][0] + originalY * AppState.INSTANCE.currentConfiguration.m[2][1] + originalZ
                                * AppState.INSTANCE.currentConfiguration.m[2][2]) * dt;
                } else if (AppState.INSTANCE.currentConfiguration.simulationOrder == Settings.SIMULATION_ORDER_2ND) {
                    double AX =
                            originalX * AppState.INSTANCE.currentConfiguration.m[0][0] + originalY * AppState.INSTANCE.currentConfiguration.m[0][1] + originalZ * AppState
                            .INSTANCE.currentConfiguration.m[0][2] + originalVX * AppState.INSTANCE.currentConfiguration.m[0][3]
                            + originalVY * AppState.INSTANCE.currentConfiguration.m[0][4] + originalVZ * AppState.INSTANCE.currentConfiguration.m[0][5];
                    newVX = originalVX + AX * dt;
                    newX = originalX + newVX * dt;
                    double AY =
                            originalX * AppState.INSTANCE.currentConfiguration.m[1][0] + originalY * AppState.INSTANCE.currentConfiguration.m[1][1] + originalZ * AppState
                            .INSTANCE.currentConfiguration.m[1][2] + originalVX * AppState.INSTANCE.currentConfiguration.m[1][3]
                            + originalVY * AppState.INSTANCE.currentConfiguration.m[1][4] + originalVZ * AppState.INSTANCE.currentConfiguration.m[1][5];
                    newVY = originalVY + AY * dt;
                    newY = originalY + newVY * dt;
                    if (AppState.INSTANCE.currentConfiguration.simulationDimensions == Settings.SIMULATION_DIMENSIONS_3D) {
                        double AZ =
                                originalX * AppState.INSTANCE.currentConfiguration.m[2][0] + originalY * AppState.INSTANCE.currentConfiguration.m[2][1] + originalZ *
                                AppState.INSTANCE.currentConfiguration.m[2][2] + originalVX
                                * AppState.INSTANCE.currentConfiguration.m[2][3] + originalVY * AppState.INSTANCE.currentConfiguration.m[2][4] + originalVZ * AppState.INSTANCE
                                .currentConfiguration.m[2][5];
                        newVZ = originalVZ + AZ * dt;
                        newZ = originalZ + newVZ * dt;
                    }
                }
            }
            if (Math.abs(realElapsedTimeS - particle.lastTrailPointUpdateTimeS) > (1.0 / (double) Particle.TRAIL_SIZE)) {
                particle.updateTrail(realElapsedTimeS);
            }
            particle.setPosition(newX, newY, newZ, newVX, newVY, newVZ);
        }

        particleRepositionQuota += realDtS * AppState.INSTANCE.currentConfiguration.particleCount / AppState.INSTANCE.currentConfiguration.particleDuration;
        while (particleRepositionQuota >= 1) {
            Particle oldestParticle = particles[0];
            for (Particle particle : particles) {
                if (particle.lifetimeS > oldestParticle.lifetimeS)
                    oldestParticle = particle;
            }

            oldestParticle.lifetimeS = 0;
            repositionParticle(oldestParticle, realElapsedTimeS);

            particleRepositionQuota--;
        }

        if (resetParticlesFlag)
            resetParticlesFlag = false;
    }

    private void repositionParticle(Particle particle, double currentTimeS) {
        double x, y, z;
        double vx, vy, vz;
        x = random.nextDouble() * xBoundaryHalfWidth * 2 - xBoundaryHalfWidth;
        y = random.nextDouble() * yBoundaryHalfWidth * 2 - yBoundaryHalfWidth;
        if (AppState.INSTANCE.currentConfiguration.simulationDimensions == Settings.SIMULATION_DIMENSIONS_3D)
            z = random.nextDouble() * zBoundaryHalfWidth * 2 - zBoundaryHalfWidth;
        else z = 0;

        if (AppState.INSTANCE.currentConfiguration.randomizeVelocities && AppState.INSTANCE.currentConfiguration.simulationOrder == Settings.SIMULATION_ORDER_2ND) {
            vx = random.nextDouble() * xBoundaryHalfWidth * 2 - xBoundaryHalfWidth;
            vy = random.nextDouble() * yBoundaryHalfWidth * 2 - yBoundaryHalfWidth;
            if (AppState.INSTANCE.currentConfiguration.simulationDimensions == Settings.SIMULATION_DIMENSIONS_3D)
                vz = random.nextDouble() * zBoundaryHalfWidth * 2 - zBoundaryHalfWidth;
            else vz = 0;
        } else {
            vx = 0;
            vy = 0;
            vz = 0;
        }
        particle.reset(x, y, z, vx, vy, vz, currentTimeS);
    }

    public Particle[] getParticles() {
        return particles;
    }

    public void setBoundaries(double xBoundaryHalfWidth, double yBoundaryHalfWidth,
                              double zBoundaryHalfWidth) {
        this.xBoundaryHalfWidth = xBoundaryHalfWidth;
        this.yBoundaryHalfWidth = yBoundaryHalfWidth;
        this.zBoundaryHalfWidth = zBoundaryHalfWidth;
    }

    public void resetElapsedTimeS() {
        elapsedTimeS = 0;
    }

    public double getElapsedTimeS() {
        return elapsedTimeS;
    }

    public boolean isSimulationRunning() {
        return simulationRunning;
    }
}
