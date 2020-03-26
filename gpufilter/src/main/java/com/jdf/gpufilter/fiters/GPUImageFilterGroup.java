/*
 * Copyright (C) 2018 CyberAgent, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jdf.gpufilter.fiters;

import android.annotation.SuppressLint;
import android.opengl.GLES20;

import com.jdf.gpufilter.util.OpenGlUtils;
import com.jdf.gpufilter.util.Rotation;
import com.jdf.gpufilter.util.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.jdf.gpufilter.util.TextureRotationUtil.CUBE;
import static com.jdf.gpufilter.util.TextureRotationUtil.TEXTURE_NO_ROTATION;

/**
 * Resembles a filter that consists of multiple filters applied after each
 * other.
 */
public class GPUImageFilterGroup extends GPUImageFilter {

    private List<GPUImageFilter> filters;
    private List<GPUImageFilter> mergedFilters;
    private int[] frameBuffers;
    private int[] frameBufferTextures;

    private final FloatBuffer glCubeBuffer;
    private final FloatBuffer glTextureBuffer;
    private final FloatBuffer glTextureFlipBuffer;

    /**
     * Instantiates a new GPUImageFilterGroup with no filters.
     */
    public GPUImageFilterGroup() {
        this(null);
    }

    /**
     * Instantiates a new GPUImageFilterGroup with the given filters.
     *
     * @param filters the filters which represent this filter
     */
    public GPUImageFilterGroup(List<GPUImageFilter> filters) {
        this.filters = filters;
        if (this.filters == null) {
            this.filters = new ArrayList<>();
        } else {
            updateMergedFilters();
        }

        glCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        glCubeBuffer.put(CUBE).position(0);

        glTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        glTextureBuffer.put(TEXTURE_NO_ROTATION).position(0);

        float[] flipTexture = TextureRotationUtil.getRotation(Rotation.NORMAL, false, true);
        glTextureFlipBuffer = ByteBuffer.allocateDirect(flipTexture.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        glTextureFlipBuffer.put(flipTexture).position(0);
    }

    public void addFilter(GPUImageFilter aFilter) {
        if (aFilter == null) {
            return;
        }
        filters.add(aFilter);
        updateMergedFilters();
    }

    /*
     * (non-Javadoc)
     * @see com.jdf.gpufilter.fiters.GPUImageFilter#onInit()
     */
    @Override
    public void onInit() {
        super.onInit();
        for (GPUImageFilter filter : filters) {
            filter.ifNeedInit();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.jdf.gpufilter.fiters.GPUImageFilter#onDestroy()
     */
    @Override
    public void onDestroy() {
        destroyFramebuffers();
        for (GPUImageFilter filter : filters) {
            filter.destroy();
        }
        super.onDestroy();
    }

    private void destroyFramebuffers() {
        if (frameBufferTextures != null) {
            GLES20.glDeleteTextures(frameBufferTextures.length, frameBufferTextures, 0);
            frameBufferTextures = null;
        }
        if (frameBuffers != null) {
            GLES20.glDeleteFramebuffers(frameBuffers.length, frameBuffers, 0);
            frameBuffers = null;
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.jdf.gpufilter.fiters.GPUImageFilter#onOutputSizeChanged(int,
     * int)
     */
    @Override
    public void onOutputSizeChanged(final int width, final int height) {
        super.onOutputSizeChanged(width, height);
        if (frameBuffers != null) {
            destroyFramebuffers();
        }

        int size = filters.size();
        for (int i = 0; i < size; i++) {
            filters.get(i).onOutputSizeChanged(width, height);
        }

        if (mergedFilters != null && mergedFilters.size() > 0) {
            size = mergedFilters.size();
            frameBuffers = new int[size - 1];
            frameBufferTextures = new int[size - 1];

            OpenGlUtils.createFrameBuffer(frameBuffers,frameBufferTextures, width, height);
        }
    }



    /*
     * (non-Javadoc)
     * @see com.jdf.gpufilter.fiters.GPUImageFilter#onDraw(int,
     * java.nio.FloatBuffer, java.nio.FloatBuffer)
     */
    @SuppressLint("WrongCall")
    @Override
    public void onDraw(final int textureId, final FloatBuffer cubeBuffer,
                       final FloatBuffer textureBuffer) {
        runPendingOnDrawTasks();
        if (!isInitialized() || frameBuffers == null || frameBufferTextures == null) {
            return;
        }
        if (mergedFilters != null) {
            int size = mergedFilters.size();
            int previousTexture = textureId;
            for (int i = 0; i < size; i++) {
                GPUImageFilter filter = mergedFilters.get(i);
                boolean isNotLast = i < size - 1;
                if (isNotLast) {
                    //绑定FBO,后续previousTexture的绘制内容会指向frameBuffers
                    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[i]);
                    GLES20.glClearColor(0, 0, 0, 0);
                }

                if (i == 0) {
                    filter.onDraw(previousTexture, cubeBuffer, textureBuffer);
                } else if (i == size - 1) {
                    filter.onDraw(previousTexture, glCubeBuffer, (size % 2 == 0) ? glTextureFlipBuffer : glTextureBuffer);
                } else {
                    filter.onDraw(previousTexture, glCubeBuffer, glTextureBuffer);
                }

                if (isNotLast) {
                    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                    previousTexture = frameBufferTextures[i];
                }
            }
        }
    }

    /**
     * Gets the filters.
     *
     * @return the filters
     */
    public List<GPUImageFilter> getFilters() {
        return filters;
    }

    public List<GPUImageFilter> getMergedFilters() {
        return mergedFilters;
    }

    public void updateMergedFilters() {
        if (filters == null) {
            return;
        }

        if (mergedFilters == null) {
            mergedFilters = new ArrayList<>();
        } else {
            mergedFilters.clear();
        }

        List<GPUImageFilter> filters;
        for (GPUImageFilter filter : this.filters) {
            if (filter instanceof GPUImageFilterGroup) {
                ((GPUImageFilterGroup) filter).updateMergedFilters();
                filters = ((GPUImageFilterGroup) filter).getMergedFilters();
                if (filters == null || filters.isEmpty())
                    continue;
                mergedFilters.addAll(filters);
                continue;
            }
            mergedFilters.add(filter);
        }
    }
}
