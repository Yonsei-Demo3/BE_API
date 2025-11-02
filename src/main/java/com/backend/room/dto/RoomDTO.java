package com.backend.room.dto;

import com.backend.room.domain.Room;

public record RoomDTO(Long id,
                      String name,
                      String description) {
    public static RoomDTO from(Room room) {
        return new RoomDTO(room.getId(), room.getName(), room.getDescription());
    }
}
