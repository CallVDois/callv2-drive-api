package com.callv2.drive.infrastructure.api.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.callv2.drive.application.file.content.get.GetFileContentInput;
import com.callv2.drive.application.file.content.get.GetFileContentOutput;
import com.callv2.drive.application.file.content.get.GetFileContentUseCase;
import com.callv2.drive.application.file.create.CreateFileUseCase;
import com.callv2.drive.application.file.delete.DeleteFileInput;
import com.callv2.drive.application.file.delete.DeleteFileUseCase;
import com.callv2.drive.application.file.remove.RemoveFileInput;
import com.callv2.drive.application.file.remove.RemoveFileUseCase;
import com.callv2.drive.application.file.retrieve.get.GetFileInput;
import com.callv2.drive.application.file.retrieve.get.GetFileUseCase;
import com.callv2.drive.application.file.retrieve.list.ListFilesUseCase;
import com.callv2.drive.domain.pagination.Filter;
import com.callv2.drive.domain.pagination.Page;
import com.callv2.drive.domain.pagination.Pagination;
import com.callv2.drive.domain.pagination.SearchQuery;
import com.callv2.drive.infrastructure.api.FileAPI;
import com.callv2.drive.infrastructure.file.adapter.FileAdapter;
import com.callv2.drive.infrastructure.file.model.CreateFileResponse;
import com.callv2.drive.infrastructure.file.model.FileListResponse;
import com.callv2.drive.infrastructure.file.model.GetFileResponse;
import com.callv2.drive.infrastructure.file.presenter.FilePresenter;
import com.callv2.drive.infrastructure.filter.adapter.QueryAdapter;
import com.callv2.drive.infrastructure.security.SecurityContext;

@RestController
public class FileController implements FileAPI {

    private final CreateFileUseCase createFileUseCase;
    private final GetFileUseCase getFileUseCase;
    private final GetFileContentUseCase getFileContentUseCase;
    private final ListFilesUseCase listFilesUseCase;
    private final DeleteFileUseCase deleteFileUseCase;
    private final RemoveFileUseCase removeFileUseCase;

    public FileController(
            final CreateFileUseCase createFileUseCase,
            final GetFileUseCase getFileUseCase,
            final GetFileContentUseCase getFileContentUseCase,
            final ListFilesUseCase listFilesUseCase,
            final DeleteFileUseCase deleteFileUseCase,
            final RemoveFileUseCase removeFileUseCase) {
        this.createFileUseCase = createFileUseCase;
        this.getFileUseCase = getFileUseCase;
        this.getFileContentUseCase = getFileContentUseCase;
        this.listFilesUseCase = listFilesUseCase;
        this.deleteFileUseCase = deleteFileUseCase;
        this.removeFileUseCase = removeFileUseCase;
    }

    @Override
    public ResponseEntity<CreateFileResponse> create(UUID folderId, MultipartFile file) {

        final var ownerId = SecurityContext.getAuthenticatedUser();

        final var response = FilePresenter
                .present(createFileUseCase.execute(FileAdapter.adapt(ownerId, folderId, file)));

        return ResponseEntity
                .created(URI.create("/files/" + response.id()))
                .body(response);
    }

    @Override
    public ResponseEntity<GetFileResponse> getById(UUID id) {
        return ResponseEntity.ok(FilePresenter.present(getFileUseCase.execute(GetFileInput.from(id))));
    }

    @Override
    @Async
    public ResponseEntity<Resource> download(UUID id) {

        final GetFileContentOutput output = getFileContentUseCase.execute(GetFileContentInput.with(id));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + output.name() + "\"")
                .contentLength(output.size())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(output.location()));
    }

    @Override
    public ResponseEntity<Page<FileListResponse>> list(
            final int page,
            final int perPage,
            final String orderField,
            final Pagination.Order.Direction orderDirection,
            final Filter.Operator filterOperator,
            final List<String> filters) {

        final List<Filter> searchFilters = filters == null ? List.of()
                : filters
                        .stream()
                        .map(QueryAdapter::of)
                        .toList();

        final SearchQuery query = SearchQuery.of(
                Pagination.of(page, perPage, Pagination.Order.of(orderField, orderDirection)),
                filterOperator,
                searchFilters);

        return ResponseEntity.ok(listFilesUseCase.execute(query).map(FilePresenter::present));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id, boolean permanently) {

        if (permanently)
            deleteFileUseCase.execute(DeleteFileInput.of(id));
        else
            removeFileUseCase.execute(RemoveFileInput.of(id));

        return ResponseEntity.noContent().build();
    }

}
