package includex.com.dio.service

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.IBinder
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import includex.com.dio.enum.PaintOp

class CanvasService : BaseService() {

    lateinit var wm: WindowManager

    private lateinit var view: GuideView

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        wm = getSystemService(WindowManager::class.java)

        view = GuideView(this)
        val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)

        layoutParams.gravity = Gravity.LEFT or Gravity.TOP
        wm.addView(view, layoutParams)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        throw RuntimeException()
    }

    override fun onDestroy() {
        try {
            wm.removeView(view)
        } catch (e: Exception) {
        }

        super.onDestroy()
    }

    data class DrawCommand(var op: PaintOp, var point: PointF)

    class GuideView : View {
        val paths = ArrayList<DrawCommand>()
        val paint = Paint().apply {
            color = Color.RED
            strokeWidth = 10f
        }

        constructor(context: Context) : super(context)

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

        fun clear() {
            paths.clear()
        }

        override fun onDraw(canvas: Canvas?) {
            var lastPoint = PointF()

            for (path in paths) {
                when (path.op) {
                    PaintOp.Draw -> {
                        canvas?.drawLine(lastPoint.x, lastPoint.y, path.point.x, path.point.y, paint)
                    }

                    PaintOp.Move -> {
                    }
                }

                lastPoint.x = path.point.x
                lastPoint.y = path.point.y
            }

            super.onDraw(canvas)
        }
    }
}