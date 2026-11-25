package cn.oyzh.easyshell.tabs.mongo.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mongo.view.MongoView;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mongo.view.ShellMongoViewsTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.mongo.ShellMongoViewDesignUtil;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * MongoDB 视图设计标签页业务。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class ShellMongoViewDesignTabController extends RichTabController {

    @FXML
    private FXTabPane tabPane;

    @FXML
    private FXComboBox<String> viewOn;

    @FXML
    private Editor pipeline;

    @FXML
    private Editor preview;

    private MongoView view;

    private ShellMongoDatabaseTreeItem dbItem;

    private DBStatusListener listener;

    private boolean unsaved;

    private boolean newData;

    private boolean initiating;

    private String newViewName;

    public void init(MongoView view, ShellMongoDatabaseTreeItem dbItem) {
        this.view = view;
        this.dbItem = dbItem;
        this.newData = view.isNew();
        StageManager.showMask(this.getWindow(), this::doInit);
    }

    private void doInit() {
        this.initDBListener();
        this.initInfo();
    }

    private void initDBListener() {
        if (this.listener != null) {
            return;
        }
        this.listener = new DBStatusListener(this.dbName() + ":view-design:" + this.view.getName()) {
            @Override
            public void changed(javafx.beans.value.ObservableValue<?> observable, Object oldValue, Object newValue) {
                initChangedFlag();
            }
        };
        DBStatusListenerManager.bindListener(this.viewOn, this.listener);
        this.pipeline.addTextChangeListener((observable, oldValue, newValue) ->
                this.listener.changed(observable, oldValue, newValue));
    }

    private void initChangedFlag() {
        if (!this.initiating) {
            this.unsaved = true;
            this.flushTab();
        }
    }

    private void initInfo() {
        this.initiating = true;
        try {
            this.initSources();
            if (this.newData) {
                this.newViewName = null;
                this.unsaved = true;
                if (this.viewOn.getItems().isEmpty()) {
                    this.viewOn.value(null);
                } else {
                    this.viewOn.selectFirst();
                }
                this.pipeline.text("[]");
                this.pipeline.forgetHistory();
                NodeGroupUtil.disappear(this.getTab(), "action3");
            } else {
                MongoView current = this.dbItem.client().selectView(this.dbName(), this.view.getName());
                if (current != null) {
                    this.view.copy(current);
                }
                this.unsaved = false;
                if (StringUtil.isNotBlank(this.view.getViewOn())
                        && !this.viewOn.containsItem(this.view.getViewOn())) {
                    this.viewOn.addItem(this.view.getViewOn());
                }
                this.viewOn.select(this.view.getViewOn());
                this.pipeline.text(ShellMongoViewDesignUtil.pipelineText(this.view.getPipeline()));
                this.pipeline.forgetHistory();
                NodeGroupUtil.display(this.getTab(), "action3");
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            FXUtil.runPulse(() -> {
                this.initiating = false;
                this.initPreview();
                this.flushTab();
            });
        }
    }

    private void initSources() {
        List<String> sources = new java.util.ArrayList<>(this.dbItem.client().listCollectionNames(this.dbName()));
        if (StringUtil.isNotBlank(this.view.getName())) {
            sources.remove(this.view.getName());
        }
        sources.sort(String::compareTo);
        this.viewOn.setItem(sources);
    }

    @FXML
    private void save() {
        StageManager.showMask(this.getWindow(), this::doSave);
    }

    private void doSave() {
        try {
            if (this.newData) {
                String name = MessageBox.prompt(I18nHelper.pleaseInputViewName(),
                        this.viewName(), this.getWindow());
                if (name == null) {
                    return;
                }
                this.newViewName = name.trim();
            }
            MongoView tempView = this.currentView();
            if (StringUtil.isBlank(tempView.getViewOn())) {
                MessageBox.warn(ShellI18nHelper.mongoInvalidViewSource(), this.getWindow());
                return;
            }
            if (StringUtil.equals(tempView.getName(), tempView.getViewOn())) {
                MessageBox.warn(ShellI18nHelper.mongoInvalidViewSource(), this.getWindow());
                return;
            }
            if (this.newData) {
                if (StringUtil.isBlank(tempView.getName())) {
                    MessageBox.warn(ShellI18nHelper.mongoInvalidViewName(), this.getWindow());
                    return;
                }
                if (this.dbItem.client().existCollection(this.dbName(), tempView.getName())) {
                    MessageBox.warn(ShellI18nHelper.mongoViewExists(tempView.getName()), this.getWindow());
                    return;
                }
                this.dbItem.client().createView(tempView);
                ShellMongoViewsTreeItem viewsItem = this.dbItem.getViewTypeChild();
                if (viewsItem != null) {
                    viewsItem.addView(tempView);
                }
            } else {
                this.dbItem.client().updateView(tempView);
            }
            this.newData = false;
            this.unsaved = false;
            this.view.copy(tempView);
            this.initInfo();
        } catch (IllegalArgumentException ex) {
            MessageBox.warn(ShellI18nHelper.mongoInvalidViewPipeline(), this.getWindow());
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.flushTab();
        }
    }

    private MongoView currentView() {
        MongoView tempView = new MongoView();
        tempView.setDbName(this.dbName());
        tempView.setName(this.newData ? this.newViewName : this.view.getName());
        String source = this.viewOn.getSelectedItem();
        tempView.setViewOn(source == null ? null : source.trim());
        try {
            tempView.setPipeline(ShellMongoViewDesignUtil.parsePipeline(this.pipeline.getTextTrim()));
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid pipeline", ex);
        }
        return tempView;
    }

    @FXML
    private void refresh() {
        if (this.newData) {
            return;
        }
        if (!MessageBox.confirm(ShellI18nHelper.confirmRefreshData(), this.getWindow())) {
            return;
        }
        StageManager.showMask(this.getWindow(), () -> {
            this.initInfo();
            this.flushTab();
        });
    }

    private void initPreview() {
        try {
            this.preview.text(ShellMongoViewDesignUtil.toScript(this.currentView(), this.newData));
        } catch (Exception ex) {
            this.preview.text("// " + ex.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        super.initialize(location, resourceBundle);
        NodeUtil.nodeOnCtrlS(this.getTab() == null ? null : this.getTab().getContent(), this::save);
        NodeUtil.nodeOnCtrlS(this.pipeline, this::save);
        this.tabPane.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null && "previewTab".equals(newValue.getId())) {
                this.initPreview();
            }
        });
    }

    public String viewName() {
        return this.newData ? this.newViewName : this.view.getName();
    }

    public String dbName() {
        return this.dbItem.dbName();
    }

    public ShellMongoDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public boolean isUnsaved() {
        return unsaved;
    }

    @Override
    public void destroy() {
        if (this.listener != null) {
            this.listener.destroy();
            this.listener = null;
        }
        this.pipeline.destroy();
        this.preview.destroy();
        super.destroy();
    }
}
