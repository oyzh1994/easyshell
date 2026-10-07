package cn.oyzh.easyshell.mysql.event;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBName;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.Date;

/**
 * MySQL事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlEvent extends DBObject implements DBName, ObjectCopier<MysqlEvent>, ObjectComparator<MysqlEvent> {

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     * ONE TIME 单次
     * RECURRING 循环
     */
    private String type;

    /**
     * 定期-循环值
     */
    private Integer intervalValue;

    /**
     * 定期-循环类型
     */
    private String intervalField;

    /**
     * 事件状态
     */
    private String eventStatus;

    /**
     * 定义者
     */
    private String definer;

    /**
     * 单次-执行时间
     */
    private Object executeAt;

    /**
     * 定期-开始时间
     */
    private Object starts;

    /**
     * 定期-开始循环值
     */
    private Integer startIntervalValue;

    /**
     * 定期-开始循环类型
     */
    private String startIntervalField;

    /**
     * 定期-结束时间
     */
    private Object ends;

    /**
     * 定期-结束循环值
     */
    private Integer endIntervalValue;

    /**
     * 定期-结束循环类型
     */
    private String endIntervalField;

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 注释
     */
    private String comment;

    /**
     * 定义
     */
    private String definition;

    /**
     * 完成时
     */
    private String onCompletion;

    /**
     * 创建定义
     */
    private String createDefinition;

    @Override
    public void copy(MysqlEvent obj) {
        this.setName(obj.getName());
        this.setEnds(obj.getEnds());
        this.setType(obj.getType());
        this.setStarts(obj.getStarts());
        this.setDefiner(obj.getDefiner());
        this.setComment(obj.getComment());
        this.setExecuteAt(obj.getExecuteAt());
        this.setDefinition(obj.getDefinition());
        this.setEventStatus(obj.getEventStatus());
        this.setOnCompletion(obj.getOnCompletion());
        this.setIntervalValue(obj.getIntervalValue());
        this.setIntervalField(obj.getIntervalField());
        this.setCreateDefinition(obj.getCreateDefinition());
        this.setEndIntervalValue(obj.getEndIntervalValue());
        this.setEndIntervalField(obj.getEndIntervalField());
        this.setStartIntervalValue(obj.getStartIntervalValue());
        this.setStartIntervalField(obj.getStartIntervalField());
    }

//    public boolean isNew() {
//        return StringUtil.isBlank(this.getDefinition());
//    }

    @Override
    public boolean compare(MysqlEvent value) {
        if (value == null) {
            return false;
        }
        return StringUtil.equalsIgnoreCase(this.dbName, value.dbName) && StringUtil.equalsIgnoreCase(this.name, value.name);
    }

    /**
     * 设置创建定义
     *
     * @param createDefinition 创建定义
     */
    public void setCreateDefinition(String createDefinition) {
        this.createDefinition = createDefinition;
        if (StringUtil.isNotBlank(createDefinition)) {
            String[] arr = createDefinition.split(" ");
            for (String string : arr) {
                if (StringUtil.startWithIgnoreCase(string, "DEFINER=")) {
                    this.definer = string.substring(8);
                    break;
                }
            }
        }
    }

    /**
     * 是否为单次类型
     *
     * @return 结果
     */
    public boolean isOnTimeType() {
        return StringUtil.equalsIgnoreCase("ONE TIME", this.type);
    }

    /**
     * 是否为循环类型
     *
     * @return 结果
     */
    public boolean isRecurringType() {
        return StringUtil.equalsIgnoreCase("RECURRING", this.type);
    }

    /**
     * 获取执行时间
     *
     * @return 执行时间
     */
    public Object executeAt() {
        if (this.executeAt instanceof Date date) {
            Object val = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
            return DBUtil.wrapData(val, DBDialect.MYSQL);
        }
        return this.executeAt;
    }

    /**
     * 获取开始时间
     *
     * @return 开始时间
     */
    public Object starts() {
        if (this.starts instanceof Date date) {
            Object val = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
            return DBUtil.wrapData(val, DBDialect.MYSQL);
        }
        return this.starts;
    }

    /**
     * 获取结束时间
     *
     * @return 结束时间
     */
    public Object ends() {
        if (this.ends instanceof Date date) {
            Object val = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
            return DBUtil.wrapData(val, DBDialect.MYSQL);
        }
        return this.ends;
    }

    /**
     * 是否启用
     *
     * @return 结果
     */
    public boolean isEnable() {
        return StringUtil.equalsIgnoreCase("ENABLE", this.eventStatus);
    }

    /**
     * 完成后是否保留
     *
     * @return 结果
     */
    public boolean isPreserve() {
        return StringUtil.equalsIgnoreCase("PRESERVE", this.onCompletion);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取类型
     *
     * @return 类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置类型
     *
     * @param type 类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取循环值
     *
     * @return 循环值
     */
    public Integer getIntervalValue() {
        return intervalValue;
    }

    /**
     * 设置循环值
     *
     * @param intervalValue 循环值
     */
    public void setIntervalValue(Integer intervalValue) {
        this.intervalValue = intervalValue;
    }

    /**
     * 获取循环类型
     *
     * @return 循环类型
     */
    public String getIntervalField() {
        return intervalField;
    }

    /**
     * 设置循环类型
     *
     * @param intervalField 循环类型
     */
    public void setIntervalField(String intervalField) {
        this.intervalField = intervalField;
    }

    /**
     * 获取事件状态
     *
     * @return 事件状态
     */
    public String getEventStatus() {
        return eventStatus;
    }

    /**
     * 设置事件状态
     *
     * @param eventStatus 事件状态
     */
    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

    /**
     * 获取定义者
     *
     * @return 定义者
     */
    public String getDefiner() {
        return definer;
    }

    /**
     * 设置定义者
     *
     * @param definer 定义者
     */
    public void setDefiner(String definer) {
        this.definer = definer;
    }

    /**
     * 获取执行时间
     *
     * @return 执行时间
     */
    public Object getExecuteAt() {
        return executeAt;
    }

    /**
     * 设置执行时间
     *
     * @param executeAt 执行时间
     */
    public void setExecuteAt(Object executeAt) {
        this.executeAt = executeAt;
    }

    /**
     * 获取开始时间
     *
     * @return 开始时间
     */
    public Object getStarts() {
        return starts;
    }

    /**
     * 设置开始时间
     *
     * @param starts 开始时间
     */
    public void setStarts(Object starts) {
        this.starts = starts;
    }

    /**
     * 获取开始循环值
     *
     * @return 开始循环值
     */
    public Integer getStartIntervalValue() {
        return startIntervalValue;
    }

    /**
     * 设置开始循环值
     *
     * @param startIntervalValue 开始循环值
     */
    public void setStartIntervalValue(Integer startIntervalValue) {
        this.startIntervalValue = startIntervalValue;
    }

    /**
     * 获取开始循环类型
     *
     * @return 开始循环类型
     */
    public String getStartIntervalField() {
        return startIntervalField;
    }

    /**
     * 设置开始循环类型
     *
     * @param startIntervalField 开始循环类型
     */
    public void setStartIntervalField(String startIntervalField) {
        this.startIntervalField = startIntervalField;
    }

    /**
     * 获取结束时间
     *
     * @return 结束时间
     */
    public Object getEnds() {
        return ends;
    }

    /**
     * 设置结束时间
     *
     * @param ends 结束时间
     */
    public void setEnds(Object ends) {
        this.ends = ends;
    }

    /**
     * 获取结束循环值
     *
     * @return 结束循环值
     */
    public Integer getEndIntervalValue() {
        return endIntervalValue;
    }

    /**
     * 设置结束循环值
     *
     * @param endIntervalValue 结束循环值
     */
    public void setEndIntervalValue(Integer endIntervalValue) {
        this.endIntervalValue = endIntervalValue;
    }

    /**
     * 获取结束循环类型
     *
     * @return 结束循环类型
     */
    public String getEndIntervalField() {
        return endIntervalField;
    }

    /**
     * 设置结束循环类型
     *
     * @param endIntervalField 结束循环类型
     */
    public void setEndIntervalField(String endIntervalField) {
        this.endIntervalField = endIntervalField;
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * 设置数据库名称
     *
     * @param dbName 数据库名称
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * 获取注释
     *
     * @return 注释
     */
    public String getComment() {
        return comment;
    }

    /**
     * 设置注释
     *
     * @param comment 注释
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * 获取定义
     *
     * @return 定义
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * 设置定义
     *
     * @param definition 定义
     */
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    /**
     * 获取完成时动作
     *
     * @return 完成时动作
     */
    public String getOnCompletion() {
        return onCompletion;
    }

    /**
     * 设置完成时动作
     *
     * @param onCompletion 完成时动作
     */
    public void setOnCompletion(String onCompletion) {
        this.onCompletion = onCompletion;
    }

    /**
     * 获取创建定义
     *
     * @return 创建定义
     */
    public String getCreateDefinition() {
        return createDefinition;
    }
}
