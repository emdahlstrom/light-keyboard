package com.thelightphone.lp3Keyboard.ui.layout

import androidx.compose.ui.geometry.Rect
import com.thelightphone.lp3Keyboard.ui.Lp3KeyboardLayoutCapture

private val FiQwertySwipeConfig: SwipeConfig by lazy {
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
 * The layouts for Finnish QWERTY.
 *
 * Finland and Sweden type on the same key grid, so this reuses [NordicQwerty] rather than
 * restating it. What is Finnish here is [extendedCharMapping]: š and ž are letters of the
 * Finnish alphabet (šakki, tšekki, džonkki) rather than foreign accents, so they lead their
 * long-press menus, and the menus drop the å/ä/ö that already have keys of their own.
 */
object FiQwerty {
    val LowerCaseLayout: Layout = NordicQwerty.lowerCaseLayout(FiQwertySwipeConfig)
    val UpperCaseLayout: Layout = NordicQwerty.upperCaseLayout(FiQwertySwipeConfig)
    val CapsLockedLayout: Layout = NordicQwerty.capsLockedLayout(FiQwertySwipeConfig)

    /**
     * Long-press menus for Finnish, overlaid on [EnShared.extendedCharMapping].
     *
     * The overridden keys follow CLDR's Finnish Android layout (fi-t-k0-android); letters it
     * leaves alone keep the shared defaults, which cost nothing and help with names.
     */
    val extendedCharMapping: Map<Int, List<List<Char>>> = EnShared.extendedCharMapping + mapOf(
        // å has its own key, so it is not repeated here
        'A'.code to listOf(listOf('Æ', 'À', 'Á', 'Â', 'Ã', 'Ā')),
        'a'.code to listOf(listOf('æ', 'à', 'á', 'â', 'ã', 'ā')),
        // ö likewise
        'O'.code to listOf(listOf('Ø', 'Ô', 'Ò', 'Ó', 'Õ', 'Œ', 'Ō')),
        'o'.code to listOf(listOf('ø', 'ô', 'ò', 'ó', 'õ', 'œ', 'ō')),
        // š and ž first: Finnish letters, not accented borrowings
        'S'.code to listOf(listOf('Š', 'ẞ', 'Ś')),
        's'.code to listOf(listOf('š', 'ß', 'ś')),
        'Z'.code to listOf(listOf('Ž', 'Ź', 'Ż')),
        'z'.code to listOf(listOf('ž', 'ź', 'ż')),
        'U'.code to listOf(listOf('Ü', 'Û', 'Ù', 'Ú', 'Ū')),
        'u'.code to listOf(listOf('ü', 'û', 'ù', 'ú', 'ū')),
        // the Nordic keys reach their neighbours' letters, for Danish/Norwegian names
        'Ä'.code to listOf(listOf('Æ')),
        'ä'.code to listOf(listOf('æ')),
        'Ö'.code to listOf(listOf('Ø')),
        'ö'.code to listOf(listOf('ø')),
    )
}
