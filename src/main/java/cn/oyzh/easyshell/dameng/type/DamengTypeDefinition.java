package cn.oyzh.easyshell.dameng.type;

import cn.oyzh.fx.db.DBTypeDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * 达梦用户自定义类型定义
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class DamengTypeDefinition implements DBTypeDefinition {

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

    @Override
    public List<String> getDependencies() {
        return dependencies;
    }

    @Override
    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies == null ? new ArrayList<>() : dependencies;
    }
}
