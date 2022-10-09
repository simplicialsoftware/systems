package simplicial.software.fragments;

import android.app.Activity;
import android.content.Context;
import android.nfc.FormatException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.RadioGroup.OnCheckedChangeListener;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import simplicial.software.Systems.R;
import simplicial.software.activities.MainActivity;
import simplicial.software.models.*;
import simplicial.software.utilities.android.DialogHelper;

public class SettingsFragment extends Fragment implements OnCheckedChangeListener,
        CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener,
        View.OnClickListener, TextWatcher, View.OnLongClickListener {
    private final Random random = new Random(System.currentTimeMillis());
    private final EditText[][] etM = new EditText[3][6];
    private LinearLayout llFocusClear;
    private RadioGroup rgMode;
    private RadioGroup rgDimensions;
    private RadioGroup rgOrder;
    private Spinner sColor;
    private Button bDefaults;
    private Button bRandomize;
    private Button bResetTime;
    private Button bResetParticles;
    private View llMatrix;
    private View llExpression;
    private View llExpressionZ;
    private View llMatrixZ;
    private View llMatrix2ndOrderX;
    private View llMatrix2ndOrderY;
    private View llMatrix2ndOrderZ;
    private TextView tv02, tv05;
    private TextView tv12, tv15;
    private TextView tv22, tv25;
    private TextView tv02B, tv05B;
    private TextView tv12B, tv15B;
    private TextView tv22B, tv25B;
    private TextView tv05C;
    private TextView tv15C;
    private TextView tv25C;
    private TextView tvXEquals;
    private TextView tvYEquals;
    private TextView tvZEquals;
    private TextView tvXEqualsExpression;
    private TextView tvYEqualsExpression;
    private TextView tvZEqualsExpression;
    private EditText etDxDt;
    private EditText etDyDt;
    private EditText etDzDt;
    private EditText etTimeScale;
    private EditText etParticles;
    private EditText lineWidth;
    private EditText etUpdateRate;
    private EditText etParticleDuration;
    private CheckBox cbRandomizeVelocities;
    private boolean suppressControlChanges = false;

    public SettingsFragment() {
        super();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.settings_fragment, container, false);
        List<View> descendants = getDescendants(view);
        for (int i = 0; i < descendants.size(); i++) {
            View child = descendants.get(i);
            if (child instanceof Spinner) {
                ((Spinner) child).setOnItemSelectedListener(this);
            }
            if (child instanceof Button) {
                child.setOnClickListener(this);
                child.setOnLongClickListener(this);
            }
            if (child instanceof EditText) {
                ((EditText) child).addTextChangedListener(this);
            }
        }

        llFocusClear = Objects.requireNonNull(view).findViewById(R.id.ll_focus_clear);

        rgMode = view.findViewById(R.id.rgMode);
        rgMode.setOnCheckedChangeListener(this);
        rgDimensions = view.findViewById(R.id.rgDimensions);
        rgDimensions.setOnCheckedChangeListener(this);
        rgOrder = view.findViewById(R.id.rgOrder);
        rgOrder.setOnCheckedChangeListener(this);

        cbRandomizeVelocities = view.findViewById(R.id.cbRandomizeVelocities);
        cbRandomizeVelocities.setOnCheckedChangeListener(this);

        sColor = view.findViewById(R.id.sColor);

        bDefaults = view.findViewById(R.id.buttonDefaults);
        bRandomize = view.findViewById(R.id.bRandomize);
        bResetTime = view.findViewById(R.id.bResetTime);
        bResetParticles = view.findViewById(R.id.bResetParticles);

        llMatrix = view.findViewById(R.id.ll_matrix);
        llExpression = view.findViewById(R.id.ll_expression);

        llMatrixZ = view.findViewById(R.id.ll_matrix_Z);
        llMatrix2ndOrderX = view.findViewById(R.id.ll_matrix_2nd_order_X);
        llMatrix2ndOrderY = view.findViewById(R.id.ll_matrix_2nd_order_Y);
        llMatrix2ndOrderZ = view.findViewById(R.id.ll_matrix_2nd_order_Z);
        llExpressionZ = view.findViewById(R.id.ll_expression_Z);

        tvXEquals = view.findViewById(R.id.tvXEquals);
        tvYEquals = view.findViewById(R.id.tvYEquals);
        tvZEquals = view.findViewById(R.id.tvZEquals);
        tvXEqualsExpression = view.findViewById(R.id.tvXEqualsExpression);
        tvYEqualsExpression = view.findViewById(R.id.tvYEqualsExpression);
        tvZEqualsExpression = view.findViewById(R.id.tvZEqualsExpression);

        tv02 = view.findViewById(R.id.tv02);
        tv02B = view.findViewById(R.id.tv02B);
        tv05 = view.findViewById(R.id.tv05);
        tv05B = view.findViewById(R.id.tv05B);
        tv12 = view.findViewById(R.id.tv12);
        tv12B = view.findViewById(R.id.tv12B);
        tv15 = view.findViewById(R.id.tv15);
        tv15B = view.findViewById(R.id.tv15B);
        tv22 = view.findViewById(R.id.tv22);
        tv22B = view.findViewById(R.id.tv22B);
        tv25 = view.findViewById(R.id.tv25);
        tv25B = view.findViewById(R.id.tv25B);
        tv05C = view.findViewById(R.id.tv05C);
        tv15C = view.findViewById(R.id.tv15C);
        tv25C = view.findViewById(R.id.tv25C);

        etM[0][0] = view.findViewById(R.id.etM00);
        etM[0][1] = view.findViewById(R.id.etM01);
        etM[0][2] = view.findViewById(R.id.etM02);
        etM[0][3] = view.findViewById(R.id.etM03);
        etM[0][4] = view.findViewById(R.id.etM04);
        etM[0][5] = view.findViewById(R.id.etM05);
        etM[1][0] = view.findViewById(R.id.etM10);
        etM[1][1] = view.findViewById(R.id.etM11);
        etM[1][2] = view.findViewById(R.id.etM12);
        etM[1][3] = view.findViewById(R.id.etM13);
        etM[1][4] = view.findViewById(R.id.etM14);
        etM[1][5] = view.findViewById(R.id.etM15);
        etM[2][0] = view.findViewById(R.id.etM20);
        etM[2][1] = view.findViewById(R.id.etM21);
        etM[2][2] = view.findViewById(R.id.etM22);
        etM[2][3] = view.findViewById(R.id.etM23);
        etM[2][4] = view.findViewById(R.id.etM24);
        etM[2][5] = view.findViewById(R.id.etM25);

        etDxDt = view.findViewById(R.id.etDxDt);
        etDyDt = view.findViewById(R.id.etDyDt);
        etDzDt = view.findViewById(R.id.etDzDt);

        etTimeScale = view.findViewById(R.id.etTimeScale);
        etParticles = view.findViewById(R.id.etParticles);
        lineWidth = view.findViewById(R.id.etLineWidth);
        etUpdateRate = view.findViewById(R.id.etUpdateRate);
        etParticleDuration = view.findViewById(R.id.etParticleDuration);

        setControlVisibilities(AppState.INSTANCE.currentConfiguration.simulationMode,
                AppState.INSTANCE.currentConfiguration.simulationDimensions, AppState.INSTANCE
                .currentConfiguration.simulationOrder);
        setControls();

        return view;
    }

    private List<View> getDescendants(ViewGroup viewGroup) {
        List<View> descendants = new ArrayList<>();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                descendants.addAll(getDescendants((ViewGroup) child));
            }
            descendants.add(child);
        }
        return descendants;
    }

    private void setControlVisibilities(int simulationMode, int simulationDimension,
                                        int simulationOrder) {
        llMatrix.setVisibility(simulationMode == Settings.SIMULATION_MODE_MATRIX ? View.VISIBLE :
                View.GONE);
        llExpression.setVisibility(simulationMode == Settings.SIMULATION_MODE_EXPRESSION ?
                View.VISIBLE : View.GONE);
        bResetTime.setVisibility(simulationMode == Settings.SIMULATION_MODE_EXPRESSION ?
                View.VISIBLE : View.GONE);

        llExpressionZ.setVisibility(simulationDimension == Settings.SIMULATION_DIMENSIONS_3D ?
                View.VISIBLE : View.GONE);

        cbRandomizeVelocities.setVisibility(simulationOrder == Settings.SIMULATION_ORDER_2ND ?
                View.VISIBLE : View.GONE);

        etM[0][2].setVisibility(simulationDimension == Settings.SIMULATION_DIMENSIONS_3D ?
                View.VISIBLE : View.GONE);
        etM[1][2].setVisibility(simulationDimension == Settings.SIMULATION_DIMENSIONS_3D ?
                View.VISIBLE : View.GONE);
        etM[2][2].setVisibility(simulationDimension == Settings.SIMULATION_DIMENSIONS_3D ?
                View.VISIBLE : View.GONE);

        tv02.setVisibility(simulationDimension == Settings.SIMULATION_DIMENSIONS_3D ?
                View.VISIBLE : View.GONE);
        tv12.setVisibility(simulationDimension == Settings.SIMULATION_DIMENSIONS_3D ?
                View.VISIBLE : View.GONE);
        tv22.setVisibility(simulationDimension == Settings.SIMULATION_DIMENSIONS_3D ?
                View.VISIBLE : View.GONE);

        tv02B.setVisibility(simulationDimension == Settings.SIMULATION_DIMENSIONS_3D ?
                View.VISIBLE : View.GONE);
        tv12B.setVisibility(simulationDimension == Settings.SIMULATION_DIMENSIONS_3D ?
                View.VISIBLE : View.GONE);
        tv22B.setVisibility(simulationDimension == Settings.SIMULATION_DIMENSIONS_3D ?
                View.VISIBLE : View.GONE);

        int secondOrder3DVisibility =
                simulationOrder == Settings.SIMULATION_ORDER_2ND && simulationDimension == Settings.SIMULATION_DIMENSIONS_3D ? View.VISIBLE
                : View.GONE;
        tv05B.setVisibility(secondOrder3DVisibility);
        tv15B.setVisibility(secondOrder3DVisibility);
        tv25B.setVisibility(secondOrder3DVisibility);

        etM[0][5].setVisibility(secondOrder3DVisibility);
        etM[1][5].setVisibility(secondOrder3DVisibility);
        etM[2][5].setVisibility(secondOrder3DVisibility);

        tv05.setVisibility(secondOrder3DVisibility);
        tv15.setVisibility(secondOrder3DVisibility);
        tv25.setVisibility(secondOrder3DVisibility);

        tv05C.setVisibility(secondOrder3DVisibility);
        tv15C.setVisibility(secondOrder3DVisibility);
        tv25C.setVisibility(secondOrder3DVisibility);

        llMatrixZ.setVisibility(simulationDimension == Settings.SIMULATION_DIMENSIONS_3D ?
                View.VISIBLE : View.GONE);
        llMatrix2ndOrderX.setVisibility(simulationOrder == Settings.SIMULATION_ORDER_2ND ?
                View.VISIBLE : View.GONE);
        llMatrix2ndOrderY.setVisibility(simulationOrder == Settings.SIMULATION_ORDER_2ND ?
                View.VISIBLE : View.GONE);
        llMatrix2ndOrderZ
                .setVisibility(simulationDimension == Settings.SIMULATION_DIMENSIONS_3D && simulationOrder == Settings.SIMULATION_ORDER_2ND ? View.VISIBLE
                        : View.GONE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        if (v == bDefaults) {
            Simulator.INSTANCE.setDefaultSettings();
            clearFocus();
            setControls();
        } else if (v == bRandomize) {
            try {
                double a;
                double N;
                double h;
                double rho;
                double sigma;
                double beta;
                switch (AppState.INSTANCE.currentConfiguration.systemMode) {
                    case Logistic_Population_1D:
                        a = random.nextDouble();
                        N = random.nextDouble();
                        Simulator.INSTANCE.setDxdt("1");
                        Simulator.INSTANCE.setDydt(a + "*y*(1-y/" + N + ")");
                        break;
                    case Periodic_Harvesting_1D:
                        a = random.nextDouble() * 10;
                        h = random.nextDouble();
                        Simulator.INSTANCE.setDxdt("1");
                        Simulator.INSTANCE.setDydt(a + "*y*(1-y)-" + h + "*(1+sin(2*3.14159*t))");
                        break;
                    case Saddle_2D:
                        do {
                            AppState.INSTANCE.currentConfiguration.m[0][0] =
                                    (random.nextDouble() - 0.5) * 2.0;
                            AppState.INSTANCE.currentConfiguration.m[0][1] = 0;
                            AppState.INSTANCE.currentConfiguration.m[1][0] = 0;
                            AppState.INSTANCE.currentConfiguration.m[1][1] =
                                    (random.nextDouble() - 0.5) * 2.0;
                        } while (Math.signum(AppState.INSTANCE.currentConfiguration.m[0][0]) == Math.signum(AppState.INSTANCE.currentConfiguration.m[1][1]));
                        break;
                    case Source_2D:
                        AppState.INSTANCE.currentConfiguration.m[0][0] = random.nextDouble();
                        AppState.INSTANCE.currentConfiguration.m[0][1] = 0;
                        AppState.INSTANCE.currentConfiguration.m[1][0] = 0;
                        AppState.INSTANCE.currentConfiguration.m[1][1] = random.nextDouble();
                        break;
                    case Sink_2D:
                        AppState.INSTANCE.currentConfiguration.m[0][0] = -random.nextDouble();
                        AppState.INSTANCE.currentConfiguration.m[0][1] = 0;
                        AppState.INSTANCE.currentConfiguration.m[1][0] = 0;
                        AppState.INSTANCE.currentConfiguration.m[1][1] = -random.nextDouble();
                        break;
                    case Center_2D:
                        do {
                            AppState.INSTANCE.currentConfiguration.m[0][0] = 0;
                            AppState.INSTANCE.currentConfiguration.m[0][1] =
                                    (random.nextDouble() - 0.5) * 2.0;
                            AppState.INSTANCE.currentConfiguration.m[1][0] =
                                    (random.nextDouble() - 0.5) * 2.0;
                            AppState.INSTANCE.currentConfiguration.m[1][1] = 0;
                        } while (Math.signum(AppState.INSTANCE.currentConfiguration.m[0][1]) == Math.signum(AppState.INSTANCE.currentConfiguration.m[1][0]));
                        break;
                    case Spiral_Source_2D:
                        do {
                            AppState.INSTANCE.currentConfiguration.m[0][0] = random.nextDouble();
                            AppState.INSTANCE.currentConfiguration.m[0][1] =
                                    (random.nextDouble() - 0.5) * 2.0;
                            AppState.INSTANCE.currentConfiguration.m[1][0] =
                                    (random.nextDouble() - 0.5) * 2.0;
                            AppState.INSTANCE.currentConfiguration.m[1][1] = random.nextDouble();
                        } while (Math.signum(AppState.INSTANCE.currentConfiguration.m[0][1]) == Math.signum(AppState.INSTANCE.currentConfiguration.m[1][0]));
                        break;
                    case Spiral_Sink_2D:
                        do {
                            AppState.INSTANCE.currentConfiguration.m[0][0] = -random.nextDouble();
                            AppState.INSTANCE.currentConfiguration.m[0][1] =
                                    (random.nextDouble() - 0.5) * 2.0;
                            AppState.INSTANCE.currentConfiguration.m[1][0] =
                                    (random.nextDouble() - 0.5) * 2.0;
                            AppState.INSTANCE.currentConfiguration.m[1][1] = -random.nextDouble();
                        } while (Math.signum(AppState.INSTANCE.currentConfiguration.m[0][1]) == Math.signum(AppState.INSTANCE.currentConfiguration.m[1][0]));
                        break;
                    case Bifurcations_2D:
                        a = (random.nextDouble() - 0.5) * 2.0;
                        Simulator.INSTANCE.setDxdt(a + "-x^2");
                        Simulator.INSTANCE.setDydt("-y");
                        break;
                    case Homoclinic_Orbit_2D:
                        break;
                    case Spiral_Saddle_3D:
                        do {
                            AppState.INSTANCE.currentConfiguration.m[0][0] =
                                    (random.nextDouble() - 0.5) * 2.0;
                            AppState.INSTANCE.currentConfiguration.m[0][1] =
                                    (random.nextDouble() - 0.5) * 2.0;
                            AppState.INSTANCE.currentConfiguration.m[0][2] = 0;
                            AppState.INSTANCE.currentConfiguration.m[1][0] =
                                    (random.nextDouble() - 0.5) * 2.0;
                            AppState.INSTANCE.currentConfiguration.m[1][1] = 0;
                            AppState.INSTANCE.currentConfiguration.m[1][2] = 0;
                            AppState.INSTANCE.currentConfiguration.m[2][0] = 0;
                            AppState.INSTANCE.currentConfiguration.m[2][1] = 0;
                            AppState.INSTANCE.currentConfiguration.m[2][2] =
                                    (random.nextDouble() - 0.5) * 2.0;
                        }
                        while (Math.signum(AppState.INSTANCE.currentConfiguration.m[0][1]) == Math.signum(AppState.INSTANCE.currentConfiguration.m[1][0]) || Math.signum
                                (AppState.INSTANCE.currentConfiguration.m[0][0]) == Math.signum(AppState.INSTANCE.currentConfiguration.m[2][2]));
                        break;
                    case Spiral_Sink_3D:
                        do {
                            AppState.INSTANCE.currentConfiguration.m[0][0] = 0;
                            AppState.INSTANCE.currentConfiguration.m[0][1] =
                                    (random.nextDouble() - 0.5) * 2.0;
                            AppState.INSTANCE.currentConfiguration.m[0][2] = 0;
                            AppState.INSTANCE.currentConfiguration.m[1][0] =
                                    (random.nextDouble() - 0.5) * 2.0;
                            AppState.INSTANCE.currentConfiguration.m[1][1] = 0;
                            AppState.INSTANCE.currentConfiguration.m[1][2] = 0;
                            AppState.INSTANCE.currentConfiguration.m[2][0] = 0;
                            AppState.INSTANCE.currentConfiguration.m[2][1] = 0;
                            AppState.INSTANCE.currentConfiguration.m[2][2] = -random.nextDouble();
                        }
                        while (Math.signum(AppState.INSTANCE.currentConfiguration.m[0][1]) == Math.signum(AppState.INSTANCE.currentConfiguration.m[1][0]));
                        break;
                    case Lorenz_3D:
                        sigma = random.nextDouble() * 10;
                        rho = random.nextDouble() * 28;
                        beta = random.nextDouble() * 8 / 3;
                        Simulator.INSTANCE.setDxdt(sigma + "*(y-x)");
                        Simulator.INSTANCE.setDydt("(x*(" + rho + "-z)-y)");
                        Simulator.INSTANCE.setDzdt("(x*y-" + beta + "*z)");
                        break;
                    case Oscillations_3D:
                        AppState.INSTANCE.currentConfiguration.m[0][0] = -random.nextDouble();
                        AppState.INSTANCE.currentConfiguration.m[0][1] = 0;
                        AppState.INSTANCE.currentConfiguration.m[0][2] = 0;
                        AppState.INSTANCE.currentConfiguration.m[1][0] = 0;
                        AppState.INSTANCE.currentConfiguration.m[1][1] = -random.nextDouble();
                        AppState.INSTANCE.currentConfiguration.m[1][2] = 0;
                        AppState.INSTANCE.currentConfiguration.m[2][0] = 0;
                        AppState.INSTANCE.currentConfiguration.m[2][1] = 0;
                        AppState.INSTANCE.currentConfiguration.m[2][2] = -random.nextDouble();
                        break;
                    default:
                        for (int r = 0; r < AppState.INSTANCE.currentConfiguration.m.length; r++) {
                            for (int c = 0; c < AppState.INSTANCE.currentConfiguration.m[r].length; c++) {
                                AppState.INSTANCE.currentConfiguration.m[r][c] =
                                        (random.nextDouble() - 0.5) * 2;
                            }
                        }

                        Simulator.INSTANCE.setDxdt(generateExpression());
                        Simulator.INSTANCE.setDydt(generateExpression());
                        Simulator.INSTANCE.setDzdt(generateExpression());
                        break;
                }
            } catch (Exception e) {
                DialogHelper.showAlertDialog(getActivity(), getString(R.string.ERROR),
                        getString(R.string.unexpected_exception_) + "\n" + e.getMessage(),
                        getString(R.string.OK));
            }
            clearFocus();
            setControls();

        } else if (v == bResetTime) {
            Simulator.INSTANCE.resetElapsedTimeS();
        } else if (v == bResetParticles) {
            Simulator.INSTANCE.resetParticles();
        }
    }

    private void clearFocus() {
        Activity activity = getActivity();
        if (activity != null) {
            View currentFocus = getActivity().getCurrentFocus();
            if (currentFocus != null) {
                InputMethodManager inputMethodManager =
                        (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
        llFocusClear.requestFocus();
    }

    private String generateExpression() {
        StringBuilder expression = new StringBuilder();
        int operators = random.nextInt(4) + 1;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        for (int i = 0; i < operators; i++) {
            List<String> variables = new ArrayList<>();
            variables.add("x");
            variables.add("y");
            if (AppState.INSTANCE.currentConfiguration.simulationOrder == Settings.SIMULATION_ORDER_2ND) {
                variables.add("x'");
                variables.add("y'");
            }
            variables.add("t");
            variables.add(decimalFormat.format((random.nextDouble() - 0.5) * 2.0));
            if (AppState.INSTANCE.currentConfiguration.simulationDimensions == Settings.SIMULATION_DIMENSIONS_3D) {
                variables.add("z");
                if (AppState.INSTANCE.currentConfiguration.simulationOrder == Settings.SIMULATION_ORDER_2ND)
                    variables.add("z'");
            }
            expression.append(variables.get(random.nextInt(variables.size())));

            if (i + 1 < operators) {
                switch (random.nextInt(5)) {
                    case 0:
                        expression.append("*");
                        break;
                    case 1:
                        expression.append("/");
                        break;
                    case 2:
                        expression.append("+");
                        break;
                    case 3:
                        expression.append("-");
                        break;
                    case 4:
                        expression.append("^");
                        break;
                }
            }
        }

        return expression.toString();
    }

    @Override
    public boolean onLongClick(View v) {
        Toast toast = null;
        if (v == bDefaults) {
            toast = Toast.makeText(getActivity(), R.string.defaults_button_description,
                    Toast.LENGTH_SHORT);
            clearFocus();
        } else if (v == bRandomize) {
            toast = Toast.makeText(getActivity(), R.string.randomize_button_description,
                    Toast.LENGTH_SHORT);
            clearFocus();
        } else if (v == bResetTime) {
            toast = Toast.makeText(getActivity(), R.string.reset_time_button_description,
                    Toast.LENGTH_SHORT);
            clearFocus();
        } else if (v == bResetParticles) {
            toast = Toast.makeText(getActivity(), R.string.reset_particles_button_description,
                    Toast.LENGTH_SHORT);
            clearFocus();
        }
        if (toast != null) {
            int[] location = new int[2];
            v.getLocationOnScreen(location);
            toast.setGravity(Gravity.TOP | Gravity.START, location[0],
                    location[1] - (v.getHeight() * 2));
            toast.show();
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (suppressControlChanges)
            return;

        Activity activity = getActivity();
        View focus = null;
        if (activity != null) {
            focus = getActivity().getCurrentFocus();
        }
        String newText = s.toString();

        try {
            if (focus == etDxDt) {
                ((MainActivity) getActivity()).selectMode(SystemMode.Custom.ordinal());
                Simulator.INSTANCE.setDxdt(newText);
                etDxDt.setError(null);
            } else if (focus == etDyDt) {
                ((MainActivity) getActivity()).selectMode(SystemMode.Custom.ordinal());
                Simulator.INSTANCE.setDydt(newText);
                etDyDt.setError(null);
            } else if (focus == etDzDt) {
                ((MainActivity) getActivity()).selectMode(SystemMode.Custom.ordinal());
                Simulator.INSTANCE.setDzdt(newText);
                etDzDt.setError(null);
            } else if (focus == etTimeScale) {
                AppState.INSTANCE.currentConfiguration.timeScale = Double.parseDouble(newText);
            } else if (focus == etParticles) {
                int particleCount = Integer.parseInt(newText);
                if (particleCount >= 0 && particleCount <= Simulator.MAX_PARTICLE_COUNT)
                    Simulator.INSTANCE.setParticleCount(particleCount);
                else
                    etParticles.setError(getString(R.string.valid_range_0_) + Simulator.MAX_PARTICLE_COUNT);
            } else if (focus == lineWidth) {
                double lineWidthValue = Double.parseDouble(newText);
                if (lineWidthValue >= Simulator.INSTANCE.getLineWidthMin() && lineWidthValue <= Simulator.INSTANCE.getLineWidthMax())
                    Simulator.INSTANCE.setLineWidth(lineWidthValue);
                else
                    lineWidth.setError(getString(R.string.valid_range_) + Simulator.INSTANCE.getLineWidthMin() + " - " + Simulator.INSTANCE.getLineWidthMax());
            } else if (focus == etUpdateRate) {
                int updateRateValue = Integer.parseInt(newText);
                if (updateRateValue >= 1 && updateRateValue <= 1000)
                    AppState.INSTANCE.currentConfiguration.updateRate = updateRateValue;
                else etUpdateRate.setError(getString(R.string.valid_range_1_1000));
            } else if (focus == etParticleDuration) {
                double particleDurationValue = Double.parseDouble(newText);
                if (particleDurationValue >= 1 && particleDurationValue <= 10)
                    AppState.INSTANCE.currentConfiguration.particleDuration = particleDurationValue;
                else etParticleDuration.setError(getString(R.string.valid_range_1_10));
            } else {
                focusLoop:
                for (int r = 0; r < etM.length; r++) {
                    for (int c = 0; c < etM[r].length; c++) {
                        if (focus == etM[r][c]) {
                            AppState.INSTANCE.currentConfiguration.m[r][c] =
                                    Double.parseDouble(newText);
                            ((MainActivity) getActivity()).selectMode(SystemMode.Custom.ordinal());
                            break focusLoop;
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            if (focus != null) {
                if (e.getMessage() == null || e.getMessage().isEmpty())
                    ((EditText) focus).setError(getString(R.string.invalid_format_));
                else
                    ((EditText) focus).setError(getString(R.string.invalid_format_) + " " + e.getMessage());
            }
        } catch (FormatException e) {
            if (focus != null) {
                if (e.getMessage() == null || e.getMessage().isEmpty())
                    ((EditText) focus).setError(getString(R.string.invalid_syntax_));
                else ((EditText) focus).setError(e.getMessage());
            }
        }
    }

    public void setControls() {
        suppressControlChanges = true;

        sColor.setSelection(AppState.INSTANCE.currentConfiguration.colorScheme.ordinal());

        rgMode.check(AppState.INSTANCE.currentConfiguration.simulationMode == Settings.SIMULATION_MODE_MATRIX ? R.id.rbMatrix : R.id.rbExpressions);
        rgDimensions.check(AppState.INSTANCE.currentConfiguration.simulationDimensions == Settings.SIMULATION_DIMENSIONS_2D ? R.id.rb2D : R.id.rb3D);
        rgOrder.check(AppState.INSTANCE.currentConfiguration.simulationOrder == Settings.SIMULATION_ORDER_1ST ? R.id.rb1stOrder : R.id.rb2ndOrder);

        cbRandomizeVelocities.setChecked(AppState.INSTANCE.currentConfiguration.randomizeVelocities);

        tvXEquals.setText(AppState.INSTANCE.currentConfiguration.simulationOrder == Settings.SIMULATION_ORDER_2ND ? R.string.apostrophe_apostrophe_equals : R.string.apostrophe_equals);
        tvYEquals.setText(AppState.INSTANCE.currentConfiguration.simulationOrder == Settings.SIMULATION_ORDER_2ND ? R.string.apostrophe_apostrophe_equals : R.string.apostrophe_equals);
        tvZEquals.setText(AppState.INSTANCE.currentConfiguration.simulationOrder == Settings.SIMULATION_ORDER_2ND ? R.string.apostrophe_apostrophe_equals : R.string.apostrophe_equals);
        tvXEqualsExpression.setText(AppState.INSTANCE.currentConfiguration.simulationOrder == Settings.SIMULATION_ORDER_2ND ? R.string.apostrophe_apostrophe_equals : R.string.apostrophe_equals);
        tvYEqualsExpression.setText(AppState.INSTANCE.currentConfiguration.simulationOrder == Settings.SIMULATION_ORDER_2ND ? R.string.apostrophe_apostrophe_equals : R.string.apostrophe_equals);
        tvZEqualsExpression.setText(AppState.INSTANCE.currentConfiguration.simulationOrder == Settings.SIMULATION_ORDER_2ND ? R.string.apostrophe_apostrophe_equals : R.string.apostrophe_equals);

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

        for (int r = 0; r < etM.length; r++) {
            for (int c = 0; c < etM[r].length; c++) {
                etM[r][c].setText(numberFormat.format(AppState.INSTANCE.currentConfiguration.m[r][c]));
            }
        }

        etDxDt.setText(AppState.INSTANCE.currentConfiguration.dxdt);
        etDyDt.setText(AppState.INSTANCE.currentConfiguration.dydt);
        etDzDt.setText(AppState.INSTANCE.currentConfiguration.dzdt);

        etTimeScale.setText(numberFormat.format(AppState.INSTANCE.currentConfiguration.timeScale));
        etParticles.setText(numberFormat.format(AppState.INSTANCE.currentConfiguration.particleCount));
        lineWidth.setText(numberFormat.format(AppState.INSTANCE.currentConfiguration.lineWidth));
        etUpdateRate.setText(numberFormat.format(AppState.INSTANCE.currentConfiguration.updateRate));
        etParticleDuration.setText(numberFormat.format(AppState.INSTANCE.currentConfiguration.particleDuration));

        for (int r = 0; r < etM.length; r++) {
            for (int c = 0; c < etM[r].length; c++) {
                etM[r][c].setError(null);
            }
        }

        etDxDt.setError(null);
        etDyDt.setError(null);
        etDzDt.setError(null);

        etTimeScale.setError(null);
        etParticles.setError(null);
        lineWidth.setError(null);
        etUpdateRate.setError(null);
        etParticleDuration.setError(null);

        suppressControlChanges = false;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == rgMode) {
            if (checkedId == R.id.rbMatrix)
                Simulator.INSTANCE.setSimulationMode(Settings.SIMULATION_MODE_MATRIX);
            else Simulator.INSTANCE.setSimulationMode(Settings.SIMULATION_MODE_EXPRESSION);
        } else if (group == rgDimensions) {
            if (checkedId == R.id.rb2D)
                AppState.INSTANCE.currentConfiguration.simulationDimensions =
                        Settings.SIMULATION_DIMENSIONS_2D;
            else
                AppState.INSTANCE.currentConfiguration.simulationDimensions =
                        Settings.SIMULATION_DIMENSIONS_3D;
        } else if (group == rgOrder) {
            if (checkedId == R.id.rb1stOrder)
                AppState.INSTANCE.currentConfiguration.simulationOrder =
                        Settings.SIMULATION_ORDER_1ST;
            else
                AppState.INSTANCE.currentConfiguration.simulationOrder =
                        Settings.SIMULATION_ORDER_2ND;
        }

        setControlVisibilities(AppState.INSTANCE.currentConfiguration.simulationMode,
                AppState.INSTANCE.currentConfiguration.simulationDimensions, AppState.INSTANCE
                .currentConfiguration.simulationOrder);

        if (suppressControlChanges)
            return;

        setControls();
        clearFocus();
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) getActivity()).selectMode(SystemMode.Custom.ordinal());
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (suppressControlChanges)
            return;

        if (parent == sColor)
            AppState.INSTANCE.currentConfiguration.colorScheme = ColorScheme.values()[(int) id];
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (suppressControlChanges)
            return;

        if (buttonView == cbRandomizeVelocities)
            AppState.INSTANCE.currentConfiguration.randomizeVelocities = isChecked;
    }
}
