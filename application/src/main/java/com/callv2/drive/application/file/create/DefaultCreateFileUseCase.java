package com.callv2.drive.application.file.create;

import java.util.Objects;

import com.callv2.drive.domain.exception.InternalErrorException;
import com.callv2.drive.domain.exception.ValidationException;
import com.callv2.drive.domain.file.BinaryContent;
import com.callv2.drive.domain.file.ContentGateway;
import com.callv2.drive.domain.file.File;
import com.callv2.drive.domain.file.FileGateway;
import com.callv2.drive.domain.file.FileName;
import com.callv2.drive.domain.validation.handler.Notification;

public class DefaultCreateFileUseCase extends CreateFileUseCase {

    private final FileGateway fileGateway;
    private final ContentGateway contentGateway;

    public DefaultCreateFileUseCase(
            final FileGateway fileGateway,
            final ContentGateway contentGateway) {
        this.fileGateway = Objects.requireNonNull(fileGateway);
        this.contentGateway = Objects.requireNonNull(contentGateway);
    }

    @Override
    public CreateFileOutput execute(final CreateFileInput input) {

        final FileName fileName = FileName.of(input.name());
        final String contentType = input.contentType();
        final BinaryContent binaryContent = BinaryContent.create(input.content());
        sotoreBinaryContent(binaryContent);

        final Notification notification = Notification.create();

        final File file = notification.valdiate(() -> File.create(fileName, contentType, binaryContent.getId()));

        if (notification.hasError())
            throw ValidationException.with("Could not create Aggregate File", notification);

        return CreateFileOutput.from(fileGateway.create(file));
    }

    private void sotoreBinaryContent(final BinaryContent binaryContent) {
        try {
            contentGateway.store(binaryContent);
        } catch (Exception e) {
            throw InternalErrorException.with("Could not store BinaryContent", e);
        }
    }

}
