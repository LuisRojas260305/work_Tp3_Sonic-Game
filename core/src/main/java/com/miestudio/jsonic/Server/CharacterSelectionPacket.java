package com.miestudio.jsonic.Server;

import java.io.Serializable;

public class CharacterSelectionPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    public String characterName;

    public CharacterSelectionPacket(String characterName) {
        this.characterName = characterName;
    }
}
