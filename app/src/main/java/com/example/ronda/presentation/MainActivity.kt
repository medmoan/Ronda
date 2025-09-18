package com.example.ronda.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ronda.domain.rondaGame.Difficulty
import com.example.ronda.presentation.ui.PlayRonda
import com.example.ronda.presentation.ui.Ronda
import com.example.ronda.presentation.ui.theme.RondaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RondaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PlayRonda(modifier = Modifier
                            .padding(innerPadding),
                        diff = Difficulty.Easy)
                }
            }
        }
    }
}