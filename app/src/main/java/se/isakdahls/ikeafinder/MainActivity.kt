package se.isakdahls.ikeafinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import se.isakdahls.ikeafinder.navigation.NavigationGraph
import se.isakdahls.ikeafinder.ui.theme.IkeaFinderTheme
import se.isakdahls.ikeafinder.utils.AssetManager

/**
 * Huvudaktivitet för IKEA Finder appen
 */
class MainActivity : ComponentActivity() {
    // denna funktion körs när appen startar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AssetManager.copyDatabaseIfNeeded(this) //kopiera databas från assets
        
        enableEdgeToEdge() //fullskärm
        setContent {
            IkeaFinderTheme { //temainställningar
                val navController = rememberNavController()
                
                NavigationGraph(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

