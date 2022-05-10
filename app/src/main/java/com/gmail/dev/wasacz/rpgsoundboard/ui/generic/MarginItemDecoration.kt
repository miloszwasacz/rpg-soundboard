package com.gmail.dev.wasacz.rpgsoundboard.ui.generic

import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(
    @DimenRes private val leftSpace: Int,
    @DimenRes private val rightSpace: Int,
    @DimenRes private val topSpace: Int,
    @DimenRes private val bottomSpace: Int,
    private val spanCount: Int = 1,
    private val orientation: Int = GridLayoutManager.VERTICAL
) : RecyclerView.ItemDecoration() {
    constructor(
        @DimenRes horizontalSpace: Int,
        @DimenRes verticalSpace: Int,
        spanCount: Int = 1,
        orientation: Int = GridLayoutManager.VERTICAL
    ) : this(
        horizontalSpace,
        horizontalSpace,
        verticalSpace,
        verticalSpace,
        spanCount,
        orientation
    )

    constructor(@DimenRes spaceSize: Int, spanCount: Int = 1, orientation: Int = GridLayoutManager.VERTICAL) : this(
        spaceSize,
        spaceSize,
        spanCount,
        orientation
    )

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        with(view.context.resources) {
            outRect.apply {
                if (orientation == GridLayoutManager.VERTICAL) {
                    if (parent.getChildAdapterPosition(view) < spanCount)
                        top = getDimensionPixelSize(topSpace)
                    if (parent.getChildAdapterPosition(view) % spanCount == 0)
                        left = getDimensionPixelSize(leftSpace)
                } else {
                    if (parent.getChildAdapterPosition(view) < spanCount)
                        left = getDimensionPixelSize(leftSpace)
                    if (parent.getChildAdapterPosition(view) % spanCount == 0)
                        top = getDimensionPixelSize(topSpace)
                }

                right = getDimensionPixelSize(rightSpace)
                bottom = getDimensionPixelSize(bottomSpace)
            }
        }
    }
}