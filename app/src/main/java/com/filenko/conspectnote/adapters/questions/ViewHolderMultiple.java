package com.filenko.conspectnote.adapters.questions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.swipe.SwipeLayout;
import com.filenko.conspectnote.R;
import com.filenko.conspectnote.adapters.AnswerAdapter;
import com.filenko.conspectnote.db.QuestionDataBase;
import com.filenko.conspectnote.model.Question;

public class ViewHolderMultiple extends ViewContainer {
    final EditText questionTitle;
    final RecyclerView recyclerViewSection;
    final ToggleButton toggleButton;
    final ImageButton btnSaveQuestion;
    final LinearLayout linearLayout;
    final TextView teCountAnswers;
    final SwipeLayout layoutQuestionItem;
    final Button addNewAnswer;

    @SuppressLint("SetTextI18n")
    ViewHolderMultiple(View view, QuestionDataBase db, Context ctx){
        super(view, db, ctx);
        questionTitle = view.findViewById(R.id.item_question_title);
        btnSaveQuestion = view.findViewById(R.id.btnSaveQuestion);
        teCountAnswers = view.findViewById(R.id.teCountAnswers);
        recyclerViewSection = view.findViewById(R.id.rvAnswers);
        toggleButton = view.findViewById(R.id.btnSetViewPanelAnswer);

        addNewAnswer = view.findViewById(R.id.addNewAnswer);
        layoutQuestionItem = view.findViewById(R.id.layoutQuestionItem);
        linearLayout = view.findViewById(R.id.layoutRecyclerList);
        linearLayout.setVisibility(View.GONE);

        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                linearLayout.setVisibility(View.VISIBLE);
            } else {
                linearLayout.setVisibility(View.GONE);
            }
        });

        btnSaveQuestion.setOnClickListener(v-> {
            if(getQuestion() != null) {
                getQuestion().setTitle(questionTitle.getText().toString());
                saveOrUpdateQuestion (getQuestion());
                btnEnabled(btnSaveQuestion, false);
            }
        });

        addNewAnswer.setOnClickListener(v-> {
            if(getQuestion().getId() != 0) {
                AnswerAdapter adapter = (AnswerAdapter) recyclerViewSection.getAdapter();

                if(adapter != null) {
                    adapter.addNewAnswer();
                    this.teCountAnswers.setText("Ответов: " + getQuestion().getListAnswers().size());
                }
            } else {
                Toast toast = Toast.makeText(this.recyclerViewSection.getContext(),
                        "Сначала сохраните вопрос!!!", Toast.LENGTH_LONG);
                toast.show();
            }
        });

        questionTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnEnabled(btnSaveQuestion, s.length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    @SuppressLint("SetTextI18n")
    public void addQuestion(Question question) {
        setQuestion(question);
        this.questionTitle.setText(question.getTitle());
        this.teCountAnswers.setText("Ответов: " + getQuestion().getListAnswers().size());
        btnEnabled (btnSaveQuestion, false);
    }


}
