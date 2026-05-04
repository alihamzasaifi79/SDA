package com.society.main;

import java.util.HashMap;
import java.util.Map;

public class DB {
    public static Map<String, Models.User> users = new HashMap<>();
    public static Map<String, Models.Society> societies = new HashMap<>();
    public static Map<String, Models.Membership> memberships = new HashMap<>();
    public static Map<String, Models.Notification> notifications = new HashMap<>();
    public static Map<String, Models.Event> events = new HashMap<>();
    public static Map<String, Models.FinancialRecord> financialRecords = new HashMap<>();
    public static Map<String, Models.AuditLog> auditLogs = new HashMap<>();
    public static Map<String, Models.Announcement> announcements = new HashMap<>();
    public static Map<String, Models.EventBudget> eventBudgets = new HashMap<>();
}
