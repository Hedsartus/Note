package com.filenko.conspectnote.common;

import com.filenko.conspectnote.common.CircularLinkedList.CommandNote;

import java.util.ArrayList;

public class CircularArrayList {
    ArrayList<CommandNote> list = new ArrayList<>();
    private int count = 0;

    public void add(CommandNote command) {
        list.add(command);
    }

    public CommandNote get() {
        count++;
        if(count == list.size()) {
            count = 0;
        }
        return list.get(count);

    }
}
