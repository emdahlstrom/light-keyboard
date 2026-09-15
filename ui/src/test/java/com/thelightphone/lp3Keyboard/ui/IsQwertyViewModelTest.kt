package com.thelightphone.lp3Keyboard.ui

import com.thelightphone.lp3Keyboard.ui.layout.EnShared
import com.thelightphone.lp3Keyboard.ui.layout.IsQwerty
import com.thelightphone.lp3Keyboard.ui.viewmodel.IsQwertyLp3KeyboardViewModel
import com.thelightphone.lp3Keyboard.ui.viewmodel.Lp3RepeatableKeyboardCallback
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class IsQwertyViewModelTest {

    private val callback = mockk<Lp3RepeatableKeyboardCallback>(relaxed = true)
    private val swipeCallback = mockk<Lp3KeyboardSwipeCallback<Unit>>(relaxed = true)

    private val vm = IsQwertyLp3KeyboardViewModel(
        passedCallback = callback,
        swipeCallback = swipeCallback,
    )

    @Test
    fun `icelandic letters commit through the callback in lowercase`() {
        for (char in "ðæöþ") {
            vm.onKeyPressed(char.code)
            vm.onKeyReleased(char.code)
            verify(exactly = 1) { callback.onKeyReleased(char.code) }
        }
        assertSame(IsQwerty.LowerCaseLayout, vm.layoutFlow.value)
    }

    @Test
    fun `shift swaps to the icelandic uppercase layout and reverts after one letter`() {
        vm.onSpecialKeyPressed(SpecialKey.UpCase)
        vm.onSpecialKeyReleased(SpecialKey.UpCase)
        assertSame(IsQwerty.UpperCaseLayout, vm.layoutFlow.value)

        vm.onKeyPressed('Þ'.code)
        vm.onKeyReleased('Þ'.code)
        verify(exactly = 1) { callback.onKeyReleased('Þ'.code) }
        assertSame(IsQwerty.LowerCaseLayout, vm.layoutFlow.value)
    }

    @Test
    fun `every icelandic accented letter is on a long-press`() {
        val accents = mapOf('a' to 'á', 'e' to 'é', 'i' to 'í', 'o' to 'ó', 'u' to 'ú', 'y' to 'ý')
        for ((base, accented) in accents) {
            val lower = EnShared.extendedCharMapping.getValue(base.code).flatten()
            val upper = EnShared.extendedCharMapping.getValue(base.uppercaseChar().code).flatten()
            assertTrue("$accented missing from $base", accented in lower)
            assertTrue("${accented.uppercaseChar()} missing from ${base.uppercaseChar()}", accented.uppercaseChar() in upper)
        }
    }
}
