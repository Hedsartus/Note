package com.filenko.conspectnote.common.tree;

import com.filenko.conspectnote.model.Note;

import java.util.Comparator;

public class ComporatorNote implements Comparator<Note> {
    @Override
    public int compare(Note o1, Note o2) {
        if(o1.getParent()> o2.getParent()) {
            return 1;
        } else if(o1.getParent() < o2.getParent()) {
            return -1;
        } else if(o1.getId() > o2.getId()) {
            return 1;
        } else if (o1.getId() < o2.getId()) {
            return -1;
        }

        return o1.compareTo(o2);
    }
}
