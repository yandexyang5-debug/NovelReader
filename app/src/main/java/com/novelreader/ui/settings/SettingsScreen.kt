package com.novelreader.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novelreader.data.model.ReadingSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: ReadingSettings,
    onSettingsChange: (ReadingSettings) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 字体大小设置
            SettingsSection(title = "字体大小") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (settings.fontSize > 12f) {
                                onSettingsChange(settings.copy(fontSize = settings.fontSize - 1f))
                            }
                        },
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("A-", fontSize = 16.sp)
                    }

                    Slider(
                        value = settings.fontSize,
                        onValueChange = { onSettingsChange(settings.copy(fontSize = it)) },
                        valueRange = 12f..36f,
                        steps = 23,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )

                    Button(
                        onClick = {
                            if (settings.fontSize < 36f) {
                                onSettingsChange(settings.copy(fontSize = settings.fontSize + 1f))
                            }
                        },
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("A+", fontSize = 16.sp)
                    }
                }

                Text(
                    text = "${settings.fontSize.toInt()}sp",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color(ReadingSettings.PRIMARY)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 行间距设置
            SettingsSection(title = "行间距") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (settings.lineHeight > 1.0f) {
                                onSettingsChange(settings.copy(lineHeight = settings.lineHeight - 0.1f))
                            }
                        },
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("-", fontSize = 16.sp)
                    }

                    Slider(
                        value = settings.lineHeight,
                        onValueChange = { onSettingsChange(settings.copy(lineHeight = it)) },
                        valueRange = 1.0f..4.0f,
                        steps = 29,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )

                    Button(
                        onClick = {
                            if (settings.lineHeight < 4.0f) {
                                onSettingsChange(settings.copy(lineHeight = settings.lineHeight + 0.1f))
                            }
                        },
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+", fontSize = 16.sp)
                    }
                }

                Text(
                    text = String.format("%.1f", settings.lineHeight),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color(ReadingSettings.PRIMARY)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 字间距设置
            SettingsSection(title = "字间距") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (settings.letterSpacing > 0f) {
                                onSettingsChange(settings.copy(letterSpacing = settings.letterSpacing - 0.5f))
                            }
                        },
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("-", fontSize = 16.sp)
                    }

                    Slider(
                        value = settings.letterSpacing,
                        onValueChange = { onSettingsChange(settings.copy(letterSpacing = it)) },
                        valueRange = 0f..5f,
                        steps = 9,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )

                    Button(
                        onClick = {
                            if (settings.letterSpacing < 5f) {
                                onSettingsChange(settings.copy(letterSpacing = settings.letterSpacing + 0.5f))
                            }
                        },
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+", fontSize = 16.sp)
                    }
                }

                Text(
                    text = "${settings.letterSpacing.toInt()}sp",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color(ReadingSettings.PRIMARY)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 背景颜色设置
            SettingsSection(title = "背景颜色") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ColorOption(
                        color = Color(ReadingSettings.BG_WHITE),
                        isSelected = settings.backgroundColor == ReadingSettings.BG_WHITE,
                        onClick = { onSettingsChange(settings.copy(backgroundColor = ReadingSettings.BG_WHITE)) }
                    )
                    ColorOption(
                        color = Color(ReadingSettings.BG_CREAM),
                        isSelected = settings.backgroundColor == ReadingSettings.BG_CREAM,
                        onClick = { onSettingsChange(settings.copy(backgroundColor = ReadingSettings.BG_CREAM)) }
                    )
                    ColorOption(
                        color = Color(ReadingSettings.BG_GREEN),
                        isSelected = settings.backgroundColor == ReadingSettings.BG_GREEN,
                        onClick = { onSettingsChange(settings.copy(backgroundColor = ReadingSettings.BG_GREEN)) }
                    )
                    ColorOption(
                        color = Color(ReadingSettings.BG_DARK),
                        isSelected = settings.backgroundColor == ReadingSettings.BG_DARK,
                        onClick = { onSettingsChange(settings.copy(backgroundColor = ReadingSettings.BG_DARK)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 夜间模式开关
            SettingsSection(title = "夜间模式") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("启用夜间模式")
                    Switch(
                        checked = settings.isNightMode,
                        onCheckedChange = { isNight ->
                            onSettingsChange(
                                settings.copy(
                                    isNightMode = isNight,
                                    backgroundColor = if (isNight) ReadingSettings.NIGHT_BG else ReadingSettings.BG_WHITE,
                                    textColor = if (isNight) ReadingSettings.NIGHT_TEXT else 0xFF333333
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun ColorOption(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected)
                    Modifier.border(3.dp, Color(ReadingSettings.PRIMARY), CircleShape)
                else
                    Modifier.border(1.dp, Color.Gray, CircleShape)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Text("✓", color = if (color == Color.Black) Color.White else Color.Black)
        }
    }
}
