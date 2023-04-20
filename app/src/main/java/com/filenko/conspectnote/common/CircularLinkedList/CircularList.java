package com.filenko.conspectnote.common.CircularLinkedList;

public class CircularList<T>  {
    private Node<T> mHead;
    private Node<T> mCurrent;

    public CircularList() {
        mHead = null;
        mCurrent = null;
    }

    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (mHead == null) {
            mHead = newNode;
            mHead.next = mHead;
            mCurrent = mHead;
        } else {
            newNode.next = mHead;
            mCurrent.next = newNode;
            mCurrent = newNode;
        }
    }

    public T getCurrent() {
        if (mCurrent == null) {
            return null;
        }
        T data = mCurrent.data;
        mCurrent = mCurrent.next;
        return data;
    }

    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }
}