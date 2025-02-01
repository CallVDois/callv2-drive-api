package com.callv2.drive.domain.file;

import java.util.List;
import java.util.Optional;

import com.callv2.drive.domain.folder.FolderID;
import com.callv2.drive.domain.member.MemberID;
import com.callv2.drive.domain.pagination.Page;
import com.callv2.drive.domain.pagination.SearchQuery;

public interface FileGateway {

    File create(File file);

    void delete(FileID id);

    File update(File file);

    Optional<File> findById(FileID id);

    List<File> findByFolder(FolderID folderId);

    List<File> findByOwner(MemberID ownerId);

    Page<File> findAll(SearchQuery searchQuery);

}
