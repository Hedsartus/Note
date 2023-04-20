package com.filenko.conspectnote.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.filenko.conspectnote.R;
import com.filenko.conspectnote.adapters.questions.QuestionAdapter;
import com.filenko.conspectnote.db.DataBaseConnection;
import com.filenko.conspectnote.db.QuestionDataBase;

import java.util.Objects;

public class EditQuestion extends AppCompatActivity {
    private QuestionAdapter adapter;
    private int idNote;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_question);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle("Вопросы");
        QuestionDataBase db = new QuestionDataBase(new DataBaseConnection(this));

        Bundle bundle = getIntent().getExtras();
        if(bundle!= null && bundle.getInt("idnote") > 0) {
            idNote = bundle.getInt("idnote");
        }


        DividerItemDecoration itemDecorator = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.divider2)));

        RecyclerView listViewQuestion = findViewById(R.id.rvQuestions);
        listViewQuestion.setLayoutManager(new LinearLayoutManager(this));
        listViewQuestion.addItemDecoration(itemDecorator);

        this.adapter = new QuestionAdapter(db,this, idNote);
        listViewQuestion.setAdapter(this.adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Добавить вопрос");
        menu.add(0, 2, 0, "Добавить вопрос да/нет");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1 : this.adapter.addNewQuestion(1); break;
            case 2 : this.adapter.addNewQuestion(2); break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}
