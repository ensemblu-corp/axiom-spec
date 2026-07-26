package com.ensemblu.axiom.spec.database.materializer;

import com.ensemblu.axiom.core.data_structure.map.PersistentMap;
import com.ensemblu.axiom.core.foundation.Dop;

public interface RowMaterializer {
    static PersistentMap<String, Object> materialize(ResultRow row) {
        return Dop.<String, Object>projectMap()//
                .ingest(row.columns()) //
                .extractKey(col -> col) //
                .extractValue(col -> row.navigate(col).toObjectVal()) //
                .deploy();
    }
}