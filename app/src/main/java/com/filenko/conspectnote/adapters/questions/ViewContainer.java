package com.filenko.conspectnote.adapters.questions;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.filenko.conspectnote.R;
import com.filenko.conspectnote.db.QuestionDataBase;
import com.filenko.conspectnote.model.Question;

public abstract class ViewContainer extends RecyclerView.ViewHolder {
    private final QuestionDataBase db;
    private final Context ctx;
    final ImageView buttonDelete;
    private Question question;

    protected final EditText questionTitle;
    protected final ImageButton btnSaveQuestion;

    public ViewContainer(View view, QuestionDataBase db, Context ctx) {
        super(view);
        questionTitle = view.findViewById(R.id.item_question_title);
        btnSaveQuestion = view.findViewById(R.id.btnSaveQuestion);
        buttonDelete = view.findViewById(R.id.buttonDeleteQuestion);
        this.db = db;
        this.ctx = ctx;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Question getQuestion() {
        return this.question;
    }

    public static boolean checkQuestion(String questionTitle) {
        return questionTitle.length()>0;
    }

    protected void saveOrUpdateQuestion (Question question) {
        if(ViewContainer.checkQuestion(question.getTitle())) {
            int index = db.saveQuestion(question, null);
            if(question.getId()==0) { question.setId(index);}
        } else {
            Toast toast = Toast.makeText(ctx,"Нельзя сохранить пустой вопрос!", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    protected void btnEnabled (ImageButton btn , boolean enb) {
        if(enb) {
            btn.setEnabled(true);
            btn.setImageAlpha(255);
        } else {
            btn.setEnabled(false);
            btn.setImageAlpha(75);
        }
    }
}
