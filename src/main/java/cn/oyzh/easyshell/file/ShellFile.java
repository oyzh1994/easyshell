package cn.oyzh.easyshell.file;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.svg.glyph.ReturnFolderSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.File3gpSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.File7zSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileAmrSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileApkSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileAspSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileBatSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileBinSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileBmpSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileBz2SVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileCSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileCerSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileCfgSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileChmSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileClassSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileCmdSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileComSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileCompressSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileConfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileCppSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileCsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileCssSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileDbSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileDllSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileDmgSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileDotSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileDsstoreSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileDylibSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileEpubSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileExcelSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileExeSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileGifSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileGradleSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileGzSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileHtmlSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileIcnsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileIcoSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileImageSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileInfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileIniSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileIsoSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileJarSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileJavaSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileJpgSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileJsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileJsonSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileJspSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileKmkSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileKtSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileLinkSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileLuaSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileMarkdownSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileMkvSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileMovSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileMp3SVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileMp4SVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileOcxSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FilePdfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FilePlistSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FilePptSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FilePsdSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FilePySVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FilePycSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileRarSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileRmSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileRmvbSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileRpmSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileRssSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileRtfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileShSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileSoSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileSqlSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileSrtSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileSvgSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileSwfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileTarSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileTerminalSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileTextSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileTsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileTsxSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileTtfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileUnknownSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileVbSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileVbsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileWarSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileWavSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileWebpSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileWordSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileWpsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileXlsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileXmlSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileXzSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileYamlSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileYmlSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileZipSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FolderLinkSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.FolderSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 文件接口
 *
 * @author oyzh
 * @since 2025-04-28
 */
public interface ShellFile extends ObjectCopier<ShellFile> {

    /**
     * 是否文件
     *
     * @return 结果
     */
    boolean isFile();

    /**
     * 是否链接
     *
     * @return 结果
     */
    boolean isLink();

    /**
     * 获取拥有者
     *
     * @return 拥有者
     */
    String getOwner();

    /**
     * 获取分组
     *
     * @return 分组
     */
    String getGroup();

    /**
     * 获取文件大小
     *
     * @return 文件大小
     */
    long getFileSize();

    /**
     * 设置文件大小
     *
     * @param fileSize 文件大小
     */
    void setFileSize(long fileSize);

    /**
     * 获取显示用文件大小
     *
     * @return 文件大小
     */
    default String getFileSizeDisplay() {
        if (this.isDirectory() || this.isReturnDirectory() || this.isCurrentFile()) {
            return "-";
        }
        return NumberUtil.formatSize(this.getFileSize(), 4);
    }

    /**
     * 获取文件名称
     *
     * @return 文件名称
     */
    String getFileName();

    /**
     * 设置文件名称
     *
     * @param fileName 文件名称
     */
    void setFileName(String fileName);

    /**
     * 是否目录
     *
     * @return 结果
     */
    boolean isDirectory();

    /**
     * 获取父路径
     *
     * @return 父路径
     */
    String getParentPath();

    /**
     * 获取权限
     *
     * @return 权限
     */
    String getPermissions();

    /**
     * 设置权限
     *
     * @param permissions 权限
     */
    void setPermissions(String permissions);

    /**
     * 获取修改时间
     *
     * @return 修改时间
     */
    String getModifyTime();

    /**
     * 设置修改时间
     *
     * @param modifyTime 修改时间
     */
    void setModifyTime(String modifyTime);

    /**
     * 获取文件排序
     *
     * @return 文件排序
     */
    default int getFileOrder() {
        if (this.isReturnDirectory()) {
            return -10;
        }
        if (this.isDirectory() && this.isHiddenFile()) {
            return -9;
        }
        if (this.isFile() && this.isHiddenFile()) {
            return -8;
        }
        if (this.isDirectory()) {
            return -7;
        }
        return 0;
    }

    /**
     * 获取文件路径
     *
     * @return 文件路径
     */
    default String getFilePath() {
        return ShellFileUtil.concat(this.getParentPath(), this.getFileName());
    }

    /**
     * 获取图标
     *
     * @return 图标
     */
    default SVGGlyph getIcon() {
        SVGGlyph glyph;
        if (this.isReturnDirectory()) {
            glyph = new ReturnFolderSVGGlyph("12");
        } else if (this.isLink() && this.isDirectory()) {
            glyph = new FolderLinkSVGGlyph("12");
        } else if (this.isLink() && this.isFile()) {
            glyph = new FileLinkSVGGlyph("12");
        } else if (this.isLink()) {
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
            } else if (FileNameUtil.isXzType(extName)) {
                glyph = new FileXzSVGGlyph("12");
            } else if (FileNameUtil.isBz2Type(extName)) {
                glyph = new FileBz2SVGGlyph("12");
            } else if (FileNameUtil.isZipType(extName)) {
                glyph = new FileZipSVGGlyph("12");
            } else if (FileNameUtil.isTsType(extName)) {
                glyph = new FileTsSVGGlyph("12");
            } else if (FileNameUtil.isApkType(extName)) {
                glyph = new FileApkSVGGlyph("12");
            } else if (FileNameUtil.is3gpType(extName)) {
                glyph = new File3gpSVGGlyph("12");
            } else if (FileNameUtil.isAmrType(extName)) {
                glyph = new FileAmrSVGGlyph("12");
            } else if (FileNameUtil.isRpmType(extName)) {
                glyph = new FileRpmSVGGlyph("12");
            } else if (FileNameUtil.isWpsType(extName)) {
                glyph = new FileWpsSVGGlyph("12");
            } else if (FileNameUtil.isWebpType(extName)) {
                glyph = new FileWebpSVGGlyph("12");
            } else if (FileNameUtil.isSqlType(extName)) {
                glyph = new FileSqlSVGGlyph("12");
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
            } else if (FileNameUtil.isJavaType(extName)) {
                glyph = new FileJavaSVGGlyph("12");
            } else if (FileNameUtil.isPlistType(extName)) {
                glyph = new FilePlistSVGGlyph("12");
            } else if (FileNameUtil.isVbType(extName)) {
                glyph = new FileVbSVGGlyph("12");
            } else if (FileNameUtil.isVbsType(extName)) {
                glyph = new FileVbsSVGGlyph("12");
            } else if (FileNameUtil.isWarType(extName)) {
                glyph = new FileWarSVGGlyph("12");
            } else if (FileNameUtil.isTsxType(extName)) {
                glyph = new FileTsxSVGGlyph("12");
            } else if (FileNameUtil.isCType(extName)) {
                glyph = new FileCSVGGlyph("12");
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
            } else if (FileNameUtil.isLuaType(extName)) {
                glyph = new FileLuaSVGGlyph("12");
            } else if (FileNameUtil.isKtType(extName)) {
                glyph = new FileKtSVGGlyph("12");
            } else if (FileNameUtil.isKmkType(extName)) {
                glyph = new FileKmkSVGGlyph("12");
            } else if (FileNameUtil.isIcnsType(extName)) {
                glyph = new FileIcnsSVGGlyph("12");
            } else if (FileNameUtil.isCsType(extName)) {
                glyph = new FileCsSVGGlyph("12");
            } else if (FileNameUtil.isSvgType(extName)) {
                glyph = new FileSvgSVGGlyph("12");
            } else if (FileNameUtil.isCerType(extName)) {
                glyph = new FileCerSVGGlyph("12");
            } else if (FileNameUtil.isCfgType(extName)) {
                glyph = new FileCfgSVGGlyph("12");
            } else if (FileNameUtil.isChmType(extName)) {
                glyph = new FileChmSVGGlyph("12");
            } else if (FileNameUtil.isClassType(extName)) {
                glyph = new FileClassSVGGlyph("12");
            } else if (FileNameUtil.isComType(extName)) {
                glyph = new FileComSVGGlyph("12");
            } else if (FileNameUtil.isConfigType(extName)) {
                glyph = new FileConfSVGGlyph("12");
            } else if (FileNameUtil.isCppType(extName)) {
                glyph = new FileCppSVGGlyph("12");
            } else if (FileNameUtil.isDbType(extName)) {
                glyph = new FileDbSVGGlyph("12");
            } else if (FileNameUtil.isEpubType(extName)) {
                glyph = new FileEpubSVGGlyph("12");
            } else if (FileNameUtil.isGradleType(extName)) {
                glyph = new FileGradleSVGGlyph("12");
            } else if (FileNameUtil.isInfType(extName)) {
                glyph = new FileInfSVGGlyph("12");
            } else if (FileNameUtil.isOcxType(extName)) {
                glyph = new FileOcxSVGGlyph("12");
            } else if (FileNameUtil.isPycType(extName)) {
                glyph = new FilePycSVGGlyph("12");
            } else if (FileNameUtil.isDsstoreType(extName)) {
                glyph = new FileDsstoreSVGGlyph("12");
            } else {
                glyph = new FileSVGGlyph("12");
            }
        }
        if (this.isHiddenFile()) {
            glyph.setOpacity(0.5);
        }
        return glyph;
    }

    /**
     * 开始等待动画
     */
    default void startWaiting() {
        SVGGlyph icon = this.getIcon();
        if (icon != null) {
            icon.startWaiting();
        }
    }

    /**
     * 结束等待动画
     */
    default void stopWaiting() {
        SVGGlyph icon = this.getIcon();
        if (icon != null) {
            icon.stopWaiting();
        }
    }

    /**
     * 等待动画是否开启中
     *
     * @return 结果
     */
    default boolean isWaiting() {
        SVGGlyph icon = this.getIcon();
        if (icon != null) {
            return icon.isWaiting();
        }
        return false;
    }

    /**
     * 是否当前"文件"
     *
     * @return 结果
     */
    default boolean isCurrentFile() {
        return ".".equals(this.getFileName());
    }

    /**
     * 是否返回"文件"
     *
     * @return 结果
     */
    default boolean isReturnDirectory() {
        return "..".equals(this.getFileName());
    }

    /**
     * 是否正常文件
     *
     * @return 结果
     */
    default boolean isNormal() {
        return !this.isCurrentFile() && !this.isReturnDirectory();
    }

    /**
     * 是否隐藏文件
     *
     * @return 结果
     */
    default boolean isHiddenFile() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return false;
        }
        return this.getFileName().startsWith(".");
    }

    /**
     * 是否有当前用户读取权限
     *
     * @return 结果
     */
    default boolean hasOwnerReadPermission() {
        return ShellFileUtil.hasOwnerReadPermission(this.getPermissions());
    }

    /**
     * 是否有当前用户写入权限
     *
     * @return 结果
     */
    default boolean hasOwnerWritePermission() {
        return ShellFileUtil.hasOwnerWritePermission(this.getPermissions());
    }

    /**
     * 是否有当前用户执行权限
     *
     * @return 结果
     */
    default boolean hasOwnerExecutePermission() {
        return ShellFileUtil.hasOwnerExecutePermission(this.getPermissions());
    }

    /**
     * 是否有组用户读取权限
     *
     * @return 结果
     */
    default boolean hasGroupsReadPermission() {
        return ShellFileUtil.hasGroupsReadPermission(this.getPermissions());
    }

    /**
     * 是否有组用户写入权限
     *
     * @return 结果
     */
    default boolean hasGroupsWritePermission() {
        return ShellFileUtil.hasGroupsWritePermission(this.getPermissions());
    }

    /**
     * 是否有组用户执行权限
     *
     * @return 结果
     */
    default boolean hasGroupsExecutePermission() {
        return ShellFileUtil.hasGroupsExecutePermission(this.getPermissions());
    }

    /**
     * 是否有其他用户读取权限
     *
     * @return 结果
     */
    default boolean hasOthersReadPermission() {
        return ShellFileUtil.hasOthersReadPermission(this.getPermissions());
    }

    /**
     * 是否有其他用户写入权限
     *
     * @return 结果
     */
    default boolean hasOthersWritePermission() {
        return ShellFileUtil.hasOthersWritePermission(this.getPermissions());
    }

    /**
     * 是否有其他用户执行权限
     *
     * @return 结果
     */
    default boolean hasOthersExecutePermission() {
        return ShellFileUtil.hasOthersExecutePermission(this.getPermissions());
    }

    /**
     * 是否根目录
     *
     * @return 结果
     */
    default boolean isRoot() {
        return this.isDirectory() && "/".equals(this.getFilePath());
    }

}
