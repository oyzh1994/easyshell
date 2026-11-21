package cn.oyzh.easyshell.llm.skill;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.store.llm.LLMSkillStore;
import dev.langchain4j.skills.FileSystemSkillLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Skill 文件存储、校验与释放。
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMSkillManager {

    public static final LLMSkillManager INSTANCE =
            new LLMSkillManager(Path.of(ShellConst.getSkillPath()),
                    Path.of(ShellConst.getSkillCachePath()), LLMSkillStore.INSTANCE);

    private final Path archiveRoot;

    private final Path releaseRoot;

    private final LLMSkillStore store;

    public LLMSkillManager(Path archiveRoot, LLMSkillStore store) {
        this(archiveRoot, archiveRoot.resolve("release"), store);
    }

    public LLMSkillManager(Path archiveRoot, Path releaseRoot, LLMSkillStore store) {
        this.archiveRoot = archiveRoot.toAbsolutePath().normalize();
        this.releaseRoot = releaseRoot.toAbsolutePath().normalize();
        this.store = store;
    }

    public synchronized LLMSkill create(String name, File source)
            throws IOException {
        this.validateSourceRoot(source);
        Files.createDirectories(this.archiveRoot);
        String uid = UUIDUtil.uuidSimple();
        Path work = Files.createTempDirectory(this.archiveRoot, ".skill-create-");
        Path archive = this.archivePath(uid);
        LLMSkill skill = new LLMSkill();
        skill.setUid(uid);
        skill.setName(name);
        skill.setEnabled(false);
        try {
            LLMSkillArchiveUtil.PreparedArchive prepared =
                    LLMSkillArchiveUtil.prepare(source.getAbsolutePath(), work);
            Files.move(prepared.zip(), archive, StandardCopyOption.REPLACE_EXISTING);
            skill.setFilePath(archive.toString());
            skill.setHash(prepared.hash());
            skill.setFileSize(Files.size(archive));
            if (!this.release(skill)) {
                throw new IOException("Skill validation failed");
            }
            skill.setValidationState(LLMSkillValidationState.NORMAL, null);
            if (!this.store.insert(skill)) {
                throw new IOException("Failed to save skill");
            }
            return skill;
        } catch (Exception ex) {
            this.deleteFiles(uid);
            if (ex instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException(ex);
        } finally {
            LLMSkillArchiveUtil.deleteTree(work);
        }
    }

    public synchronized LLMSkill update(LLMSkill skill,
                                        String name,
                                        File source) throws IOException {
        if (skill == null || StringUtil.isBlank(skill.getUid())) {
            throw new IllegalArgumentException("Skill is required");
        }
        String oldName = skill.getName();
        boolean oldEnabled = skill.isEnabled();
        Long oldFileSize = skill.getFileSize();
        String oldHash = skill.getHash();
        String oldFilePath = skill.getFilePath();
        if (source == null) {
            skill.setName(name);
            if (!this.store.update(skill)) {
                restoreFields(skill, oldName, oldEnabled, oldHash, oldFilePath, oldFileSize);
                throw new IOException("Failed to update skill");
            }
            return skill;
        }

        this.validateSourceRoot(source);
        Files.createDirectories(this.archiveRoot);
        Path work = Files.createTempDirectory(this.archiveRoot, ".skill-update-");
        Path archive = this.archivePath(skill.getUid());
        Path release = this.releasePath(skill.getUid());
        Path backupArchive = work.resolve("archive.bak");
        Path backupRelease = work.resolve("release.bak");
        boolean archiveMoved = false;
        boolean releaseMoved = false;
        boolean newArchiveInstalled = false;
        try {
            LLMSkillArchiveUtil.PreparedArchive prepared =
                    LLMSkillArchiveUtil.prepare(source.getAbsolutePath(), work.resolve("prepare"));
            if (Files.exists(archive)) {
                Files.move(archive, backupArchive, StandardCopyOption.REPLACE_EXISTING);
                archiveMoved = true;
            }
            if (Files.exists(release)) {
                Files.move(release, backupRelease, StandardCopyOption.REPLACE_EXISTING);
                releaseMoved = true;
            }
            Files.copy(prepared.zip(), archive, StandardCopyOption.REPLACE_EXISTING);
            newArchiveInstalled = true;
            skill.setName(name);
            skill.setFilePath(archive.toString());
            skill.setHash(prepared.hash());
            skill.setFileSize(Files.size(prepared.zip()));
            if (!this.release(skill) || !this.store.update(skill)) {
                throw new IOException("Failed to update skill");
            }
            skill.setValidationState(LLMSkillValidationState.NORMAL, null);
            return skill;
        } catch (Exception ex) {
            restoreFields(skill, oldName, oldEnabled, oldHash, oldFilePath, oldFileSize);
            if (newArchiveInstalled) {
                LLMSkillArchiveUtil.deleteTree(archive);
                LLMSkillArchiveUtil.deleteTree(release);
            }
            if (archiveMoved && Files.exists(backupArchive)) {
                Files.move(backupArchive, archive, StandardCopyOption.REPLACE_EXISTING);
            }
            if (releaseMoved && Files.exists(backupRelease)) {
                Files.move(backupRelease, release, StandardCopyOption.REPLACE_EXISTING);
            }
            if (ex instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException(ex);
        } finally {
            LLMSkillArchiveUtil.deleteTree(work);
        }
    }

    public synchronized boolean release(LLMSkill skill) {
        if (skill == null || StringUtil.isBlank(skill.getUid())) {
            JulLog.warn("refuse to release skill without uid");
            return false;
        }
        Path archive = this.archivePath(skill.getUid());
        try {
            if (StringUtil.isNotBlank(skill.getFilePath())
                    && !archive.equals(Path.of(skill.getFilePath()).toAbsolutePath().normalize())) {
                throw new IOException("Skill archive path is not managed: " + skill.getFilePath());
            }
            LLMSkillValidationState state = this.check(skill);
            if (state != LLMSkillValidationState.NORMAL) {
                throw new IOException("Skill archive validation failed: " + state);
            }
            Files.createDirectories(this.releaseRoot);
            Path temporary = Files.createTempDirectory(this.releaseRoot, ".skill-release-");
            Path release = this.releasePath(skill.getUid());
            Path backup = null;
            try {
                LLMSkillArchiveUtil.extract(archive, temporary,
                        LLMSkillArchiveUtil.inspectArchive(archive));
                FileSystemSkillLoader.loadSkill(temporary);
                if (Files.exists(release)) {
                    backup = Files.createTempDirectory(this.releaseRoot,
                                    ".skill-release-backup-")
                            .resolve("previous");
                    Files.move(release, backup, StandardCopyOption.ATOMIC_MOVE);
                }
                try {
                    Files.move(temporary, release, StandardCopyOption.ATOMIC_MOVE);
                    LLMSkillArchiveUtil.deleteTree(this.archiveRoot.resolve(skill.getUid()));
                } catch (Exception ex) {
                    if (backup != null && Files.exists(backup)) {
                        Files.move(backup, release, StandardCopyOption.ATOMIC_MOVE);
                    }
                    throw ex;
                }
            } finally {
                LLMSkillArchiveUtil.deleteTree(temporary);
                if (backup != null && Files.exists(backup.getParent())) {
                    LLMSkillArchiveUtil.deleteTree(backup.getParent());
                }
            }
            return true;
        } catch (Exception ex) {
            JulLog.error("refuse to release invalid skill " + skill.getName() + ": " + ex.getMessage(), ex);
            return false;
        }
    }

    public boolean verify(LLMSkill skill) {
        return this.check(skill) == LLMSkillValidationState.NORMAL;
    }

    public LLMSkillValidationState check(LLMSkill skill) {
        if (skill == null || StringUtil.isBlank(skill.getUid())) {
            return LLMSkillValidationState.INVALID;
        }
        try {
            Path archive = this.archivePath(skill.getUid());
            if (!Files.isRegularFile(archive)) {
                return LLMSkillValidationState.FILE_MISSING;
            }
            if (Files.size(archive) > LLMSkillArchiveUtil.MAX_FILE_BYTES) {
                return LLMSkillValidationState.INVALID;
            }
            if (StringUtil.isBlank(skill.getHash())
                    || !skill.getHash().equalsIgnoreCase(
                    LLMSkillArchiveUtil.sha256(archive))) {
                return LLMSkillValidationState.HASH_INVALID;
            }
            return LLMSkillValidationState.NORMAL;
        } catch (Exception ex) {
            JulLog.warn("invalid skill " + skill.getName() + ": " + ex.getMessage());
            return LLMSkillValidationState.INVALID;
        }
    }

    public void export(LLMSkill skill, Path target) throws IOException {
        if (skill == null) {
            throw new IllegalArgumentException("Skill is required");
        }
        LLMSkillArchiveUtil.copy(this.archivePath(skill.getUid()), target);
    }

    public synchronized void deleteFiles(String uid) {
        if (StringUtil.isBlank(uid)) {
            return;
        }
        try {
            LLMSkillArchiveUtil.deleteTree(this.archivePath(uid));
            LLMSkillArchiveUtil.deleteTree(this.releasePath(uid));
            LLMSkillArchiveUtil.deleteTree(this.archiveRoot.resolve(uid));
        } catch (Exception ex) {
            JulLog.warn("delete skill files failed: " + uid, ex);
        }
    }

    public Path archivePath(String uid) {
        return this.archiveRoot.resolve(requireSafeUid(uid) + ".zip").normalize();
    }

    public Path releasePath(String uid) {
        return this.releaseRoot.resolve(requireSafeUid(uid)).normalize();
    }

    private static String requireSafeUid(String uid) {
        if (StringUtil.isBlank(uid) || !uid.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("Invalid skill uid");
        }
        return uid;
    }

    private void validateSourceRoot(File source) throws IOException {
        if (source == null || !source.isDirectory()) {
            return;
        }
        Path sourcePath = source.toPath().toAbsolutePath().normalize();
        if (sourcePath.equals(this.archiveRoot) || this.archiveRoot.startsWith(sourcePath)
                || sourcePath.equals(this.releaseRoot) || this.releaseRoot.startsWith(sourcePath)) {
            throw new IOException(
                    "Skill source folder cannot contain the managed skills directories");
        }
    }

    private static void restoreFields(LLMSkill skill,
                                      String name,
                                      boolean enabled,
                                      String hash,
                                      String filePath,
                                      Long fileSize) {
        skill.setName(name);
        skill.setEnabled(enabled);
        skill.setHash(hash);
        skill.setFilePath(filePath);
        skill.setFileSize(fileSize);
    }
}
