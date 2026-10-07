package cn.oyzh.easyshell.fx.svg.glyph.other;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * MinIO 对象存储图标
 *
 * @author oyzh
 * @since 2025-06-29
 */
public class MinioSVGGlyph extends SVGGlyph {

    /**
     * 构造 MinIO 对象存储图标
     */
    public MinioSVGGlyph() {
        super("/font/other/minio.svg");
    }

    /**
     * 构造指定尺寸的 MinIO 对象存储图标
     *
     * @param size 图标尺寸
     */
    public MinioSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
