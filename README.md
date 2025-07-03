# ComposeColorPicker

A modular color picker library for Jetpack Compose.

## Features
- All-in-one `ColorPicker` composable for easy use
- Also provides ColorWheel, HexInput, BrightnessSlider, RGBSliders as independent composables
- Compose 1.5+ support
- Highly customizable and composable

## Installation

### Gradle (example)
```kotlin
dependencies {
    implementation("com.github.15balloon:composecolorpicker:1.0.0")
}
```

## Usage Example

```kotlin
import com.l5balloon.composecolorpicker.ColorPicker

@Composable
fun MyColorPickerScreen() {
    var color by remember { mutableStateOf(Color.Red) }
    ColorPicker(
        initialColor = color,
        onColorChanged = { color = it }
    )
}
```

## License
MIT

---

# 컴포즈 색상 선택 UI

Jetpack Compose용 컬러 피커(Color Picker) 라이브러리입니다.

## 특징
- 통합형 `ColorPicker` 컴포저블 제공 (간편 사용)
- ColorWheel, HexInput, BrightnessSlider, RGBSliders 등 개별 컴포저블도 제공
- Compose 1.5+ 지원
- 조합 및 커스터마이즈 용이

## 설치

### Gradle (예시)
```kotlin
dependencies {
    implementation("com.github.15balloon:composecolorpicker:1.0.0")
}
```

## 사용 예시

```kotlin
import com.l5balloon.composecolorpicker.ColorPicker

@Composable
fun MyColorPickerScreen() {
    var color by remember { mutableStateOf(Color.Red) }
    ColorPicker(
        initialColor = color,
        onColorChanged = { color = it }
    )
}
```

## 라이선스
MIT 