package com.bizcopay.app.ui.payer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizcopay.app.data.network.models.TopUpRequestResponse
import com.bizcopay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpRequestSheet(
    submitState: TopUpSubmitState,
    myRequests: List<TopUpRequestResponse>,
    onSubmit: (amount: Double, note: String?) -> Unit,
    onDismiss: () -> Unit,
    onClearState: () -> Unit,
) {
    var amountText by remember { mutableStateOf("") }
    var noteText   by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = {
            onClearState()
            onDismiss()
        },
        containerColor = BizcoBackground,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Request Top-Up",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BizcoTextPrimary
                )
                IconButton(onClick = {
                    onClearState()
                    onDismiss()
                }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = BizcoTextMuted)
                }
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Form
                item {
                    when (submitState) {
                        is TopUpSubmitState.Success -> {
                            SuccessBanner(amount = submitState.amount) {
                                amountText = ""
                                noteText   = ""
                                onClearState()
                            }
                        }

                        is TopUpSubmitState.Error -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDED)),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(
                                    submitState.message,
                                    modifier = Modifier.padding(14.dp),
                                    color = Color(0xFFB91C1C),
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            TopUpForm(
                                amountText = amountText,
                                noteText = noteText,
                                loading = false,
                                onAmountChange = { amountText = it },
                                onNoteChange = { noteText = it },
                                onSubmit = {
                                    val amt = amountText.toDoubleOrNull()
                                    if (amt != null && amt > 0) onSubmit(amt, noteText)
                                }
                            )
                        }

                        else -> {
                            TopUpForm(
                                amountText = amountText,
                                noteText = noteText,
                                loading = submitState is TopUpSubmitState.Loading,
                                onAmountChange = { amountText = it },
                                onNoteChange = { noteText = it },
                                onSubmit = {
                                    val amt = amountText.toDoubleOrNull()
                                    if (amt != null && amt > 0) onSubmit(amt, noteText)
                                }
                            )
                        }
                    }
                }

                // My Requests history
                if (myRequests.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "My Requests",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BizcoTextPrimary
                        )
                    }
                    items(myRequests) { req ->
                        RequestHistoryItem(req)
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun TopUpForm(
    amountText: String,
    noteText: String,
    loading: Boolean,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = amountText,
            onValueChange = { v -> if (v.isEmpty() || v.matches(Regex("""^\d*\.?\d{0,2}$"""))) onAmountChange(v) },
            label = { Text("Amount (RWF)") },
            placeholder = { Text("e.g. 5000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BizcoBlue,
                focusedLabelColor = BizcoBlue,
            )
        )

        OutlinedTextField(
            value = noteText,
            onValueChange = onNoteChange,
            label = { Text("Note (optional)") },
            placeholder = { Text("Reason for top-up…") },
            singleLine = false,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BizcoBlue,
                focusedLabelColor = BizcoBlue,
            )
        )

        val canSubmit = amountText.toDoubleOrNull()?.let { it > 0 } == true && !loading

        Button(
            onClick = onSubmit,
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BizcoBlue)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Submit Request", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun SuccessBanner(amount: String, onNewRequest: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF059669),
                modifier = Modifier.size(48.dp)
            )
            Text(
                "Request Submitted!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF065F46)
            )
            Text(
                "$amount top-up request sent to admin for approval.\nYour wallet will be credited once approved.",
                fontSize = 13.sp,
                color = Color(0xFF047857),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onNewRequest) {
                Text("Submit another request", color = BizcoBlue, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun RequestHistoryItem(req: TopUpRequestResponse) {
    val (statusColor, statusBg) = when (req.status) {
        "APPROVED" -> Color(0xFF059669) to Color(0xFFECFDF5)
        "REJECTED" -> Color(0xFFDC2626) to Color(0xFFFFEDED)
        else       -> Color(0xFFD97706) to Color(0xFFFFFBEB)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = BizcoCard),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "RWF ${"%,.0f".format(req.amount.toDouble())}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BizcoTextPrimary
                )
                if (!req.note.isNullOrBlank()) {
                    Text(req.note, fontSize = 12.sp, color = BizcoTextMuted)
                }
                Text(
                    req.createdAt.take(10),
                    fontSize = 11.sp,
                    color = BizcoTextMuted
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(req.status, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
            }
        }
    }
}
