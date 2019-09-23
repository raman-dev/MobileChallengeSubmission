package com.shopifymobilechallenge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class MainMenuDialogFragment extends DialogFragment {
    private View.OnClickListener clickListener;
    public MainMenuDialogFragment(View.OnClickListener clickListener){
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Main Menu");
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.main_menu,null);
        view.findViewById(R.id.PlayButton).setOnClickListener(clickListener);
        view.findViewById(R.id.RulesButton).setOnClickListener(clickListener);
        view.findViewById(R.id.SettingsButton).setOnClickListener(clickListener);

        builder.setView(view);
        TextView title = new TextView(getContext());
        // You Can Customise your Title here
        title.setText("Main");
        title.setBackgroundColor(0xFF333333);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);

        builder.setCustomTitle(title);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if(keyCode == KeyEvent.KEYCODE_BACK){
                getActivity().finish();
            }
            return true;
        });
        return dialog;
    }

}
