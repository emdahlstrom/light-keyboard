package com.thelightphone.lp3Keyboard.ui

import com.thelightphone.lp3Keyboard.ui.layout.EnQwerty
import com.thelightphone.lp3Keyboard.ui.layout.EnShared
import com.thelightphone.lp3Keyboard.ui.layout.SvQwerty
import com.thelightphone.lp3Keyboard.ui.viewmodel.EnQwertyLp3KeyboardViewModel
import com.thelightphone.lp3Keyboard.ui.viewmodel.Lp3RepeatableKeyboardCallback
import com.thelightphone.lp3Keyboard.ui.viewmodel.SvQwertyLp3KeyboardViewModel
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SvQwertyViewModelTest {

    private val callback = mockk<Lp3RepeatableKeyboardCallback>(relaxed = true)
    private val swipeCallback = mockk<Lp3KeyboardSwipeCallback<Unit>>(relaxed = true)

    private val vm = SvQwertyLp3KeyboardViewModel(
        passedCallback = callback,
        swipeCallback = swipeCallback,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `swedish letters commit through the callback in lowercase`() {
        for (char in "åäö") {
            vm.onKeyPressed(char.code)
            vm.onKeyReleased(char.code)
            verify(exactly = 1) { callback.onKeyReleased(char.code) }
        }
        assertSame(SvQwerty.LowerCaseLayout, vm.layoutFlow.value)
    }

    @Test
    fun `shift swaps to the swedish uppercase layout and reverts after one letter`() {
        vm.onSpecialKeyPressed(SpecialKey.UpCase)
        vm.onSpecialKeyReleased(SpecialKey.UpCase)
        assertSame(SvQwerty.UpperCaseLayout, vm.layoutFlow.value)

        vm.onKeyPressed('Å'.code)
        vm.onKeyReleased('Å'.code)
        verify(exactly = 1) { callback.onKeyReleased('Å'.code) }
        assertSame(SvQwerty.LowerCaseLayout, vm.layoutFlow.value)
    }

    @Test
    fun `long-pressing ö on the swedish layout offers ø and œ`() {
        vm.onKeyPressed('ö'.code)
        vm.onKeyLongPressed('ö'.code)
        val layout = vm.layoutFlow.value as EnShared.ExtendedCharKeyboard
        assertEquals(listOf(listOf('ø', 'œ')), layout.rows)
    }

    @Test
    fun `letters with their own swedish keys are left out of the other popups`() {
        val map = SvQwerty.extendedCharMapping
        assertEquals(listOf(listOf('à', 'á', 'â', 'æ'), listOf('ã', 'ā', 'ă', 'ą')), map['a'.code])
        assertEquals(listOf(listOf('À', 'Á', 'Â', 'Æ'), listOf('Ã', 'Ā', 'Ă', 'Ą')), map['A'.code])
        assertEquals(listOf(listOf('ô', 'ò', 'ó', 'œ', 'ø', 'ō', 'õ')), map['o'.code])
    }

    @Test
    fun `a popup opened while another popup is showing returns to the letters after a pick`() {
        vm.onKeyPressed('o'.code)
        vm.onKeyLongPressed('o'.code)
        vm.onKeyReleased('o'.code)

        vm.onKeyPressed('ö'.code)
        vm.onKeyLongPressed('ö'.code)
        vm.onKeyReleased('ö'.code)
        val nested = vm.layoutFlow.value as EnShared.ExtendedCharKeyboard
        assertEquals(listOf(listOf('ø', 'œ')), nested.rows)

        vm.onKeyPressed('ø'.code)
        vm.onKeyReleased('ø'.code)
        verify(exactly = 1) { callback.onKeyReleased('ø'.code) }
        assertSame(SvQwerty.LowerCaseLayout, vm.layoutFlow.value)
    }

    @Test
    fun `swedish layout still uses the shared map for other letters`() {
        vm.onKeyPressed('e'.code)
        vm.onKeyLongPressed('e'.code)
        val layout = vm.layoutFlow.value as EnShared.ExtendedCharKeyboard
        assertEquals(EnShared.extendedCharMapping['e'.code], layout.rows)
    }

    @Test
    fun `english layout has no long-press set for ö`() {
        val en = EnQwertyLp3KeyboardViewModel(
            passedCallback = callback,
            swipeCallback = swipeCallback,
        )
        en.onKeyPressed('ö'.code)
        en.onKeyLongPressed('ö'.code)
        assertSame(EnQwerty.LowerCaseLayout, en.layoutFlow.value)
    }
}
