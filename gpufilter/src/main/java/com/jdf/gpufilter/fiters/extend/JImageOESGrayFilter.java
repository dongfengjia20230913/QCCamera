package com.jdf.gpufilter.fiters.extend;

import com.jdf.common.utils.ApplicaitonUtil;
import com.jdf.gpufilter.R;
import com.jdf.gpufilter.util.ShaderUtil;


public class JImageOESGrayFilter extends JImageOESFilter {
    public JImageOESGrayFilter() {
        super(ShaderUtil.loadShaderFromRes(ApplicaitonUtil.context, R.raw.base_oes_vertex_shader),
                ShaderUtil.loadShaderFromRes(ApplicaitonUtil.context, R.raw.gray_oes_fragment_shader));
    }


}

