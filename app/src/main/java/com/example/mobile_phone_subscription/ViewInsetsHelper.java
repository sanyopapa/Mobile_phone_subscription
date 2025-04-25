package com.example.mobile_phone_subscription;

import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Az oldalak helyes megjelenítéséhez szükséges segédosztály
 */
public class ViewInsetsHelper {

    /**
     * Beállítja a rendszer kijelzői területek (status és navigation bar)
     * megfelelő kezelését egy görgetett nézet számára
     *
     * @param view A nézet, amire alkalmazni szeretnénk a beállítást (általában ScrollView gyermeke)
     */
    public static void setupScrollableLayoutInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).topMargin = systemBars.top;

            v.setPadding(
                systemBars.left,
                0,
                systemBars.right,
                systemBars.bottom + (int)(48 * v.getContext().getResources().getDisplayMetrics().density) // Extra tér a gombnak
            );

            return insets;
        });
    }
}