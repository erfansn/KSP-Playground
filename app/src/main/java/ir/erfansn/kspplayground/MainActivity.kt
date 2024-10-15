package ir.erfansn.kspplayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ir.erfansn.kspplayground.annotation.KspTest
import ir.erfansn.kspplayground.generated.GeneratedGreeting
import ir.erfansn.kspplayground.generated.GeneratedGreetingPreview
import ir.erfansn.kspplayground.generated.GeneratedTestAnotherModule
import ir.erfansn.kspplayground.ui.theme.KSPPlaygroundTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      KSPPlaygroundTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Greeting(
            name = "Android $GeneratedTestAnotherModule",
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}

@KspTest("Normal")
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $GeneratedGreeting $name!",
    modifier = modifier
  )
}

@KspTest("Preview")
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  KSPPlaygroundTheme {
    Greeting("Android $GeneratedGreetingPreview")
  }
}