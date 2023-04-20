package com.filenko.conspectnote.model;

import androidx.annotation.NonNull;

public interface ICloneable<T> {
    @NonNull
    T clone();
    void clone(T obj);
}
