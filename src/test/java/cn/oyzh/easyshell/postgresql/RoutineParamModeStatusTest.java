package cn.oyzh.easyshell.postgresql;

import cn.oyzh.easyshell.dameng.routine.DamengRoutineParam;
import cn.oyzh.easyshell.postgresql.routine.PostgresqlRoutineParam;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RoutineParamModeStatusTest {

    @Test
    public void postgresqlModeChangeMarksParamChanged() {
        PostgresqlRoutineParam param = new PostgresqlRoutineParam();
        param.setMode("IN");
        assertFalse(param.isChanged());

        param.setMode("OUT");
        assertTrue(param.isChanged());
    }

    @Test
    public void damengModeChangeMarksParamChanged() {
        DamengRoutineParam param = new DamengRoutineParam();
        param.setMode("IN");
        assertFalse(param.isChanged());

        param.setMode("OUT");
        assertTrue(param.isChanged());
    }
}
