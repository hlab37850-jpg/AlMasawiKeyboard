package com.almasawi.keyboard.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.media.AudioManager
import android.util.AttributeSet
import android.util.SparseArray
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import com.almasawi.keyboard.layouts.Key
import com.almasawi.keyboard.layouts.KeyType
import com.almasawi.keyboard.layouts.KeyboardLayout
import com.almasawi.keyboard.themes.KeyboardTheme
import com.almasawi.keyboard.themes.ThemeCatalog
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Canvas-drawn keyboard. Handles multi-touch, key repeat on backspace, long-press alternates,
 * and horizontal space-bar dragging for cursor movement. Contains no text logic: all actions go
 * to [Listener].
 */
class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    interface Listener {
        fun onKey(key: Key)
        fun onText(text: String)
        fun onSpaceCursor(delta: Int)
    }

    private class Cell(val key: Key, val rect: RectF)

    private class Touch(var cell: Cell, val downX: Float) {
        var anchorX = downX
        var moved = false
        var popup: List<String> = emptyList()
        var popupIndex = 0
        val popupRect = RectF()
        var popupBoxW = 0f
        var longPress: Runnable? = null
        var repeat: Runnable? = null
    }

    private var cells: List<List<Cell>> = emptyList()
    private val touches = SparseArray<Touch>()
    private val density = resources.displayMetrics.density
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val path = Path()

    var listener: Listener? = null

    var keyboardTheme: KeyboardTheme = ThemeCatalog.default
        set(value) { field = value; invalidate() }

    var layout: KeyboardLayout? = null
        set(value) {
            if (field === value) return
            field = value
            cancelAllTouches()
            requestLayout()
            rebuild()
            invalidate()
        }

    var shiftState: ShiftState = ShiftState.OFF
        set(value) { if (field != value) { field = value; invalidate() } }

    var enterLabel: String = ""
        set(value) { if (field != value) { field = value; invalidate() } }

    var heightScale: Float = 1f
        set(value) {
            if (field == value) return
            field = value
            requestLayout(); rebuild(); invalidate()
        }

    var hapticEnabled = true
    var soundEnabled = false

    private fun dp(v: Float) = v * density

    private fun rowHeight(): Float {
        val land = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        return dp(52f) * heightScale * (if (land) 0.8f else 1f)
    }

    private val padTop get() = dp(6f)
    private val padSide get() = dp(3f)
    private val gap get() = dp(5f)

    private val isShifted: Boolean
        get() = shiftState != ShiftState.OFF && layout?.id?.isLetters == true

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val rows = layout?.rows?.size ?: 4
        val h = rows * rowHeight() + padTop + dp(4f)
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), h.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuild()
    }

    private fun rebuild() {
        val l = layout
        val w = width.toFloat()
        if (l == null || w <= 0f) { cells = emptyList(); return }
        val rowH = rowHeight()
        val maxWeight = l.rows.maxOf { row -> row.sumOf { it.weight.toDouble() } }.toFloat()
        val unit = (w - 2 * padSide) / maxWeight
        cells = l.rows.mapIndexed { ri, row ->
            val rowW = row.sumOf { it.weight.toDouble() }.toFloat() * unit
            var x = padSide + ((w - 2 * padSide) - rowW) / 2f
            val top = padTop + ri * rowH
            row.map { key ->
                val kw = key.weight * unit
                var left = x + gap / 2
                var right = x + kw - gap / 2
                x += kw
                if (l.isRtl) { val nl = w - right; right = w - left; left = nl }
                Cell(key, RectF(left, top + gap / 2, right, top + rowH - gap / 2))
            }
        }
    }

    // ---------------------------------------------------------------- drawing

    override fun onDraw(canvas: Canvas) {
        val t = keyboardTheme
        canvas.drawColor(t.background)
        val radius = dp(7f)
        for (row in cells) for (cell in row) drawKey(canvas, cell, t, radius)
        for (i in 0 until touches.size()) {
            val touch = touches.valueAt(i)
            if (touch.popup.isNotEmpty()) drawPopup(canvas, touch, t)
        }
    }

    private fun isPressed(cell: Cell): Boolean {
        for (i in 0 until touches.size()) if (touches.valueAt(i).cell === cell) return true
        return false
    }

    private fun drawKey(canvas: Canvas, cell: Cell, t: KeyboardTheme, radius: Float) {
        val k = cell.key
        val pressed = isPressed(cell)
        val capsOn = k.type == KeyType.SHIFT && shiftState == ShiftState.CAPS
        val isFunction = k.type != KeyType.CHAR && k.type != KeyType.SPACE
        val fill = when {
            k.type == KeyType.ENTER || capsOn -> t.accent
            pressed -> t.keyPressed
            isFunction -> t.functionKey
            else -> t.key
        }
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = fill
        canvas.drawRoundRect(cell.rect, radius, radius, fillPaint)
        if (pressed && fill == t.accent) {
            fillPaint.color = 0x33000000
            canvas.drawRoundRect(cell.rect, radius, radius, fillPaint)
        }
        if (k.type == KeyType.SHIFT && shiftState == ShiftState.ONCE) {
            strokePaint.color = t.accent
            strokePaint.strokeWidth = dp(2f)
            canvas.drawRoundRect(cell.rect, radius, radius, strokePaint)
        }

        val fg = if (k.type == KeyType.ENTER || capsOn) t.onAccent else t.text
        val rowH = rowHeight()
        val cx = cell.rect.centerX()
        val cy = cell.rect.centerY()
        val s = min(cell.rect.width(), cell.rect.height()) * 0.2f

        when {
            k.type == KeyType.SHIFT -> drawShiftIcon(canvas, cx, cy, s, fg)
            k.type == KeyType.DELETE -> drawDeleteIcon(canvas, cx, cy, s, fg)
            k.type == KeyType.ENTER && enterLabel.isEmpty() -> drawEnterIcon(canvas, cx, cy, s, fg)
            else -> {
                val label = when {
                    k.type == KeyType.ENTER -> enterLabel
                    k.type == KeyType.CHAR -> k.labelFor(isShifted)
                    else -> k.label
                }
                textPaint.color = if (k.type == KeyType.SPACE) t.hint else fg
                textPaint.textSize = when (k.type) {
                    KeyType.CHAR -> rowH * 0.38f
                    KeyType.SPACE -> rowH * 0.26f
                    else -> rowH * 0.28f
                }
                val fm = textPaint.fontMetrics
                canvas.drawText(label, cx, cy - (fm.ascent + fm.descent) / 2f, textPaint)
            }
        }
    }

    private fun setupStroke(color: Int) {
        strokePaint.color = color
        strokePaint.strokeWidth = dp(1.8f)
    }

    private fun drawShiftIcon(c: Canvas, cx: Float, cy: Float, s: Float, color: Int) {
        path.reset()
        path.moveTo(cx, cy - s)
        path.lineTo(cx + s, cy)
        path.lineTo(cx + s * 0.45f, cy)
        path.lineTo(cx + s * 0.45f, cy + s)
        path.lineTo(cx - s * 0.45f, cy + s)
        path.lineTo(cx - s * 0.45f, cy)
        path.lineTo(cx - s, cy)
        path.close()
        if (shiftState != ShiftState.OFF) {
            fillPaint.style = Paint.Style.FILL
            fillPaint.color = color
            c.drawPath(path, fillPaint)
        } else {
            setupStroke(color)
            c.drawPath(path, strokePaint)
        }
    }

    private fun drawDeleteIcon(c: Canvas, cx: Float, cy: Float, s: Float, color: Int) {
        setupStroke(color)
        path.reset()
        path.moveTo(cx - s * 1.2f, cy)
        path.lineTo(cx - s * 0.5f, cy - s * 0.85f)
        path.lineTo(cx + s * 1.1f, cy - s * 0.85f)
        path.lineTo(cx + s * 1.1f, cy + s * 0.85f)
        path.lineTo(cx - s * 0.5f, cy + s * 0.85f)
        path.close()
        c.drawPath(path, strokePaint)
        c.drawLine(cx - s * 0.2f, cy - s * 0.35f, cx + s * 0.6f, cy + s * 0.35f, strokePaint)
        c.drawLine(cx - s * 0.2f, cy + s * 0.35f, cx + s * 0.6f, cy - s * 0.35f, strokePaint)
    }

    private fun drawEnterIcon(c: Canvas, cx: Float, cy: Float, s: Float, color: Int) {
        setupStroke(color)
        c.drawLine(cx + s, cy - s, cx + s, cy + s * 0.2f, strokePaint)
        c.drawLine(cx + s, cy + s * 0.2f, cx - s, cy + s * 0.2f, strokePaint)
        c.drawLine(cx - s, cy + s * 0.2f, cx - s * 0.35f, cy - s * 0.4f, strokePaint)
        c.drawLine(cx - s, cy + s * 0.2f, cx - s * 0.35f, cy + s * 0.8f, strokePaint)
    }

    private fun drawPopup(canvas: Canvas, touch: Touch, t: KeyboardTheme) {
        val r = touch.popupRect
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = t.functionKey
        canvas.drawRoundRect(r, dp(10f), dp(10f), fillPaint)
        strokePaint.color = t.accent
        strokePaint.strokeWidth = dp(1.5f)
        canvas.drawRoundRect(r, dp(10f), dp(10f), strokePaint)
        textPaint.textSize = rowHeight() * 0.4f
        val fm = textPaint.fontMetrics
        touch.popup.forEachIndexed { i, s ->
            val box = RectF(r.left + i * touch.popupBoxW, r.top, r.left + (i + 1) * touch.popupBoxW, r.bottom)
            val selected = i == touch.popupIndex
            if (selected) {
                fillPaint.color = t.accent
                canvas.drawRoundRect(
                    box.left + dp(3f), box.top + dp(3f), box.right - dp(3f), box.bottom - dp(3f),
                    dp(8f), dp(8f), fillPaint,
                )
            }
            textPaint.color = if (selected) t.onAccent else t.text
            canvas.drawText(s, box.centerX(), box.centerY() - (fm.ascent + fm.descent) / 2f, textPaint)
        }
    }

    // ---------------------------------------------------------------- touch

    private fun findCell(x: Float, y: Float): Cell? {
        if (cells.isEmpty()) return null
        val ri = ((y - padTop) / rowHeight()).toInt().coerceIn(0, cells.size - 1)
        return cells[ri].minByOrNull { c ->
            max(0f, max(c.rect.left - x, x - c.rect.right))
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val i = e.actionIndex
                onDown(e.getPointerId(i), e.getX(i), e.getY(i))
            }
            MotionEvent.ACTION_MOVE ->
                for (i in 0 until e.pointerCount) onMove(e.getPointerId(i), e.getX(i), e.getY(i))
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP ->
                onUp(e.getPointerId(e.actionIndex))
            MotionEvent.ACTION_CANCEL -> cancelAllTouches()
        }
        return true
    }

    private fun feedback(key: Key) {
        if (hapticEnabled) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        if (soundEnabled) {
            val fx = when (key.type) {
                KeyType.SPACE -> AudioManager.FX_KEYPRESS_SPACEBAR
                KeyType.DELETE -> AudioManager.FX_KEYPRESS_DELETE
                KeyType.ENTER -> AudioManager.FX_KEYPRESS_RETURN
                else -> AudioManager.FX_KEYPRESS_STANDARD
            }
            audio.playSoundEffect(fx, 0.5f)
        }
    }

    private fun onDown(id: Int, x: Float, y: Float) {
        val cell = findCell(x, y) ?: return
        val touch = Touch(cell, x)
        touches.put(id, touch)
        val key = cell.key
        feedback(key)
        when {
            key.type == KeyType.DELETE -> {
                listener?.onKey(key)
                val r = object : Runnable {
                    override fun run() {
                        listener?.onKey(key)
                        postDelayed(this, REPEAT_INTERVAL_MS)
                    }
                }
                touch.repeat = r
                postDelayed(r, REPEAT_DELAY_MS)
            }
            key.type == KeyType.CHAR && key.alternatesFor(isShifted).isNotEmpty() -> scheduleLongPress(touch)
        }
        invalidate()
    }

    private fun scheduleLongPress(touch: Touch) {
        touch.longPress?.let { removeCallbacks(it) }
        val r = Runnable { showPopup(touch) }
        touch.longPress = r
        postDelayed(r, LONG_PRESS_MS)
    }

    private fun showPopup(touch: Touch) {
        val key = touch.cell.key
        val items = listOf(key.outputFor(isShifted)) + key.alternatesFor(isShifted)
        val cell = touch.cell.rect
        val boxW = max(cell.width(), dp(42f))
        val total = boxW * items.size
        val extendLeft = cell.centerX() > width / 2f
        val left = if (extendLeft) max(0f, cell.right - total) else min(cell.left, max(0f, width - total))
        val boxH = rowHeight() * 1.05f
        val top = max(0f, cell.top - boxH - dp(4f))
        touch.popup = if (extendLeft) items.reversed() else items
        touch.popupBoxW = boxW
        touch.popupRect.set(left, top, left + total, top + boxH)
        touch.popupIndex = popupIndexAt(touch, touch.anchorX)
        invalidate()
    }

    private fun popupIndexAt(t: Touch, x: Float): Int =
        ((x - t.popupRect.left) / t.popupBoxW).toInt().coerceIn(0, t.popup.size - 1)

    private fun onMove(id: Int, x: Float, y: Float) {
        val t = touches.get(id) ?: return
        val key = t.cell.key
        when {
            t.popup.isNotEmpty() -> {
                val idx = popupIndexAt(t, x)
                if (idx != t.popupIndex) { t.popupIndex = idx; invalidate() }
            }
            key.type == KeyType.SPACE -> {
                if (!t.moved) {
                    if (abs(x - t.downX) < dp(16f)) return
                    t.moved = true
                    t.anchorX = x
                }
                val step = dp(14f)
                val steps = ((x - t.anchorX) / step).toInt()
                if (steps != 0) {
                    listener?.onSpaceCursor(steps)
                    t.anchorX += steps * step
                }
            }
            key.type == KeyType.CHAR -> {
                val next = findCell(x, y)
                if (next != null && next !== t.cell && next.key.type == KeyType.CHAR) {
                    t.cell = next
                    t.longPress?.let { removeCallbacks(it) }
                    if (next.key.alternatesFor(isShifted).isNotEmpty()) scheduleLongPress(t)
                    invalidate()
                }
            }
        }
    }

    private fun onUp(id: Int) {
        val t = touches.get(id) ?: return
        cancelCallbacks(t)
        touches.remove(id)
        val key = t.cell.key
        when {
            t.popup.isNotEmpty() -> listener?.onText(t.popup[t.popupIndex])
            key.type == KeyType.DELETE -> Unit
            key.type == KeyType.SPACE && t.moved -> Unit
            else -> listener?.onKey(key)
        }
        invalidate()
    }

    private fun cancelCallbacks(t: Touch) {
        t.longPress?.let { removeCallbacks(it) }
        t.repeat?.let { removeCallbacks(it) }
        t.longPress = null
        t.repeat = null
    }

    private fun cancelAllTouches() {
        for (i in 0 until touches.size()) cancelCallbacks(touches.valueAt(i))
        touches.clear()
    }

    override fun onDetachedFromWindow() {
        cancelAllTouches()
        super.onDetachedFromWindow()
    }

    private companion object {
        const val LONG_PRESS_MS = 350L
        const val REPEAT_DELAY_MS = 400L
        const val REPEAT_INTERVAL_MS = 45L
    }
}
