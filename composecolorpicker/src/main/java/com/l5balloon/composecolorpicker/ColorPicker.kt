package com.l5balloon.composecolorpicker

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.core.graphics.toColorInt

// --- Util functions ---
fun Float.toRadians() = (this / 180f) * Math.PI.toFloat()
fun Float.toDegrees() = (this * 180f) / Math.PI.toFloat()

fun makeColorWheelBitmap(width: Int, height: Int, value: Float): Bitmap {
    val bitmap = createBitmap(width, height)
    val centerX = width / 2f
    val centerY = height / 2f
    val radius = min(centerX, centerY)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val dx = x - centerX
            val dy = y - centerY
            val r = sqrt(dx * dx + dy * dy)
            if (r <= radius) {
                val hue = (atan2(dy, dx).toDegrees() + 360) % 360
                val sat = (r / radius).coerceIn(0f, 1f)
                val color = android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, value))
                bitmap[x, y] = color
            } else {
                bitmap[x, y] = android.graphics.Color.TRANSPARENT
            }
        }
    }
    return bitmap
}

fun isValidHex(hex: String, useAlpha: Boolean): Boolean {
    val regex = if (useAlpha) Regex("([A-Fa-f0-9]{8})$") else Regex("([A-Fa-f0-9]{6})$")
    return regex.matches(hex)
}

fun colorToHex(color: Color, useAlpha: Boolean): String {
    val intColor = color.toArgb()
    return if (useAlpha) {
        String.format("%08X", intColor)
    } else {
        val rgb = intColor and 0x00FFFFFF
        String.format("%06X", rgb)
    }
}

// --- Composable function ---
@Composable
fun BrightnessSlider(
    hue: Float,
    saturation: Float,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    val colors = remember(hue, saturation) {
        List(11) { i ->
            val v = i / 10f
            Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, v)))
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(
                brush = Brush.horizontalGradient(colors),
                shape = RoundedCornerShape(8.dp)
            )
            .border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
        )
    }
}

@Composable
fun RGBSlider(
    value: Int,
    inputValue: String,
    onValueChange: (Int) -> Unit,
    onInputChange: (String) -> Unit,
    channel: String, // "R", "G", "B"
    fixedR: Int,
    fixedG: Int,
    fixedB: Int,
    modifier: Modifier = Modifier
) {
    val gradientColors = remember(fixedR, fixedG, fixedB, channel) {
        when (channel) {
            "R" -> listOf(
                Color(0, fixedG, fixedB),
                Color(255, fixedG, fixedB)
            )
            "G" -> listOf(
                Color(fixedR, 0, fixedB),
                Color(fixedR, 255, fixedB)
            )
            "B" -> listOf(
                Color(fixedR, fixedG, 0),
                Color(fixedR, fixedG, 255)
            )
            else -> listOf(Color.Black, Color.White)
        }
    }
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(bottom = 4.dp)
        ) {
            Text(channel, modifier = Modifier.padding(bottom = 8.dp, end = 8.dp))
            OutlinedTextField(
                value = inputValue,
                onValueChange = { str ->
                    val filtered = str.filter { it.isDigit() }.take(3)
                    onInputChange(filtered)
                    val intVal = filtered.toIntOrNull()?.coerceIn(0, 255)
                    if (intVal != null) onValueChange(intVal)
                },
                singleLine = true,
                modifier = Modifier.width(96.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(
                    brush = Brush.horizontalGradient(gradientColors),
                    shape = RoundedCornerShape(8.dp)
                )
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 0f..255f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun RGBSliders(
    r: Int, g: Int, b: Int,
    rInput: String,
    gInput: String,
    bInput: String,
    onRChange: (Int) -> Unit,
    onGChange: (Int) -> Unit,
    onBChange: (Int) -> Unit,
    onRInputChange: (String) -> Unit,
    onGInputChange: (String) -> Unit,
    onBInputChange: (String) -> Unit
) {
    Column {
        RGBSlider(
            value = r,
            inputValue = rInput,
            onValueChange = onRChange,
            onInputChange = onRInputChange,
            channel = "R",
            fixedR = r, fixedG = g, fixedB = b
        )
        Spacer(Modifier.height(16.dp))
        RGBSlider(
            value = g,
            inputValue = gInput,
            onValueChange = onGChange,
            onInputChange = onGInputChange,
            channel = "G",
            fixedR = r, fixedG = g, fixedB = b
        )
        Spacer(Modifier.height(16.dp))
        RGBSlider(
            value = b,
            inputValue = bInput,
            onValueChange = onBChange,
            onInputChange = onBInputChange,
            channel = "B",
            fixedR = r, fixedG = g, fixedB = b
        )
    }
}

@Composable
fun ColorWheel(
    hue: Float,
    saturation: Float,
    value: Float,
    onHsvChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val density = LocalDensity.current
    val sizePx = with(density) { size.roundToPx() }
    val bitmap = remember(value, sizePx) { makeColorWheelBitmap(sizePx, sizePx, value) }
    Box(
        modifier = modifier.size(size)
            .pointerInput(value) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        if (change.changedToDown() || change.pressed) {
                            if (value == 0f) continue
                            val center = Offset(sizePx / 2f, sizePx / 2f)
                            val dx = change.position.x - center.x
                            val dy = change.position.y - center.y
                            val r = sqrt(dx * dx + dy * dy)
                            val radius = sizePx / 2f
                            if (r <= radius) {
                                val hue = (atan2(dy, dx).toDegrees() + 360) % 360
                                val sat = (r / radius).coerceIn(0f, 1f)
                                onHsvChange(hue, sat)
                            }
                            change.consume()
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawImage(bitmap.asImageBitmap())
            val radius = sizePx / 2f
            val selR = radius * saturation
            val selX = center.x + cos(hue.toRadians()) * selR
            val selY = center.y + sin(hue.toRadians()) * selR
            drawCircle(
                color = Color.White,
                radius = 16f,
                center = Offset(selX, selY),
                style = Stroke(width = 3f)
            )
        }
    }
}

@Composable
fun HexCode(
    hex: String,
    onHexChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    useAlpha: Boolean = false,
    useHexInput: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (useHexInput) Arrangement.Start else Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        when {
            useHexInput && useAlpha -> {
                // # 고정, 8자리 입력
                Text("#")
                OutlinedTextField(
                    value = hex,
                    onValueChange = { str ->
                        val filtered = str.filter { c -> c.isLetterOrDigit() }.take(8).uppercase()
                        onHexChange(filtered)
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("AARRGGBB") }
                )
            }
            useHexInput && !useAlpha -> {
                // #FF 고정, 6자리 입력
                Text("#FF")
                OutlinedTextField(
                    value = hex,
                    onValueChange = { str ->
                        val filtered = str.filter { c -> c.isLetterOrDigit() }.take(6).uppercase()
                        onHexChange(filtered)
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("RRGGBB") }
                )
            }
            else -> {
                // 입력 불가, #FF + 6자리 텍스트 표시 (useAlpha=false)
                if (useAlpha) {
                    Text(
                        "#${hex.padEnd(8, '0')}",
                        modifier = Modifier.fillMaxWidth(),
                        style = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                    )
                } else {
                    Text(
                        "#FF${hex.padEnd(6, '0')}",
                        modifier = Modifier.fillMaxWidth(),
                        style = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun ColorPicker(
    initialColor: Color,
    onColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier,
    useAlpha: Boolean = false,
    showRGBSliders: Boolean = true,
    showHexCode: Boolean = true,
    useHexInput: Boolean = true,
    colorWheelSize: Dp = 220.dp
) {
    val hsv = remember(initialColor) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(initialColor.toArgb(), it) }
    }
    var pickedColor by remember(initialColor) { mutableStateOf(initialColor) }
    var hexInput by remember(initialColor) {
        mutableStateOf(
            if (useAlpha) colorToHex(initialColor, true) else colorToHex(initialColor, false).takeLast(6)
        )
    }
    var rgb by remember(initialColor) {
        mutableStateOf(
            Triple(
                (initialColor.red * 255).roundToInt(),
                (initialColor.green * 255).roundToInt(),
                (initialColor.blue * 255).roundToInt()
            )
        )
    }
    var alpha by remember(initialColor) { mutableIntStateOf((initialColor.alpha * 255).roundToInt()) }
    var rgbInput by remember {
        mutableStateOf(
            Triple(
                (initialColor.red * 255).roundToInt().toString(),
                (initialColor.green * 255).roundToInt().toString(),
                (initialColor.blue * 255).roundToInt().toString()
            )
        )
    }
    var alphaInput by remember { mutableStateOf((initialColor.alpha * 255).roundToInt().toString()) }
    fun updateColorFromHSV() {
        val colorInt = if (useAlpha) android.graphics.Color.HSVToColor(alpha, hsv) else android.graphics.Color.HSVToColor(0xFF, hsv)
        val color = Color(colorInt)
        pickedColor = color
        hexInput = if (useAlpha) colorToHex(color, true) else colorToHex(color, false).takeLast(6)
        rgb = Triple(
            (color.red * 255).roundToInt(),
            (color.green * 255).roundToInt(),
            (color.blue * 255).roundToInt()
        )
        rgbInput = Triple(
            (color.red * 255).roundToInt().toString(),
            (color.green * 255).roundToInt().toString(),
            (color.blue * 255).roundToInt().toString()
        )
        alpha = (color.alpha * 255).roundToInt()
        alphaInput = (color.alpha * 255).roundToInt().toString()
        onColorChanged(color)
    }
    fun updateColorFromHex(hex: String) {
        val color = runCatching {
            if (useAlpha) Color(("#$hex").toColorInt())
            else Color(("#FF$hex").toColorInt())
        }.getOrNull()
        if (color != null) {
            pickedColor = color
            android.graphics.Color.colorToHSV(color.toArgb(), hsv)
            rgb = Triple(
                (color.red * 255).roundToInt(),
                (color.green * 255).roundToInt(),
                (color.blue * 255).roundToInt()
            )
            rgbInput = Triple(
                (color.red * 255).roundToInt().toString(),
                (color.green * 255).roundToInt().toString(),
                (color.blue * 255).roundToInt().toString()
            )
            alpha = (color.alpha * 255).roundToInt()
            alphaInput = (color.alpha * 255).roundToInt().toString()
            onColorChanged(color)
        }
    }
    fun updateColorFromRGB(r: Int, g: Int, b: Int) {
        val color = if (useAlpha) Color(r, g, b, alpha) else Color(r, g, b)
        pickedColor = color
        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
        hexInput = if (useAlpha) colorToHex(color, true) else colorToHex(color, false).takeLast(6)
        rgbInput = Triple(
            (color.red * 255).roundToInt().toString(),
            (color.green * 255).roundToInt().toString(),
            (color.blue * 255).roundToInt().toString()
        )
        onColorChanged(color)
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ColorWheel(
            hue = hsv[0],
            saturation = hsv[1],
            value = hsv[2],
            onHsvChange = { hue, sat ->
                hsv[0] = hue
                hsv[1] = sat
                updateColorFromHSV()
            },
            size = colorWheelSize
        )
        Spacer(Modifier.height(16.dp))
        BrightnessSlider(
            hue = hsv[0],
            saturation = hsv[1],
            value = hsv[2],
            onValueChange = {
                hsv[2] = it
                updateColorFromHSV()
            }
        )
        if (showRGBSliders) {
            Spacer(Modifier.height(16.dp))
            RGBSliders(
                r = rgb.first,
                g = rgb.second,
                b = rgb.third,
                rInput = rgbInput.first,
                gInput = rgbInput.second,
                bInput = rgbInput.third,
                onRChange = { r ->
                    rgb = Triple(r, rgb.second, rgb.third)
                    updateColorFromRGB(r, rgb.second, rgb.third)
                },
                onGChange = { g ->
                    rgb = Triple(rgb.first, g, rgb.third)
                    updateColorFromRGB(rgb.first, g, rgb.third)
                },
                onBChange = { b ->
                    rgb = Triple(rgb.first, rgb.second, b)
                    updateColorFromRGB(rgb.first, rgb.second, b)
                },
                onRInputChange = { str ->
                    val intVal = str.toIntOrNull()?.coerceIn(0, 255)
                    if (intVal != null) {
                        rgb = Triple(intVal, rgb.second, rgb.third)
                        updateColorFromRGB(intVal, rgb.second, rgb.third)
                    }
                },
                onGInputChange = { str ->
                    val intVal = str.toIntOrNull()?.coerceIn(0, 255)
                    if (intVal != null) {
                        rgb = Triple(rgb.first, intVal, rgb.third)
                        updateColorFromRGB(rgb.first, intVal, rgb.third)
                    }
                },
                onBInputChange = { str ->
                    val intVal = str.toIntOrNull()?.coerceIn(0, 255)
                    if (intVal != null) {
                        rgb = Triple(rgb.first, rgb.second, intVal)
                        updateColorFromRGB(rgb.first, rgb.second, intVal)
                    }
                }
            )
        }
        if (showHexCode) {
            Spacer(Modifier.height(16.dp))
            HexCode(
                hex = hexInput,
                onHexChange = { filtered ->
                    if (useHexInput) {
                        val hex = filtered.filter { c -> c.isLetterOrDigit() }.take(if (useAlpha) 8 else 6).uppercase()
                        hexInput = hex
                        if (isValidHex(hex, useAlpha)) {
                            updateColorFromHex(hex)
                        }
                    }
                },
                useAlpha = useAlpha,
                useHexInput = useHexInput
            )
        }
    }
}