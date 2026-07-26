package com.ensemblu.axiom.spec.database.materializer;

import com.ensemblu.axiom.api.Axiom;
import com.ensemblu.axiom.core.validation.Result;
import com.ensemblu.axiom.core.config.ConfigSource;
import com.ensemblu.axiom.core.data_structure.map.PersistentMap;

/**
 * ⚖️ Axiom Database Specification: The Sovereign Law.
 * Moved from the Jars to the Core to serve as the Single Source of Truth.
 */

public interface DefaultDataContract {

    static Result<PersistentMap<String, Object>> validate(ConfigSource config) {
        return config
                .targetField("engine.url")//
                .toStringResult()//
                .reject(u -> u.equalsIgnoreCase("null"), "Fake Pocket Breach: URL cannot be the literal string 'null'")//
                .flatMap(url -> config//
                        .targetField("engine.user")//
                        .toStringResult()//
                        .map(user -> {//
                            final var data = Axiom.Data.<String, Object>emptyMap()
                                    .put("engine.url", url)
                                    .put("engine.user", user)
                                    .put("engine.pool.max", getPoolMax(config));

                            // Optional password check
                            final var password = config//
                                    .targetField("engine.password")//
                                    .toStringResult()
                                    .getOrElse(() -> null);

                            return Axiom.Check.optional(password)//
                                    .map(p -> data.put("engine.password", p) )//
                                    .getOrElse(() -> data);
                        })
                );
    }

    private static int getPoolMax(ConfigSource config) {
        return config//
                .targetField("engine.pool.max")//
                .toIntResult()//
                .validate(max -> max > 0, "engine.pool.max must be positive number")//
                .validate(max -> max > 9, "engine.pool.max must be at least 10")//
                .getOrElse(() -> 10); //
    }
}