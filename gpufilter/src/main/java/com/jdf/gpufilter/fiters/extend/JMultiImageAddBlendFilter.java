
package com.jdf.gpufilter.fiters.extend;

import android.content.Context;

import com.jdf.gpufilter.util.ShaderUtil;

public class JMultiImageAddBlendFilter extends JImageFourInputFilter {

    public JMultiImageAddBlendFilter(Context context) {
        super(ShaderUtil.loadShaderFromAssets("multi_image_vertex.glsl", context),
                ShaderUtil.loadShaderFromAssets("multi_image_fragment.glsl", context));
    }
}
