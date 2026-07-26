package com.ensemblu.axiom.spec.database.materializer;

import com.ensemblu.axiom.api.Axiom;
import com.ensemblu.axiom.core.data_structure.map.PersistentMap;

public final class AxiomRegistry<E extends Enum<E>> {
    private final PersistentMap<String, E> registry;

    public AxiomRegistry(Class<E> enumClass) {
        var  map = Axiom.Data.<String, E>emptyMap();

        for (final var constant : enumClass.getEnumConstants()) {
            map = map.put(constant.name(), constant);
        }

        this.registry = map;
    }

    public E get(String typeName) {
        final var constant = registry.get(typeName);

        if (constant == null) {
            throw new IllegalArgumentException("DataType Breach: '" + typeName + "' is not supported.");
        }

        return constant;
    }
}