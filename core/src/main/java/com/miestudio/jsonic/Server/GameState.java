package com.miestudio.jsonic.Server;

import java.io.Serializable;
import java.util.ArrayList;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<PlayerState> players;

    public GameState(ArrayList<PlayerState> players) {
        this.players = players;
    }

    public ArrayList<PlayerState> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<PlayerState> players) {
        this.players = players;
    }
}
