package com.shopifymobilechallenge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SettingsDialogFragment extends DialogFragment implements RadioGroup.OnCheckedChangeListener{


    public GameStateManager gsm;
    private int currentGridSize = -1;
    public SettingsDialogFragment(){
        this.gsm = gsm;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.settings,null);

        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        radioGroup.check(R.id.MediumGridButton);
        radioGroup.setOnCheckedChangeListener(this);
        builder.setView(view);

        TextView title = new TextView(getContext());
        // You Can Customise your Title here
        title.setText("Settings");
        title.setBackgroundColor(0xFF333333);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);

        builder.setCustomTitle(title);

        return builder.create();
    }

    public void setGameStateManager(GameStateManager gsm){
        this.gsm = gsm;
        currentGridSize = gsm.currentGridSize;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        gsm.ChangeGridSize(currentGridSize);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch(checkedId){
            case R.id.SmallGridButton:
                System.out.println("SmallGrid");
                currentGridSize = GameStateManager.SMALL_GRID;
                break;
            case R.id.MediumGridButton:
                System.out.println("MediumGrid");
                currentGridSize = GameStateManager.MEDIUM_GRID;
                break;
            case R.id.LargeGridButton:
                System.out.println("LargeGrid");
                currentGridSize = GameStateManager.LARGE_GRID;
                break;
        }
    }
}
