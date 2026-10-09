package com.cbp.testgen.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import jakarta.annotation.PostConstruct;
import java.util.*;

@RestController
@RequestMapping("/api/sql")
public class SqlConsoleApiController {

    private static final Logger logger = LoggerFactory.getLogger(SqlConsoleApiController.class);

    private final DataSource dataSource;

    public SqlConsoleApiController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void initDatabaseView() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = """
                CREATE OR REPLACE VIEW latest_class_health AS
                SELECT 
                    c.id AS class_id,
                    c.class_name,
                    c.package_name,
                    c.project_id,
                    COALESCE(cov.line_coverage_pct, 0.0) AS latest_line_coverage_pct,
                    COALESCE(cov.branch_coverage_pct, 0.0) AS latest_branch_coverage_pct,
                    COALESCE(mut.mutation_score_pct, 0.0) AS latest_mutation_score_pct,
                    COALESCE(mut.total_mutants, 0) AS total_mutants,
                    COALESCE(mut.mutants_killed, 0) AS mutants_killed,
                    (SELECT COUNT(*) FROM flaky_tests ft 
                     JOIN test_cases tc ON ft.test_case_id = tc.id 
                     WHERE tc.class_id = c.id) AS total_flaky_count,
                    (SELECT COUNT(*) FROM test_cases tc WHERE tc.class_id = c.id AND tc.is_weak = TRUE) AS weak_test_count
                FROM classes c
                LEFT JOIN (
                    SELECT cr1.* FROM coverage_results cr1
                    INNER JOIN (
                        SELECT class_id, MAX(id) AS max_id FROM coverage_results GROUP BY class_id
                    ) cr2 ON cr1.id = cr2.max_id
                ) cov ON c.id = cov.class_id
                LEFT JOIN (
                    SELECT mr1.* FROM mutation_results mr1
                    INNER JOIN (
                        SELECT class_id, MAX(id) AS max_id FROM mutation_results GROUP BY class_id
                    ) mr2 ON mr1.id = mr2.max_id
                ) mut ON c.id = mut.class_id
            """;
            stmt.execute(sql);
            logger.info("SQL View 'latest_class_health' initialized successfully.");
        } catch (Exception e) {
            logger.warn("Could not initialize 'latest_class_health' view: {}", e.getMessage());
        }
    }

    public static class SqlExecuteRequest {
        private String query;

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
    }

    public static class ColumnInfo {
        private String name;
        private String type;
        private boolean nullable;

        public ColumnInfo(String name, String type, boolean nullable) {
            this.name = name;
            this.type = type;
            this.nullable = nullable;
        }

        public String getName() { return name; }
        public String getType() { return type; }
        public boolean isNullable() { return nullable; }
    }

    public static class TableSchemaInfo {
        private String tableName;
        private String type;
        private long rowCount;
        private List<ColumnInfo> columns = new ArrayList<>();

        public TableSchemaInfo(String tableName, String type) {
            this.tableName = tableName;
            this.type = type;
        }

        public String getTableName() { return tableName; }
        public String getType() { return type; }
        public long getRowCount() { return rowCount; }
        public void setRowCount(long rowCount) { this.rowCount = rowCount; }
        public List<ColumnInfo> getColumns() { return columns; }
    }

    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeQuery(@RequestBody SqlExecuteRequest request) {
        Map<String, Object> response = new HashMap<>();
        String query = request.getQuery() != null ? request.getQuery().trim() : "";

        if (query.isEmpty()) {
            response.put("success", false);
            response.put("errorMessage", "Query string cannot be empty.");
            return ResponseEntity.badRequest().body(response);
        }

        // Clean query (strip trailing semicolon if needed for some drivers)
        if (query.endsWith(";")) {
            query = query.substring(0, query.length() - 1).trim();
        }

        long startTime = System.currentTimeMillis();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setMaxRows(500); // Safety limit for UI display
            boolean isResultSet = stmt.execute(query);
            long executionTimeMs = System.currentTimeMillis() - startTime;

            response.put("executionTimeMs", executionTimeMs);

            if (isResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();
                    List<String> columns = new ArrayList<>();
                    for (int i = 1; i <= colCount; i++) {
                        String label = meta.getColumnLabel(i);
                        columns.add(label != null && !label.isEmpty() ? label : meta.getColumnName(i));
                    }

                    List<List<Object>> rows = new ArrayList<>();
                    while (rs.next()) {
                        List<Object> row = new ArrayList<>();
                        for (int i = 1; i <= colCount; i++) {
                            Object val = rs.getObject(i);
                            if (val instanceof Clob clob) {
                                val = clob.getSubString(1, (int) Math.min(clob.length(), 2000));
                            } else if (val instanceof byte[] bytes) {
                                val = new String(bytes, StandardCharsets.UTF_8);
                            }
                            row.add(val != null ? val.toString() : null);
                        }
                        rows.add(row);
                    }

                    response.put("success", true);
                    response.put("queryType", "SELECT");
                    response.put("columns", columns);
                    response.put("rows", rows);
                    response.put("rowCount", rows.size());
                }
            } else {
                int updateCount = stmt.getUpdateCount();
                response.put("success", true);
                response.put("queryType", "DML/DDL");
                response.put("affectedRows", updateCount);
                response.put("message", "Statement executed successfully. Affected rows: " + updateCount);
            }

            return ResponseEntity.ok(response);

        } catch (SQLException e) {
            long executionTimeMs = System.currentTimeMillis() - startTime;
            response.put("success", false);
            response.put("executionTimeMs", executionTimeMs);
            response.put("errorCode", e.getErrorCode());
            response.put("sqlState", e.getSQLState());
            response.put("errorMessage", e.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("errorMessage", "Execution error: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/tables")
    public ResponseEntity<List<TableSchemaInfo>> getTablesAndSchema() {
        List<TableSchemaInfo> tableList = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String catalog = conn.getCatalog();

            try (ResultSet rs = meta.getTables(catalog, null, "%", new String[]{"TABLE", "VIEW"})) {
                while (rs.next()) {
                    String tableSchem = rs.getString("TABLE_SCHEM");
                    String tableName = rs.getString("TABLE_NAME");
                    String tableType = rs.getString("TABLE_TYPE");

                    // Filter out internal system schemas (H2, Postgres, MySQL sys)
                    if (tableSchem != null && (tableSchem.equalsIgnoreCase("INFORMATION_SCHEMA") 
                            || tableSchem.equalsIgnoreCase("SYSTEM_LOBS") 
                            || tableSchem.equalsIgnoreCase("PG_CATALOG"))) {
                        continue;
                    }

                    // Filter out internal system tables by prefix
                    if (!tableName.startsWith("SYSTEM_") && !tableName.startsWith("INFORMATION_SCHEMA") && !tableName.startsWith("SYS_")) {
                        TableSchemaInfo info = new TableSchemaInfo(tableName, tableType);

                        // Columns
                        try (ResultSet colRs = meta.getColumns(catalog, tableSchem, tableName, "%")) {
                            while (colRs.next()) {
                                String colName = colRs.getString("COLUMN_NAME");
                                String typeName = colRs.getString("TYPE_NAME");
                                boolean isNullable = "YES".equalsIgnoreCase(colRs.getString("IS_NULLABLE"));
                                info.getColumns().add(new ColumnInfo(colName, typeName, isNullable));
                            }
                        }

                        // Row count estimate
                        try (Statement stmt = conn.createStatement();
                             ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
                            if (countRs.next()) {
                                info.setRowCount(countRs.getLong(1));
                            }
                        } catch (Exception ignored) {
                            // Some views or complex tables might not support simple count
                        }

                        tableList.add(info);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not retrieve schema information: {}", e.getMessage());
        }

        return ResponseEntity.ok(tableList);
    }

    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exportQueryCsv(@RequestParam("query") String query) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            if (query.endsWith(";")) {
                query = query.substring(0, query.length() - 1).trim();
            }

            try (ResultSet rs = stmt.executeQuery(query)) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter pw = new PrintWriter(baos, true, StandardCharsets.UTF_8);

                // CSV Header
                List<String> headers = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    headers.add(escapeCsv(meta.getColumnLabel(i)));
                }
                pw.println(String.join(",", headers));

                // CSV Data
                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    for (int i = 1; i <= colCount; i++) {
                        Object val = rs.getObject(i);
                        row.add(escapeCsv(val != null ? val.toString() : ""));
                    }
                    pw.println(String.join(",", row));
                }
                pw.flush();

                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setContentType(MediaType.parseMediaType("text/csv"));
                responseHeaders.setContentDispositionFormData("attachment", "query_export.csv");

                return ResponseEntity.ok()
                        .headers(responseHeaders)
                        .body(baos.toByteArray());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(("Error generating CSV: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    private String escapeCsv(String val) {
        if (val == null) return "\"\"";
        val = val.replace("\"", "\"\"");
        if (val.contains(",") || val.contains("\n") || val.contains("\r") || val.contains("\"")) {
            return "\"" + val + "\"";
        }
        return val;
    }
}
