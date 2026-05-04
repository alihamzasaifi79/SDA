package com.society.main;

import java.util.*;

public class Models {
    public static class Announcement {
        public String id, title, content, societyId, postedBy;
        public Date postedAt;
    
        public Announcement(String title, String content, String societyId, String postedBy) {
            this.id = Utils.generateId();
            this.title = title;
            this.content = content;
            this.societyId = societyId;
            this.postedBy = postedBy;
            this.postedAt = new Date();
        }
    }

    public static class AuditLog {
        public String id;
        public Enums.ActionType actionType;
        public String entityAffected;
        public String performedBy;
        public Date timestamp;
        public String details;
    
        public AuditLog(Enums.ActionType actionType, String entityAffected, String performedBy, String details) {
            this.id = Utils.generateId();
            this.actionType = actionType;
            this.entityAffected = entityAffected;
            this.performedBy = performedBy;
            this.timestamp = new Date();
            this.details = details;
        }
    }

    public static class Enums {
        public enum Role { STUDENT, SOCIETY_HEAD, ADMIN }
        public enum MembershipStatus { PENDING, APPROVED, REJECTED }
        public enum NotifType { MEMBERSHIP, EVENT, ANNOUNCEMENT, SYSTEM }
        public enum EventStatus { ACTIVE, CANCELLED }
        public enum SocietyStatus { ACTIVE, INACTIVE }
        public enum ActionType { CREATE, UPDATE, DELETE, STATUS_CHANGE, LOGIN }
    }

    public static class Event {
        public String id;
        public String title, description, venue;
        public String societyId;
        public Date date, regDeadline;
        public int maxParticipants;
        public Enums.EventStatus status;
        public List<String> participants;
    
        public Event(String title, String description, String venue, String societyId, Date date, Date regDeadline, int maxParticipants) {
            this.id = Utils.generateId();
            this.title = title;
            this.description = description;
            this.venue = venue;
            this.societyId = societyId;
            this.date = date;
            this.regDeadline = regDeadline;
            this.maxParticipants = maxParticipants;
            this.status = Enums.EventStatus.ACTIVE;
            this.participants = new ArrayList<>();
        }
    }

    public static class EventBudget {
        public String id, eventId;
        public double estimatedExpense, actualExpense;
        public String description;
    
        public EventBudget(String eventId, double estimatedExpense, String description) {
            this.id = Utils.generateId();
            this.eventId = eventId;
            this.estimatedExpense = estimatedExpense;
            this.actualExpense = 0.0;
            this.description = description;
        }
    }

    public static class FinancialRecord {
        public String id;
        public String societyId;
        public double amount;
        public String description;
        public Date date;
    
        public FinancialRecord(String societyId, double amount, String description) {
            this.id = Utils.generateId();
            this.societyId = societyId;
            this.amount = amount;
            this.description = description;
            this.date = new Date();
        }
    }

    public static class Membership {
        public String id, userId, societyId;
        public Enums.MembershipStatus status;
    
        public Membership(String userId, String societyId) {
            this.id = Utils.generateId();
            this.userId = userId;
            this.societyId = societyId;
            this.status = Enums.MembershipStatus.PENDING;
        }
    }

    public static class Notification {
        public String id;
        public String userId;
        public String message;
        public Enums.NotifType type;
        public boolean isRead;
        public Date createdAt;
    
        public Notification(String userId, String message, Enums.NotifType type) {
            this.id = Utils.generateId();
            this.userId = userId;
            this.message = message;
            this.type = type;
            this.isRead = false;
            this.createdAt = new Date();
        }
    }

    public static class Society {
        public String id, name, category, description, logo;
        public String headId;
        public Enums.SocietyStatus status;
    
        public Society(String name, String category, String headId, String description) {
            this.id = Utils.generateId();
            this.name = name;
            this.category = category;
            this.description = description;
            this.headId = headId;
            this.logo = "default_logo.png";
            this.status = Enums.SocietyStatus.ACTIVE;
        }
    }

    public static class User {
        public String id, name, email, studentId, password;
        public String degree, contactNumber, profilePicPath;
        public Enums.Role role;
    
        public User(String name, String email, String studentId, String password, Enums.Role role) {
            this.id = Utils.generateId();
            this.name = name;
            this.email = email;
            this.studentId = studentId;
            this.password = Utils.hashPassword(password);
            this.role = role;
            this.degree = "N/A";
            this.contactNumber = "N/A";
            this.profilePicPath = "default.png";
        }
    }
}
