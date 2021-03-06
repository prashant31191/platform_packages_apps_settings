/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.core.instrumentation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsRobolectricTestRunner;
import com.android.settings.TestConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

import static com.android.settings.core.instrumentation.Instrumentable.METRICS_CATEGORY_UNKNOWN;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(SettingsRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class VisibilityLoggerMixinTest {

    @Mock
    private MetricsFeatureProvider mMetricsFeature;

    private VisibilityLoggerMixin mMixin;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mMixin = new VisibilityLoggerMixin(TestInstrumentable.TEST_METRIC, mMetricsFeature);
    }

    @Test
    public void shouldLogVisibleOnResume() {
        mMixin.onResume();

        verify(mMetricsFeature, times(1))
                .visible(any(Context.class), eq(MetricsProto.MetricsEvent.VIEW_UNKNOWN),
                        eq(TestInstrumentable.TEST_METRIC));
    }

    @Test
    public void shouldLogVisibleWithSource() {
        final Intent sourceIntent = new Intent()
                .putExtra(SettingsActivity.EXTRA_SOURCE_METRICS_CATEGORY,
                        MetricsProto.MetricsEvent.SETTINGS_GESTURES);
        final Activity activity = mock(Activity.class);
        when(activity.getIntent()).thenReturn(sourceIntent);
        mMixin.setSourceMetricsCategory(activity);
        mMixin.onResume();

        verify(mMetricsFeature, times(1))
                .visible(any(Context.class), eq(MetricsProto.MetricsEvent.SETTINGS_GESTURES),
                        eq(TestInstrumentable.TEST_METRIC));
    }

    @Test
    public void shouldLogHideOnPause() {
        mMixin.onPause();

        verify(mMetricsFeature, times(1))
                .hidden(any(Context.class), eq(TestInstrumentable.TEST_METRIC));
    }

    @Test
    public void shouldNotLogIfMetricsFeatureIsNull() {
        mMixin = new VisibilityLoggerMixin(TestInstrumentable.TEST_METRIC);
        mMixin.onResume();
        mMixin.onPause();

        verify(mMetricsFeature, never())
                .hidden(any(Context.class), anyInt());
    }

    @Test
    public void shouldNotLogIfMetricsCategoryIsUnknown() {
        mMixin = new VisibilityLoggerMixin(METRICS_CATEGORY_UNKNOWN, mMetricsFeature);

        mMixin.onResume();
        mMixin.onPause();

        verify(mMetricsFeature, never())
                .hidden(any(Context.class), anyInt());
    }

    private final class TestInstrumentable implements Instrumentable {

        public static final int TEST_METRIC = 12345;

        @Override
        public int getMetricsCategory() {
            return TEST_METRIC;
        }
    }
}
