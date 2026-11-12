package cn.oyzh.easyshell.postgresql;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PostgresqlTriggerBodyTest {

    @Test
    public void addsReturnPathToInlineBody() {
        String body = ShellPostgresqlHelper.normalizeTriggerBody(
                "UPDATE demo.t1 SET age = age + 1 WHERE id = NEW.id",
                "AFTER INSERT");

        assertEquals("BEGIN UPDATE demo.t1 SET age = age + 1 WHERE id = NEW.id;"
                + " RETURN NEW; END;", body);
    }

    @Test
    public void returnsOldRowForDeleteTrigger() {
        String body = ShellPostgresqlHelper.normalizeTriggerBody(
                "BEGIN\n"
                        + "DELETE FROM demo.audit WHERE id = OLD.id;\n"
                        + "END;",
                "BEFORE DELETE");

        assertEquals("BEGIN\n"
                + "DELETE FROM demo.audit WHERE id = OLD.id; RETURN OLD; END;", body);
    }

    @Test
    public void keepsExistingReturnPath() {
        String body = ShellPostgresqlHelper.normalizeTriggerBody(
                "BEGIN\n"
                        + "NEW.updated_at = CURRENT_TIMESTAMP;\n"
                        + "RETURN NEW;\n"
                        + "END;",
                "BEFORE UPDATE");

        assertEquals("BEGIN\n"
                + "NEW.updated_at = CURRENT_TIMESTAMP;\n"
                + "RETURN NEW; END;", body);
    }

    @Test
    public void separatesKeywordFromCompactBody() {
        String body = ShellPostgresqlHelper.normalizeTriggerBody(
                "BEGINinsert into demo.t2(name) values('a1');RETURN NEW;END;",
                "AFTER INSERT");

        assertEquals("BEGIN insert into demo.t2(name) values('a1');RETURN NEW; END;", body);
    }

    @Test
    public void separatesKeywordAfterDeclareBlock() {
        String body = ShellPostgresqlHelper.normalizeTriggerBody(
                "DECLARE value integer;BEGINinsert into demo.t2(name) values('a2');RETURN NEW;END;",
                "AFTER INSERT");

        assertEquals("DECLARE value integer;BEGIN insert into demo.t2(name) values('a2');"
                + "RETURN NEW; END;", body);
    }
}
