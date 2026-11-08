package cn.oyzh.easyshell.trees.http.environment;

import cn.oyzh.easyshell.trees.http.ShellHttpTreeItem;
import cn.oyzh.easyshell.trees.http.ShellHttpTreeView;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.http.ShellHttpKeyValue;
import cn.oyzh.easyshell.store.http.ShellHttpEnvironmentStore;
import cn.oyzh.easyshell.store.http.ShellHttpWorkspaceStore;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.svg.glyph.AddSVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP 环境分类节点。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpEnvironmentsTreeItem extends ShellHttpTreeItem<ShellHttpEnvironmentsTreeItemValue> {

    /**
     * 构造环境分类节点
     *
     * @param treeView 树视图
     */
    public ShellHttpEnvironmentsTreeItem(ShellHttpTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        super.setSortable(false);
        this.setValue(new ShellHttpEnvironmentsTreeItemValue(this));
        this.setExpanded(true);
    }

    @Override
    public Object data() {
        return null;
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new FXMenuItem(new AddSVGGlyph(), ShellI18nHelper.httpAddEnvironment(), this::addEnvironment));
        return items;
    }

    @Override
    public void loadChild() {
        List<TreeItem<?>> items = new ArrayList<>();
        for (ShellHttpEnvironment environment : this.workspace().getEnvironments()) {
            items.add(new ShellHttpEnvironmentTreeItem(environment, this.getTreeView()));
        }
        this.setChild(items);
        this.setLoaded(true);
        this.setExpanded(true);
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.loadChild();
    }

    /**
     * 新增环境
     */
    public void addEnvironment() {
        String name = MessageBox.prompt(I18nHelper.pleaseInputName(), ShellI18nHelper.httpAddEnvironment(), this.getWindow());
        if (name == null) {
            return;
        }
        if (name.isBlank()) {
            MessageBox.warn(I18nHelper.pleaseInputName(), this.getWindow());
            return;
        }
        ShellHttpEnvironment environment = new ShellHttpEnvironment();
        environment.setIid(this.workspace().getIid());
        environment.setName(name.trim());
        environment.getValues().add(new ShellHttpKeyValue("baseUrl", ""));
        if (!ShellHttpEnvironmentStore.INSTANCE.insert(environment)) {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            return;
        }
        this.workspace().getEnvironments().add(environment);
        if (this.workspace().getActiveEnvironmentId() == null) {
            this.workspace().activeEnvironment(environment);
            ShellHttpWorkspaceStore.INSTANCE.saveSetting(this.workspace());
        }
        this.reloadTree();
        this.getTreeView().selectData(environment);
        this.getTreeView().refreshEnvironmentCombo();
        this.tabPane().refreshHomeTab();
    }

    @Override
    public void onPrimaryDoubleClick() {
        this.setExpanded(!this.isExpanded());
    }
}
