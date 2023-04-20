package com.filenko.conspectnote.activity.tests;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;

@SuppressLint("ViewConstructor")
public class ButtonTest extends androidx.appcompat.widget.AppCompatButton {
    private boolean select = false;
    private boolean correct = false;

    public ButtonTest(Context context, String textButton) {
        super(context);
        setText(textButton);
        setBackgroundColor(Color.rgb(213, 213, 213)); // gray
        setAllCaps(false);
    }

    public boolean getCorrect () {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public boolean isSelectAnswer() {
        return this.select;
    }

    public void setSelectedAnswer(boolean select) {
        this.select = select;
    }
}
