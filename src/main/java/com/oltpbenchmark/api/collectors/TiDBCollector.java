package com.oltpbenchmark.api.collectors;

import java.sql.*;
import java.util.Map;
import java.util.TreeMap;

public class TiDBCollector extends DBCollector {
    private static final String VERSION_SQL = "SELECT @@GLOBAL.version;";

    private static final String PARAMETERS_SQL = "SHOW CONFIG;";

    private static final Map<String, String> METRICS_SQL = new TreeMap<String, String>(){
        {
            put("tidb_qps",
                    "SELECT instance, sum(value) from METRICS_SCHEMA.tidb_qps where result='OK' and time=now() group by instance;");
            put("tidb_qps",
                    "SELECT instance, sum(value) from METRICS_SCHEMA.tidb_qps where result='OK' and time=now() group by instance;");
            put("tidb_query_duration",
                    "SELECT instance, sum(value) from METRICS_SCHEMA.tidb_query_duration where quantile=0.99 and time = now() group by instance;");
            put("tikv_grpc_message_total_count",
                    "SELECT instance, sum(value) from METRICS_SCHEMA.tikv_grpc_message_total_count where time=now() group by instance;");
            put("tikv_grpc_message_duration",
                    "SELECT instance, sum(value) from METRICS_SCHEMA.tikv_grpc_message_duration where time=now() group by instance;");
            put("tikv_thread_cpu",
                    "SELECT instance, sum(value) from METRICS_SCHEMA.tikv_thread_cpu where time=now() group by instance;");
            put("tikv_memory",
                    "SELECT instance, sum(value) from METRICS_SCHEMA.tikv_memory where time=now() group by instance;");
            put("node_disk_io_util",
                    "SELECT instance, sum(value) from METRICS_SCHEMA.node_disk_io_util where device='vdc' and time=now() group by instance;");
        }
    };

    public TiDBCollector(String oriDBUrl, String username, String password) {
        try (Connection conn = DriverManager.getConnection(oriDBUrl, username, password)) {
            try (Statement s = conn.createStatement()) {

                // Collect DBMS version
                try (ResultSet out = s.executeQuery(VERSION_SQL)) {
                    if (out.next()) {
                        this.version = out.getString(1);
                    }
                }

                // Collect DBMS parameters
                try (ResultSet out = s.executeQuery(PARAMETERS_SQL)) {
                    while (out.next()) {
                        dbParameters.put(
                                "("
                                    + out.getString("Type")
                                    + ","
                                    + out.getString("Instance")
                                    + ","
                                    + out.getString("Name")
                                    + ")",
                                out.getString("Value"));
                    }
                }

                // Collect DBMS internal metrics
                for (Map.Entry<String, String> entry : METRICS_SQL.entrySet()) {
                    try (ResultSet out = s.executeQuery(entry.getValue())) {
                        while (out.next()) {
                            dbMetrics.put(
                                    "("
                                        + entry.getKey()
                                        + ","
                                        + out.getString("instance")
                                        + ")",
                                    out.getString("sum(value)"));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOG.error("Error while collecting DB parameters: {}", e.getMessage());
        }
    }
}
