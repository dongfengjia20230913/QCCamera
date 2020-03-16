package com.jdf.common.widget.recycleview;

public class EffectData {
    public FilterType filterType;
    public String effectName;
    public int effPostion;
    public EffectData(FilterType type, String name,int postion){
        filterType = type;
        effectName = name;
        effPostion = postion;
    }

    public EffectData(String name,int postion){
        effectName = name;
        effPostion = postion;
    }
}
