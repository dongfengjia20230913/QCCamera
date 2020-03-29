/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.detection;

import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.util.Size;
import android.view.View;

import com.jdf.tf.R;


public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {

    // Configuration values for the prepackaged SSD model.
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetectionController.SAVE_PREVIEW_BITMAP = true;
            }
        });
    }

    /**
     * will be called when get preivew size
     * @param size
     * @param rotation
     */
    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        mDectectionController.onPreviewSizeChosen(size, rotation);
    }

    /**
     * Callback for Camera2 API when preview is available
     */
    @Override
    public void onImageAvailable(final ImageReader reader) {
        mDectectionController.onImageAvailbel(reader);
    }



    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }



}
