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
     * 镜像名称
     */
    private String imageName;

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
     * 参数
     */
    private String params;

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

    /**
     * 获取容器名称
     *
     * @return 容器名称
     */
    public String getContainerName() {
        return containerName;
    }

    /**
     * 设置容器名称
     *
     * @param containerName 容器名称
     */
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    /**
     * 获取-i参数
     *
     * @return -i参数
     */
    public boolean isI() {
        return i;
    }

    /**
     * 设置-i参数
     *
     * @param i -i参数
     */
    public void setI(boolean i) {
        this.i = i;
    }

    /**
     * 获取-t参数
     *
     * @return -t参数
     */
    public boolean isT() {
        return t;
    }

    /**
     * 设置-t参数
     *
     * @param t -t参数
     */
    public void setT(boolean t) {
        this.t = t;
    }

    /**
     * 获取-d参数
     *
     * @return -d参数
     */
    public boolean isD() {
        return d;
    }

    /**
     * 设置-d参数
     *
     * @param d -d参数
     */
    public void setD(boolean d) {
        this.d = d;
    }

    /**
     * 获取端口
     *
     * @return 端口
     */
    public List<DockerPort> getPorts() {
        return ports;
    }

    /**
     * 设置端口
     *
     * @param ports 端口
     */
    public void setPorts(List<DockerPort> ports) {
        this.ports = ports;
    }

    /**
     * 获取镜像id
     *
     * @return 镜像id
     */
    public String getImageId() {
        return imageId;
    }

    /**
     * 设置镜像id
     *
     * @param imageId 镜像id
     */
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    /**
     * 获取--privileged参数
     *
     * @return --privileged参数
     */
    public boolean isPrivileged() {
        return privileged;
    }

    /**
     * 设置--privileged参数
     *
     * @param privileged --privileged参数
     */
    public void setPrivileged(boolean privileged) {
        this.privileged = privileged;
    }

    /**
     * 获取重启策略
     *
     * @return 重启策略
     */
    public String getRestart() {
        return restart;
    }

    /**
     * 设置重启策略
     *
     * @param restart 重启策略
     */
    public void setRestart(String restart) {
        this.restart = restart;
    }

    /**
     * 获取环境变量
     *
     * @return 环境变量
     */
    public List<DockerEnv> getEnvs() {
        return envs;
    }

    /**
     * 设置环境变量
     *
     * @param envs 环境变量
     */
    public void setEnvs(List<DockerEnv> envs) {
        this.envs = envs;
    }

    /**
     * 获取标签
     *
     * @return 标签
     */
    public List<DockerLabel> getLabels() {
        return labels;
    }

    /**
     * 设置标签
     *
     * @param labels 标签
     */
    public void setLabels(List<DockerLabel> labels) {
        this.labels = labels;
    }

    /**
     * 获取卷
     *
     * @return 卷
     */
    public List<DockerVolume> getVolumes() {
        return volumes;
    }

    /**
     * 设置卷
     *
     * @param volumes 卷
     */
    public void setVolumes(List<DockerVolume> volumes) {
        this.volumes = volumes;
    }

    /**
     * 是否忽略重启策略
     *
     * @return 是否忽略重启策略
     */
    public boolean isIgnoreRestart() {
        return StringUtil.isEmpty(restart) || StringUtil.equalsIgnoreCase(restart, "no");
    }

    /**
     * 获取--rm参数
     *
     * @return --rm参数
     */
    public boolean isRm() {
        return rm;
    }

    /**
     * 设置--rm参数
     *
     * @param rm --rm参数
     */
    public void setRm(boolean rm) {
        this.rm = rm;
    }

    /**
     * 获取镜像名称
     *
     * @return 镜像名称
     */
    public String getImageName() {
        return imageName;
    }

    /**
     * 设置镜像名称
     *
     * @param imageName 镜像名称
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * 获取参数
     *
     * @return 参数
     */
    public String getParams() {
        return params;
    }

    /**
     * 设置参数
     *
     * @param params 参数
     */
    public void setParams(String params) {
        this.params = params;
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

        /**
         * 获取类型
         *
         * @return 类型
         */
        public String getType() {
            return type;
        }

        /**
         * 设置类型
         *
         * @param type 类型
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * 获取外部端口
         *
         * @return 外部端口
         */
        public int getOuterPort() {
            return outerPort;
        }

        /**
         * 设置外部端口
         *
         * @param outerPort 外部端口
         */
        public void setOuterPort(int outerPort) {
            this.outerPort = outerPort;
        }

        /**
         * 获取内部端口
         *
         * @return 内部端口
         */
        public int getInnerPort() {
            return innerPort;
        }

        /**
         * 设置内部端口
         *
         * @param innerPort 内部端口
         */
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

        /**
         * 是否tcp协议
         *
         * @return 是否tcp协议
         */
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

        /**
         * 获取外部卷
         *
         * @return 外部卷
         */
        public String getOuterVolume() {
            return outerVolume;
        }

        /**
         * 设置外部卷
         *
         * @param outerVolume 外部卷
         */
        public void setOuterVolume(String outerVolume) {
            this.outerVolume = outerVolume;
        }

        /**
         * 获取内部卷
         *
         * @return 内部卷
         */
        public String getInnerVolume() {
            return innerVolume;
        }

        /**
         * 设置内部卷
         *
         * @param innerVolume 内部卷
         */
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

        /**
         * 获取名称
         *
         * @return 名称
         */
        public String getName() {
            return name;
        }

        /**
         * 设置名称
         *
         * @param name 名称
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * 获取值
         *
         * @return 值
         */
        public String getValue() {
            return value;
        }

        /**
         * 设置值
         *
         * @param value 值
         */
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

        /**
         * 获取名称
         *
         * @return 名称
         */
        public String getName() {
            return name;
        }

        /**
         * 设置名称
         *
         * @param name 名称
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * 获取值
         *
         * @return 值
         */
        public String getValue() {
            return value;
        }

        /**
         * 设置值
         *
         * @param value 值
         */
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
