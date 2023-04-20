package com.filenko.conspectnote.common;

import com.filenko.conspectnote.model.ICloneable;

public class ChangeCheck<T extends ICloneable<T>> {
    private final T objectObservable;

    public ChangeCheck(T objectCheckChange) {
        this.objectObservable = objectCheckChange.clone();
    }

    public boolean isChange(T objectCheckChange) {
        return !this.objectObservable.equals(objectCheckChange);
    }

    public void setChange(T objectCheckChange) {
        this.objectObservable.clone(objectCheckChange);
    }
}
