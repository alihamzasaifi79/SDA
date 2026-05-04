package com.society.main;

public class Main {
    public static void main(String[] args) {
        GUIs.StyleUtils.applyTheme();
        
        // Seed initial data
        Services.UserService.register("Admin", "admin@nu.edu.pk", "A1", "123", Models.Enums.Role.ADMIN);
        Services.UserService.register("Sara", "sara@nu.edu.pk", "S1", "123", Models.Enums.Role.STUDENT);
        Services.UserService.register("Ali", "ali@nu.edu.pk", "S2", "123", Models.Enums.Role.STUDENT);
        
        System.out.println("System initialized with test users.");
        System.out.println("Admin: admin@nu.edu.pk / 123");
        System.out.println("Student: sara@nu.edu.pk / 123");
        System.out.println("Student: ali@nu.edu.pk / 123");

        new GUIs.LoginGUI();
    }
}
