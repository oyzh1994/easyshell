package cn.oyzh.easyshell.util;

import cn.oyzh.common.file.FileNameUtil;
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
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

public interface ShellFile {

    boolean isFile();

    boolean isLink();

    String getOwner();

    String getGroup();

    long getFileSize();

    String getFileName();

    boolean isDirectory();

    String getParentPath();

    String getPermissions();

    default int getFileOrder() {
        if (this.isReturnDirectory()) {
            return -10;
        }
        if (this.isDirectory()) {
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

    default String getFilePath() {
        return ShellFileUtil.concat(this.getParentPath(), this.getFileName());
    }

    default SVGGlyph getIcon() {
        SVGGlyph glyph;
        if (this.isReturnDirectory()) {
            glyph = new ReturnFolderSVGGlyph("12");
        } else if (this.isLink() && this.isDirectory()) {
            glyph = new FolderLinkSVGGlyph("12");
        } else if (this.isLink() && this.isFile()) {
            glyph = new FileLinkSVGGlyph("12");
        } else if (this.isDirectory()) {
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
        return glyph;
    }

    default void startWaiting() {
        SVGGlyph icon = this.getIcon();
        if (icon != null) {
            icon.startWaiting();
        }
    }

    default void stopWaiting() {
        SVGGlyph icon = this.getIcon();
        if (icon != null) {
            icon.stopWaiting();
        }
    }

    default boolean isWaiting() {
        SVGGlyph icon = this.getIcon();
        if (icon != null) {
            return icon.isWaiting();
        }
        return false;
    }

    default boolean isCurrentFile() {
        return ".".equals(this.getFileName());
    }

    default boolean isReturnDirectory() {
        return "..".equals(this.getFileName());
    }

    default boolean isHiddenFile() {
        return this.getFileName().startsWith(".");
    }


}
