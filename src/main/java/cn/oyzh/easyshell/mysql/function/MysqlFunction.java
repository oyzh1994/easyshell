package cn.oyzh.easyshell.mysql.function;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineParam;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineSchema;

import java.util.ArrayList;
import java.util.List;

/**
 * MySQL函数
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlFunction extends MysqlRoutineSchema implements ObjectCopier<MysqlFunction> {

    /**
     * 返回参数
     */
    private MysqlRoutineParam returnParam;

    @Override
    public void setParams(List<MysqlRoutineParam> params) {
        List<MysqlRoutineParam> paramsList = new ArrayList<>();
        for (MysqlRoutineParam param : params) {
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
    public void copy(MysqlFunction function) {
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
    public MysqlRoutineParam getReturnParam() {
        return returnParam;
    }

    /**
     * 设置返回参数
     *
     * @param returnParam 返回参数
     */
    public void setReturnParam(MysqlRoutineParam returnParam) {
        this.returnParam = returnParam;
    }
}
