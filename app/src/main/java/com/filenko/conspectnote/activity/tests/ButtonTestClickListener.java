package com.filenko.conspectnote.activity.tests;

import android.graphics.Color;
import android.view.View;

import com.filenko.conspectnote.model.Question;

import java.util.List;

public class ButtonTestClickListener implements View.OnClickListener {
    private final Question question;
    private final List<ButtonTest> list;

    private final int GRAY = Color.rgb(213, 213, 213);
    private final int YELLOW = Color.rgb(252, 219, 170);

    public ButtonTestClickListener(Question question, List<ButtonTest> buttonTestList) {
        this.question = question;
        this.list = buttonTestList;

    }

    @Override
    public void onClick(View v) {
        if (question.getType() == 2) {
            for (ButtonTest bt : this.list) {
                bt.setBackgroundColor(GRAY);
                bt.setSelectedAnswer(false);
            }

            ButtonTest bt = (ButtonTest) v;
            bt.setBackgroundColor(YELLOW); //yellow
            bt.setSelectedAnswer(true);
        }

        if (question.getType() == 1) {
            ButtonTest bt = (ButtonTest) v;
            if (bt.isSelectAnswer()) {
                bt.setBackgroundColor(GRAY); // gray
                bt.setSelectedAnswer(false);
            } else {
                bt.setBackgroundColor(YELLOW); //yellow
                bt.setSelectedAnswer(true);
            }
        }
    }
}
