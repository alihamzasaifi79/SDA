package com.society.main;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

public class Services {
    public static class AnnouncementService {
        public static void postAnnouncement(Models.User head, String societyId, String title, String content) {
            if (title.isEmpty() || content.isEmpty()) throw new RuntimeException("Title and Content are required.");
            
            Models.Announcement a = new Models.Announcement(title, content, societyId, head.id);
            DB.announcements.put(a.id, a);
    
            for (Models.Membership m : DB.memberships.values()) {
                if (m.societyId.equals(societyId) && m.status == Models.Enums.MembershipStatus.APPROVED) {
                    NotificationService.send(m.userId, "New Announcement: " + title, Models.Enums.NotifType.ANNOUNCEMENT);
                }
            }
        }
    
        public static List<Models.Announcement> getAnnouncementsForSociety(String societyId) {
            List<Models.Announcement> list = new ArrayList<>();
            for (Models.Announcement a : DB.announcements.values()) {
                if (a.societyId.equals(societyId)) {
                    list.add(a);
                }
            }
            list.sort((a, b) -> b.postedAt.compareTo(a.postedAt));
            return list;
        }
    }

    public static class AuditService {
        public static void log(Models.Enums.ActionType type, String entity, String by, String details) {
            Models.AuditLog log = new Models.AuditLog(type, entity, by, details);
            DB.auditLogs.put(log.id, log);
        }
    
        public static List<Models.AuditLog> getAllLogs() {
            List<Models.AuditLog> list = new ArrayList<>(DB.auditLogs.values());
            list.sort((a, b) -> b.timestamp.compareTo(a.timestamp));
            return list;
        }
    }

    public static class EventService {
        public static Models.Event createEvent(Models.User head, Models.Society society, String title, String description, String venue, Date date, Date regDeadline, int maxParticipants) {
            if (!society.headId.equals(head.id))
                throw new RuntimeException("Only society head can create events");
    
            if (date.before(new Date())) throw new RuntimeException("Event date must be in the future");
            if (regDeadline.after(date)) throw new RuntimeException("Registration deadline cannot be after the event date");
    
            Models.Event e = new Models.Event(title, description, venue, society.id, date, regDeadline, maxParticipants);
            DB.events.put(e.id, e);
            AuditService.log(Models.Enums.ActionType.CREATE, "Event", head.id, "Created event " + title);
    
            for (Models.Membership m : DB.memberships.values()) {
                if (m.societyId.equals(society.id) && m.status == Models.Enums.MembershipStatus.APPROVED) {
                    NotificationService.send(m.userId, "New Event by " + society.name + ": " + title, Models.Enums.NotifType.EVENT);
                }
            }
            return e;
        }
    
        public static void register(Models.User user, Models.Event event) {
            if (event.status == Models.Enums.EventStatus.CANCELLED) throw new RuntimeException("Event cancelled");
            if (event.participants.size() >= event.maxParticipants) throw new RuntimeException("Event full");
            if (event.participants.contains(user.id)) throw new RuntimeException("Already registered");
    
            event.participants.add(user.id);
    
            NotificationService.send(user.id, "You registered for " + event.title, Models.Enums.NotifType.EVENT);
        }
    
        public static void cancel(Models.User head, Models.Event event) {
            Models.Society s = DB.societies.get(event.societyId);
            if (!s.headId.equals(head.id)) throw new RuntimeException("Not authorized");
    
            event.status = Models.Enums.EventStatus.CANCELLED;
            AuditService.log(Models.Enums.ActionType.STATUS_CHANGE, "Event", head.id, "Cancelled event " + event.title);
    
            for (String userId : event.participants) {
                NotificationService.send(userId, "Event cancelled: " + event.title, Models.Enums.NotifType.EVENT);
            }
        }
    
        public static List<Models.Event> getAllActiveEvents() {
            List<Models.Event> list = new ArrayList<>();
            for (Models.Event e : DB.events.values()) {
                if (e.status == Models.Enums.EventStatus.ACTIVE) {
                    list.add(e);
                }
            }
            return list;
        }
    }

    public static class FinanceService {
        public static void addRecord(Models.Society society, double amount, String description) {
            Models.FinancialRecord r = new Models.FinancialRecord(society.id, amount, description);
            DB.financialRecords.put(r.id, r);
        }
    
        public static void trackDues(Models.Society society, String memberId, double amountPaid) {
            Models.FinancialRecord r = new Models.FinancialRecord(society.id, amountPaid, "Membership Dues: " + memberId);
            DB.financialRecords.put(r.id, r);
        }
    
        public static void createEventBudget(String eventId, double estimated, String description) {
            Models.EventBudget b = new Models.EventBudget(eventId, estimated, description);
            DB.eventBudgets.put(b.id, b);
        }
    
        public static void recordActualExpense(String budgetId, double actual) {
            Models.EventBudget b = DB.eventBudgets.get(budgetId);
            if (b != null) {
                b.actualExpense = actual;
            }
        }
    }

    public static class MembershipService {
        public static Models.Membership request(Models.User user, Models.Society society) {
            for (Models.Membership m : DB.memberships.values()) {
                if (m.userId.equals(user.id) && m.societyId.equals(society.id)) {
                    throw new RuntimeException("Membership already requested or approved.");
                }
            }
    
            Models.Membership m = new Models.Membership(user.id, society.id);
            DB.memberships.put(m.id, m);
    
            NotificationService.send(society.headId, "New membership request from " + user.name + " for " + society.name, Models.Enums.NotifType.MEMBERSHIP);
            return m;
        }
    
        public static void approve(Models.User head, Models.Membership m) {
            Models.Society s = DB.societies.get(m.societyId);
            if (!s.headId.equals(head.id)) throw new RuntimeException("Not authorized");
    
            m.status = Models.Enums.MembershipStatus.APPROVED;
            AuditService.log(Models.Enums.ActionType.STATUS_CHANGE, "Membership", head.id, "Approved user " + m.userId);
            NotificationService.send(m.userId, "Your membership has been APPROVED in " + s.name, Models.Enums.NotifType.MEMBERSHIP);
        }
    
        public static void reject(Models.User head, Models.Membership m, String reason) {
            Models.Society s = DB.societies.get(m.societyId);
            if (!s.headId.equals(head.id)) throw new RuntimeException("Not authorized");
    
            m.status = Models.Enums.MembershipStatus.REJECTED;
            AuditService.log(Models.Enums.ActionType.STATUS_CHANGE, "Membership", head.id, "Rejected user " + m.userId + " Reason: " + reason);
            NotificationService.send(m.userId, "Your membership has been REJECTED in " + s.name + ". Reason: " + reason, Models.Enums.NotifType.MEMBERSHIP);
        }
    
        public static List<Models.Membership> getPendingRequests(String societyId) {
            List<Models.Membership> list = new ArrayList<>();
            for (Models.Membership m : DB.memberships.values()) {
                if (m.societyId.equals(societyId) && m.status == Models.Enums.MembershipStatus.PENDING) {
                    list.add(m);
                }
            }
            return list;
        }
    
        public static List<Models.Society> getMyMemberships(String userId) {
            List<Models.Society> list = new ArrayList<>();
            for (Models.Membership m : DB.memberships.values()) {
                if (m.userId.equals(userId) && m.status == Models.Enums.MembershipStatus.APPROVED) {
                    list.add(DB.societies.get(m.societyId));
                }
            }
            return list;
        }
    }

    public static class NotificationService {
        public static void send(String userId, String message, Models.Enums.NotifType type) {
            Models.Notification n = new Models.Notification(userId, message, type);
            DB.notifications.put(n.id, n);
        }
    
        public static List<Models.Notification> getUserNotifications(String userId) {
            List<Models.Notification> list = new ArrayList<>();
            for (Models.Notification n : DB.notifications.values()) {
                if (n.userId.equals(userId)) {
                    list.add(n);
                }
            }
            list.sort((n1, n2) -> n2.createdAt.compareTo(n1.createdAt));
            return list;
        }
    
        public static void markAsRead(String notifId) {
            Models.Notification n = DB.notifications.get(notifId);
            if (n != null) n.isRead = true;
        }
    }

    public static class ReportService {
        public static String getFinancialReport() {
            double total = 0;
            StringBuilder sb = new StringBuilder();
            sb.append("--- FINANCIAL REPORT ---\n");
    
            for (Models.FinancialRecord r : DB.financialRecords.values()) {
                sb.append("• ").append(r.description).append(" | Rs.").append(r.amount).append("\n");
                total += r.amount;
            }
    
            sb.append("\nTotal Amount: Rs.").append(total);
            return sb.toString();
        }
    
        public static String getActivityReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("--- ACTIVITY REPORT ---\n");
    
            for (Models.Society s : DB.societies.values()) {
                int members = 0;
                int events = 0;
                int participants = 0;
    
                for (Models.Membership m : DB.memberships.values()) {
                    if (m.societyId.equals(s.id) && m.status == Models.Enums.MembershipStatus.APPROVED) {
                        members++;
                    }
                }
    
                for (Models.Event e : DB.events.values()) {
                    if (e.societyId.equals(s.id)) {
                        events++;
                        participants += e.participants.size();
                    }
                }
    
                sb.append("[").append(s.name).append("]\n");
                sb.append(" Members: ").append(members).append("\n");
                sb.append(" Events: ").append(events).append("\n");
                sb.append(" Total Participation: ").append(participants).append("\n");
                sb.append("------------------------\n");
            }
            return sb.toString();
        }
    }

    public static class SocietyService {
        public static Models.Society createSociety(Models.User admin, String name, String category, String description, Models.User head) {
            if (admin.role != Models.Enums.Role.ADMIN) throw new RuntimeException("Only admin allowed to create societies");
            for (Models.Society s : DB.societies.values()) {
                if (s.name.equalsIgnoreCase(name)) throw new RuntimeException("Society name already exists");
            }
    
            head.role = Models.Enums.Role.SOCIETY_HEAD;
    
            Models.Society s = new Models.Society(name, category, head.id, description);
            DB.societies.put(s.id, s);
            AuditService.log(Models.Enums.ActionType.CREATE, "Society", admin.id, "Created society " + name);
            return s;
        }
    
        public static void deactivateSociety(Models.User admin, Models.Society society) {
            if (admin.role != Models.Enums.Role.ADMIN) throw new RuntimeException("Only admin allowed to deactivate societies");
            society.status = Models.Enums.SocietyStatus.INACTIVE;
            AuditService.log(Models.Enums.ActionType.STATUS_CHANGE, "Society", admin.id, "Deactivated society " + society.name);
        }
    
        public static List<Models.Society> getAllActiveSocieties() {
            List<Models.Society> list = new ArrayList<>();
            for (Models.Society s : DB.societies.values()) {
                if (s.status == Models.Enums.SocietyStatus.ACTIVE) {
                    list.add(s);
                }
            }
            return list;
        }
    
        public static List<Models.Society> getAllSocieties() {
            return new ArrayList<>(DB.societies.values());
        }
    
        public static List<Models.Society> getSocietiesByHead(String headId) {
            List<Models.Society> list = new ArrayList<>();
            for (Models.Society s : DB.societies.values()) {
                if (s.headId.equals(headId)) {
                    list.add(s);
                }
            }
            return list;
        }
    }

    public static class UserService {
        public static Models.User register(String name, String email, String studentId, String password, Models.Enums.Role role) {
            if (name.isEmpty() || email.isEmpty() || studentId.isEmpty() || password.isEmpty()) {
                throw new RuntimeException("All fields are required.");
            }
            if (!email.endsWith("@nu.edu.pk")) throw new RuntimeException("Only university emails (@nu.edu.pk) allowed.");
            
            for (Models.User u : DB.users.values()) {
                if (u.email.equals(email)) throw new RuntimeException("User exists with this email.");
                if (u.studentId.equals(studentId)) throw new RuntimeException("Student ID already registered.");
            }
            Models.User u = new Models.User(name, email, studentId, password, role);
            DB.users.put(u.id, u);
            AuditService.log(Models.Enums.ActionType.CREATE, "User", u.id, "Registered " + name);
            return u;
        }
    
        public static Models.User login(String email, String password) {
            for (Models.User u : DB.users.values()) {
                if (u.email.equals(email) && u.password.equals(Utils.hashPassword(password))) {
                    AuditService.log(Models.Enums.ActionType.LOGIN, "User", u.id, "Logged in");
                    return u;
                }
            }
            throw new RuntimeException("Invalid email or password");
        }
    
        public static void updateProfile(Models.User user, String name, String degree, String contact) {
            if (name.isEmpty()) throw new RuntimeException("Name cannot be empty");
            user.name = name;
            user.degree = degree;
            user.contactNumber = contact;
            AuditService.log(Models.Enums.ActionType.UPDATE, "User", user.id, "Profile updated");
        }
    
        public static List<Models.User> getAllStudents() {
            List<Models.User> students = new ArrayList<>();
            for (Models.User u : DB.users.values()) {
                if (u.role == Models.Enums.Role.STUDENT) {
                    students.add(u);
                }
            }
            return students;
        }
    }
}
