package simplicial.software.models;

import java.io.Serializable;
import java.util.Arrays;

public class Settings implements Serializable {
    public final static int SIMULATION_MODE_MATRIX = 0;
    public final static int SIMULATION_MODE_EXPRESSION = 1;
    public final static int SIMULATION_DIMENSIONS_2D = 0;
    public final static int SIMULATION_DIMENSIONS_3D = 1;
    public final static int SIMULATION_ORDER_1ST = 0;
    public final static int SIMULATION_ORDER_2ND = 1;
    private static final long serialVersionUID = 3;
    public final double[][] m = new double[3][6];
    public String name = "";
    public boolean randomizeVelocities;
    public int simulationMode = SIMULATION_MODE_MATRIX;
    public int simulationDimensions = SIMULATION_DIMENSIONS_2D;
    public int simulationOrder = SIMULATION_ORDER_1ST;
    public String dxdt;
    public String dydt;
    public String dzdt;
    public double timeScale = 1;
    public int particleCount = 0;
    public int updateRate = 1;
    public double particleDuration = 3;
    public double lineWidth = 1;
    public ColorScheme colorScheme = ColorScheme.YELLOW_ORANGE;
    public SystemMode systemMode = SystemMode.Custom;

    public Settings() {
        for (int r = 0; r < m.length; r++) {
            Arrays.fill(m[r], 0);
        }
        m[0][3] = -1;
        m[1][2] = 1;
        m[2][4] = -0.5;

        dxdt = "y";
        dydt = "-x";
        dzdt = "-.5z";
    }
}