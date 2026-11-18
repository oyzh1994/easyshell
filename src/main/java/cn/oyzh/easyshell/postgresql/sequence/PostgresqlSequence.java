package cn.oyzh.easyshell.postgresql.sequence;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBSequence;

import java.math.BigInteger;

/**
 * PostgreSQL检查约束对象
 *
 * @author oyzh
 * @since 2024/09/11
 */
public class PostgresqlSequence extends DBObject implements DBSequence, ObjectCopier<PostgresqlSequence>, ObjectComparator<PostgresqlSequence> {

    /**
     * 库名称
     */
    private String schema;

    /**
     * 名称
     */
    private String name;

    /**
     * 最小值
     */
    private BigInteger minVal;

    /**
     * 最大值
     */
    private BigInteger maxVal;

    /**
     * 当前值
     */
    private BigInteger currentVal;

    /**
     * 缓存
     */
    private BigInteger cacheSize;

    /**
     * 递进
     */
    private BigInteger incrementBy;

    /**
     * 循环
     */
    private boolean cycle;

    /**
     * 顺序
     */
    private boolean order;

    /**
     * 创建定义
     */
    private String createDefinition;

    /**
     * 构造检查约束对象
     */
    public PostgresqlSequence() {

    }

    /**
     * 构造检查约束对象
     *
     * @param name 检查约束名称
     */
    public PostgresqlSequence(String name) {
        this.name = name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    /**
     * 检查约束名称是否已变更
     *
     * @return 结果
     */
    public boolean isNameChanged() {
        return super.checkOriginalData("name", this.name);
    }

    /**
     * 获取原始名称
     *
     * @return 原始名称
     */
    public String originalName() {
        return (String) super.getOriginalData("name");
    }

    public void setMinVal(BigInteger minVal) {
        this.minVal = minVal;
        super.putOriginalData("minVal", minVal);
    }

    public BigInteger getMinVal() {
        return minVal;
    }

    public boolean isMinValChanged() {
        return super.checkOriginalData("minVal", this.minVal);
    }

    public BigInteger getMaxVal() {
        return maxVal;
    }

    public void setMaxVal(BigInteger maxVal) {
        this.maxVal = maxVal;
        super.putOriginalData("maxVal", maxVal);
    }

    public boolean isMaxValChanged() {
        return super.checkOriginalData("maxVal", this.maxVal);
    }

    public BigInteger getCurrentVal() {
        return currentVal;
    }

    public void setCurrentVal(BigInteger currentVal) {
        this.currentVal = currentVal;
        super.putOriginalData("currentVal", currentVal);
    }

    public boolean isCurrentValChanged() {
        return super.checkOriginalData("currentVal", this.currentVal);
    }

    public BigInteger getCacheSize() {
        return cacheSize == null ? null : this.cacheSize.max(BigInteger.ONE);
    }

    public void setCacheSize(BigInteger cacheSize) {
        this.cacheSize = cacheSize;
        super.putOriginalData("cacheSize", cacheSize);
    }

    public boolean isCacheSizeChanged() {
        return super.checkOriginalData("cacheSize", this.cacheSize);
    }

    public BigInteger getIncrementBy() {
        return incrementBy;
    }

    public void setIncrementBy(BigInteger incrementBy) {
        this.incrementBy = incrementBy;
        super.putOriginalData("incrementBy", incrementBy);
    }

    public boolean isIncrementByChanged() {
        return super.checkOriginalData("incrementBy", this.incrementBy);
    }

    public boolean isCycle() {
        return cycle;
    }

    public void setCycle(boolean cycle) {
        this.cycle = cycle;
        super.putOriginalData("cycle", cycle);
    }

    public boolean isCycleChanged() {
        return super.checkOriginalData("cycle", this.cycle);
    }

    public boolean isOrder() {
        return order;
    }

    public void setOrder(boolean order) {
        this.order = order;
        super.putOriginalData("order", order);
    }

    public boolean isOrderChanged() {
        return super.checkOriginalData("order", this.order);
    }

    @Override
    public void copy(PostgresqlSequence check) {
        if (check != null) {
            this.name = check.name;
            this.order = check.order;
            this.cycle = check.cycle;
            this.minVal = check.minVal;
            this.maxVal = check.maxVal;
            this.currentVal = check.currentVal;
            this.schema = check.schema;
            this.cacheSize = check.cacheSize;
            this.incrementBy = check.incrementBy;
            this.createDefinition = check.createDefinition;
        }
    }

    /**
     * 获取库名称
     */
    public String getSchema() {
        return schema;
    }

    /**
     * 设置库名称
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setCreateDefinition(String createDefinition) {
        this.createDefinition = createDefinition;
        super.putOriginalData("createDefinition", createDefinition);
    }

    public String getCreateDefinition() {
        return createDefinition;
    }

    @Override
    public boolean compare(PostgresqlSequence sequence) {
        if (sequence == null) {
            return false;
        }
        if (!StringUtil.equals(this.getSchema(), sequence.getSchema())) {
            return false;
        }
        return StringUtil.equals(this.getName(), sequence.getName());
    }
}
