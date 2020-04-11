package Model.UsersTypes;

import Model.Enums.PlayerRole;
import Model.Team;

import java.util.Date;
import java.util.Objects;

public class Player extends Subscriber {
    private Team team;
    private Date birthDate;
    private PlayerRole playerRole;

    public Player(Integer id, String firstName, String lastName, Date birthDate, PlayerRole playerRole) {
        super(id,firstName,lastName);
        this.birthDate = birthDate;
        this.playerRole = playerRole;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public PlayerRole getPlayerRole() {
        return playerRole;
    }

    public void setPlayerRole(PlayerRole playerRole) {
        this.playerRole = playerRole;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        if (!super.equals(o)) return false;
        Player player = (Player) o;
        return Objects.equals(birthDate, player.birthDate) &&
                playerRole == player.playerRole;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), team, birthDate, playerRole);
    }
}
