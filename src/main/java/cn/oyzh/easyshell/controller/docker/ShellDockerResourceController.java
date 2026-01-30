package cn.oyzh.easyshell.controller.docker;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerExec;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerResource;
import cn.oyzh.fx.gui.text.field.DecimalTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * docker容器资源业务
 *
 * @author oyzh
 * @since 2025/03/13
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "docker/shellDockerResource.fxml"
)
public class ShellDockerResourceController extends StageController {

    /**
     * 内存
     */
    @FXML
    private NumberTextField memory;

    /**
     * 内存交换区
     */
    @FXML
    private NumberTextField memorySwap;

    /**
     * CpuShares
     */
    @FXML
    private NumberTextField cpuShares;

    /**
     * NanoCpus
     */
    @FXML
    private DecimalTextField nanoCpus;

    /**
     * CpuPeriod
     */
    @FXML
    private DecimalTextField cpuPeriod;

    /**
     * CpuQuota
     */
    @FXML
    private NumberTextField cpuQuota;

    /**
     * exec对象
     */
    private ShellDockerExec exec = null;

    /**
     * 容器id
     */
    private String containerId;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.exec = this.getProp("exec");
        this.containerId = this.getProp("id");
        ShellDockerResource resource = this.getProp("resource");
        this.memory.setValue(resource.getMemory() / 1024 / 1024);
        this.memorySwap.setValue(resource.getMemorySwap() / 1024 / 1024);
        this.cpuQuota.setValue(resource.getCpuQuota());
        this.cpuShares.setValue(resource.getCpuShares());
        this.nanoCpus.setValue(resource.getNanoCpus() / 1000000000);
        this.cpuPeriod.setValue(resource.getCpuPeriod() / 1000);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.containerResource();
    }

    @FXML
    private void save() {
        try {
            long memory = this.memory.getValue();
            long memorySwap = this.memorySwap.getValue();
            double cpuPeriod = this.cpuPeriod.getValue();
            long cpuShares = this.cpuShares.getValue();
            double nanoCpus = this.nanoCpus.getValue();
            long cpuQuota = this.cpuQuota.getValue();
            ShellDockerResource resource = new ShellDockerResource();
            resource.setMemory(memory);
            resource.setMemorySwap(memorySwap);
            resource.setCpuQuota(cpuQuota);
            resource.setCpuShares(cpuShares);
            resource.setNanoCpus((long) (nanoCpus));
            resource.setCpuPeriod((long) cpuPeriod * 1000);
            String output = this.exec.docker_update(resource, this.containerId);
            if (JulLog.isInfoEnabled()) {
                JulLog.info("docker update result: {}", output);
            }
            if (StringUtil.isBlank(output)) {
                MessageBox.warn(I18nHelper.operationFail());
            } else if (!StringUtil.contains(output, this.containerId)) {
                MessageBox.warn(output);
            } else if (StringUtil.notEquals(output.replace("\n", ""), this.containerId)) {
                String msg = output.split("\n")[1];
                MessageBox.info(msg);
            } else {
                this.closeWindow();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
