//--------------version 0.05--------------------
//data: Diumenge, 11 de Maig de 2025
//---(c)----Jordi Sala---------------------------
@file:OptIn(ExperimentalMaterial3Api::class) // Ja no necessitem ExperimentalLayoutApi aquí

package com.example.msxtileeditor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

val GRID_LINE_COLOR = Color.DarkGray
val EDITOR_BACKGROUND_COLOR = Color.LightGray
val NON_CONFORMANT_PIXEL_OVERLAY_COLOR = Color.Red.copy(alpha = 0.3f) // Color per superposar als píxels no conformants

@Composable
fun TileEditorScreen(editorViewModel: TileEditorViewModel = viewModel()) {
    val tiles = editorViewModel.tiles
    val selectedTileIndex = editorViewModel.selectedTileIndex
    val selectedTile = editorViewModel.selectedTile
    val selectedDrawingColorIndex = editorViewModel.selectedColorIndex

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editor de Tiles MSX v0.05") }, // Actualitzem versió al títol
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingActionButton(onClick = { editorViewModel.addNewTile() }) {
                    Icon(Icons.Filled.Add, "Afegir nou tile")
                }
                FloatingActionButton(onClick = { editorViewModel.deleteSelectedTile() }) {
                    Icon(Icons.Filled.Delete, "Eliminar tile seleccionat")
                }
                if (selectedTile != null) {
                    FloatingActionButton(
                        onClick = { editorViewModel.conformSelectedTileToDesignatedRowColors() },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Icon(Icons.Filled.Check, "Conformar Tile a 2 colors/fila")
                    }
                }
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            ColorPaletteSidebar(
                selectedColorIndex = selectedDrawingColorIndex,
                onColorSelected = { colorIndex -> editorViewModel.selectDrawingColor(colorIndex) },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 4.dp)
            )

            Column(
                modifier = Modifier
                    .padding(start = 4.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp) // Augmentem una mica l'espaiat
            ) {
                Text("Tiles:", style = MaterialTheme.typography.titleMedium)
                TileSelector(
                    tiles = tiles,
                    selectedTileIndex = selectedTileIndex,
                    onTileSelected = { index -> editorViewModel.selectTile(index) }
                )

                // Ja no necessitem Spacer aquí perquè l'espaiat de la Column principal ja ho fa

                if (selectedTile != null) {
                    Text("Editor (Tile ${selectedTileIndex + 1}):", style = MaterialTheme.typography.titleMedium)
                    EditableTileView( // Passant els paràmetres necessaris
                        tileData = selectedTile,
                        currentlySelectedPaletteColorIndex = selectedDrawingColorIndex,
                        onPixelClicked = { row, col -> editorViewModel.setPixelColor(row, col) },
                        onDesignatedColorChange = { rowIndex, isFg, newColorIndex ->
                            editorViewModel.setDesignatedRowColor(rowIndex, isFg, newColorIndex)
                        },
                        modifier = Modifier.fillMaxWidth(0.95f) // Ajustem amplada
                    )

                    // Ja no necessitem el DesignatedRowColorsEditor separat
                    // ni el text que l'anunciava.

                    Text("Previsualització (64x64px):", style = MaterialTheme.typography.titleMedium)
                    TilePreview(
                        tileData = selectedTile,
                        modifier = Modifier.size(64.dp)
                    )

                } else {
                    Text("No hi ha cap tile seleccionat o la llista és buida.")
                }
            }
        }
    }
}

@Composable
fun ColorPaletteSidebar( /* ... (sense canvis respecte v0.04) ... */
                         selectedColorIndex: Int,
                         onColorSelected: (Int) -> Unit,
                         modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(IntrinsicSize.Min),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MSXColorPalette.colors.forEachIndexed { index, color ->
                val itemSize = 32.dp
                val borderColor = if (index == selectedColorIndex) MaterialTheme.colorScheme.outline else Color.Transparent
                val displayColor = if (color == Color.Transparent) EDITOR_BACKGROUND_COLOR else color // Mostra EDITOR_BACKGROUND_COLOR per transparent

                Box(
                    modifier = Modifier
                        .size(itemSize)
                        .clip(CircleShape)
                        .background(displayColor)
                        .border(2.dp, borderColor, CircleShape)
                        .clickable { onColorSelected(index) }
                ) {
                    if (color == Color.Transparent) { // Si el color de la paleta ÉS transparent
                        Text("T", Modifier.align(Alignment.Center), color = GRID_LINE_COLOR, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TileSelector( /* ... (sense canvis respecte v0.04) ... */
                  tiles: List<TileData>,
                  selectedTileIndex: Int,
                  onTileSelected: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(tiles) { index, tile ->
            val borderColor = if (index == selectedTileIndex) MaterialTheme.colorScheme.primary else Color.Gray
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .border(2.dp, borderColor, RoundedCornerShape(4.dp))
                    .padding(2.dp)
                    .clickable { onTileSelected(index) }
            ) {
                TilePreview(
                    tileData = tile,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Un petit quadre de color clicable, usat per als selectors FG/BG de cada fila.
 */
@Composable
fun ColorSwatch(colorIndex: Int, size: Dp, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = MSXColorPalette.colors.getOrElse(colorIndex) { Color.Magenta }
    val displayColor = if (color == Color.Transparent) EDITOR_BACKGROUND_COLOR else color
    Box(
        modifier = modifier // Permet passar modificadors externs
            .size(size)
            .clip(RoundedCornerShape(4.dp))
            .background(displayColor)
            .border(1.dp, GRID_LINE_COLOR, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
    ) {
        if (color == Color.Transparent) {
            Text(
                "T",
                Modifier.align(Alignment.Center),
                color = GRID_LINE_COLOR,
                fontSize = (size.value / 2.2).sp, // Ajusta mida de la T
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Nou Composable per a una cel·la de píxel individual dins de l'editor.
 */
@Composable
fun PixelCell(
    pixelColorIndex: Int,
    isNonConformant: Boolean,
    modifier: Modifier = Modifier // Per al .weight() i .aspectRatio()
) {
    val pixelColor = MSXColorPalette.colors.getOrElse(pixelColorIndex) { MSXColorPalette.colors[1] } // Negre per defecte si l'índex és invàlid
    val displayPixelColor = if(pixelColor == Color.Transparent) EDITOR_BACKGROUND_COLOR else pixelColor

    Box(
        modifier = modifier
            .background(displayPixelColor) // Color base del píxel (o fons si és transparent)
            .border(0.5.dp, GRID_LINE_COLOR.copy(alpha = 0.3f)) // Línia de graella fina
    ) {
        if (isNonConformant) {
            // Superposició per indicar que no és conformant
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NON_CONFORMANT_PIXEL_OVERLAY_COLOR)
            )
        }
    }
}

/**
 * Vista de tile editable, ara amb selectors de color FG/BG integrats per cada fila.
 */
@Composable
fun EditableTileView(
    tileData: TileData,
    currentlySelectedPaletteColorIndex: Int, // Per assignar als selectors FG/BG de fila
    onPixelClicked: (row: Int, col: Int) -> Unit,
    onDesignatedColorChange: (rowIndex: Int, isForegroundColor: Boolean, newColorIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.border(1.dp, GRID_LINE_COLOR.copy(alpha=0.7f)), // Contorn general
        verticalArrangement = Arrangement.spacedBy(0.dp) // Sense espai vertical entre les files internes
    ) {
        tileData.pixels.forEachIndexed { rowIndex, pixelRowList ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min), // Alçada basada en el contingut més alt
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Editor de píxels per a aquesta fila (8 cel·les)
                Row(
                    modifier = Modifier
                        .weight(1f) // Ocupa l'espai principal
                        .aspectRatio(8f/1f) // Proporció 8:1 per als 8 píxels
                    // Si l'alçada total de la fila del tile editor es fixa (ex: 32.dp),
                    // llavors l'aspectRatio aquí no és estrictament necessari o
                    // s'ha de coordinar amb l'alçada del ColorSwatch.
                    // Per ara, deixem que l'alçada de la fila es determini
                    // per l'alçada dels ColorSwatch més grans (24.dp + padding).
                ) {
                    pixelRowList.forEachIndexed { colIndex, currentPixelColorIndex ->
                        val (designatedFg, designatedBg) = tileData.designatedRowColors[rowIndex]
                        val isNonConformant = currentPixelColorIndex != designatedFg && currentPixelColorIndex != designatedBg

                        PixelCell(
                            pixelColorIndex = currentPixelColorIndex,
                            isNonConformant = isNonConformant,
                            modifier = Modifier
                                .weight(1f) // Cada cel·la de píxel té la mateixa amplada
                                .fillMaxHeight() // Ocupa tota l'alçada disponible a la seva fila
                                .clickable { onPixelClicked(rowIndex, colIndex) }
                        )
                    }
                }

                // Separador visual
                Spacer(modifier = Modifier.width(8.dp))

                // Selectors de color designat (FG/BG) per a aquesta fila
                val swatchSize = 24.dp // Mida dels selectors de color FG/BG
                ColorSwatch(
                    colorIndex = tileData.designatedRowColors[rowIndex].first, // FG
                    size = swatchSize,
                    onClick = {
                        onDesignatedColorChange(rowIndex, true, currentlySelectedPaletteColorIndex)
                    },
                    modifier = Modifier.align(Alignment.CenterVertically) // Assegura alineació
                )
                Spacer(modifier = Modifier.width(4.dp))
                ColorSwatch(
                    colorIndex = tileData.designatedRowColors[rowIndex].second, // BG
                    size = swatchSize,
                    onClick = {
                        onDesignatedColorChange(rowIndex, false, currentlySelectedPaletteColorIndex)
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp)) // Petit espai al final de la fila
            }
            if (rowIndex < TILE_SIZE - 1) {
                // Línia divisòria horitzontal entre les files de l'editor
                Divider(color = GRID_LINE_COLOR.copy(alpha=0.5f), thickness = 0.5.dp)
            }
        }
    }
}


@Composable
fun TilePreview( /* ... (sense canvis respecte v0.04) ... */
                 tileData: TileData,
                 modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier
        .background(EDITOR_BACKGROUND_COLOR)
        .border(1.dp, GRID_LINE_COLOR)
    ) {
        drawTileContent(tileData, drawGridLines = false, showNonConformantMarkers = false)
    }
}

fun DrawScope.drawTileContent( /* ... (sense canvis respecte v0.04, excepte el nom del paràmetre non-conformant) ... */
                               tileData: TileData,
                               drawGridLines: Boolean,
                               showNonConformantMarkers: Boolean
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val pixelWidth = canvasWidth / TILE_SIZE
    val pixelHeight = canvasHeight / TILE_SIZE

    for (row in 0 until TILE_SIZE) {
        for (col in 0 until TILE_SIZE) {
            val colorIndex = tileData.pixels[row][col]
            var pixelColor = MSXColorPalette.colors.getOrElse(colorIndex) { MSXColorPalette.colors[1] }

            // Si el color és transparent, per al dibuix el tractem com el fons de l'editor
            // Això només és per a la visualització, el valor a TileData segueix sent l'índex transparent
            if (pixelColor == Color.Transparent){
                pixelColor = EDITOR_BACKGROUND_COLOR
            }

            drawRect(
                color = pixelColor,
                topLeft = Offset(col * pixelWidth, row * pixelHeight),
                size = Size(pixelWidth, pixelHeight)
            )

            if (showNonConformantMarkers) {
                val (fgDesignated, bgDesignated) = tileData.designatedRowColors[row]
                // Comprovem l'índex original, no el 'pixelColor' modificat per a la visualització de transparents
                if (tileData.pixels[row][col] != fgDesignated && tileData.pixels[row][col] != bgDesignated) {
                    drawRect( // Dibuixa una superposició translúcida
                        color = NON_CONFORMANT_PIXEL_OVERLAY_COLOR,
                        topLeft = Offset(col * pixelWidth, row * pixelHeight),
                        size = Size(pixelWidth, pixelHeight)
                    )
                }
            }
        }
    }

    if (drawGridLines) {
        for (i in 1 until TILE_SIZE) {
            drawLine(GRID_LINE_COLOR, Offset(i * pixelWidth, 0f), Offset(i * pixelWidth, canvasHeight), strokeWidth = 0.5.dp.toPx())
            drawLine(GRID_LINE_COLOR, Offset(0f, i * pixelHeight), Offset(canvasWidth, i * pixelHeight), strokeWidth = 0.5.dp.toPx())
        }
    }
}