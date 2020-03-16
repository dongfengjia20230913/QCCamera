
package com.jdf.camera.Filter;


import com.jdf.camera.applicaiton.CameraApplication;
import com.jdf.camera.util.ShaderUtil;

import com.jdf.gpufilter.fiters.GPUImageTwoInputFilter;

public class JGPUImageAddBlendFilter extends GPUImageTwoInputFilter {


    public JGPUImageAddBlendFilter() {
        super(ShaderUtil.loadShaderFromAssets("add_blend_normal_fragment.glsl", CameraApplication.context));
    }
}
