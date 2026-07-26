package com.ensemblu.axiom.spec.database.contract;

import com.ensemblu.axiom.core.foundation.Nothing;
import com.ensemblu.axiom.core.validation.Result;
import com.ensemblu.axiom.spec.database.binder.AxiomBinder;

public enum AxiomProtocol {
    OPAQUE((b, i, v) -> b.bindString(i, (String) v)),// It uses bindString under the hood
    STRING((b, i, v) -> b.bindString(i, (String) v)),
    INTEGER((b, i, v) -> b.bindInteger(i, (Integer) v)),
    LONG((b, i, v) -> b.bindLong(i, (Long) v)),
    DOUBLE((b, i, v) -> b.bindDouble(i, (Double) v)),
    BOOLEAN((b, i, v) -> b.bindBoolean(i, (Boolean) v)),
    TIMESTAMP((b, i, v) -> b.bindTimestamp(i, (java.util.Date) v));

    private final BinderSetter setter;

    AxiomProtocol(BinderSetter setter) {
        this.setter = setter;
    }

    public BinderSetter getSetter() { return setter; }

    @FunctionalInterface
    public interface BinderSetter {
        Result<Nothing> set(AxiomBinder binder, int index, Object value);
    }
}