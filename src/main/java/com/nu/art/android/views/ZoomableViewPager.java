/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2018  Adam van der Kruk aka TacB0sS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nu.art.android.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class ZoomableViewPager
	extends ViewPager {

	public interface ZoomableView {

		boolean canScrollHorizontally(int i);
	}

	public ZoomableViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ZoomableViewPager(Context context) {
		super(context);
	}

	@Override
	@SuppressWarnings("RedundantCast")
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (v instanceof ZoomableView)
			return ((ZoomableView) v).canScrollHorizontally(-dx);
		return super.canScroll(v, checkV, dx, x, y);
	}
}
