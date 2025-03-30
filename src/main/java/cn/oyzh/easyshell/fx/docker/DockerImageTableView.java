package cn.oyzh.easyshell.fx.docker;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.docker.DockerHistoryController;
import cn.oyzh.easyshell.controller.docker.DockerInspectController;
import cn.oyzh.easyshell.docker.DockerExec;
import cn.oyzh.easyshell.docker.DockerHistory;
import cn.oyzh.easyshell.docker.DockerImage;
import cn.oyzh.easyshell.docker.DockerParser;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-12
 */
public class DockerImageTableView extends FXTableView<DockerImage> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }

    private DockerExec exec;

    public void setExec(DockerExec exec) {
        this.exec = exec;
    }

    public DockerExec getExec() {
        return exec;
    }
//
//    private byte status;
//
//    public byte getStatus() {
//        return status;
//    }
//
//    public void setStatus(byte status) {
//        if (status != this.status) {
//            this.status = status;
//            this.loadImage();
//        }
//    }

    private List<DockerImage> images;

    public void loadImage() {
        String output = this.exec.docker_images();
        this.images = DockerParser.images(output);
        this.setItem(this.doFilter(this.images));
    }

    private String filterText;

    public void setFilterText(String filterText) {
        if (!StringUtil.equals(this.filterText, filterText)) {
            this.filterText = filterText;
            this.refreshImage();
        }
    }

    public void refreshImage() {
        if (this.images == null) {
            this.loadImage();
        } else {
            this.setItem(this.doFilter(this.images));
        }
    }

    private List<DockerImage> doFilter(List<DockerImage> files) {
        if (CollectionUtil.isNotEmpty(files)) {
            return files.stream()
                    .filter(f -> {
                        if (StringUtil.isEmpty(this.filterText)) {
                            return true;
                        }
                        return StringUtil.containsIgnoreCase(f.getImageId(), this.filterText)
                                || StringUtil.containsIgnoreCase(f.getRepository(), this.filterText)
                                || StringUtil.containsIgnoreCase(f.getTag(), this.filterText);
                    })
                    .collect(Collectors.toList());
        }
        return files;
    }

    public void deleteImage(boolean force) {
        DockerImage image = this.getSelectedItem();
        if (!MessageBox.confirm(I18nHelper.deleteImage() + " " + image.getRepository())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                String output;
                if (force) {
                    output = this.exec.docker_rmi_f(image.getImageId());
                } else {
                    output = this.exec.docker_rmi(image.getImageId());
                }
                if (StringUtil.isNotBlank(output)) {
                    this.images.remove(image);
                    this.refreshImage();
                } else {
                    MessageBox.warn(I18nHelper.operationFail());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    public void imageInspect() {
        DockerImage image = this.getSelectedItem();
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_inspect(image.getImageId());
                if (StringUtil.isBlank(output)) {
                    MessageBox.warn(I18nHelper.operationFail());
                } else {
                    FXUtil.runLater(() -> {
                        StageAdapter adapter = StageManager.parseStage(DockerInspectController.class);
                        adapter.setProp("inspect", output);
                        adapter.setProp("image", true);
                        adapter.display();
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    public void imageHistory() {
        DockerImage image = this.getSelectedItem();
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_history(image.getImageId());
                List<DockerHistory> histories = DockerParser.history(output);
                FXUtil.runLater(() -> {
                    StageAdapter adapter = StageManager.parseStage(DockerHistoryController.class);
                    adapter.setProp("histories", histories);
                    adapter.display();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        DockerImage image = this.getSelectedItem();
        if (image == null) {
            return Collections.emptyList();
        }
        List<FXMenuItem> menuItems = new ArrayList<>();
        FXMenuItem imageInfo = MenuItemHelper.imageInspect("12", this::imageInspect);
        FXMenuItem imageHistory = MenuItemHelper.imageHistory("12", this::imageHistory);
        FXMenuItem deleteImage = MenuItemHelper.deleteImage("12", () -> this.deleteImage(false));
        FXMenuItem forceDeleteImage = MenuItemHelper.forceDeleteImage("12", () -> this.deleteImage(true));
        menuItems.add(imageInfo);
        menuItems.add(imageHistory);
        menuItems.add(deleteImage);
        menuItems.add(forceDeleteImage);
        return menuItems;
    }

    @Override
    protected void initEvenListener() {
        super.initEvenListener();
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            List<? extends MenuItem> items = this.getMenuItems();
            if (CollectionUtil.isNotEmpty(items)) {
                this.showContextMenu(items, e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
    }
}
