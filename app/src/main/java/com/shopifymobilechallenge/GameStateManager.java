package com.shopifymobilechallenge;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.Collections;

public class GameStateManager implements View.OnClickListener,Runnable{

    public static final int SMALL_GRID = 1;
    public static final int MEDIUM_GRID = 2;
    public static final int LARGE_GRID = 3;

    public static String HIGH_SCORE_KEY = "highscore123";

    private static final String TAG = "GameStateManager";

    private ArrayList<ImageCard> deck;
    private ArrayList<ImageCard> playField;

    private final int MIN_MATCHING_PAIRS = 10;

    public int rows = 6;
    public int columns = 4;

    public int currentGridSize = MEDIUM_GRID;
    private int matching_pairs = MIN_MATCHING_PAIRS;

    private LinearLayout linearLayout;
    private Chronometer chronometer;
    private Bitmap defaultImage = null;

    private int CardsFaceUp = 0;
    private int NumCardsUpAllowed = 2;

    private ArrayList<ImageCard> FaceUpCardList;
    private Handler handler;

    private long FlipDelay = 500;
    private boolean GameStarted = false;
    private int animationsCompleted = 0;

    private int imageButtonHeight = 0;
    private int imageButtonWidth = 0;
    private int remaining_pairs = matching_pairs;

    private WinDialogFragment winDialogFragment;
    private TextView remainingPairLabel;
    private TextView fastestTimeLabel;

    private FragmentManager fragmentManager;

    private AnimatorListenerAdapter initFlipDownListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            Log.i(TAG,"Animations Completed => "+animationsCompleted);
            if(animationsCompleted == playField.size() - 1){
                Log.i(TAG,"Starting Chronometer!");
                GameStarted = true;
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
            }
            animationsCompleted++;
        }
    };


    public GameStateManager(ArrayList<ImageCard> deck, Bitmap defaultImage, Handler handler){
        this.deck = deck;
        this.defaultImage = defaultImage;

        this.handler = handler;


        playField = new ArrayList<>();
        FaceUpCardList = new ArrayList<>();
        deck.forEach( x -> {
            x.setOnClickListener(this);
            x.initFlipDownAnimator.addListener(initFlipDownListener);
        });
    }

    public void setUiElements(FragmentManager fragmentManager,WinDialogFragment winDialogFragment, LinearLayout linearLayout, Chronometer chronometer, TextView remainingPairLabel, TextView fastestTimeLabel) {
        this.linearLayout = linearLayout;
        this.chronometer = chronometer;
        this.remainingPairLabel = remainingPairLabel;
        this.fastestTimeLabel = fastestTimeLabel;
        this.winDialogFragment = winDialogFragment;
        this.fragmentManager = fragmentManager;
    }

    public void NewGame(){
        GetNewPlayFieldDimensions();
        ShufflePlayField();
        UpdatePlayField();
        remaining_pairs = matching_pairs;
        updateRemainingPairLabel();
    }

    private void updateRemainingPairLabel(){
        String text = "Pairs Remaining: "+remaining_pairs;
        remainingPairLabel.setText(text);
    }

    /**
     * Calculate play field size and button size dimensions
     */
    private void GetNewPlayFieldDimensions(){
        switch (currentGridSize){
            case SMALL_GRID:
                rows = 5;
                matching_pairs = 1;//\MIN_MATCHING_PAIRS;
                break;
            case MEDIUM_GRID:
                rows = 6;
                matching_pairs = 1;
                break;
            case LARGE_GRID:
                rows = 7;
                matching_pairs = MIN_MATCHING_PAIRS + 2;
                break;
        }

        int height = linearLayout.getMeasuredHeight();
        int width = linearLayout.getMeasuredWidth();
        //make buttons square
        //take the smaller of the two
        imageButtonWidth = width/columns;
        imageButtonHeight = height/rows;//height/gsm.columns;
        //now the rows need to be calculated to maintain square buttons
        if(imageButtonWidth < imageButtonHeight){
            rows = Math.round((float)Math.floor((float)height/imageButtonWidth));
            imageButtonHeight = imageButtonWidth;
        }else{
            columns = Math.round((float)Math.floor((float)width/imageButtonHeight));
            imageButtonWidth = imageButtonHeight;
        }

        //make sure new gridsize does not go beyond the number of cards in the deck
        int grid_size = rows * columns;
        if(grid_size > deck.size()){
            //reduce the larger value by 1 until the value is <= 50
            if(rows > columns){
                while(grid_size > deck.size()){
                    rows--;
                    grid_size = rows*columns;
                }
            }else{
                while(grid_size > deck.size()){
                    columns--;
                    grid_size = rows*columns;
                }
            }
        }
        System.out.println("w x h => "+imageButtonWidth+" x "+imageButtonHeight);
        System.out.println("GridSize => "+rows+" x "+columns);
    }

    /**
     * Get a new shuffled collection of cards to display to the user
     */
    public void ShufflePlayField(){
        Collections.shuffle(deck);//
        //shuffle the deck
        //take from deck num_cards
        //playfield size  = rows x columns
        int playfield_size = rows * columns;//must be larger than matching pairs
        //total cards on the field = playfield_size
        //you need to atleast take matching pairs number of cards
        int num_cards = matching_pairs + (playfield_size - matching_pairs*2);//number of extra cards to take from the deck
        int i = 0;
        int j = 0;
        playField.clear();//empty playfield
        LinearLayout.LayoutParams buttonSizeAdjusted = new LinearLayout.LayoutParams(imageButtonWidth,imageButtonHeight,1f);
        while (i < num_cards){
            ImageCard card = deck.get(i);
            card.setLayoutParams(buttonSizeAdjusted);
            if(j < matching_pairs){//add the same card twice until j = matching pairs
                ImageCard duplicate = new ImageCard(card.getContext(),this,card.id,card.image,defaultImage);
                duplicate.setOnClickListener(this);
                duplicate.initFlipDownAnimator.addListener(initFlipDownListener);
                duplicate.setLayoutParams(buttonSizeAdjusted);
                playField.add(duplicate);
                j++;
            }
            playField.add(card);
            i++;
        }
        Collections.shuffle(playField);
    }


    /*
     * Put playfield onto screen
     * */
    private void UpdatePlayField() {
        linearLayout.removeAllViews();
        int current = 0;
        //we know the size of source images
        //make the size
        for (int i = 0; i < rows; i++) {
            //create a row here
            //could add a linear layout horizontal that match_parent width but
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout row = new LinearLayout(linearLayout.getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);//row
            row.setLayoutParams(layoutParams);
            for (int j = 0; j < columns; j++,current++) {
                //add image views here
                //fill the row
                ImageCard imageCard = playField.get(current);
                ViewGroup parent = (ViewGroup) imageCard.getParent();
                if(parent != null) {
                    parent.removeView(imageCard);
                }
                row.addView(imageCard);
            }
            //now add the
            linearLayout.addView(row);
        }
    }

    public void StartGame(){
        playField.forEach(ImageCard::initialFlip);//show every card at its position
        //when the animations are over then
    }

    @Override
    public void onClick(View v) {
        //now a single on click handles every image button click
        //so when a button is clicked flip it up
        ImageCard card = (ImageCard)v;
        //the current card that was clicked
        if(GameStarted && CardsFaceUp < NumCardsUpAllowed){
            Log.i(TAG,"Clicked card.id=>"+card.id);
            if(!card.isEliminated){
                //this if is to tap the same card without tapping an additional card
                if(card.isFaceUp){
                    card.HideImage();
                    FaceUpCardList.remove(card);
                    CardsFaceUp--;
                }//if card is face down
                else{
                    card.ShowImage();
                    FaceUpCardList.add(card);
                    CardsFaceUp++;
                    CheckMatching();
                }
            }else{
                Log.i(TAG,"(Eliminated)Clicked card.id=>"+card.id);
            }
            Log.i(TAG,"Cards Face Up =>"+CardsFaceUp);
        }
    }

    void CheckMatching() {
        if(CardsFaceUp < NumCardsUpAllowed){
            Log.i(TAG,"Not enough cards face up for match");
            return;
        }
        Log.i(TAG,"Checking Cards!!");
        ImageCard card = FaceUpCardList.get(0);
        for (int i = 1; i < FaceUpCardList.size(); i++) {
            if(FaceUpCardList.get(i).id != card.id){
                Log.i(TAG,"Cards do not match!");
                //flip cards back over after some time
                //so the user can notice the card was tested then failed
                FaceUpCardList.forEach(x ->{
                    x.setBackgroundResource(R.drawable.button_incorrect_highlight);
                });
                handler.postDelayed(this,FlipDelay);
                return;
            }
        }
        Log.i(TAG,"Cards are a Match!!!");
        //now mark as eliminated and clear list
        for (int i = 0; i < FaceUpCardList.size(); i++) {
            FaceUpCardList.get(i).Eliminate();
        }
        FaceUpCardList.clear();
        CardsFaceUp = 0;
        //decrement remaining pairs
        remaining_pairs--;
        updateRemainingPairLabel();
        //check if user has found all matching pairs
        if(remaining_pairs == 0){
            GameStarted = false;
            winDialogFragment.show(fragmentManager,"WinDialog!");
        }
    }

    @Override
    public void run() {
        //for each card flip it back
        for (int i = 0; i < FaceUpCardList.size(); i++) {
            FaceUpCardList.get(i).HideImage();
        }

        FaceUpCardList.clear();
        CardsFaceUp = 0;
    }

    public void ChangeGridSize(int newGridSize) {
        //if the grid size is different than
        if(newGridSize != currentGridSize){
            currentGridSize = newGridSize;
            NewGame();//a new grid should be generated
        }
    }


}
