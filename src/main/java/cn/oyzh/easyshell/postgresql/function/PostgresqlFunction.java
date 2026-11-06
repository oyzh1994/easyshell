package cn.oyzh.easyshell.postgresql.function;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.postgresql.routine.PostgresqlRoutineParam;
import cn.oyzh.easyshell.postgresql.routine.PostgresqlRoutineSchema;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL函数
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class PostgresqlFunction extends PostgresqlRoutineSchema implements ObjectCopier<PostgresqlFunction> {

    /**
     * 返回参数
     */
    private PostgresqlRoutineParam returnParam;

    @Override
    public void setParams(List<PostgresqlRoutineParam> params) {
        List<PostgresqlRoutineParam> paramsList = new ArrayList<>();
        for (PostgresqlRoutineParam param : params) {
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
    public void copy(PostgresqlFunction function) {
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
    public PostgresqlRoutineParam getReturnParam() {
        return returnParam;
    }

    /**
     * 设置返回参数
     *
     * @param returnParam 返回参数
     */
    public void setReturnParam(PostgresqlRoutineParam returnParam) {
        this.returnParam = returnParam;
    }
}
