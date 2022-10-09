package simplicial.software.utilities.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

public class DialogHelper {
    private static AlertDialog DIALOG;

    public static void showAlertToast(final Context context, final String message) {
        if (!(context instanceof Activity))
            return;

        Activity activity = (Activity) context;
        activity.runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    public static void showAlertDialog(Context context, String title, String message,
                                       String OKButtonText) {
        if (DIALOG != null && DIALOG.isShowing())
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNeutralButton(OKButtonText, null);
        DIALOG = builder.create();
        DIALOG.show();
    }
}
