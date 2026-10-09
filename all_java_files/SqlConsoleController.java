package com.cbp.testgen.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SqlConsoleController {

    private static final Logger logger = LoggerFactory.getLogger(SqlConsoleController.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public SqlConsoleController(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/sql-console")
    public String showSqlConsole(Model model) {
        String dbProduct = "Relational Database";
        String dbVersion = "";
        String dbUrl = "";
        List<String> tableNames = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            dbProduct = meta.getDatabaseProductName();
            dbVersion = meta.getDatabaseProductVersion();
            dbUrl = meta.getURL();

            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE", "VIEW"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    // Exclude internal database system tables
                    if (!tableName.startsWith("SYSTEM_") && !tableName.startsWith("INFORMATION_SCHEMA") && !tableName.startsWith("SYS_")) {
                        tableNames.add(tableName);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not retrieve database metadata for SQL console: {}", e.getMessage());
        }

        model.addAttribute("dbProduct", dbProduct);
        model.addAttribute("dbVersion", dbVersion);
        model.addAttribute("dbUrl", maskJdbcUrl(dbUrl));
        model.addAttribute("tableNames", tableNames);

        return "sql-console";
    }

    private String maskJdbcUrl(String url) {
        if (url == null) return "Connected";
        // Strip sensitive password query params if present
        return url.replaceAll("(?i)password=[^;&]*", "password=***");
    }
}
