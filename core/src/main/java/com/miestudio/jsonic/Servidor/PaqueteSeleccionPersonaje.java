package com.miestudio.jsonic.Servidor;

import java.io.Serializable;

public class PaqueteSeleccionPersonaje implements Serializable {
    private static final long serialVersionUID = 1L;
    public String nombrePersonaje;

    public PaqueteSeleccionPersonaje(String nombrePersonaje) {
        this.nombrePersonaje = nombrePersonaje;
    }
}
