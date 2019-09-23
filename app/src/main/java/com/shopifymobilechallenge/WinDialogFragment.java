package com.shopifymobilechallenge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class WinDialogFragment extends DialogFragment {
    private View.OnClickListener clickListener;
    private CharSequence scoreText;

    public WinDialogFragment(View.OnClickListener clickListener){
        this.clickListener = clickListener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.win_dialog,null);

        view.findViewById(R.id.PlayAgainButton).setOnClickListener(clickListener);
        view.findViewById(R.id.BackToMainButton).setOnClickListener(clickListener);
        ((TextView)view.findViewById(R.id.YourTimeLabel)).setText(scoreText);
        builder.setView(view);

        TextView title = new TextView(getContext());
        // You Can Customise your Title here
        title.setText("You Win!");
        title.setBackgroundColor(0xFF333333);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);

        builder.setCustomTitle(title);
        return builder.create();
    }

    public void setTime(CharSequence text) {
        scoreText = text;
    }
}
