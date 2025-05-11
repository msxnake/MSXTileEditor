//--------------version 0.01--------------------
//data: Diumenge, 11 de Maig de 2025
//---(c)----Jordi Sala---------------------------
package com.example.msxtileeditor // Assegura't que aquest és el teu paquet

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

/**
 * ViewModel per a l'editor de Tiles.
 * S'encarrega de mantenir l'estat de la llista de tiles, el tile seleccionat
 * i la lògica per modificar els tiles.
 */
class TileEditorViewModel : ViewModel() {

    // Llista observable de tots els tiles disponibles a l'editor.
    // Fem servir mutableStateListOf per a què Compose reaccioni als canvis (afegir/eliminar tiles).
    val tiles = mutableStateListOf<TileData>()

    // Índex del tile actualment seleccionat per a l'edició.
    // mutableStateOf fa que Compose reaccioni als canvis d'aquest valor.
    private val _selectedTileIndex = mutableStateOf(0)
    val selectedTileIndex: Int
        get() = _selectedTileIndex.value

    // El TileData actualment seleccionat.
    // Si no hi ha tiles o l'índex no és vàlid, retorna null.
    val selectedTile: TileData?
        get() = tiles.getOrNull(_selectedTileIndex.value)

    init {
        // Inicialitzem amb un tile buit per començar.
        if (tiles.isEmpty()) {
            tiles.add(TileData())
        }
    }

    /**
     * Canvia el tile seleccionat per a l'edició.
     * @param index L'índex del nou tile a seleccionar dins de la llista 'tiles'.
     */
    fun selectTile(index: Int) {
        if (index >= 0 && index < tiles.size) {
            _selectedTileIndex.value = index
        }
    }

    /**
     * Modifica l'estat d'un píxel (l'activa o desactiva) en el tile actualment seleccionat.
     * @param row La fila del píxel a modificar (0 a TILE_SIZE-1).
     * @param col La columna del píxel a modificar (0 a TILE_SIZE-1).
     */
    fun togglePixel(row: Int, col: Int) {
        selectedTile?.let { currentTile ->
            // Per tal que Compose detecti el canvi dins de la llista de píxels d'un TileData,
            // necessitem crear una nova instància del TileData modificat o modificar-lo
            // d'una manera que SnapshotStateList pugui detectar.
            // Aquí, creem una còpia, la modifiquem, i la reassignem.
            val newTile = currentTile.deepCopy()
            if (row in 0 until TILE_SIZE && col in 0 until TILE_SIZE) {
                newTile.pixels[row][col] = !newTile.pixels[row][col]
                tiles[_selectedTileIndex.value] = newTile // Reemplaça l'objecte a la llista
            }
        }
    }

    /**
     * Afegeix un nou tile buit a la llista de tiles i el selecciona.
     */
    fun addNewTile() {
        val newTile = TileData()
        tiles.add(newTile)
        _selectedTileIndex.value = tiles.size - 1 // Selecciona el nou tile
    }

    /**
     * Elimina el tile actualment seleccionat.
     * Si només queda un tile, el buida enlloc d'eliminar-lo.
     */
    fun deleteSelectedTile() {
        if (tiles.size > 1 && _selectedTileIndex.value < tiles.size) {
            tiles.removeAt(_selectedTileIndex.value)
            // Ajusta l'índex seleccionat si s'ha eliminat l'últim o un del mig
            if (_selectedTileIndex.value >= tiles.size) {
                _selectedTileIndex.value = tiles.size - 1
            }
        } else if (tiles.size == 1) {
            // Si només queda un, el reseteja a un tile buit
            tiles[0] = TileData(id = tiles[0].id) // Conserva l'ID si es vol
        }
        if (tiles.isEmpty()){ // Si per alguna raó queda buit, afegeix un de nou
            addNewTile()
        }
    }
}