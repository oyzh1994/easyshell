package cn.oyzh.easyshell.fx.docker;

import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerContainer;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerExec;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerParser;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerPort;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerResource;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-12
 */
public class ShellDockerContainerTableView extends FXTableView<ShellDockerContainer> {

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
            //if (KeyboardUtil.info_keyCombination.match(event)) {// 容器信息
            //    this.containerInspect(this.getSelectedItem());
            //    event.consume();
            //}
            if (KeyboardUtil.run_keyCombination.match(event)) {// 启动容器
                this.startContainer(this.getSelectedItem());
                event.consume();
            } else if (KeyboardUtil.delete_keyCombination.match(event)) {// 删除容器
                this.deleteContainer(this.getSelectedItem(), false);
                event.consume();
            //} else if (KeyboardUtil.restart_keyCombination.match(event)) {// 重启容器
            //    this.restartContainer(this.getSelectedItem());
            //    event.consume();
            } else if (KeyboardUtil.stop_keyCombination.match(event)) {// 停止容器
                this.stopContainer(this.getSelectedItem());
                event.consume();
            } else if (KeyboardUtil.rename_keyCombination.match(event)) {// 重命名容器
                this.renameContainer(this.getSelectedItem());
                event.consume();
            } else if (KeyboardUtil.pause_keyCombination.match(event)) {// 重命名容器
                this.pauseContainer(this.getSelectedItem());
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

    private byte status;

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        if (status != this.status) {
            this.status = status;
            StageManager.showMask(() -> {
                try {
                    this.loadContainer();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageBox.exception(ex);
                }
            });
        }
    }

    private List<ShellDockerContainer> containers;

    public void loadContainer() {
        String output;
        if (this.status == 0) {
            output = this.exec.docker_ps();
        } else if (this.status == 1) {
            output = this.exec.docker_ps_a();
        } else {
            output = this.exec.docker_ps_exited();
        }
        this.containers = ShellDockerParser.ps(output);
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

    private List<ShellDockerContainer> doFilter(List<ShellDockerContainer> files) {
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

    /**
     * 删除容器
     *
     * @param container 容器
     * @param force     是否强制
     */
    public void deleteContainer(ShellDockerContainer container, boolean force) {
        if (container == null) {
            return;
        }
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
        ShellDockerContainer container = this.getSelectedItem();
        if (container == null) {
            return Collections.emptyList();
        }

        List<FXMenuItem> menuItems = new ArrayList<>();
        FXMenuItem containerInfo = MenuItemHelper.containerInspect("12", () -> this.containerInspect(container));
        //containerInfo.setAccelerator(KeyboardUtil.info_keyCombination);
        menuItems.add(containerInfo);
        FXMenuItem containerResource = MenuItemHelper.containerResource("12", this::containerResource);
        menuItems.add(containerResource);
        FXMenuItem containerPorts = MenuItemHelper.containerPorts("12", this::containerPorts);
        menuItems.add(containerPorts);
        if (container.isExited()) {
            FXMenuItem startContainer = MenuItemHelper.start1Container("12", () -> this.startContainer(container));
            startContainer.setAccelerator(KeyboardUtil.run_keyCombination);
            menuItems.add(startContainer);
        } else {
            FXMenuItem stopContainer = MenuItemHelper.stop1Container("12", () -> this.stopContainer(container));
            stopContainer.setAccelerator(KeyboardUtil.stop_keyCombination);
            FXMenuItem killContainer = MenuItemHelper.killContainer("12", this::killContainer);
            FXMenuItem restartContainer = MenuItemHelper.restartContainer("12", () -> this.restartContainer(container));
            //restartContainer.setAccelerator(KeyboardUtil.restart_keyCombination);
            menuItems.add(stopContainer);
            menuItems.add(killContainer);
            menuItems.add(restartContainer);
            if (container.isPaused()) {
                FXMenuItem unpauseContainer = MenuItemHelper.unpauseContainer("12", this::unpauseContainer);
                menuItems.add(unpauseContainer);
            } else {
                FXMenuItem pauseContainer = MenuItemHelper.pauseContainer("12", () -> this.pauseContainer(container));
                menuItems.add(pauseContainer);
            }
        }
        FXMenuItem containerLogs = MenuItemHelper.containerLogs("12", this::containerLogs);
        FXMenuItem renameContainer = MenuItemHelper.renameContainer("12", () -> this.renameContainer(container));
        renameContainer.setAccelerator(KeyboardUtil.rename_keyCombination);
        FXMenuItem deleteContainer = MenuItemHelper.deleteContainer("12", () -> this.deleteContainer(container, false));
        deleteContainer.setAccelerator(KeyboardUtil.delete_keyCombination);
        FXMenuItem forceDeleteContainer = MenuItemHelper.forceDeleteContainer("12", () -> this.deleteContainer(container, true));
        menuItems.add(containerLogs);
        menuItems.add(renameContainer);
        menuItems.add(deleteContainer);
        menuItems.add(forceDeleteContainer);
        FXMenuItem saveContainer = MenuItemHelper.saveContainer("12", () -> this.saveContainer(container));
        menuItems.add(saveContainer);
        return menuItems;
    }

    /**
     * 保存容器
     *
     * @param container 容器
     */
    public void saveContainer(ShellDockerContainer container) {
        if (container == null) {
            return;
        }
        ShellViewFactory.commitContainer(this.exec, container);
    }

    /**
     * 启动容器
     *
     * @param container 容器
     */
    public void startContainer(ShellDockerContainer container) {
        if (container == null || !container.isExited()) {
            return;
        }
        if (!MessageBox.confirm(I18nHelper.start1Container() + " " + container.getNames())) {
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

    /**
     * 停止容器
     *
     * @param container 容器
     */
    public void stopContainer(ShellDockerContainer container) {
        if (container == null) {
            return;
        }
        if (!MessageBox.confirm(I18nHelper.stop1Container() + " " + container.getNames())) {
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

    public void killContainer() {
        ShellDockerContainer container = this.getSelectedItem();
        if (!MessageBox.confirm(I18nHelper.killContainer() + " " + container.getNames())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_kill(container.getContainerId());
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

    /**
     * 重启容器
     *
     * @param container 容器
     */
    public void restartContainer(ShellDockerContainer container) {
        if (container == null) {
            return;
        }
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

    /**
     * 暂停容器
     *
     * @param container 容器
     */
    public void pauseContainer(ShellDockerContainer container) {
        if (container == null) {
            return;
        }
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
        ShellDockerContainer container = this.getSelectedItem();
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

    /**
     * 容器审查
     *
     * @param container 容器
     */
    public void containerInspect(ShellDockerContainer container) {
        if (container == null) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_inspect(container.getContainerId());
                if (StringUtil.isBlank(output)) {
                    MessageBox.warn(I18nHelper.operationFail());
                } else {
                    ShellViewFactory.dockerInspect(output, false);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    public void containerResource() {
        ShellDockerContainer container = this.getSelectedItem();
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_resource(container.getContainerId());
                if (StringUtil.isBlank(output)) {
                    MessageBox.warn(I18nHelper.operationFail());
                } else {
                    ShellDockerResource resource = ShellDockerParser.resource(output);
                    ShellViewFactory.dockerResource(exec, resource, container.getContainerId());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    public void containerLogs() {
        ShellDockerContainer container = this.getSelectedItem();
        StageManager.showMask(() -> {
            try {
                DownLatch latch = DownLatch.of();
                AtomicReference<String> ref = new AtomicReference<>();
                AtomicReference<Throwable> errRef = new AtomicReference<>();
                ThreadUtil.start(() -> {
                    try {
                        String output = this.exec.docker_logs(container.getContainerId());
                        ref.set(output);
                    } catch (Throwable ex) {
                        errRef.set(ex);
                    } finally {
                        latch.countDown();
                    }
                });
                // 最多等待30秒
                if (!latch.await(30_000)) {
                    MessageBox.warn(I18nHelper.executeTimout());
                    return;
                }
                // 抛出异常
                if (errRef.get() != null) {
                    throw errRef.get();
                }
                String output = ref.get();
                if (StringUtil.isBlank(output)) {
                    MessageBox.warn(I18nHelper.operationFail());
                } else if (StringUtil.length(output) > 512 * 1024) {
                    MessageBox.warn(I18nHelper.dataTooLarge());
                } else {
                    ShellViewFactory.dockerLogs(output);
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 重命名容器
     *
     * @param container 容器
     */
    public void renameContainer(ShellDockerContainer container) {
        if (container == null) {
            return;
        }
        String newName = MessageBox.prompt(I18nHelper.pleaseInputName(), container.getNames());
        if (StringUtil.isBlank(newName) || StringUtil.equalsIgnoreCase(container.getNames(), newName)) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_rename(container.getContainerId(), newName);
                if (StringUtil.isNotBlank(output)) {
                    MessageBox.warn(output);
                } else {
                    container.setNames(newName);
                    this.refreshContainer();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    public void containerPorts() {
        ShellDockerContainer container = this.getSelectedItem();
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_port(container.getContainerId());
                List<ShellDockerPort> ports = ShellDockerParser.port(output);
//                FXUtil.runLater(() -> {
//                    StageAdapter adapter = StageManager.parseStage(ShellDockerPortController.class);
//                    adapter.setProp("ports", ports);
//                    adapter.display();
//                });
                ShellViewFactory.dockerPort(ports);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }
}
