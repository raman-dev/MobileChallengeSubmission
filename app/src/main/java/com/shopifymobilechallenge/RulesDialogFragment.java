package com.shopifymobilechallenge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class RulesDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        TextView title = new TextView(getContext());
        // You Can Customise your Title here
        title.setText("Rules");
        title.setBackgroundColor(0xFF333333);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);

        builder.setMessage(
                "1. Cards are laid face down in rows.\n" +
                "\n2. Turn over any two cards.\n" +
                "\n3. If the two cards match, keep them.\n" +
                "\n4. If they don't match cards will be turned back over.\n" +
                "\n5. Remember what was on each card and where it was.\n" +
                "\n6. The game is over when all the cards have been matched.\n");
        return builder.create();
    }
}
