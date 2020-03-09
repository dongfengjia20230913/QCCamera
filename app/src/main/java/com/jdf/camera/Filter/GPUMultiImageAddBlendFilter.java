
package com.jdf.camera.Filter;

import android.content.Context;

import com.jdf.camera.util.FilterResUtils;
import com.jdf.camera.util.ShaderUtil;

public class GPUMultiImageAddBlendFilter extends GPUImageFourInputFilter {

    public GPUMultiImageAddBlendFilter(Context context) {
        super(ShaderUtil.loadShaderFromAssets("multi_image_vertex.glsl",context),
                ShaderUtil.loadShaderFromAssets("multi_image_fragment.glsl", context));
//        super(ADD_BLEND_FRAGMENT_SHADER);

        android.util.Log.e("jiadongfeng","mBlendBitmap:"+ FilterResUtils.loadShaderFromAssetFilter("paris20/paris_fragment.glsl"));


    }
}
