package org.example.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.StringJoiner;

@Entity
@Table(name = "device")
public class Device {

    public Device() {}

    public Device(String name, String operatingSystem) {
        this.name = name;
        this.operatingSystem = operatingSystem;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, insertable = false, updatable = false)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "operating_system")
    private String operatingSystem;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Device.class.getSimpleName() + "[", "]").add("id=" + id)
                                                                              .add("name='" + name + "'")
                                                                              .add("operatingSystem='" + operatingSystem + "'")
                                                                              .toString();
    }
}