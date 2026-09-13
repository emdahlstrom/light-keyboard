package com.thelightphone.lp3Keyboard.ui.layout

import androidx.compose.ui.geometry.Rect
import com.thelightphone.lp3Keyboard.ui.Lp3KeyboardLayoutCapture

private val SvQwertySwipeConfig: SwipeConfig by lazy {
    object : Lp3KeyboardLayoutCapture(NordicQwerty.SWEDISH_FINNISH_ALPHABET) {
        override fun report(code: Int, bounds: Rect) {
            val lower = code.toChar().lowercaseChar()
            if (lower !in letters) return
            // onGloballyPositioned fires on every layout pass; skip identical
            // writes so we don't churn the snapshot or re-fire boundsFlow.
            if (letterBounds[lower.code] == bounds) return
            letterBounds[lower.code] = bounds
        }
    }
}

/**
 * The layouts for Swedish QWERTY.
 *
 * The key grid is [NordicQwerty], which Finnish shares; long presses use the default
 * [EnShared.extendedCharMapping].
 */
object SvQwerty {
    val LowerCaseLayout: Layout = NordicQwerty.lowerCaseLayout(SvQwertySwipeConfig)
    val UpperCaseLayout: Layout = NordicQwerty.upperCaseLayout(SvQwertySwipeConfig)
    val CapsLockedLayout: Layout = NordicQwerty.capsLockedLayout(SvQwertySwipeConfig)
}
