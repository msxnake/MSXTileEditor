//--------------version 0.02--------------------
//data: Diumenge, 11 de Maig de 2025
//---(c)----Jordi Sala---------------------------
@file:OptIn(ExperimentalMaterial3Api::class) // <--- AFEGEIX AQUESTA LÍNIA AQUÍ

package com.example.msxtileeditor // Assegura't que aquest és el teu paquet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.* // Aquesta importació ja inclou ExperimentalMaterial3Api si calgués individualment
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// Colors per defecte per als píxels (podem fer-los configurables més endavant)
val PIXEL_ON_COLOR = Color.Black
val PIXEL_OFF_COLOR = Color.White
val GRID_LINE_COLOR = Color.LightGray

/**
 * Composable principal que organitza la pantalla de l'editor de tiles.
 * Mostra la llista de tiles, l'editor del tile seleccionat i controls addicionals.
 * @param editorViewModel El ViewModel que gestiona l'estat de l'editor.
 */
@Composable
fun TileEditorScreen(editorViewModel: TileEditorViewModel = viewModel()) {
    // Recull l'estat del ViewModel per a què Compose reaccioni als canvis.
    val tiles = editorViewModel.tiles
    val selectedTileIndex = editorViewModel.selectedTileIndex
    val selectedTile = editorViewModel.selectedTile

    Scaffold(
        topBar = {
            TopAppBar( // Aquesta és una de les APIs que pot ser experimental
                title = { Text("Editor de Tiles MSX") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingActionButton(onClick = { editorViewModel.addNewTile() }) { // Aquesta també
                    Icon(Icons.Filled.Add, "Afegir nou tile")
                }
                FloatingActionButton(onClick = { editorViewModel.deleteSelectedTile() }) {
                    Icon(Icons.Filled.Delete, "Eliminar tile seleccionat")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp) // Marge general
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espai entre elements
        ) {
            // Secció per seleccionar un tile de la llista
            Text("Tiles:", style = MaterialTheme.typography.titleMedium)
            TileSelector(
                tiles = tiles,
                selectedTileIndex = selectedTileIndex,
                onTileSelected = { index -> editorViewModel.selectTile(index) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Secció per editar el tile seleccionat
            if (selectedTile != null) {
                Text("Editor (Tile ${selectedTileIndex + 1}):", style = MaterialTheme.typography.titleMedium)
                EditableTileView(
                    tileData = selectedTile,
                    onPixelToggle = { row, col ->
                        editorViewModel.togglePixel(row, col)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f) // Ocupa el 80% de l'amplada
                        .aspectRatio(1f) // Manté la proporció quadrada
                        .border(1.dp, GRID_LINE_COLOR)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Previsualització (64x64px):", style = MaterialTheme.typography.titleMedium)
                TilePreview(
                    tileData = selectedTile,
                    pixelSize = 8.dp, // Cada píxel del tile serà de 8dp en la previsualització
                    modifier = Modifier.size(64.dp) // 8 pixels * 8dp/pixel = 64dp
                )

            } else {
                Text("No hi ha cap tile seleccionat o la llista és buida.")
            }
        }
    }
}

/**
 * Mostra una fila de previsualitzacions de tiles per a la selecció.
 * @param tiles La llista de TileData a mostrar.
 * @param selectedTileIndex L'índex del tile actualment seleccionat.
 * @param onTileSelected Lambda que es crida quan un tile és seleccionat.
 */
@Composable
fun TileSelector(
    tiles: List<TileData>,
    selectedTileIndex: Int,
    onTileSelected: (Int) -> Unit
) {
    LazyRow( // Permet scroll horitzontal si hi ha molts tiles
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(tiles) { index, tile ->
            val borderColor = if (index == selectedTileIndex) MaterialTheme.colorScheme.primary else Color.Transparent
            Box(
                modifier = Modifier
                    .size(68.dp) // Mida una mica més gran per al border
                    .border(2.dp, borderColor)
                    .padding(2.dp) // Padding intern per a què el TilePreview no toqui el border
                    .clickable { onTileSelected(index) }
            ) {
                TilePreview(
                    tileData = tile,
                    pixelSize = (64.dp / TILE_SIZE), // Ajusta la mida del píxel per encaixar
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Composable que mostra un tile de forma editable.
 * Permet a l'usuari fer clic als píxels per canviar el seu estat.
 * @param tileData Les dades del tile a mostrar i editar.
 * @param onPixelToggle Lambda que es crida quan un píxel és clicat, passant la fila i columna.
 * @param modifier Modificador per personalitzar l'aparença i comportament.
 */
@Composable
fun EditableTileView(
    tileData: TileData,
    onPixelToggle: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier
        .pointerInput(tileData) { // Important re-executar el pointerInput si tileData canvia
            detectTapGestures { offset ->
                // Calculem la mida de cada cel·la del grid
                val cellSizePx = size.width / TILE_SIZE.toFloat()
                // Calculem la fila i columna clicada a partir de la posició del clic (offset)
                val col = (offset.x / cellSizePx).toInt().coerceIn(0, TILE_SIZE - 1)
                val row = (offset.y / cellSizePx).toInt().coerceIn(0, TILE_SIZE - 1)
                onPixelToggle(row, col)
            }
        }
    ) {
        // Dibuixa el tile utilitzant la funció compartida drawTileContent
        drawTileContent(tileData, PIXEL_ON_COLOR, PIXEL_OFF_COLOR, GRID_LINE_COLOR)
    }
}

/**
 * Composable que mostra una previsualització estàtica d'un tile.
 * @param tileData Les dades del tile a mostrar.
 * @param pixelSize La mida en Dp de cada píxel del tile en aquesta previsualització.
 * @param modifier Modificador per personalitzar l'aparença i comportament.
 */
@Composable
fun TilePreview(
    tileData: TileData,
    pixelSize: Dp, // Mida de cada píxel en la previsualització
    modifier: Modifier = Modifier
) {
    // Convertim Dp a píxels per al Canvas, però el Canvas en si es dimensiona amb Dp
    // La lògica de dibuix dins de 'drawTileContent' treballarà amb les dimensions reals del Canvas.
    Canvas(modifier = modifier.border(1.dp, GRID_LINE_COLOR)) {
        // Dibuixa el tile utilitzant la funció compartida drawTileContent
        // Per a la previsualització, no dibuixem les línies de la graella interna per claredat.
        drawTileContent(tileData, PIXEL_ON_COLOR, PIXEL_OFF_COLOR, Color.Transparent)
    }
}


/**
 * Funció d'extensió de DrawScope per dibuixar el contingut d'un tile (píxels i graella).
 * Aquesta funció es pot reutilitzar tant per l'editor com per la previsualització.
 * @param tileData Les dades del tile a dibuixar.
 * @param onColor El color per als píxels actius.
 * @param offColor El color per als píxels inactius.
 * @param gridColor El color per a les línies de la graella. Si és Color.Transparent, no es dibuixen.
 */
fun DrawScope.drawTileContent(
    tileData: TileData,
    onColor: Color,
    offColor: Color,
    gridColor: Color
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val pixelWidth = canvasWidth / TILE_SIZE
    val pixelHeight = canvasHeight / TILE_SIZE

    // Dibuixa cada píxel del tile
    for (row in 0 until TILE_SIZE) {
        for (col in 0 until TILE_SIZE) {
            val color = if (tileData.pixels[row][col]) onColor else offColor
            drawRect(
                color = color,
                topLeft = Offset(col * pixelWidth, row * pixelHeight),
                size = Size(pixelWidth, pixelHeight)
            )
        }
    }

    // Dibuixa les línies de la graella si el color de la graella no és transparent
    if (gridColor != Color.Transparent) {
        // Línies verticals
        for (i in 1 until TILE_SIZE) {
            drawLine(
                color = gridColor,
                start = Offset(i * pixelWidth, 0f),
                end = Offset(i * pixelWidth, canvasHeight),
                strokeWidth = 1f
            )
        }
        // Línies horitzontals
        for (i in 1 until TILE_SIZE) {
            drawLine(
                color = gridColor,
                start = Offset(0f, i * pixelHeight),
                end = Offset(canvasWidth, i * pixelHeight),
                strokeWidth = 1f
            )
        }
    }
}