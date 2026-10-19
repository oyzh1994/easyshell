package cn.oyzh.easyshell.mariadb.function;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.mariadb.routine.MariadbRoutineParam;
import cn.oyzh.easyshell.mariadb.routine.MariadbRoutineSchema;

import java.util.ArrayList;
import java.util.List;

/**
 * MariaDB函数
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbFunction extends MariadbRoutineSchema implements ObjectCopier<MariadbFunction> {

    /**
     * 返回参数
     */
    private MariadbRoutineParam returnParam;

    @Override
    public void setParams(List<MariadbRoutineParam> params) {
        List<MariadbRoutineParam> paramsList = new ArrayList<>();
        for (MariadbRoutineParam param : params) {
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
    public void copy(MariadbFunction function) {
        this.setParams(function.getParams());
        this.setComment(function.getComment());
        this.setDefiner(function.getDefiner());
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
    public MariadbRoutineParam getReturnParam() {
        return returnParam;
    }

    /**
     * 设置返回参数
     *
     * @param returnParam 返回参数
     */
    public void setReturnParam(MariadbRoutineParam returnParam) {
        this.returnParam = returnParam;
    }
}
