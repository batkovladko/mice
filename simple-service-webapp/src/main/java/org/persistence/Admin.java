package org.persistence;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ADMIN")
public class Admin {

    @GeneratedValue(strategy=GenerationType.AUTO)
    @Id
    private long id;
    @Basic
    private String username;
    @Basic
    private String password;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setUsername(final String param) {
        this.username = param;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(final String param) {
        this.password = param;
    }

    public String getPassword() {
        return password;
    }

}