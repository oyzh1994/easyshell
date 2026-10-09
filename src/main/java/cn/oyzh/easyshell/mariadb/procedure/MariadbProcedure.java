package cn.oyzh.easyshell.mariadb.procedure;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.mariadb.routine.MariadbRoutineSchema;

/**
 * MariaDB存储过程
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbProcedure extends MariadbRoutineSchema implements ObjectCopier<MariadbProcedure> {

    @Override
    public void copy(MariadbProcedure procedure) {
        this.setParams(procedure.getParams());
        this.setDefiner(procedure.getDefiner());
        this.setComment(procedure.getComment());
        this.setDefinition(procedure.getDefinition());
        this.setSecurityType(procedure.getSecurityType());
        this.setCharacteristic(procedure.getCharacteristic());
    }
}
