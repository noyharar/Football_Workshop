package Data;

import Model.Team;
import Model.UsersTypes.Player;
import Model.UsersTypes.TeamManager;

public interface TeamDb {
    void createTeam(String teamName) throws Exception;
    void addPlayer(String teamName, Player player) throws Exception;
    void removePlayer(String teamName, Integer playerId) throws Exception;
    void addTeamManager(String teamName, TeamManager player) throws Exception;

    Team getTeam(String teamName) throws Exception;
}