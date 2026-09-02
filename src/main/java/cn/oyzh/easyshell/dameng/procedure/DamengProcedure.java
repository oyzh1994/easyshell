package cn.oyzh.easyshell.dameng.procedure;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.dameng.routine.DamengRoutineSchema;

/**
 * @author oyzh
 * @since 2024/06/29
 */
public class DamengProcedure extends DamengRoutineSchema implements ObjectCopier<DamengProcedure> {

    @Override
    public void copy(DamengProcedure procedure) {
        this.setParams(procedure.getParams());
//        this.setDefiner(procedure.getDefiner());
//        this.setComment(procedure.getComment());
        this.setDefinition(procedure.getDefinition());
        this.setSecurityType(procedure.getSecurityType());
        this.setCharacteristic(procedure.getCharacteristic());
    }
}
