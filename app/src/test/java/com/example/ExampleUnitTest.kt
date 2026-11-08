package com.example

import com.example.model.IconThemeStyle
import com.example.model.LauncherSettings
import com.example.model.WidgetIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testWidgetIds_allKnownWidgetsPresent() {
    val order = WidgetIds.DEFAULT_ORDER
    assertTrue(order.contains(WidgetIds.CLOCK))
    assertTrue(order.contains(WidgetIds.WEATHER))
    assertTrue(order.contains(WidgetIds.STEPS))
    assertTrue(order.contains(WidgetIds.MUSIC))
    assertTrue(order.contains(WidgetIds.BATTERY))
    assertTrue(order.contains(WidgetIds.QUICK_TOGGLES))
    assertTrue(order.contains(WidgetIds.QUICK_NOTES))
  }

  @Test
  fun testIconThemeStyles() {
    val styles = IconThemeStyle.values()
    assertEquals(4, styles.size)
    assertEquals(IconThemeStyle.LIQUID_GLASS, IconThemeStyle.valueOf("LIQUID_GLASS"))
    assertEquals(IconThemeStyle.FROSTED_PEBBLE, IconThemeStyle.valueOf("FROSTED_PEBBLE"))
    assertEquals(IconThemeStyle.TINTED_NEON, IconThemeStyle.valueOf("TINTED_NEON"))
    assertEquals(IconThemeStyle.ORIGINAL, IconThemeStyle.valueOf("ORIGINAL"))
  }

  @Test
  fun testLauncherSettings_defaults() {
    val defaults = LauncherSettings()
    assertEquals(0.65f, defaults.glassBendCurvature, 0.01f)
    assertEquals(3, defaults.glassBlurIntensity)
    assertEquals(8000, defaults.dailyStepGoal)
    assertEquals("Aurora City", defaults.weatherCity)
    assertTrue(defaults.enabledWidgets.contains(WidgetIds.STEPS))
    assertTrue(defaults.enabledWidgets.contains(WidgetIds.WEATHER))
  }
}
