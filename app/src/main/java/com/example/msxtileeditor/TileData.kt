//--------------version 0.04--------------------
//data: Diumenge, 11 de Maig de 2025
//---(c)----Jordi Sala---------------------------
package com.example.msxtileeditor

import androidx.compose.ui.graphics.Color // Assegura't que Color està importat

// Mida estàndard d'un tile en píxels (MSX sol ser 8x8)
const val TILE_SIZE = 8

/**
 * Representa les dades d'un tile.
 * @property id Un identificador únic per al tile.
 * @property pixels Matriu de píxels, cada Int és un índex de la MSXColorPalette.
 * @property designatedRowColors Llista de parells d'índexs de color (FG, BG) designats per a cada fila.
 */
data class TileData(
    val id: String = java.util.UUID.randomUUID().toString(),
    val pixels: List<MutableList<Int>> = List(TILE_SIZE) {
        MutableList(TILE_SIZE) { MSXColorPalette.DEFAULT_PIXEL_COLOR_INDEX }
    },
    // Per a cada fila (8 files), un parell d'índexs de color (Foreground, Background)
    // Índex 1: Negre, Índex 15: Blanc, segons MSXColorPalette
    val designatedRowColors: MutableList<Pair<Int, Int>> = MutableList(TILE_SIZE) {
        Pair(1, 15)
    }
) {
    fun deepCopy(): TileData {
        val newPixels = pixels.map { row -> row.toMutableList() }.toMutableList()
        // Per a Pair, la còpia per defecte és superficial, però com Int és primitiu, està bé.
        // Si fossin objectes mutables dins del Pair, necessitaríem una còpia més profunda.
        val newDesignatedRowColors = designatedRowColors.toMutableList() // Crea una nova llista mutable
        return TileData(id, newPixels, newDesignatedRowColors)
    }
}