![Version](https://img.shields.io/badge/version-3.0.0-blue)
![Java](https://img.shields.io/badge/Java-26-orange)
![Depends](https://img.shields.io/badge/depends%20on-axiom-informational)
![License](https://img.shields.io/badge/license-Limited%20Commercial-red)

# Axiom Spec (`axiom-spec`)

> The standard contract layer of the Axiom ecosystem: off-heap JSON and CSV parsers, and a JSON emitter, that every
> native driver and every application gateway shares.

`axiom-spec` defines how raw wire bytes become Axiom data and how Axiom data becomes wire bytes again. All parsers read
directly from a Panama `MemorySegment` and produce frozen `PersistentMap` / `PersistentList` values. It is the shared
base of every `axiom-warp-*-driver`.

---

## Architecture overview

```
              bytes in (MemorySegment)                               bytes out (String)
                       │                                                    ▲
     ┌─────────────────┴──────────────────┐                                 │
     ▼                                    ▼                                 │
 JsonParser                          CsvRowParser                      JsonEmitter
 take → openBuffer                   takeLine(segment)                 emit(Object)
   ├─ ensureRootIsObject().parseObject()   .basedOnHeaders(..)              │
   └─ ensureRootIsList()                                                    │
     │                                    │                                 │
     └────────────► PersistentMap / PersistentList ─────────────────────────┘
                     (frozen into Arena.ofShared(); numbers via Dop.normalize)
```

| Class | Responsibility |
|---|---|
| `JsonParser` | Recursive-descent JSON parser over a `MemorySegment`. Enforces the expected root (`{}` or `[]`) before parsing. |
| `JsonEmitter` | Serialises `PersistentMap`, `PersistentList`, `String`, `Number`, `Boolean`, `null` (anything else via `toString()`) to compact JSON. |
| `CsvRowParser` | Parses a single CSV line (quote-aware) and zips it with headers into a map. |

Dependency graph:

```
axiom  ◄──  axiom-spec  ◄──  axiom-warp-{postgresql,mysql,oracle,sqlite,sqlserver}-driver
```

---

## Installation

**Maven**

```xml
<dependency>
    <groupId>com.ensemblu</groupId>
    <artifactId>axiom-spec</artifactId>
    <version>3.0.0</version>
</dependency>
```

**Gradle (Kotlin DSL)**

```kotlin
implementation("com.ensemblu:axiom-spec:3.0.0")
```

JPMS automatic module name: `com.ensemblu.axiom.spec` · Runtime: JDK 26 with `--enable-preview`.

> If you already depend on any `axiom-warp-*-driver`, `axiom-spec` is on your classpath transitively.

---

## Usage examples

### Parse a JSON object

```java
import com.ensemblu.axiom.spec.parser.JsonParser;
import java.lang.foreign.MemorySegment;

MemorySegment body = MemorySegment.ofArray(exchange.getRequestBody().readAllBytes());

PersistentMap<String, Object> request = JsonParser
        .take(body)
        .openBuffer()
        .ensureRootIsObject()      // throws "Contract Violation: Expected Object ({})" otherwise
        .parseObject();

String status = request.targetKey("status").toStringVal();
int limit     = request.targetKey("limit").toIntVal();
```

### Parse a JSON array

```java
PersistentList<Object> items = JsonParser.take(segment).openBuffer().ensureRootIsList();
```

### Emit JSON

```java
import com.ensemblu.axiom.spec.parser.JsonEmitter;

var response = Axiom.Data.<String, Object>emptyMap()
        .put("status", "OK")
        .put("rows", rows);                 // PersistentList<PersistentMap<String,Object>>

String json = JsonEmitter.emit(response);   // {"status":"OK","rows":[...]}
```

### Parse a CSV line

```java
import com.ensemblu.axiom.spec.parser.CsvRowParser;

var line = MemorySegment.ofArray("Ofek Malka,50000.00,ILS,ACTIVE".getBytes(StandardCharsets.UTF_8));

PersistentMap<String, Object> row = CsvRowParser
        .takeLine(line)
        .basedOnHeaders("owner", "balance", "currency", "status");

// Split a line without headers
PersistentList<String> cells = CsvRowParser.scanLine(line);
```

---

## Behaviour and configuration guide

`axiom-spec` has no configuration file. Behaviour you should design around:

| Area | Behaviour |
|---|---|
| JSON numbers | Parsed as `double`, then normalised through `Dop.normalize`: whole values become `Integer` (or `Long` beyond int range), fractional values stay `Double`. Use `toIntVal()` / `toDoubleVal()` at the edge rather than casting. |
| JSON strings | Escapes `\" \\ \n \r \t` are decoded; other escaped characters are taken literally (no `\uXXXX` decoding). |
| JSON emitting | Escapes `\ " \b \f \n \r \t`. Output is compact (no whitespace). |
| CSV | Comma separated, double-quote aware, each cell trimmed. Missing trailing cells become `null`. Blank or empty lines throw an `Axiom Contract Violation`. |
| Memory | Parsed results are frozen into `Arena.ofShared()`, so they outlive the input segment. |
| Errors | Malformed input throws `RuntimeException` with the byte position. Wrap with `Axiom.Check.attempt(..)` to get a `Result`. |

Combine with [`axiom-language`](../axiom-language) to validate parsed JSON against a schema in one step:

```java
SchemaGuard.checkContent(body)
        .basedOnSchemaInPath("schemas/request")
        .withParser(seg -> JsonParser.take(seg).openBuffer().ensureRootIsObject().parseObject());
```

---

## License

**Limited Commercial License** — free for evaluation, testing, and non-commercial development.
Commercial or production use requires a paid annual contract.
See [LICENSE](https://raw.githubusercontent.com/ensemblu-corp/axiom/main/LICENSE) · Contact: contact@ensemblu.com

© Ensemblu Corp
