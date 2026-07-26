package com.ensemblu.axiom.spec.parser;

import com.ensemblu.axiom.api.Axiom;
import com.ensemblu.axiom.core.data_structure.list.PersistentList;
import com.ensemblu.axiom.core.data_structure.map.PersistentMap;

/**
 * 🏛️ SqlParser: The Template Architect.
 * Defines the law for transforming Axiom templates into database strikes.
 */
public interface SqlParser {

    String SIGNAL = ":java.";

    static ExecutionPlan forge(String template) {
        final var sql = new StringBuilder();
        var indexToKey = Axiom.Data.<Integer, String>emptyMap().asTransient() ;

        var  paramCount = 0;
        var pos = 0;

        while (pos < template.length()) {
            if (template.startsWith(SIGNAL, pos)) {
                pos += SIGNAL.length();
                final var start = pos;
                while (pos < template.length() && Character.isJavaIdentifierPart(template.charAt(pos))) {
                    pos++;
                }
                final var key = template.substring(start, pos);
                indexToKey = indexToKey.put(paramCount++, key);
                sql.append("?");
            } else {
                sql.append(template.charAt(pos++));
            }
        }
        return new ExecutionPlan(sql.toString(), indexToKey.freeze());
    }

    record ExecutionPlan(String sql, PersistentMap<Integer, String> indexToKey) {}
}