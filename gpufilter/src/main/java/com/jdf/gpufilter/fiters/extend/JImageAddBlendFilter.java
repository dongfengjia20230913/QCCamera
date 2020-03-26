
package com.jdf.gpufilter.fiters.extend;


import com.jdf.common.utils.ApplicaitonUtil;
import com.jdf.gpufilter.fiters.GPUImageTwoInputFilter;
import com.jdf.gpufilter.util.ShaderUtil;

public class JImageAddBlendFilter extends GPUImageTwoInputFilter {


    public JImageAddBlendFilter() {
        super(ShaderUtil.loadShaderFromAssets("add_blend_normal_fragment.glsl", ApplicaitonUtil.context));
    }
}
