package cn.oyzh.easyssh.sftp;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.fx.svg.glyph.File7zSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileBmpSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileCompressSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileDmgSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileExcelSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileExeSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileGifSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileGzSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileHtmlSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileImageSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileJarSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileJpgSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileJsSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileJsonSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileJspSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileLinkSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileMarkdownSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileMp3SVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileMp4SVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FilePdfSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FilePptSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileRarSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileRmvbSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileSwfSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileTarSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileTerminalSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileTextSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.FileTsSVGGlyph;
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

    private ChannelSftp.LsEntry entry;

    @Setter
    private SftpATTRS attrs;

    @Setter
    @Getter
    private String owner;

    @Setter
    @Getter
    private String group;

    @Setter
    private String fileName;

    @Setter
    @Getter
    private String parentPath;

    public SftpFile(ChannelSftp.LsEntry entry) {
        this.entry = entry;
    }

    public SftpFile(String fileName, SftpATTRS attrs) {
        this.fileName = fileName;
        this.attrs = attrs;
    }

    protected SftpATTRS attrs() {
        if (this.attrs == null) {
            return this.entry.getAttrs();
        }
        return this.attrs;
    }

    public boolean isDir() {
        return this.attrs().isDir();
    }

    public boolean isNormal() {
        return !this.isCurrentFile() && !this.isReturnDirectory();
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
            } else if (FileNameUtil.isExeType(extName)) {
                glyph = new FileExeSVGGlyph("12");
            } else if (FileNameUtil.isMp3Type(extName)) {
                glyph = new FileMp3SVGGlyph("12");
            } else if (FileNameUtil.isMp4Type(extName)) {
                glyph = new FileMp4SVGGlyph("12");
            } else if (FileNameUtil.isDmgType(extName)) {
                glyph = new FileDmgSVGGlyph("12");
            } else if (FileNameUtil.isRarType(extName)) {
                glyph = new FileRarSVGGlyph("12");
            } else if (FileNameUtil.is7zType(extName)) {
                glyph = new File7zSVGGlyph("12");
            } else if (FileNameUtil.isGzType(extName)) {
                glyph = new FileGzSVGGlyph("12");
            } else if (FileNameUtil.isZipType(extName)) {
                glyph = new FileZipSVGGlyph("12");
            } else if (FileNameUtil.isTsType(extName)) {
                glyph = new FileTsSVGGlyph("12");
            } else if (FileNameUtil.isBmpType(extName)) {
                glyph = new FileBmpSVGGlyph("12");
            } else if (FileNameUtil.isJarType(extName)) {
                glyph = new FileJarSVGGlyph("12");
            } else if (FileNameUtil.isSwfType(extName)) {
                glyph = new FileSwfSVGGlyph("12");
            } else if (FileNameUtil.isTarType(extName)) {
                glyph = new FileTarSVGGlyph("12");
            } else if (FileNameUtil.isRmvbType(extName)) {
                glyph = new FileRmvbSVGGlyph("12");
            } else if (FileNameUtil.isGifType(extName)) {
                glyph = new FileGifSVGGlyph("12");
            } else if (FileNameUtil.isGzType(extName)) {
                glyph = new FileRmvbSVGGlyph("12");
            } else if (FileNameUtil.isHtmType(extName) || FileNameUtil.isHtmlType(extName)) {
                glyph = new FileHtmlSVGGlyph("12");
            } else if (FileNameUtil.isXmlType(extName)) {
                glyph = new FileXmlSVGGlyph("12");
            } else if (FileNameUtil.isJsType(extName)) {
                glyph = new FileJsSVGGlyph("12");
            } else if (FileNameUtil.isJspType(extName)) {
                glyph = new FileJspSVGGlyph("12");
            } else if (FileNameUtil.isJsonType(extName)) {
                glyph = new FileJsonSVGGlyph("12");
            } else if (FileNameUtil.isMarkdownType(extName)) {
                glyph = new FileMarkdownSVGGlyph("12");
            } else if (FileNameUtil.isTxtType(extName) || FileNameUtil.isTextType(extName)
                    || FileNameUtil.isLogType(extName)) {
                glyph = new FileTextSVGGlyph("12");
            } else if (FileNameUtil.isImageType(extName)) {
                glyph = new FileImageSVGGlyph("12");
            } else if (FileNameUtil.isCompressType(extName)) {
                glyph = new FileCompressSVGGlyph("12");
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
        if (this.isDir() || this.isReturnDirectory() || this.isCurrentFile()) {
            return "";
        }
        return NumberUtil.formatSize(this.attrs().getSize(), 4);
    }

    public long size() {
        return this.attrs().getSize();
    }

    public String getName() {
        String fileName = this.getFileName();
        if (fileName.contains("/")) {
            return fileName.substring(fileName.lastIndexOf("/") + 1);
        }
        return fileName;
    }

    public String getFileName() {
        if (this.fileName == null) {
            return this.entry.getFilename();
        }
        return this.fileName;
    }

    public String getFilePath() {
        String fileName = this.getFileName();
        if (fileName.startsWith("/")) {
            return fileName;
        }
        return SftpUtil.concat(this.parentPath, fileName);
    }

    public String getPermissions() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return "";
        }
        return this.attrs().getPermissionsString();
    }

    public String getAddTime() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return "";
        }
        int aTime = this.attrs().getATime();
        return DateHelper.formatDateTime(new Date(aTime * 1000L));
    }

    public String getModifyTime() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return "";
        }
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
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return false;
        }
        return this.getFileName().startsWith(".");
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

    public boolean isFile() {
        return !this.isDir() && !this.attrs().isLink();
    }
}
