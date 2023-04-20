package com.filenko.conspectnote.common.CircularLinkedList;

public class Node<T> {
    private final T value;
    private Node<T> next;

    public Node(T value) {
        this.value = value;
    }

    public boolean hasNext() {
        return next != null;
    }

    public Node<T> getNext() {
        return next;
    }

    public void setNext(Node<T> node) {
        this.next = node;
    }

    public T getValue() {
        return value;
    }
}
