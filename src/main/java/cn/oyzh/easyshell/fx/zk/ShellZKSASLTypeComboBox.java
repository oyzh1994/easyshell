package cn.oyzh.easyshell.fx.zk;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * ZooKeeper SASL 类型下拉框
 *
 * @author oyzh
 * @since 2024/4/23
 */
public class ShellZKSASLTypeComboBox extends FXComboBox<String>  {

    @Override
    public void initNode() {
        this.addItem("Digest");
        // this.addItem("Kerberos");
        super.initNode();
    }
}
