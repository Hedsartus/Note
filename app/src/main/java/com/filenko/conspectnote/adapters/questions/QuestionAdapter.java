package com.filenko.conspectnote.adapters.questions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;

import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.filenko.conspectnote.R;
import com.filenko.conspectnote.adapters.AnswerAdapter;
import com.filenko.conspectnote.db.QuestionDataBase;
import com.filenko.conspectnote.model.Answer;
import com.filenko.conspectnote.model.Question;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class QuestionAdapter extends RecyclerSwipeAdapter<ViewContainer> {
    private final QuestionDataBase db;
    private final Context ctx;
    private final LayoutInflater lInflater;
    private final ArrayList<Question> objects = new ArrayList<>();
    private final int idNote;

    public QuestionAdapter(QuestionDataBase db, Context ctx, int idNote) {
        this.db = db;
        this.ctx = ctx;
        lInflater = LayoutInflater.from(ctx);
        this.idNote = idNote;

        if (this.idNote > 0) loadQuestionData(this.idNote);

    }

    @SuppressLint("NotifyDataSetChanged")
    public void addNewQuestion(int type) {
        Question q = new Question();
        q.setType(type);
        q.setIdNote(this.idNote);
        this.objects.add(q);
        notifyDataSetChanged();
    }

    public void loadQuestionData(int idNote) {
        this.objects.clear();

        SQLiteDatabase database = this.db.getConnection().getWritableDatabase();
        try (Cursor query = database.rawQuery(
                "SELECT * FROM QUESTIONS WHERE idnote = " + idNote + ";", null)) {
            while (query.moveToNext()) {
                this.objects.add(
                        new Question(
                                query.getInt(0),
                                query.getInt(1),
                                query.getInt(2),
                                query.getString(3),
                                query.getInt(4))
                );
            }
        } finally {
            addAnswersToQuestion();
        }
    }

    private void addAnswersToQuestion() {
        SQLiteDatabase database = this.db.getConnection().getWritableDatabase();
        for (Question q : this.objects) {
            if(q.getType() == 1) {
                loadAnswers(q, database);
            }
        }
        database.close();
    }

    private void loadAnswers(Question question, SQLiteDatabase database) {
        try (Cursor query = database.rawQuery(
                "SELECT * FROM ANSWER WHERE idquestion = " + question.getId() + ";", null)) {

            while (query.moveToNext()) {
                question.addAnswer(
                        new Answer(
                                query.getInt(0),
                                query.getInt(1),
                                query.getString(2),
                                query.getInt(3))
                );
            }

        }
    }


    @NotNull
    @Override
    public ViewContainer onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewContainer viewContainer = null;
        switch (viewType) {
            case 1:
                View view = this.lInflater.inflate(R.layout.item_question, parent, false);
                ViewHolderMultiple viewHolderMultiple = new ViewHolderMultiple(view, db, ctx);
                viewHolderMultiple.recyclerViewSection.setAdapter(
                        new AnswerAdapter(db.getAnswerDataBase(), ctx));
                viewContainer = viewHolderMultiple;
                break;
            case 2:
                View view1 = this.lInflater.inflate(R.layout.item_question_single, parent, false);
                viewContainer = new ViewHolderSingle(view1, db, ctx);
                break;
        }

        return Objects.requireNonNull(viewContainer);
    }

    @Override
    public int getItemViewType(int position) {
        return this.objects.get(position).getType();
    }

    @Override
    public void onBindViewHolder(ViewContainer holder, int position) {
        Question question = objects.get(position);
        switch (holder.getItemViewType()) {
            case 1:
                ViewHolderMultiple viewHolderMultiple = (ViewHolderMultiple) holder;
                viewHolderMultiple.addQuestion(question);

                AnswerAdapter answerAdapter = (AnswerAdapter) viewHolderMultiple.recyclerViewSection.getAdapter();
                Objects.requireNonNull(answerAdapter).setQuestion(question);
                break;

            case 2:
                ViewHolderSingle viewHolderSingle = (ViewHolderSingle) holder;
                viewHolderSingle.addQuestion(question);
                break;
        }

        holder.buttonDelete.setOnClickListener(view -> {
            AlertDialog diaBox = askDelete(holder, position);
            diaBox.show();
        });
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.layoutQuestionItem;
    }

    @SuppressLint("NotifyDataSetChanged")
    private AlertDialog askDelete(ViewContainer holder, int position) {
        return new AlertDialog.Builder(this.ctx)
                .setTitle("Удаление вопроса")
                .setMessage("Удалить вопрос с ответами?")
                .setIcon(R.drawable.delete)
                .setPositiveButton("Да", (dialog, whichButton) -> {
                    int index = holder.getQuestion().getId();
                    if (index > 0) {
                        if (this.db.deleteQuestion(index, null)) {
                            this.objects.remove(position);
                            this.notifyDataSetChanged();
                        }
                    }
                    dialog.dismiss();
                }).setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .create();
    }
}
