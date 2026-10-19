package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 串口协议图标
 *
 * @author oyzh
 * @since 2025-06-29
 */
public class SerialPortSVGGlyph extends SVGGlyph {

    /**
     * 构造串口协议图标
     */
    public SerialPortSVGGlyph() {
        super("/font/protocol/serial_port.svg");
    }

    /**
     * 构造指定尺寸的串口协议图标
     *
     * @param size 尺寸
     */
    public SerialPortSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
