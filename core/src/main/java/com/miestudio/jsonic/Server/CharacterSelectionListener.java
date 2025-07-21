package com.miestudio.jsonic.Server;

import java.util.List;

@FunctionalInterface
public interface CharacterSelectionListener {
    void onCharacterSelectionChanged(List<String> selectedCharacters);
}
