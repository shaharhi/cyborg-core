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

package com.nu.art.cyborg.modules.downloader.converters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.nu.art.core.exceptions.runtime.ThisShouldNotHappenException;
import com.nu.art.core.generics.Function;

import java.io.InputStream;

/**
 * Created by TacB0sS on 14/06/2017.
 */

@SuppressWarnings("unused")
public class Converter_Bitmap
	implements Function<InputStream, Bitmap> {

	public static final Converter_Bitmap converter = new Converter_Bitmap();

	@Override
	public Bitmap map(InputStream inputStream) {
		Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
		if (bitmap == null)
			throw new ThisShouldNotHappenException("Could not create bitmap from input stream");

		return bitmap;
	}
}
