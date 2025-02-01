package com.callv2.drive.application.file.delete;

import java.util.UUID;

public record DeleteFileInput(UUID id) {

    public static DeleteFileInput of(final UUID id) {
        return new DeleteFileInput(id);
    }

}
