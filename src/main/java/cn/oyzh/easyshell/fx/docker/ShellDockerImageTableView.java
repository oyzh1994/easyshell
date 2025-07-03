package cn.oyzh.easyshell.fx.docker;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerExec;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerImageHistory;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerImage;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerParser;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-12
 */
public class ShellDockerImageTableView extends FXTableView<ShellDockerImage> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
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
        // 快捷键
        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (KeyboardUtil.info_keyCombination.match(event)) {// 镜像信息
                this.imageInspect(this.getSelectedItem());
                event.consume();
            } else if (KeyboardUtil.delete_keyCombination.match(event)) {// 删除镜像
                this.deleteImage(this.getSelectedItem(), false);
                event.consume();
            }
        });
    }

    private ShellDockerExec exec;

    public void setExec(ShellDockerExec exec) {
        this.exec = exec;
    }

    public ShellDockerExec getExec() {
        return exec;
    }

    private List<ShellDockerImage> images;

    public void loadImage() {
        String output = this.exec.docker_images();
        this.images = ShellDockerParser.images(output);
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

    private List<ShellDockerImage> doFilter(List<ShellDockerImage> files) {
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

    /**
     * 删除镜像
     *
     * @param image 镜像
     * @param force 是否强制
     */
    public void deleteImage(ShellDockerImage image, boolean force) {
        if (image == null) {
            return;
        }
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

    /**
     * 运行镜像
     *
     * @param image 镜像
     */
    public void runImage(ShellDockerImage image) {
        if (image == null) {
            return;
        }
        ShellViewFactory.runImage(this.exec, image);
    }

    /**
     * 保存镜像
     *
     * @param image 镜像
     */
    public void saveImage(ShellDockerImage image) {
        if (image == null) {
            return;
        }
        ShellViewFactory.saveImage(this.exec, image);
    }

    /**
     * 镜像审查
     *
     * @param image 镜像
     */
    public void imageInspect(ShellDockerImage image) {
        if (image == null) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_inspect(image.getImageId());
                if (StringUtil.isBlank(output)) {
                    MessageBox.warn(I18nHelper.operationFail());
                } else {
                    ShellViewFactory.dockerInspect(output, true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    public void imageHistory() {
        ShellDockerImage image = this.getSelectedItem();
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_history(image.getImageId());
                List<ShellDockerImageHistory> histories = ShellDockerParser.history(output);
//                FXUtil.runLater(() -> {
//                    StageAdapter adapter = StageManager.parseStage(ShellDockerImageHistoryController.class);
//                    adapter.setProp("histories", histories);
//                    adapter.display();
//                });
                ShellViewFactory.dockerHistory(histories);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        ShellDockerImage image = this.getSelectedItem();
        if (image == null) {
            return Collections.emptyList();
        }
        List<FXMenuItem> menuItems = new ArrayList<>();
        FXMenuItem runImage = MenuItemHelper.runImage("12", () -> this.runImage(image));
        FXMenuItem imageInfo = MenuItemHelper.imageInspect("12", () -> this.imageInspect(image));
        FXMenuItem saveImage = MenuItemHelper.saveImage("12", () -> this.saveImage(image));
        imageInfo.setAccelerator(KeyboardUtil.info_keyCombination);
        FXMenuItem imageHistory = MenuItemHelper.imageHistory("12", this::imageHistory);
        FXMenuItem deleteImage = MenuItemHelper.deleteImage("12", () -> this.deleteImage(image, false));
        deleteImage.setAccelerator(KeyboardUtil.delete_keyCombination);
        FXMenuItem forceDeleteImage = MenuItemHelper.forceDeleteImage("12", () -> this.deleteImage(image, true));
        menuItems.add(runImage);
        menuItems.add(imageInfo);
        menuItems.add(saveImage);
        menuItems.add(imageHistory);
        menuItems.add(deleteImage);
        menuItems.add(forceDeleteImage);
        return menuItems;
    }

}
