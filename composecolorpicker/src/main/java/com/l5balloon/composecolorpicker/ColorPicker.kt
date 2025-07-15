package com.l5balloon.composecolorpicker

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
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

fun isValidHex(hex: String): Boolean {
    val regex = Regex("([A-Fa-f0-9]{8})$")
    return regex.matches(hex)
}

fun colorToHex(color: Color): String {
    val intColor = color.toArgb()
    return String.format("%08X", intColor)
}

// --- Composable function ---
@Composable
private fun SliderTextField(
    inputValue: String,
    onValueChange: (Int) -> Unit,
    onInputChange: (String) -> Unit,
    maxValue: Int
) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(32.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = inputValue,
            onValueChange = { str ->
                val filtered = str.filter { it.isDigit() }.take(3)
                onInputChange(filtered)
                val intVal = filtered.toIntOrNull()?.coerceIn(0, maxValue)
                if (intVal != null) onValueChange(intVal)
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
            ),
        )
    }
}

@Composable
private fun SliderTextField(
    inputValue: String,
    onValueChange: (Float) -> Unit,
    onInputChange: (String) -> Unit,
    maxValue: Int
) {
    val maxDigits = maxValue.toString().length
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(32.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = inputValue,
            onValueChange = { str ->
                val filtered = str.filter { it.isDigit() }.take(maxDigits)
                onInputChange(filtered)
                val floatVal = filtered.toFloatOrNull()?.coerceIn(0f, maxValue.toFloat())
                if (floatVal != null) onValueChange(floatVal)
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrightnessSlider(
    hue: Float,
    saturation: Float,
    value: Float,
    valueInput: String,
    onValueChange: (Float) -> Unit,
    onValueInputChange: (String) -> Unit
) {
    val colors = remember(hue, saturation) {
        List(11) { i ->
            val v = i / 10f
            Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, v)))
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(30.dp)
                .background(
                    brush = Brush.horizontalGradient(colors),
                    shape = RoundedCornerShape(15.dp)
                )
                .border(1.dp, Color.Black, RoundedCornerShape(15.dp))
                .padding(horizontal = 1.dp),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))),
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .border(
                                width = 2.dp,
                                color = Color.Gray,
                                shape = CircleShape
                            )
                    )
                }
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        SliderTextField(valueInput, onValueChange, onValueInputChange, 100)
    }
}

@Composable
fun CheckerboardBackground(
    modifier: Modifier = Modifier,
    cellSize: Dp = 8.dp
) {
    Canvas(modifier = modifier) {
        val cellPx = cellSize.toPx()
        val cols = (size.width / cellPx).toInt() + 1
        val rows = (size.height / cellPx).toInt() + 1
        val color1 = Color(0xFFC0C0C0)
        val color2 = Color.White
        for (x in 0 until cols) {
            for (y in 0 until rows) {
                drawRect(
                    color = if ((x + y) % 2 == 0) color1 else color2,
                    topLeft = Offset(x * cellPx, y * cellPx),
                    size = Size(cellPx, cellPx)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlphaSlider(
    color: Color,
    alpha: Int,
    alphaInput: String,
    onAlphaChange: (Int) -> Unit,
    onAlphaInputChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientColors = remember(color) {
        listOf(
            color.copy(alpha = 0f),
            color.copy(alpha = 1f)
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = modifier
                .weight(1f)
                .height(30.dp)
                .clip(RoundedCornerShape(15.dp))
        ) {
            // Checkerboard Background
            CheckerboardBackground(
                modifier = modifier
                    .fillMaxWidth()
                    .height(30.dp)
            )
            // Alpha Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(
                        brush = Brush.horizontalGradient(gradientColors),
                        shape = RoundedCornerShape(15.dp)
                    )
                    .border(1.dp, Color.Black, RoundedCornerShape(15.dp))
                    .padding(horizontal = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Slider(
                    value = alpha.toFloat(),
                    onValueChange = { onAlphaChange(it.toInt()) },
                    valueRange = 0f..255f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = color,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .border(
                                    width = 2.dp,
                                    color = Color.Gray,
                                    shape = CircleShape
                                )
                        )
                    }
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        SliderTextField(alphaInput, onAlphaChange, onAlphaInputChange, 255)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RGBSlider(
    value: Int,
    inputValue: String,
    onValueChange: (Int) -> Unit,
    onInputChange: (String) -> Unit,
    channel: String, // "Red", "Green", "Blue"
    fixedR: Int,
    fixedG: Int,
    fixedB: Int,
    modifier: Modifier = Modifier
) {
    val gradientColors = remember(fixedR, fixedG, fixedB, channel) {
        when (channel) {
            "Red" -> listOf(
                Color(0, fixedG, fixedB),
                Color(255, fixedG, fixedB)
            )
            "Green" -> listOf(
                Color(fixedR, 0, fixedB),
                Color(fixedR, 255, fixedB)
            )
            "Blue" -> listOf(
                Color(fixedR, fixedG, 0),
                Color(fixedR, fixedG, 255)
            )
            else -> listOf(Color.Black, Color.White)
        }
    }
    Column(modifier = modifier) {
        Text(
            channel,
            modifier = modifier
                .padding(bottom = 8.dp),
            fontSize = 14.sp,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
                    .background(
                        brush = Brush.horizontalGradient(gradientColors),
                        shape = RoundedCornerShape(15.dp)
                    )
                    .border(1.dp, Color.Black, RoundedCornerShape(15.dp))
                    .padding(horizontal = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Slider(
                    value = value.toFloat(),
                    onValueChange = { onValueChange(it.toInt()) },
                    valueRange = 0f..255f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(fixedR, fixedG, fixedB),
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .border(
                                    width = 2.dp,
                                    color = Color.Gray,
                                    shape = CircleShape
                                )
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            SliderTextField(inputValue, onValueChange, onInputChange, 255)
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
            channel = "Red",
            fixedR = r, fixedG = g, fixedB = b
        )
        Spacer(Modifier.height(16.dp))
        RGBSlider(
            value = g,
            inputValue = gInput,
            onValueChange = onGChange,
            onInputChange = onGInputChange,
            channel = "Green",
            fixedR = r, fixedG = g, fixedB = b
        )
        Spacer(Modifier.height(16.dp))
        RGBSlider(
            value = b,
            inputValue = bInput,
            onValueChange = onBChange,
            onInputChange = onBInputChange,
            channel = "Blue",
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
        modifier = modifier
            .size(size)
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
                // write 8 letters
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
                // write 6 letters
                Text("#${hex.substring(0, 2)}")
                OutlinedTextField(
                    value = hex.substring(2),
                    onValueChange = { str ->
                        val newStr = hex.substring(0, 2) + str
                        val filtered = newStr.filter { c -> c.isLetterOrDigit() }.take(8).uppercase()
                        onHexChange(filtered)
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("RRGGBB") }
                )
            }
            else -> {
                // Can't write, 8 letters
                Text(
                    "#${hex.padEnd(8, '0')}",
                    modifier = Modifier.fillMaxWidth(),
                    style = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                )
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
    showAlphaSlider: Boolean = false,
    showRGBSliders: Boolean = true,
    showHexCode: Boolean = true,
    useHexInput: Boolean = true,
    colorWheelSize: Dp = 220.dp
) {
    var pickedColor by remember(initialColor) { mutableStateOf(initialColor) }
    val hsv by remember(initialColor) {
        mutableStateOf(
            FloatArray(3).also { android.graphics.Color.colorToHSV(initialColor.toArgb(), it) }
        )
    }
    var hsvInput by remember(initialColor) {
        mutableStateOf(
            FloatArray(3).also { android.graphics.Color.colorToHSV(initialColor.toArgb(), it) }
                .let { hsv ->
                    arrayOf(
                        hsv[0].roundToInt().toString(),           // Hue: 0~360
                        (hsv[1] * 100).roundToInt().toString(),   // Saturation: 0~100
                        (hsv[2] * 100).roundToInt().toString()    // Value: 0~100
                    )
                }
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
    var rgbInput by remember {
        mutableStateOf(
            Triple(
                (initialColor.red * 255).roundToInt().toString(),
                (initialColor.green * 255).roundToInt().toString(),
                (initialColor.blue * 255).roundToInt().toString()
            )
        )
    }
    var alpha by remember(initialColor) { mutableIntStateOf((initialColor.alpha * 255).roundToInt()) }
    var alphaInput by remember { mutableStateOf((initialColor.alpha * 255).roundToInt().toString()) }
    var hexInput by remember(initialColor) {
        mutableStateOf(
            colorToHex(initialColor)
        )
    }

    fun updateColorFromHSV() {
        val colorInt = android.graphics.Color.HSVToColor(alpha, hsv)
        val color = Color(colorInt)
        pickedColor = color
        hexInput = colorToHex(color)
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
    fun updateColorFromAlpha() {
        val colorInt = android.graphics.Color.HSVToColor(alpha, hsv)
        val color = Color(colorInt)
        pickedColor = color
        hexInput = colorToHex(color)
        onColorChanged(color)
    }
    fun updateColorFromRGB(r: Int, g: Int, b: Int) {
        val color = Color(r, g, b, alpha)
        pickedColor = color
        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
        hsvInput = arrayOf(
            hsv[0].roundToInt().toString(),
            (hsv[1] * 100).roundToInt().toString(),
            (hsv[2] * 100).roundToInt().toString()
        )
        hexInput = colorToHex(color)
        rgbInput = Triple(
            (color.red * 255).roundToInt().toString(),
            (color.green * 255).roundToInt().toString(),
            (color.blue * 255).roundToInt().toString()
        )
        onColorChanged(color)
    }
    fun updateColorFromHex(hex: String) {
        val color = runCatching {
            Color(("#$hex").toColorInt())
        }.getOrNull()
        if (color != null) {
            pickedColor = color
            android.graphics.Color.colorToHSV(color.toArgb(), hsv)
            hsvInput = arrayOf(
                hsv[0].roundToInt().toString(),
                (hsv[1] * 100).roundToInt().toString(),
                (hsv[2] * 100).roundToInt().toString()
            )
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
            valueInput = hsvInput[2],
            onValueChange = {
                hsv[2] = it
                hsvInput[2] = (it * 100).roundToInt().toString()
                updateColorFromHSV()
            },
            onValueInputChange = { str ->
                val floatVal = str.toFloatOrNull()?.coerceIn(0f, 100f)
                if (floatVal != null) {
                    hsv[2] = floatVal
                    hsvInput[2] = str
                    updateColorFromHSV()
                }
            },
        )
        if (showAlphaSlider) {
            Spacer(Modifier.height(16.dp))
            AlphaSlider(
                color = pickedColor,
                alpha = alpha,
                alphaInput = alphaInput,
                onAlphaChange = { newAlpha ->
                    alpha = newAlpha
                    alphaInput = alpha.toString()
                    updateColorFromAlpha()
                },
                onAlphaInputChange = { str ->
                    val intVal = str.toIntOrNull()?.coerceIn(0, 255)
                    if (intVal != null) {
                        alpha = intVal
                        alphaInput = alpha.toString()
                        updateColorFromAlpha()
                    }
                },
            )
        }
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
                        val hex = filtered.filter { c -> c.isLetterOrDigit() }.take(8).uppercase()
                        hexInput = hex
                        if (isValidHex(hex)) {
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