package com.example.mobile_phone_subscription;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;

/**
 * Segédosztály megerősítő párbeszédablakok egységes megjelenítéséhez
 */
public class DialogHelper {

    /**
     * Általános megerősítő párbeszédablak megjelenítése
     */
    public static void showConfirmationDialog(
            Context context,
            String title,
            String message,
            String positiveButtonText,
            String negativeButtonText,
            Runnable onConfirm) {

        new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText, (dialog, which) -> {
                if (onConfirm != null) {
                    onConfirm.run();
                }
            })
            .setNegativeButton(negativeButtonText, null)
            .show();
    }

    /**
     * Vásárlási megerősítő párbeszédablak
     */
    public static void showPurchaseConfirmationDialog(
            Context context,
            String planName,
            Runnable onConfirm) {

        showConfirmationDialog(
                context,
                "Megerősítés",
                "Biztosan meg szeretnéd vásárolni a(z) " + planName + " csomagot?",
                "Igen",
                "Nem",
                onConfirm
        );
    }

    /**
     * Előfizetés lemondási megerősítő párbeszédablak
     */
    public static void showCancelSubscriptionDialog(
            Context context,
            Runnable onConfirm) {

        showConfirmationDialog(
                context,
                "Megerősítés",
                "Biztosan le szeretnéd mondani az előfizetésed?",
                "Igen",
                "Nem",
                onConfirm
        );
    }

    /**
     * Profil mentési megerősítő párbeszédablak
     */
    public static void showSaveProfileDialog(
            Context context,
            Runnable onConfirm) {

        showConfirmationDialog(
                context,
                "Megerősítés",
                "Biztosan menteni szeretnéd a változtatásokat?",
                "Igen",
                "Nem",
                onConfirm
        );
    }

    /**
     * Csomag szerkesztési megerősítő párbeszédablak
     */
    public static void showSavePlanDialog(
            Context context,
            Runnable onConfirm) {

        showConfirmationDialog(
                context,
                "Megerősítés",
                "Biztosan menteni szeretnéd a csomag változtatásait?",
                "Igen",
                "Nem",
                onConfirm
        );
    }
}