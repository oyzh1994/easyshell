package cn.oyzh.easyssh.ssh;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.fx.svg.glyph.FileExcelSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileHtmlSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileImageSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileJsonSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileLinkSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileMarkdownSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FilePdfSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FilePptSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileTerminalSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileTextSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileUnknownSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileWordSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileXlsSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileXmlSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileZipSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FolderSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class SftpFile {

    ChannelSftp.LsEntry entry;

    @Setter
    @Getter
    private String owner;

    @Setter
    @Getter
    private String group;

    public SftpFile(ChannelSftp.LsEntry entry) {
        this.entry = entry;
    }

    protected SftpATTRS attrs() {
        return this.entry.getAttrs();
    }

    public boolean isDir() {
        return this.entry.getAttrs().isDir();
    }

    public SVGGlyph getIcon() {
        if (this.attrs().isDir()) {
            return new FolderSVGGlyph("12");
        }
        if (this.attrs().isLink()) {
            return new FileLinkSVGGlyph("12");
        }
        String extName = FileNameUtil.extName(this.getFileName());
        if (StringUtil.isEmpty(extName)) {
            return new FileUnknownSVGGlyph("12");
        }
        if (FileNameUtil.isExcelType(extName)) {
            return new FileExcelSVGGlyph("12");
        }
        if (FileNameUtil.isXlsType(extName) || FileNameUtil.isXlsxType(extName)) {
            return new FileXlsSVGGlyph("12");
        }
        if (FileNameUtil.isWordType(extName)) {
            return new FileWordSVGGlyph("12");
        }
        if (FileNameUtil.isPptType(extName) || FileNameUtil.isPptxType(extName)) {
            return new FilePptSVGGlyph("12");
        }
        if (FileNameUtil.isPdfType(extName)) {
            return new FilePdfSVGGlyph("12");
        }
        if (FileNameUtil.isImageType(extName)) {
            return new FileImageSVGGlyph("12");
        }
        if (FileNameUtil.isCompressType(extName)) {
            return new FileZipSVGGlyph("12");
        }
        if (FileNameUtil.isHtmType(extName) || FileNameUtil.isHtmlType(extName)) {
            return new FileHtmlSVGGlyph("12");
        }
        if (FileNameUtil.isXmlType(extName)) {
            return new FileXmlSVGGlyph("12");
        }
        if (FileNameUtil.isJsonType(extName)) {
            return new FileJsonSVGGlyph("12");
        }
        if (FileNameUtil.isMarkdownType(extName)) {
            return new FileMarkdownSVGGlyph("12");
        }
        if (FileNameUtil.isTxtType(extName) || FileNameUtil.isTextType(extName)
                || FileNameUtil.isLogType(extName)) {
            return new FileTextSVGGlyph("12");
        }
        if (FileNameUtil.isTerminalType(extName)) {
            return new FileTerminalSVGGlyph("12");
        }
        return new FileSVGGlyph("12");
    }

    public String getSize() {
        if (this.isDir()) {
            return "";
        }
        return NumberUtil.formatSize(this.attrs().getSize());
    }

    public String getFileName() {
        return this.entry.getFilename();
    }

    public String getFilePath() {
        String name = this.getFileName();
        if (name.startsWith("/")) {
            return name;
        }
        return "/" + name;
    }

    public String getLongName() {
        return this.entry.getLongname();
    }

    public String getPermissionsString() {
        return this.attrs().getPermissionsString();
    }

    public String getModifyTime() {
        int mtime = this.attrs().getMTime();
        return DateHelper.formatDateTime(new Date(mtime * 1000L));
    }

    public int getUid() {
        return this.attrs().getUId();
    }

    public int getGid() {
        return this.attrs().getGId();
    }
}
