package com.callv2.drive.application.file.remove;

import java.util.UUID;

public record RemoveFileInput(UUID id) {

    public static RemoveFileInput of(UUID id) {
        return new RemoveFileInput(id);
    }

}
