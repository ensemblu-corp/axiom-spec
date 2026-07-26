package com.ensemblu.axiom.spec.database.binder;

import com.ensemblu.axiom.core.data_structure.map.PersistentMap;
import com.ensemblu.axiom.spec.database.contract.AxiomProtocol;
import com.ensemblu.axiom.spec.parser.SqlParser;

public interface IngressBinder {

     static void apply(AxiomBinder binder, SqlParser.ExecutionPlan plan,
                             PersistentMap<String, AxiomProtocol> contract,
                             PersistentMap<String, Object> data) {

        plan.indexToKey().forEach((index, key) -> {
            final var paramIndex = index + 1;
            final var protocol = contract.get(key);
            final var val = data.get(key);

            if (val != null) {
                final var toBind = (protocol == AxiomProtocol.LONG && val instanceof Integer)
                        ? ((Integer) val).longValue() : val;
                protocol.getSetter().set(binder, paramIndex, toBind);
            } else {
                binder.bindNull(paramIndex, protocol);
            }
        });
    }


}
