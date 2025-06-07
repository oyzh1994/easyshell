package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileApkSVGGlyph extends SVGGlyph {

    public FileApkSVGGlyph() {
        super("/font/file/file-apk.svg");
    }

    public FileApkSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
