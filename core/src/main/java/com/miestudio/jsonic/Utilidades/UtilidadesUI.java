package com.miestudio.jsonic.Utilidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Clase de utilidad para la creación de elementos de interfaz de usuario (UI) comunes en LibGDX.
 */
public class UtilidadesUI {

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

    public static TextButton.TextButtonStyle createDefaultButtonStyle() {
        return createDefaultButtonStyle(Color.BLUE);
    }

    public static TextButton.TextButtonStyle createDefaultButtonStyle(Color color) {
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = new BitmapFont();
        buttonStyle.up = createColorDrawable(color);
        buttonStyle.down = createColorDrawable(color.cpy().mul(0.8f));
        buttonStyle.disabled = createColorDrawable(Color.GRAY);
        buttonStyle.fontColor = Color.WHITE;
        return buttonStyle;
    }
}