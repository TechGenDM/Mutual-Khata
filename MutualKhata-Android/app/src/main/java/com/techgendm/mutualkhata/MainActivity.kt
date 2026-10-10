package com.techgendm.mutualkhata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val Ink = Color(0xFF1C2824)
private val Green = Color(0xFF176B54)
private val GreenLight = Color(0xFFE8F3ED)
private val Page = Color(0xFFF7F8F6)
private val Muted = Color(0xFF66736D)
private val Border = Color(0xFFE4E9E5)
private val Amber = Color(0xFF9A640D)
private val AmberLight = Color(0xFFFFF3D9)
private val Red = Color(0xFFB42318)
private val RedLight = Color(0xFFFDECEA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Page) {
                    MutualKhataApp()
                }
            }
        }
    }
}

private enum class AppTab(val label: String) {
    HOME("Home"), LEDGER("Khata"), REQUESTS("Requests"), PROFILE("Profile")
}

private data class Partner(
    val name: String,
    val description: String,
    val balance: String,
    val initials: String,
    val status: String
)

private data class LedgerRequest(
    val id: Int,
    val partner: String,
    val title: String,
    val amount: String,
    val note: String,
    val incoming: Boolean,
    val status: String = "Pending"
)

private val partners = listOf(
    Partner("Sharma Distributors", "FMCG · Last activity today", "₹8,450", "SD", "You owe"),
    Partner("Gupta Foods", "Snacks & beverages · Yesterday", "₹4,000", "GF", "You owe"),
    Partner("Maa Telecom Supply", "Mobile accessories · 4 Oct", "₹2,200", "MT", "They owe")
)

private fun inr(value: String): String = try {
    NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(value.toDouble())
        .replace("Rs.", "₹")
        .replace("INR", "₹")
} catch (_: Exception) {
    "₹$value"
}

@Composable
private fun MutualKhataApp() {
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedPartner by remember { mutableStateOf<Partner?>(null) }
    var nextId by remember { mutableStateOf(3) }
    val requests = remember {
        mutableStateListOf(
            LedgerRequest(1, "Sharma Distributors", "Invoice #SD-104", "4,850", "2 cartons biscuits, cooking oil", true),
            LedgerRequest(2, "Gupta Foods", "Payment recorded", "3,000", "Cash paid during visit", false)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Page)
            .padding(WindowInsets.safeDrawing.asPaddingValues())
    ) {
        if (selectedPartner != null) {
            PartnerDetailScreen(modifier = Modifier.fillMaxSize(), partner = selectedPartner!!, onBack = { selectedPartner = null }, onAdd = { showAddDialog = true })
        } else {
            AppHeader(onNotificationClick = { selectedTab = AppTab.REQUESTS })
            when (selectedTab) {
                AppTab.HOME -> HomeScreen(
                    modifier = Modifier.weight(1f),
                    requests = requests,
                    onAdd = { showAddDialog = true },
                    onPartnerClick = { selectedPartner = it },
                    onRequests = { selectedTab = AppTab.REQUESTS }
                )
                AppTab.LEDGER -> LedgerScreen(modifier = Modifier.weight(1f), onPartnerClick = { selectedPartner = it }, onAdd = { showAddDialog = true })
                AppTab.REQUESTS -> RequestsScreen(
                    modifier = Modifier.weight(1f),
                    requests = requests,
                    onAction = { id, status ->
                        val index = requests.indexOfFirst { it.id == id }
                        if (index >= 0) requests[index] = requests[index].copy(status = status)
                    }
                )
                AppTab.PROFILE -> ProfileScreen(modifier = Modifier.weight(1f))
            }
            BottomBar(selectedTab = selectedTab, onSelect = { selectedTab = it })
        }
    }

    if (showAddDialog) {
        AddEntryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { partner, kind, amount, note ->
                requests.add(
                    0,
                    LedgerRequest(
                        id = nextId++, partner = partner, title = kind, amount = amount,
                        note = note.ifBlank { "No extra note" }, incoming = false
                    )
                )
                selectedTab = AppTab.REQUESTS
                selectedPartner = null
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AppHeader(onNotificationClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Green),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(23.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Mutual Khata", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text("Dono ka hisaab. Dono ki tasdeeq.", fontSize = 11.sp, color = Muted)
        }
        Surface(color = AmberLight, shape = RoundedCornerShape(50)) {
            Text("DEMO", modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Amber)
        }
        IconButton(onClick = onNotificationClick) {
            Icon(Icons.Default.NotificationsNone, contentDescription = "Requests", tint = Ink)
        }
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    requests: List<LedgerRequest>,
    onAdd: () -> Unit,
    onPartnerClick: (Partner) -> Unit,
    onRequests: () -> Unit
) {
    val pendingCount = requests.count { it.status == "Pending" }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text("Namaste! 👋", fontSize = 14.sp, color = Muted)
                Text("Aapka hisaab", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("Apne saare business khate ek jagah dekhein.", fontSize = 13.sp, color = Muted)
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Green), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("SAMPLE SUMMARY", fontSize = 10.sp, color = Color(0xFFCFE5D9), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Aapko dena hai", color = Color(0xFFD7E9DF), fontSize = 12.sp)
                            Text("₹12,450", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0x33FFFFFF)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                    HorizontalDivider(color = Color(0x446FFFFFFF), modifier = Modifier.padding(vertical = 13.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Aapko lena hai", color = Color(0xFFD7E9DF), fontSize = 11.sp)
                            Text("₹2,200", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pending approvals", color = Color(0xFFD7E9DF), fontSize = 11.sp)
                            Text("$pendingCount", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                QuickAction(Modifier.weight(1f), icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Green) }, title = "Nayi entry", subtitle = "Maal / payment", onClick = onAdd)
                QuickAction(Modifier.weight(1f), icon = { Icon(Icons.AutoMirrored.Filled.Rule, contentDescription = null, tint = Green) }, title = "Approve karein", subtitle = "$pendingCount request(s)", onClick = onRequests)
            }
        }
        item {
            SectionHeader("Aapke khate", action = "Sab dekhein", onAction = { onPartnerClick(partners.first()) })
        }
        items(partners.take(3)) { partner -> PartnerRow(partner = partner, onClick = { onPartnerClick(partner) }) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = Muted, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(7.dp))
                Text("Yaad rakhein", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Approval ke baad hi balance badlega", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                        Text("Is prototype mein data sample hai. Doosre phone se real approval abhi connect nahi hai.", fontSize = 12.sp, color = Muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAction(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(17.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(13.dp)) {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(GreenLight), contentAlignment = Alignment.Center) {
                icon()
            }
            Spacer(Modifier.height(9.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(subtitle, fontSize = 11.sp, color = Muted)
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
        if (action != null) TextButton(onClick = onAction, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            Text(action, color = Green, fontSize = 12.sp)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Green, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun PartnerRow(partner: Partner, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(43.dp).clip(RoundedCornerShape(14.dp)).background(GreenLight), contentAlignment = Alignment.Center) {
                Text(partner.initials, color = Green, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(partner.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(partner.description, fontSize = 11.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(5.dp))
                Text(partner.status, fontSize = 10.sp, color = if (partner.status == "You owe") Amber else Green)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(partner.balance, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Open khata", tint = Muted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun LedgerScreen(modifier: Modifier = Modifier, onPartnerClick: (Partner) -> Unit, onAdd: () -> Unit) {
    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Saare khate", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text("Har distributor ka alag, saaf record.", fontSize = 13.sp, color = Muted)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Green), shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(7.dp))
            Text("Nayi entry karein")
        }
        Spacer(Modifier.height(14.dp))
        partners.forEach { partner ->
            PartnerRow(partner, onClick = { onPartnerClick(partner) })
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text("Sample balances — live accounts se connected nahi.", fontSize = 11.sp, color = Muted)
    }
}

@Composable
private fun RequestsScreen(modifier: Modifier = Modifier, requests: List<LedgerRequest>, onAction: (Int, String) -> Unit) {
    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Tasdeeq / Requests", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text("Entry ko confirm karne se pehle details milayein.", fontSize = 13.sp, color = Muted)
        Spacer(Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = AmberLight), shape = RoundedCornerShape(14.dp)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.AutoMirrored.Filled.Rule, contentDescription = null, tint = Amber, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Demo actions sirf is phone par status badalte hain; doosri party ko notification nahi jaati.", color = Amber, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        if (requests.isEmpty()) {
            EmptyState("Abhi koi request nahi", "Nayi entry banane par approval request yahan dikhegi.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 18.dp)) {
                items(requests, key = { it.id }) { request -> RequestCard(request, onAction) }
            }
        }
    }
}

@Composable
private fun RequestCard(request: LedgerRequest, onAction: (Int, String) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(17.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(request.partner, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink)
                    Text(if (request.incoming) "Unki taraf se aaya" else "Aapki taraf se bheja", fontSize = 11.sp, color = Muted)
                }
                Surface(color = when (request.status) { "Pending" -> AmberLight; "Accepted (demo)" -> GreenLight; else -> RedLight }, shape = RoundedCornerShape(50)) {
                    Text(request.status, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), fontSize = 10.sp, color = when (request.status) { "Pending" -> Amber; "Accepted (demo)" -> Green; else -> Red }, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(request.title, fontSize = 13.sp, color = Ink)
            Text(inr(request.amount), fontSize = 22.sp, color = Ink, fontWeight = FontWeight.Bold)
            Text(request.note, fontSize = 12.sp, color = Muted)
            if (request.status == "Pending" && request.incoming) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { onAction(request.id, "Rejected (demo)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(5.dp)); Text("Reject")
                    }
                    Button(onClick = { onAction(request.id, "Accepted (demo)") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Green), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(5.dp)); Text("Simulate approve")
                    }
                }
            } else if (request.status == "Pending") {
                Text("Doosri party ki approval ka intezaar.", fontSize = 11.sp, color = Amber)
            }
        }
    }
}

@Composable
private fun ProfileScreen(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Profile & settings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text("Simple controls for dukandaar aur distributor.", fontSize = 13.sp, color = Muted)
        Spacer(Modifier.height(18.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(GreenLight), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Green, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Demo Dukandaar", color = Ink, fontWeight = FontWeight.Bold)
                    Text("Retailer mode · sample profile", color = Muted, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        InfoSetting("Language", "Hindi / simple English — language controls next milestone")
        InfoSetting("Security", "PIN / phone verification to be added")
        InfoSetting("Data & backup", "Cloud sync and recovery to be connected")
        Spacer(Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = AmberLight), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text("Prototype status", color = Amber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Text("Abhi koi real business, phone number, payment account ya cloud backend connected nahi hai. Sample data hi use karein.", color = Amber, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun InfoSetting(title: String, detail: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(detail, color = Muted, fontSize = 12.sp)
    }
    HorizontalDivider(color = Border)
}

@Composable
private fun PartnerDetailScreen(modifier: Modifier = Modifier, partner: Partner, onBack: () -> Unit, onAdd: () -> Unit) {
    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink) }
            Column {
                Text(partner.name, fontSize = 18.sp, color = Ink, fontWeight = FontWeight.Bold)
                Text("Shared khata · Demo", fontSize = 11.sp, color = Muted)
            }
        }
        Spacer(Modifier.height(14.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Green), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("Current sample balance", color = Color(0xFFD7E9DF), fontSize = 12.sp)
                Text(partner.balance, color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.Bold)
                Text(partner.status, color = Color(0xFFD7E9DF), fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Green), shape = RoundedCornerShape(13.dp)) {
            Icon(Icons.Default.Add, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Record maal / payment")
        }
        Spacer(Modifier.height(16.dp))
        Text("Recent entries", fontWeight = FontWeight.Bold, color = Ink, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        ActivityRow("Maal received", "6 Oct · Invoice #104", "+₹4,850", "Confirmed sample")
        ActivityRow("Cash payment", "4 Oct · Receipt #21", "−₹3,000", "Confirmed sample")
        ActivityRow("Invoice note", "2 Oct · No photo", "+₹1,200", "Sample record")
        Spacer(Modifier.height(12.dp))
        Text("Important: sample history only. Real approval locks and cloud audit trail are not connected yet.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun ActivityRow(title: String, detail: String, amount: String, status: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(GreenLight), contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = Green, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("$detail · $status", color = Muted, fontSize = 10.sp)
        }
        Text(amount, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider(color = Border)
}

@Composable
private fun EmptyState(title: String, detail: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 45.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = Muted, modifier = Modifier.size(42.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, color = Ink, fontWeight = FontWeight.Bold)
        Text(detail, color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun AddEntryDialog(onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var partner by remember { mutableStateOf("Sharma Distributors") }
    var kind by remember { mutableStateOf("Maal mila") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nayi entry", color = Ink, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Kis ke saath?", color = Muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("Sharma Distributors", "Gupta Foods").forEach { name ->
                        FilterChip(selected = partner == name, onClick = { partner = name }, label = { Text(if (name.startsWith("Sharma")) "Sharma" else "Gupta", fontSize = 11.sp) })
                    }
                }
                Text("Entry ka type", color = Muted, fontSize = 12.sp)
                listOf("Maal mila", "Payment ki", "Maal return").forEach { option ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { kind = option }) {
                        RadioButton(selected = kind == option, onClick = { kind = option })
                        Text(option, color = Ink, fontSize = 13.sp)
                    }
                }
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' }.take(10); error = null },
                    label = { Text("Amount (₹)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(value = note, onValueChange = { note = it.take(120) }, label = { Text("Note / bill reference (optional)") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
                if (error != null) Text(error!!, color = Red, fontSize = 12.sp)
                Text("Save karne par request pending rahegi. Is demo mein balance automatically change nahi hoga.", color = Muted, fontSize = 11.sp)
            }
        },
        confirmButton = {
            Button(onClick = {
                val value = amount.toDoubleOrNull()
                if (value == null || value <= 0.0) error = "Valid amount enter karein."
                else onSave(partner, kind, String.format(Locale.US, "%.2f", value), note.trim())
            }, colors = ButtonDefaults.buttonColors(containerColor = Green)) { Text("Request approval") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = Color.White
    )
}

@Composable
private fun BottomBar(selectedTab: AppTab, onSelect: (AppTab) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
        AppTab.entries.forEach { tab ->
            val icon = when (tab) {
                AppTab.HOME -> Icons.Default.Home
                AppTab.LEDGER -> Icons.AutoMirrored.Filled.ReceiptLong
                AppTab.REQUESTS -> Icons.AutoMirrored.Filled.Rule
                AppTab.PROFILE -> Icons.Default.Person
            }
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(icon, contentDescription = tab.label) },
                label = { Text(tab.label, fontSize = 10.sp) },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(selectedIconColor = Green, selectedTextColor = Green, indicatorColor = GreenLight, unselectedIconColor = Muted, unselectedTextColor = Muted)
            )
        }
    }
}
