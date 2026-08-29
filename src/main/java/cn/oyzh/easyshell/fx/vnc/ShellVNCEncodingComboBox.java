package cn.oyzh.easyshell.fx.vnc;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import com.glavsoft.rfb.encoding.EncodingType;

/**
 * @author oyzh
 * @since 2026-08-29
 */
public class ShellVNCEncodingComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        for (EncodingType value : EncodingType.ordinaryEncodings) {
            this.addItem(value.getName());
        }
    }
}
