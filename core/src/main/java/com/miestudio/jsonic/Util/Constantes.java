package com.miestudio.jsonic.Util;

/**
* Aqui declaramos constantes utilizadas en el proyecto para tenerlas todas centralizadas
* */
public class Constantes {

    // Constantes de red
    /** Puerto de comunicacion */
    public static final int DISCOVERY_PORT = 8888;
    public static final int GAME_PORT = 7777;
    
    /** Numerno maximo de jugadores */
    public static final int MAX_PLAYERS = 3;

    // Pixels Per Meter para Box2D
    public static final float PPM = 32f;

    // Bitmasks de colisión para Box2D
    public static final short CATEGORY_PLAYER = 0x0001;  // 0001 en binario
    public static final short CATEGORY_GROUND = 0x0002;  // 0010 en binario
    public static final short CATEGORY_ENEMY = 0x0004;   // 0100 en binario
    public static final short CATEGORY_ITEM = 0x0008;    // 1000 en binario

}
