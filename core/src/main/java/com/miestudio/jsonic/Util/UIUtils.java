package com.miestudio.jsonic.Util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Clase de utilidad para la creación de elementos de interfaz de usuario (UI) comunes en LibGDX.
 */
public class UIUtils {

    /**
     * Crea un objeto {@link Drawable} de un solo color. Es útil para establecer fondos de botones
     * u otros elementos de UI que requieran un color sólido.
     *
     * @param color El {@link Color} deseado para el Drawable.
     * @return Un {@link Drawable} que puede ser usado en estilos de Scene2D.
     */
    public static Drawable createColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Drawable drawable = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();
        return drawable;
    }
}