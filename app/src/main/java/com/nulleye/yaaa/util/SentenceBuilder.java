package com.nulleye.yaaa.util;

/**
 * SQL sentence builder
 *
 * Created by Cristian Alvarez on 30/4/16.
 */
public class SentenceBuilder {

    public static final String AND = " AND ";
    public static final String OR = " OR ";

    public static final String LP = " ( ";
    public static final String RP = " ) ";

    public static final String EQ = " = ";
    public static final String NE = " != ";
    public static final String LE = " <= ";
    public static final String LT = " < ";
    public static final String GE = " >= ";
    public static final String GT = " > ";
    public static final String LK = " LIKE ";
    public static final String NLK = " NOT LIKE ";

    final StringBuffer sb;


    public SentenceBuilder() {
        sb = new StringBuffer();
    }


    public SentenceBuilder(final String text) {
        sb = new StringBuffer(text);
    }


    public SentenceBuilder(final String column, final String operation, final Object value) {
        sb = new StringBuffer(column);
        sb.append(operation).append(value);
    }


    public SentenceBuilder andExpr(final String column, final String operation, final Object value) {
        return addExpr(AND, column, operation, value);
    }


    public SentenceBuilder orExpr(final String column, final String operation, final Object value) {
        return addExpr(OR, column, operation, value);
    }


    public SentenceBuilder addExpr(final String column, final String operation, final Object value) {
        sb.append(column).append(operation).append(value);
        return this;
    }


    public SentenceBuilder addExpr(final String concat, final String column, final String operation, final Object value) {
        if (sb.length() > 0) sb.append(concat);
        sb.append(column).append(operation).append(value);
        return this;
    }


    public SentenceBuilder add(final Object value) {
        sb.append(value);
        return this;
    }


    public SentenceBuilder addLP() {
        sb.append(LP);
        return this;
    }


    public SentenceBuilder addRP() {
        sb.append(RP);
        return this;
    }


    public String build() {
        return sb.toString();
    }


    public String build(final String text) {
        if (text != null) sb.append(text);
        return sb.toString();
    }


    public SentenceBuilder clear() {
        sb.setLength(0);
        return this;
    }


    public int length() {
        return sb.length();
    }

} //SentenceBuilder
