package com.callv2.drive.domain.file;

import java.time.Instant;

import com.callv2.drive.domain.AggregateRoot;
import com.callv2.drive.domain.exception.ValidationException;
import com.callv2.drive.domain.folder.FolderID;
import com.callv2.drive.domain.member.MemberID;
import com.callv2.drive.domain.validation.ValidationHandler;
import com.callv2.drive.domain.validation.handler.Notification;

public class File extends AggregateRoot<FileID> {

    private MemberID owner;

    private FolderID folder;

    private FileName name;
    private Content content;

    private Instant createdAt;
    private Instant updatedAt;

    private boolean deleted;
    private Instant deletedAt;

    private File(
            final FileID anId,
            final MemberID owner,
            final FolderID folder,
            final FileName name,
            final Content content,
            final Instant createdAt,
            final Instant updatedAt,
            final boolean deleted,
            final Instant deletedAt) {
        super(anId);

        this.folder = folder;
        this.owner = owner;
        this.name = name;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

        selfValidate();
    }

    @Override
    public void validate(final ValidationHandler handler) {
        new FileValidator(this, handler).validate();
    }

    public static File with(
            final FileID id,
            final MemberID owner,
            final FolderID folder,
            final FileName name,
            final Content content,
            final Instant createdAt,
            final Instant updatedAt,
            final boolean deleted,
            final Instant deletedAt) {
        return new File(id, owner, folder, name, content, createdAt, updatedAt, deleted, deletedAt);
    }

    public static File with(final File file) {
        return File.with(
                file.getId(),
                file.getOwner(),
                file.getFolder(),
                file.getName(),
                file.getContent(),
                file.getCreatedAt(),
                file.getUpdatedAt(),
                file.isDeleted(),
                file.getDeletedAt());
    }

    public static File create(
            final MemberID owner,
            final FolderID folder,
            final FileName name,
            final Content content) {

        final Instant now = Instant.now();

        return new File(
                FileID.unique(),
                owner,
                folder,
                name,
                content,
                now,
                now,
                false,
                null);
    }

    public File update(
            final FolderID folder,
            final FileName name,
            final Content content) {

        if (this.folder.equals(folder) && this.name.equals(name) && this.content.equals(content))
            return this;

        this.folder = folder;
        this.name = name;
        this.content = content;

        this.updatedAt = Instant.now();

        selfValidate();
        return this;
    }

    public File delete() {
        if (this.deleted)
            return this;

        this.deleted = true;
        this.deletedAt = Instant.now();

        return this;
    }

    private void selfValidate() {
        final var notification = Notification.create();
        validate(notification);

        if (notification.hasError())
            throw ValidationException.with("Validation fail has occoured", notification);
    }

    public MemberID getOwner() {
        return owner;
    }

    public FolderID getFolder() {
        return folder;
    }

    public FileName getName() {
        return name;
    }

    public Content getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

}