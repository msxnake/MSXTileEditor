//--------------version 0.01--------------------
//data: Diumenge, 11 de Maig de 2025
//---(c)----Jordi Sala---------------------------
package com.example.msxtileeditor // Assegura't que aquest és el teu paquet

// Mida estàndard d'un tile en píxels (MSX sol ser 8x8)
const val TILE_SIZE = 8

/**
 * Representa les dades d'un tile.
 * Un tile és una matriu de Booleans, on 'true' pot significar píxel actiu (ences)
 * i 'false' píxel inactiu (apagat).
 *
 * @property id Un identificador únic per al tile.
 * @property pixels Una llista mutable de llistes mutables de Booleans que representen la graella de píxels.
 * pixels[fila][columna]
 */
data class TileData(
    val id: String = java.util.UUID.randomUUID().toString(), // Genera un ID únic per defecte
    val pixels: List<MutableList<Boolean>> = List(TILE_SIZE) { MutableList(TILE_SIZE) { false } }
) {
    // Funció per crear una còpia profunda (deep copy) del tile.
    // Això és important per a la gestió de l'estat en Compose,
    // per assegurar que les modificacions creen un nou objecte i es detecten els canvis.
    fun deepCopy(): TileData {
        val newPixels = pixels.map { row ->
            row.toMutableList() // Crea una nova llista mutable per a cada fila
        }.toMutableList() // Crea una nova llista mutable de files
        return TileData(id, newPixels)
    }
}