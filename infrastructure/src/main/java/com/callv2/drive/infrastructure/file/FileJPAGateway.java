package com.callv2.drive.infrastructure.file;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.callv2.drive.domain.file.File;
import com.callv2.drive.domain.file.FileGateway;
import com.callv2.drive.domain.file.FileID;
import com.callv2.drive.domain.folder.FolderID;
import com.callv2.drive.domain.pagination.Pagination;
import com.callv2.drive.domain.pagination.SearchQuery;
import com.callv2.drive.infrastructure.file.persistence.FileJpaEntity;
import com.callv2.drive.infrastructure.file.persistence.FileJpaRepository;
import com.callv2.drive.infrastructure.filter.FilterService;
import com.callv2.drive.infrastructure.filter.adapter.QueryAdapter;

@Component
public class FileJPAGateway implements FileGateway {

    private final FilterService filterService;
    private final FileJpaRepository fileRepository;

    public FileJPAGateway(
            final FileJpaRepository fileRepository,
            final FilterService specificationFilterService) {
        this.fileRepository = fileRepository;
        this.filterService = specificationFilterService;
    }

    @Override
    public File create(File file) {
        return fileRepository.save(FileJpaEntity.from(file)).toDomain();
    }

    @Override
    public Optional<File> findById(FileID id) {
        return fileRepository.findById(id.getValue()).map(FileJpaEntity::toDomain);
    }

    @Override
    public List<File> findByFolder(FolderID folderId) {
        return this.fileRepository
                .findByFolderId(folderId.getValue())
                .stream()
                .map(FileJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Pagination<File> findAll(final SearchQuery searchQuery) {

        final var page = QueryAdapter.of(searchQuery);

        final Specification<FileJpaEntity> specification = filterService.buildSpecification(
                FileJpaEntity.class,
                searchQuery.filterMethod(),
                searchQuery.filters());

        final Page<FileJpaEntity> pageResult = this.fileRepository.findAll(Specification.where(specification),
                page);

        return new Pagination<>(
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements(),
                pageResult.map(FileJpaEntity::toDomain).toList());

    }

}
