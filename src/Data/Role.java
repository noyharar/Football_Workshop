package Data;

import Model.Enums.RoleType;

import java.sql.Timestamp;
import java.util.Date;

public class Role {
    private String teamName;
    private RoleType roleType;
    private Long assignedDate;

    public Role(String teamName, RoleType roleType) {
        this.teamName = teamName;
        this.roleType = roleType;
        this.assignedDate = System.currentTimeMillis();
    }

    public Role(RoleType roleType) {
        this.roleType = roleType;
        this.assignedDate  = System.currentTimeMillis();;
    }


    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public Long getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(Long assignedDate) {
        this.assignedDate = assignedDate;
    }
}