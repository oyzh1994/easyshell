package cn.oyzh.easyssh.trees.docker;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.controller.docker.DockerInspectController;
import cn.oyzh.easyssh.controller.docker.DockerLogsController;
import cn.oyzh.easyssh.controller.docker.DockerResourceController;
import cn.oyzh.easyssh.docker.DockerContainer;
import cn.oyzh.easyssh.docker.DockerExec;
import cn.oyzh.easyssh.docker.DockerParser;
import cn.oyzh.easyssh.docker.DockerResource;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
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
public class SSHContainerTableView extends FXTableView<DockerContainer> {

    private DockerExec exec;

    public void setExec(DockerExec exec) {
        this.exec = exec;
    }

    public DockerExec getExec() {
        return exec;
    }

    private byte status;

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        if (status != this.status) {
            this.status = status;
            this.loadContainer();
        }
    }

    private List<DockerContainer> containers;

    public void loadContainer() {
        String output;
        if (this.status == 0) {
            output = this.exec.docker_ps();
        } else if (this.status == 1) {
            output = this.exec.docker_ps_a();
        } else {
            output = this.exec.docker_ps_exited();
        }
        this.containers = DockerParser.ps(output);
        this.setItem(this.doFilter(this.containers));
    }

    private String filterText;

    public void setFilterText(String filterText) {
        if (!StringUtil.equals(this.filterText, filterText)) {
            this.filterText = filterText;
            this.refreshContainer();
        }
    }

    public void refreshContainer() {
        if (this.containers == null) {
            this.loadContainer();
        } else {
            this.setItem(this.doFilter(this.containers));
        }
    }

    private List<DockerContainer> doFilter(List<DockerContainer> files) {
        if (CollectionUtil.isNotEmpty(files)) {
            return files.stream()
                    .filter(f -> {
                        if (StringUtil.isEmpty(this.filterText)) {
                            return true;
                        }
                        return StringUtil.containsIgnoreCase(f.getContainerId(), this.filterText)
                                || StringUtil.containsIgnoreCase(f.getImage(), this.filterText)
                                || StringUtil.containsIgnoreCase(f.getNames(), this.filterText);
                    })
                    .collect(Collectors.toList());
        }
        return files;
    }

    public void deleteContainer(boolean force) {
        DockerContainer container = this.getSelectedItem();
        if (!MessageBox.confirm(I18nHelper.deleteContainer() + " " + container.getNames())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                String output;
                if (force) {
                    output = this.exec.docker_rm_f(container.getContainerId());
                } else {
                    output = this.exec.docker_rm(container.getContainerId());
                }
                if (StringUtil.isNotBlank(output)) {
                    this.containers.remove(container);
                    this.refreshContainer();
                } else {
                    MessageBox.warn(I18nHelper.operationFail());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        DockerContainer container = this.getSelectedItem();
        if (container == null) {
            return Collections.emptyList();
        }

        List<FXMenuItem> menuItems = new ArrayList<>();
        FXMenuItem containerInfo = MenuItemHelper.containerInfo("12", this::containerInspect);
        menuItems.add(containerInfo);
        FXMenuItem containerResource = MenuItemHelper.containerResource("12", this::containerResource);
        menuItems.add(containerResource);
        if (container.isExited()) {
            FXMenuItem startContainer = MenuItemHelper.startContainer("12", this::startContainer);
            menuItems.add(startContainer);
        } else {
            FXMenuItem stopContainer = MenuItemHelper.stopContainer("12", this::stopContainer);
            FXMenuItem restartContainer = MenuItemHelper.restartContainer("12", this::restartContainer);
            menuItems.add(stopContainer);
            menuItems.add(restartContainer);
            if (container.isPaused()) {
                FXMenuItem unpauseContainer = MenuItemHelper.unpauseContainer("12", this::unpauseContainer);
                menuItems.add(unpauseContainer);
            } else {
                FXMenuItem pauseContainer = MenuItemHelper.pauseContainer("12", this::pauseContainer);
                menuItems.add(pauseContainer);
            }
        }
        FXMenuItem containerLogs = MenuItemHelper.containerLogs("12", this::containerLogs);
        FXMenuItem deleteContainer = MenuItemHelper.deleteContainer("12", () -> this.deleteContainer(false));
        FXMenuItem forceDeleteContainer = MenuItemHelper.forceDeleteContainer("12", () -> this.deleteContainer(true));
        menuItems.add(containerLogs);
        menuItems.add(deleteContainer);
        menuItems.add(forceDeleteContainer);
        return menuItems;
    }

    public void startContainer() {
        DockerContainer container = this.getSelectedItem();
        if (!MessageBox.confirm(I18nHelper.startContainer() + " " + container.getNames())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_start(container.getContainerId());
                if (StringUtil.isNotBlank(output)) {
                    this.loadContainer();
                } else {
                    MessageBox.warn(I18nHelper.operationFail());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    public void stopContainer() {
        DockerContainer container = this.getSelectedItem();
        if (!MessageBox.confirm(I18nHelper.stopContainer() + " " + container.getNames())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_stop(container.getContainerId());
                if (StringUtil.isNotBlank(output)) {
                    this.loadContainer();
                } else {
                    MessageBox.warn(I18nHelper.operationFail());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    public void restartContainer() {
        DockerContainer container = this.getSelectedItem();
        if (!MessageBox.confirm(I18nHelper.restartContainer() + " " + container.getNames())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_restart(container.getContainerId());
                if (StringUtil.isNotBlank(output)) {
                    this.loadContainer();
                } else {
                    MessageBox.warn(I18nHelper.operationFail());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    public void pauseContainer() {
        DockerContainer container = this.getSelectedItem();
        if (!MessageBox.confirm(I18nHelper.pauseContainer() + " " + container.getNames())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_pause(container.getContainerId());
                if (StringUtil.isNotBlank(output)) {
                    this.loadContainer();
                } else {
                    MessageBox.warn(I18nHelper.operationFail());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    public void unpauseContainer() {
        DockerContainer container = this.getSelectedItem();
        if (!MessageBox.confirm(I18nHelper.unpauseContainer() + " " + container.getNames())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_unpause(container.getContainerId());
                if (StringUtil.isNotBlank(output)) {
                    this.loadContainer();
                } else {
                    MessageBox.warn(I18nHelper.operationFail());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
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

    public void containerInspect() {
        DockerContainer container = this.getSelectedItem();
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_inspect(container.getContainerId());
                if (StringUtil.isBlank(output)) {
                    MessageBox.warn(I18nHelper.operationFail());
                } else {
                    FXUtil.runLater(() -> {
                        StageAdapter adapter = StageManager.parseStage(DockerInspectController.class);
                        adapter.setProp("inspect", output);
                        adapter.display();
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    public void containerResource() {
        DockerContainer container = this.getSelectedItem();
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_resource(container.getContainerId());
                if (StringUtil.isBlank(output)) {
                    MessageBox.warn(I18nHelper.operationFail());
                } else {
                    DockerResource resource = DockerParser.resource(output);
                    FXUtil.runLater(() -> {
                        StageAdapter adapter = StageManager.parseStage(DockerResourceController.class);
                        adapter.setProp("exec", this.exec);
                        adapter.setProp("resource", resource);
                        adapter.setProp("id", container.getContainerId());
                        adapter.display();
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    public void containerLogs() {
        DockerContainer container = this.getSelectedItem();
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_logs(container.getContainerId());
                if (StringUtil.isBlank(output)) {
                    MessageBox.warn(I18nHelper.operationFail());
                } else {
                    FXUtil.runLater(() -> {
                        StageAdapter adapter = StageManager.parseStage(DockerLogsController.class);
                        adapter.setProp("logs", output);
                        adapter.display();
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }
}
