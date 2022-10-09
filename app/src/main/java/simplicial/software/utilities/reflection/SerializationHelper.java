package simplicial.software.utilities.reflection;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.Arrays;

import simplicial.software.utilities.android.DialogHelper;

public class SerializationHelper {

    public static void storeObjectInFile(Context context, Object object, String fileName,
                                         int mode) {
        FileOutputStream fileStream = null;
        DataOutputStream dataStream;
        ObjectOutputStream objectStream;
        ByteArrayOutputStream byteStream = null;
        try {
            byteStream = new ByteArrayOutputStream();

            objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(object);

            dataStream = new DataOutputStream(byteStream);
            dataStream.writeInt(Arrays.hashCode(byteStream.toByteArray()));

            fileStream = context.openFileOutput(fileName, mode);
            fileStream.write(byteStream.toByteArray());
        } catch (Exception e) {
            Log.e("Utilities", "Save failed.", e);
        } finally {
            try {
                if (fileStream != null)
                    fileStream.close();
            } catch (IOException e) {
                Log.e("Utilities", "Save failed.", e);
            }

            try {
                if (byteStream != null)
                    byteStream.close();
            } catch (IOException e) {
                Log.e("Utilities", "Save failed.", e);
            }
        }
    }

    public static Object loadObjectFromfile(Context context, String fileName) {
        FileInputStream fileStream = null;
        ObjectInputStream objectStream;
        DataInputStream dataStream;
        ByteArrayInputStream byteStream = null;

        try {
            byte[] dataBuffer =
                    new byte[(int) new File(context.getFilesDir(), fileName).length() - Integer.SIZE / Byte.SIZE];
            fileStream = context.openFileInput(fileName);
            //noinspection ResultOfMethodCallIgnored
            fileStream.read(dataBuffer);

            dataStream = new DataInputStream(fileStream);

            if (dataStream.readInt() != Arrays.hashCode(dataBuffer)) {
                DialogHelper.showAlertToast(context, "Failed to validate file " + fileName + " .");
                Log.e("Utilities", "Failed to validate file " + fileName + " .");
                return null;
            }

            byteStream = new ByteArrayInputStream(dataBuffer);
            objectStream = new ObjectInputStream(byteStream);
            return objectStream.readObject();
        } catch (Exception e) {
            Log.e("Utilities", "Load failed.", e);
            return null;
        } finally {
            try {
                if (fileStream != null)
                    fileStream.close();
            } catch (IOException e) {
                Log.e("Utilities", "Load failed.", e);
            }

            try {
                if (byteStream != null)
                    byteStream.close();
            } catch (IOException e) {
                Log.e("Utilities", "Load failed.", e);
            }
        }
    }
}
