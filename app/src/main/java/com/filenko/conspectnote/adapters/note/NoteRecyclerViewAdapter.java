package com.filenko.conspectnote.adapters.note;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.filenko.conspectnote.R;
import com.filenko.conspectnote.db.NoteDataBase;
import com.filenko.conspectnote.model.Note;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class NoteRecyclerViewAdapter extends RecyclerView.Adapter<NoteRecyclerViewAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {
    private final NoteDataBase dataBase;
    private final LayoutInflater lInflater;
    private final List<Note> objects;
    public OnClickListener onClickListener;
    private Note rootNote;

    public NoteRecyclerViewAdapter(NoteDataBase db, Context ctx) {
        this.dataBase = db;
        this.lInflater = LayoutInflater.from(ctx);
        this.rootNote = new Note();
        this.objects = this.dataBase.getRootNotes();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(objects, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    // Обновление позиций элементов в базе данных
    public void updateItemPositionsInDatabase() {
        for (int i = 0; i < objects.size(); i++) {
            Note note = objects.get(i);
            // Обновить позицию элемента в базе данных
            dataBase.updateNotePosition(note, i, null);
        }
    }

    public interface OnClickListener {
        void onOpenNoteViewClick(View view, int position);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    public Note getItem(int position) {
        return this.objects.get(position);
    }

    public Note getRootNote() {
        return this.rootNote;
    }

    public void setRootNote(Note note) {
        this.rootNote = note;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = lInflater.inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = this.objects.get(position);
        holder.noteName.setText(note.getTitle());

        holder.layoutView.setOnClickListener(v -> onClickListener.onOpenNoteViewClick(v, position));
    }

    @Override
    public int getItemCount() {
        return this.objects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        public final ImageView itemImageViewIcon;
        public final TextView noteName;
        public final LinearLayout layoutView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemImageViewIcon = itemView.findViewById(R.id.item_image_view_icon);
            this.noteName = itemView.findViewById(R.id.noteName);
            this.layoutView = itemView.findViewById(R.id.layoutView);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addNote(Note note) {
        this.objects.add(getItemCount(), note);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void getNotesByIdParent(int id) {
        this.dataBase.getNotesByIdParent(id, this.objects);
        notifyDataSetChanged();
    }

    public void getNoteById(int id) {
        this.dataBase.getNoteById(id, this.rootNote);
    }


}
