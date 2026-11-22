package cn.oyzh.easyshell.sqlserver.procedure;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.sqlserver.routine.SqlServerRoutineSchema;

/**
 * SqlServer存储过程
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerProcedure extends SqlServerRoutineSchema implements ObjectCopier<SqlServerProcedure> {

    @Override
    public void copy(SqlServerProcedure procedure) {
        this.setParams(procedure.getParams());
//        this.setDefiner(procedure.getDefiner());
//        this.setComment(procedure.getComment());
        this.setDefinition(procedure.getDefinition());
        this.setSecurityType(procedure.getSecurityType());
        this.setCharacteristic(procedure.getCharacteristic());
    }
}
