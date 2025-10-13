// BoardView.kt
package com.example.reto_3_4 // Asegúrate que el package sea el correcto

import android.content.Context
import android.graphics.Canvas // Lo necesitarás más adelante para onDraw
import android.graphics.Paint // Lo necesitarás más adelante para dibujar líneas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat // Para obtener drawables de forma compatible


class BoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        // Constantes para la vista, si son necesarias públicamente
        // Si solo se usa internamente, pueden ser privadas dentro de la clase o del companion object.
        const val GRID_LINE_WIDTH_DP = 6f // Usar Float para DP y luego convertir a PX
    }

    private var gridLineWidthPx: Float = 0f // Se inicializará en init o en onSizeChanged

    // Paint object para dibujar las líneas del tablero (lo inicializaremos en init)
    private lateinit var gridPaint: Paint

    private var xDrawable: Drawable? = null
    private var oDrawable: Drawable? = null

    // Dimensiones de la celda
    private var cellWidth: Float = 0f
    private var cellHeight: Float = 0f

    //private var mGame: TicTacToeGame? = null

    private var boardState: List<String>? = null

    interface OnCellTouchListener {
        fun onCellTouched(cellIndex: Int)
    }
    private var cellTouchListener: OnCellTouchListener? = null

    fun setOnCellTouchListener(listener: OnCellTouchListener) {
        cellTouchListener = listener
    }

    init {
        // Inicializar el Paint para las líneas del tablero
        gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.grid_lines_color) // Define este color en colors.xml
            style = Paint.Style.STROKE
            strokeWidth = convertDpToPx(GRID_LINE_WIDTH_DP)
        }
        gridLineWidthPx = convertDpToPx(GRID_LINE_WIDTH_DP) // Guardar el valor en Px

        // Cargar los bitmaps (o drawables)
        loadPlayerImages()
    }

    private fun convertDpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    private fun loadPlayerImages() {
        // Opción 2: Cargando como Drawables (más flexible, especialmente para VectorDrawables)
        xDrawable = ContextCompat.getDrawable(context, R.drawable.x_img) // Si usas ic_x.xml
        oDrawable = ContextCompat.getDrawable(context, R.drawable.o_img) // Si usas ic_o.xml
    }

    fun setBoardState(board: List<String>) {
        boardState = board
        // No es necesario llamar a invalidate() aquí,
        // MainActivity lo hará después de actualizar el estado.
    }

    // Necesitarás sobrescribir onDraw() más adelante para hacer el dibujo real
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Dibujar las dos líneas verticales
        // canvas.drawLine(startX, startY, stopX, stopY, paint)
        canvas.drawLine(cellWidth, 0f, cellWidth, height.toFloat(), gridPaint)
        canvas.drawLine(cellWidth * 2, 0f, cellWidth * 2, height.toFloat(), gridPaint)

        // DIBUJAR LAS DOS LÍNEAS HORIZONTALES (¡tu tarea!)
        canvas.drawLine(0f, cellHeight, width.toFloat(), cellHeight, gridPaint)
        canvas.drawLine(0f, cellHeight * 2, width.toFloat(), cellHeight * 2, gridPaint)

        // Dibujar X y O usando Drawables
        val currentBoard = boardState ?: return

        for (i in currentBoard.indices) {
            val col = i % 3
            val row = i / 3

            val left = (col * cellWidth)
            val top = (row * cellHeight)
            val right = ((col + 1) * cellWidth)
            val bottom = ((row + 1) * cellHeight)

            // Aplicar un padding si se desea, para que el drawable no toque las líneas
            val padding = (gridLineWidthPx * 2).toInt() // Ejemplo de padding, ajústalo a tu gusto

            val occupant = currentBoard[i]

            when (occupant) {
                "X" -> {
                    xDrawable?.let { drawable ->
                        // Establecer los límites donde se dibujará el Drawable
                        // Los convierte a Int porque setBounds espera Int
                        drawable.setBounds(
                            left.toInt() + padding,
                            top.toInt() + padding,
                            right.toInt() - padding,
                            bottom.toInt() - padding
                        )
                        drawable.draw(canvas) // Dibujar el Drawable en el canvas
                    }
                }
                "O" -> {
                    oDrawable?.let { drawable ->
                        drawable.setBounds(
                            left.toInt() + padding,
                            top.toInt() + padding,
                            right.toInt() - padding,
                            bottom.toInt() - padding
                        )
                        drawable.draw(canvas)
                    }
                }
                else -> { /* Celda vacía, no dibujar nada */ }
            }
        }

    }

    fun getBoardCellWidth(): Float {
        return cellWidth
    }

    fun getBoardCellHeight(): Float {
        return cellHeight
    }
    // También es muy común sobrescribir onSizeChanged() para calcular dimensiones
    // una vez que la vista tiene un tamaño.
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // w y h son el ancho y alto actuales de la vista en píxeles
        cellWidth = w / 3f
        cellHeight = h / 3f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (cellTouchListener == null) return super.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (cellWidth == 0f || cellHeight == 0f) return true
            val x = event.x
            val y = event.y
            if (x < 0 || x > width || y < 0 || y > height) return true
            val col = (x / cellWidth).toInt().coerceIn(0, 2)
            val row = (y / cellHeight).toInt().coerceIn(0, 2)
            cellTouchListener?.onCellTouched(row * 3 + col)
            return true
        }
        return super.onTouchEvent(event)
    }
}
   