package cn.oyzh.easyshell.dameng.sequence;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBSequence;

/**
 * 达梦检查约束对象
 *
 * @author oyzh
 * @since 2024/09/11
 */
public class DamengSequence extends DBObject implements DBSequence, ObjectCopier<DamengSequence>, ObjectComparator<DamengSequence> {

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
    private Long minVal;

    /**
     * 最大值
     */
    private Long maxVal;

    /**
     * 当前值
     */
    private Long currentVal;

    /**
     * 缓存
     */
    private Long cacheSize;

    /**
     * 递进
     */
    private Long incrementBy;

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
    public DamengSequence() {

    }

    /**
     * 构造检查约束对象
     *
     * @param name 检查约束名称
     */
    public DamengSequence(String name) {
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

    public void setMinVal(Long minVal) {
        this.minVal = minVal;
        super.putOriginalData("minVal", minVal);
    }

    public Long getMinVal() {
        return minVal;
    }

    public boolean isMinValChanged() {
        return super.checkOriginalData("minVal", this.minVal);
    }

    public Long getMaxVal() {
        return maxVal;
    }

    public void setMaxVal(Long maxVal) {
        this.maxVal = maxVal;
        super.putOriginalData("maxVal", maxVal);
    }

    public boolean isMaxValChanged() {
        return super.checkOriginalData("maxVal", this.maxVal);
    }

    public Long getCurrentVal() {
        return currentVal;
    }

    public void setCurrentVal(Long currentVal) {
        this.currentVal = currentVal;
        super.putOriginalData("currentVal", currentVal);
    }

    public boolean isCurrentValChanged() {
        return super.checkOriginalData("currentVal", this.currentVal);
    }

    public Long getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(Long cacheSize) {
        this.cacheSize = cacheSize;
        super.putOriginalData("cacheSize", cacheSize);
    }

    public boolean isCacheSizeChanged() {
        return super.checkOriginalData("cacheSize", this.cacheSize);
    }

    public Long getIncrementBy() {
        return incrementBy;
    }

    public void setIncrementBy(Long incrementBy) {
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
    public void copy(DamengSequence check) {
        if (check != null) {
            this.name = check.name;
            this.order = check.order;
            this.cycle = check.cycle;
            this.minVal = check.minVal;
            this.maxVal = check.maxVal;
            this.schema = check.schema;
            this.cacheSize = check.cacheSize;
            this.incrementBy = check.incrementBy;
            this.createDefinition = check.createDefinition;
        }
    }

    /** 获取库名称 */
    public String getSchema() {
        return schema;
    }

    /** 设置库名称 */
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
    public boolean compare(DamengSequence sequence) {
        if (sequence == null) {
            return false;
        }
        if (!StringUtil.equals(this.getSchema(), sequence.getSchema())) {
            return false;
        }
        return StringUtil.equals(this.getName(), sequence.getName());
    }
}
