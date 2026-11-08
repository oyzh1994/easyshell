package cn.oyzh.easyshell.trees.http.environment;

import cn.oyzh.easyshell.trees.http.ShellHttpTreeItem;
import cn.oyzh.easyshell.trees.http.ShellHttpTreeView;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.store.http.ShellHttpEnvironmentStore;
import cn.oyzh.easyshell.store.http.ShellHttpWorkspaceStore;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.svg.glyph.ApplySVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.CopySVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.DeleteSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.OpenSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.RenameSVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * HTTP 环境节点。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpEnvironmentTreeItem extends ShellHttpTreeItem<ShellHttpEnvironmentTreeItemValue> {

    /**
     * 环境
     */
    private final ShellHttpEnvironment environment;

    /**
     * 构造环境节点
     *
     * @param environment 环境
     * @param treeView    树视图
     */
    public ShellHttpEnvironmentTreeItem(ShellHttpEnvironment environment, ShellHttpTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.environment = environment;
        this.setValue(new ShellHttpEnvironmentTreeItemValue(this));
    }

    @Override
    public Object data() {
        return this.environment;
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new FXMenuItem(new OpenSVGGlyph(), I18nHelper.open(), this::open));
        items.add(new FXMenuItem(new ApplySVGGlyph(), ShellI18nHelper.httpSetActive(), this::activate));
        items.add(new FXMenuItem(new CopySVGGlyph(), ShellI18nHelper.httpDuplicate(), this::duplicate));
        items.add(new FXMenuItem(new RenameSVGGlyph(), I18nHelper.rename(), this::rename));
        items.add(new FXMenuItem(new DeleteSVGGlyph(), I18nHelper.delete(), this::delete));
        return items;
    }

    /**
     * 打开环境
     */
    public void open() {
        this.tabPane().openEnvironment(this);
    }

    /**
     * 激活环境
     */
    public void activate() {
        this.workspace().activeEnvironment(this.environment);
        ShellHttpWorkspaceStore.INSTANCE.saveSetting(this.workspace());
        this.reloadTree();
        this.getTreeView().selectData(this.environment);
        this.getTreeView().refreshEnvironmentCombo();
        this.tabPane().refreshHomeTab();
    }

    /**
     * 复制环境
     */
    public void duplicate() {
        ShellHttpEnvironment copy = this.environment.copy();
        copy.setName(this.environment.getName() + " " + ShellI18nHelper.httpCopySuffix());
        if (!ShellHttpEnvironmentStore.INSTANCE.insert(copy)) {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            return;
        }
        List<ShellHttpEnvironment> environments = this.workspace().getEnvironments();
        int index = environments.indexOf(this.environment);
        if (index >= 0) {
            environments.add(index + 1, copy);
        } else {
            environments.add(copy);
        }
        this.reloadTree();
        this.getTreeView().selectData(copy);
        this.getTreeView().refreshEnvironmentCombo();
    }

    /**
     * 重命名
     */
    @Override
    public void rename() {
        String name = MessageBox.prompt(I18nHelper.pleaseInputName(), this.environment.getName(), this.getWindow());
        if (name == null || Objects.equals(name, this.environment.getName())) {
            return;
        }
        if (name.isBlank()) {
            MessageBox.warn(I18nHelper.pleaseInputName(), this.getWindow());
            return;
        }
        this.environment.setName(name.trim());
        if (!ShellHttpEnvironmentStore.INSTANCE.update(this.environment)) {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            return;
        }
        this.refreshEnvironment();
        this.tabPane().renameEnvironmentTab(this.environment.getId(), this.environment.getName());
        this.getTreeView().refreshEnvironmentCombo();
        this.tabPane().refreshHomeTab();
    }

    /**
     * 删除
     */
    @Override
    public void delete() {
        if (!MessageBox.confirm(ShellI18nHelper.httpConfirmDelete(this.environment.getName()), this.getWindow())) {
            return;
        }
        this.tabPane().closeEnvironmentTab(this.environment.getId());
        ShellHttpEnvironmentStore.INSTANCE.delete(this.environment);
        this.workspace().getEnvironments().remove(this.environment);
        if (Objects.equals(this.environment.getId(), this.workspace().getActiveEnvironmentId())) {
            this.workspace().setActiveEnvironmentId(null);
            ShellHttpWorkspaceStore.INSTANCE.saveSetting(this.workspace());
        }
        this.reloadTree();
        this.getTreeView().refreshEnvironmentCombo();
        this.tabPane().refreshHomeTab();
        this.getTreeView().clearSelection();
    }

    /**
     * 刷新环境节点显示。
     */
    public void refreshEnvironment() {
        this.setValue(new ShellHttpEnvironmentTreeItemValue(this));
    }

    /**
     * 是否为当前环境
     *
     * @return 结果
     */
    public boolean isActive() {
        return this.environment != null
                && Objects.equals(this.environment.getId(), this.workspace().getActiveEnvironmentId());
    }

    /**
     * 获取环境
     *
     * @return 环境
     */
    public ShellHttpEnvironment environment() {
        return environment;
    }

    @Override
    public void onPrimaryDoubleClick() {
        this.open();
    }

    @Override
    public void onPrimarySingleClick() {
        this.open();
    }
}
