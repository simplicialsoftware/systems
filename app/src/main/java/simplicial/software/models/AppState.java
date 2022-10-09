package simplicial.software.models;

import android.content.Context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import simplicial.software.utilities.reflection.SerializationHelper;

public class AppState implements Serializable {
    private static final long serialVersionUID = 24;
    private static final String APPSTATE_FILE_NAME = "APP_STATE";

    public static transient AppState INSTANCE;
    public final Map<String, Settings> configurations = new HashMap<>();
    public boolean firstRun = true;
    public Settings currentConfiguration = new Settings();

    public AppState() {
        currentConfiguration.simulationMode = Settings.SIMULATION_MODE_MATRIX;
        currentConfiguration.simulationDimensions = Settings.SIMULATION_DIMENSIONS_2D;
        currentConfiguration.simulationOrder = Settings.SIMULATION_ORDER_1ST;
        currentConfiguration.m[0][0] = -1;
        currentConfiguration.m[0][1] = -1;
        currentConfiguration.m[1][0] = 1;
        currentConfiguration.m[1][1] = -1;
    }

    public static void save(Context context) {
        SerializationHelper.storeObjectInFile(context, INSTANCE, APPSTATE_FILE_NAME,
                Context.MODE_PRIVATE);
    }

    public static void load(Context context) {
        INSTANCE = (AppState) SerializationHelper.loadObjectFromfile(context, APPSTATE_FILE_NAME);
        if (INSTANCE == null)
            INSTANCE = new AppState();
    }
}
