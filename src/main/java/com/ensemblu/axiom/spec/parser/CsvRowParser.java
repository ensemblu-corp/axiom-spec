package com.ensemblu.axiom.spec.parser;

import com.ensemblu.axiom.api.Axiom;
import com.ensemblu.axiom.core.validation.If;
import com.ensemblu.axiom.core.data_structure.list.PersistentList;
import com.ensemblu.axiom.core.data_structure.map.PersistentMap;

/**
 * Axiom CSV Row Parser: A high-precision character scanner.
 * Designed to feed the MasterGuard with raw PersistentMaps.
 */
public interface CsvRowParser {

    interface AddHeaders {
        default PersistentMap<String, Object> basedOnHeaders(String... headers) {
            if (headers == null || headers.length == 0) {
                throw new RuntimeException("Axiom Contract Violation: CSV Varargs headers cannot be null or empty.");
            }

            return basedOnHeaders(Axiom.Data.list(headers));
        }

        PersistentMap<String, Object> basedOnHeaders(PersistentList<String> headers);
    }


    static AddHeaders takeLine(String line) {
        If.givenObject(line)//
                .isNonNull("CSV Line")//
                .andIsNot(String::isEmpty, "CSV Line cannot be empty")//
                .andIsNot(String::isBlank, "CSV Line cannot be blank (whitespace only)")//
                .will()//
                .getResult()//
                .mapFailure(e ->"Contract Violation:" + Axiom.Check.success(e.getMessage()).getOrElse("")  )//
                .getOrThrow()//
        ;

        return headers -> {
            if (headers == null || headers.isEmpty()) {
                throw new RuntimeException("Axiom Contract Violation: Headers are mandatory for mapping.");
            }
            var map = Axiom.Data.<String, Object>emptyMap().asTransient();
            final var values = scanLine(line);

            for (int i = 0; i < headers.size(); i++) {
                final var key = headers.get(i);
                // If the row is shorter than the header list, we provide null (MasterGuard handles optionality)
                final var value = (i < values.size()) ? values.get(i) : null;
                map = map.put(key, value);
            }
            return map.freeze();
        };
    }

    /**
     * Character-level scanner that respects quoted strings.
     * "AX-1,00",50.0 -> ["AX-1,00", "50.0"]
     */
     static PersistentList<String> scanLine(String line) {
        var values = Axiom.Data.<String>emptyList().asTransient();
        if (line == null || line.isEmpty()) return values;

        var sb = new StringBuilder();
        var inQuotes = false;
        final var chars = line.toCharArray();

        for (final char c : chars) {
            if (c == '\"') {
                inQuotes = !inQuotes; // Toggle quote state
            } else if (c == ',' && !inQuotes) {
                values = values.append(sb.toString().trim());
                sb.setLength(0); // Reset buffer
            } else {
                sb.append(c);
            }
        }
        // Append the last value
        values = values.append(sb.toString().trim());
        return values.freeze();
    }
}