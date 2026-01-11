package com.scheduleengine.navigation;

/**
 * Represents a single node in the navigation breadcrumb trail
 */
public class NavigationNode {

    private final String viewId;
    private final String displayName;
    private final Object contextObject;

    public NavigationNode(String viewId, String displayName, Object contextObject) {
        this.viewId = viewId;
        this.displayName = displayName;
        this.contextObject = contextObject;
    }

    public String getViewId() {
        return viewId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Object getContextObject() {
        return contextObject;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

