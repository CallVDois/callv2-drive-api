package com.callv2.drive.application.file.remove;

import com.callv2.drive.domain.file.FileGateway;
import com.callv2.drive.domain.file.FileID;

public class DefaultRemoveFileUseCase extends RemoveFileUseCase {

    private final FileGateway fileGateway;

    public DefaultRemoveFileUseCase(final FileGateway fileGateway) {
        this.fileGateway = fileGateway;
    }

    @Override
    public void execute(RemoveFileInput input) {
        this.fileGateway
                .findById(FileID.of(input.id()))
                .ifPresent(file -> fileGateway.update(file.delete()));
    }

}
