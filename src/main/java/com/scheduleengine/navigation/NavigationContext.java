package com.scheduleengine.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a navigation context with hierarchical breadcrumb support.
 * Enables drill-down navigation patterns like: Leagues > Teams > Roster
 */
public class NavigationContext {

  private final List<NavigationNode> breadcrumb;
  private final Map<String, Object> contextData;

  public NavigationContext() {
    this.breadcrumb = new ArrayList<>();
    this.contextData = new HashMap<>();
  }

  private NavigationContext(List<NavigationNode> breadcrumb, Map<String, Object> contextData) {
    this.breadcrumb = new ArrayList<>(breadcrumb);
    this.contextData = new HashMap<>(contextData);
  }

  /**
   * Navigate to a new view, adding it to the breadcrumb trail
   */
  public NavigationContext navigateTo(String viewId, String displayName) {
    List<NavigationNode> newBreadcrumb = new ArrayList<>(breadcrumb);
    newBreadcrumb.add(new NavigationNode(viewId, displayName, null));
    return new NavigationContext(newBreadcrumb, contextData);
  }

  /**
   * Navigate to a new view with associated data (e.g., a specific league or team)
   */
  public NavigationContext navigateTo(String viewId, String displayName, Object contextObject) {
    List<NavigationNode> newBreadcrumb = new ArrayList<>(breadcrumb);
    newBreadcrumb.add(new NavigationNode(viewId, displayName, contextObject));

    Map<String, Object> newContextData = new HashMap<>(contextData);
    if (contextObject != null) {
      newContextData.put(viewId, contextObject);
    }

    return new NavigationContext(newBreadcrumb, newContextData);
  }

  /**
   * Navigate back to a specific level in the breadcrumb
   */
  public NavigationContext navigateToLevel(int level) {
    if (level < 0 || level >= breadcrumb.size()) {
      return this;
    }

    List<NavigationNode> newBreadcrumb = new ArrayList<>(breadcrumb.subList(0, level + 1));
    Map<String, Object> newContextData = new HashMap<>();

    // Rebuild context data for remaining breadcrumb items
    for (NavigationNode node : newBreadcrumb) {
      if (node.contextObject() != null) {
        newContextData.put(node.viewId(), node.contextObject());
      }
    }

    return new NavigationContext(newBreadcrumb, newContextData);
  }

  /**
   * Navigate back one level
   */
  public NavigationContext navigateBack() {
    if (breadcrumb.isEmpty()) {
      return this;
    }
    return navigateToLevel(breadcrumb.size() - 2);
  }

  /**
   * Clear all navigation and return to root
   */
  public NavigationContext navigateToRoot() {
    return new NavigationContext();
  }

  /**
   * Get the current view ID (last item in breadcrumb)
   */
  public String getCurrentViewId() {
    if (breadcrumb.isEmpty()) {
      return null;
    }
    return breadcrumb.get(breadcrumb.size() - 1).viewId();
  }

  /**
   * Get context data for a specific view
   */
  public <T> T getContextData(String viewId, Class<T> type) {
    Object data = contextData.get(viewId);
    if (type.isInstance(data)) {
      return type.cast(data);
    }
    return null;
  }

  /**
   * Get all context data
   */
  public Map<String, Object> getAllContextData() {
    return new HashMap<>(contextData);
  }

  /**
   * Get the breadcrumb trail
   */
  public List<NavigationNode> getBreadcrumb() {
    return new ArrayList<>(breadcrumb);
  }

  /**
   * Check if we're at the root level
   */
  public boolean isAtRoot() {
    return breadcrumb.isEmpty();
  }

  /**
   * Get the depth of the current navigation
   */
  public int getDepth() {
    return breadcrumb.size();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < breadcrumb.size(); i++) {
      if (i > 0) {
        sb.append(" > ");
      }
      sb.append(breadcrumb.get(i).displayName());
    }
    return sb.toString();
  }
}

