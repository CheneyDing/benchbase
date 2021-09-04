package com.oltpbenchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TFIDFStatistics {
    private static final Logger LOG = LoggerFactory.getLogger(TFIDFStatistics.class);

    private static final ArrayList<String> sqlReservedWords = new ArrayList<String>(Arrays.asList(
            "ABSOLUTE",
            "ACTION",
            "ADD",
            "ALL",
            "ALLOCATE",
            "ALTER",
            "AND",
            "ANY",
            "ARE",
            "AS",
            "ASC",
            "ASSERTION",
            "AT",
            "AUTHORIZATION",
            "AVG",
            "BEGIN",
            "BETWEEN",
            "BIT",
            "BIT_LENGTH",
            "BOTH",
            "BY",
            "CALL",
            "CASCADE",
            "CASCADED",
            "CASE",
            "CAST",
            "CATALOG",
            "CHAR",
            "CHARACTER",
            "CHARACTER_LENGTH",
            "CHAR_LENGTH",
            "CHECK",
            "CLOSE",
            "COALESCE",
            "COLLATE",
            "COLLATION",
            "COLUMN",
            "COMMIT",
            "CONDITION",
            "CONNECT",
            "CONNECTION",
            "CONSTRAINT",
            "CONSTRAINTS",
            "CONTAINS",
            "CONTINUE",
            "CONVERT",
            "CORRESPONDING",
            "COUNT",
            "CREATE",
            "CROSS",
            "CURRENT",
            "CURRENT_DATE",
            "CURRENT_PATH",
            "CURRENT_TIME",
            "CURRENT_TIMESTAMP",
            "CURRENT_USER",
            "CURSOR",
            "DATE",
            "DAY",
            "DEALLOCATE",
            "DEC",
            "DECIMAL",
            "DECLARE",
            "DEFAULT",
            "DEFERRABLE",
            "DEFERRED",
            "DELETE",
            "DESC",
            "DESCRIBE",
            "DESCRIPTOR",
            "DETERMINISTIC",
            "DIAGNOSTICS",
            "DISCONNECT",
            "DISTINCT",
            "DO",
            "DOMAIN",
            "DOUBLE",
            "DROP",
            "ELSE",
            "ELSEIF",
            "END",
            "ESCAPE",
            "EXCEPT",
            "EXCEPTION",
            "EXEC",
            "EXECUTE",
            "EXISTS",
            "EXIT",
            "EXTERNAL",
            "EXTRACT",
            "FALSE",
            "FETCH",
            "FIRST",
            "FLOAT",
            "FOR",
            "FOREIGN",
            "FOUND",
            "FROM",
            "FULL",
            "FUNCTION",
            "GET",
            "GLOBAL",
            "GO",
            "GOTO",
            "GRANT",
            "GROUP",
            "HANDLER",
            "HAVING",
            "HOUR",
            "IDENTITY",
            "IF",
            "IMMEDIATE",
            "IN",
            "INDICATOR",
            "INITIALLY",
            "INNER",
            "INOUT",
            "INPUT",
            "INSENSITIVE",
            "INSERT",
            "INT",
            "INTEGER",
            "INTERSECT",
            "INTERVAL",
            "INTO",
            "IS",
            "ISOLATION",
            "JOIN",
            "KEY",
            "LANGUAGE",
            "LAST",
            "LEADING",
            "LEAVE",
            "LEFT",
            "LEVEL",
            "LIKE",
            "LOCAL",
            "LOOP",
            "LOWER",
            "MATCH",
            "MAX",
            "MIN",
            "MINUTE",
            "MODULE",
            "MONTH",
            "NAMES",
            "NATIONAL",
            "NATURAL",
            "NCHAR",
            "NEXT",
            "NO",
            "NOT",
            "NULL",
            "NULLIF",
            "NUMERIC",
            "OCTET_LENGTH",
            "OF",
            "ON",
            "ONLY",
            "OPEN",
            "OPTION",
            "OR",
            "ORDER",
            "OUT",
            "OUTER",
            "OUTPUT",
            "OVERLAPS",
            "PAD",
            "PARAMETER",
            "PARTIAL",
            "PATH",
            "POSITION",
            "PRECISION",
            "PREPARE",
            "PRESERVE",
            "PRIMARY",
            "PRIOR",
            "PRIVILEGES",
            "PROCEDURE",
            "PUBLIC",
            "READ",
            "REAL",
            "REFERENCES",
            "RELATIVE",
            "REPEAT",
            "RESIGNAL",
            "RESTRICT",
            "RETURN",
            "RETURNS",
            "REVOKE",
            "RIGHT",
            "ROLLBACK",
            "ROUTINE",
            "ROWS",
            "SCHEMA",
            "SCROLL",
            "SECOND",
            "SECTION",
            "SELECT",
            "SESSION",
            "SESSION_USER",
            "SET",
            "SIGNAL",
            "SIZE",
            "SMALLINT",
            "SOME",
            "SPACE",
            "SPECIFIC",
            "SQL",
            "SQLCODE",
            "SQLERROR",
            "SQLEXCEPTION",
            "SQLSTATE",
            "SQLWARNING",
            "SUBSTRING",
            "SUM",
            "SYSTEM_USER",
            "TABLE",
            "TEMPORARY",
            "THEN",
            "TIME",
            "TIMESTAMP",
            "TIMEZONE_HOUR",
            "TIMEZONE_MINUTE",
            "TO",
            "TRAILING",
            "TRANSACTION",
            "TRANSLATE",
            "TRANSLATION",
            "TRIM",
            "TRUE",
            "UNDO",
            "UNION",
            "UNIQUE",
            "UNKNOWN",
            "UNTIL",
            "UPDATE",
            "UPPER",
            "USAGE",
            "USER",
            "USING",
            "VALUE",
            "VALUES",
            "VARCHAR",
            "VARYING",
            "VIEW",
            "WHEN",
            "WHENEVER",
            "WHERE",
            "WHILE",
            "WITH",
            "WORK",
            "WRITE",
            "YEAR",
            "ZONE"
    ));

    private final ArrayList<HashMap<String, Double>> result;

    public TFIDFStatistics(ArrayList<HashMap<String, Double>> result) {
        this.result = result;
    }

    public static TFIDFStatistics computeStatistics(ArrayList<String> sqlStmts) {
        int totalStms = sqlStmts.size();
        ArrayList<HashMap<String, Integer>> totalCount = new ArrayList<HashMap<String, Integer>>();
        for (String s : sqlStmts) {
            String[] words = s.split(" ");
            HashMap<String, Integer> count = new HashMap<String, Integer>();
            for (String w : words) {
                if (sqlReservedWords.contains(w)) {
                    if (count.containsKey(w))
                        count.put(w, count.get(w)+1);
                    else
                        count.put(w, 1);
                }
            }
            totalCount.add(count);
        }
        ArrayList<HashMap<String, Double>> totalRes = new ArrayList<HashMap<String, Double>>();
        for (HashMap<String, Integer> c : totalCount) {
            HashMap<String, Double> res = new HashMap<String, Double>();
            for (Map.Entry<String, Integer> entry: c.entrySet()) {
                double tfidf = computeTfidf(entry.getKey(), c, totalCount);
                res.put(entry.getKey(), tfidf);
            }
            totalRes.add(res);
        }
        return new TFIDFStatistics(totalRes);
    }

    private static double computeTfidf(String key, HashMap<String, Integer> sql, ArrayList<HashMap<String, Integer>> allSql) {
        int wordCountInThisSql = 0;
        for (int c : sql.values()) {
            wordCountInThisSql += c;
        }
        double tf = sql.get(key).doubleValue() / wordCountInThisSql;

        int wordCountInAllSql = 0;
        for (HashMap<String, Integer> s : allSql) {
            if (s.containsKey(key)) {
                wordCountInAllSql += 1;
            }
        }
        double idf = Math.log(Double.valueOf(allSql.size()) / (wordCountInAllSql + 1));
        return tf * idf;
    }
}
