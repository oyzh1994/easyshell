package cn.oyzh.easyshell.trees.h2.sequence;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.h2.sequence.H2Sequence;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.trees.h2.ShellH2TreeItem;
import cn.oyzh.easyshell.trees.h2.schema.ShellH2SchemaTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.svg.glyph.database.SequenceSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * H2 序列分组节点。
 */
public class ShellH2SequencesTreeItem extends ShellH2TreeItem<ShellH2SequencesTreeItem.Value> {

    public ShellH2SequencesTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new Value(this));
    }

    @Override
    public ShellH2SchemaTreeItem parent() {
        return (ShellH2SchemaTreeItem) super.parent();
    }

    public String schema() {
        return this.parent().schema();
    }

    public ShellH2Client client() {
        return this.parent().client();
    }

    public ShellConnect info() {
        return this.parent().info();
    }

    public Integer getSequenceSize() {
        return !this.isLoaded() || this.isLoading() ? null : this.unfilteredChildrenSize();
    }

    @Override
    public void loadChild() {
        if (this.isLoading() || this.isLoaded()) {
            return;
        }
        this.setLoading(true);
        try {
            this.clearChild();
            List<TreeItem<?>> items = new ArrayList<>();
            for (H2Sequence sequence : this.client().selectSequences(this.schema())) {
                items.add(new Item(sequence, this.getTreeView()));
            }
            this.setChild(items);
            this.setLoaded(true);
            this.doFilter();
            this.doSort();
            this.expend();
            this.refresh();
        } finally {
            this.setLoading(false);
        }
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (!this.isLoaded()) {
            this.loadChild();
        } else {
            super.onPrimaryDoubleClick();
        }
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        items.add(MenuItemHelper.addSequence(this::addSequence));
        FXMenuItem reload = MenuItemHelper.refreshData(this::reloadChild);
        items.add(reload);
        return items;
    }

    private void addSequence() {
        H2Sequence sequence = new H2Sequence();
        sequence.setSchema(this.schema());
        this.parent().parent().tabPane().openSequenceDesign(sequence, this);
    }

    public void addSequence(H2Sequence sequence) {
        if (sequence != null && this.richChildren().stream()
                .filter(Item.class::isInstance)
                .map(Item.class::cast)
                .noneMatch(item -> StringUtil.equalsIgnoreCase(
                        item.sequence.getName(), sequence.getName()))) {
            this.addChild(new Item(sequence, this.getTreeView()));
            this.doFilter();
            this.doSort();
            this.refresh();
        }
    }

    static class Value extends RichTreeItemValue {
        Value(ShellH2SequencesTreeItem item) {
            super(item);
        }

        @Override
        public ShellH2SequencesTreeItem item() {
            return (ShellH2SequencesTreeItem) super.item();
        }

        @Override
        public String name() {
            return I18nHelper.sequence();
        }

        @Override
        public SVGGlyph graphic() {
            if (super.graphic() == null) {
                super.graphic(new SequenceSVGGlyph());
                super.graphic().disableTheme();
            }
            return super.graphic();
        }

        @Override
        public String extra() {
            Integer size = this.item().getSequenceSize();
            return size == null ? super.extra() : " (" + size + ")";
        }

        @Override
        public Color extraColor() {
            return Color.valueOf("#228B22");
        }
    }

    static class Item extends ShellH2TreeItem<Item.Value> {
        private final H2Sequence sequence;

        Item(H2Sequence sequence, RichTreeView treeView) {
            super(treeView);
            super.setFilterable(true);
            this.sequence = sequence;
            this.setValue(new Value(this));
        }

        @Override
        public ShellH2SequencesTreeItem parent() {
            return (ShellH2SequencesTreeItem) super.parent();
        }

        private ShellH2SchemaTreeItem schemaItem() {
            return this.parent().parent();
        }

        private String schema() {
            return this.parent().schema();
        }

        @Override
        public List<MenuItem> getMenuItems() {
            List<MenuItem> items = new ArrayList<>();
            items.add(MenuItemHelper.designSequence(this::design));
            items.add(MenuItemHelper.renameSequence(this::rename));
            items.add(MenuItemHelper.deleteSequence(this::delete));
            items.add(MenuItemHelper.separator());
            items.add(MenuItemHelper.cloneSequence(this::cloneSequence));
            return items;
        }

        private void design() {
            this.schemaItem().parent().tabPane().openSequenceDesign(
                    this.sequence, this.parent());
        }

        @Override
        public void onPrimaryDoubleClick() {
            this.design();
        }

        @Override
        public void rename() {
            String name = MessageBox.prompt(I18nHelper.pleaseInputName(),
                    this.sequence.getName(), this.getWindow());
            if (name == null || StringUtil.equals(name, this.sequence.getName())) {
                return;
            }
            try {
                H2Sequence sequence = this.schemaItem().client().renameSequence(
                        this.schema(), this.sequence.getName(), name.trim());
                this.schemaItem().parent().tabPane().closeSequenceDesignTabs(
                        this.schema(), this.sequence.getName());
                this.sequence.setName(sequence.getName());
                this.parent().reloadChild();
            } catch (Exception ex) {
                MessageBox.exception(ex, this.getWindow());
            }
        }

        @Override
        public void delete() {
            if (!MessageBox.confirm(ShellI18nHelper.confirmDelete(this.sequence.getName()),
                    this.getWindow())) {
                return;
            }
            try {
                this.schemaItem().client().dropSequence(
                        this.schema(), this.sequence.getName());
                this.schemaItem().parent().tabPane().closeSequenceDesignTabs(
                        this.schema(), this.sequence.getName());
                this.remove();
            } catch (Exception ex) {
                MessageBox.exception(ex, this.getWindow());
            }
        }

        private void cloneSequence() {
            StageManager.showMask(this.getWindow(), () -> {
                try {
                    String name = this.sequence.getName() + DBUtil.genCloneName();
                    this.schemaItem().client().cloneSequence(
                            this.schema(), this.sequence.getName(), name);
                    this.parent().reloadChild();
                } catch (Exception ex) {
                    MessageBox.exception(ex, this.getWindow());
                }
            });
        }

        static class Value extends RichTreeItemValue {
            Value(Item item) {
                super(item);
            }

            @Override
            public String name() {
                return ((Item) super.item()).sequence.getName();
            }

            @Override
            public SVGGlyph graphic() {
                if (super.graphic() == null) {
                    super.graphic(new SequenceSVGGlyph());
                    super.graphic().disableTheme();
                }
                return super.graphic();
            }
        }
    }
}
