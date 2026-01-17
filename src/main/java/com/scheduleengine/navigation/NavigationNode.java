package com.scheduleengine.navigation;

/**
 * Represents a single node in the navigation breadcrumb trail
 */
public record NavigationNode(String viewId, String displayName, Object contextObject) {

  @Override
  public String toString() {
    return displayName;
  }
}

