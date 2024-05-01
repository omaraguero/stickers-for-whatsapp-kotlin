package com.roa.cswstickers.whatsapp_api

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class BottomFadingRecyclerView : RecyclerView {
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    override fun getTopFadingEdgeStrength(): Float {
        return 0.0f
    }
}
