package com.ensemblu.axiom.spec.parser;

import com.ensemblu.axiom.api.Axiom;
import com.ensemblu.axiom.core.data_structure.list.PersistentList;
import com.ensemblu.axiom.core.data_structure.map.PersistentMap;


public interface JsonParser {

    static Initial take(String json) {
        return () -> {
            final var trimmed = json.trim();
            return  new Selector(trimmed);
        };
    }

    @FunctionalInterface
    interface Initial {
        Selector openBuffer();
    }

    final class Selector {

        private String json;

        public Selector(String json) {
            this.json = json;
        }

        public Final ensureRootIsObject() {
            if (!json.trim().startsWith("{")) {
                throw new RuntimeException("Contract Violation: Expected Object ({})");
            }
            return () -> NativeEngine.parse(json);
        }

        public PersistentList<Object> ensureRootIsList() {
            if (!json.trim().startsWith("[")) {
                throw new RuntimeException("Contract Violation: Expected List ([])");
            }
            return  new NativeEngine(json).parseList();
        }
    }

    @FunctionalInterface
    interface Final {
        PersistentMap<String, Object> parseObject();
    }

    @FunctionalInterface
    interface FinalList {
        PersistentList<Object> parseList();
    }

    final class NativeEngine implements Final, FinalList {
        private final String json;
        private int pos = 0;

        private NativeEngine(String json) {
            this.json = json;
        }

        static PersistentMap<String, Object> parse(String json) {
            return new NativeEngine(json).parseObject();
        }

        // CHANGE 4: Expose existing parseArray() as parseList()
        public PersistentList<Object> parseList() {
            return parseArray();
        }

        public  PersistentMap<String, Object> parseObject() {
            var map = Axiom.Data.<String, Object>emptyMap().asTransient() ;
            consume('{');
            skipWhitespace();

            while (peek() != '}') {
                String key = parseString();
                skipWhitespace();
                consume(':');
                skipWhitespace();
                map = map.put(key, parseValue());
                skipWhitespace();
                if (peek() == ',') {
                    consume(',');
                    skipWhitespace();
                }
            }
            consume('}');
            return map.freeze();
        }

        private Object parseValue() {
            skipWhitespace();
            char c = peek();
            Object val = switch (c) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't', 'f' -> parseBoolean();
                case 'n' -> parseNull();
                default -> parseNumber();
            };
            skipWhitespace();
            return val;
        }

        private PersistentList<Object> parseArray() {
            var list = Axiom.Data.emptyList().asTransient();
            consume('[');
            skipWhitespace();
            while (peek() != ']') {
                list = list.append(parseValue());
                if (peek() == ',') {
                    consume(',');
                    skipWhitespace();
                }
            }
            consume(']');
            return list.freeze();
        }

        private String parseString() {
            consume('"');
            final var sb = new StringBuilder();
            while (peek() != '"') {
                final var c = next();
                if (c == '\\') {
                    final var esc = next();
                    sb.append(switch (esc) {
                        case '"' -> '"';
                        case '\\' -> '\\';
                        case 'n' -> '\n';
                        case 'r' -> '\r';
                        case 't' -> '\t';
                        default -> esc;
                    });
                } else sb.append(c);
            }
            consume('"');
            return sb.toString();
        }

        private Object parseNumber() {
            final var start = pos;
            while (pos < json.length() && "-0123456789.eE+".indexOf(json.charAt(pos)) != -1) pos++;
            final var n = json.substring(start, pos);

            try {
                final var d = Double.parseDouble(n);
                return com.ensemblu.axiom.core.foundation.Dop.normalize(d);
            } catch (NumberFormatException e) {
                return n;
            }
        }

        private Boolean parseBoolean() {
            if (json.startsWith("true", pos)) {
                pos += 4;
                return true;
            }
            if (json.startsWith("false", pos)) {
                pos += 5;
                return false;
            }
            throw new RuntimeException("Invalid Boolean at " + pos);
        }

        private Object parseNull() {
            if (json.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new RuntimeException("Invalid Null at " + pos);
        }

        private void skipWhitespace() {
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;
        }

        private char peek() {
            return json.charAt(pos);
        }

        private char next() {
            return json.charAt(pos++);
        }

        private void consume(char c) {
            if (next() != c) throw new RuntimeException("Expected '" + c + "' at " + (pos - 1));
        }
    }
}