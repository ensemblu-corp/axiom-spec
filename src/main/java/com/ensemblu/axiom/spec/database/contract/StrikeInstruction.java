package com.ensemblu.axiom.spec.database.contract;

import com.ensemblu.axiom.api.Axiom;
import com.ensemblu.axiom.core.data_structure.map.PersistentMap;

public record StrikeInstruction(
        String sql,//
        PersistentMap<String, AxiomProtocol> types,//
        PersistentMap<String, Object> data//
) {

    @FunctionalInterface
    public interface TypedStrike {
        DataBinder withContract(PersistentMap<String, AxiomProtocol> types);
    }

    @FunctionalInterface
    public interface DataBinder {
        StrikeInstruction withData(PersistentMap<String, Object> data);
    }

    public static TypedStrike dynamic(String sql) {
        return types -> data ->
                new StrikeInstruction(sql, types, data);
    }

    public static StrikeInstruction shot(String sql) {
        return new StrikeInstruction(sql, Axiom.Data.emptyMap(), Axiom.Data.emptyMap());
    }

    public StrikeInstruction withSql(String newSql) {
        return new StrikeInstruction(newSql, this.types, this.data);
    }
}