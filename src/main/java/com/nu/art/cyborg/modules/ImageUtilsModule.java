/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2017  Adam van der Kruk aka TacB0sS
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

package com.nu.art.cyborg.modules;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Base64;
import android.widget.ImageView;

import com.nu.art.core.tools.FileTools;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.ActivityStack.ActivityStackAction;
import com.nu.art.cyborg.core.CyborgActivityBridge;
import com.nu.art.cyborg.core.CyborgModule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@ModuleDescriptor(usesPermissions = {})
public final class ImageUtilsModule
		extends CyborgModule {

	private static final int SELECT_PICTURE = getNextRandomPositiveShort();

	public static interface OnImageSelected {

		void onImageSelected(String uriToImage);

		void onActionCancelled();
	}

	@Override
	protected void init() {

	}

	public final Bitmap decodeImageFromBase64(String imageAsString) {
		imageAsString = imageAsString.substring(imageAsString.indexOf(',') + 1);
		byte[] imageData = Base64.decode(imageAsString, Base64.DEFAULT);
		ByteArrayInputStream byteStream = new ByteArrayInputStream(imageData);
		return BitmapFactory.decodeStream(byteStream);
	}

	public final String encodeImageToBase64(Bitmap image, CompressFormat format) {
		byte[] imageData = getImageBytes(image, format);
		return new String(Base64.encode(imageData, Base64.DEFAULT));
	}

	public byte[] getImageBytesFromBase64(String imageAsString, CompressFormat format) {
		Bitmap image = decodeImageFromBase64(imageAsString);
		return getImageBytes(image, format);
	}

	public byte[] getImageBytes(Bitmap image, CompressFormat format) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		image.compress(format, 100, stream);
		return stream.toByteArray();
	}

	public final void saveImageToFile(File imageFile, Bitmap image)
			throws IOException {
		saveImageToFile(imageFile, image, CompressFormat.PNG);
	}

	public final byte[] convertBitmapToByteArray(Bitmap image, CompressFormat format, int quality) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		image.compress(format, quality, os);
		return os.toByteArray();
	}

	public final void saveImageToFile(File imageFile, Bitmap image, CompressFormat format)
			throws IOException {
		FileTools.createNewFile(imageFile);

		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(imageFile);
			image.compress(format, 100, fileOutputStream);
		} finally {
			if (fileOutputStream != null)
				fileOutputStream.close();
		}
	}

	public final Bitmap loadImageFromFile(File imageFile)
			throws IOException {
		return loadImageFromFile(imageFile, 1f);
	}

	public final Bitmap loadImageFromFile(File imageFile, float ratio)
			throws IOException {
		if (!imageFile.exists())
			throw new FileNotFoundException("Cannot find file: " + imageFile);
		int rotate = getImageOrientation(imageFile);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = (int) (1 / ratio);
		Matrix matrix = new Matrix();
		matrix.postRotate(rotate);
		Bitmap image = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
		if (rotate == 0)
			return image;

		Bitmap finalBitmap = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
		image.recycle();
		return finalBitmap;
	}

	public final int getImageOrientation(File imageFile)
			throws IOException {
		ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

		switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_270:
				return 270;
			case ExifInterface.ORIENTATION_ROTATE_180:
				return 180;
			case ExifInterface.ORIENTATION_ROTATE_90:
				return 90;
			default:
				return 0;
		}
	}

	public Bitmap[] splitImages(Bitmap image, int imageColumnCount, int imageRowCount) {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		int singleImageWidth = imageWidth / imageColumnCount;
		int singleImageHeight = imageHeight / imageRowCount;
		Bitmap[] images = new Bitmap[imageColumnCount * imageRowCount];
		for (int r = 0, index = 0; r < imageRowCount; r++) {
			for (int c = 0; c < imageColumnCount; c++, index++) {
				images[index] = Bitmap.createBitmap(image, singleImageWidth * c, +singleImageHeight * r, singleImageWidth, singleImageHeight);
			}
		}

		return images;
	}

	public void setRoundedImage(Context context, Uri photoUri, ImageView imageView)
			throws FileNotFoundException {

		RoundedBitmapDrawable roundedImage;
		InputStream inputStream = null;
		try {
			inputStream = context.getContentResolver().openInputStream(photoUri);
			Bitmap image = BitmapFactory.decodeStream(inputStream);
			int originalWidth = image.getWidth();
			int originalHeight = image.getHeight();

			int dimen = Math.min(originalWidth, originalHeight);
			image = Bitmap.createBitmap(image, (originalWidth - dimen) / 2, (originalHeight - dimen) / 2, dimen, dimen);

			roundedImage = RoundedBitmapDrawableFactory.create(context.getResources(), image);
			roundedImage.setCornerRadius(dimen / 2.0f);
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (IOException e) {
				logError("Failed to close photo uri input stream", e);
			}
		}

		imageView.setImageDrawable(roundedImage);
	}

	public void selectImage(final int chooserTitleId, final OnImageSelected onImageSelected) {
		postActivityAction(new ActivityStackAction() {

			@Override
			public void execute(final CyborgActivityBridge activity) {
				logInfo("Selecting Image started...");
				activity.addResultListener(new OnActivityResultListener() {

					@Override
					public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
						if (requestCode != SELECT_PICTURE)
							return false;

						activity.removeResultListener(this);
						if (resultCode != Activity.RESULT_OK) {
							logInfo("User Cancelled Image Selection");
							onImageSelected.onActionCancelled();
							return true;
						}

						if (data == null || data.getData() == null) {
							return true;
						}

						Uri _uri = data.getData();

						Cursor cursor = getContentResolver().query(_uri, new String[]{MediaColumns.DATA}, null, null, null);
						cursor.moveToFirst();

						final String uriToImage = cursor.getString(0);
						cursor.close();
						logInfo("User Had Picked an Image: " + uriToImage);
						onImageSelected.onImageSelected(uriToImage);
						return true;
					}
				});
				Intent pickIntent = new Intent();
				pickIntent.setType("image/*");
				pickIntent.setAction(Intent.ACTION_GET_CONTENT);

				Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				Intent chooserIntent = Intent.createChooser(pickIntent, getString(chooserTitleId));
				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePhotoIntent});

				activity.startActivityForResult(chooserIntent, SELECT_PICTURE);
			}
		});
	}
}
