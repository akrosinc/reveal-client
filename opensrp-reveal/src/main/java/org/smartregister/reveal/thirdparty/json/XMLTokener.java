package org.smartregister.reveal.thirdparty.json;

/*
Public Domain / org.json license.

Vendored from the org.json reference implementation because Android's platform
android.jar ships a partial org.json package that shadows the org.json:json
dependency and hides XML/JSONML/XMLTokener. This local copy references the
platform org.json.{JSONObject,JSONArray,JSONException} types.
*/

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * The XMLTokener extends the JSONTokener to provide additional methods
 * for the parsing of XML texts.
 */
public class XMLTokener extends JSONTokener {

    /** The table of entity values. It initially contains Character values for
     * amp, apos, gt, lt, quot.
     */
    public static final java.util.HashMap<String, Character> entity;

    static {
        entity = new java.util.HashMap<String, Character>(8);
        entity.put("amp",  XML.AMP);
        entity.put("apos", XML.APOS);
        entity.put("gt",   XML.GT);
        entity.put("lt",   XML.LT);
        entity.put("quot", XML.QUOT);
    }

    /**
     * Construct an XMLTokener from a string.
     * @param s A source string.
     */
    public XMLTokener(String s) {
        super(s);
    }

    /**
     * Get the text in the CDATA block.
     * @return The string up to the <code>]]&gt;</code>.
     * @throws JSONException If the <code>]]&gt;</code> is not found.
     */
    public String nextCDATA() throws JSONException {
        char c;
        int i;
        StringBuilder sb = new StringBuilder();
        while (more()) {
            c = next();
            sb.append(c);
            i = sb.length() - 3;
            if (i >= 0 && sb.charAt(i) == ']' &&
                    sb.charAt(i + 1) == ']' && sb.charAt(i + 2) == '>') {
                sb.setLength(i);
                return sb.toString();
            }
        }
        throw syntaxError("Unclosed CDATA");
    }

    /**
     * Get the next XML outer token, trimming whitespace. There are two kinds
     * of tokens: the '<' character which begins a markup tag, and the content
     * text between markup tags.
     *
     * @return  A string, or a '<' Character, or null if there is no more source text.
     * @throws JSONException on error
     */
    public Object nextContent() throws JSONException {
        char         c;
        StringBuilder sb;
        do {
            c = next();
        } while (Character.isWhitespace(c));
        if (c == 0) {
            return null;
        }
        if (c == '<') {
            return XML.LT;
        }
        sb = new StringBuilder();
        for (;;) {
            if (c == 0) {
                return sb.toString().trim();
            }
            if (c == '<') {
                back();
                return sb.toString().trim();
            }
            if (c == '&') {
                sb.append(nextEntity(c));
            } else {
                sb.append(c);
            }
            c = next();
        }
    }

    /**
     * Return the next entity. These entities are translated to Characters:
     * <code>&amp;  &apos;  &gt;  &lt;  &quot;</code>.
     * @param ampersand An ampersand character.
     * @return  A Character or an entity String if the entity is not recognized.
     * @throws JSONException If missing ';' in XML entity.
     */
    public Object nextEntity(char ampersand) throws JSONException {
        StringBuilder sb = new StringBuilder();
        for (;;) {
            char c = next();
            if (Character.isLetterOrDigit(c) || c == '#') {
                sb.append(Character.toLowerCase(c));
            } else if (c == ';') {
                break;
            } else {
                throw syntaxError("Missing ';' in XML entity: &" + sb);
            }
        }
        String string = sb.toString();
        return unescapeEntity(string);
    }

    /**
     * Unescapes an XML entity encoding;
     * @param e entity (only the actual entity value, not the preceding & or ending ;
     * @return unescaped value
     */
    static String unescapeEntity(String e) {
        // validate
        if (e == null || e.isEmpty()) {
            return "";
        }
        // if our entity is an encoded unicode point, parse it.
        if (e.charAt(0) == '#') {
            int cp;
            if (e.charAt(1) == 'x' || e.charAt(1) == 'X') {
                // hex encoded unicode
                cp = Integer.parseInt(e.substring(2), 16);
            } else {
                // decimal encoded unicode
                cp = Integer.parseInt(e.substring(1));
            }
            return new String(new int[] {cp},0,1);
        }
        Character knownEntity = entity.get(e);
        if(knownEntity==null) {
            // we don't know the entity so keep it encoded
            return '&' + e + ';';
        }
        return knownEntity.toString();
    }

    /**
     * Returns the next XML meta token. This is used for skipping over <!...>
     * and <?...?> structures.
     * @return  Syntax characters (<code>&lt; &gt; / = ! ?</code>) are returned
     *  as Character, and strings and names are returned as Boolean. We don't care
     *  what the values actually are.
     * @throws JSONException If a string is not properly closed or if the XML is badly structured.
     */
    public Object nextMeta() throws JSONException {
        char c;
        char q;
        do {
            c = next();
        } while (Character.isWhitespace(c));
        switch (c) {
            case 0:
                throw syntaxError("Misshaped meta tag");
            case '<':
                return XML.LT;
            case '>':
                return XML.GT;
            case '/':
                return XML.SLASH;
            case '=':
                return XML.EQ;
            case '!':
                return XML.BANG;
            case '?':
                return XML.QUEST;
            case '"':
            case '\'':
                q = c;
                for (;;) {
                    c = next();
                    if (c == 0) {
                        throw syntaxError("Unterminated string");
                    }
                    if (c == q) {
                        return Boolean.TRUE;
                    }
                }
            default:
                for (;;) {
                    c = next();
                    if (Character.isWhitespace(c)) {
                        return Boolean.TRUE;
                    }
                    switch (c) {
                        case 0:
                        case '<':
                        case '>':
                        case '/':
                        case '=':
                        case '!':
                        case '?':
                        case '"':
                        case '\'':
                            back();
                            return Boolean.TRUE;
                    }
                }
        }
    }

    /**
     * Get the next XML Token. These tokens are found inside of angle
     * brackets. It may be one of these characters: <code>/ &gt; = ! ?</code> or it
     * may be a string wrapped in single quotes or double quotes, or it may be a
     * name.
     * @return a String or a Character.
     * @throws JSONException If the XML is not well formed.
     */
    public Object nextToken() throws JSONException {
        char c;
        char q;
        StringBuilder sb;
        do {
            c = next();
        } while (Character.isWhitespace(c));
        switch (c) {
            case 0:
                throw syntaxError("Misshaped element");
            case '<':
                throw syntaxError("Misplaced '<'");
            case '>':
                return XML.GT;
            case '/':
                return XML.SLASH;
            case '=':
                return XML.EQ;
            case '!':
                return XML.BANG;
            case '?':
                return XML.QUEST;

            case '"':
            case '\'':
                q = c;
                sb = new StringBuilder();
                for (;;) {
                    c = next();
                    if (c == 0) {
                        throw syntaxError("Unterminated string");
                    }
                    if (c == q) {
                        return sb.toString();
                    }
                    if (c == '&') {
                        sb.append(nextEntity(c));
                    } else {
                        sb.append(c);
                    }
                }

            default:
                sb = new StringBuilder();
                for (;;) {
                    sb.append(c);
                    c = next();
                    if (Character.isWhitespace(c)) {
                        return sb.toString();
                    }
                    switch (c) {
                        case 0:
                            return sb.toString();
                        case '>':
                        case '/':
                        case '=':
                        case '!':
                        case '?':
                        case '[':
                        case ']':
                            back();
                            return sb.toString();
                        case '<':
                        case '"':
                        case '\'':
                            throw syntaxError("Bad character in a name");
                    }
                }
        }
    }

    /**
     * Skip characters until past the requested string.
     * If it is not found, we are left at the end of the source with a result of false.
     * @param to A string to skip past.
     * @throws JSONException on error
     */
    public boolean skipPastTo(String to) throws JSONException {
        boolean b;
        char c;
        int i;
        int j;
        int offset = 0;
        int length = to.length();
        char[] circle = new char[length];

        for (i = 0; i < length; i += 1) {
            c = next();
            if (c == 0) {
                return false;
            }
            circle[i] = c;
        }

        for (;;) {
            j = offset;
            b = true;
            for (i = 0; i < length; i += 1) {
                if (circle[j] != to.charAt(i)) {
                    b = false;
                    break;
                }
                j += 1;
                if (j >= length) {
                    j -= length;
                }
            }
            if (b) {
                return true;
            }
            c = next();
            if (c == 0) {
                return false;
            }
            circle[offset] = c;
            offset += 1;
            if (offset >= length) {
                offset -= length;
            }
        }
    }
}
