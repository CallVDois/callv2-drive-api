package com.callv2.drive.infrastructure.api;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.callv2.drive.domain.pagination.Filter;
import com.callv2.drive.domain.pagination.Page;
import com.callv2.drive.domain.pagination.Pagination;
import com.callv2.drive.infrastructure.api.controller.ApiError;
import com.callv2.drive.infrastructure.file.model.CreateFileResponse;
import com.callv2.drive.infrastructure.file.model.FileListResponse;
import com.callv2.drive.infrastructure.file.model.GetFileResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Files")
@RequestMapping("files")
public interface FileAPI {

	@PostMapping(value = "/folders/{folderId}/upload", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	@Operation(summary = "Upload a file to a specific folder", description = "This method uploads a file", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "File uploaded successfully", content = @Content(schema = @Schema(implementation = CreateFileResponse.class))),
			@ApiResponse(responseCode = "404", description = "Folder not found", content = @Content(schema = @Schema(implementation = Void.class))),
			@ApiResponse(responseCode = "413", description = "File is too large", content = @Content(schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ApiError.class)))
	})
	ResponseEntity<CreateFileResponse> create(
			@PathVariable UUID folderId,
			@RequestPart("file") MultipartFile file);

	@GetMapping(value = "{id}", produces = { MediaType.APPLICATION_JSON_VALUE })
	@Operation(summary = "Retrive a file", description = "This method retrive a file", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "File retrived successfully", content = @Content(schema = @Schema(implementation = GetFileResponse.class))),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content(schema = @Schema(implementation = Void.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ApiError.class)))
	})
	ResponseEntity<GetFileResponse> getById(@PathVariable(required = true) UUID id);

	@GetMapping(value = "{id}/download", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE })
	@Operation(summary = "Download a file", description = "This method downloads a file", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "File downloaded successfully", content = @Content(schema = @Schema(implementation = Resource.class))),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content(schema = @Schema(implementation = Void.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
	})
	ResponseEntity<Resource> download(@PathVariable(required = true) UUID id);

	@GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	@Operation(summary = "List files", description = "This method list files", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Files listed successfully", content = @Content(schema = @Schema(implementation = Page.class, subTypes = {
					FileListResponse.class }))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ApiError.class)))
	})
	ResponseEntity<Page<FileListResponse>> list(
			@RequestParam(name = "page", required = false, defaultValue = "0") final int page,
			@RequestParam(name = "perPage", required = false, defaultValue = "10") final int perPage,
			@RequestParam(name = "orderField", required = false, defaultValue = "createdAt") String orderField,
			@RequestParam(name = "orderDirection", required = false, defaultValue = "DESC") Pagination.Order.Direction orderDirection,
			@RequestParam(name = "filterOperator", required = false, defaultValue = "AND") Filter.Operator filterOperator,
			@RequestParam(name = "filters", required = false) List<String> filters);

	@DeleteMapping(value = "{id}")
	@Operation(summary = "Delete file", description = "This method delete file", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Files deleted successfully", content = @Content(schema = @Schema(implementation = Void.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ApiError.class)))
	})
	ResponseEntity<Void> delete(
			@PathVariable(required = true) UUID id,
			@RequestParam(name = "force", required = false, defaultValue = "false") final boolean permanently);

}
