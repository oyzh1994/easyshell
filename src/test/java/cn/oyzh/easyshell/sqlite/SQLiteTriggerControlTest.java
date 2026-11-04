package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaObject;
import cn.oyzh.easyshell.sqlite.generator.trigger.SQLiteTriggerCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.trigger.SQLiteTrigger;
import cn.oyzh.easyshell.sqlite.trigger.SQLiteTriggerControl;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import javafx.application.Platform;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * SQLite触发器设计组件测试。
 *
 * @since 2026-11-03
 */
public class SQLiteTriggerControlTest {

    @Test
    public void testDefinitionContainsOnlyUserBody() {
        SQLiteSchemaObject object = new SQLiteSchemaObject();
        object.setName("before insert");
        object.setTableName("item");
        object.setSql("CREATE TRIGGER \"before insert\" AFTER UPDATE ON item "
                + "BEGIN\n    UPDATE audit SET value = NEW.name;\nEND;");

        SQLiteTrigger trigger = SQLiteTrigger.of(object);

        assertEquals("UPDATE audit SET value = NEW.name", trigger.getDefinition());
        assertEquals("AFTER UPDATE", trigger.getPolicy());
        trigger.setPolicy("BEFORE UPDATE");
        assertTrue(trigger.isChangedDefinition());
    }

    @Test
    public void testCreateSqlIsAssembledFromTriggerAttributes() {
        SQLiteTrigger trigger = new SQLiteTrigger();
        trigger.setName("trg_item");
        trigger.setTableName("item");
        trigger.setPolicy("BEFORE INSERT");
        trigger.setDefinition("UPDATE audit SET value = NEW.name;");

        String sql = SQLiteTriggerCreateSqlGenerator.generateSqlSingle(trigger, "item");

        assertTrue(sql.startsWith("CREATE TRIGGER \"trg_item\" BEFORE INSERT ON \"item\""));
        assertTrue(sql.contains("BEGIN\nUPDATE audit SET value = NEW.name;"));
        assertTrue(sql.endsWith("\nEND;"));
    }

    @Test
    public void testNewTriggerDoesNotGenerateDefinition() {
        SQLiteTrigger trigger = SQLiteTrigger.newTrigger("item");

        assertEquals("", trigger.getDefinition());
        assertEquals("AFTER INSERT", trigger.getPolicy());
    }

    @Test
    public void testGeneratorDoesNotValidateCallerData() {
        SQLiteTrigger trigger = new SQLiteTrigger();
        trigger.setName("trg_item");
        trigger.setPolicy("AFTER INSERT");
        trigger.setDefinition("UPDATE item SET name = NEW.name;");

        String sql = SQLiteTriggerCreateSqlGenerator.generateSqlSingle(trigger, "item");

        assertTrue(sql.contains("UPDATE item SET name = NEW.name;"));
    }

    @Test
    public void testControlInitializationDoesNotMarkExistingTriggerChanged() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue(startup.await(20, TimeUnit.SECONDS));
        CountDownLatch loaded = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                SQLiteTrigger trigger = new SQLiteTrigger();
                trigger.setName("trg_item");
                trigger.setTableName("item");
                trigger.setPolicy("AFTER INSERT");
                trigger.setDefinition("UPDATE audit SET value = NEW.name;");
                SQLiteTriggerControl control = SQLiteTriggerControl.of(trigger);

                ClearableTextField nameControl = control.getNameControl();
                control.getPolicyControl();
                control.getDefinitionControl();

                assertFalse(control.isChanged());
                nameControl.setText("trg_item_updated");
                assertTrue(control.isChanged());
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                loaded.countDown();
            }
        });
        assertTrue(loaded.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Trigger control initialization failed", error.get());
        }
    }

    @Test
    public void testTableDesignerMatchesMysqlAndDamengColumns() throws Exception {
        String source = java.nio.file.Files.readString(java.nio.file.Path.of(
                "src/main/resources/tabs/sqlite/table/shellSqliteTableDesignTab.fxml"));
        int indexStart = source.indexOf("<FixedTab text=\"%base.indexes\"");
        int foreignKeyStart = source.indexOf("<FixedTab text=\"%base.foreignKeys\"");
        int triggerStart = source.indexOf("<FixedTab text=\"%base.triggers\"");
        int checkStart = source.indexOf("<FixedTab text=\"%base.checks\"");

        assertEquals(1, occurrences(source.substring(indexStart, foreignKeyStart), "valueName=\"nameControl\""));
        assertTrue(source.substring(indexStart, foreignKeyStart).contains("valueName=\"columnControl\""));
        assertTrue(source.substring(indexStart, foreignKeyStart).contains("valueName=\"typeControl\""));
        assertTrue(source.substring(indexStart, foreignKeyStart).contains("valueName=\"methodControl\""));
        assertTrue(source.substring(indexStart, foreignKeyStart).contains("valueName=\"whereControl\""));
        assertEquals(1, occurrences(source.substring(triggerStart, checkStart), "valueName=\"nameControl\""));
        assertTrue(source.substring(triggerStart, checkStart).contains("valueName=\"policyControl\""));
        assertTrue(source.substring(triggerStart, checkStart).contains("valueName=\"definitionControl\""));

        String controller = java.nio.file.Files.readString(java.nio.file.Path.of(
                "src/main/java/cn/oyzh/easyshell/tabs/sqlite/table/ShellSqliteTableDesignTabController.java"));
        assertTrue(controller.contains("this.initIndexTable();"));
        org.junit.Assert.assertFalse(controller.contains("column.setName(\"id\")"));
    }

    private int occurrences(String source, String value) {
        int count = 0;
        int index = source.indexOf(value);
        while (index >= 0) {
            count++;
            index = source.indexOf(value, index + value.length());
        }
        return count;
    }
}
