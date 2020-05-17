package Data;

import Model.Team;
import Model.UsersTypes.Subscriber;
import Model.UsersTypes.TeamOwner;

import java.util.*;

public class TeamOwnerDbInMemory implements TeamOwnerDb{

    /*structure like the DB of teamOwners*/
    private Map<String, TeamOwner> teamOwners;

    public TeamOwnerDbInMemory() {
        teamOwners = new HashMap<>();
    }

    private static TeamOwnerDbInMemory ourInstance = new TeamOwnerDbInMemory();

    public static TeamOwnerDbInMemory getInstance() {
        return ourInstance;
    }
    @Override
    public void createTeamOwner(TeamOwner teamOwner) throws Exception {
        if(teamOwner == null) {
            throw new NullPointerException("bad input");
        }
        String teamOwnerEmailAddress = teamOwner.getEmailAddress();
        if (teamOwners.containsKey(teamOwnerEmailAddress)) {
            throw new Exception("TeamOwner already exists");
        }
        teamOwners.put(teamOwnerEmailAddress, teamOwner);
    }

    @Override
    public void updateTeamOwnerTeam(Team team, String teamOwnerEmailAddress) throws Exception {
        TeamOwner teamOwner = teamOwners.get(teamOwnerEmailAddress);
        if(teamOwner.getTeam() != null){
            throw new Exception("This teamOwner has already team");
        }
        teamOwner.setTeam(team);
        team.getTeamOwners().put(teamOwnerEmailAddress,teamOwner);
    }

    @Override
    public TeamOwner getTeamOwner(String teamOwnerEmailAddress) throws Exception {
        if(teamOwnerEmailAddress == null || !teamOwners.containsKey(teamOwnerEmailAddress)){
            throw new NotFoundException("TeamOwner not found");
        }
        return teamOwners.get(teamOwnerEmailAddress);
    }

    @Override
    public void subscriptionTeamOwner(Team team, String teamOwnerEmail, Subscriber subscriber) throws Exception {
        if(team == null || teamOwnerEmail == null || subscriber == null){
            throw new NullPointerException();
        }
        if(teamOwners.containsKey(subscriber.getEmailAddress())){
            throw new Exception("TeamOwner to add already exists");
        }
        if(!teamOwners.containsKey(teamOwnerEmail)){
            throw new Exception("Major Team Owner not found");
        }
        TeamOwner teamOwner = new TeamOwner(team,subscriber,teamOwnerEmail);
        String emailAddressToAdd = teamOwner.getEmailAddress();
        TeamOwner teamOwnerMajor = teamOwners.get(teamOwnerEmail);
        teamOwnerMajor.getTeamOwnersByThis().put(teamOwner.getEmailAddress(),teamOwner);
        teamOwners.put(emailAddressToAdd,teamOwner);
        Map<String, TeamOwner> teamOwners = team.getTeamOwners();
        teamOwners.put(emailAddressToAdd,teamOwner);
    }

    @Override
    public void removeSubscriptionTeamOwner(String ownerToRemoveEmail) throws Exception {
        if(ownerToRemoveEmail == null){
            throw new NullPointerException();
        }
        if(!teamOwners.containsKey(ownerToRemoveEmail)){
            throw new Exception("TeamOwner not found");
        }

        TeamOwner removeTeamOwner = teamOwners.remove(ownerToRemoveEmail);
        String ownedByEmailAddress = removeTeamOwner.getOwnedByEmailAddress();
        TeamOwner teamOwner = getTeamOwner(ownedByEmailAddress);
        Map<String, TeamOwner> teamOwnersByThis = teamOwner.getTeamOwnersByThis();
        teamOwnersByThis.remove(ownerToRemoveEmail);
        Team team = removeTeamOwner.getTeam();
        team.getTeamOwners().remove(ownerToRemoveEmail);
    }

    @Override
    public List<String> getAllTeamOwnersOwnedBy(String teamOwnerEmail) {
        List<String> teamOwnersOwnedBy = new ArrayList<>();
        for (TeamOwner tOwner: teamOwners.values()) {
            if(teamOwnerEmail.equals(tOwner.getOwnedByEmailAddress())){
                teamOwnersOwnedBy.add(tOwner.getEmailAddress());
            }
        }
        return teamOwnersOwnedBy;
    }

    @Override
    public Set<String> getAllTeamOwnersInDB() {
        return teamOwners.keySet();
    }

    @Override
    public void deleteAll() {
        teamOwners.clear();
    }

}