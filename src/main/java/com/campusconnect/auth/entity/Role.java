package com.campusconnect.auth.entity;

public enum Role {
    STUDENT,
    RECRUITER,
    PLACEMENT_OFFICER;

    public String getAuthority() {
        return "ROLE_" + name();
    }
}
