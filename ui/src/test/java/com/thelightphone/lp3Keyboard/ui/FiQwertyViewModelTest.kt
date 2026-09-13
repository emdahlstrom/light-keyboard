package com.thelightphone.lp3Keyboard.ui

import com.thelightphone.lp3Keyboard.ui.layout.EnShared
import com.thelightphone.lp3Keyboard.ui.layout.FiQwerty
import com.thelightphone.lp3Keyboard.ui.layout.NordicQwerty
import com.thelightphone.lp3Keyboard.ui.layout.SvQwerty
import com.thelightphone.lp3Keyboard.ui.viewmodel.FiQwertyLp3KeyboardViewModel
import com.thelightphone.lp3Keyboard.ui.viewmodel.Lp3RepeatableKeyboardCallback
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FiQwertyViewModelTest {

    private val callback = mockk<Lp3RepeatableKeyboardCallback>(relaxed = true)
    private val swipeCallback = mockk<Lp3KeyboardSwipeCallback<Unit>>(relaxed = true)

    private val vm = FiQwertyLp3KeyboardViewModel(
        passedCallback = callback,
        swipeCallback = swipeCallback,
    )

    @Before
    fun setUp() {
        // onKeyLongPressed launches a coroutine on viewModelScope, which needs a Main
        // dispatcher installed even for the ones that complete without suspending.
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `finnish letters commit through the callback in lowercase`() {
        for (char in "åäö") {
            vm.onKeyPressed(char.code)
            vm.onKeyReleased(char.code)
            verify(exactly = 1) { callback.onKeyReleased(char.code) }
        }
        assertSame(FiQwerty.LowerCaseLayout, vm.layoutFlow.value)
    }

    @Test
    fun `shift swaps to the finnish uppercase layout and reverts after one letter`() {
        vm.onSpecialKeyPressed(SpecialKey.UpCase)
        vm.onSpecialKeyReleased(SpecialKey.UpCase)
        assertSame(FiQwerty.UpperCaseLayout, vm.layoutFlow.value)

        vm.onKeyPressed('Ä'.code)
        vm.onKeyReleased('Ä'.code)
        verify(exactly = 1) { callback.onKeyReleased('Ä'.code) }
        assertSame(FiQwerty.LowerCaseLayout, vm.layoutFlow.value)
    }

    @Test
    fun `finnish keeps its own layout instances, separate from swedish`() {
        // Same grid, but the two locales must stay distinguishable so caps/layout state and
        // the swipe bounds captured for each keyboard never bleed across.
        assertNotSame(SvQwerty.LowerCaseLayout, FiQwerty.LowerCaseLayout)
        assertNotSame(SvQwerty.UpperCaseLayout, FiQwerty.UpperCaseLayout)
        assertNotSame(SvQwerty.CapsLockedLayout, FiQwerty.CapsLockedLayout)
    }

    @Test
    fun `nordic grid puts a ring-a on the top row and o-e umlauts on the home row`() {
        assertEquals("qwertyuiopå", NordicQwerty.TOP_ROW)
        assertEquals("asdfghjklöä", NordicQwerty.HOME_ROW)
        assertEquals("zxcvbnm", NordicQwerty.BOTTOM_ROW)
        assertEquals(NordicQwerty.TOP_ROW.uppercase(), NordicQwerty.TOP_ROW_UPPER)
        assertEquals(NordicQwerty.HOME_ROW.uppercase(), NordicQwerty.HOME_ROW_UPPER)
        assertEquals(NordicQwerty.BOTTOM_ROW.uppercase(), NordicQwerty.BOTTOM_ROW_UPPER)
    }

    @Test
    fun `long pressing s and z leads with the finnish caron letters`() {
        assertEquals('š', firstExtendedChar('s'))
        assertEquals('Š', firstExtendedChar('S'))
        assertEquals('ž', firstExtendedChar('z'))
        assertEquals('Ž', firstExtendedChar('Z'))
    }

    @Test
    fun `long press menus do not repeat letters that have their own key`() {
        for (root in "aAoO") {
            val offered = FiQwerty.extendedCharMapping.getValue(root.code).flatten()
            for (duplicate in "åäöÅÄÖ") {
                assertFalse(
                    "long-pressing $root should not offer $duplicate, which has its own key",
                    duplicate in offered
                )
            }
        }
    }

    @Test
    fun `long pressing a nordic key reaches its danish and norwegian counterpart`() {
        assertEquals('æ', firstExtendedChar('ä'))
        assertEquals('ø', firstExtendedChar('ö'))
    }

    private fun firstExtendedChar(root: Char): Char {
        vm.onKeyLongPressed(root.code)
        val layout = vm.layoutFlow.value
        assertTrue(
            "long-pressing $root should open an extended char keyboard, got $layout",
            layout is EnShared.ExtendedCharKeyboard
        )
        val first = (layout as EnShared.ExtendedCharKeyboard).rows!!.first().first()
        // leave the keyboard back on letters so the next call starts from a clean state
        vm.onKeyReleased(root.code)
        vm.onSpecialKeyReleased(SpecialKey.Letters)
        return first
    }
}
