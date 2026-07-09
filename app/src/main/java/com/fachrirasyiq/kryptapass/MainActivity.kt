package com.fachrirasyiq.kryptapass

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.fachrirasyiq.kryptapass.navigation.KryptaNavGraph
import com.fachrirasyiq.kryptapass.security.SessionManager
import com.fachrirasyiq.kryptapass.theme.KryptapassTheme
/**
 * Titik masuk utama aplikasi KryptaPass.
 */
class MainActivity : FragmentActivity() {
    

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        
        sessionManager = (application as KryptaPassApp).container.sessionManager
        
        enableEdgeToEdge()
        
        setContent {
            KryptapassTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val networkMonitor = (application as KryptaPassApp).container.networkMonitor
                    KryptaNavGraph(
                        sessionManager = sessionManager,
                        networkMonitor = networkMonitor
                    )
                }
            }
        }
    }
}
