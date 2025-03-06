package cn.oyzh.easyssh.sftp;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.fx.svg.glyph.FileExcelSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileHtmlSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileImageSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileJpgSVGGlyph;
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
import cn.oyzh.easyssh.fx.svg.glyph.ReturnFolderSVGGlyph;
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
        SVGGlyph glyph;
        if (this.isReturnDirectory()) {
            glyph = new ReturnFolderSVGGlyph("12");
        } else if (this.attrs().isDir()) {
            glyph = new FolderSVGGlyph("12");
        } else if (this.attrs().isLink()) {
            glyph = new FileLinkSVGGlyph("12");
        } else {
            String extName = FileNameUtil.extName(this.getFileName());
            if (StringUtil.isEmpty(extName)) {
                glyph = new FileUnknownSVGGlyph("12");
            } else if (FileNameUtil.isExcelType(extName)) {
                glyph = new FileExcelSVGGlyph("12");
            } else if (FileNameUtil.isXlsType(extName) || FileNameUtil.isXlsxType(extName)) {
                glyph = new FileXlsSVGGlyph("12");
            } else if (FileNameUtil.isWordType(extName)) {
                glyph = new FileWordSVGGlyph("12");
            } else if (FileNameUtil.isPptType(extName) || FileNameUtil.isPptxType(extName)) {
                glyph = new FilePptSVGGlyph("12");
            } else if (FileNameUtil.isPdfType(extName)) {
                glyph = new FilePdfSVGGlyph("12");
            } else if (FileNameUtil.isJpgType(extName)) {
                glyph = new FileJpgSVGGlyph("12");
            } else if (FileNameUtil.isImageType(extName)) {
                glyph = new FileImageSVGGlyph("12");
            } else if (FileNameUtil.isCompressType(extName)) {
                glyph = new FileZipSVGGlyph("12");
            } else if (FileNameUtil.isHtmType(extName) || FileNameUtil.isHtmlType(extName)) {
                glyph = new FileHtmlSVGGlyph("12");
            } else if (FileNameUtil.isXmlType(extName)) {
                glyph = new FileXmlSVGGlyph("12");
            } else if (FileNameUtil.isJsonType(extName)) {
                glyph = new FileJsonSVGGlyph("12");
            } else if (FileNameUtil.isMarkdownType(extName)) {
                glyph = new FileMarkdownSVGGlyph("12");
            } else if (FileNameUtil.isTxtType(extName) || FileNameUtil.isTextType(extName)
                    || FileNameUtil.isLogType(extName)) {
                glyph = new FileTextSVGGlyph("12");
            } else if (FileNameUtil.isTerminalType(extName)) {
                glyph = new FileTerminalSVGGlyph("12");
            } else {
                glyph = new FileSVGGlyph("12");
            }
        }
        if (this.isHiddenFile()) {
            glyph.setOpacity(0.5);
        }
        return glyph;
    }

    public String getSize() {
        if (this.isDir()) {
            return "";
        }
        return NumberUtil.formatSize(this.attrs().getSize());
    }

    public String getName() {
        String fileName = this.getFileName();
        if (fileName.contains("/")) {
            return fileName.substring(fileName.lastIndexOf("/") + 1);
        }
        return fileName;
    }

    public String getFileName() {
        return this.entry.getFilename();
    }

    public String getFilePath() {
        String fileName = this.getFileName();
        if (fileName.startsWith("/")) {
            return fileName;
        }
        return "/" + fileName;
    }

    public String getLongName() {
        return this.entry.getLongname();
    }

    public String getPermissionsString() {
        return this.attrs().getPermissionsString();
    }

    public String getAddTime() {
        int aTime = this.attrs().getATime();
        return DateHelper.formatDateTime(new Date(aTime * 1000L));
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

    public boolean isHiddenFile() {
        if (!this.isCurrentFile() && !this.isReturnDirectory()) {
            return this.getFileName().startsWith(".");
        }
        return false;
    }

    public boolean isCurrentFile() {
        return ".".equals(this.getName());
    }

    public boolean isReturnDirectory() {
        return "..".equals(this.getName());
    }

    public int getOrder() {
        if (this.isReturnDirectory()) {
            return -10;
        }
        if (this.isDir()) {
            if (this.isHiddenFile()) {
                return -9;
            }
            return -8;
        }
        if (this.isHiddenFile()) {
            return -7;
        }
        return 0;
    }
}
