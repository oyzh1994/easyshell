package cn.oyzh.easyshell.fx.term;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.tty.TtyTerminalEngine;

/**
 * 终端引擎选择框
 *
 * @author oyzh
 * @since 2026-11-11
 */
public class ShellTermEngineComboBox extends FXComboBox<String> {

    {
        this.addItem("JediTerm");
        this.addItem("Ghostty");
    }

    /**
     * 获取选中的终端引擎
     *
     * @return 终端引擎
     */
    public TtyTerminalEngine getEngine() {
        return this.getSelectedIndex() == 1 ? TtyTerminalEngine.GHOSTTY : TtyTerminalEngine.JEDITERM;
    }

    /**
     * 选择终端引擎
     *
     * @param engine 终端引擎
     */
    public void selectEngine(TtyTerminalEngine engine) {
        this.select(engine == TtyTerminalEngine.GHOSTTY ? 1 : 0);
    }
}
