package cn.oyzh.easyshell.dameng.function;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.dameng.routine.DamengRoutineParam;
import cn.oyzh.easyshell.dameng.routine.DamengRoutineSchema;

import java.util.ArrayList;
import java.util.List;

/**
 * 达梦函数
 *
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

    /**
     * 获取返回类型
     *
     * @return 返回类型
     */
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

    /**
     * 获取返回参数
     *
     * @return 返回参数
     */
    public DamengRoutineParam getReturnParam() {
        return returnParam;
    }

    /**
     * 设置返回参数
     *
     * @param returnParam 返回参数
     */
    public void setReturnParam(DamengRoutineParam returnParam) {
        this.returnParam = returnParam;
    }
}
