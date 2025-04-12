package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.svg.glyph.ReturnFolderSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.File7zSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileAspSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileBatSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileBinSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileBmpSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileCmdSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileCompressSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileConfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileCssSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileDllSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileDmgSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileDotSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileDylibSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileExcelSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileExeSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileGifSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileGzSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileHtmlSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileIcoSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileImageSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileIniSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileIsoSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileJarSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileJpgSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileJsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileJsonSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileJspSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileLinkSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileMarkdownSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileMkvSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileMovSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileMp3SVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileMp4SVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FilePdfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FilePptSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FilePsdSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FilePySVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileRarSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileRmSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileRmvbSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileRssSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileRtfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileShSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileSoSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileSrtSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileSwfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileTarSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileTerminalSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileTextSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileTsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileTtfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileUnknownSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileWavSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileWordSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileXlsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileXmlSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileYamlSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileYmlSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileZipSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FolderLinkSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FolderSVGGlyph;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Date;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class SftpFile implements ObjectCopier<SftpFile> {

    private ChannelSftp.LsEntry entry;

    private SftpATTRS attrs;


    private String owner;

    private String group;

    private String fileName;

    /**
     * 链接路径
     */
    private String linkPath;

    /**
     * 链接属性
     */
    private SftpATTRS linkAttrs;

    public ChannelSftp.LsEntry getEntry() {
        return entry;
    }

    public void setEntry(ChannelSftp.LsEntry entry) {
        this.entry = entry;
    }

    public SftpATTRS getAttrs() {
        if (this.attrs == null) {
            return this.entry.getAttrs();
        }
        return attrs;
    }

    public void setAttrs(SftpATTRS attrs) {
        this.attrs = attrs;
        this.updatePermissions();
    }

    public SftpATTRS getLinkAttrs() {
        return linkAttrs;
    }

    public void setLinkAttrs(SftpATTRS linkAttrs) {
        this.linkAttrs = linkAttrs;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public void setIcon(SVGGlyph icon) {
        this.icon = icon;
    }

    private String parentPath;

    public SftpFile(String parentPath, ChannelSftp.LsEntry entry) {
        this.parentPath = parentPath;
        this.entry = entry;
        this.updatePermissions();
    }

    public SftpFile(String parentPath, String fileName, SftpATTRS attrs) {
        this.parentPath = parentPath;
        this.fileName = fileName;
        this.attrs = attrs;
        this.updatePermissions();
    }

    public boolean isNormal() {
        return !this.isCurrentFile() && !this.isReturnDirectory();
    }

    private SVGGlyph icon;

    public SVGGlyph getIcon() {
        if (this.icon != null) {
            return this.icon;
        }
        SVGGlyph glyph;
        if (this.isReturnDirectory()) {
            glyph = new ReturnFolderSVGGlyph("12");
        } else if (this.isLink() && this.isDir()) {
            glyph = new FolderLinkSVGGlyph("12");
        } else if (this.isLink() && this.isFile()) {
            glyph = new FileLinkSVGGlyph("12");
        } else if (this.isDir()) {
            glyph = new FolderSVGGlyph("12");
        } else {
            String extName = FileNameUtil.extName(this.getFileName());
            if (StringUtil.isEmpty(extName)) {
                glyph = new FileUnknownSVGGlyph("12");
            } else if (FileNameUtil.isAspType(extName)) {
                glyph = new FileAspSVGGlyph("12");
            } else if (FileNameUtil.isCmdType(extName)) {
                glyph = new FileCmdSVGGlyph("12");
            } else if (FileNameUtil.isRmType(extName)) {
                glyph = new FileRmSVGGlyph("12");
            } else if (FileNameUtil.isMkvType(extName)) {
                glyph = new FileMkvSVGGlyph("12");
            } else if (FileNameUtil.isDotType(extName)) {
                glyph = new FileDotSVGGlyph("12");
            } else if (FileNameUtil.isRtfType(extName)) {
                glyph = new FileRtfSVGGlyph("12");
            } else if (FileNameUtil.isWavType(extName)) {
                glyph = new FileWavSVGGlyph("12");
            } else if (FileNameUtil.isMovType(extName)) {
                glyph = new FileMovSVGGlyph("12");
            } else if (FileNameUtil.isSoType(extName)) {
                glyph = new FileSoSVGGlyph("12");
            } else if (FileNameUtil.isDllType(extName)) {
                glyph = new FileDllSVGGlyph("12");
            } else if (FileNameUtil.isDylibType(extName)) {
                glyph = new FileDylibSVGGlyph("12");
            } else if (FileNameUtil.isIsoType(extName)) {
                glyph = new FileIsoSVGGlyph("12");
            } else if (FileNameUtil.isRssType(extName)) {
                glyph = new FileRssSVGGlyph("12");
            } else if (FileNameUtil.isSrtType(extName)) {
                glyph = new FileSrtSVGGlyph("12");
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
            } else if (FileNameUtil.isCssType(extName)) {
                glyph = new FileCssSVGGlyph("12");
            } else if (FileNameUtil.isConfType(extName)) {
                glyph = new FileConfSVGGlyph("12");
            } else if (FileNameUtil.isBatType(extName)) {
                glyph = new FileBatSVGGlyph("12");
            } else if (FileNameUtil.isYamlType(extName)) {
                glyph = new FileYamlSVGGlyph("12");
            } else if (FileNameUtil.isIniType(extName)) {
                glyph = new FileIniSVGGlyph("12");
            } else if (FileNameUtil.isIcoType(extName)) {
                glyph = new FileIcoSVGGlyph("12");
            } else if (FileNameUtil.isTtfType(extName)) {
                glyph = new FileTtfSVGGlyph("12");
            } else if (FileNameUtil.isShType(extName)) {
                glyph = new FileShSVGGlyph("12");
            } else if (FileNameUtil.isPyType(extName)) {
                glyph = new FilePySVGGlyph("12");
            } else if (FileNameUtil.isYmlType(extName)) {
                glyph = new FileYmlSVGGlyph("12");
            } else if (FileNameUtil.isBinType(extName)) {
                glyph = new FileBinSVGGlyph("12");
            } else if (FileNameUtil.isPsdType(extName)) {
                glyph = new FilePsdSVGGlyph("12");
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
        return this.icon = glyph;
    }

    public void startWaiting() {
        if (this.icon != null) {
            this.icon.startWaiting();
        }
    }

    public void stopWaiting() {
        if (this.icon != null) {
            this.icon.stopWaiting();
        }
    }

    public boolean isWaiting() {
        if (this.icon != null) {
            return this.icon.isWaiting();
        }
        return false;
    }

    public String getSize() {
        if (this.isDir() || this.isReturnDirectory() || this.isCurrentFile()) {
            return "-";
        }
        return NumberUtil.formatSize(this.getAttrs().getSize(), 4);
    }

    public long size() {
        return this.getAttrs().getSize();
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
        if (this.linkPath != null) {
            return this.linkPath;
        }
        String fileName = this.getFileName();
        if (fileName.startsWith("/")) {
            return fileName;
        }
        return SftpUtil.concat(this.parentPath, fileName);
    }

    private StringProperty permissionsProperty;

    public StringProperty permissionsProperty() {
        if (this.permissionsProperty == null) {
            this.permissionsProperty = new SimpleStringProperty();
        }
        return this.permissionsProperty;
    }

    protected void updatePermissions() {
        this.permissionsProperty().set(this.getAttrs().getPermissionsString());
    }

    public String getPermissions() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return "";
        }
        return permissionsProperty().get();
    }

    public String getAddTime() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return "";
        }
        int aTime = this.getAttrs().getATime();
        return DateHelper.formatDateTime(new Date(aTime * 1000L));
    }

    public String getModifyTime() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return "";
        }
        int mtime = this.getAttrs().getMTime();
        return DateHelper.formatDateTime(new Date(mtime * 1000L));
    }

    public int getUid() {
        return this.getAttrs().getUId();
    }

    public int getGid() {
        return this.getAttrs().getGId();
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

    public boolean isDir() {
        if (this.isLink()) {
            if (this.linkAttrs != null) {
                return this.linkAttrs.isDir();
            }
            return false;
        }
        return this.getAttrs().isDir();
    }

    public boolean isFile() {
        if (this.isLink()) {
            if (this.linkAttrs != null) {
                return this.linkAttrs.isReg();
            }
            return false;
        }
        return this.getAttrs().isReg();
    }

    public boolean isLink() {
        return this.getAttrs().isLink();
    }

    @Override
    public void copy(SftpFile t1) {
        if (t1.entry != null) {
            this.entry = t1.entry;
        }
        if (t1.attrs != null) {
            this.attrs = t1.attrs;
        }
        if (t1.owner != null) {
            this.owner = t1.owner;
        }
        if (t1.group != null) {
            this.group = t1.group;
        }
        this.fileName = t1.fileName;
        this.linkPath = t1.linkPath;
        this.linkAttrs = t1.linkAttrs;
        this.parentPath = t1.parentPath;
    }

    public boolean canWrite() {
        return this.getPermissions().contains("w");
    }

    public boolean isDirectory() {
        return this.isDir();
    }

    public String getPath() {
        return this.getFilePath();
    }

    public long length() {
        return this.size();
    }

    public boolean hasOwnerReadPermission() {
        return ShellUtil.hasOwnerReadPermission(this.getPermissions());
    }

    public boolean hasOwnerWritePermission() {
        return ShellUtil.hasOwnerWritePermission(this.getPermissions());
    }

    public boolean hasOwnerExecutePermission() {
        return ShellUtil.hasOwnerExecutePermission(this.getPermissions());
    }

    public boolean hasGroupsReadPermission() {
        return ShellUtil.hasGroupsReadPermission(this.getPermissions());
    }

    public boolean hasGroupsWritePermission() {
        return ShellUtil.hasGroupsWritePermission(this.getPermissions());
    }

    public boolean hasGroupsExecutePermission() {
        return ShellUtil.hasGroupsExecutePermission(this.getPermissions());
    }

    public boolean hasOthersReadPermission() {
        return ShellUtil.hasOthersReadPermission(this.getPermissions());
    }

    public boolean hasOthersWritePermission() {
        return ShellUtil.hasOthersWritePermission(this.getPermissions());
    }

    public String getLinkPath() {
        return linkPath;
    }

    public void setLinkPath(String linkPath) {
        this.linkPath = linkPath;
    }

    public boolean hasOthersExecutePermission() {
        return ShellUtil.hasOthersExecutePermission(this.getPermissions());
    }
}
