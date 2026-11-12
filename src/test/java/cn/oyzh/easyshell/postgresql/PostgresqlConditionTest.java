package cn.oyzh.easyshell.postgresql;

import cn.oyzh.easyshell.postgresql.condition.PostgresqlBetweenCondition;
import cn.oyzh.easyshell.postgresql.condition.PostgresqlContainsCondition;
import cn.oyzh.easyshell.postgresql.condition.PostgresqlEmptyCondition;
import cn.oyzh.easyshell.postgresql.condition.PostgresqlEndWithCondition;
import cn.oyzh.easyshell.postgresql.condition.PostgresqlEqCondition;
import cn.oyzh.easyshell.postgresql.condition.PostgresqlInListCondition;
import cn.oyzh.easyshell.postgresql.condition.PostgresqlNotContainsCondition;
import cn.oyzh.easyshell.postgresql.condition.PostgresqlNotBetweenCondition;
import cn.oyzh.easyshell.postgresql.condition.PostgresqlNotEndWithCondition;
import cn.oyzh.easyshell.postgresql.condition.PostgresqlNotInListCondition;
import cn.oyzh.easyshell.postgresql.condition.PostgresqlNotStartWithCondition;
import cn.oyzh.easyshell.postgresql.condition.PostgresqlNullCondition;
import cn.oyzh.easyshell.postgresql.condition.PostgresqlStartWithCondition;
import cn.oyzh.fx.db.DBDialect;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PostgresqlConditionTest {

    @Test
    public void reusesBaseConditionDesign() {
        assertEquals("= 'a'", PostgresqlEqCondition.INSTANCE.wrapCondition("name", "a"));
        assertEquals("LIKE '%a%'", PostgresqlContainsCondition.INSTANCE.wrapCondition("name", "a"));
        assertEquals("NOT LIKE '%a%'", PostgresqlNotContainsCondition.INSTANCE.wrapCondition("name", "a"));
        assertEquals("IS NULL", PostgresqlNullCondition.INSTANCE.wrapCondition("name", "IS NULL"));
        assertEquals("= ''", PostgresqlEmptyCondition.INSTANCE.wrapCondition("name", "= ''"));
        assertEquals("LIKE 'a%'", PostgresqlStartWithCondition.INSTANCE.wrapCondition("name", "a"));
        assertEquals("LIKE '%a'", PostgresqlEndWithCondition.INSTANCE.wrapCondition("name", "a"));
        assertEquals("BETWEEN 1 AND 9", PostgresqlBetweenCondition.INSTANCE.wrapCondition("age", List.of(1, 9)));
        assertEquals("NOT BETWEEN 1 AND 9", PostgresqlNotBetweenCondition.INSTANCE.wrapCondition("age", List.of(1, 9)));
        assertEquals("IN ('a', 'b')", PostgresqlInListCondition.INSTANCE.wrapCondition("name", "a,b"));
        assertEquals("NOT IN ('a', 'b')", PostgresqlNotInListCondition.INSTANCE.wrapCondition("name", "a,b"));
        assertEquals(DBDialect.POSTGRESQL, PostgresqlEqCondition.INSTANCE.getDialect());
    }

    @Test
    public void negativeConditionsExtendPositiveConditions() {
        assertTrue(PostgresqlNotContainsCondition.INSTANCE instanceof PostgresqlContainsCondition);
        assertTrue(PostgresqlNotBetweenCondition.INSTANCE instanceof PostgresqlBetweenCondition);
        assertTrue(PostgresqlNotInListCondition.INSTANCE instanceof PostgresqlInListCondition);
        assertTrue(PostgresqlNotStartWithCondition.INSTANCE instanceof PostgresqlStartWithCondition);
        assertTrue(PostgresqlNotEndWithCondition.INSTANCE instanceof PostgresqlEndWithCondition);
    }
}
