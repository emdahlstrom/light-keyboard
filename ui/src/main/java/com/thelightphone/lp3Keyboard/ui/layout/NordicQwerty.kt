package com.thelightphone.lp3Keyboard.ui.layout

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thelightphone.lp3Keyboard.ui.DefaultRow
import com.thelightphone.lp3Keyboard.ui.FinalRow
import com.thelightphone.lp3Keyboard.ui.FirstRow
import com.thelightphone.lp3Keyboard.ui.ICON_KEY_WIDTH_DP
import com.thelightphone.lp3Keyboard.ui.IconKey
import com.thelightphone.lp3Keyboard.ui.Key
import com.thelightphone.lp3Keyboard.ui.KeyboardOptions
import com.thelightphone.lp3Keyboard.ui.Lp3KeyboardCallback
import com.thelightphone.lp3Keyboard.ui.MultiLabelKey
import com.thelightphone.lp3Keyboard.ui.NARROW_KEY_WIDTH_DP
import com.thelightphone.lp3Keyboard.ui.R
import com.thelightphone.lp3Keyboard.ui.SecondRow
import com.thelightphone.lp3Keyboard.ui.SpecialKey

/**
 * The Nordic QWERTY key grid, shared by the Swedish and Finnish keyboards.
 *
 * Sweden and Finland type on the same letter arrangement: å closes the top row and ö ä close
 * the home row, giving two 11-key rows over a 7-key row. That holds for the national desktop
 * standards (SFS 5966 in Finland), for the AOSP/Gboard "nordic" soft keyboard, and for iOS —
 * so the geometry lives here once and each locale only supplies its own [SwipeConfig] and its
 * own long-press sets.
 *
 * Danish (`…klæø`) and Norwegian (`…kløæ`) share this top and bottom row and differ only in
 * those last two home-row keys, so they would fit here once the home row becomes a parameter —
 * along with the alphabet below, which is not the one they collate.
 */
object NordicQwerty {
    const val TOP_ROW = "qwertyuiopå"
    const val HOME_ROW = "asdfghjklöä"
    const val BOTTOM_ROW = "zxcvbnm"

    const val TOP_ROW_UPPER = "QWERTYUIOPÅ"
    const val HOME_ROW_UPPER = "ASDFGHJKLÖÄ"
    const val BOTTOM_ROW_UPPER = "ZXCVBNM"

    /**
     * The Swedish and Finnish alphabet a swipe decoder is fed, in collation order. It is not
     * pan-Nordic: Danish and Norwegian close the alphabet with æ ø å instead.
     */
    const val SWEDISH_FINNISH_ALPHABET = "abcdefghijklmnopqrstuvwxyzåäö"

    /** Lower case, with the shift key idle. */
    fun lowerCaseLayout(swipeConfig: SwipeConfig): Layout = NordicLayout(
        swipeConfig = swipeConfig,
        topRow = TOP_ROW,
        homeRow = HOME_ROW,
        bottomRow = BOTTOM_ROW,
        shiftIcon = R.drawable.up_lp3,
        shiftKey = SpecialKey.UpCase,
        shiftIconModifier = Modifier.padding(12.dp).padding(bottom = 6.dp, end = 8.dp)
    )

    /** Upper case for a single letter; the shift key reads as "tap to go back down". */
    fun upperCaseLayout(swipeConfig: SwipeConfig): Layout = NordicLayout(
        swipeConfig = swipeConfig,
        topRow = TOP_ROW_UPPER,
        homeRow = HOME_ROW_UPPER,
        bottomRow = BOTTOM_ROW_UPPER,
        shiftIcon = R.drawable.down_lp3,
        shiftKey = SpecialKey.DownCase,
        shiftIconModifier = Modifier.padding(12.dp).padding(bottom = 6.dp, end = 8.dp)
    )

    /** Caps lock, which differs from [upperCaseLayout] only in the shift key's icon. */
    fun capsLockedLayout(swipeConfig: SwipeConfig): Layout = NordicLayout(
        swipeConfig = swipeConfig,
        topRow = TOP_ROW_UPPER,
        homeRow = HOME_ROW_UPPER,
        bottomRow = BOTTOM_ROW_UPPER,
        shiftIcon = R.drawable.caps_lp3,
        shiftKey = SpecialKey.DownCase,
        shiftIconModifier = Modifier.padding(9.dp).padding(bottom = 2.dp, end = 4.dp)
    )
}

private class NordicLayout(
    override val swipeConfig: SwipeConfig,
    private val topRow: String,
    private val homeRow: String,
    private val bottomRow: String,
    @DrawableRes private val shiftIcon: Int,
    private val shiftKey: SpecialKey,
    private val shiftIconModifier: Modifier,
) : Layout {
    override val isRootLayout: Boolean
        get() = true

    @Composable
    override fun ColumnScope.Render(
        options: KeyboardOptions,
        callback: Lp3KeyboardCallback
    ) {
        // FirstRow/SecondRow narrow themselves once a row passes 10 keys, so the 11-key
        // Nordic rows fit a 360dp screen without any extra handling here.
        FirstRow(topRow, callback, swipeConfig, options.enableKeyAnimation)
        SecondRow(homeRow, callback, swipeConfig, options.enableKeyAnimation)
        NordicThirdRow(bottomRow, callback, swipeConfig, options) {
            IconKey(
                shiftIcon,
                shiftKey,
                callback,
                options.enableKeyAnimation,
                width = ICON_KEY_WIDTH_DP.dp,
                modifier = shiftIconModifier
            )
        }
        FinalRow(options, callback) {
            MultiLabelKey("123", SpecialKey.Numbers, callback, options.enableKeyAnimation)
        }
    }
}

/**
 * The shared [com.thelightphone.lp3Keyboard.ui.ThirdRow] sizes its letters at the standard
 * pitch, which would leave the bottom row out of step with the narrowed 11-key rows above it.
 * This one matches them, so the grid keeps one rhythm.
 */
@Composable
private fun ColumnScope.NordicThirdRow(
    characters: String,
    callback: Lp3KeyboardCallback,
    swipeConfig: SwipeConfig?,
    options: KeyboardOptions,
    leftButton: @Composable RowScope.() -> Unit
) {
    DefaultRow {
        leftButton()
        for (char in characters) {
            Key(
                char.code,
                callback,
                swipeConfig,
                options.enableKeyAnimation,
                width = NARROW_KEY_WIDTH_DP.dp
            )
        }
        IconKey(
            R.drawable.back_lp3,
            SpecialKey.Backspace,
            callback,
            options.enableKeyAnimation,
            width = ICON_KEY_WIDTH_DP.dp,
            modifier = Modifier.padding(10.dp).padding(start = 8.dp, bottom = 6.dp)
        )
    }
}
