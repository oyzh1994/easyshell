package cn.oyzh.easyshell.query.sqlserver;

import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.schema.SqlServerSchema;
import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.easyshell.sqlserver.view.SqlServerView;
import cn.oyzh.fx.db.query.util.DBQueryUtil;
import javafx.scene.input.KeyCode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShellSqlServerQueryTokenAnalyzerTest {

    @Test
    public void openBracketShowsIdentifierPrompts() {
        String schemaName = "prompt_schema";
        String tableName = "prompt_table";
        String viewName = "prompt_view";
        String columnName = "prompt_column";

        SqlServerSchema schema = new SqlServerSchema();
        schema.setName(schemaName);
        SqlServerTable table = new SqlServerTable();
        table.setSchema(schemaName);
        table.setName(tableName);
        SqlServerView view = new SqlServerView();
        view.setSchema(schemaName);
        view.setName(viewName);
        SqlServerColumn column = new SqlServerColumn(columnName);
        column.setSchema(schemaName);
        column.setTableName(tableName);

        List<SqlServerSchema> schemas = ShellSqlServerQueryUtil.getSchemas();
        List<SqlServerTable> tables = ShellSqlServerQueryUtil.getTables();
        List<SqlServerView> views = ShellSqlServerQueryUtil.getViews();
        List<SqlServerColumn> columns = ShellSqlServerQueryUtil.getColumns();
        schemas.add(schema);
        tables.add(table);
        views.add(view);
        columns.add(column);
        try {
            assertTrue(DBQueryUtil.PROMPT_CODES.contains(KeyCode.OPEN_BRACKET));

            String sql = "SELECT * FROM [";
            ShellSqlServerQueryToken token = ShellSqlServerQueryTokenAnalyzer.INSTANCE
                    .currentToken(sql, sql.length());
            assertEquals(Character.valueOf('['), token.getToken());
            assertTrue(token.isOpenBracketContext());
            assertTrue(token.isEmpty());

            List<ShellSqlServerQueryPromptItem> prompts = ShellSqlServerQueryTokenAnalyzer.INSTANCE
                    .initPrompts(token, 0.5f);
            assertTrue(prompts.stream().anyMatch(item -> item.isSchemaType()
                    && schemaName.equals(item.getContent())));
            assertTrue(prompts.stream().anyMatch(item -> item.isTableType()
                    && tableName.equals(item.getContent())));
            assertTrue(prompts.stream().anyMatch(item -> item.isViewType()
                    && viewName.equals(item.getContent())));
            assertFalse(prompts.stream().anyMatch(item -> columnName.equals(item.getContent())));
            assertFalse(prompts.stream().anyMatch(ShellSqlServerQueryPromptItem::isKeywordType));
        } finally {
            schemas.remove(schema);
            tables.remove(table);
            views.remove(view);
            columns.remove(column);
        }
    }

    @Test
    public void openBracketKeepsTypedTextAsPromptInput() {
        String sql = "SELECT * FROM [prompt_";
        ShellSqlServerQueryToken token = ShellSqlServerQueryTokenAnalyzer.INSTANCE
                .currentToken(sql, sql.length());

        assertEquals(Character.valueOf('['), token.getToken());
        assertEquals("prompt_", token.getContent());
        assertEquals(sql.lastIndexOf('[') + 1, token.getStartIndex());
        assertEquals(sql.length(), token.getEndIndex());
    }
}
