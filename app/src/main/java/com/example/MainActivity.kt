package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            AppNavigator()
        }
      }
    }
  }
}

enum class AppScreen {
    LOGIN, MAIN, PREMIUM
}

@Composable
fun AppNavigator() {
    var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }
    var isPremium by remember { mutableStateOf(false) }

    when (currentScreen) {
        AppScreen.LOGIN -> LoginScreen(onLoginSuccess = { currentScreen = AppScreen.MAIN })
        AppScreen.MAIN -> VpnApp(
            isPremium = isPremium,
            onNavigateToPremium = { currentScreen = AppScreen.PREMIUM }
        )
        AppScreen.PREMIUM -> PremiumScreen(
            onBack = { currentScreen = AppScreen.MAIN },
            onUpgrade = {
                isPremium = true
                currentScreen = AppScreen.MAIN
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Secure VPN - Login", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Security",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Email (Gmail) or Mobile Number") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        onLoginSuccess()
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
            if (isError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Please enter email/mobile and password", color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("OR", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onLoginSuccess,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue with Google")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(onBack: () -> Unit, onUpgrade: () -> Unit) {
    var transactionId by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ProtonVPN Plus") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = "Premium",
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF25C74B) // Proton Green
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Upgrade to Plus", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Get the most out of your privacy.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // Features list
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PremiumFeature("Highest speed servers (up to 10 Gbps)")
                PremiumFeature("Protect up to 10 devices at once")
                PremiumFeature("Strict no-logs policy")
                PremiumFeature("Access blocked content & streaming")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            var selectedPlan by remember { mutableStateOf(1) } // 1: Monthly, 2: Yearly

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier.weight(1f).clickable { selectedPlan = 1 },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedPlan == 1) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (selectedPlan == 1) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("1 Month", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("₹49", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f).clickable { selectedPlan = 2 },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedPlan == 2) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (selectedPlan == 2) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("1 Year", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("₹299", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Save 49%", fontSize = 12.sp, color = Color(0xFF25C74B), fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Pay via UPI", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "amresh923417das1674-1@okhdfcbank",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = transactionId,
                onValueChange = { transactionId = it },
                label = { Text("Enter UTR / Transaction ID") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (transactionId.isNotEmpty()) {
                        coroutineScope.launch {
                            isVerifying = true
                            kotlinx.coroutines.delay(2000)
                            isVerifying = false
                            showSuccessDialog = true
                        }
                    }
                },
                enabled = transactionId.isNotEmpty() && !isVerifying,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Verify Payment", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Payment Verified") },
            text = { Text("Welcome to Plus! Your premium plan is now active.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onUpgrade()
                }) {
                    Text("Continue")
                }
            }
        )
    }
}

@Composable
fun PremiumFeature(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, contentDescription = "Check", tint = Color(0xFF25C74B), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 15.sp)
    }
}

enum class ConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED
}

data class VpnServer(val name: String, val flag: String, val ping: Int, val isPremium: Boolean = false)

val servers = listOf(
    VpnServer("United States", "🇺🇸", 45),
    VpnServer("United Kingdom", "🇬🇧", 85),
    VpnServer("Germany", "🇩🇪", 110),
    VpnServer("Japan", "🇯🇵", 150),
    VpnServer("Australia", "🇦🇺", 210),
    VpnServer("Singapore", "🇸🇬", 180),
    VpnServer("Canada", "🇨🇦", 60),
    VpnServer("France", "🇫🇷", 100),
    VpnServer("Brazil", "🇧🇷", 190),
    VpnServer("India", "🇮🇳", 220),
    VpnServer("South Korea", "🇰🇷", 145),
    VpnServer("Netherlands", "🇳🇱", 105),
    VpnServer("Switzerland", "🇨🇭", 95),
    VpnServer("United Arab Emirates", "🇦🇪", 170),
    VpnServer("Mauritius", "🇲🇺", 35, isPremium = true),
    VpnServer("Hong Kong", "🇭🇰", 20, isPremium = true),
    VpnServer("Sweden", "🇸🇪", 30, isPremium = true),
    VpnServer("Italy", "🇮🇹", 40, isPremium = true),
    VpnServer("Spain", "🇪🇸", 42, isPremium = true)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnApp(isPremium: Boolean, onNavigateToPremium: () -> Unit) {
    var connectionState by remember { mutableStateOf(ConnectionState.DISCONNECTED) }
    var selectedServer by remember { mutableStateOf(servers[0]) }
    var showServerList by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var isProxyEnabled by remember { mutableStateOf(false) }
    var downloadSpeed by remember { mutableFloatStateOf(0f) }
    var uploadSpeed by remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.CONNECTED) {
            while (true) {
                downloadSpeed = (10..500).random().toFloat() / 10f
                uploadSpeed = (5..100).random().toFloat() / 10f
                kotlinx.coroutines.delay(1000)
            }
        } else {
            downloadSpeed = 0f
            uploadSpeed = 0f
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Secure VPN", fontWeight = FontWeight.Bold) },
                actions = {
                    if (!isPremium) {
                        IconButton(onClick = onNavigateToPremium) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = "Premium", tint = Color(0xFFFFC107))
                        }
                    }
                    IconButton(onClick = { showPrivacyPolicy = true }) {
                        Icon(Icons.Default.Security, contentDescription = "Privacy Policy")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Status Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when (connectionState) {
                        ConnectionState.DISCONNECTED -> "Not Connected"
                        ConnectionState.CONNECTING -> "Connecting..."
                        ConnectionState.CONNECTED -> "Connected"
                    },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (connectionState) {
                        ConnectionState.CONNECTED -> Color(0xFF4CAF50)
                        ConnectionState.CONNECTING -> Color(0xFFFFC107)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (connectionState == ConnectionState.CONNECTED) {
                    Text("Your IP is hidden. Data is encrypted.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Tap the button to secure your connection.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Connection Button
            val buttonColor by animateColorAsState(
                targetValue = when (connectionState) {
                    ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.surfaceVariant
                    ConnectionState.CONNECTING -> MaterialTheme.colorScheme.tertiary
                    ConnectionState.CONNECTED -> Color(0xFF25C74B) // Proton Green
                },
                animationSpec = tween(500),
                label = "ButtonColor"
            )

            val iconColor by animateColorAsState(
                targetValue = when (connectionState) {
                    ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.onSurface
                    ConnectionState.CONNECTING -> Color.White
                    ConnectionState.CONNECTED -> Color.Black
                },
                animationSpec = tween(500),
                label = "IconColor"
            )

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(buttonColor.copy(alpha = 0.1f))
                    .clickable(enabled = connectionState != ConnectionState.CONNECTING) {
                        if (connectionState == ConnectionState.DISCONNECTED) {
                            coroutineScope.launch {
                                connectionState = ConnectionState.CONNECTING
                                delay(2000) // Simulate connection delay
                                connectionState = ConnectionState.CONNECTED
                            }
                        } else {
                            connectionState = ConnectionState.DISCONNECTED
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(buttonColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Quick Connect Toggle",
                        tint = iconColor,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            // Speed Dashboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Download
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ArrowDownward, 
                            contentDescription = "Download", 
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", downloadSpeed)} MB/s",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                
                // Upload
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ArrowUpward, 
                            contentDescription = "Upload", 
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Upload", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", uploadSpeed)} MB/s",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            // Server and Settings
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Server Selection
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { if (connectionState == ConnectionState.DISCONNECTED) showServerList = true },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = selectedServer.flag, fontSize = 32.sp, modifier = Modifier.padding(end = 16.dp))
                            Column {
                                Text(text = "Current Server", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = selectedServer.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        Text(
                            text = "${selectedServer.ping} ms",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedServer.ping < 100) Color(0xFF25C74B) else Color(0xFFFF9800)
                        )
                    }
                }

                // Proxy Setting
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Smart Proxy", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = "Route app traffic via proxy", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isProxyEnabled,
                            onCheckedChange = { if (connectionState == ConnectionState.DISCONNECTED) isProxyEnabled = it },
                            enabled = connectionState == ConnectionState.DISCONNECTED
                        )
                    }
                }
            }
        }
    }

    if (showServerList) {
        ModalBottomSheet(onDismissRequest = { showServerList = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Server", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                LazyColumn {
                    items(servers) { server ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (server.isPremium && !isPremium) {
                                        showServerList = false
                                        onNavigateToPremium()
                                    } else {
                                        selectedServer = server
                                        showServerList = false
                                    }
                                }
                                .padding(vertical = 16.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = server.flag, fontSize = 28.sp, modifier = Modifier.padding(end = 16.dp))
                                Text(text = server.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                                if (server.isPremium) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = "Premium",
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${server.ping} ms",
                                color = if (server.ping < 100) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showPrivacyPolicy) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicy = false },
            title = { Text("Privacy Policy") },
            text = { Text("We value your privacy. This VPN application does not log your browsing history, traffic destination, data content, or DNS queries. Your connection is highly secured and encrypted. We ensure high-speed data transmission while maintaining strict privacy standards.") },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicy = false }) {
                    Text("I Agree")
                }
            }
        )
    }
}
