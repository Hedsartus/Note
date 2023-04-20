package com.filenko.conspectnote.common.CircularLinkedList;

import jp.wasabeef.richeditor.RichEditor;

@FunctionalInterface
public interface CommandNote {
    void command(RichEditor editor);

}
