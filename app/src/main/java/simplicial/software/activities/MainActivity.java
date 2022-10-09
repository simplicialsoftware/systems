package simplicial.software.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.nfc.FormatException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import java.util.Arrays;

import simplicial.software.Systems.R;
import simplicial.software.fragments.SettingsFragment;
import simplicial.software.models.AppState;
import simplicial.software.models.Settings;
import simplicial.software.models.Simulator;
import simplicial.software.models.SystemMode;
import simplicial.software.utilities.android.ObjectCloner;
import simplicial.software.utilities.string.NameGenerator;

public class MainActivity extends FragmentActivity {
    private final String[] mainMenuItems = new String[]{"Custom", "Logistic Population (1D)",
            "Periodic Harvesting (1D)", "Saddle (2D)", "Source (2D)",
            "Sink (2D)", "Center (2D)", "Spiral Source (2D)", "Spiral Sink (2D)", "Bifurcations " +
            "(2D)", "Homoclinic Orbit" +
            " (2D)", "Spiral Saddle (3D)", "Spiral Sink (3D)", "Lorenz (3D)", "Oscillations (3D)"};
    SettingsFragment settingsFragment;
    boolean isSettingsVisible;
    private DrawerLayout drawerLayout;
    private ListView lvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private MenuItem miSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppState.load(this);

        setContentView(R.layout.main);

        settingsFragment =
                (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_settings);
        drawerLayout = findViewById(R.id.dl_drawer);
        lvDrawer = findViewById(R.id.lv_drawer);

        // set a custom shadow that overlays the main content when the drawer
        // opens
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        lvDrawer.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, mainMenuItems));
        lvDrawer.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        drawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                drawerLayout, /* DrawerLayout object */
                R.string.example_systems, /* nav drawer image to replace 'Up' caret */
                R.string.example_systems) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);

        if (AppState.INSTANCE.firstRun) {
            Simulator.INSTANCE.setDefaultSettings();
            AppState.INSTANCE.firstRun = false;
            AppState.save(this);
        }
        Simulator.INSTANCE.initializeParticles();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        drawerLayout.removeDrawerListener(drawerToggle);
    }

    @Override
    protected void onResume() {
        settingsFragment.setControls();
        changeSettingsVisible(true);
        selectMode(AppState.INSTANCE.currentConfiguration.systemMode.ordinal());
        Simulator.INSTANCE.initializeParticles();
        Simulator.INSTANCE.resumeSimulation();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Simulator.INSTANCE.pauseSimulation();
        AppState.save(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        miSettings = menu.findItem(R.id.item_settings);
        miSettings.setIcon(isSettingsVisible ? R.drawable.ic_menu_preferences_enabled :
                R.drawable.ic_menu_preferences);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(lvDrawer) || drawerLayout.isDrawerVisible(lvDrawer))
            drawerLayout.closeDrawer(lvDrawer);
        else if (isSettingsVisible)
            changeSettingsVisible(false);
        else
            super.onBackPressed();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_singlechoice);

        // Handle item selection
        int itemId = item.getItemId();
        if (itemId == R.id.item_info) {
            Simulator.INSTANCE.pauseSimulation();
            startActivity(new Intent(this, HelpActivity.class));
            return true;
        } else if (itemId == R.id.item_settings) {
            changeSettingsVisible(!isSettingsVisible);
            return true;
        } else if (itemId == R.id.item_save) {
            final EditText etSave = new EditText(this);
            etSave.setSelectAllOnFocus(true);
            etSave.setFocusableInTouchMode(true);
            String name = NameGenerator.generateUniqueName("Configuration",
                    AppState.INSTANCE.configurations.keySet());
            if (AppState.INSTANCE.currentConfiguration.name.length() > 0)
                name = AppState.INSTANCE.currentConfiguration.name;
            etSave.setText(name);
            AlertDialog dialog =
                    new AlertDialog.Builder(this).setTitle(R.string.save_configuration).setView(etSave)
                            .setPositiveButton("Ok", (dialog1, whichButton) -> {
                                String text = etSave.getText().toString();

                                AppState.INSTANCE.currentConfiguration.name = text;
                                try {
                                    AppState.INSTANCE.configurations.put(text,
                                            (Settings) ObjectCloner.deepCopy(AppState.INSTANCE.currentConfiguration));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                AppState.save(MainActivity.this);
                            }).setNegativeButton("Cancel", null).create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();
            return true;
        } else if (itemId == R.id.item_load) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
            builderSingle.setTitle(R.string.load_configuration);
            arrayAdapter.addAll(AppState.INSTANCE.configurations.keySet());
            builderSingle.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

            builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
                String selectedString = arrayAdapter.getItem(which);
                Simulator.INSTANCE.pauseSimulation();
                try {
                    AppState.INSTANCE.currentConfiguration =
                            (Settings) ObjectCloner.deepCopy(AppState.INSTANCE.configurations.get(selectedString));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Simulator.INSTANCE.setDxdt(AppState.INSTANCE.currentConfiguration.dxdt);
                    Simulator.INSTANCE.setDydt(AppState.INSTANCE.currentConfiguration.dydt);
                    Simulator.INSTANCE.setDzdt(AppState.INSTANCE.currentConfiguration.dzdt);
                } catch (FormatException e) {
                    e.printStackTrace();
                }
                Simulator.INSTANCE.setLineWidth(AppState.INSTANCE.currentConfiguration.lineWidth);
                Simulator.INSTANCE.resetElapsedTimeS();
                Simulator.INSTANCE.setParticleCount(AppState.INSTANCE.currentConfiguration.particleCount);

                settingsFragment.setControls();
                selectMode(AppState.INSTANCE.currentConfiguration.systemMode.ordinal());

                Simulator.INSTANCE.resumeSimulation();
            });
            builderSingle.show();
            return true;
        } else if (itemId == R.id.item_delete) {
            AlertDialog.Builder builderSingle;
            builderSingle = new AlertDialog.Builder(this);
            builderSingle.setTitle(R.string.delete_configuration);
            arrayAdapter.addAll(AppState.INSTANCE.configurations.keySet());
            builderSingle.setNegativeButton(getText(R.string.cancel),
                    (dialog, which) -> dialog.dismiss());

            builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
                String selectedString = arrayAdapter.getItem(which);
                AppState.INSTANCE.configurations.remove(selectedString);

            });
            builderSingle.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeSettingsVisible(boolean visible) {
        if (miSettings != null)
            miSettings.setIcon(visible ? R.drawable.ic_menu_preferences_enabled :
                    R.drawable.ic_menu_preferences);

        if (visible) {
            settingsFragment.requireView().setVisibility(View.VISIBLE);
        } else {
            if (getCurrentFocus() != null) {
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            settingsFragment.requireView().setVisibility(View.GONE);
        }
        isSettingsVisible = visible;
    }

    public void selectMode(int position) {
        lvDrawer.setItemChecked(position, true);//lets mode be changed visually from code
        setTitle(mainMenuItems[position]);
        drawerLayout.closeDrawer(lvDrawer);

        if (position == SystemMode.Custom.ordinal())
            setTitle(R.string.app_name);

        if (AppState.INSTANCE.currentConfiguration.systemMode == SystemMode.values()[position])
            return;

        AppState.INSTANCE.currentConfiguration.systemMode = SystemMode.values()[position];
        if (AppState.INSTANCE.currentConfiguration.systemMode == SystemMode.Custom)
            return;

        reloadConfiguration();
    }

    private void reloadConfiguration() {
        boolean wasRunning = Simulator.INSTANCE.isSimulationRunning();
        Simulator.INSTANCE.pauseSimulation();

        for (int r = 0; r < AppState.INSTANCE.currentConfiguration.m.length; r++) {
            Arrays.fill(AppState.INSTANCE.currentConfiguration.m[r], 0);
        }

        switch (AppState.INSTANCE.currentConfiguration.systemMode) {
            case Logistic_Population_1D:
                AppState.INSTANCE.currentConfiguration.simulationMode =
                        Settings.SIMULATION_MODE_EXPRESSION;
                AppState.INSTANCE.currentConfiguration.simulationDimensions =
                        Settings.SIMULATION_DIMENSIONS_2D;
                AppState.INSTANCE.currentConfiguration.simulationOrder =
                        Settings.SIMULATION_ORDER_1ST;
                try {
                    Simulator.INSTANCE.setDxdt("1");
                    Simulator.INSTANCE.setDydt("y*(1-y/.5)");
                } catch (FormatException e) {
                    e.printStackTrace();
                }
                break;
            case Periodic_Harvesting_1D:
                AppState.INSTANCE.currentConfiguration.simulationMode =
                        Settings.SIMULATION_MODE_EXPRESSION;
                AppState.INSTANCE.currentConfiguration.simulationDimensions =
                        Settings.SIMULATION_DIMENSIONS_2D;
                AppState.INSTANCE.currentConfiguration.simulationOrder =
                        Settings.SIMULATION_ORDER_1ST;
                try {
                    Simulator.INSTANCE.setDxdt("1");
                    Simulator.INSTANCE.setDydt(".5*(5*y*(1-y)-.8*(1+sin(2*3.14*t)))");
                } catch (FormatException e) {
                    e.printStackTrace();
                }
                break;
            case Saddle_2D:
                AppState.INSTANCE.currentConfiguration.simulationMode =
                        Settings.SIMULATION_MODE_MATRIX;
                AppState.INSTANCE.currentConfiguration.simulationDimensions =
                        Settings.SIMULATION_DIMENSIONS_2D;
                AppState.INSTANCE.currentConfiguration.simulationOrder =
                        Settings.SIMULATION_ORDER_1ST;
                AppState.INSTANCE.currentConfiguration.m[0][0] = -1;
                AppState.INSTANCE.currentConfiguration.m[0][1] = 0;
                AppState.INSTANCE.currentConfiguration.m[1][0] = 0;
                AppState.INSTANCE.currentConfiguration.m[1][1] = 1;
                break;
            case Source_2D:
                AppState.INSTANCE.currentConfiguration.simulationMode =
                        Settings.SIMULATION_MODE_MATRIX;
                AppState.INSTANCE.currentConfiguration.simulationDimensions =
                        Settings.SIMULATION_DIMENSIONS_2D;
                AppState.INSTANCE.currentConfiguration.simulationOrder =
                        Settings.SIMULATION_ORDER_1ST;
                AppState.INSTANCE.currentConfiguration.m[0][0] = 1;
                AppState.INSTANCE.currentConfiguration.m[0][1] = 0;
                AppState.INSTANCE.currentConfiguration.m[1][0] = 0;
                AppState.INSTANCE.currentConfiguration.m[1][1] = 1;
                break;
            case Sink_2D:
                AppState.INSTANCE.currentConfiguration.simulationMode =
                        Settings.SIMULATION_MODE_MATRIX;
                AppState.INSTANCE.currentConfiguration.simulationDimensions =
                        Settings.SIMULATION_DIMENSIONS_2D;
                AppState.INSTANCE.currentConfiguration.simulationOrder =
                        Settings.SIMULATION_ORDER_1ST;
                AppState.INSTANCE.currentConfiguration.m[0][0] = -1;
                AppState.INSTANCE.currentConfiguration.m[0][1] = 0;
                AppState.INSTANCE.currentConfiguration.m[1][0] = 0;
                AppState.INSTANCE.currentConfiguration.m[1][1] = -1;
                break;
            case Center_2D:
                AppState.INSTANCE.currentConfiguration.simulationMode =
                        Settings.SIMULATION_MODE_MATRIX;
                AppState.INSTANCE.currentConfiguration.simulationDimensions =
                        Settings.SIMULATION_DIMENSIONS_2D;
                AppState.INSTANCE.currentConfiguration.simulationOrder =
                        Settings.SIMULATION_ORDER_1ST;
                AppState.INSTANCE.currentConfiguration.m[0][0] = 0;
                AppState.INSTANCE.currentConfiguration.m[0][1] = 1;
                AppState.INSTANCE.currentConfiguration.m[1][0] = -1;
                AppState.INSTANCE.currentConfiguration.m[1][1] = 0;
                break;
            case Spiral_Source_2D:
                AppState.INSTANCE.currentConfiguration.simulationMode =
                        Settings.SIMULATION_MODE_MATRIX;
                AppState.INSTANCE.currentConfiguration.simulationDimensions =
                        Settings.SIMULATION_DIMENSIONS_2D;
                AppState.INSTANCE.currentConfiguration.simulationOrder =
                        Settings.SIMULATION_ORDER_1ST;
                AppState.INSTANCE.currentConfiguration.m[0][0] = 1;
                AppState.INSTANCE.currentConfiguration.m[0][1] = 1;
                AppState.INSTANCE.currentConfiguration.m[1][0] = -1;
                AppState.INSTANCE.currentConfiguration.m[1][1] = 1;
                break;
            case Spiral_Sink_2D:
                AppState.INSTANCE.currentConfiguration.simulationMode =
                        Settings.SIMULATION_MODE_MATRIX;
                AppState.INSTANCE.currentConfiguration.simulationDimensions =
                        Settings.SIMULATION_DIMENSIONS_2D;
                AppState.INSTANCE.currentConfiguration.simulationOrder =
                        Settings.SIMULATION_ORDER_1ST;
                AppState.INSTANCE.currentConfiguration.m[0][0] = -1;
                AppState.INSTANCE.currentConfiguration.m[0][1] = -1;
                AppState.INSTANCE.currentConfiguration.m[1][0] = 1;
                AppState.INSTANCE.currentConfiguration.m[1][1] = -1;
                break;
            case Bifurcations_2D:
                AppState.INSTANCE.currentConfiguration.simulationMode =
                        Settings.SIMULATION_MODE_EXPRESSION;
                AppState.INSTANCE.currentConfiguration.simulationDimensions =
                        Settings.SIMULATION_DIMENSIONS_2D;
                AppState.INSTANCE.currentConfiguration.simulationOrder =
                        Settings.SIMULATION_ORDER_1ST;
                try {
                    Simulator.INSTANCE.setDxdt("1-x^2");
                    Simulator.INSTANCE.setDydt("-y");
                } catch (FormatException e) {
                    e.printStackTrace();
                }
                break;
            case Homoclinic_Orbit_2D:
                AppState.INSTANCE.currentConfiguration.simulationMode =
                        Settings.SIMULATION_MODE_EXPRESSION;
                AppState.INSTANCE.currentConfiguration.simulationDimensions =
                        Settings.SIMULATION_DIMENSIONS_2D;
                AppState.INSTANCE.currentConfiguration.simulationOrder =
                        Settings.SIMULATION_ORDER_1ST;
                try {
                    Simulator.INSTANCE.setDxdt("-y-(x^4/4-x^2/2+y^2/2)*(x^3-x)");
                    Simulator.INSTANCE.setDydt("x^3-x-(x^4/4-x^2/2+y^2/2)*y");
                } catch (FormatException e) {
                    e.printStackTrace();
                }
                break;
            case Spiral_Saddle_3D:
                AppState.INSTANCE.currentConfiguration.simulationMode =
                        Settings.SIMULATION_MODE_MATRIX;
                AppState.INSTANCE.currentConfiguration.simulationDimensions =
                        Settings.SIMULATION_DIMENSIONS_3D;
                AppState.INSTANCE.currentConfiguration.simulationOrder =
                        Settings.SIMULATION_ORDER_1ST;
                AppState.INSTANCE.currentConfiguration.m[0][0] = -1;
                AppState.INSTANCE.currentConfiguration.m[0][1] = 1;
                AppState.INSTANCE.currentConfiguration.m[0][2] = 0;
                AppState.INSTANCE.currentConfiguration.m[1][0] = -1;
                AppState.INSTANCE.currentConfiguration.m[1][1] = -1;
                AppState.INSTANCE.currentConfiguration.m[1][2] = 0;
                AppState.INSTANCE.currentConfiguration.m[2][0] = 0;
                AppState.INSTANCE.currentConfiguration.m[2][1] = 0;
                AppState.INSTANCE.currentConfiguration.m[2][2] = 1;
                break;
            case Spiral_Sink_3D:
                AppState.INSTANCE.currentConfiguration.simulationMode =
                        Settings.SIMULATION_MODE_MATRIX;
                AppState.INSTANCE.currentConfiguration.simulationDimensions =
                        Settings.SIMULATION_DIMENSIONS_3D;
                AppState.INSTANCE.currentConfiguration.simulationOrder =
                        Settings.SIMULATION_ORDER_1ST;
                AppState.INSTANCE.currentConfiguration.m[0][0] = 0;
                AppState.INSTANCE.currentConfiguration.m[0][1] = 1;
                AppState.INSTANCE.currentConfiguration.m[0][2] = 0;
                AppState.INSTANCE.currentConfiguration.m[1][0] = -1;
                AppState.INSTANCE.currentConfiguration.m[1][1] = 0;
                AppState.INSTANCE.currentConfiguration.m[1][2] = 0;
                AppState.INSTANCE.currentConfiguration.m[2][0] = 0;
                AppState.INSTANCE.currentConfiguration.m[2][1] = 0;
                AppState.INSTANCE.currentConfiguration.m[2][2] = -1;
                break;
            case Lorenz_3D:
                AppState.INSTANCE.currentConfiguration.simulationMode =
                        Settings.SIMULATION_MODE_EXPRESSION;
                AppState.INSTANCE.currentConfiguration.simulationDimensions =
                        Settings.SIMULATION_DIMENSIONS_3D;
                AppState.INSTANCE.currentConfiguration.simulationOrder =
                        Settings.SIMULATION_ORDER_1ST;
                try {
                    Simulator.INSTANCE.setDxdt("10*(y-x)");
                    Simulator.INSTANCE.setDydt("(x*(28-z)-y)");
                    Simulator.INSTANCE.setDzdt("(x*y-8/3*z)");
                } catch (FormatException e) {
                    e.printStackTrace();
                }
                break;
            case Oscillations_3D:
                AppState.INSTANCE.currentConfiguration.simulationMode =
                        Settings.SIMULATION_MODE_MATRIX;
                AppState.INSTANCE.currentConfiguration.simulationDimensions =
                        Settings.SIMULATION_DIMENSIONS_3D;
                AppState.INSTANCE.currentConfiguration.simulationOrder =
                        Settings.SIMULATION_ORDER_2ND;
                for (int r = 0; r < AppState.INSTANCE.currentConfiguration.m.length; r++) {
                    Arrays.fill(AppState.INSTANCE.currentConfiguration.m[r], 0);
                }
                AppState.INSTANCE.currentConfiguration.m[0][0] = -1;
                AppState.INSTANCE.currentConfiguration.m[1][1] = -1;
                AppState.INSTANCE.currentConfiguration.m[2][2] = -1;
                break;
        }
        settingsFragment.setControls();
        if (wasRunning)
            Simulator.INSTANCE.resumeSimulation();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectMode(position);
            reloadConfiguration();
        }
    }
}