package com.ensemblu.axiom.spec.database.binder;

import com.ensemblu.axiom.core.foundation.Nothing;
import com.ensemblu.axiom.core.validation.Result;
import com.ensemblu.axiom.spec.database.contract.AxiomProtocol;

import java.util.Date;

public interface AxiomBinder {
    Result<Nothing> bindString(int index, String val);
    Result<Nothing> bindInteger(int index, Integer val);
    Result<Nothing> bindLong(int index, Long val);
    Result<Nothing> bindDouble(int index, Double val);
    Result<Nothing> bindBoolean(int index, Boolean val);
    Result<Nothing> bindTimestamp(int index, Date val);
    Result<Nothing> bindNull(int index, AxiomProtocol protocol);
}