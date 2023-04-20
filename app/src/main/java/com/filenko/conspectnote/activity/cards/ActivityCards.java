package com.filenko.conspectnote.activity.cards;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.filenko.conspectnote.R;
import com.filenko.conspectnote.adapters.ViewPagerAdapter;
import com.filenko.conspectnote.db.NoteDataBase;
import com.filenko.conspectnote.model.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivityCards extends AppCompatActivity {
    private NoteDataBase db;
    private final List<Note> objectsNote = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityProperty();

        Bundle bundle = getIntent().getExtras();
        catchBundle(bundle);

        ViewPager2 pager = findViewById(R.id.pager);
        FragmentStateAdapter pageAdapter = new ViewPagerAdapter(this, objectsNote);
        pager.setAdapter(pageAdapter);

    }

    private void catchBundle(Bundle bundle) {
        if (bundle != null && bundle.getInt("idnote") > 0) {
            int idNode = bundle.getInt("idnote");
            this.db.loadTreeNotesByIdParent(objectsNote, idNode);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setActivityProperty() {
        setContentView(R.layout.activity_cards);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        this.db = new NoteDataBase(this);
    }
}
