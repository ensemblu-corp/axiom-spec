package com.ensemblu.axiom.spec.database.materializer;

import com.ensemblu.axiom.api.TargetNavigator;
import com.ensemblu.axiom.core.data_structure.list.PersistentList;

public interface ResultRow {
    TargetNavigator navigate(String column);

    PersistentList<String> columns();
}