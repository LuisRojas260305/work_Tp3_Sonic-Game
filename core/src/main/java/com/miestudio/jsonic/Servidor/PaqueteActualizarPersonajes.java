package com.miestudio.jsonic.Servidor;

import java.io.Serializable;
import java.util.List;

public class PaqueteActualizarPersonajes implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<String> personajesSeleccionados;

    public PaqueteActualizarPersonajes(List<String> personajesSeleccionados) {
        this.personajesSeleccionados = personajesSeleccionados;
    }
}
