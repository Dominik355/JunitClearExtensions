package org.example.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.StringJoiner;

@Entity
@Table(name = "users")
public class User {

    public User() {}

    public User(String username, String role) {
        this.username = username;
        this.role = role;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, insertable = false, updatable = false)
    private Integer id;

    @Column(name = "username", unique = true, length = 50)
    private String username;

    @Column(name = "role")
    private String role;

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", User.class.getSimpleName() + "[", "]").add("id=" + id)
                                                                            .add("username='" + username + "'")
                                                                            .add("role=" + role)
                                                                            .toString();
    }
}
