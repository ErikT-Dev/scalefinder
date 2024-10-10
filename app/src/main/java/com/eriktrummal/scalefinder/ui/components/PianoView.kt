package com.eriktrummal.scalefinder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.graphics.Paint as NativePaint
import android.graphics.LinearGradient
import android.graphics.Shader
import com.eriktrummal.scalefinder.R

@Composable
fun PianoView(
    selectedNotes: List<Int>,
    noteNames: List<String>,
    rootNote: Int?,
    modifier: Modifier = Modifier,
    chordNotes: List<Int> = listOf(),
    whiteKeyHeight: Dp = 120.dp,
    whiteKeyGradientTop: Color = colorResource(id = R.color.white_key_gradient_top),
    whiteKeyGradientBottom: Color = colorResource(id = R.color.white_key_gradient_bottom),
    blackKeyGradientLeft: Color = colorResource(id = R.color.black_key_gradient_left),
    blackKeyGradientRight: Color = colorResource(id = R.color.black_key_gradient_right),
    selectedWhiteKeyTint: Color = colorResource(id = R.color.selected_white_key_tint),
    selectedBlackKeyTint: Color = colorResource(id = R.color.selected_black_key_tint),
    rootWhiteKeyTint: Color = colorResource(id = R.color.root_white_key_tint),
    rootBlackKeyTint: Color = colorResource(id = R.color.root_black_key_tint),
    whiteChordKeyColor: Color = colorResource(id = R.color.white_chord_note_key_tint),
    blackChordKeyColor: Color = colorResource(id = R.color.black_chord_note_key_tint),
    borderColor: Color = colorResource(id = R.color.piano_border),
    textColor: Color = Color.Black,
    fontSize: Float = 24f,
    shadowColor: Color = colorResource(id = R.color.key_shadow),
    hideShadows: Boolean = false,
    potentialMatchCounts: List<Int>? = null,
    onNoteSelected: ((Int) -> Unit)? = null,
    onNoteLongPress: ((Int) -> Unit)? = null
) {
    val whiteNotes = listOf(1, 3, 5, 6, 8, 10, 12)
    val blackNotes = listOf(2, 4, 7, 9, 11)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(whiteKeyHeight)
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .then(
                if (onNoteSelected != null || onNoteLongPress != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                onNoteSelected?.let {
                                    handleTap(offset, size.width, size.height, whiteNotes, blackNotes, it)
                                }
                            },
                            onLongPress = { offset ->
                                onNoteLongPress?.let {
                                    handleTap(offset, size.width, size.height, whiteNotes, blackNotes, it)
                                }
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val keyWidth = canvasWidth / 7
            val keyHeight = canvasHeight
            val blackKeyWidth = keyWidth * 0.6f
            val blackKeyHeight = keyHeight * 0.6f
            val shadowOffsetX = whiteKeyHeight.value/12f
            val shadowOffsetY = whiteKeyHeight.value/40f
            val shadowRadius = whiteKeyHeight.value/12f

            val textPaint = NativePaint().apply {
                color = textColor.toArgb()
                textAlign = NativePaint.Align.CENTER
                textSize = fontSize
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            // Function to determine the text to display on a key
            fun getKeyText(note: Int): String {
                return when {
                    note in selectedNotes || note == rootNote -> noteNames.getOrNull(selectedNotes.indexOf(note)) ?: ""
                    potentialMatchCounts != null -> potentialMatchCounts[note - 1].toString()
                    else -> ""
                }
            }

            // Draw white keys
            whiteNotes.forEachIndexed { index, note ->
                val isSelected = note in selectedNotes
                val isChordNote = note in chordNotes
                val isRoot = note == rootNote
                val left = index * keyWidth
                val top = 0f
                val right = left + keyWidth
                val bottom = keyHeight

                drawIntoCanvas { canvas ->
                    if (!hideShadows) {
                        canvas.nativeCanvas.save()
                        canvas.nativeCanvas.translate(shadowOffsetX, shadowOffsetY)
                        canvas.nativeCanvas.drawRect(
                            left, top, right, bottom,
                            NativePaint().apply {
                                color = shadowColor.toArgb()
                                maskFilter = android.graphics.BlurMaskFilter(shadowRadius, android.graphics.BlurMaskFilter.Blur.NORMAL)
                            }
                        )
                        canvas.nativeCanvas.restore()
                    }
                    val whiteKeyGradient = LinearGradient(
                        left, top, left, bottom,
                        whiteKeyGradientTop.toArgb(), whiteKeyGradientBottom.toArgb(),
                        Shader.TileMode.CLAMP
                    )
                    val keyPaint = NativePaint().apply {
                        shader = whiteKeyGradient
                    }
                    canvas.nativeCanvas.drawRect(left, top, right, bottom, keyPaint)
                    if (isChordNote || isSelected || isRoot) {
                        canvas.nativeCanvas.drawRect(
                            left, top, right, bottom,
                            NativePaint().apply {
                                color = (if (isChordNote) whiteChordKeyColor else if (isRoot) rootWhiteKeyTint else selectedWhiteKeyTint).toArgb()
                                style = NativePaint.Style.FILL
                            }
                        )
                    }
                    canvas.nativeCanvas.drawRect(
                        left, top, right, bottom,
                        NativePaint().apply {
                            color = borderColor.toArgb()
                            style = NativePaint.Style.STROKE
                            strokeWidth = 2f
                        }
                    )
                    val text = getKeyText(note)
                    if (text.isNotEmpty()) {
                        canvas.nativeCanvas.drawText(
                            text,
                            left + keyWidth / 2,
                            bottom - 20f,
                            textPaint
                        )
                    }
                }
            }

            // Draw black keys
            blackNotes.forEachIndexed { index, note ->
                val isSelected = note in selectedNotes
                val isChordNote = note in chordNotes
                val isRoot = note == rootNote
                val xOffset = when (index) {
                    0 -> keyWidth * 0.6f
                    1 -> keyWidth * 1.8f
                    2 -> keyWidth * 3.6f
                    3 -> keyWidth * 4.7f
                    4 -> keyWidth * 5.8f
                    else -> 0f
                }
                val left = xOffset
                val top = 0f
                val right = left + blackKeyWidth
                val bottom = blackKeyHeight

                drawIntoCanvas { canvas ->
                    if (!hideShadows) {
                        canvas.nativeCanvas.save()
                        canvas.nativeCanvas.translate(shadowOffsetX, shadowOffsetY)
                        canvas.nativeCanvas.drawRect(
                            left, top, right, bottom,
                            NativePaint().apply {
                                color = shadowColor.toArgb()
                                maskFilter = android.graphics.BlurMaskFilter(shadowRadius, android.graphics.BlurMaskFilter.Blur.NORMAL)
                            }
                        )
                        canvas.nativeCanvas.restore()
                    }
                    val blackKeyGradient = android.graphics.LinearGradient(
                        left, 0f, right, 0f,
                        blackKeyGradientLeft.toArgb(), blackKeyGradientRight.toArgb(),
                        android.graphics.Shader.TileMode.CLAMP
                    )
                    val keyPaint = NativePaint().apply {
                        shader = blackKeyGradient
                    }
                    canvas.nativeCanvas.drawRect(left, top, right, bottom, keyPaint)

                    if (isChordNote || isSelected || isRoot) {
                        canvas.nativeCanvas.drawRect(
                            left, top, right, bottom,
                            NativePaint().apply {
                                color = (if (isChordNote) blackChordKeyColor else if (isRoot) rootBlackKeyTint else selectedBlackKeyTint).toArgb()
                                style = NativePaint.Style.FILL
                            }
                        )
                    }
                    val text = getKeyText(note)
                    if (text.isNotEmpty()) {
                        canvas.nativeCanvas.drawText(
                            text,
                            left + blackKeyWidth / 2,
                            bottom - 20f,
                            textPaint.apply { color = Color.White.toArgb() }
                        )
                    }
                }
            }
        }
    }
}

private fun handleTap(
    offset: Offset,
    width: Int,
    height: Int,
    whiteNotes: List<Int>,
    blackNotes: List<Int>,
    onNoteAction: (Int) -> Unit
) {
    val keyWidth = width / 7
    val keyHeight = height
    val blackKeyWidth = keyWidth * 0.6f
    val blackKeyHeight = keyHeight * 0.6f

    val blackKeyIndex = blackNotes.indexOfFirst { note ->
        val index = blackNotes.indexOf(note)
        val xOffset = when (index) {
            0 -> keyWidth * 0.6f
            1 -> keyWidth * 1.8f
            2 -> keyWidth * 3.6f
            3 -> keyWidth * 4.7f
            4 -> keyWidth * 5.8f
            else -> 0f
        }
        offset.x >= xOffset &&
                offset.x <= xOffset + blackKeyWidth &&
                offset.y <= blackKeyHeight
    }

    if (blackKeyIndex != -1) {
        onNoteAction(blackNotes[blackKeyIndex])
    } else {
        val whiteKeyIndex = (offset.x / keyWidth).toInt()
        if (whiteKeyIndex in 0..6 && offset.y <= keyHeight) {
            onNoteAction(whiteNotes[whiteKeyIndex])
        }
    }
}