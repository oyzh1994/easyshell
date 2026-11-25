package cn.oyzh.easyshell.mongo.collection;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import org.bson.Document;

/**
 * MongoDB 集合设计参数。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class MongoCollectionOptions implements ObjectCopier<MongoCollectionOptions> {

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 集合名称
     */
    private String name;

    /**
     * 是否固定大小集合
     */
    private boolean capped;

    /**
     * 固定大小集合容量，单位字节
     */
    private Long sizeInBytes;

    /**
     * 最大文档数量
     */
    private Long maxDocuments;

    /**
     * 校验器
     */
    private Document validator;

    /**
     * 校验级别：off、strict、moderate
     */
    private String validationLevel;

    /**
     * 校验动作：error、warn
     */
    private String validationAction;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCapped() {
        return capped;
    }

    public void setCapped(boolean capped) {
        this.capped = capped;
    }

    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public Long getMaxDocuments() {
        return maxDocuments;
    }

    public void setMaxDocuments(Long maxDocuments) {
        this.maxDocuments = maxDocuments;
    }

    public Document getValidator() {
        return validator;
    }

    public void setValidator(Document validator) {
        this.validator = validator;
    }

    public String getValidationLevel() {
        return validationLevel;
    }

    public void setValidationLevel(String validationLevel) {
        this.validationLevel = validationLevel;
    }

    public String getValidationAction() {
        return validationAction;
    }

    public void setValidationAction(String validationAction) {
        this.validationAction = validationAction;
    }

    /**
     * 是否为新集合。
     *
     * @return 是否为新集合
     */
    public boolean isNew() {
        return StringUtil.isBlank(this.name);
    }

    @Override
    public void copy(MongoCollectionOptions options) {
        if (options != null) {
            this.setDbName(options.dbName);
            this.setName(options.name);
            this.setCapped(options.capped);
            this.setSizeInBytes(options.sizeInBytes);
            this.setMaxDocuments(options.maxDocuments);
            this.setValidator(options.validator);
            this.setValidationLevel(options.validationLevel);
            this.setValidationAction(options.validationAction);
        }
    }
}
