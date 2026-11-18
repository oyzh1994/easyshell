package cn.oyzh.easyshell.oracle.procedure;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.oracle.routine.OracleRoutineSchema;

/**
 * Oracle存储过程
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleProcedure extends OracleRoutineSchema implements ObjectCopier<OracleProcedure> {

    @Override
    public void copy(OracleProcedure procedure) {
        this.setParams(procedure.getParams());
//        this.setDefiner(procedure.getDefiner());
//        this.setComment(procedure.getComment());
        this.setDefinition(procedure.getDefinition());
        this.setSecurityType(procedure.getSecurityType());
        this.setCharacteristic(procedure.getCharacteristic());
    }
}
