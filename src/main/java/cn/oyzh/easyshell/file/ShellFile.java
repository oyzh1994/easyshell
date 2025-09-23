package cn.oyzh.easyshell.file;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.svg.glyph.ReturnFolderSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.*;
import cn.oyzh.easyshell.fx.svg.glyph.file.a.FileAacSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.a.FileActionScriptSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.a.FileAmrSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.a.FileApkSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.a.FileAsciidoctorSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.a.FileAsmSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.a.FileAspSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.b.FileBatSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.b.FileBibtexSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.b.FileBinSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.b.FileBmpSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.b.FileBz2SVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.c.FileCSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.c.FileCerSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.c.FileCfgSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.c.FileChmSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.c.FileClassSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.c.FileClojureSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.c.FileCmdSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.c.FileCoffeeScriptSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.c.FileComSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.c.FileCompressSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.c.FileConfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.c.FileCppSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.c.FileCsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.c.FileCssSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.c.FileCudaSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.d.FileDartSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.d.FileDbSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.d.FileDiffSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.d.FileDllSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.d.FileDmgSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.d.FileDockerfileSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.d.FileDotSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.d.FileDsstoreSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.d.FileDylibSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.e.FileEpubSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.e.FileErlangSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.e.FileExcelSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.e.FileExeSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.f.FileFlacSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.f.FileFlvSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.f.FileFsharpSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.g.FileGifSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.g.FileGitIgnoreSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.g.FileGitRebaseSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.g.FileGoSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.g.FileGradleSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.g.FileGroovySVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.g.FileGzSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.h.FileHandlebarsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.h.FileHlslSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.h.FileHtmlSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.i.FileIcnsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.i.FileIcoSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.i.FileImageSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.i.FileInfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.i.FileIniSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.i.FileIsoSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.j.FileJarSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.j.FileJavaSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.j.FileJpgSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.j.FileJsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.j.FileJsonSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.j.FileJspSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.j.FileJuliaSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.k.FileKconfigSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.k.FileKmkSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.k.FileKtSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.l.FileLatexSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.l.FileLessSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.l.FileLinkSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.l.FileLuaSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.m.FileM4aSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.m.FileMakefileSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.m.FileMarkdownSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.m.FileMkvSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.m.FileMovSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.m.FileMp3SVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.m.FileMp4SVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.o.FileObjectiveCPPSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.o.FileObjectiveCSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.o.FileOcxSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.o.FileOggSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.p.FilePcmSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.p.FilePdfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.p.FilePerlSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.p.FilePhpSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.p.FilePlistSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.p.FilePowershellSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.p.FilePptSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.p.FilePropertiesSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.p.FileProtobufSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.p.FilePsdSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.p.FilePugSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.p.FilePySVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.p.FilePycSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.r.FileRSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.r.FileRarSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.r.FileRazorSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.r.FileRestructuredtextSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.r.FileRmSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.r.FileRmvbSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.r.FileRpmSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.r.FileRssSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.r.FileRtfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.r.FileRubySVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.r.FileRustSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.s.FileSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.s.FileSasSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.s.FileScalaSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.s.FileScssSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.s.FileSearchResultSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.s.FileShSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.s.FileShaderlabSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.s.FileSoSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.s.FileSqlSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.s.FileSrtSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.s.FileSvgSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.s.FileSwfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.s.FileSwiftSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.t.FileTarSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.t.FileTerminalSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.t.FileTexSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.t.FileTextSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.t.FileTsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.t.FileTsxSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.t.FileTtfSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.t.FileTwigSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.u.FileUnknownSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.v.FileVbSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.v.FileVbsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.v.FileVimSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.w.FileWarSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.w.FileWavSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.w.FileWbmpSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.w.FileWebmSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.w.FileWebpSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.w.FileWmaSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.w.FileWordSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.w.FileWpsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.x.FileXlsSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.x.FileXmlSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.x.FileXslSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.x.FileXzSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.y.FileYamlSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.y.FileYmlSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.file.z.FileZipSVGGlyph;
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
        if (this.isLink()) {
            return -9;
        }
        if (this.isDirectory() && this.isHiddenFile()) {
            return -8;
        }
        if (this.isFile() && this.isHiddenFile()) {
            return -7;
        }
        if (this.isDirectory()) {
            return -6;
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
            } else if (FileNameUtil.isPerlType(extName)) {
                glyph = new FilePerlSVGGlyph("12");
            } else if (FileNameUtil.isActionScriptType(extName)) {
                glyph = new FileActionScriptSVGGlyph("12");
            } else if (FileNameUtil.isDartType(extName)) {
                glyph = new FileDartSVGGlyph("12");
            } else if (FileNameUtil.isPhpType(extName)) {
                glyph = new FilePhpSVGGlyph("12");
            } else if (FileNameUtil.isRustType(extName)) {
                glyph = new FileRustSVGGlyph("12");
            } else if (FileNameUtil.isRubyType(extName)) {
                glyph = new FileRubySVGGlyph("12");
            } else if (FileNameUtil.isScalaType(extName)) {
                glyph = new FileScalaSVGGlyph("12");
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
            } else if (FileNameUtil.isWbmpType(extName)) {
                glyph = new FileWbmpSVGGlyph("12");
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
            } else if (FileNameUtil.isFlvType(extName)) {
                glyph = new FileFlvSVGGlyph("12");
            } else if (FileNameUtil.isWebmType(extName)) {
                glyph = new FileWebmSVGGlyph("12");
            } else if (FileNameUtil.isFlacType(extName)) {
                glyph = new FileFlacSVGGlyph("12");
            } else if (FileNameUtil.isAacType(extName)) {
                glyph = new FileAacSVGGlyph("12");
            } else if (FileNameUtil.isM4aType(extName)) {
                glyph = new FileM4aSVGGlyph("12");
            } else if (FileNameUtil.isOggType(extName)) {
                glyph = new FileOggSVGGlyph("12");
            } else if (FileNameUtil.isPcmType(extName)) {
                glyph = new FilePcmSVGGlyph("12");
            } else if (FileNameUtil.isWmaType(extName)) {
                glyph = new FileWmaSVGGlyph("12");
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
            } else if (FileNameUtil.isJsonType(extName) || FileNameUtil.isJsoncType(extName) || FileNameUtil.isJsonlType(extName)) {
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
            } else if (FileNameUtil.isCsvType(extName)) {
                glyph = new FileCSVGGlyph("12");
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
            } else if (FileNameUtil.isAsmType(extName)) {
                glyph = new FileAsmSVGGlyph("12");
            } else if (FileNameUtil.isLessType(extName)) {
                glyph = new FileLessSVGGlyph("12");
            } else if (FileNameUtil.isProtobufType(extName)) {
                glyph = new FileProtobufSVGGlyph("12");
            } else if (FileNameUtil.isPropertiesType(extName)) {
                glyph = new FilePropertiesSVGGlyph("12");
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
            } else if (FileNameUtil.isClojureType(extName)) {
                glyph = new FileClojureSVGGlyph("12");
            } else if (FileNameUtil.isCoffeeScriptType(extName)) {
                glyph = new FileCoffeeScriptSVGGlyph("12");
            } else if (FileNameUtil.isCudaType(extName)) {
                glyph = new FileCudaSVGGlyph("12");
            } else if (FileNameUtil.isDiffType(extName)) {
                glyph = new FileDiffSVGGlyph("12");
            } else if (FileNameUtil.isDockerfileType(extName)) {
                glyph = new FileDockerfileSVGGlyph("12");
            } else if (FileNameUtil.isErlangType(extName)) {
                glyph = new FileErlangSVGGlyph("12");
            } else if (FileNameUtil.isFsharpType(extName)) {
                glyph = new FileFsharpSVGGlyph("12");
            } else if (FileNameUtil.isGoType(extName)) {
                glyph = new FileGoSVGGlyph("12");
            } else if (FileNameUtil.isGroovyType(extName)) {
                glyph = new FileGroovySVGGlyph("12");
            } else if (FileNameUtil.isHandlebarsType(extName)) {
                glyph = new FileHandlebarsSVGGlyph("12");
            } else if (FileNameUtil.isHlslType(extName)) {
                glyph = new FileHlslSVGGlyph("12");
            } else if (FileNameUtil.isJuliaType(extName)) {
                glyph = new FileJuliaSVGGlyph("12");
            } else if (FileNameUtil.isKconfigType(extName)) {
                glyph = new FileKconfigSVGGlyph("12");
            } else if (FileNameUtil.isLatexType(extName)) {
                glyph = new FileLatexSVGGlyph("12");
            } else if (FileNameUtil.isMakefileType(extName)) {
                glyph = new FileMakefileSVGGlyph("12");
            } else if (FileNameUtil.isObjectiveCType(extName)) {
                glyph = new FileObjectiveCSVGGlyph("12");
            } else if (FileNameUtil.isObjectiveCPPType(extName)) {
                glyph = new FileObjectiveCPPSVGGlyph("12");
            } else if (FileNameUtil.isPowershellType(extName)) {
                glyph = new FilePowershellSVGGlyph("12");
            } else if (FileNameUtil.isPugType(extName)) {
                glyph = new FilePugSVGGlyph("12");
            } else if (FileNameUtil.isRType(extName)) {
                glyph = new FileRSVGGlyph("12");
            } else if (FileNameUtil.isRazorType(extName)) {
                glyph = new FileRazorSVGGlyph("12");
            } else if (FileNameUtil.isRestructuredtextType(extName)) {
                glyph = new FileRestructuredtextSVGGlyph("12");
            } else if (FileNameUtil.isScssType(extName)) {
                glyph = new FileScssSVGGlyph("12");
            } else if (FileNameUtil.isShaderlabType(extName)) {
                glyph = new FileShaderlabSVGGlyph("12");
            } else if (FileNameUtil.isSwiftType(extName)) {
                glyph = new FileSwiftSVGGlyph("12");
            } else if (FileNameUtil.isTexType(extName)) {
                glyph = new FileTexSVGGlyph("12");
            } else if (FileNameUtil.isTwigType(extName)) {
                glyph = new FileTwigSVGGlyph("12");
            } else if (FileNameUtil.isVimType(extName)) {
                glyph = new FileVimSVGGlyph("12");
            } else if (FileNameUtil.isXslType(extName)) {
                glyph = new FileXslSVGGlyph("12");
            } else if (FileNameUtil.isSearchResultType(extName)) {
                glyph = new FileSearchResultSVGGlyph("12");
            } else if (FileNameUtil.isAsciidocType(extName)) {
                glyph = new FileAsciidoctorSVGGlyph("12");
            } else if (FileNameUtil.isBibtexType(extName)) {
                glyph = new FileBibtexSVGGlyph("12");
            } else if (FileNameUtil.isGitRebaseType(extName)) {
                glyph = new FileGitRebaseSVGGlyph("12");
            } else if (FileNameUtil.isSasType(extName)) {
                glyph = new FileSasSVGGlyph("12");
            } else if (FileNameUtil.isGitIgnoreType(extName)) {
                glyph = new FileGitIgnoreSVGGlyph("12");
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
     * 刷新图标
     */
    void refreshIcon();

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

    /**
     * 获取扩展名
     *
     * @return 扩展名
     */
    default String getExtName() {
        return FileNameUtil.extName(this.getFileName());
    }
}
