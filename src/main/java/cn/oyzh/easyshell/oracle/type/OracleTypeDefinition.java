package cn.oyzh.easyshell.oracle.type;

import java.util.ArrayList;
import java.util.List;

/**
 * Oracle用户自定义类型定义
 *
 * @author oyzh
 * @since 2026-11-20
 */
public class OracleTypeDefinition {

    /**
     * 类型名称
     */
    private String name;

    /**
     * 类型类型，OBJECT/COLLECTION
     */
    private String typeCode;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getCreateDefinition() {
        return createDefinition;
    }

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
