package cn.oyzh.easyshell.mysql.event;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.DBObjectStatus;
import cn.oyzh.easyshell.util.mysql.DBUtil;

import java.util.Date;

/**
 * @author oyzh
 * @since 2024/09/09
 */
public class MysqlEvent extends DBObjectStatus implements ObjectCopier<MysqlEvent>, ObjectComparator<MysqlEvent> {

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
     * 状态
     */
    private String status;

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
        this.setEnds(obj.getEnds());
        this.setType(obj.getType());
        this.setStarts(obj.getStarts());
        this.setStatus(obj.getStatus());
        this.setDefiner(obj.getDefiner());
        this.setComment(obj.getComment());
        this.setExecuteAt(obj.getExecuteAt());
        this.setDefinition(obj.getDefinition());
        this.setOnCompletion(obj.getOnCompletion());
        this.setIntervalValue(obj.getIntervalValue());
        this.setIntervalField(obj.getIntervalField());
        this.setCreateDefinition(obj.getCreateDefinition());
    }

    public boolean isNew() {
        return StringUtil.isBlank(this.getDefinition());
    }

    @Override
    public boolean compare(MysqlEvent value) {
        if (value == null) {
            return false;
        }
        return StringUtil.equalsIgnoreCase(this.dbName, value.dbName) && StringUtil.equalsIgnoreCase(this.name, value.name);
    }

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

    public boolean isOnTimeType() {
        return StringUtil.equalsIgnoreCase("ONE TIME", this.type);
    }

    public boolean isRecurringType() {
        return StringUtil.equalsIgnoreCase("RECURRING", this.type);
    }

    public Object executeAt() {
        if (this.executeAt instanceof Date date) {
            Object val = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
            return DBUtil.wrapData(val);
        }
        return this.executeAt;
    }

    public Object starts() {
        if (this.starts instanceof Date date) {
            Object val = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
            return DBUtil.wrapData(val);
        }
        return this.starts;
    }

    public Object ends() {
        if (this.ends instanceof Date date) {
            Object val = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
            return DBUtil.wrapData(val);
        }
        return this.ends;
    }

    public boolean isEnable() {
        return StringUtil.equalsIgnoreCase("ENABLE", this.status);
    }

    public boolean isPreserve() {
        return StringUtil.equalsIgnoreCase("PRESERVE", this.onCompletion);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getIntervalValue() {
        return intervalValue;
    }

    public void setIntervalValue(Integer intervalValue) {
        this.intervalValue = intervalValue;
    }

    public String getIntervalField() {
        return intervalField;
    }

    public void setIntervalField(String intervalField) {
        this.intervalField = intervalField;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDefiner() {
        return definer;
    }

    public void setDefiner(String definer) {
        this.definer = definer;
    }

    public Object getExecuteAt() {
        return executeAt;
    }

    public void setExecuteAt(Object executeAt) {
        this.executeAt = executeAt;
    }

    public Object getStarts() {
        return starts;
    }

    public void setStarts(Object starts) {
        this.starts = starts;
    }

    public Integer getStartIntervalValue() {
        return startIntervalValue;
    }

    public void setStartIntervalValue(Integer startIntervalValue) {
        this.startIntervalValue = startIntervalValue;
    }

    public String getStartIntervalField() {
        return startIntervalField;
    }

    public void setStartIntervalField(String startIntervalField) {
        this.startIntervalField = startIntervalField;
    }

    public Object getEnds() {
        return ends;
    }

    public void setEnds(Object ends) {
        this.ends = ends;
    }

    public Integer getEndIntervalValue() {
        return endIntervalValue;
    }

    public void setEndIntervalValue(Integer endIntervalValue) {
        this.endIntervalValue = endIntervalValue;
    }

    public String getEndIntervalField() {
        return endIntervalField;
    }

    public void setEndIntervalField(String endIntervalField) {
        this.endIntervalField = endIntervalField;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getOnCompletion() {
        return onCompletion;
    }

    public void setOnCompletion(String onCompletion) {
        this.onCompletion = onCompletion;
    }

    public String getCreateDefinition() {
        return createDefinition;
    }
}
