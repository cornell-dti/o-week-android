package com.cornellsatech.o_week;


import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * An item to be set for a {@link RecyclerView} to provide extra spacing between items. Note that this
 * is structured such that it only works for horizontal orientations, though one that works vertically
 * would be trivial to implement. See {@link MainActivity#setUpRecycler()} for an example of how it is used.
 */
public class SpacingItemDecor extends RecyclerView.ItemDecoration
{
	private final int leftMargin;
	private final int rightMargin;

	public SpacingItemDecor(@DimenRes int leftMarginRes, @DimenRes int rightMarginRes, Context context)
	{
		leftMargin = (int) context.getResources().getDimension(leftMarginRes);
		rightMargin = (int) context.getResources().getDimension(rightMarginRes);
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
	{
		outRect.left = leftMargin;
		outRect.right = rightMargin;
	}
}
