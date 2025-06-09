// package cn.oyzh.easyshell.sshj;
//
// import cn.oyzh.common.date.DateHelper;
// import cn.oyzh.easyshell.file.ShellFile;
// import cn.oyzh.easyshell.file.ShellFileUtil;
// import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
// import com.jcraft.jsch.SftpATTRS;
// import javafx.beans.property.SimpleStringProperty;
// import javafx.beans.property.StringProperty;
// import net.schmizz.sshj.sftp.FileAttributes;
// import net.schmizz.sshj.sftp.FileMode;
// import net.schmizz.sshj.sftp.RemoteResourceInfo;
//
// import java.util.Date;
//
// /**
//  * @author oyzh
//  * @since 2025-03-05
//  */
// public class ShellSFTPFile implements ShellFile {
//
//     /**
//      * 文件对象
//      */
//     private RemoteResourceInfo entry;
//
//     /**
//      * 文件属性
//      */
//     private FileAttributes attrs;
//
//     /**
//      * 拥有者
//      */
//     private String owner;
//
//     /**
//      * 分组
//      */
//     private String group;
//
//     /**
//      * 文件名
//      */
//     private String fileName;
//
//     /**
//      * 父路径
//      */
//     private String parentPath;
//
//     /**
//      * 链接属性
//      */
//     private SftpATTRS linkAttrs;
//
//     /**
//      * 文件图标
//      */
//     private SVGGlyph icon;
//
//     public RemoteResourceInfo getEntry() {
//         return entry;
//     }
//
//     public void setEntry(RemoteResourceInfo entry) {
//         this.entry = entry;
//     }
//
//     public FileAttributes getAttrs() {
//         if (this.attrs == null) {
//             return this.entry.getAttributes();
//         }
//         return this.attrs;
//     }
//
//     public SftpATTRS getLinkAttrs() {
//         return linkAttrs;
//     }
//
//     public void setLinkAttrs(SftpATTRS linkAttrs) {
//         this.linkAttrs = linkAttrs;
//         if (this.icon != null) {
//             this.refreshIcon();
//         }
//     }
//
//     public String getOwner() {
//         return owner;
//     }
//
//     public void setOwner(String owner) {
//         this.owner = owner;
//     }
//
//     public String getGroup() {
//         return group;
//     }
//
//     @Override
//     public long getFileSize() {
//         return this.getAttrs().getSize();
//     }
//
//     @Override
//     public void setFileSize(long fileSize) {
//         // this.getAttrs().setSIZE(fileSize);
//         FileAttributes attrs = this.getAttrs();
//         FileAttributes.Builder builder = new FileAttributes.Builder();
//         this.attrs = builder.withPermissions(attrs.getPermissions())
//                 .withAtimeMtime(attrs.getAtime(), attrs.getMtime())
//                 .withSize(fileSize)
//                 .withUIDGID(attrs.getUID(), attrs.getGID())
//                 .build();
//     }
//
//     public void setGroup(String group) {
//         this.group = group;
//     }
//
//     public void setFileName(String fileName) {
//         this.fileName = fileName;
//     }
//
//     public String getParentPath() {
//         return parentPath;
//     }
//
//     public void setParentPath(String parentPath) {
//         this.parentPath = parentPath;
//     }
//
//     public void setIcon(SVGGlyph icon) {
//         this.icon = icon;
//     }
//
//     public ShellSFTPFile(String parentPath, RemoteResourceInfo entry) {
//         this.parentPath = parentPath;
//         this.entry = entry;
//         this.updatePermissions();
//     }
//
//     public ShellSFTPFile(String parentPath, String fileName, FileAttributes attrs) {
//         this.parentPath = parentPath;
//         this.fileName = fileName;
//         this.attrs = attrs;
//         this.updatePermissions();
//     }
//
//     @Override
//     public SVGGlyph getIcon() {
//         if (this.icon == null) {
//             this.refreshIcon();
//         }
//         return this.icon;
//     }
//
//     /**
//      * 刷新图标
//      */
//     private void refreshIcon() {
//         this.icon = ShellFile.super.getIcon();
//     }
//
//     @Override
//     public String getFileName() {
//         if (this.fileName == null) {
//             return this.entry.getName();
//         }
//         return this.fileName;
//     }
//
//     @Override
//     public String getFilePath() {
//         String fileName = this.getFileName();
//         if (fileName.startsWith("/")) {
//             return fileName;
//         }
//         return ShellFileUtil.concat(this.parentPath, fileName);
//     }
//
//     private StringProperty permissionsProperty;
//
//     public StringProperty permissionsProperty() {
//         if (this.permissionsProperty == null) {
//             this.permissionsProperty = new SimpleStringProperty();
//         }
//         return this.permissionsProperty;
//     }
//
//     protected void updatePermissions() {
//         String permissions = ShellSFTPUtil.formatPermissions(this.getAttrs());
//         this.permissionsProperty().set(permissions);
//     }
//
//     @Override
//     public String getPermissions() {
//         if (this.isReturnDirectory() || this.isCurrentFile()) {
//             return "";
//         }
//         return permissionsProperty().get();
//     }
//
//     @Override
//     public void setPermissions(String permissions) {
//         int permissionInt = ShellFileUtil.toPermissionInt(permissions);
//         FileAttributes attrs = this.getAttrs();
//         FileAttributes.Builder builder = new FileAttributes.Builder();
//         this.attrs = builder.withPermissions(permissionInt)
//                 .withAtimeMtime(attrs.getAtime(), attrs.getMtime())
//                 .withSize(attrs.getSize())
//                 .withUIDGID(attrs.getUID(), attrs.getGID())
//                 .build();
//         // this.getAttrs().setPERMISSIONS(permissionInt);
//         this.updatePermissions();
//     }
//
//     public String getAddTime() {
//         if (this.isReturnDirectory() || this.isCurrentFile()) {
//             return "";
//         }
//         long aTime = this.getAttrs().getAtime();
//         return DateHelper.formatDateTime(new Date(aTime));
//     }
//
//     @Override
//     public String getModifyTime() {
//         if (this.isReturnDirectory() || this.isCurrentFile()) {
//             return "";
//         }
//         long mtime = this.getAttrs().getMtime();
//         return DateHelper.formatDateTime(new Date(mtime));
//     }
//
//     /**
//      * 获取修改时间戳
//      *
//      * @return 结果
//      */
//     public long getMTime() {
//         return this.getAttrs().getMtime();
//     }
//
//     @Override
//     public void setModifyTime(String modifyTime) {
//         Date date = DateHelper.parseDateTime(modifyTime);
//         // int atime = this.getAttrs().getATime();
//         // int mtime = Math.toIntExact(date.getTime() / 1000);
//         // this.getAttrs().setACMODTIME(atime, mtime);
//         FileAttributes attrs = this.getAttrs();
//         FileAttributes.Builder builder = new FileAttributes.Builder();
//         this.attrs = builder.withPermissions(attrs.getPermissions())
//                 .withAtimeMtime(attrs.getAtime(), date.getTime())
//                 .withSize(attrs.getSize())
//                 .withUIDGID(attrs.getUID(), attrs.getGID())
//                 .build();
//     }
//
//     public int getUid() {
//         return this.getAttrs().getUID();
//     }
//
//     public int getGid() {
//         return this.getAttrs().getGID();
//     }
//
//     @Override
//     public boolean isFile() {
//         if (this.isLink()) {
//             if (this.linkAttrs != null) {
//                 return this.linkAttrs.isReg();
//             }
//             return false;
//         }
//         return this.getAttrs().getType() == FileMode.Type.REGULAR;
//     }
//
//     @Override
//     public boolean isLink() {
//         return this.getAttrs().getType() == FileMode.Type.SYMLINK;
//     }
//
//     @Override
//     public void copy(ShellFile t1) {
//         if (t1 instanceof ShellSFTPFile file) {
//             if (file.entry != null) {
//                 this.entry = file.entry;
//             }
//             if (file.attrs != null) {
//                 this.attrs = file.attrs;
//             }
//             if (file.owner != null) {
//                 this.owner = file.owner;
//             }
//             if (file.group != null) {
//                 this.group = file.group;
//             }
//             this.fileName = file.fileName;
//             this.linkAttrs = file.linkAttrs;
//             this.parentPath = file.parentPath;
//             this.updatePermissions();
//         }
//     }
//
//     @Override
//     public boolean isDirectory() {
//         if (this.isLink()) {
//             if (this.linkAttrs != null) {
//                 return this.linkAttrs.isDir();
//             }
//             return false;
//         }
//         return this.getAttrs().getType() == FileMode.Type.DIRECTORY;
//     }
// }
