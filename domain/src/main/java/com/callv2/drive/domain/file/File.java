package com.callv2.drive.domain.file;

import java.time.Instant;

import com.callv2.drive.domain.AggregateRoot;
import com.callv2.drive.domain.exception.ValidationException;
import com.callv2.drive.domain.validation.ValidationHandler;
import com.callv2.drive.domain.validation.handler.Notification;

public class File extends AggregateRoot<FileID> {

    private FileName name;
    private FileExtension extension;
    private BinaryContentID content;

    private Instant createdAt;
    private Instant updatedAt;

    private File(
            final FileID anId,
            final FileName name,
            final FileExtension extension,
            final BinaryContentID content,
            final Instant createdAt,
            final Instant updatedAt) {
        super(anId);

        this.name = name;
        this.extension = extension;
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
            final FileName name,
            final FileExtension extension,
            final BinaryContentID content,
            final Instant createdAt,
            final Instant updatedAt) {
        return new File(id, name, extension, content, createdAt, updatedAt);
    }

    public static File with(final File file) {
        return File.with(
                file.getId(),
                file.getName(),
                file.getExtension(),
                file.getContent(),
                file.getCreatedAt(),
                file.getUpdatedAt());
    }

    public static File create(
            final FileName name,
            final FileExtension extension,
            final BinaryContentID content) {

        final Instant now = Instant.now();

        return new File(
                FileID.unique(),
                name,
                extension,
                content,
                now,
                now);
    }

    public File update(
            final FileName name,
            final FileExtension extension) {

        this.name = name;
        this.extension = extension;

        this.updatedAt = Instant.now();

        selfValidate();
        return this;
    }

    public FileName getName() {
        return name;
    }

    public FileExtension getExtension() {
        return extension;
    }

    public BinaryContentID getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private void selfValidate() {
        final var notification = Notification.create();
        validate(notification);

        if (notification.hasError())
            throw ValidationException.with("Validation fail has occoured", notification);
    }

}