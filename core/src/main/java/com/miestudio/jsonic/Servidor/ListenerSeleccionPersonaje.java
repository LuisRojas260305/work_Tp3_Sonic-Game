package com.miestudio.jsonic.Servidor;

import java.util.List;

@FunctionalInterface
public interface ListenerSeleccionPersonaje {
    void onSeleccionPersonajeCambiada(List<String> personajesSeleccionados);
}
