package com.shopifymobilechallenge;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import android.view.ViewGroup;
import android.widget.LinearLayout;


import androidx.appcompat.widget.AppCompatImageButton;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageCard extends AppCompatImageButton{

    private static final String TAG = "ImageCard";

    private GameStateManager gsm;
    public ObjectAnimator initFlipUpAnimator;
    public ObjectAnimator initFlipDownAnimator;

    private final float rotationDegrees = 180f;
    private final long animDuration = 300;
    private final long flipDownDelay = 2000;
    Bitmap image;
    private Bitmap defaultImage = null;
    private Drawable defaultDrawable;

    boolean isFaceUp = false;
    boolean isEliminated = false;

    String id;

    public ImageCard(Context context) {
        super(context);
    }
    public ImageCard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ImageCard(Context context,GameStateManager gsm,String id, String image_source,Bitmap defaultImage){
        super(context);
        this.id = id;
        this.defaultImage = defaultImage;
        this.gsm = gsm;
        downloadImage(image_source);
        init(context);
    }
    public ImageCard(Context context,GameStateManager gsm,String id,Bitmap image,Bitmap defaultImage){
        super(context);
        this.id = id;
        this.image = image;
        this.defaultImage = defaultImage;
        this.gsm = gsm;
        init(context);

    }

    private void init(Context context){
        LinearLayout.LayoutParams buttonParams =//for even spacing
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,1f);
        setLayoutParams(buttonParams);
        defaultDrawable = getBackground();
        setImageBitmap(defaultImage);
        setScaleType(ScaleType.CENTER_INSIDE);

        initFlipUpAnimator = ObjectAnimator.ofFloat(this,"rotationY",rotationDegrees);
        initFlipUpAnimator.setDuration(animDuration);
        initFlipUpAnimator.addListener(animatorListenerAdapter);
        initFlipUpAnimator.addUpdateListener(animatorUpdateListenerFlipUp);

        initFlipDownAnimator = ObjectAnimator.ofFloat(this,"rotationY",0);
        initFlipDownAnimator.setDuration(animDuration);
        initFlipDownAnimator.setStartDelay(flipDownDelay);
        initFlipDownAnimator.addUpdateListener(animatorUpdateListenerFlipDown);

        float scale = context.getResources().getDisplayMetrics().density;
        setCameraDistance(2000 * scale);
    }

    private void downloadImage(String image_source) {
        HttpURLConnection imageConnection = null;
        try {
            imageConnection = (HttpURLConnection) new URL(image_source).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //get the image from resource and set to bitmap
        try {
            image = BitmapFactory.decodeStream(imageConnection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageConnection.disconnect();
    }

    private AnimatorListenerAdapter animatorListenerAdapter = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            initFlipDownAnimator.start();//flip it back down
        }
    };
    private ValueAnimator.AnimatorUpdateListener animatorUpdateListenerFlipUp = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = (float)animation.getAnimatedValue();
            if(105f > value && value > 75f){
                setImageBitmap(image);
            }
        }
    };
    private ValueAnimator.AnimatorUpdateListener animatorUpdateListenerFlipDown = animation -> {
        float value = (float)animation.getAnimatedValue();
        if(105f > value && value > 75f){
            setImageBitmap(defaultImage);
        }
    };


    public void initialFlip(){
        //show the user the cards for certain amount of time then
        initFlipUpAnimator.start();
    }

    public void ShowImage() {
        isFaceUp = true;
        setImageBitmap(image);
    }

    public void HideImage(){
        isFaceUp = false;

        setImageBitmap(defaultImage);
        setBackgroundDrawable(defaultDrawable);
    }

    public void Eliminate() {
        isEliminated = true;
        isFaceUp = true;
        setImageBitmap(image);
        setBackgroundResource(R.drawable.button_correct_highlight);
    }

    public void Reset(){
        isEliminated = false;
        isFaceUp = false;
        setImageBitmap(defaultImage);
        setBackground(defaultDrawable);
    }
}
