package cn.oyzh.easyshell.trees.snippet;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSnippet;
import cn.oyzh.easyshell.store.ShellSnippetStore;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 片段树
 *
 * @author oyzh
 * @since 2025-06-11
 */
public class ShellSnippetTreeView extends RichTreeView implements MenuItemAdapter {

    /**
     * 片段存储
     */
    private final ShellSnippetStore snippetStore = ShellSnippetStore.INSTANCE;

    @Override
    protected void initTreeView() {
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.initTreeView();
    }

    @Override
    protected void initRoot() {
        super.setRoot(new ShellSnippetRootTreeItem(this));
        this.root().expend();
        super.initRoot();
    }

    /**
     * 新增回调
     */
    private Consumer<ShellSnippet> addCallback;

    /**
     * 编辑回调
     */
    private Consumer<ShellSnippet> editCallback;

    /**
     * 删除回调
     */
    private Consumer<ShellSnippet> deleteCallback;

    public Consumer<ShellSnippet> getAddCallback() {
        return addCallback;
    }

    public void setAddCallback(Consumer<ShellSnippet> addCallback) {
        this.addCallback = addCallback;
    }

    public Consumer<ShellSnippet> getEditCallback() {
        return editCallback;
    }

    public void setEditCallback(Consumer<ShellSnippet> editCallback) {
        this.editCallback = editCallback;
    }

    public Consumer<ShellSnippet> getDeleteCallback() {
        return deleteCallback;
    }

    public void setDeleteCallback(Consumer<ShellSnippet> deleteCallback) {
        this.deleteCallback = deleteCallback;
    }

    /**
     * 新增片段
     *
     */
    public void addSnippet( ) {
        if (this.addCallback != null) {
            String name = MessageBox.prompt(I18nHelper.pleaseInputName());
            if (StringUtil.isBlank(name)) {
                return;
            }
            ShellSnippet snippet = new ShellSnippet();
            snippet.setName(name);
            this.addSnippet(snippet);
            this.snippetStore.insert(snippet);
            this.addCallback.accept(snippet);
        }
    }

    /**
     * 新增片段
     *
     * @param snippet 片段
     */
    public void addSnippet(ShellSnippet snippet) {
        this.root().addChild(new ShellSnippetTreeItem(snippet, this));
    }

    /**
     * 编辑片段
     *
     * @param snippet 片段
     */
    public void editSnippet(ShellSnippet snippet) {
        if (this.editCallback != null) {
            this.editCallback.accept(snippet);
        }
    }

    /**
     * 删除片段
     *
     * @param snippet 片段
     */
    public void deleteSnippet(ShellSnippet snippet) {
        if (this.deleteCallback != null) {
            this.deleteCallback.accept(snippet);
        }
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>(12);
        FXMenuItem addSnippet = MenuItemHelper.addSnippet("12", this::addSnippet);
        items.add(addSnippet);
        return items;
    }
}
