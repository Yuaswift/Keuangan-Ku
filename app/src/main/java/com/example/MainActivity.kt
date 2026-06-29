package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AuthRootScreen
import com.example.ui.FinanceDashboard
import com.example.ui.FinanceViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Instantiate State ViewModel
    val viewModel: FinanceViewModel by viewModels {
      FinanceViewModel.Factory(application)
    }

    setContent {
      MyApplicationTheme {
        val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
        
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          if (isLoggedIn) {
            FinanceDashboard(
              viewModel = viewModel,
              modifier = Modifier.padding(innerPadding)
            )
          } else {
            AuthRootScreen(
              viewModel = viewModel,
              modifier = Modifier.padding(innerPadding)
            )
          }
        }
      }
    }
  }
}

