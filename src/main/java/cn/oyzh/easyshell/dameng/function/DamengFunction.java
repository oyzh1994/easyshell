package cn.oyzh.easyshell.dameng.function;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.dameng.routine.DamengRoutineParam;
import cn.oyzh.easyshell.dameng.routine.DamengRoutineSchema;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/06/29
 */
public class DamengFunction extends DamengRoutineSchema implements ObjectCopier<DamengFunction> {

    /**
     * 返回参数
     */
    private DamengRoutineParam returnParam;

    @Override
    public void setParams(List<DamengRoutineParam> params) {
        List<DamengRoutineParam> paramsList = new ArrayList<>();
        for (DamengRoutineParam param : params) {
            if (param.isReturnParam()) {
                this.returnParam = param;
            } else {
                paramsList.add(param);
            }
        }
        super.setParams(paramsList);
    }

    public String getReturnType() {
        return this.returnParam == null ? null : this.returnParam.getType();
    }

    @Override
    public void copy(DamengFunction function) {
        this.setParams(function.getParams());
//        this.setComment(function.getComment());
//        this.setDefiner(function.getDefiner());
        this.setDefinition(function.getDefinition());
        this.setReturnParam(function.getReturnParam());
        this.setSecurityType(function.getSecurityType());
        this.setCharacteristic(function.getCharacteristic());
    }

    public DamengRoutineParam getReturnParam() {
        return returnParam;
    }

    public void setReturnParam(DamengRoutineParam returnParam) {
        this.returnParam = returnParam;
    }
}
