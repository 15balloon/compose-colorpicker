[![](https://jitpack.io/v/15balloon/compose-colorpicker.svg)](https://jitpack.io/#15balloon/compose-colorpicker)

# ComposeColorPicker

A modular color picker library for Jetpack Compose.

## Features
- All-in-one `ColorPicker` composable for easy use
- Also provides ColorWheel, BrightnessSlider, AlphaSlider, RGBSliders, HexInput as independent composables
- Compose 1.5+ support
- Highly customizable and composable

## Installation

### Gradle
Add the JitPack repository to your settings.gradle.kts
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your build.gradle.kts
```kotlin
dependencies {
    implementation("com.github.15balloon:compose-colorpicker:1.1.0")
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
        onColorChanged = { color = it },
        useAlpha: Boolean = true,
        showAlphaSlider: Boolean = true,
        showRGBSliders: Boolean = true,
        showHexCode: Boolean = true,
        useHexInput: Boolean = true,
    )
}
```

## License
MIT

---

# 컴포즈 색상 선택 UI

Jetpack Compose용 컬러 피커(Color Picker) 라이브러리입니다.

## 특징
- 간편하게 사용할 수 있는 통합형 `ColorPicker` 컴포저블 제공
- ColorWheel, BrightnessSlider, AlphaSlider, RGBSliders, HexInput 등 개별 컴포저블도 제공
- Compose 1.5+ 지원
- 원하는 컴포저블 조합으로 UI 커스터마이징

## 설치

### Gradle
settings.gradle.kts에 다음과 같이 JitPack 레포지토리 추가
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

build.gradle.kts에 다음과 같이 의존성 추가
```kotlin
dependencies {
    implementation("com.github.15balloon:compose-colorpicker:1.1.0")
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
        onColorChanged = { color = it },
        useAlpha: Boolean = true,
        showAlphaSlider: Boolean = true,
        showRGBSliders: Boolean = true,
        showHexCode: Boolean = true,
        useHexInput: Boolean = true,
    )
}
```

## 라이선스
MIT