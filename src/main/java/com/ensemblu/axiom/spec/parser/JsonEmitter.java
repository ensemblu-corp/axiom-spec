package com.ensemblu.axiom.spec.parser;

import com.ensemblu.axiom.core.data_structure.list.PersistentList;
import com.ensemblu.axiom.core.data_structure.map.PersistentMap;

public final class JsonEmitter {

    public static String emit(Object obj) {
        return switch (obj) {
            case null -> "null";
            case PersistentMap<?, ?> map -> emitObject(map);
            case PersistentList<?> list -> emitArray(list);
            case String s -> "\"" + escape(s) + "\"";
            case Boolean b -> obj.toString();
            case Number number -> obj.toString();
            default -> "\"" + escape(obj.toString()) + "\"";
        };
    }

    private static String emitObject(PersistentMap<?, ?> map) {
        final var sb = new StringBuilder("{");
        final boolean[] first = {true};
        
        map.forEach((k, v) -> {
            if (!first[0]) sb.append(",");
            first[0] = false;
            
            sb.append("\"").append(escape(String.valueOf(k))).append("\":");
            sb.append(emit(v));
        });
        
        return sb.append("}").toString();
    }

    private static String emitArray(PersistentList<?> list) {
        final var sb = new StringBuilder("[");
        final boolean[] first = {true};
        
        list.forEach(v -> {
            if (!first[0]) sb.append(",");
            first[0] = false;
            
            sb.append(emit(v));
        });
        
        return sb.append("]").toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}