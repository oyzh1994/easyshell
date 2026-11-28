package cn.oyzh.easyshell.h2.view;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2Columns;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBView;

import java.util.ArrayList;
import java.util.List;

/**
 * H2 视图。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2View extends DBObject implements DBView, ObjectCopier<H2View>, ObjectComparator<H2View> {

    private String dbName;

    private String name;

    private String comment;

    private String definition;

    private boolean updatable;

    private H2ViewUpdateMapping updateMapping;

    private String createDefinition;

    private H2Columns columns;

    public H2View() {
    }

    public H2View(String name) {
        this.setName(name);
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    public String originalName() {
        return (String) super.getOriginalData("name");
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
        super.putOriginalData("comment", comment);
    }

    public String getDefinition() {
        return this.definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
        super.putOriginalData("definition", definition);
    }

    public String getCreateDefinition() {
        return this.createDefinition;
    }

    public void setCreateDefinition(String createDefinition) {
        this.createDefinition = createDefinition;
        super.putOriginalData("createDefinition", createDefinition);
    }

    @Override
    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
        super.putOriginalData("updatable", updatable);
    }

    @Override
    public boolean isUpdatable() {
        return this.updatable;
    }

    /**
     * 获取简单视图到基表的更新映射。
     *
     * @return 更新映射，复杂视图返回 null
     */
    public H2ViewUpdateMapping getUpdateMapping() {
        return updateMapping;
    }

    /**
     * 设置更新映射。只有成功映射到单表的视图才允许编辑。
     *
     * @param updateMapping 更新映射
     */
    public void setUpdateMapping(H2ViewUpdateMapping updateMapping) {
        this.updateMapping = updateMapping;
        this.setUpdatable(updateMapping != null);
    }

    public H2Columns getColumns() {
        return this.columns;
    }

    public void setColumns(List<H2Column> columns) {
        this.columns = columns == null ? null : new H2Columns(columns);
        super.putOriginalData("columns", this.columns);
    }

    public void setColumns(H2Columns columns) {
        this.columns = columns;
        super.putOriginalData("columns", columns);
    }

    @Override
    public void copy(H2View view) {
        if (view != null) {
            this.setName(view.getName());
            this.setDbName(view.getDbName());
            this.setComment(view.getComment());
            this.setUpdatable(view.isUpdatable());
            this.updateMapping = view.getUpdateMapping();
            this.setDefinition(view.getDefinition());
            this.setCreateDefinition(view.getCreateDefinition());
            this.setColumns(view.getColumns() == null
                    ? null : new H2Columns(new ArrayList<>(view.getColumns())));
        }
    }

    @Override
    public boolean compare(H2View view) {
        return view != null && java.util.Objects.equals(view.getName(), this.name)
                && java.util.Objects.equals(view.getDefinition(), this.definition)
                && view.isUpdatable() == this.updatable;
    }
}
