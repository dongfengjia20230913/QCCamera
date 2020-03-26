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

import com.jdf.gpufilter.fiters.extend.JGPUImageFilter;

public class JGPUImageGrayFilter extends JGPUImageFilter {
    private static final String NO_FILTER_FRAGMENT_SHADER =
                      "precision mediump float;\n"
                    + "uniform sampler2D inputImageTexture;\n"
                    + "varying vec2 textureCoordinate;\n"
                    + "const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);\n"
                    + "void main() {\n"
                    + "lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n"
                    + "float luminance = dot(textureColor.rgb, W);\n"
                    + "gl_FragColor = vec4(vec3(luminance), textureColor.w);\n"

                    + "}\n";

    public JGPUImageGrayFilter() {
        this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
    }

    public JGPUImageGrayFilter(final String vertexShader, final String fragmentShader) {
      super(vertexShader,fragmentShader);
    }

}