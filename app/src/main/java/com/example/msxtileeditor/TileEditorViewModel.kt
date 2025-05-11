//--------------version 0.04--------------------
//data: Diumenge, 11 de Maig de 2025
//---(c)----Jordi Sala---------------------------
package com.example.msxtileeditor

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class TileEditorViewModel : ViewModel() {

    val tiles = mutableStateListOf<TileData>()
    private val _selectedTileIndex = mutableStateOf(0)
    val selectedTileIndex: Int get() = _selectedTileIndex.value

    val selectedTile: TileData? get() = tiles.getOrNull(_selectedTileIndex.value)

    private val _selectedColorIndex = mutableStateOf(MSXColorPalette.DEFAULT_PIXEL_COLOR_INDEX)
    val selectedColorIndex: Int get() = _selectedColorIndex.value

    init {
        if (tiles.isEmpty()) {
            tiles.add(TileData())
        }
    }

    fun selectTile(index: Int) {
        if (index >= 0 && index < tiles.size) {
            _selectedTileIndex.value = index
        }
    }

    fun setPixelColor(row: Int, col: Int) {
        selectedTile?.let { currentTile ->
            val newTile = currentTile.deepCopy()
            if (row in 0 until TILE_SIZE && col in 0 until TILE_SIZE) {
                newTile.pixels[row][col] = _selectedColorIndex.value
                tiles[_selectedTileIndex.value] = newTile
            }
        }
    }

    fun selectDrawingColor(colorIndex: Int) {
        if (colorIndex in MSXColorPalette.colors.indices) {
            _selectedColorIndex.value = colorIndex
        }
    }

    /**
     * Estableix un dels colors designats (FG o BG) per a una fila específica del tile seleccionat.
     * @param rowIndex L'índex de la fila (0-7).
     * @param isForegroundColor Si true, estableix el color Foreground; si false, el Background.
     * @param colorIndex L'índex del color de la paleta MSX a establir.
     */
    fun setDesignatedRowColor(rowIndex: Int, isForegroundColor: Boolean, colorIndex: Int) {
        selectedTile?.let { currentTile ->
            if (rowIndex in 0 until TILE_SIZE && colorIndex in MSXColorPalette.colors.indices) {
                val newTile = currentTile.deepCopy()
                val currentPair = newTile.designatedRowColors[rowIndex]
                newTile.designatedRowColors[rowIndex] = if (isForegroundColor) {
                    Pair(colorIndex, currentPair.second)
                } else {
                    Pair(currentPair.first, colorIndex)
                }
                tiles[_selectedTileIndex.value] = newTile
            }
        }
    }

    /**
     * Ajusta els píxels del tile seleccionat perquè compleixin amb els colors designats per fila.
     * Els píxels que no coincideixin amb cap dels dos colors designats per a la seva fila
     * es canviaran al color de fons (BG) designat per a aquella fila.
     */
    fun conformSelectedTileToDesignatedRowColors() {
        selectedTile?.let { currentTile ->
            val newTile = currentTile.deepCopy()
            for (r in 0 until TILE_SIZE) {
                val (fgColor, bgColor) = newTile.designatedRowColors[r]
                for (c in 0 until TILE_SIZE) {
                    val currentPixelColor = newTile.pixels[r][c]
                    if (currentPixelColor != fgColor && currentPixelColor != bgColor) {
                        newTile.pixels[r][c] = bgColor // Canvia al color de fons designat
                    }
                }
            }
            tiles[_selectedTileIndex.value] = newTile
        }
    }

    fun addNewTile() {
        val newTile = TileData()
        tiles.add(newTile)
        _selectedTileIndex.value = tiles.size - 1
    }

    fun deleteSelectedTile() {
        if (tiles.size > 1 && _selectedTileIndex.value < tiles.size) {
            tiles.removeAt(_selectedTileIndex.value)
            if (_selectedTileIndex.value >= tiles.size) {
                _selectedTileIndex.value = tiles.size - 1
            }
        } else if (tiles.size == 1) {
            tiles[0] = TileData(id = tiles[0].id)
        }
        if (tiles.isEmpty()){
            addNewTile()
        }
    }
}