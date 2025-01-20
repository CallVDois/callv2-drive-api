package com.callv2.drive.infrastructure.api.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.callv2.drive.application.folder.create.CreateFolderUseCase;
import com.callv2.drive.application.folder.move.MoveFolderInput;
import com.callv2.drive.application.folder.move.MoveFolderUseCase;
import com.callv2.drive.application.folder.retrieve.get.GetFolderUseCase;
import com.callv2.drive.application.folder.retrieve.get.root.GetRootFolderUseCase;
import com.callv2.drive.application.folder.retrieve.list.ListFoldersUseCase;
import com.callv2.drive.domain.pagination.Pagination;
import com.callv2.drive.domain.pagination.SearchQuery;
import com.callv2.drive.domain.pagination.SearchQuery.FilterMethod;
import com.callv2.drive.domain.pagination.SearchQuery.Order.Direction;
import com.callv2.drive.infrastructure.api.FolderAPI;
import com.callv2.drive.infrastructure.filter.adapter.QueryAdapter;
import com.callv2.drive.infrastructure.folder.adapter.FolderAdapter;
import com.callv2.drive.infrastructure.folder.model.CreateFolderRequest;
import com.callv2.drive.infrastructure.folder.model.CreateFolderResponse;
import com.callv2.drive.infrastructure.folder.model.FolderListResponse;
import com.callv2.drive.infrastructure.folder.model.GetFolderResponse;
import com.callv2.drive.infrastructure.folder.model.GetRootFolderResponse;
import com.callv2.drive.infrastructure.folder.model.MoveFolderRequest;
import com.callv2.drive.infrastructure.folder.presenter.FolderPresenter;

@RestController
public class FolderController implements FolderAPI {

    private final GetRootFolderUseCase getRootFolderUseCase;
    private final CreateFolderUseCase createFolderUseCase;
    private final GetFolderUseCase getFolderUseCase;
    private final MoveFolderUseCase moveFolderUseCase;
    private final ListFoldersUseCase listFoldersUseCase;

    public FolderController(
            final GetRootFolderUseCase getRootFolderUseCase,
            final CreateFolderUseCase createFolderUseCase,
            final GetFolderUseCase getFolderUseCase,
            final MoveFolderUseCase moveFolderUseCase,
            final ListFoldersUseCase listFoldersUseCase) {
        this.getRootFolderUseCase = getRootFolderUseCase;
        this.createFolderUseCase = createFolderUseCase;
        this.getFolderUseCase = getFolderUseCase;
        this.moveFolderUseCase = moveFolderUseCase;
        this.listFoldersUseCase = listFoldersUseCase;
    }

    @Override
    public ResponseEntity<GetRootFolderResponse> getRoot() {
        return ResponseEntity.ok(FolderPresenter.present(getRootFolderUseCase.execute()));
    }

    @Override
    public ResponseEntity<CreateFolderResponse> create(final CreateFolderRequest request) {
        final var response = FolderPresenter.present(createFolderUseCase.execute(FolderAdapter.adapt(request)));

        return ResponseEntity
                .created(URI.create("/folders/" + response.id()))
                .body(response);
    }

    @Override
    public ResponseEntity<GetFolderResponse> getById(final UUID id) {
        return ResponseEntity
                .ok(FolderPresenter.present(getFolderUseCase.execute(FolderAdapter.adapt(id))));
    }

    @Override
    public ResponseEntity<Void> move(final UUID id, final MoveFolderRequest request) {
        moveFolderUseCase.execute(MoveFolderInput.with(id, request.newParentId()));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Pagination<FolderListResponse>> list(
            final int page,
            final int perPage,
            final String orderField,
            final Direction orderDirection,
            final FilterMethod filterMethod,
            final List<String> filters) {

        final List<SearchQuery.Filter> searchFilters = filters == null ? List.of()
                : filters
                        .stream()
                        .map(QueryAdapter::of)
                        .toList();

        final SearchQuery query = SearchQuery.of(
                page,
                perPage,
                SearchQuery.Order.of(orderField, orderDirection),
                filterMethod,
                searchFilters);

        return ResponseEntity.ok(listFoldersUseCase.execute(query).map(FolderPresenter::present));
    }

}
