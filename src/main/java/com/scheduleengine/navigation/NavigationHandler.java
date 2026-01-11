package com.scheduleengine.navigation;

/**
 * Interface for views that support hierarchical navigation
 */
@FunctionalInterface
public interface NavigationHandler {

    /**
     * Navigate to a new view with the given context
     *
     * @param context The navigation context containing breadcrumb and context data
     */
    void navigate(NavigationContext context);
}

