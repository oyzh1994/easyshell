package cn.oyzh.easyshell.oracle.function;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.oracle.routine.OracleRoutineParam;
import cn.oyzh.easyshell.oracle.routine.OracleRoutineSchema;

import java.util.ArrayList;
import java.util.List;

/**
 * Oracle函数
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class OracleFunction extends OracleRoutineSchema implements ObjectCopier<OracleFunction> {

    /**
     * 返回参数
     */
    private OracleRoutineParam returnParam;

    @Override
    public void setParams(List<OracleRoutineParam> params) {
        List<OracleRoutineParam> paramsList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(params)) {
            for (OracleRoutineParam param : params) {
                if (param.isReturnParam()) {
                    this.returnParam = param;
                } else {
                    paramsList.add(param);
                }
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
    public void copy(OracleFunction function) {
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
    public OracleRoutineParam getReturnParam() {
        return returnParam;
    }

    /**
     * 设置返回参数
     *
     * @param returnParam 返回参数
     */
    public void setReturnParam(OracleRoutineParam returnParam) {
        this.returnParam = returnParam;
    }
}
