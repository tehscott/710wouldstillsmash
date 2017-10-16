package com.stromberg.scott.seventenwouldstillsmash.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout

class SquareLinearLayout : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = if (width > height) height else width

        setMeasuredDimension(size, size)
    }
}