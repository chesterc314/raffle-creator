package co.za.chester.rafflecreator.premium

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import org.funktionale.option.Option
import org.funktionale.option.toOption

class RecyclerItemClickListener(context: Context, recyclerView: RecyclerView, private val listener: OnItemClickListener?) : RecyclerView.OnItemTouchListener {
    private var gestureDetector: GestureDetector

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)

        fun onLongItemClick(view: View?, position: Int)
    }

    init {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val maybeChildView: Option<View> = recyclerView.findChildViewUnder(e.x, e.y).toOption()
                val maybeListener = listener.toOption()
                maybeChildView.map { child ->
                    maybeListener.map { l ->
                        l.onLongItemClick(child, recyclerView.getChildAdapterPosition(child))
                    }
                }
            }
        })
    }

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val maybeChildView: Option<View> = view.findChildViewUnder(e.x, e.y).toOption()
        val maybeListener = listener.toOption()
        maybeChildView.map { childView ->
            maybeListener.map { l ->
                if (gestureDetector.onTouchEvent(e)) {
                    l.onItemClick(childView, view.getChildAdapterPosition(childView))
                    return true
                }
            }
        }
        return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}
