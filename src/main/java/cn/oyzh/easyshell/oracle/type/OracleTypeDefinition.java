package cn.oyzh.easyshell.oracle.type;

import cn.oyzh.fx.db.DBTypeDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Oracle用户自定义类型定义
 *
 * @author oyzh
 * @since 2026-11-20
 */
public class OracleTypeDefinition implements DBTypeDefinition {

    /**
     * 类型名称
     */
    private String name;

    /**
     * 类型类型，OBJECT/COLLECTION
     */
    private String objectType;

    /**
     * 创建语句
     */
    private String createDefinition;

    /**
     * 类型体创建语句
     */
    private String bodyDefinition;

    /**
     * 依赖的同模式类型
     */
    private List<String> dependencies = new ArrayList<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getObjectType() {
        return objectType;
    }

    @Override
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    @Override
    public String getCreateDefinition() {
        return createDefinition;
    }

    @Override
    public void setCreateDefinition(String createDefinition) {
        this.createDefinition = createDefinition;
    }

    public String getBodyDefinition() {
        return bodyDefinition;
    }

    public void setBodyDefinition(String bodyDefinition) {
        this.bodyDefinition = bodyDefinition;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies == null ? new ArrayList<>() : dependencies;
    }
}
