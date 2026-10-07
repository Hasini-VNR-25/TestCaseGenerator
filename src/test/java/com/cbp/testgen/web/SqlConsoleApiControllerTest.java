package com.cbp.testgen.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SqlConsoleApiControllerTest {

    @Autowired
    private SqlConsoleApiController sqlController;

    @Test
    void testGetTablesAndSchema() {
        ResponseEntity<List<SqlConsoleApiController.TableSchemaInfo>> resp = sqlController.getTablesAndSchema();
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode().value());
        List<SqlConsoleApiController.TableSchemaInfo> tables = resp.getBody();
        assertNotNull(tables);
        System.out.println("Discovered tables count: " + tables.size());
        for (SqlConsoleApiController.TableSchemaInfo t : tables) {
            System.out.println("Table: " + t.getTableName() + " (" + t.getType() + ") - rows: " + t.getRowCount() + ", cols: " + t.getColumns().size());
        }
    }

    @Test
    void testExecuteQuery() {
        SqlConsoleApiController.SqlExecuteRequest req = new SqlConsoleApiController.SqlExecuteRequest();
        req.setQuery("SELECT * FROM classes LIMIT 5;");
        ResponseEntity<Map<String, Object>> resp = sqlController.executeQuery(req);
        assertNotNull(resp);
        System.out.println("Execute response: " + resp.getBody());
        assertTrue(Boolean.TRUE.equals(resp.getBody().get("success")));
    }

    @Test
    void testLatestClassHealthView() {
        SqlConsoleApiController.SqlExecuteRequest req = new SqlConsoleApiController.SqlExecuteRequest();
        req.setQuery("SELECT * FROM latest_class_health;");
        ResponseEntity<Map<String, Object>> resp = sqlController.executeQuery(req);
        assertNotNull(resp);
        System.out.println("Execute latest_class_health: " + resp.getBody());
    }
}
