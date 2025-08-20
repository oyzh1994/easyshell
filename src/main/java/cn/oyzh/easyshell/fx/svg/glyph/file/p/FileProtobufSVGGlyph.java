package cn.oyzh.easyshell.fx.svg.glyph.file.p;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileProtobufSVGGlyph extends SVGGlyph {

    public FileProtobufSVGGlyph() {
        super("/font/file/p/file-protobuf.svg");
    }

    public FileProtobufSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
