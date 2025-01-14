package com.callv2.drive.infrastructure.configuration.usecase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.callv2.drive.application.file.content.get.DefaultGetFileContentUseCase;
import com.callv2.drive.application.file.content.get.GetFileContentUseCase;
import com.callv2.drive.application.file.create.CreateFileUseCase;
import com.callv2.drive.application.file.create.DefaultCreateFileUseCase;
import com.callv2.drive.application.file.retrieve.get.DefaultGetFileUseCase;
import com.callv2.drive.application.file.retrieve.get.GetFileUseCase;
import com.callv2.drive.domain.file.FileContentGateway;
import com.callv2.drive.domain.file.FileGateway;
import com.callv2.drive.domain.folder.FolderGateway;

@Configuration
public class FileUseCaseConfig {

    private final FolderGateway folderGateway;
    private final FileGateway fileGateway;
    private final FileContentGateway contentGateway;

    public FileUseCaseConfig(
            final FolderGateway folderGateway,
            final FileGateway fileGateway,
            final FileContentGateway contentGateway) {
        this.folderGateway = folderGateway;
        this.fileGateway = fileGateway;
        this.contentGateway = contentGateway;
    }

    @Bean
    CreateFileUseCase createFileUseCase() {
        return new DefaultCreateFileUseCase(folderGateway, fileGateway, contentGateway);
    }

    @Bean
    GetFileUseCase getFileUseCase() {
        return new DefaultGetFileUseCase(fileGateway);
    }

    @Bean
    GetFileContentUseCase getFileContentUseCase() {
        return new DefaultGetFileContentUseCase(fileGateway);
    }

}
