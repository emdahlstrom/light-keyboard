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
import org.junit.Assert.assertTrue
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
    fun `no swedish popup offers a letter that has its own key`() {
        val offered = SvQwerty.extendedCharMapping.values.flatten().flatten()
        assertTrue(offered.none { it in "åäöÅÄÖ" })
    }

    @Test
    fun `ö and ä long-press to ø and æ`() {
        assertEquals(listOf(listOf('ø', 'œ')), SvQwerty.extendedCharMapping['ö'.code])
        assertEquals(listOf(listOf('Ø', 'Œ')), SvQwerty.extendedCharMapping['Ö'.code])
        assertEquals(listOf(listOf('æ')), SvQwerty.extendedCharMapping['ä'.code])
        assertEquals(listOf(listOf('Æ')), SvQwerty.extendedCharMapping['Ä'.code])
    }

    @Test
    fun `every popup fits the keyboard`() {
        for (mapping in listOf(EnShared.extendedCharMapping, SvQwerty.extendedCharMapping)) {
            for (rows in mapping.values) {
                assertTrue(rows.size in 1..3)
                assertTrue(rows.all { it.size in 1..8 })
            }
        }
    }

    @Test
    fun `long-pressing ö opens a popup on the swedish layout but not on english`() {
        vm.onKeyPressed('ö'.code)
        vm.onKeyLongPressed('ö'.code)
        assertTrue(vm.layoutFlow.value is EnShared.ExtendedCharKeyboard)

        val en = EnQwertyLp3KeyboardViewModel(
            passedCallback = callback,
            swipeCallback = swipeCallback,
        )
        en.onKeyPressed('ö'.code)
        en.onKeyLongPressed('ö'.code)
        assertSame(EnQwerty.LowerCaseLayout, en.layoutFlow.value)
    }
}
