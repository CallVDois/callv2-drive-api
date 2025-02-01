package com.callv2.drive.infrastructure.file.persistence;

import java.time.Instant;
import java.util.UUID;

import com.callv2.drive.domain.file.Content;
import com.callv2.drive.domain.file.File;
import com.callv2.drive.domain.file.FileID;
import com.callv2.drive.domain.file.FileName;
import com.callv2.drive.domain.folder.FolderID;
import com.callv2.drive.domain.member.MemberID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name = "File")
@Table(name = "files")
public class FileJpaEntity {

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "folder_id", nullable = false)
    private UUID folderId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "content_location", nullable = false)
    private String contentLocation;

    @Column(name = "content_size", nullable = false)
    private Long contentSize;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted")
    private boolean deleted;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    private FileJpaEntity(
            final UUID id,
            final String ownerId,
            final UUID folderId,
            final String name,
            final String contentType,
            final String contentLocation,
            final Long contentSize,
            final Instant createdAt,
            final Instant updatedAt,
            final boolean deleted,
            final Instant deletedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.folderId = folderId;
        this.name = name;
        this.contentType = contentType;
        this.contentLocation = contentLocation;
        this.contentSize = contentSize;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
        this.deletedAt = deletedAt;
    }

    public FileJpaEntity() {
    }

    public static FileJpaEntity from(final File file) {
        return new FileJpaEntity(
                file.getId().getValue(),
                file.getOwner().getValue(),
                file.getFolder().getValue(),
                file.getName().value(),
                file.getContent().type(),
                file.getContent().location(),
                file.getContent().size(),
                file.getCreatedAt(),
                file.getUpdatedAt(),
                file.isDeleted(),
                file.getDeletedAt());
    }

    public File toDomain() {
        return File.with(
                FileID.of(getId()),
                MemberID.of(getOwnerId()),
                FolderID.of(getFolderId()),
                FileName.of(getName()),
                Content.of(getContentLocation(), getContentType(), getContentSize()),
                getCreatedAt(),
                getUpdatedAt(),
                isDeleted(),
                getDeletedAt());
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public UUID getFolderId() {
        return folderId;
    }

    public void setFolderId(UUID folderId) {
        this.folderId = folderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentLocation() {
        return contentLocation;
    }

    public void setContentLocation(String contentLocation) {
        this.contentLocation = contentLocation;
    }

    public Long getContentSize() {
        return contentSize;
    }

    public void setContentSize(Long contentSize) {
        this.contentSize = contentSize;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

}
