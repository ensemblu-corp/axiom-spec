package com.ensemblu.axiom.spec.database.integrity;

import com.ensemblu.axiom.api.Axiom;
import com.ensemblu.axiom.core.data_structure.map.PersistentMap;
import com.ensemblu.axiom.core.foundation.Nothing;
import com.ensemblu.axiom.spec.database.contract.AxiomProtocol;
import com.ensemblu.axiom.spec.parser.SqlParser;

public interface IngressIntegrity {

    static Nothing verifyAlignment(
            SqlParser.ExecutionPlan plan,
            PersistentMap<String, AxiomProtocol> types,
            PersistentMap<String, Object> data) {

        Axiom.Check.that(types.size())//
                .is(s -> data.size() == s, "Ingress Integrity Breach: Contract/Data size mismatch (Contract: " + types.size() + ", Data: " + data.size() + ")")//
                .will()//
                .thenApprovedOrElseThrowException();

        plan.indexToKey()//
                .forEach((index, key) -> {//
            Axiom.Check.that(key)//
                    .isNonNull("Ingress Integrity Breach: SQL template corruption at index " + index)//
                    .andIsNot(k -> types.get(k) == null, "Ingress Integrity Breach: Type missing for SQL key [" + key + "]")//
                    .andIsNot(k -> data.get(k) == null, "Ingress Integrity Breach: Data missing for SQL key [" + key + "]")//
                    .will()//
                    .thenApprovedOrElseThrowException();
        });

        return Nothing.INSTANCE;
    }
}
