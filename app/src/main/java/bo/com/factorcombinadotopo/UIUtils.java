package bo.com.factorcombinadotopo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

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
     * Crea una guía técnica sobre el ajuste de barómetro mediante un Diálogo Pro.
     */
    public static void showBarometerInfo(Context context) {
        View dv = LayoutInflater.from(context).inflate(R.layout.layout_barometer_info, null);
        androidx.appcompat.app.AlertDialog.Builder b = new androidx.appcompat.app.AlertDialog.Builder(context);
        androidx.appcompat.app.AlertDialog d = b.create();
        if (d.getWindow() != null) d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.setView(dv);

        dv.findViewById(R.id.btn_guide_close).setOnClickListener(v -> d.dismiss());
        d.show();
    }

    /**
     * Muestra un diálogo de información profesional con el estilo unificado de la App.
     */
    public static void showProInfoDialog(Context context, String title, CharSequence content, int iconRes) {
        View dv = LayoutInflater.from(context).inflate(R.layout.layout_dialog_info_pro, null);
        androidx.appcompat.app.AlertDialog.Builder b = new androidx.appcompat.app.AlertDialog.Builder(context);
        androidx.appcompat.app.AlertDialog d = b.create();
        if (d.getWindow() != null) d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.setView(dv);

        TextView txtTitle = dv.findViewById(R.id.txt_dialog_title);
        TextView txtContent = dv.findViewById(R.id.txt_dialog_content);
        ImageView imgIcon = dv.findViewById(R.id.img_dialog_icon);

        txtTitle.setText(title);
        txtContent.setText(content);
        if (iconRes != 0) {
            imgIcon.setImageResource(iconRes);
            imgIcon.setColorFilter(context.getColor(R.color.accent_primary));
        }

        dv.findViewById(R.id.btn_dialog_close).setOnClickListener(v -> d.dismiss());
        d.show();
    }

    /**
     * Crea una ventana flotante genérica con un título y un mensaje.
     * Mantenido por compatibilidad, pero se recomienda usar showProInfoDialog.
     */
    public static PopupWindow showPopupInfo(Context context, View parentView, String title, String message) {
        showProInfoDialog(context, title, message, R.drawable.ic_info_round_blue);
        return null; // Retornamos null ya que ahora es un diálogo, no un popup
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
        TextView txtWarn = view.findViewById(R.id.txt_dialog_warning);
        
        txtTitle.setText(titleRes);
        txtMsg.setText(msgRes);
        
        // Lógica específica para advertencia de caché
        if (titleRes == R.string.title_confirm_cache_clear || 
            titleRes == R.string.title_confirm_cache_street || 
            titleRes == R.string.title_confirm_cache_sat) {
            txtWarn.setVisibility(View.VISIBLE);
            txtWarn.setText(R.string.warn_cache_clear);
        } else {
            txtWarn.setVisibility(View.GONE);
        }

        view.findViewById(R.id.btn_dialog_yes).setOnClickListener(v -> {
            onConfirm.run();
            dialog.dismiss();
        });
        view.findViewById(R.id.btn_dialog_no).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Muestra un diálogo de forma segura, verificando que la Activity siga activa.
     */
    public static void safeShowDialog(AlertDialog dialog) {
        try {
            if (dialog != null && !dialog.isShowing()) {
                dialog.show();
            }
        } catch (Exception e) {
            android.util.Log.e("UIUtils", "Error showing dialog", e);
        }
    }

    /**
     * Cierra un diálogo de forma segura.
     */
    public static void safeDismissDialog(AlertDialog dialog) {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            android.util.Log.e("UIUtils", "Error dismissing dialog", e);
        }
    }

    private static Toast currentToast;

    private static void showCustomToast(Context context, String type, String label, String message, int duration) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (currentToast != null) currentToast.cancel();
            
            Toast toast = prepareCustomToast(context, type, label, message, duration);
            if (toast != null) {
                currentToast = toast;
                toast.show();
            }
        });
    }

    private static Toast prepareCustomToast(Context context, String type, String labelStr, String message, int duration) {
        try {
            LayoutInflater inflater = LayoutInflater.from(context);
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
            return Toast.makeText(context, message, duration);
        }
    }
}
