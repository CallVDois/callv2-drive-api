package com.callv2.drive.infrastructure.configuration.usecase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.callv2.drive.application.folder.create.CreateFolderUseCase;
import com.callv2.drive.application.folder.create.DefaultCreateFolderUseCase;
import com.callv2.drive.application.folder.move.DefaultMoveFolderUseCase;
import com.callv2.drive.application.folder.move.MoveFolderUseCase;
import com.callv2.drive.application.folder.retrieve.get.DefaultGetFolderUseCase;
import com.callv2.drive.application.folder.retrieve.get.GetFolderUseCase;
import com.callv2.drive.application.folder.retrieve.get.root.DefaultGetRootFolderUseCase;
import com.callv2.drive.application.folder.retrieve.get.root.GetRootFolderUseCase;
import com.callv2.drive.application.folder.retrieve.list.DefaultListFoldersUseCase;
import com.callv2.drive.application.folder.retrieve.list.ListFoldersUseCase;
import com.callv2.drive.domain.file.FileGateway;
import com.callv2.drive.domain.folder.FolderGateway;

@Configuration
public class FolderUseCaseConfig {

    private final FolderGateway folderGateway;
    private final FileGateway fileGateway;

    public FolderUseCaseConfig(
            final FolderGateway folderGateway,
            final FileGateway fileGateway) {
        this.folderGateway = folderGateway;
        this.fileGateway = fileGateway;
    }

    @Bean
    GetRootFolderUseCase getRootFolderUseCase() {
        return new DefaultGetRootFolderUseCase(folderGateway, fileGateway);
    }

    @Bean
    CreateFolderUseCase createFolderUseCase() {
        return new DefaultCreateFolderUseCase(folderGateway);
    }

    @Bean
    GetFolderUseCase getFolderUseCase() {
        return new DefaultGetFolderUseCase(folderGateway, fileGateway);
    }

    @Bean
    MoveFolderUseCase moveFolderUseCase() {
        return new DefaultMoveFolderUseCase(folderGateway);
    }

    @Bean
    ListFoldersUseCase listFoldersUseCase() {
        return new DefaultListFoldersUseCase(folderGateway);
    }

}
