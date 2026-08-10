
# Axiom Spec

The `axiom-spec` engine acts as the **System Architect** for the Axiom ecosystem. It defines the grammar for data exchange (JSON, CSV, SQL templates) and provides the materialization logic to bind raw input to strict database protocols.

## 🏛️ Integration

Summon the Specification engine into your project:

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

## ⚖️ Sovereign Law

This JAR enforces the structural integrity of incoming data streams:

-   **Contract-First Parsing**: Data ingestion is governed by `AxiomProtocol`, ensuring that every field is typed and bound before database contact.

-   **Integrity Guard**: `IngressIntegrity` verifies that the SQL template, contract types, and raw data are perfectly aligned before execution.

-   **Zero-Reflection Materialization**: Uses character-level scanning (`JsonParser`, `CsvRowParser`) to avoid the bloat of standard reflection-based deserializers.


## ⚡ Operational Entry

### 1. The Database Handshake

Bind raw persistent maps to SQL templates using the `IngressBinder`:

```java  
final var plan = SqlParser.forge("INSERT INTO users (name, age) VALUES (:java.name, :java.age)");  
IngressBinder.apply(binder, plan, myContractMap, myDataMap);  
```  

### 2. High-Precision Parsing

Ingest raw data with native character scanners:

```java  
// JSON  
final var data = JsonParser.take(jsonString.getBytes(StandardCharsets.UTF_8)).openBuffer().ensureRootIsObject().parseObject();  
  
// CSV  
final var row = CsvRowParser.takeLine("val1,val2".getBytes(StandardCharsets.UTF_8)).basedOnHeaders("col1", "col2");  
```  

### 3. Structural Materialization

Transform database result sets into Sovereign `PersistentMaps` using the `RowMaterializer`:

```java  
final var materialized = RowMaterializer.materialize(row);
```  
### 4. Defining the Protocol

The `AxiomProtocol` defines how your Java types manifest in the database. Every protocol implements a `BinderSetter` to ensure type-safe injection:

```java
// Example of extending the protocol
AxiomProtocol.LONG.getSetter().set(binder, index, myLongValue);
```

### 5. Registry Management

Use `AxiomRegistry` to centralize your protocols. This ensures that when your application parses dynamic data, it maps to the correct `AxiomProtocol` at runtime without reflection:


```java
final var registry = new AxiomRegistry<>(AxiomProtocol.class);
final var protocol = registry.get("LONG"); // Returns AxiomProtocol.LONG
```
## 🛡️ Structural Guardrails

-   **CSV Strictness**: The `CsvRowParser` enforces a "no-empty-input" policy. If the input is null, empty, or whitespace, the parser invokes a contract violation, killing the process at the boundary.

-   **SQL Template Architect**: `SqlParser` replaces custom Axiom signals (`:java.key`) with JDBC-standard placeholders (`?`) during the forge phase, ensuring secure and predictable execution plans.

-   **Protocol Enforcement**: `AxiomProtocol` maps your Java data types to database-native setters, eliminating runtime casting errors.


## 📜 Legal

This project is governed by the principles of immutable software architecture. See `LICENSE.md` for the specific terms of use.
