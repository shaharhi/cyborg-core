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

package com.nu.art.cyborg.modules.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat.Builder;

import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.CyborgModuleItem;
import com.nu.art.cyborg.errorMessages.ExceptionGenerator;

public abstract class NotificationHandler
	extends CyborgModuleItem
	implements NotificationKeys {

	protected NotificationsModule module;

	protected void init() {
		module = getModule(NotificationsModule.class);
	}

	protected final Builder createBasicBuilder(short notificationId) {
		return createBasicBuilder(notificationId, new Bundle());
	}

	protected Builder createBasicBuilder(short notificationId, Bundle data) {
		Builder builder = new Builder(getApplicationContext());

		PendingIntent clickedPendingIntent = createPendingIntent(notificationId, Action_Click, data);
		builder.setContentIntent(clickedPendingIntent);

		PendingIntent deletePendingIntent = createPendingIntent(notificationId, Action_Cancel, data);
		builder.setDeleteIntent(deletePendingIntent);
		return builder;
	}

	protected final void cancelNotification(short notificationId) {
		module.disposeNotification(notificationId);
	}

	protected final Notification postNotification(Builder builder, short notificationId) {
		Notification notification = module.postNotification(builder, notificationId);
		validateNotificationChannel(notification);
		return notification;
	}

	private void validateNotificationChannel(Notification notification) {
		if (VERSION.SDK_INT < VERSION_CODES.O)
			return;

		if (notification.getChannelId() == null || notification.getChannelId().isEmpty())
			throw ExceptionGenerator.notificationMissingChannelId(notification);

		if (getSystemService(NotificationService).getNotificationChannel(notification.getChannelId()) == null)
			throw ExceptionGenerator.notificationChannelDoesNotExist(notification.getChannelId(), notification);
	}

	protected void addActionButton(Builder builder, short notificationId, String action, int iconResId, String label) {
		addActionButton(builder, notificationId, action, iconResId, label, null);
	}

	protected void addActionButton(Builder builder, short notificationId, String action, int iconResId, String label, Bundle notificationData) {
		PendingIntent moreInfoIntent = createPendingIntent(notificationId, action, notificationData, CyborgModule.getNextRandomPositiveShort());
		builder.addAction(iconResId, label, moreInfoIntent);
	}

	protected final PendingIntent createPendingIntent(short notificationId, String action) {
		return createPendingIntent(notificationId, action, new Bundle());
	}

	protected final PendingIntent createPendingIntent(short notificationId, String action, Bundle data) {
		return createPendingIntent(notificationId, action, data, 0);
	}

	protected final PendingIntent createPendingIntent(short notificationId, String action, Bundle data, int flags) {
		return module.createPendingIntent(this, notificationId, action, data, flags);
	}

	protected abstract void processNotification(short notificationId, String action, Bundle bundle);
}