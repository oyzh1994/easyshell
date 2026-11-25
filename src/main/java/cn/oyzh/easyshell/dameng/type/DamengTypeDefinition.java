package cn.oyzh.easyshell.dameng.type;

import java.util.ArrayList;
import java.util.List;

/**
 * 达梦用户自定义类型定义
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class DamengTypeDefinition {

    /**
     * 类型名称
     */
    private String name;

    /**
     * 对象类型，CLASS/TYPE
     */
    private String objectType;

    /**
     * 创建语句
     */
    private String createDefinition;

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

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getCreateDefinition() {
        return createDefinition;
    }

    public void setCreateDefinition(String createDefinition) {
        this.createDefinition = createDefinition;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies == null ? new ArrayList<>() : dependencies;
    }
}
