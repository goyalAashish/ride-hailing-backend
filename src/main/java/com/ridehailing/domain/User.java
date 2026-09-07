package com.ridehailing.domain;

import java.util.Objects;

/**
 * Rider account. {@code phone} is unique across the system.
 */
public class User {

    private Long userId;
    private String name;
    private String phone;

    public User() {
    }

    public User(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User user) || userId == null || user.userId == null) {
            return false;
        }
        return userId.equals(user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }
}
