package com.filenko.conspectnote.activity.cards;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.filenko.conspectnote.R;
import com.filenko.conspectnote.model.Note;

public class PageFragment extends Fragment {
    private final Note note;
    private final int positions;
    private final int size;

    public PageFragment(Note note, int positions, int size) {
        this.note = note;
        this.positions = positions;
        this.size = size;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.fragment_view_pager, null);

        TextView tvPage = view.findViewById(R.id.tvPage);
        tvPage.setText(note.getTitle());

        TextView tvNote = view.findViewById(R.id.tvNote);
        tvNote.setText(note.getHtml() != null ? Html.fromHtml(note.getHtml()) : "empty");

        ScrollView scrollView = view.findViewById(R.id.scrollViewNoteHtml);
        LinearLayout linearLayout = view.findViewById(R.id.layoutWrapNote);

        tvPage.setOnClickListener(v -> {
            tvPage.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        });
        linearLayout.setOnClickListener((v) -> {
            scrollView.setVisibility(View.GONE);
            tvPage.setVisibility(View.VISIBLE);
        });

        TextView tvInform = view.findViewById(R.id.tvInform);
        tvInform.setText((positions+1)+" из "+size);

        return view;
    }
}