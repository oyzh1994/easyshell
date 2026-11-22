package cn.oyzh.easyshell.sqlserver.function;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.sqlserver.routine.SqlServerRoutineParam;
import cn.oyzh.easyshell.sqlserver.routine.SqlServerRoutineSchema;

import java.util.ArrayList;
import java.util.List;

/**
 * SqlServer函数
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class SqlServerFunction extends SqlServerRoutineSchema implements ObjectCopier<SqlServerFunction> {

    /**
     * 返回参数
     */
    private SqlServerRoutineParam returnParam;

    /**
     * 修改前的参数列表（用于判断函数签名是否变化）
     */
    private List<SqlServerRoutineParam> originalParams;

    /**
     * 修改前的返回参数
     */
    private SqlServerRoutineParam originalReturnParam;

    @Override
    public void setParams(List<SqlServerRoutineParam> params) {
        List<SqlServerRoutineParam> paramsList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(params)) {
            for (SqlServerRoutineParam param : params) {
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
    public void copy(SqlServerFunction function) {
        this.setParams(function.getParams());
        this.setOriginalParams(function.getOriginalParams());
        this.setOriginalReturnParam(function.getOriginalReturnParam());
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
    public SqlServerRoutineParam getReturnParam() {
        return returnParam;
    }

    /**
     * 设置返回参数
     *
     * @param returnParam 返回参数
     */
    public void setReturnParam(SqlServerRoutineParam returnParam) {
        this.returnParam = returnParam;
    }

    /**
     * 获取修改前的参数列表
     *
     * @return 参数列表
     */
    public List<SqlServerRoutineParam> getOriginalParams() {
        return originalParams;
    }

    /**
     * 设置修改前的参数列表
     *
     * @param originalParams 参数列表
     */
    public void setOriginalParams(List<SqlServerRoutineParam> originalParams) {
        this.originalParams = originalParams;
    }

    /**
     * 获取修改前的返回参数
     *
     * @return 返回参数
     */
    public SqlServerRoutineParam getOriginalReturnParam() {
        return originalReturnParam;
    }

    /**
     * 设置修改前的返回参数
     *
     * @param originalReturnParam 返回参数
     */
    public void setOriginalReturnParam(SqlServerRoutineParam originalReturnParam) {
        this.originalReturnParam = originalReturnParam;
    }
}
