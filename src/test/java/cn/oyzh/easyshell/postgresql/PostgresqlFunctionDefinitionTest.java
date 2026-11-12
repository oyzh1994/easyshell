package cn.oyzh.easyshell.postgresql;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PostgresqlFunctionDefinitionTest {

    @Test
    public void removesDefaultReturnExpressionForOutputParams() {
        String definition = """
                BEGIN
                    -- Routine body goes here...

                    RETURN '';
                END;
                """;

        assertEquals("""
                BEGIN
                    -- Routine body goes here...

                    RETURN;
                END;
                """, ShellPostgresqlHelper.normalizeFunctionBody(definition, true));
    }

    @Test
    public void keepsCustomBodyForOutputParams() {
        String definition = """
                BEGIN
                    c := 'value';
                    RETURN;
                END;
                """;

        assertEquals(definition, ShellPostgresqlHelper.normalizeFunctionBody(definition, true));
    }

    @Test
    public void keepsReturnExpressionWithoutOutputParams() {
        String definition = """
                BEGIN
                    -- Routine body goes here...

                    RETURN '';
                END;
                """;

        assertEquals(definition, ShellPostgresqlHelper.normalizeFunctionBody(definition, false));
    }
}
