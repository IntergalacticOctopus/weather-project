import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Json {

    private final String text;
    private int pos;

    private Json(String text) {
        this.text = text;
    }

    static Object parse(String text) {
        return new Json(text).value();
    }

    private Object value() {
        return switch (peek()) {
            case '{' -> object();
            case '[' -> array();
            case '"' -> string();
            default -> literal();
        };
    }

    private Map<String, Object> object() {
        Map<String, Object> map = new HashMap<>();
        expect('{');
        if (peek() == '}') {
            pos++;
            return map;
        }
        do {
            String key = string();
            expect(':');
            map.put(key, value());
        } while (next() == ',');
        return map;
    }

    private List<Object> array() {
        List<Object> list = new ArrayList<>();
        expect('[');
        if (peek() == ']') {
            pos++;
            return list;
        }
        do {
            list.add(value());
        } while (next() == ',');
        return list;
    }

    private String string() {
        expect('"');
        int start = pos;
        while (text.charAt(pos) != '"') {
            pos += text.charAt(pos) == '\\' ? 2 : 1;
        }
        return text.substring(start, pos++);
    }

    private String literal() {
        int start = pos;
        while (",]} \t\r\n".indexOf(text.charAt(pos)) < 0) {
            pos++;
        }
        return text.substring(start, pos);
    }

    private char peek() {
        while (Character.isWhitespace(text.charAt(pos))) {
            pos++;
        }
        return text.charAt(pos);
    }

    private char next() {
        char c = peek();
        pos++;
        return c;
    }

    private void expect(char expected) {
        if (next() != expected) {
            throw new IllegalArgumentException("Ожидался символ '" + expected + "' в позиции " + (pos - 1));
        }
    }
}
