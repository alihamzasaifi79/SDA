package com.society.main;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Date;

public class GUIs {
    public static class StyleUtils {
        public static final Color PRIMARY = new Color(41, 128, 185);
        public static final Color BACKGROUND = new Color(236, 240, 241);
        public static final Color TEXT_COLOR = new Color(44, 62, 80);
        public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
        public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
        public static final Font FONT_BTN = new Font("Segoe UI", Font.BOLD, 14);
    
        public static void applyTheme() {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                UIManager.put("Panel.background", BACKGROUND);
                UIManager.put("Label.foreground", TEXT_COLOR);
                UIManager.put("OptionPane.background", BACKGROUND);
                UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
                UIManager.put("List.background", Color.WHITE);
                UIManager.put("List.foreground", TEXT_COLOR);
                UIManager.put("TextField.background", Color.WHITE);
                UIManager.put("TextField.foreground", TEXT_COLOR);
                UIManager.put("ComboBox.background", Color.WHITE);
                UIManager.put("ComboBox.foreground", TEXT_COLOR);
                UIManager.put("PasswordField.background", Color.WHITE);
                UIManager.put("PasswordField.foreground", TEXT_COLOR);
                UIManager.put("TabbedPane.background", BACKGROUND);
                UIManager.put("TabbedPane.foreground", TEXT_COLOR);
            } catch (Exception e) {}
        }
    
        public static void styleButton(JButton btn) {
            btn.setBackground(PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.setFont(FONT_BTN);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    
        public static void styleLabel(JLabel lbl, boolean isTitle) {
            lbl.setForeground(TEXT_COLOR);
            if (isTitle) {
                lbl.setFont(FONT_TITLE);
            } else {
                lbl.setFont(FONT_NORMAL);
            }
        }
    }

    public static class AdminGUI {
        public AdminGUI(Models.User admin) {
            JFrame frame = new JFrame("Admin Panel");
            frame.setSize(600, 500);
            frame.setLocationRelativeTo(null);
            
            JTabbedPane tabs = new JTabbedPane();
            
            JPanel createPanel = new JPanel(new GridLayout(6, 2, 10, 10));
            createPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
            createPanel.add(new JLabel("Society Name:"));
            JTextField nameF = new JTextField();
            createPanel.add(nameF);
            createPanel.add(new JLabel("Category:"));
            JTextField catF = new JTextField();
            createPanel.add(catF);
            createPanel.add(new JLabel("Description:"));
            JTextField descF = new JTextField();
            createPanel.add(descF);
            createPanel.add(new JLabel("Assign Head:"));
            JComboBox<String> headC = new JComboBox<>();
            List<Models.User> students = Services.UserService.getAllStudents();
            for (Models.User u : students) headC.addItem(u.name);
            createPanel.add(headC);
            
            JButton createBtn = new JButton("Create Society");
            StyleUtils.styleButton(createBtn);
            createPanel.add(new JLabel());
            createPanel.add(createBtn);
            createBtn.addActionListener(e -> {
                int idx = headC.getSelectedIndex();
                if (idx == -1) return;
                try {
                    Services.SocietyService.createSociety(admin, nameF.getText(), catF.getText(), descF.getText(), students.get(idx));
                    JOptionPane.showMessageDialog(frame, "Created!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
            });
            
            JPanel deactPanel = new JPanel(new BorderLayout());
            deactPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
            DefaultListModel<String> sModel = new DefaultListModel<>();
            List<Models.Society> allSocs = Services.SocietyService.getAllActiveSocieties();
            for (Models.Society s : allSocs) sModel.addElement(s.name);
            JList<String> sList = new JList<>(sModel);
            deactPanel.add(new JScrollPane(sList), BorderLayout.CENTER);
            
            JButton deactBtn = new JButton("Deactivate Selected");
            StyleUtils.styleButton(deactBtn);
            deactPanel.add(deactBtn, BorderLayout.SOUTH);
            deactBtn.addActionListener(e -> {
                int idx = sList.getSelectedIndex();
                if (idx != -1) {
                    Services.SocietyService.deactivateSociety(admin, allSocs.get(idx));
                    JOptionPane.showMessageDialog(frame, "Deactivated!");
                    sModel.remove(idx);
                }
            });
            
            JPanel repPanel = new JPanel(new BorderLayout(10,10));
            JButton genRepBtn = new JButton("Generate Activity Report");
            StyleUtils.styleButton(genRepBtn);
            JTextArea repArea = new JTextArea();
            repArea.setEditable(false);
            repPanel.add(genRepBtn, BorderLayout.NORTH);
            repPanel.add(new JScrollPane(repArea), BorderLayout.CENTER);
            genRepBtn.addActionListener(e -> {
                repArea.setText(Services.ReportService.getActivityReport() + "\n\n" + Services.ReportService.getFinancialReport());
            });
            
            JPanel auditPanel = new JPanel(new BorderLayout());
            DefaultListModel<String> aModel = new DefaultListModel<>();
            for (Models.AuditLog log : Services.AuditService.getAllLogs()) {
                aModel.addElement("["+log.timestamp.toString()+"] " + log.actionType + " on " + log.entityAffected + " by " + log.performedBy + " -> " + log.details);
            }
            auditPanel.add(new JScrollPane(new JList<>(aModel)), BorderLayout.CENTER);
            
            tabs.add("Create Society", createPanel);
            tabs.add("Deactivate Society", deactPanel);
            tabs.add("Monitor & Reports", repPanel);
            tabs.add("Audit Logs", auditPanel);
            frame.add(tabs);
            frame.setVisible(true);
        }
    }

    public static class AnnouncementsGUI {
        public AnnouncementsGUI(Models.User user) {
            JFrame frame = new JFrame("Announcements");
            frame.setSize(500, 400);
            frame.setLocationRelativeTo(null);
            JPanel panel = new JPanel(new BorderLayout(10,10));
            panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
            JTextArea display = new JTextArea();
            display.setEditable(false);
            display.setLineWrap(true);
            display.setWrapStyleWord(true);
            List<Models.Society> mySocs = Services.MembershipService.getMyMemberships(user.id);
            StringBuilder sb = new StringBuilder();
            for (Models.Society s : mySocs) {
                sb.append("--- ").append(s.name).append(" ---\n");
                List<Models.Announcement> anns = Services.AnnouncementService.getAnnouncementsForSociety(s.id);
                if (anns.isEmpty()) sb.append("No announcements.\n");
                for (Models.Announcement a : anns) {
                    sb.append("[").append(a.postedAt.toString()).append("] ").append(a.title).append("\n");
                    sb.append(a.content).append("\n\n");
                }
            }
            if (mySocs.isEmpty()) sb.append("Join a society to see announcements.");
            display.setText(sb.toString());
            panel.add(new JScrollPane(display), BorderLayout.CENTER);
            frame.add(panel);
            frame.setVisible(true);
        }
    }

    public static class DashboardGUI {
        public DashboardGUI(Models.User user) {
            JFrame frame = new JFrame("Dashboard - " + user.name);
            frame.setSize(700, 500);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout());
            JPanel topPanel = new JPanel();
            topPanel.setBackground(StyleUtils.PRIMARY);
            JLabel title = new JLabel("Welcome, " + user.name + " (" + user.role + ")");
            title.setForeground(Color.WHITE);
            title.setFont(StyleUtils.FONT_TITLE);
            topPanel.add(title);
            frame.add(topPanel, BorderLayout.NORTH);
            JPanel centerPanel = new JPanel(new GridLayout(4, 2, 15, 15));
            centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
            JButton profileBtn = new JButton("My Profile");
            JButton societyBtn = new JButton("Browse Societies");
            JButton myMemBtn = new JButton("My Memberships");
            JButton calendarBtn = new JButton("Event Calendar");
            JButton eventBtn = new JButton("Register Events");
            JButton annBtn = new JButton("Announcements");
            JButton notifBtn = new JButton("Notifications");
            StyleUtils.styleButton(profileBtn); StyleUtils.styleButton(societyBtn);
            StyleUtils.styleButton(myMemBtn); StyleUtils.styleButton(calendarBtn);
            StyleUtils.styleButton(eventBtn); StyleUtils.styleButton(annBtn);
            StyleUtils.styleButton(notifBtn);
            centerPanel.add(profileBtn); centerPanel.add(societyBtn);
            centerPanel.add(myMemBtn); centerPanel.add(calendarBtn);
            centerPanel.add(eventBtn); centerPanel.add(annBtn);
            centerPanel.add(notifBtn);
            profileBtn.addActionListener(e -> new ProfileGUI(user));
            societyBtn.addActionListener(e -> new SocietyGUI(user));
            myMemBtn.addActionListener(e -> new MyMembershipsGUI(user));
            calendarBtn.addActionListener(e -> new EventCalendarGUI());
            eventBtn.addActionListener(e -> new EventGUI(user));
            annBtn.addActionListener(e -> new AnnouncementsGUI(user));
            notifBtn.addActionListener(e -> new NotificationGUI(user));
            switch (user.role) {
                case ADMIN:
                    JButton adminBtn = new JButton("Admin Panel"); StyleUtils.styleButton(adminBtn);
                    centerPanel.add(adminBtn); adminBtn.addActionListener(e -> new AdminGUI(user)); break;
                case SOCIETY_HEAD:
                    JButton headBtn = new JButton("Manage Society"); StyleUtils.styleButton(headBtn);
                    centerPanel.add(headBtn); headBtn.addActionListener(e -> new HeadGUI(user)); break;
                default: centerPanel.add(new JLabel()); break;
            }
            JButton logoutBtn = new JButton("Logout");
            logoutBtn.setBackground(new Color(231, 76, 60)); logoutBtn.setForeground(Color.WHITE);
            logoutBtn.setFont(StyleUtils.FONT_BTN); centerPanel.add(logoutBtn);
            logoutBtn.addActionListener(e -> { frame.dispose(); new LoginGUI(); });
            frame.add(centerPanel, BorderLayout.CENTER);
            frame.setVisible(true);
        }
    }

    public static class EventCalendarGUI {
        public EventCalendarGUI() {
            JFrame frame = new JFrame("Event Calendar");
            frame.setSize(500, 400); frame.setLocationRelativeTo(null);
            JPanel panel = new JPanel(new BorderLayout(10,10));
            panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
            List<Models.Event> events = Services.EventService.getAllActiveEvents();
            events.sort((a,b) -> a.date.compareTo(b.date));
            DefaultListModel<String> model = new DefaultListModel<>();
            for (Models.Event e : events) model.addElement(e.date.toString() + " - " + e.title + " (" + e.participants.size() + "/" + e.maxParticipants + ")");
            if (events.isEmpty()) model.addElement("No events scheduled at this time.");
            panel.add(new JScrollPane(new JList<>(model)), BorderLayout.CENTER);
            frame.add(panel); frame.setVisible(true);
        }
    }

    public static class EventGUI {
        public EventGUI(Models.User user) {
            JFrame frame = new JFrame("Events");
            frame.setSize(500, 400); frame.setLocationRelativeTo(null); frame.setLayout(new BorderLayout());
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); panel.setBackground(StyleUtils.BACKGROUND);
            JLabel title = new JLabel("Upcoming Events"); StyleUtils.styleLabel(title, true); panel.add(title, BorderLayout.NORTH);
            DefaultListModel<String> model = new DefaultListModel<>();
            List<Models.Event> events = Services.EventService.getAllActiveEvents();
            for (Models.Event e : events) model.addElement(e.title + " | Part: " + e.participants.size() + "/" + e.maxParticipants);
            JList<String> list = new JList<>(model); list.setFont(StyleUtils.FONT_NORMAL);
            panel.add(new JScrollPane(list), BorderLayout.CENTER);
            JButton registerBtn = new JButton("Register for Event"); StyleUtils.styleButton(registerBtn); panel.add(registerBtn, BorderLayout.SOUTH);
            registerBtn.addActionListener(e -> {
                int index = list.getSelectedIndex();
                if (index != -1) {
                    Models.Event event = events.get(index);
                    try {
                        Services.EventService.register(user, event);
                        JOptionPane.showMessageDialog(frame, "Successfully registered for " + event.title);
                        model.set(index, event.title + " | Part: " + event.participants.size() + "/" + event.maxParticipants);
                    } catch (Exception ex) { JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
                }
            });
            frame.add(panel); frame.setVisible(true);
        }
    }

    public static class HeadGUI {
        public HeadGUI(Models.User head) {
            JFrame frame = new JFrame("Society Head Panel");
            frame.setSize(700, 500); frame.setLocationRelativeTo(null);
            List<Models.Society> mySocs = Services.SocietyService.getSocietiesByHead(head.id);
            if (mySocs.isEmpty()) { JOptionPane.showMessageDialog(null, "You are not a head of any society."); return; }
            Models.Society mySociety = mySocs.get(0);
            JTabbedPane tabs = new JTabbedPane();
            JPanel memPanel = new JPanel(new BorderLayout());
            DefaultListModel<String> memModel = new DefaultListModel<>();
            List<Models.Membership> reqs = Services.MembershipService.getPendingRequests(mySociety.id);
            for (Models.Membership m : reqs) memModel.addElement("User ID: " + m.userId);
            JList<String> reqList = new JList<>(memModel); memPanel.add(new JScrollPane(reqList), BorderLayout.CENTER);
            JPanel memBtns = new JPanel(); JButton appBtn = new JButton("Approve"); StyleUtils.styleButton(appBtn);
            JButton rejBtn = new JButton("Reject"); StyleUtils.styleButton(rejBtn);
            memBtns.add(appBtn); memBtns.add(rejBtn); memPanel.add(memBtns, BorderLayout.SOUTH);
            appBtn.addActionListener(e -> { int idx = reqList.getSelectedIndex(); if (idx!=-1) { Services.MembershipService.approve(head, reqs.get(idx)); memModel.remove(idx); } });
            rejBtn.addActionListener(e -> { int idx = reqList.getSelectedIndex(); if (idx!=-1) { String reason = JOptionPane.showInputDialog("Reason for rejection:"); Services.MembershipService.reject(head, reqs.get(idx), reason != null ? reason : ""); memModel.remove(idx); } });
            
            JPanel evPanel = new JPanel(new BorderLayout());
            JPanel createEvP = new JPanel(new GridLayout(6,2));
            createEvP.add(new JLabel("Title:")); JTextField tF = new JTextField(); createEvP.add(tF);
            createEvP.add(new JLabel("Description:")); JTextField dF = new JTextField(); createEvP.add(dF);
            createEvP.add(new JLabel("Venue:")); JTextField vF = new JTextField(); createEvP.add(vF);
            createEvP.add(new JLabel("Days until event:")); JTextField daysF = new JTextField(); createEvP.add(daysF);
            createEvP.add(new JLabel("Max Part:")); JTextField mF = new JTextField(); createEvP.add(mF);
            JButton cEvBtn = new JButton("Create Event"); StyleUtils.styleButton(cEvBtn); createEvP.add(new JLabel()); createEvP.add(cEvBtn);
            evPanel.add(createEvP, BorderLayout.NORTH);
            DefaultListModel<String> eModel = new DefaultListModel<>();
            List<Models.Event> evs = Services.EventService.getAllActiveEvents();
            for (Models.Event e: evs) if(e.societyId.equals(mySociety.id)) eModel.addElement(e.title);
            JList<String> eList = new JList<>(eModel); evPanel.add(new JScrollPane(eList), BorderLayout.CENTER);
            JButton cxBtn = new JButton("Cancel Selected Event"); StyleUtils.styleButton(cxBtn); evPanel.add(cxBtn, BorderLayout.SOUTH);
            cEvBtn.addActionListener(e -> {
                try {
                    int days = Integer.parseInt(daysF.getText());
                    Date evDate = new Date(System.currentTimeMillis() + (long)days * 86400000L);
                    Date regDate = new Date(System.currentTimeMillis() + (long)(days - 1) * 86400000L);
                    Models.Event ev = Services.EventService.createEvent(head, mySociety, tF.getText(), dF.getText(), vF.getText(), evDate, regDate, Integer.parseInt(mF.getText()));
                    eModel.addElement(ev.title); JOptionPane.showMessageDialog(frame, "Event created successfully!");
                } catch(Exception ex) { JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
            });
            cxBtn.addActionListener(e -> { int idx = eList.getSelectedIndex(); if (idx!=-1) { for (Models.Event ev: evs) if (ev.title.equals(eModel.get(idx))) { Services.EventService.cancel(head, ev); break; } eModel.remove(idx); } });
            
            JPanel annP = new JPanel(new BorderLayout());
            JPanel annTop = new JPanel(new GridLayout(2,2));
            annTop.add(new JLabel("Title:")); JTextField anTitle = new JTextField(); annTop.add(anTitle);
            annTop.add(new JLabel("Content:")); JTextArea anContent = new JTextArea(); annTop.add(new JScrollPane(anContent));
            annP.add(annTop, BorderLayout.CENTER); JButton pAnn = new JButton("Post Announcement"); StyleUtils.styleButton(pAnn);
            annP.add(pAnn, BorderLayout.SOUTH); pAnn.addActionListener(e -> { try { Services.AnnouncementService.postAnnouncement(head, mySociety.id, anTitle.getText(), anContent.getText()); JOptionPane.showMessageDialog(frame, "Posted!"); } catch(Exception ex) {} });
            
            JPanel finP = new JPanel(new GridLayout(2,1));
            JPanel duesP = new JPanel(new FlowLayout()); duesP.add(new JLabel("User ID:")); JTextField dUser = new JTextField(10); duesP.add(dUser);
            duesP.add(new JLabel("Amount:")); JTextField dAmt = new JTextField(5); duesP.add(dAmt);
            JButton pDues = new JButton("Track Dues"); StyleUtils.styleButton(pDues); duesP.add(pDues);
            pDues.addActionListener(e -> Services.FinanceService.trackDues(mySociety, dUser.getText(), Double.parseDouble(dAmt.getText())));
            JPanel budP = new JPanel(new FlowLayout()); budP.add(new JLabel("Event ID:")); JTextField bEv = new JTextField(10); budP.add(bEv);
            budP.add(new JLabel("Est Exp:")); JTextField bEst = new JTextField(5); budP.add(bEst);
            JButton pBud = new JButton("Set Budget"); StyleUtils.styleButton(pBud); budP.add(pBud);
            pBud.addActionListener(e -> Services.FinanceService.createEventBudget(bEv.getText(), Double.parseDouble(bEst.getText()), "Event Budget"));
            finP.add(duesP); finP.add(budP);
            
            tabs.add("Memberships", memPanel); tabs.add("Events", evPanel); tabs.add("Announcements", annP); tabs.add("Finance", finP);
            frame.add(tabs); frame.setVisible(true);
        }
    }

    public static class LoginGUI {
        public LoginGUI() {
            JFrame frame = new JFrame("Login - Society Management System");
            frame.setSize(450, 350); frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); frame.setLocationRelativeTo(null); frame.setLayout(new BorderLayout());
            JPanel panel = new JPanel(new GridLayout(5, 2, 10, 20)); panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
            JLabel lblTitle = new JLabel("Login", SwingConstants.CENTER); StyleUtils.styleLabel(lblTitle, true);
            panel.add(new JLabel("Email:")); JTextField emailField = new JTextField(); panel.add(emailField);
            panel.add(new JLabel("Password:")); JPasswordField passField = new JPasswordField(); panel.add(passField);
            JButton loginBtn = new JButton("Login"); StyleUtils.styleButton(loginBtn);
            JButton registerBtn = new JButton("Register New"); StyleUtils.styleButton(registerBtn); registerBtn.setBackground(new Color(46, 204, 113));
            panel.add(registerBtn); panel.add(loginBtn);
            frame.add(lblTitle, BorderLayout.NORTH); frame.add(panel, BorderLayout.CENTER);
            loginBtn.addActionListener(e -> {
                try {
                    Models.User user = Services.UserService.login(emailField.getText(), new String(passField.getPassword()));
                    frame.dispose(); new DashboardGUI(user);
                } catch (Exception ex) { JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
            });
            registerBtn.addActionListener(e -> { frame.dispose(); new RegisterGUI(); });
            frame.setVisible(true);
        }
    }

    public static class MyMembershipsGUI {
        public MyMembershipsGUI(Models.User user) {
            JFrame frame = new JFrame("My Memberships");
            frame.setSize(400, 300); frame.setLocationRelativeTo(null);
            JPanel panel = new JPanel(new BorderLayout(10, 10)); panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            List<Models.Society> mySocs = Services.MembershipService.getMyMemberships(user.id);
            if (mySocs.isEmpty()) panel.add(new JLabel("You are not a member of any society yet.", SwingConstants.CENTER));
            else {
                DefaultListModel<String> model = new DefaultListModel<>();
                for (Models.Society s : mySocs) model.addElement(s.name + " (" + s.category + ")");
                panel.add(new JScrollPane(new JList<>(model)), BorderLayout.CENTER);
            }
            frame.add(panel); frame.setVisible(true);
        }
    }

    public static class NotificationGUI {
        public NotificationGUI(Models.User user) {
            JFrame frame = new JFrame("Notifications");
            frame.setSize(400, 300); frame.setLocationRelativeTo(null); frame.setLayout(new BorderLayout());
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); panel.setBackground(StyleUtils.BACKGROUND);
            JLabel title = new JLabel("Your Notifications"); StyleUtils.styleLabel(title, true); panel.add(title, BorderLayout.NORTH);
            DefaultListModel<String> model = new DefaultListModel<>();
            List<Models.Notification> notifs = Services.NotificationService.getUserNotifications(user.id);
            for (Models.Notification n : notifs) model.addElement((n.isRead ? "" : "[NEW] ") + n.message);
            JList<String> list = new JList<>(model); list.setFont(StyleUtils.FONT_NORMAL);
            panel.add(new JScrollPane(list), BorderLayout.CENTER);
            JButton markBtn = new JButton("Mark as Read"); StyleUtils.styleButton(markBtn); panel.add(markBtn, BorderLayout.SOUTH);
            markBtn.addActionListener(e -> {
                int index = list.getSelectedIndex();
                if (index != -1) {
                    Models.Notification n = notifs.get(index);
                    Services.NotificationService.markAsRead(n.id);
                    model.set(index, n.message);
                }
            });
            frame.add(panel); frame.setVisible(true);
        }
    }

    public static class ProfileGUI {
        public ProfileGUI(Models.User user) {
            JFrame frame = new JFrame("My Profile");
            frame.setSize(400, 350); frame.setLocationRelativeTo(null);
            JPanel panel = new JPanel(new GridLayout(5, 2, 10, 15)); panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            panel.add(new JLabel("Name:")); JTextField nameF = new JTextField(user.name); panel.add(nameF);
            panel.add(new JLabel("Degree Program:")); JTextField degF = new JTextField(user.degree); panel.add(degF);
            panel.add(new JLabel("Contact Number:")); JTextField contF = new JTextField(user.contactNumber); panel.add(contF);
            JButton saveBtn = new JButton("Save Changes"); StyleUtils.styleButton(saveBtn);
            panel.add(new JLabel()); panel.add(saveBtn);
            saveBtn.addActionListener(e -> {
                try {
                    Services.UserService.updateProfile(user, nameF.getText(), degF.getText(), contF.getText());
                    JOptionPane.showMessageDialog(frame, "Profile updated successfully!"); frame.dispose();
                } catch (Exception ex) { JOptionPane.showMessageDialog(frame, ex.getMessage()); }
            });
            frame.add(panel); frame.setVisible(true);
        }
    }

    public static class RegisterGUI {
        public RegisterGUI() {
            JFrame frame = new JFrame("Register - Society Management System");
            frame.setSize(450, 400); frame.setLocationRelativeTo(null); frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10)); panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            panel.add(new JLabel("Full Name:")); JTextField nameF = new JTextField(); panel.add(nameF);
            panel.add(new JLabel("Student ID:")); JTextField stIdF = new JTextField(); panel.add(stIdF);
            panel.add(new JLabel("University Email:")); JTextField emailF = new JTextField(); panel.add(emailF);
            panel.add(new JLabel("Password:")); JPasswordField passF = new JPasswordField(); panel.add(passF);
            JButton regBtn = new JButton("Register"); StyleUtils.styleButton(regBtn);
            JButton backBtn = new JButton("Back to Login"); StyleUtils.styleButton(backBtn); backBtn.setBackground(Color.GRAY);
            panel.add(backBtn); panel.add(regBtn);
            regBtn.addActionListener(e -> {
                try {
                    Services.UserService.register(nameF.getText(), emailF.getText(), stIdF.getText(), new String(passF.getPassword()), Models.Enums.Role.STUDENT);
                    JOptionPane.showMessageDialog(frame, "Registration successful! You can now log in.");
                    frame.dispose(); new LoginGUI();
                } catch(Exception ex) { JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
            });
            backBtn.addActionListener(e -> { frame.dispose(); new LoginGUI(); });
            frame.add(panel); frame.setVisible(true);
        }
    }

    public static class SocietyGUI {
        public SocietyGUI(Models.User user) {
            JFrame frame = new JFrame("Societies");
            frame.setSize(500, 400); frame.setLocationRelativeTo(null); frame.setLayout(new BorderLayout());
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); panel.setBackground(StyleUtils.BACKGROUND);
            JLabel title = new JLabel("Available Societies"); StyleUtils.styleLabel(title, true); panel.add(title, BorderLayout.NORTH);
            DefaultListModel<String> model = new DefaultListModel<>();
            List<Models.Society> societies = Services.SocietyService.getAllSocieties();
            for (Models.Society s : societies) model.addElement(s.name + " (" + s.category + ")");
            JList<String> list = new JList<>(model); list.setFont(StyleUtils.FONT_NORMAL);
            panel.add(new JScrollPane(list), BorderLayout.CENTER);
            JButton joinBtn = new JButton("Request to Join"); StyleUtils.styleButton(joinBtn); panel.add(joinBtn, BorderLayout.SOUTH);
            joinBtn.addActionListener(e -> {
                int index = list.getSelectedIndex();
                if (index != -1) {
                    Models.Society s = societies.get(index);
                    try {
                        Services.MembershipService.request(user, s);
                        JOptionPane.showMessageDialog(frame, "Membership Request Sent to " + s.name);
                    } catch (Exception ex) { JOptionPane.showMessageDialog(frame, ex.getMessage(), "Info", JOptionPane.INFORMATION_MESSAGE); }
                }
            });
            frame.add(panel); frame.setVisible(true);
        }
    }
}
