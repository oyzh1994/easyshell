package cn.oyzh.easyshell.ssh2.docker;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.PortTextField;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;
import com.alibaba.fastjson2.annotation.JSONField;

import java.util.List;

/**
 * docker运行参数
 *
 * @author oyzh
 * @since 2025-07-03
 */
public class ShellDockerRun {

    /**
     * 镜像id
     */
    private String imageId;

    /**
     * 容器名称
     */
    private String containerName;

    /**
     * -i参数
     */
    private boolean i;

    /**
     * -t参数
     */
    private boolean t;

    /**
     * -d参数
     */
    private boolean d;

    /**
     * --rm
     */
    private boolean rm;

    /**
     * --privileged参数
     */
    private boolean privileged;

    /**
     * 重启策略
     */
    private String restart;

    /**
     * 端口
     */
    private List<DockerPort> ports;

    /**
     * 参数
     */
    private List<DockerEnv> envs;

    /**
     * 标签
     */
    private List<DockerLabel> labels;

    /**
     * 卷
     */
    private List<DockerVolume> volumes;

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public boolean isI() {
        return i;
    }

    public void setI(boolean i) {
        this.i = i;
    }

    public boolean isT() {
        return t;
    }

    public void setT(boolean t) {
        this.t = t;
    }

    public boolean isD() {
        return d;
    }

    public void setD(boolean d) {
        this.d = d;
    }

    public List<DockerPort> getPorts() {
        return ports;
    }

    public void setPorts(List<DockerPort> ports) {
        this.ports = ports;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public boolean isPrivileged() {
        return privileged;
    }

    public void setPrivileged(boolean privileged) {
        this.privileged = privileged;
    }

    public String getRestart() {
        return restart;
    }

    public void setRestart(String restart) {
        this.restart = restart;
    }

    public List<DockerEnv> getEnvs() {
        return envs;
    }

    public void setEnvs(List<DockerEnv> envs) {
        this.envs = envs;
    }

    public List<DockerLabel> getLabels() {
        return labels;
    }

    public void setLabels(List<DockerLabel> labels) {
        this.labels = labels;
    }

    public List<DockerVolume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<DockerVolume> volumes) {
        this.volumes = volumes;
    }

    public boolean isIgnoreRestart() {
        return StringUtil.isEmpty(restart) || StringUtil.equalsIgnoreCase(restart, "no");
    }

    public boolean isRm() {
        return rm;
    }

    public void setRm(boolean rm) {
        this.rm = rm;
    }

    /**
     * docker端口
     */
    public static class DockerPort {

        /**
         * 类型
         */
        private String type = "tcp";

        /**
         * 外部端口
         */
        private int outerPort;

        /**
         * 内部端口
         */
        private int innerPort;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getOuterPort() {
            return outerPort;
        }

        public void setOuterPort(int outerPort) {
            this.outerPort = outerPort;
        }

        public int getInnerPort() {
            return innerPort;
        }

        public void setInnerPort(int innerPort) {
            this.innerPort = innerPort;
        }

        /**
         * 类型控件
         */
        @JSONField(serialize = false, deserialize = false)
        public FXComboBox<String> getTypeControl() {
            FXComboBox<String> comboBox = new FXComboBox<>();
            comboBox.setFlexWidth("100% - 12");
            comboBox.addItem("tcp");
            comboBox.addItem("udp");
            comboBox.selectedItemChanged((obs, o, n) -> this.setType(n));
            TableViewUtil.selectRowOnMouseClicked(comboBox);
            comboBox.selectFirst();
            return comboBox;
        }

        /**
         * 外部端口控件
         */
        @JSONField(serialize = false, deserialize = false)
        public NumberTextField getOuterPortControl() {
            PortTextField textField = new PortTextField();
            textField.addTextChangeListener((obs, o, n) -> this.setOuterPort(textField.getIntValue()));
            TableViewUtil.selectRowOnMouseClicked(textField);
            return textField;
        }

        /**
         * 内部端口控件
         */
        @JSONField(serialize = false, deserialize = false)
        public PortTextField getInnerPortControl() {
            PortTextField textField = new PortTextField();
            textField.addTextChangeListener((obs, o, n) -> this.setInnerPort(textField.getIntValue()));
            TableViewUtil.selectRowOnMouseClicked(textField);
            return textField;
        }

        public boolean isTcp() {
            return StringUtil.equalsIgnoreCase(type, "tcp");
        }
    }

    /**
     * docker卷
     */
    public static class DockerVolume {

        /**
         * 外部卷
         */
        private String outerVolume;

        /**
         * 内部卷
         */
        private String innerVolume;

        public String getOuterVolume() {
            return outerVolume;
        }

        public void setOuterVolume(String outerVolume) {
            this.outerVolume = outerVolume;
        }

        public String getInnerVolume() {
            return innerVolume;
        }

        public void setInnerVolume(String innerVolume) {
            this.innerVolume = innerVolume;
        }

        /**
         * 外部卷控件
         */
        @JSONField(serialize = false, deserialize = false)
        public ClearableTextField getOuterVolumeControl() {
            ClearableTextField textField = new ClearableTextField();
            textField.setTipText(I18nHelper.pleaseInputContent());
            textField.addTextChangeListener((obs, o, n) -> this.setOuterVolume(n));
            TableViewUtil.selectRowOnMouseClicked(textField);
            return textField;
        }

        /**
         * 内部卷控件
         */
        @JSONField(serialize = false, deserialize = false)
        public ClearableTextField getInnerVolumeControl() {
            ClearableTextField textField = new ClearableTextField();
            textField.setTipText(I18nHelper.pleaseInputContent());
            textField.addTextChangeListener((obs, o, n) -> this.setInnerVolume(n));
            TableViewUtil.selectRowOnMouseClicked(textField);
            return textField;
        }
    }

    /**
     * docker环境
     */
    public static class DockerEnv {

        /**
         * 名称
         */
        private String name;

        /**
         * 值
         */
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        /**
         * 名称控件
         */
        @JSONField(serialize = false, deserialize = false)
        public ClearableTextField getNameControl() {
            ClearableTextField textField = new ClearableTextField();
            textField.setTipText(I18nHelper.pleaseInputName());
            textField.addTextChangeListener((obs, o, n) -> this.setName(n));
            TableViewUtil.selectRowOnMouseClicked(textField);
            return textField;
        }

        /**
         * 值控件
         */
        @JSONField(serialize = false, deserialize = false)
        public ClearableTextField getValueControl() {
            ClearableTextField textField = new ClearableTextField();
            textField.setTipText(I18nHelper.pleaseInputContent());
            textField.addTextChangeListener((obs, o, n) -> this.setValue(n));
            TableViewUtil.selectRowOnMouseClicked(textField);
            return textField;
        }
    }

    /**
     * docker标签
     */
    public static class DockerLabel {

        /**
         * 名称
         */
        private String name;

        /**
         * 值
         */
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        /**
         * 名称控件
         */
        @JSONField(serialize = false, deserialize = false)
        public ClearableTextField getNameControl() {
            ClearableTextField textField = new ClearableTextField();
            textField.setTipText(I18nHelper.pleaseInputName());
            textField.addTextChangeListener((obs, o, n) -> this.setName(n));
            TableViewUtil.selectRowOnMouseClicked(textField);
            return textField;
        }

        /**
         * 值控件
         */
        @JSONField(serialize = false, deserialize = false)
        public ClearableTextField getValueControl() {
            ClearableTextField textField = new ClearableTextField();
            textField.setTipText(I18nHelper.pleaseInputContent());
            textField.addTextChangeListener((obs, o, n) -> this.setValue(n));
            TableViewUtil.selectRowOnMouseClicked(textField);
            return textField;
        }
    }
}
