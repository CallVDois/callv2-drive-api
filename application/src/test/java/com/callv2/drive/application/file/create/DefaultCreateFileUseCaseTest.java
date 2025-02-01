package com.callv2.drive.application.file.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.callv2.drive.domain.exception.InternalErrorException;
import com.callv2.drive.domain.exception.NotFoundException;
import com.callv2.drive.domain.exception.ValidationException;
import com.callv2.drive.domain.file.Content;
import com.callv2.drive.domain.file.File;
import com.callv2.drive.domain.file.FileGateway;
import com.callv2.drive.domain.file.FileName;
import com.callv2.drive.domain.folder.Folder;
import com.callv2.drive.domain.folder.FolderGateway;
import com.callv2.drive.domain.member.Member;
import com.callv2.drive.domain.member.MemberGateway;
import com.callv2.drive.domain.member.MemberID;
import com.callv2.drive.domain.member.Quota;
import com.callv2.drive.domain.member.QuotaUnit;
import com.callv2.drive.domain.storage.StorageService;

@ExtendWith(MockitoExtension.class)
public class DefaultCreateFileUseCaseTest {

    @InjectMocks
    DefaultCreateFileUseCase useCase;

    @Mock
    MemberGateway memberGateway;

    @Mock
    FolderGateway folderGateway;

    @Mock
    StorageService storageService;

    @Mock
    FileGateway fileGateway;

    @Test
    void givenAValidParams_whenCallsExecute_thenShouldCreateFile() {

        final var owner = Member.create(MemberID.of("owner"))
                .requestQuota(Quota.of(1, QuotaUnit.GIGABYTE))
                .approveQuotaRequest();

        final var ownerId = owner.getId();

        final var folder = Folder.createRoot();
        final var expectedFolderId = folder.getId();

        final var expectedFileName = FileName.of("file");
        final var expectedContentType = "image/jpeg";
        final var contentBytes = "content".getBytes();

        final var expectedContent = new ByteArrayInputStream(contentBytes);
        final var expectedContentSize = (long) contentBytes.length;

        when(memberGateway.findById(any()))
                .thenReturn(Optional.of(owner));

        when(fileGateway.findByFolder(any()))
                .thenReturn(List.of());

        when(folderGateway.findById(any()))
                .thenReturn(Optional.of(folder));

        when(storageService.store(any(), any()))
                .then(returnsFirstArg());

        when(fileGateway.create(any()))
                .thenAnswer(returnsFirstArg());

        final var input = CreateFileInput.of(
                ownerId.getValue(),
                expectedFolderId.getValue(),
                expectedFileName.value(),
                expectedContentType,
                expectedContent,
                expectedContentSize);

        final var actualOuptut = useCase.execute(input);

        assertNotNull(actualOuptut.id());

        verify(folderGateway, times(1)).findById(any());
        verify(folderGateway, times(1)).findById(eq(expectedFolderId));
        verify(storageService, times(1)).store(any(), any());
        verify(storageService, times(1)).store(any(), eq(expectedContent));
        verify(storageService, times(0)).delete(any());
        verify(fileGateway, times(1)).findByFolder(any());
        verify(fileGateway, times(1)).findByFolder(eq(folder.getId()));
        verify(fileGateway, times(1)).create(any());
        verify(fileGateway, times(1)).create(argThat(file -> {

            assertEquals(actualOuptut.id().getValue(), file.getId().getValue());
            assertEquals(expectedFileName, file.getName());
            assertEquals(expectedContentType, file.getContent().type());
            assertNotNull(file.getCreatedAt());
            assertNotNull(file.getUpdatedAt());
            assertNotNull(file.getContent().location());
            assertEquals(file.getCreatedAt(), file.getUpdatedAt());

            return true;
        }));

    }

    @Test
    void givenAnInvalidId_whenCallsExecute_thenShouldThrowNotFoundException() {

        final var owner = Member.create(MemberID.of("owner"))
                .requestQuota(Quota.of(1, QuotaUnit.GIGABYTE))
                .approveQuotaRequest();

        final var ownerId = owner.getId();

        final var folder = Folder.createRoot();

        final var expectedFolderId = folder.getId();

        final var expectedFileName = FileName.of("file");
        final var expectedContentType = "image/jpeg";
        final var contentBytes = "content".getBytes();

        final var expectedContent = new ByteArrayInputStream(contentBytes);
        final var expectedContentSize = (long) contentBytes.length;

        final var expectedExceptionMessage = "Folder with id '%s' not found"
                .formatted(expectedFolderId.getValue());

        when(memberGateway.findById(any()))
                .thenReturn(Optional.of(owner));

        when(folderGateway.findById(any()))
                .thenReturn(Optional.empty());

        final var input = CreateFileInput.of(
                ownerId.getValue(),
                expectedFolderId.getValue(),
                expectedFileName.value(),
                expectedContentType,
                expectedContent,
                expectedContentSize);

        final var actualException = assertThrows(NotFoundException.class, () -> useCase.execute(input));

        assertEquals(expectedExceptionMessage, actualException.getMessage());

        verify(folderGateway, times(1)).findById(any());
        verify(folderGateway, times(1)).findById(eq(expectedFolderId));
        verify(storageService, times(0)).store(any(), any());
        verify(storageService, times(0)).store(any(), eq(expectedContent));
        verify(storageService, times(0)).delete(any());
        verify(fileGateway, times(0)).findByFolder(any());
        verify(fileGateway, times(0)).create(any());

    }

    @Test
    void givenAValidParamsWithAlreadyExistingFileNameOnSameFolder_whenCallsExecute_thenShouldThrowValidationException() {

        final var owner = Member.create(MemberID.of("owner"))
                .requestQuota(Quota.of(1, QuotaUnit.GIGABYTE))
                .approveQuotaRequest();

        final var ownerId = owner.getId();

        final var folder = Folder.createRoot();

        final var expectedFolderId = folder.getId();

        final var expectedFileName = FileName.of("file");
        final var expectedContentType = "image/jpeg";
        final var contentBytes = "content".getBytes();

        final var expectedContent = new ByteArrayInputStream(contentBytes);
        final var expectedContentSize = (long) contentBytes.length;

        final var fileWithSameName = File.create(ownerId, folder.getId(), expectedFileName,
                Content.of("location", "text", 10));

        final var expectedExceptionMessage = "Could not create Aggregate File";
        final var expectedErrorMessage = "File with same name already exists on this folder";

        when(memberGateway.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(fileGateway.findByFolder(any()))
                .thenReturn(List.of(fileWithSameName));

        when(folderGateway.findById(any()))
                .thenReturn(Optional.of(folder));

        when(storageService.store(any(), any()))
                .then(returnsFirstArg());

        final var input = CreateFileInput.of(
                ownerId.getValue(),
                expectedFolderId.getValue(),
                expectedFileName.value(),
                expectedContentType,
                expectedContent,
                expectedContentSize);

        final var actualException = assertThrows(ValidationException.class, () -> useCase.execute(input));

        assertEquals(expectedExceptionMessage, actualException.getMessage());
        assertEquals(expectedErrorMessage, actualException.getErrors().get(0).message());

        verify(folderGateway, times(1)).findById(any());
        verify(folderGateway, times(1)).findById(eq(expectedFolderId));
        verify(storageService, times(1)).store(any(), any());
        verify(storageService, times(1)).store(any(), eq(expectedContent));
        verify(storageService, times(0)).delete(any());
        verify(fileGateway, times(1)).findByFolder(any());
        verify(fileGateway, times(1)).findByFolder(eq(folder.getId()));
        verify(fileGateway, times(0)).create(any());

    }

    @Test
    void givenAValidParams_whenCallsExecuteAndFileGatewayCreateThrowsRandomException_thenShouldThrowInternalErrorException() {

        final var owner = Member.create(MemberID.of("owner"))
                .requestQuota(Quota.of(1, QuotaUnit.GIGABYTE))
                .approveQuotaRequest();

        final var ownerId = owner.getId();

        final var folder = Folder.createRoot();
        final var expectedFolderId = folder.getId();

        final var expectedFileName = FileName.of("file");
        final var expectedContentType = "image/jpeg";
        final var contentBytes = "content".getBytes();

        final var expectedContent = new ByteArrayInputStream(contentBytes);
        final var expectedContentSize = (long) contentBytes.length;

        final var expectedExceptionMessage = "Could not store File";

        when(memberGateway.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(fileGateway.findByFolder(any()))
                .thenReturn(List.of());

        when(folderGateway.findById(any()))
                .thenReturn(Optional.of(folder));

        when(storageService.store(any(), any()))
                .then(returnsFirstArg());

        when(fileGateway.create(any()))
                .thenThrow(new IllegalStateException("FileGateway Exception"));

        doNothing()
                .when(storageService)
                .delete(any());

        final var input = CreateFileInput.of(
                ownerId.getValue(),
                expectedFolderId.getValue(),
                expectedFileName.value(),
                expectedContentType,
                expectedContent,
                expectedContentSize);

        final var actualException = assertThrows(InternalErrorException.class, () -> useCase.execute(input));

        assertEquals(expectedExceptionMessage, actualException.getMessage());
        assertEquals("FileGateway Exception", actualException.getCause().getMessage());

        verify(folderGateway, times(1)).findById(any());
        verify(folderGateway, times(1)).findById(eq(expectedFolderId));
        verify(storageService, times(1)).store(any(), any());
        verify(storageService, times(1)).store(any(), eq(expectedContent));
        verify(storageService, times(1)).delete(any());
        verify(fileGateway, times(1)).findByFolder(any());
        verify(fileGateway, times(1)).findByFolder(eq(folder.getId()));
        verify(fileGateway, times(1)).create(any());
        verify(fileGateway, times(1)).create(argThat(file -> {

            assertNotNull(file.getId().getValue());
            assertEquals(expectedFileName, file.getName());
            assertEquals(expectedContentType, file.getContent().type());
            assertNotNull(file.getCreatedAt());
            assertNotNull(file.getUpdatedAt());
            assertNotNull(file.getContent().location());
            assertEquals(file.getCreatedAt(), file.getUpdatedAt());

            return true;
        }));

    }

    @Test
    void givenAValidParams_whenCallsExecuteAndFileGatewayCreateAndContentGatewayDeleteThrowsRandomException_thenShouldThrowInternalErrorException() {

        final var owner = Member.create(MemberID.of("owner"))
                .requestQuota(Quota.of(1, QuotaUnit.GIGABYTE))
                .approveQuotaRequest();

        final var ownerId = owner.getId();

        final var folder = Folder.createRoot();
        final var expectedFolderId = folder.getId();

        final var expectedFileName = FileName.of("file");
        final var expectedContentType = "image/jpeg";
        final var contentBytes = "content".getBytes();

        final var expectedContent = new ByteArrayInputStream(contentBytes);
        final var expectedContentSize = (long) contentBytes.length;

        final var expectedExceptionMessage = "Could not delete BinaryContent";

        when(memberGateway.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(fileGateway.findByFolder(any()))
                .thenReturn(List.of());

        when(folderGateway.findById(any()))
                .thenReturn(Optional.of(folder));

        when(storageService.store(any(), any()))
                .then(returnsFirstArg());

        when(fileGateway.create(any()))
                .thenThrow(new IllegalStateException("FileGateway Exception"));

        doThrow(new IllegalStateException("ContentGateway Exception"))
                .when(storageService)
                .delete(any());

        final var input = CreateFileInput.of(
                ownerId.getValue(),
                expectedFolderId.getValue(),
                expectedFileName.value(),
                expectedContentType,
                expectedContent,
                expectedContentSize);

        final var actualException = assertThrows(InternalErrorException.class, () -> useCase.execute(input));

        assertEquals(expectedExceptionMessage, actualException.getMessage());
        assertEquals("ContentGateway Exception", actualException.getCause().getMessage());

        verify(folderGateway, times(1)).findById(any());
        verify(folderGateway, times(1)).findById(eq(expectedFolderId));
        verify(storageService, times(1)).store(any(), any());
        verify(storageService, times(1)).store(any(), eq(expectedContent));
        verify(storageService, times(1)).delete(any());
        verify(fileGateway, times(1)).findByFolder(any());
        verify(fileGateway, times(1)).findByFolder(eq(folder.getId()));
        verify(fileGateway, times(1)).create(any());
        verify(fileGateway, times(1)).create(argThat(file -> {

            assertNotNull(file.getId().getValue());
            assertEquals(expectedFileName, file.getName());
            assertEquals(expectedContentType, file.getContent().type());
            assertNotNull(file.getCreatedAt());
            assertNotNull(file.getUpdatedAt());
            assertNotNull(file.getContent().location());
            assertEquals(file.getCreatedAt(), file.getUpdatedAt());

            return true;
        }));

    }

    @Test
    void givenAValidParams_whenCallsExecuteAndContentGatewayStoreThrowsRandomException_thenShouldThrowInternalErrorException() {

        final var owner = Member.create(MemberID.of("owner"))
                .requestQuota(Quota.of(1, QuotaUnit.GIGABYTE))
                .approveQuotaRequest();

        final var ownerId = owner.getId();

        final var folder = Folder.createRoot();
        final var expectedFolderId = folder.getId();

        final var expectedFileName = FileName.of("file");
        final var expectedContentType = "image/jpeg";
        final var contentBytes = "content".getBytes();

        final var expectedContent = new ByteArrayInputStream(contentBytes);
        final var expectedContentSize = (long) contentBytes.length;

        final var expectedExceptionMessage = "Could not store BinaryContent";

        when(memberGateway.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(folderGateway.findById(any()))
                .thenReturn(Optional.of(folder));

        when(storageService.store(any(), any()))
                .thenThrow(new IllegalStateException("ContentGateway Exception"));

        final var input = CreateFileInput.of(
                ownerId.getValue(),
                expectedFolderId.getValue(),
                expectedFileName.value(),
                expectedContentType,
                expectedContent,
                expectedContentSize);

        final var actualException = assertThrows(InternalErrorException.class, () -> useCase.execute(input));

        assertEquals(expectedExceptionMessage, actualException.getMessage());
        assertEquals("ContentGateway Exception", actualException.getCause().getMessage());

        verify(folderGateway, times(1)).findById(any());
        verify(folderGateway, times(1)).findById(eq(expectedFolderId));
        verify(storageService, times(1)).store(any(), any());
        verify(storageService, times(1)).store(any(), eq(expectedContent));
        verify(storageService, times(0)).delete(any());
        verify(fileGateway, times(0)).findByFolder(any());
        verify(fileGateway, times(0)).create(any());

    }

}
