//--------------version 0.03--------------------
//data: Diumenge, 11 de Maig de 2025
//---(c)----Jordi Sala---------------------------
package com.example.msxtileeditor
import androidx.compose.ui.graphics.Color

/**
 * Objecte que conté la paleta de 16 colors estàndard de l'MSX.
 */
object MSXColorPalette {
    // Definim els colors de la paleta MSX.
    // L'índex 0 és 'Transparent'. En Compose, Color.Transparent farà que es vegi el fons.
    val colors: List<Color> = listOf(
        Color.Transparent,                  // 0: Transparent
        Color(0, 0, 0),                     // 1: Negre
        Color(32, 192, 32),                 // 2: Verd Mig
        Color(96, 224, 96),                 // 3: Verd Clar
        Color(32, 32, 224),                 // 4: Blau Fosc
        Color(64, 96, 224),                 // 5: Blau Clar
        Color(160, 32, 32),                 // 6: Vermell Fosc
        Color(64, 192, 224),                // 7: Cian
        Color(224, 32, 32),                 // 8: Vermell Mig
        Color(255, 96, 96),                 // 9: Vermell Clar
        Color(192, 192, 32),                // 10: Groc Fosc
        Color(224, 224, 96),                // 11: Groc Clar
        Color(32, 128, 32),                 // 12: Verd Fosc
        Color(192, 64, 192),                // 13: Magenta
        Color(192, 192, 192),               // 14: Gris
        Color(255, 255, 255)                // 15: Blanc
    )

    // Color per defecte per als píxels nous o buits (per exemple, transparent o negre)
    // Fem servir l'índex 1 (Negre) com a color inicial per defecte per als píxels nous,
    // ja que transparent pot ser menys visible inicialment.
    const val DEFAULT_PIXEL_COLOR_INDEX = 1
}