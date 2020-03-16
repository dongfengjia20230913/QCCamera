package com.jdf.common.widget.recycleview;

import java.util.LinkedList;
import java.util.List;

public class FilterList {
    public List<String> names = new LinkedList<String>();
    public List<FilterType> filters = new LinkedList<FilterType>();

    public void addFilter(final String name, final FilterType filter) {
        names.add(name);
        filters.add(filter);
    }
}