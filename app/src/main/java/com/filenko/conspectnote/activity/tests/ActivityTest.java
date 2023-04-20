package com.filenko.conspectnote.activity.tests;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.filenko.conspectnote.R;
import com.filenko.conspectnote.db.DataBaseConnection;
import com.filenko.conspectnote.db.QuestionDataBase;
import com.filenko.conspectnote.model.Answer;
import com.filenko.conspectnote.model.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ActivityTest extends AppCompatActivity {
    private QuestionDataBase questionDataBase;
    private final List<Question> questions = new ArrayList<>();
    private int count;
    private LinearLayout layoutAnswersButton;
    private Button btnNextQuestion;
    private final List<ButtonTest> buttonTestList = new ArrayList<>();
    private int errorAnswer = 0;
    private TextView tvTestQuestion;
    private boolean isNext = false;
    private LinearLayout.LayoutParams layoutParams;

    private final int GREEN = Color.rgb(130, 202, 113);
    private final int RED = Color.rgb(243, 99, 113);


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityProperty();
        setInitialsVariable();

        this.btnNextQuestion.setOnClickListener(v -> nextButtonClick());

        Bundle bundle = getIntent().getExtras();
        catchBundle(bundle);
    }

    private void nextButtonClick() {
        if (!isNext) {
            checkAnswer();
        } else {
            this.count++;

            if (this.questions.size() > this.count) {
                createAnswersButtons(this.questions.get(count));
                setTitle("Вопрос " + (this.count + 1) + " из " + this.questions.size());
                setEnableButton(true);
                this.btnNextQuestion.setText("Проверить");
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Ошибок: " + this.errorAnswer + "!",
                        Toast.LENGTH_LONG);
                //toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                this.btnNextQuestion.setVisibility(View.GONE);
                //Log.d("------", "Ошибок: " + this.errorAnswer + "!");
            }

        }
    }

    private void checkAnswer() {
        setEnableButton(false);

        boolean isError = false;

        for (ButtonTest bt : this.buttonTestList) {

            if (bt.getCorrect() && bt.isSelectAnswer()) {
                bt.setBackgroundColor(GREEN); //green
            }
            if (bt.getCorrect() && !bt.isSelectAnswer()) {
                bt.setTextColor(RED);
                isError = true;
            }
            if (!bt.getCorrect() && bt.isSelectAnswer()) {
                bt.setBackgroundColor(RED); //red
                isError = false;
            }
        }
        if (isError) {
            this.errorAnswer++;
        }
        this.btnNextQuestion.setText("Дальше");
    }

    private void setInitialsVariable() {
        this.questionDataBase = new QuestionDataBase(new DataBaseConnection(this));

        this.layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 7);

        this.count = 0;

        this.layoutAnswersButton = findViewById(R.id.createButtonAnswerLayout);
        this.tvTestQuestion = findViewById(R.id.tvTestQuestion);

        this.btnNextQuestion = findViewById(R.id.btnNextQuestion);
    }

    private void setActivityProperty() {
        setContentView(R.layout.activity_test);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void setEnableButton(boolean b) {
        for (ButtonTest bt : this.buttonTestList) {
            bt.setEnabled(b);
        }
        this.isNext = !b;

    }

    private void createAnswersButtons(Question question) {
        this.layoutAnswersButton.removeAllViews();
        this.buttonTestList.clear();
        this.tvTestQuestion.setText(question.getTitle());

        switch (question.getType()) {
            case 1:
                createMultipleButtons(question);
                break;
            case 2:
                createYesNoButtons(question);
                break;
        }

    }

    private void createMultipleButtons(Question question) {
        List<Answer> answers = question.getListAnswers();
        Collections.shuffle(answers);
        for (Answer anr : answers) {
            ButtonTest buttonTest = new ButtonTest(this, anr.getAnswer());
            buttonTest.setCorrect(anr.isCorrect());
            buttonTest.setOnClickListener(new ButtonTestClickListener(question, this.buttonTestList));
            this.layoutAnswersButton.addView(buttonTest, layoutParams);
            this.buttonTestList.add(buttonTest);
        }
    }

    private void createYesNoButtons(Question question) {
        ButtonTest buttonTest = new ButtonTest(this, "Да");
        ButtonTest buttonTest1 = new ButtonTest(this, "Нет");

        if(question.getCorrect()) {
            buttonTest.setCorrect(true);
        } else {
            buttonTest1.setCorrect(true);
        }

        this.layoutAnswersButton.addView(buttonTest, layoutParams);
        this.layoutAnswersButton.addView(buttonTest1, layoutParams);

        buttonTest.setOnClickListener(new ButtonTestClickListener(question, this.buttonTestList));
        buttonTest1.setOnClickListener(new ButtonTestClickListener(question, this.buttonTestList));
        this.buttonTestList.add(buttonTest1);
        this.buttonTestList.add(buttonTest);
    }


    private void checkQuestionList(List<Question> list) {
        List<Question> errorQuestion = new ArrayList<>();
        for (Question q : list) {
            if (q.getType() == 1 && q.getListAnswers().size() < 2) {
                errorQuestion.add(q);
            }
        }

        for (Question q : errorQuestion) {
            list.remove(q);
        }
    }
    private void catchBundle(Bundle bundle) {
        if (bundle != null && bundle.getInt("idnote") > 0) {
            int idNode = bundle.getInt("idnote");
            this.questionDataBase.loadTreeQuestions(idNode, this.questions);
            this.questionDataBase.getAnswerDataBase().setAnswersToQuestions(this.questions);

            checkQuestionList(this.questions);
            if (this.questions.size() > 0) {
                Collections.shuffle(this.questions);
                createAnswersButtons(this.questions.get(count));
                setTitle("Вопрос 1 из " + this.questions.size());
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Вопросов для теста к сожалению нет!",
                        Toast.LENGTH_LONG);
                toast.show();
                finish();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

