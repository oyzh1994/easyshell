package cn.oyzh.easyshell.postgresql.procedure;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.postgresql.routine.PostgresqlRoutineSchema;

/**
 * PostgreSQL存储过程
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlProcedure extends PostgresqlRoutineSchema implements ObjectCopier<PostgresqlProcedure> {

    @Override
    public void copy(PostgresqlProcedure procedure) {
        this.setParams(procedure.getParams());
//        this.setDefiner(procedure.getDefiner());
//        this.setComment(procedure.getComment());
        this.setDefinition(procedure.getDefinition());
        this.setSecurityType(procedure.getSecurityType());
        this.setCharacteristic(procedure.getCharacteristic());
    }
}
