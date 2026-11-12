package com.scc.model;

public record ActivityLog (String username, String role, String status, String LoginDate){
    @Override
    public String toString(){return String.format("%s %s %s %s", username, role, status, LoginDate);}

}
