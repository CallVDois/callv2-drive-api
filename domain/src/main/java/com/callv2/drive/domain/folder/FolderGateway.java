package com.callv2.drive.domain.folder;

import java.util.List;
import java.util.Optional;

import com.callv2.drive.domain.pagination.Pagination;
import com.callv2.drive.domain.pagination.SearchQuery;

public interface FolderGateway {

    Optional<Folder> findRoot();

    Folder create(Folder folder);

    Folder update(Folder folder);

    void updateAll(List<Folder> folders);

    Optional<Folder> findById(FolderID id);

    Pagination<Folder> findAll(final SearchQuery searchQuery);

}
