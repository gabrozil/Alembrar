package com.clipboardreminder.notification

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.clipboardreminder.domain.model.Reminder
import com.clipboardreminder.domain.repository.ReminderRepository
import com.clipboardreminder.ui.theme.GoldAccent
import com.clipboardreminder.ui.theme.GoldLight
import com.clipboardreminder.ui.theme.NavyDeep
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class FloatingBubbleService : LifecycleService(), SavedStateRegistryOwner {

    @Inject
    lateinit var reminderRepository: ReminderRepository

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        showBubble()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showBubble() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        val context = this
        composeView = ComposeView(context).apply {
            // Important for Compose in a Service
            setViewTreeLifecycleOwner(this@FloatingBubbleService)
            setViewTreeSavedStateRegistryOwner(this@FloatingBubbleService)
            
            setContent {
                var isExpanded by remember { mutableStateOf(false) }
                var offsetX by remember { mutableStateOf(params.x.toFloat()) }
                var offsetY by remember { mutableStateOf(params.y.toFloat()) }
                val pinnedReminders by reminderRepository.getPinnedReminders().collectAsState(initial = emptyList())

                Box(modifier = Modifier.fillMaxSize()) {
                    if (isExpanded) {
                        // Background overlay to dim when expanded
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                                .clickable { isExpanded = false }
                        )

                        // Expanded Menu
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp)
                                .shadow(12.dp, RoundedCornerShape(24.dp))
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .widthIn(max = 300.dp)
                                .heightIn(max = 400.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(GoldAccent)
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Lembretes Fixados",
                                        color = NavyDeep,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { isExpanded = false },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, null, tint = NavyDeep)
                                    }
                                }

                                if (pinnedReminders.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Nenhum lembrete fixado",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(pinnedReminders) { reminder ->
                                            BubbleReminderItem(reminder) {
                                                copyToClipboard(reminder.content)
                                                isExpanded = false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Floating Bubble
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                            .size(60.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(GoldAccent, GoldLight))
                            )
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { },
                                    onDragEnd = {
                                        // Snap to edge or stay
                                        params.x = offsetX.toInt()
                                        params.y = offsetY.toInt()
                                        windowManager.updateViewLayout(composeView, params)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        offsetX += dragAmount.x
                                        offsetY += dragAmount.y
                                        
                                        params.x = offsetX.toInt()
                                        params.y = offsetY.toInt()
                                        windowManager.updateViewLayout(composeView, params)
                                    }
                                )
                            }
                            .clickable { isExpanded = !isExpanded },
                        contentAlignment = Alignment.Center
                    ) {
                        // Simple Dot/Logo representation
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A", color = NavyDeep, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        // Adjust params to match the overlay needs
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        
        windowManager.addView(composeView, params)
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Alembrar", text)
        clipboard.setPrimaryClip(clip)
        // Optionally show a toast or notification, but since it's an overlay it should be handled carefully
    }

    override fun onDestroy() {
        composeView?.let { windowManager.removeView(it) }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}

@Composable
fun BubbleReminderItem(reminder: Reminder, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = reminder.content,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp), tint = GoldAccent)
        }
    }
}
