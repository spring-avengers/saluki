package com.quancheng.saluki.zuul.filter;

/**
 * @author HWEB
 */
public interface FiltersChangeNotifier {

    FiltersChangeNotifier IGNORE = new FiltersChangeNotifier() {
        @Override
        public void addFiltersListener(FiltersListener filtersListener) {
        }
    };

    void addFiltersListener(FiltersListener filtersListener);
}
