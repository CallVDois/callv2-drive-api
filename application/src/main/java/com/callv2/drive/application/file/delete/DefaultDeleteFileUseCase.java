package com.callv2.drive.application.file.delete;

import com.callv2.drive.domain.file.FileGateway;
import com.callv2.drive.domain.file.FileID;
import com.callv2.drive.domain.storage.StorageService;

public class DefaultDeleteFileUseCase extends DeleteFileUseCase {

    private final FileGateway fileGateway;
    private final StorageService storageService;

    public DefaultDeleteFileUseCase(
            final FileGateway fileGateway,
            final StorageService storageService) {
        this.fileGateway = fileGateway;
        this.storageService = storageService;
    }

    @Override
    public void execute(final DeleteFileInput input) {
        this.fileGateway
                .findById(FileID.of(input.id()))
                .ifPresent(file -> {
                    this.storageService.delete(file.getContent().location());
                    this.fileGateway.delete(file.getId());
                });
    }

}
