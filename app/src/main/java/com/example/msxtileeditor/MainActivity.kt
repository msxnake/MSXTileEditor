//--------------version 0.01--------------------
//data: Diumenge, 11 de Maig de 2025
//---(c)----Jordi Sala---------------------------
package com.example.msxtileeditor


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.msxtileeditor.ui.theme.MSXTileEditorTheme // El nom del teu tema pot variar una mica

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MSXTileEditorTheme { // Aplica el tema definit per al projecte
                // Un Surface container utilitzant el color 'background' del tema
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Crida al Composable principal de l'editor de tiles
                    TileEditorScreen()
                }
            }
        }
    }
}

// Previsualització per a Android Studio (opcional però útil)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MSXTileEditorTheme {
        TileEditorScreen()
    }
}