package com.filenko.conspectnote.adapters.questions;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;

import com.filenko.conspectnote.R;
import com.filenko.conspectnote.db.QuestionDataBase;
import com.filenko.conspectnote.model.Question;

public class ViewHolderSingle extends ViewContainer {
    final CheckBox checkBox;

    public ViewHolderSingle(View view, QuestionDataBase db, Context ctx) {
        super(view, db, ctx);
        checkBox = view.findViewById(R.id.checkBoxTrue);

        btnSaveQuestion.setOnClickListener(v-> {
            if(getQuestion()!= null) {
                getQuestion().setTitle(questionTitle.getText().toString());
                getQuestion().setCorrect(checkBox.isChecked());
                saveOrUpdateQuestion (getQuestion());
                btnEnabled(btnSaveQuestion, false);
            }
        });

        questionTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnEnabled(btnSaveQuestion, s.length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                btnEnabled(btnSaveQuestion, true));

    }

    public void addQuestion (Question question) {
        setQuestion(question);
        this.questionTitle.setText(question.getTitle());
        this.checkBox.setChecked(question.getCorrect());
        btnEnabled (btnSaveQuestion, false);
    }
}
