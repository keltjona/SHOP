package com.example.shop;

import java.io.*;
import java.util.ArrayList;

public class UserManager {
    private ArrayList<User> users;
    private final String filePath = "users.bin";

    public UserManager() {
        users = new ArrayList<>();
        loadUsers();
    }

    public void addUser(User user) {
        users.add(user);
        saveUsers();
    }

    public void removeUser(String username) {
        users.removeIf(user -> user.getUsername().equals(username));
        saveUsers();

    }

    public User findUser(String username) {
        return users.stream().filter(user -> user.getUsername().equals(username)).findFirst().orElse(null);
    }

    private void saveUsers() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<User> getAllUsers() {
        return users;
    }

    @SuppressWarnings("unchecked")
    private void loadUsers() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            Object data = in.readObject();
            if (data instanceof ArrayList<?>) {
                this.users = (ArrayList<User>) data;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
