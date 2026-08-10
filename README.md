
# 📐 Axiom Spec

![Version](https://img.shields.io/badge/version-2.0.0-blue)
![Java](https://img.shields.io/badge/Java-26-orange)
![Depends](https://img.shields.io/badge/depends%20on-axiom-informational)
![License](https://img.shields.io/badge/license-Limited%20Commercial-red)

**Specification layer: parsers, emitters, database contracts, and materializers.**

`axiom-spec` is the bridge between the outside world (CSV, JSON, SQL templates, JDBC/Vert.x rows) and Axiom’s immutable core. It owns the **byte-oriented** CSV and JSON parsers, the production-grade **JSON emitter**, SQL template forging, and type-safe database binding protocols.

---

## What it does

| Capability | Entry point |
|------------|-------------|
| CSV row parsing | `CsvRowParser.takeLine(byte[])` / `scanLine(byte[])` |
| JSON parsing | `JsonParser.take(byte[])` |
| JSON emission | `JsonEmitter.emit(Object)` |
| SQL template → plan | `SqlParser.forge(String)` |
| Row → `PersistentMap` | `RowMaterializer` |
| Typed DB binding | `AxiomProtocol` + `AxiomRegistry` + `IngressBinder` |

Zero reflection. Zero annotations. Character / byte scanners instead of heavy deserializers.

---

## Requirements

- **Java 26**
- [`axiom`](https://github.com/ensemblu-corp/axiom) `2.0.0`

---

## Installation

**Maven**

```xml
<dependency>
    <groupId>com.ensemblu</groupId>
    <artifactId>axiom-spec</artifactId>
    <version>2.0.0</version>
</dependency>
```

**Gradle**

```groovy
implementation("com.ensemblu:axiom-spec:2.0.0")
```

---

## Quick start (2.0.0 APIs)

### CSV

```java
import com.ensemblu.axiom.spec.parser.CsvRowParser;
import java.nio.charset.StandardCharsets;

byte[] line = "Ada,36,London".getBytes(StandardCharsets.UTF_8);

var row = CsvRowParser.takeLine(line)
    .basedOnHeaders("name", "age", "city");

// or low-level scan
var cells = CsvRowParser.scanLine(line);
```

### JSON parse

```java
import com.ensemblu.axiom.spec.parser.JsonParser;

byte[] json = "{\"name\":\"Ada\",\"age\":36}".getBytes(StandardCharsets.UTF_8);

var data = JsonParser.take(json)
    .openBuffer()
    .ensureRootIsObject()
    .parseObject();
```

### JSON emit (replacement for removed `Dop.toJson`)

```java
import com.ensemblu.axiom.spec.parser.JsonEmitter;

String json = JsonEmitter.emit(myPersistentMap);
// Correctly handles nested maps/lists, escaping, null, numbers, booleans
```

### SQL template

```java
import com.ensemblu.axiom.spec.parser.SqlParser;

var plan = SqlParser.forge(
    "INSERT INTO users (name, age) VALUES (:java.name, :java.age)"
);
// plan.sql()        → "INSERT INTO users (name, age) VALUES (?, ?)"
// plan.indexToKey() → {0 → "name", 1 → "age"}
```

### Database handshake

```java
IngressBinder.apply(binder, plan, contractMap, dataMap);
var materialized = RowMaterializer.materialize(row);
```

> [!IMPORTANT]
> **Breaking changes from 1.0.0**  
> - `CsvRowParser.takeLine` / `scanLine` and `JsonParser.take` now take **`byte[]`**, not `String`.  
> - `Dop.toJson` (from `axiom`) was removed — always use **`JsonEmitter.emit`**.

---

## Package structure

```
com.ensemblu.axiom.spec
├── parser
│   ├── CsvRowParser.java      // takeLine(byte[]) / scanLine(byte[])
│   ├── JsonParser.java        // take(byte[])
│   ├── JsonEmitter.java       // emit(Object) → String
│   └── SqlParser.java         // forge(String) → ExecutionPlan
└── database
    ├── binder/                // IngressBinder, type setters
    └── materializer/
        ├── RowMaterializer.java
        ├── ResultRow.java
        ├── AxiomProtocol.java
        ├── AxiomRegistry.java
        └── DefaultDataContract.java
```

---

## Structural guardrails

- **CSV** — empty / null / whitespace-only input is a contract violation at the boundary.
- **SQL** — `:java.key` signals are replaced with `?` placeholders; parameter order is captured in the plan.
- **Protocol** — `AxiomProtocol` maps Java types to database-native setters without reflection.
- **Registry** — `AxiomRegistry` resolves protocol names at runtime without class scanning.

---

## How it fits

```text
CSV / JSON bytes ──► CsvRowParser / JsonParser ──► PersistentMap
                                                      │
SQL template ──────► SqlParser.forge ─────────────────┼──► binders / materializers
                                                      │
PersistentMap ─────► JsonEmitter.emit ──────────────► JSON string
```

Warp modules (`axiom-warp-jdbc`, `axiom-warp-reactive`) depend on this layer for ingestion and result shaping.

---

## Related modules

| Module | Relationship |
|--------|----------------|
| `axiom` | Core data structures & `Result` |
| `axiom-warp-jdbc` | Uses parsers + materializers for blocking JDBC |
| `axiom-warp-reactive` | Uses parsers + materializers for Vert.x pipelines |

---

## Legal

Limited Commercial License — free for evaluation, testing, and non-commercial development.  
Commercial or production use requires a paid annual contract from Ensemblu Corp.

See `LICENSE.md`. Contact: **contact@ensemblu.com**
