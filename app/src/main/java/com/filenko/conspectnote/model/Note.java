package com.filenko.conspectnote.model;


import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Note implements Comparable<Note>, ICloneable<Note> {
    private int id;
    private int position;
    private int parent;
    private String title;
    private String html;
    private final List<Note> listChild = new ArrayList<>();
    private final List<Question> questionsList = new ArrayList<>();

    public Note(int id, int position, int parent, String title, String html) {
        this.id = id;
        this.parent = parent;
        this.position = position;
        this.title = title;
        this.html = html;
    }

    public Note() {
        this.id = 0;
        this.parent = 0;
        this.position = 0;
        this.title = null;
        this.html = null;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public int getId() {
        return id;
    }

    public int getParent() {
        return parent;
    }

    public String getTitle() {
        return title;
    }

    public String getHtml() {
        return html;
    }

    public void addChild(Note note) {
        note.setParent(this.getId());
        this.listChild.add(note);
    }

    public List<Question> getListQuestion() {
        return this.questionsList;
    }

    public void addChild(Question question) {
        question.setIdNote(this.getId());
        this.questionsList.add(question);
    }

    public List<Note> getListChild() {
        return this.listChild;
    }

    @NotNull
    @Override
    public String toString() {
        return "Name = " + getTitle() + ", id = " + getId() + ", parent = " + getParent();
    }

    public void clear() {
        this.id = 0;
        this.parent = 0;
        this.title = null;
        this.html = null;
    }

    @Override
    public int compareTo(Note o) {
        return this.getTitle().compareTo(o.getTitle());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return id == note.id && position == note.position && parent == note.parent
                && Objects.equals(title, note.title) && Objects.equals(html, note.html);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, position, parent, title, html);
    }

    @NonNull
    @Override
    public Note clone() {
        Note clone = new Note();
        clone.setId(this.getId());
        clone.setPosition(this.getPosition());
        clone.setParent(this.getParent());
        clone.setTitle(this.getTitle());
        clone.setHtml(this.getHtml());
        return clone;
    }

    @Override
    public void clone(Note obj) {
        this.setId(obj.getId());
        this.setPosition(obj.getPosition());
        this.setParent(obj.getParent());
        this.setTitle(obj.getTitle());
        this.setHtml(obj.getHtml());
    }
}
