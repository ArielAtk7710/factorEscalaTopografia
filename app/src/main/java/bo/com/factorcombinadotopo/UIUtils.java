package bo.com.factorcombinadotopo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class UIUtils {

    public static void showSuccessToast(Context context, String message) {
        showCustomToast(context, "success", context.getString(R.string.label_success), message, Toast.LENGTH_SHORT);
    }

    public static void showErrorToast(Context context, String message) {
        showCustomToast(context, "error", context.getString(R.string.label_error), message, Toast.LENGTH_LONG);
    }

    public static void showWarningToast(Context context, String message) {
        showCustomToast(context, "warning", context.getString(R.string.label_warning), message, Toast.LENGTH_SHORT);
    }

    public static void showInfoToast(Context context, String message) {
        showCustomToast(context, "info", context.getString(R.string.label_info), message, Toast.LENGTH_SHORT);
    }

    /**
     * Muestra un Toast de información que dura aproximadamente 5 segundos.
     */
    public static void showInfoToastLong(Context context, String message) {
        final Toast toast = prepareCustomToast(context, "info", context.getString(R.string.label_info), message, Toast.LENGTH_LONG);
        if (toast != null) {
            toast.show();
            // Extender duración disparando de nuevo a los 2 segundos
            new Handler(Looper.getMainLooper()).postDelayed(toast::show, 2000);
        }
    }

    /**
     * Crea una guía técnica sobre el ajuste de barómetro mediante un Popup temporal.
     * Retorna la instancia para permitir al llamador cerrarla manualmente.
     */
    public static PopupWindow showBarometerInfo(Context context, View parentView) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.layout_barometer_info, null);
        
        final PopupWindow popupWindow = new PopupWindow(popupView, 
                ViewGroup.LayoutParams.WRAP_CONTENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setElevation(30);
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        
        // Centrar en la pantalla
        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);

        // Auto-dismiss opcional o manual por el llamador
        return popupWindow;
    }

    public static void showConfirmDialog(Context context, int titleRes, int msgRes, Runnable onConfirm) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_confirm, null);
        builder.setView(view);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView txtTitle = view.findViewById(R.id.txt_dialog_title);
        TextView txtMsg = view.findViewById(R.id.txt_dialog_message);
        txtTitle.setText(titleRes);
        txtMsg.setText(msgRes);

        view.findViewById(R.id.btn_dialog_yes).setOnClickListener(v -> {
            onConfirm.run();
            dialog.dismiss();
        });
        view.findViewById(R.id.btn_dialog_no).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private static void showCustomToast(Context context, String type, String label, String message, int duration) {
        Toast toast = prepareCustomToast(context, type, label, message, duration);
        if (toast != null) toast.show();
    }

    private static Toast prepareCustomToast(Context context, String type, String labelStr, String message, int duration) {
        try {
            LayoutInflater inflater = LayoutInflater.from(context);
            // Se usa null porque el Toast no tiene un contenedor raíz disponible en el momento de inflar
            View layout = inflater.inflate(R.layout.layout_custom_toast_pro, null);

            View root = layout.findViewById(R.id.toast_root);
            ImageView icon = layout.findViewById(R.id.toast_icon);
            TextView label = layout.findViewById(R.id.toast_label);
            TextView msg = layout.findViewById(R.id.toast_message);

            switch (type.toLowerCase()) {
                case "success":
                    root.setBackgroundResource(R.drawable.bg_toast_success);
                    icon.setImageResource(R.drawable.ic_toast_success);
                    break;
                case "error":
                    root.setBackgroundResource(R.drawable.bg_toast_error);
                    icon.setImageResource(R.drawable.ic_toast_error);
                    break;
                case "warning":
                    root.setBackgroundResource(R.drawable.bg_toast_warning);
                    icon.setImageResource(R.drawable.ic_toast_warning);
                    break;
                default: // info
                    root.setBackgroundResource(R.drawable.bg_toast_info);
                    icon.setImageResource(R.drawable.ic_toast_info);
                    break;
            }

            label.setText(labelStr);
            msg.setText(message);

            Toast toast = new Toast(context);
            toast.setDuration(duration);
            toast.setView(layout);
            toast.setGravity(Gravity.CENTER, 0, 0);
            return toast;
        } catch (Exception e) {
            // Fallback al Toast estándar si algo falla con el layout custom
            return Toast.makeText(context, message, duration);
        }
    }
}
