package Controller;

import Data.*;
import Model.*;
import Model.Enums.*;
import Model.UsersTypes.*;

import java.util.*;

public class TeamOwnerController {
    private TeamDb teamDb;
    private PlayerDb playerDb;
    private TeamManagerDb teamManagerDb;
    private CourtDb courtDb;
    private CoachDb coachDb;
    private TeamOwnerDb teamOwnerDb;
    private SubscriberDb subscriberDb;
    private RoleDb roleDb;
    private FinancialActivityDb financialActivityDb;
    private PageDb pageDb;
    private PermissionsDb permissionDb;

    public TeamDb getTeamDb() {
        return teamDb;
    }

    public TeamOwnerDb getTeamOwnerDb() {
        return teamOwnerDb;
    }

    public TeamOwnerController(){
        teamDb =  TeamDbInMemory.getInstance();
        playerDb = PlayerDbInMemory.getInstance();
        teamManagerDb = TeamManagerDbInMemory.getInstance();
        coachDb =  CoachDbInMemory.getInstance();
        courtDb =  CourtDbInMemory.getInstance();
        teamOwnerDb =  TeamOwnerDbInMemory.getInstance();
        subscriberDb =  SubscriberDbInMemory.getInstance();
        roleDb =  RoleDbInMemory.getInstance();
        financialActivityDb =  FinancialActivityDbInMemory.getInstance();
        pageDb = PageDbInMemory.getInstance();
        permissionDb = PermissionDbInMemory.getInstance();
    }

    /**
     * get team from db
     * @param teamName
     * @return
     * @throws Exception
     */
    public Team getTeam(String teamName) throws Exception {
        if(teamName == null) {
            throw new NullPointerException("bad input");
        }
       return teamDb.getTeam(teamName);
    }

    /**
     * create new team
     * @param teamName
     * @param teamOwnerEmail
     * @param players
     * @param coaches
     * @param teamManagers
     * @param court
     * @throws Exception
     */
    public void createNewTeam(String teamName, String teamOwnerEmail, List<Player> players, List<Coach> coaches, List<TeamManager> teamManagers, Court court, Double budget) throws Exception {
       if(teamName == null || teamOwnerEmail == null || players == null || coaches == null || teamManagers == null || court == null){
           throw new NullPointerException("bad input");
       }
        checkPermissions(teamOwnerEmail,null,PermissionType.CREATE_NEW_TEAM);
        TeamOwner teamOwner = teamOwnerDb.getTeamOwner(teamOwnerEmail);
        teamDb.createTeam(teamName);
        teamOwnerDb.updateTeamOwnerTeam(teamDb.getTeam(teamName),teamOwnerEmail);
        for (Player player : players) {

            addPlayer(teamName,teamOwnerEmail,player.getEmailAddress(),player.getId(),player.getFirstName(),player.getLastName(),player.getBirthDate(),player.getPlayerRole());
        }
        for (Coach coach : coaches) {
            addCoach(teamName,teamOwnerEmail,coach.getEmailAddress(),coach.getId(),coach.getFirstName(),coach.getLastName(),coach.getCoachRole(),coach.getQualificationCoach());
        }
        for (TeamManager teamManager : teamManagers) {
            addTeamManager(teamName,teamManager.getEmailAddress(),teamManager.getId(),teamManager.getFirstName(),teamManager.getLastName(),teamManager.getPermissionTypes(),teamManager.getOwnedByEmail());
        }
        addCourt(teamName,teamOwnerEmail,court.getCourtName(),court.getCourtCity());
        Team team = getTeam(teamName);
        team.setBudget(budget);
        TeamPage teamPage = new TeamPage(teamName, team);
        pageDb.createTeamPage(teamName, team);
        teamDb.addTeamPage(teamPage);
    }


    /**
     * add player to team
     * @param teamName
     * @param playerId
     * @throws Exception
     */
    public void addPlayer(String teamName,String ownerEmail, String emailAddress, Integer playerId, String firstName, String lastName, Date birthDate, PlayerRole playerRole) throws Exception {
        if(teamName == null || ownerEmail == null || emailAddress == null || playerId == null || firstName == null || lastName == null || birthDate == null || playerRole == null) {
            throw new NullPointerException("bad input");
        }
        /*check if the team already exists*/
        Team team = teamDb.getTeam(teamName);
        /*check if the subscription has permission to do this action*/
        checkPermissions(ownerEmail,teamName,PermissionType.ADD_PLAYER);
        /*check if the team in active status*/
        checkTeamStatusIsActive(team);
        Player currPlayer = new Player(emailAddress,playerId,firstName,lastName,birthDate,playerRole);
        Player player;
        try{
            /*get the player from DB if exists*/
            player = playerDb.getPlayer(emailAddress);
            /*get the team of the player if there is a team already, will throw exception*/
            if (player.getTeam() != null) {
                if(teamName.equals(player.getTeam().getTeamName())){
                    throw new Exception("Player associated this team");
                }
                throw new Exception("Player associated with a team");
            }
            /*check if the player's details match with the DB details*/
            if(!equalsDetailsPlayer(player,currPlayer)){
                throw new Exception("One or more of the details incorrect");
            }
        }catch(NotFoundException e){
            /*if player not exists*/
            try {
                /*check if there is other subscriber already*/
                subscriberDb.getSubscriber(emailAddress);
                List<Role> roles = roleDb.getRoles(emailAddress);
                boolean isCanBePlayer = false;
                /*check if the player has subscriber with TeamOwner type in the same team*/
                for (Role role : roles) {
                    if(RoleType.TEAM_OWNER.equals(role.getRoleType())){
                        if(teamName.equals(role.getTeamName())) {
                            isCanBePlayer = true;
                        }else{
                            throw new Exception("The player to added already has other team");
                        }
                    }
                }
                if(!isCanBePlayer){
                    throw new Exception("The player to added already has other subscriber type");
                }
            } catch(NotFoundException ex) {
                /*give random password to player when open new subscriber*/
                currPlayer.setPassword(UUID.randomUUID().toString());
                subscriberDb.createSubscriber(currPlayer);
                /*Player doesnt exist in the db - add to players's db*/
                // TODO: 14/04/2020 add message to the new subscriber
            }
            playerDb.createPlayer(currPlayer);
            player = currPlayer;
        }
        /*add to DB the player to the team*/
        teamDb.addPlayer(teamName, player);
        roleDb.createRole(emailAddress,teamName, RoleType.PLAYER);
    }

    /**
     * check if all the information about the play want to add match with the db details
     * @param playerInDb
     * @param playerToAdd
     * @return
     */
    private boolean equalsDetailsPlayer(Player playerInDb, Player playerToAdd){
        return Objects.equals(playerInDb.getEmailAddress(), playerToAdd.getEmailAddress()) &&
                playerInDb.getId().equals(playerToAdd.getId()) &&
                playerInDb.getFirstName().equals(playerToAdd.getFirstName()) &&
                playerInDb.getLastName().equals(playerToAdd.getLastName()) &&
                playerInDb.getBirthDate().equals(playerToAdd.getBirthDate()) &&
                playerInDb.getPlayerRole().equals(playerToAdd.getPlayerRole());
    }

    /**
     * add teamManger to team if the teamManager exists in the system as teamManager or if not exists
     * @param teamName
     * @param emailAddress
     * @param teamManagerId
     * @param firstName
     * @param lastName
     * @param ownedByEmail
     * @throws Exception
     */
    public void addTeamManager(String teamName, String emailAddress, Integer teamManagerId, String firstName ,String lastName,List<PermissionType> permissions,String ownedByEmail) throws Exception {
        if(teamName == null || emailAddress == null ||teamManagerId == null || firstName == null || lastName == null || ownedByEmail == null) {
            throw new NullPointerException("bad input");
        }
        Team team = teamDb.getTeam(teamName);
        checkPermissions(ownedByEmail,teamName,PermissionType.ADD_TEAM_MANAGER);
        checkTeamStatusIsActive(team);
        TeamManager currTeamManager = new TeamManager(emailAddress,teamManagerId, firstName, lastName,ownedByEmail);
        /*get the teamManager from DB*/
        TeamManager teamManager;
        try{
            teamManager = teamManagerDb.getTeamManager(emailAddress);
            /*get the team of the teamManager if there is a team already, will throw exception*/
            if (teamManager.getTeam() != null) {
                if(teamName.equals(teamManager.getTeam().getTeamName())){
                    throw new Exception("TeamManager associated with a this team");
                }
                throw new Exception("Team Manager associated with a team");
            }
            /*check if the teamManager's details match with the DB details*/
            if(!equalsDetailsTeamManager(teamManager,currTeamManager)){
                throw new Exception("One or more of the details incorrect");
            }
            if(teamManager.getOwnedByEmail() != null){
                throw new Exception("Team Manager owned by another teamOwner");
            }
        }catch (NotFoundException e){
            try {
                /*check if there is other subscriber already*/
                subscriberDb.getSubscriber(emailAddress);
                List<Role> roles = roleDb.getRoles(emailAddress);
                boolean isCanBePlayer = false;
                /*check if the player has subscriber with TeamOwner type in the same team*/
                for (Role role : roles) {
                    if(RoleType.TEAM_OWNER.equals(role.getRoleType())){
                        if(teamName.equals(role.getTeamName())) {
                            isCanBePlayer = true;
                        }else{
                            throw new Exception("The teamManager to added already has other team");
                        }
                    }
                }
                if(!isCanBePlayer){
                    throw new Exception("The teamManager to added already has other subscriber type");
                }
            } catch(NotFoundException ex) {
                /*give random password to player when open new subscriber*/
                currTeamManager.setPassword(UUID.randomUUID().toString());
                subscriberDb.createSubscriber(currTeamManager);
                /*teamManager doesnt exist in the db - add to teamManagers's db*/
                // TODO: 14/04/2020 add message to the new subscriber
            }
            teamManagerDb.createTeamManager(currTeamManager);
            teamManager = currTeamManager;
        }
        /*add to DB the teamManager to the team*/
        teamDb.addTeamManager(teamName, teamManager,permissions,ownedByEmail);
        roleDb.createRole(emailAddress,teamName, RoleType.TEAM_MANAGER);
    }

    private boolean equalsDetailsTeamManager(TeamManager teamManagerInDb, TeamManager teamManagerToAdd){
        return (teamManagerInDb.getEmailAddress().equals(teamManagerToAdd.getEmailAddress()) &&
                teamManagerInDb.getId().equals(teamManagerToAdd.getId()) &&
                teamManagerInDb.getFirstName().equals(teamManagerToAdd.getFirstName()) &&
                teamManagerInDb.getLastName().equals(teamManagerToAdd.getLastName()));
    }

    /**
     * add coach to the team
     * @param teamName
     * @param emailAddress
     * @param coachId
     * @param firstName
     * @param lastName
     * @param coachRole
     * @param qualificationCoach
     * @throws Exception
     */
    public void addCoach(String teamName, String ownerEmail,String emailAddress, Integer coachId, String firstName, String lastName, CoachRole coachRole, QualificationCoach qualificationCoach) throws Exception {
        if(teamName == null || emailAddress == null || coachId == null || firstName == null || lastName == null || coachRole == null|| qualificationCoach == null) {
            throw new NullPointerException("bad input");
        }
        Team team = teamDb.getTeam(teamName);
        checkPermissions(ownerEmail,teamName,PermissionType.ADD_COACH);
        checkTeamStatusIsActive(team);
        Coach currCoach = new Coach(emailAddress, coachId, firstName, lastName, coachRole, qualificationCoach);
        /*get the coach from DB*/
        Coach coach;
        try {
            coach = coachDb.getCoach(emailAddress);
            /*get the team of the coach if there is a team already, will throw exception*/
            if (coach.getTeam() != null) {
                if(teamName.equals(coach.getTeam().getTeamName())){
                    throw new Exception("Coach associated this team");
                }
                throw new Exception("Coach associated with a team");
            }
            /*check if the coach's details match with the DB details*/
            if(!equalsDetailsCoach(coach,currCoach)){
                throw new Exception("One or more of the details incorrect");
            }
        }catch (NotFoundException e){
            try {
                /*check if there is other subscriber already*/
                subscriberDb.getSubscriber(emailAddress);
                List<Role> roles = roleDb.getRoles(emailAddress);
                boolean isCanBePlayer = false;
                /*check if the player has subscriber with TeamOwner type in the same team*/
                for (Role role : roles) {
                    if(RoleType.TEAM_OWNER.equals(role.getRoleType()) || RoleType.TEAM_MANAGER.equals(role.getRoleType()) || RoleType.PLAYER.equals(role.getRoleType())){
                        if(teamName.equals(role.getTeamName())) {
                            isCanBePlayer = true;
                        }else{
                            throw new Exception("The coach to added already has other team");
                        }
                    }
                }
                if(!isCanBePlayer){
                    throw new Exception("The coach to added already has other subscriber type");
                }
            } catch(NotFoundException ex) {
                /*give random password to player when open new subscriber*/
                currCoach.setPassword(UUID.randomUUID().toString());
                /*create subscriber in db*/
                subscriberDb.createSubscriber(currCoach);
                /*Coach doesnt exist in the db - add to coachs's db*/
                // TODO: 14/04/2020 add message to the new subscriber
            }
            coachDb.createCoach(currCoach);
            coach = currCoach;
        }
        /* add to DB the player to the team*/
        teamDb.addCoach(teamName, coach);
        roleDb.createRole(emailAddress,teamName, RoleType.COACH);
    }

    private boolean equalsDetailsCoach(Coach coachInDb, Coach coachToAdd){
        return (coachInDb.getEmailAddress().equals(coachToAdd.getEmailAddress()) &&
                coachInDb.getId().equals(coachToAdd.getId()) &&
                coachInDb.getFirstName().equals(coachToAdd.getFirstName()) &&
                coachInDb.getLastName().equals(coachToAdd.getLastName()) &&
                coachInDb.getCoachRole().equals(coachToAdd.getCoachRole()) &&
                coachInDb.getQualificationCoach().equals(coachToAdd.getQualificationCoach()));
    }

    /**
     * add court to the team
     * @param teamName
     * @param courtName
     * @param courtCity
     * @throws Exception
     */
    public void addCourt(String teamName,String ownerEmail, String courtName, String courtCity) throws Exception {
        if (teamName == null || ownerEmail == null || courtName == null || courtCity == null) {
            throw new NullPointerException("bad input");
        }
        Team team = teamDb.getTeam(teamName);
        checkPermissions(ownerEmail,teamName,PermissionType.ADD_COURT);
        /*check if the team exists*/
        checkTeamStatusIsActive(team);
        Court court;
        try {
            /*check if the court already in the db*/
            court = courtDb.getCourt(courtName);
            if(team.getCourt()!= null){
                throw new Exception("team already associated with court");
            }
            if (!courtCity.equals(court.getCourtCity())) {
                throw new Exception("The court name isn't match to the city");
            }
        } catch (NotFoundException e) { //court in the db
//            /*check if the court associated with team*/
//            Team team = court.getTeam();
//            if (team != null) {
//                throw new Exception("There is a court associated with this team");
            court = new Court(courtName, courtCity);
            courtDb.createCourt(court);
            courtDb.addTeamToCourt(court,team);
        }
        teamDb.addCourt(teamName, court);
    }

    /**
     * remove player from the team
     * @param teamName
     * @param playerEmailAddress
     * @throws Exception
     */
    public void removePlayer(String teamName,String ownerEmail, String playerEmailAddress) throws Exception {
        /*check if one of the inputs null*/
        if(teamName == null || ownerEmail == null || playerEmailAddress == null) {
            throw new NullPointerException("bad input");
        }
        Team team = teamDb.getTeam(teamName);
        checkPermissions(ownerEmail,teamName,PermissionType.REMOVE_PLAYER);
        checkTeamStatusIsActive(team);
        /* get the player from the database*/
        Player player = playerDb.getPlayer(playerEmailAddress);
        /*check if the team that associated with the player match to the player want to delete*/
        Team teamPlayer = player.getTeam();
        if(teamPlayer == null || !teamName.equals(teamPlayer.getTeamName())) {
            throw new Exception("Player is not part with associated team");
        }
        teamDb.removePlayer(teamName, playerEmailAddress);
        roleDb.removeRoleFromTeam(playerEmailAddress,teamName, RoleType.PLAYER);
    }

    /**
     * remove teamManager to team
     * @param teamName
     * @param teamManagerEmailAddress
     * @throws Exception
     */
    public void removeTeamManager(String teamName, String ownerEmail,String teamManagerEmailAddress) throws Exception {
        /*check if one of the inputs null*/
        if(teamName == null || ownerEmail == null || teamManagerEmailAddress == null) {
            throw new NullPointerException("bad input");
        }
        Team team = teamDb.getTeam(teamName);
        checkPermissions(ownerEmail,teamName,PermissionType.REMOVE_TEAM_MANAGER);
        checkTeamStatusIsActive(team);
        /* get the teamManager from the database*/
        TeamManager teamManager = teamManagerDb.getTeamManager(teamManagerEmailAddress);
        /*check if the team that associated with the teamManager match to the teamManager want to delete*/
        Team teamManagerTeam = teamManager.getTeam();
        if(teamManagerTeam == null || !teamName.equals(teamManagerTeam.getTeamName())) {
            throw new Exception("TeamManager is not part of the team");
        }
        teamDb.removeTeamManager(teamName, teamManagerEmailAddress);
        roleDb.removeRoleFromTeam(teamManagerEmailAddress,teamName, RoleType.TEAM_MANAGER);
    }

    /**
     * remove coach from team
     * @param teamName
     * @param coachEmailAddress
     * @throws Exception
     */
    public void removeCoach(String teamName,String emailAddress, String coachEmailAddress) throws Exception {
        /*check if one of the inputs null*/
        if(teamName == null || emailAddress == null || coachEmailAddress == null) {
            throw new NullPointerException("bad input");
        }
        Team team = teamDb.getTeam(teamName);
        checkPermissions(emailAddress,teamName,PermissionType.REMOVE_COACH);
        checkTeamStatusIsActive(team);
        /* get the coach from the database*/
        Coach coach = coachDb.getCoach(coachEmailAddress);
        /*check if the team that associated with the coach match to the coach want to delete*/
        Team coachTeam = coach.getTeam();
        if(coachTeam == null || !teamName.equals(coachTeam.getTeamName())) {
            throw new Exception("Coach is not part with associated team");
        }
        teamDb.removeCoach(teamName, coachEmailAddress);
        roleDb.removeRoleFromTeam(coachEmailAddress,teamName, RoleType.COACH);
    }

    /**
     * remove court from team
     * @param teamName
     * @param courtName
     * @throws Exception
     */
    public void removeCourt(String teamName, String ownerEmail,String courtName) throws Exception {
        /*check if one of the inputs null*/
        if(teamName == null || ownerEmail == null || courtName == null) {
            throw new NullPointerException("bad input");
        }
        Team team = teamDb.getTeam(teamName);
        checkPermissions(ownerEmail,teamName,PermissionType.REMOVE_COURT);
        checkTeamStatusIsActive(team);
        Court court = courtDb.getCourt(courtName);
        /*check if one of the teams that associated with the court match to the court want to delete*/
        Team courtTeam = court.getTeam(teamName);
        if(courtTeam == null || !teamName.equals(courtTeam.getTeamName())) {
            throw new Exception("Court is not part of the with associated team");
        }
        teamDb.removeCourt(teamName, courtName);
    }

    /**
     * subscription teamOwner in case the subscriber's role is not teamOwner or associated with other team
     * @param teamName
     * @param teamOwnerEmail
     * @param ownerToAddEmail
     * @throws Exception
     */
    public void subscriptionTeamOwner(String teamName, String teamOwnerEmail, String ownerToAddEmail) throws Exception {
        if(teamName == null || teamOwnerEmail == null || ownerToAddEmail == null) {
            throw new NullPointerException("bad input");
        }
        Team team = teamDb.getTeam(teamName);
        checkPermissions(teamOwnerEmail,teamName,PermissionType.OWNER);
        checkTeamStatusIsActive(team);
        /*check if the major team owner in db*/
        TeamOwner teamOwner = teamOwnerDb.getTeamOwner(teamOwnerEmail);
        /*check if the subscriber exists*/
        Subscriber subscriber = subscriberDb.getSubscriber(ownerToAddEmail);
        List<Role> rolesOfOwnerToAdd = roleDb.getRoles(ownerToAddEmail);
        for (Role tr: rolesOfOwnerToAdd) {
            if(tr.getTeamName() != null && !teamName.equals(tr.getTeamName())){
                throw new Exception("OwnerToAdd already associated with other team");
            }
            if(RoleType.TEAM_OWNER.equals(tr.getRoleType())){
                throw new Exception("This subscriber already teamOwner");
            }
        }
        teamOwnerDb.subscriptionTeamOwner(team,teamOwnerEmail,subscriber);
        roleDb.createRole(ownerToAddEmail,teamName, RoleType.TEAM_OWNER);
    }

    /**
     * subscription teamOwner in case the subscriber's role is not teamOwner/teamManager or associated with other team
     * @param teamName
     * @param teamOwnerEmail
     * @param managerToAddEmail
     * @throws Exception
     */
    public void subscriptionTeamManager(String teamName, String teamOwnerEmail, String managerToAddEmail,List<PermissionType> permissionTypes) throws Exception {
        if(teamName == null || teamOwnerEmail == null || managerToAddEmail == null || permissionTypes == null) {
            throw new NullPointerException("bad input");
        }
        Team team = teamDb.getTeam(teamName);
        checkPermissions(teamOwnerEmail,teamName,PermissionType.OWNER);
        checkTeamStatusIsActive(team);
        /*check if the major team owner in db*/
        TeamOwner teamOwner = teamOwnerDb.getTeamOwner(teamOwnerEmail);
        Subscriber subscriber = subscriberDb.getSubscriber(managerToAddEmail);
        List<Role> teamRolesOfManagerToAdd = roleDb.getRoles(managerToAddEmail);
        for (Role tr: teamRolesOfManagerToAdd) {
            if(tr.getTeamName() != null && !teamName.equals(tr.getTeamName())){
                throw new Exception("ManagerToAdd already associated with other team");
            }
            RoleType roleType = tr.getRoleType();
            if(RoleType.TEAM_OWNER.equals(roleType)){
                throw new Exception("This subscriber already teamOwner");
            }
            if(RoleType.TEAM_MANAGER.equals(roleType)){
                throw new Exception("This subscriber already teamManager");
            }
        }
        for (PermissionType pt: permissionTypes) {
            permissionDb.createPermission(managerToAddEmail,pt);
        }
        teamManagerDb.subscriptionTeamManager(team,teamOwnerEmail,subscriber,permissionTypes);
        roleDb.createRole(managerToAddEmail,teamName, RoleType.TEAM_MANAGER);
    }

    /**
     * remove subscription teamOwner and all the owners and teamManagers under this subscription from team
     * remove role teamOwner for this subscription
     * @param teamName
     * @param teamOwnerEmailAddress
     * @param ownerToRemove
     * @throws Exception
     */
    public void removeSubscriptionTeamOwner(String teamName, String teamOwnerEmailAddress, String ownerToRemove) throws Exception {
        if(teamName == null || teamOwnerEmailAddress == null || ownerToRemove == null) {
            throw new NullPointerException("bad input");
        }
        Team team = teamDb.getTeam(teamName);
        checkPermissions(teamOwnerEmailAddress,teamName,PermissionType.OWNER);
        checkTeamStatusIsActive(team);
        /*check if the major team owner in db*/
        TeamOwner teamOwner = teamOwnerDb.getTeamOwner(teamOwnerEmailAddress);
        if(!team.equals(teamOwner.getTeam())){
            throw new Exception("TeamOwner's team does't match");
        }
        TeamOwner teamOwnerToRemove = teamOwnerDb.getTeamOwner(ownerToRemove);
        if(!team.equals(teamOwnerToRemove.getTeam())){
            throw new Exception("TeamOwnerToRemove associated with other team");
        }
        if(!teamOwnerEmailAddress.equals(teamOwnerToRemove.getOwnedByEmailAddress())){
            throw new Exception("TeamOwnerToRemove owned by another teamOwner");
        }
        // todo - move foreach to db
        List<String> allTeamOwnersOwnedBy = teamOwnerDb.getAllTeamOwnersOwnedBy(ownerToRemove);
        for (String emailToRemove: allTeamOwnersOwnedBy) {
            removeSubscriptionTeamOwner(teamName,ownerToRemove,emailToRemove);
        }
        List<String> allTeamManagersOwnedBy = teamManagerDb.getAllTeamManagersOwnedBy(ownerToRemove);
        for (String emailToRemove: allTeamManagersOwnedBy) {
            teamManagerDb.removeSubscriptionTeamManager(emailToRemove);
            roleDb.removeRole(emailToRemove,RoleType.TEAM_MANAGER);
        }
        teamOwnerDb.removeSubscriptionTeamOwner(ownerToRemove);
//        roleDb.removeRoleFromTeam(ownerToRemove,teamName, RoleType.TEAM_OWNER);
        roleDb.removeRole(ownerToRemove,RoleType.TEAM_OWNER);

    }

    /**
     * remove subscription teamManager
     * remove role teamManager from this subscription and update the last role to be the role
     * @param teamName
     * @param teamOwnerEmail
     * @param managerToRemoveEmail
     * @throws Exception
     */
    public void removeSubscriptionTeamManager(String teamName, String teamOwnerEmail, String managerToRemoveEmail) throws Exception {
        if(teamName == null || teamOwnerEmail == null || managerToRemoveEmail == null) {
            throw new NullPointerException("bad input");
        }
        Team team = teamDb.getTeam(teamName);
        checkPermissions(teamOwnerEmail,teamName,PermissionType.OWNER);
        checkTeamStatusIsActive(team);
        /*check if the major team owner in db*/
        TeamOwner teamOwner = teamOwnerDb.getTeamOwner(teamOwnerEmail);
        if(!team.equals(teamOwner.getTeam())){
            throw new Exception("TeamOwner's team doesn't match");
        }
        TeamManager teamManagerToRemove = teamManagerDb.getTeamManager(managerToRemoveEmail);
        if(!team.equals(teamManagerToRemove.getTeam())){
            throw new Exception("TeamManagerToRemove associated with other team");
        }
        if(!teamOwnerEmail.equals(teamManagerToRemove.getOwnedByEmail())){
            throw new Exception("TeamManagerToRemove owned by another teamOwner");
        }

        teamManagerDb.removeSubscriptionTeamManager(managerToRemoveEmail);
        roleDb.removeRole(managerToRemoveEmail, RoleType.TEAM_MANAGER);
    }

    /**
     *add financial activity of the team and update the budget
     * @param teamName
     * @param financialActivityAmount
     * @param description
     * @param financialActivityType
     * @throws Exception
     */
    public void addFinancialActivity(String teamName, String emailAddress,Double financialActivityAmount, String description, FinancialActivityType financialActivityType) throws Exception {
        if(teamName == null || financialActivityAmount == null || description == null || financialActivityType == null) {
            throw new NullPointerException("bad input");
        }
        Team team = teamDb.getTeam(teamName);
        checkPermissions(emailAddress,teamName,PermissionType.ADD_FINANCIAL);
        checkTeamStatusIsActive(team);
        if(financialActivityType.equals(FinancialActivityType.OUTCOME) ){
            if(team.getBudget() - financialActivityAmount < 0){
                throw new Exception("The financial outcome exceeds from the budget");
            }
        }
        /*for security and unique id*/
        String financialActivityId = UUID.randomUUID().toString();
        FinancialActivity financialActivity = new FinancialActivity(financialActivityId,financialActivityAmount,description,financialActivityType,team);
        financialActivityDb.createFinancialActivity(financialActivity);
        teamDb.addFinancialActivity(teamName,financialActivity);
    }

    /**
     * change the team's status
     * @param teamName
     * @param teamStatus
     * @throws Exception
     */
    public void changeStatus(String teamName,String ownerEmail,TeamStatus teamStatus) throws Exception {
        if(teamName == null) {
            throw new NullPointerException("bad input");
        }
        Team team = teamDb.getTeam(teamName);
        checkPermissions(ownerEmail,teamName,PermissionType.CHANGE_STATUS);
        teamDb.changeStatus(teamName,teamStatus);
    }

    /**
     * in case that the status is INACTIVE, will not can do any functions
     * @param team
     * @throws Exception
     */
    private void checkTeamStatusIsActive(Team team) throws Exception {
        if(TeamStatus.INACTIVE.equals(team.getTeamStatus())){
            throw new Exception("This Team's status - Inactive");
        }
    }


    /**
     * update player's details
     * @param ownerEmailAddress
     * @param playerEmailAddress
     * @param firstName
     * @param lastName
     * @param birthDate
     * @param playerRole
     * @throws Exception
     */
    public void updatePlayerDetails(String teamName,String ownerEmailAddress,String playerEmailAddress, String firstName, String lastName, Date birthDate, PlayerRole playerRole) throws Exception {
        if(teamName == null || ownerEmailAddress == null || playerEmailAddress == null || firstName == null || firstName == null || lastName == null || birthDate == null || playerRole == null) {
            throw new NullPointerException("bad input");
        }
        checkPermissions(ownerEmailAddress,teamName,PermissionType.UPDATE_PLAYER);
        /*check if the teamOwner in Db, than check if the player want to change is in teamOwner's team*/
        TeamOwner teamOwner = teamOwnerDb.getTeamOwner(ownerEmailAddress);
        Map<String, Player> players = teamOwner.getTeam().getPlayers();
        if(!players.containsKey(playerEmailAddress)) {
            throw new Exception("Player not associated with teamOwner's team");
        }
            Player playerFromDb = playerDb.getPlayer(playerEmailAddress);
            playerDb.updatePlayerDetails(playerEmailAddress,firstName,lastName,birthDate,playerRole);
    }

    /**
     * update coach's details
     * @param ownerEmailAddress
     * @param coachEmailAddress
     * @param firstName
     * @param lastName
     * @param coachRole
     * @throws Exception
     */
    public void updateCoachDetails(String teamName,String ownerEmailAddress,String coachEmailAddress, String firstName, String lastName, CoachRole coachRole,QualificationCoach qualificationCoach) throws Exception {
        if(teamName == null || ownerEmailAddress == null || coachEmailAddress == null || firstName == null || lastName == null || coachRole == null || qualificationCoach == null) {
            throw new NullPointerException("bad input");
        }
        checkPermissions(ownerEmailAddress,teamName,PermissionType.UPDATE_COACH);
        /*check if the teamOwner in Db, than check if the player want to change is in teamOwner's team*/
        TeamOwner teamOwner = teamOwnerDb.getTeamOwner(ownerEmailAddress);
        Map<String, Coach> coaches = getTeam(teamName).getCoaches();
        if(!coaches.containsKey(coachEmailAddress)) {
            throw new Exception("Coach not associated with teamOwner's team");
        }
        coachDb.updateCoachDetails(coachEmailAddress,firstName,lastName,coachRole,qualificationCoach);
    }

    /**
     * update teamManager's details
     * @param ownerEmailAddress
     * @param teamManagerEmailAddress
     * @param firstName
     * @param lastName
     * @throws Exception
     */
    public void updateTeamManagerDetails(String teamName,String ownerEmailAddress,String teamManagerEmailAddress, String firstName, String lastName,List<PermissionType> permissionTypes) throws Exception {
        if(teamName == null || ownerEmailAddress == null || teamManagerEmailAddress == null || firstName == null || lastName == null || permissionTypes == null) {
            throw new NullPointerException("bad input");
        }
        checkPermissions(ownerEmailAddress,teamName,PermissionType.UPDATE_TEAM_MANAGER);
        /*check if the teamOwner in Db, than check if the player want to change is in teamOwner's team*/
        TeamOwner teamOwner = teamOwnerDb.getTeamOwner(ownerEmailAddress);
        Map<String, TeamManager> teamManagers = getTeam(teamName).getTeamManagers();
        if(!teamManagers.containsKey(teamManagerEmailAddress)) {
            throw new Exception("TeamManager not associated with teamOwner's team");
        }
        teamManagerDb.updateTeamManagerDetails(teamManagerEmailAddress,firstName,lastName,permissionTypes);
    }

    public void updateCourtDetails(String teamName,String ownerEmailAddress,String courtName, String courtCity) throws Exception {
        if(teamName == null || ownerEmailAddress == null || courtName == null || courtCity == null) {
            throw new NullPointerException("bad input");
        }
        checkPermissions(ownerEmailAddress,teamName,PermissionType.UPDATE_COURT);
        /*check if the teamOwner in Db, than check if the player want to change is in teamOwner's team*/
        TeamOwner teamOwner = teamOwnerDb.getTeamOwner(ownerEmailAddress);
        Court court = getTeam(teamName).getCourt();
        if(court == null || !courtName.equals(court.getCourtName())) {
            throw new Exception("Court not associated with teamOwner's team");
        }
        courtDb.updateCourtDetails(courtName,courtCity);
    }

    private void checkPermissions(String emailAddress,String teamName,PermissionType permissionType) throws Exception {
        SubscriberDbInMemory subscriberDbInMemory = SubscriberDbInMemory.getInstance();
        subscriberDbInMemory.getSubscriber(emailAddress);
        List<Role> roles = roleDb.getRoles(emailAddress);
        boolean isPermitted = false;
        for (Role role: roles) {
            String roleTeam = role.getTeamName();
            RoleType roleType = role.getRoleType();
            if(teamName == null && PermissionType.CREATE_NEW_TEAM.equals(permissionType) && RoleType.TEAM_OWNER.equals(roleType)){
                isPermitted = true;
            }
            else if(teamName != null && teamName.equals(roleTeam)){
                if(RoleType.TEAM_OWNER.equals(roleType)){
                    isPermitted = true;
                }else if(RoleType.TEAM_MANAGER.equals(roleType)){
                    List<PermissionType> permissionTypes = permissionDb.getPermissions(emailAddress);
                    if (permissionTypes.contains(permissionType)) {
                        isPermitted = true;
                    }
                }
            }
        }
        if(!isPermitted){
            throw new Exception("This user hasn't Permissions for this operation");
        }
    }
}