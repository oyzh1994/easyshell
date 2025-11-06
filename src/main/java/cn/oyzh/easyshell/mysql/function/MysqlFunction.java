package cn.oyzh.easyshell.mysql.function;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineParam;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineSchema;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/06/29
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

    public MysqlRoutineParam getReturnParam() {
        return returnParam;
    }

    public void setReturnParam(MysqlRoutineParam returnParam) {
        this.returnParam = returnParam;
    }
}
