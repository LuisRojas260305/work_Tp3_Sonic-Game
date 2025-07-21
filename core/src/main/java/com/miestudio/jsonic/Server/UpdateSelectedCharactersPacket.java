package com.miestudio.jsonic.Server;

import java.io.Serializable;
import java.util.List;

public class UpdateSelectedCharactersPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<String> selectedCharacters;

    public UpdateSelectedCharactersPacket(List<String> selectedCharacters) {
        this.selectedCharacters = selectedCharacters;
    }
}
