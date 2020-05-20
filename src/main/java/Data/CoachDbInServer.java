package Data;

import Model.Enums.CoachRole;
import Model.Enums.PlayerRole;
import Model.Enums.QualificationCoach;
import Model.Enums.Status;
import Model.UsersTypes.Coach;
import Model.UsersTypes.Player;

import java.sql.*;
import java.util.Date;

public class CoachDbInServer implements CoachDb {
    @Override
    public Coach getCoach(String coachEmailAddress) throws Exception {
        if (coachEmailAddress == null) {
            throw new NullPointerException("bad input");
        }

        Connection conn = DbConnector.getConnection();

        String query = "select * from subscriber, coach where subscriber.email_address = coach.email_address and subscriber.email_address = \'" + coachEmailAddress + "\'";

        Statement preparedStmt = conn.createStatement();
        ResultSet rs = preparedStmt.executeQuery(query);

        // checking if ResultSet is empty
        if (rs.next() == false) {
            throw new NotFoundException("Coach not found");
        }

        String userName = rs.getString("email_address");
        String password = rs.getString("password");
        Integer id = rs.getInt("id");
        String first_name = rs.getString("first_name");
        String last_name = rs.getString("last_name");
        String status = rs.getString("status");
        String team = rs.getString("team");
        String coach_role = rs.getString("coach_role");
        String coach_qualification = rs.getString("qualification_coach");
        conn.close();

        Coach coach = new Coach(userName, id, first_name, last_name, CoachRole.valueOf(coach_role),QualificationCoach.valueOf(coach_qualification));
        coach.setPassword(password);
        coach.setTeam(team);
        coach.setStatus(Status.valueOf(status));
        return coach;    }

    @Override
    public void insertCoach(Coach currCoach) throws Exception {
        Connection conn = DbConnector.getConnection();
        try
        {
            // the mysql insert statement
            String query = " insert into coach (email_address,team,coach_role,qualification_coach)"
                    + " values (?,?,?,?)";

            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString (1, currCoach.getEmailAddress());
            preparedStmt.setString (2, currCoach.getTeam());
            preparedStmt.setString (3, currCoach.getCoachRole().name());
            preparedStmt.setString (4, currCoach.getQualificationCoach().name());

            // execute the preparedstatement
            preparedStmt.execute();
        } finally {
            conn.close();
        }
    }

    @Override
    public void removeCoach(Coach coachToRemove) throws Exception {

    }

    @Override
    public void updateCoachDetails(String coachEmailAddress, String firstName, String lastName, CoachRole coachRole, QualificationCoach qualificationCoach) throws NotFoundException, SQLException {
        Connection conn = DbConnector.getConnection();
        String query = "UPDATE coach join subscriber SET first_name = \'" + firstName + "\', last_Name = \'" + lastName + "\', coach_role = \'"+ coachRole.name() + "\', qualification_coach = \'" + qualificationCoach.name()  + "\'  WHERE subscriber.email_address =  \'"+ coachEmailAddress + "\' and coach.email_address = \'" + coachEmailAddress + "\'" ;

        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.executeUpdate();
        conn.close();
    }

    @Override
    public void deleteAll() {

    }

    public static void main(String[] args) throws Exception {
        CoachDbInServer coachDbInServer = new CoachDbInServer();
        Coach coach = new Coach("coach@gmail.com", "1234",1, "first", "last", CoachRole.MAJOR, QualificationCoach.UEFA_A);

//        coachDbInServer.insertCoach(coach);
        Coach coach1 = coachDbInServer.getCoach("coach@gmail.com");
        System.out.println(coach1.toString());
        coachDbInServer.updateCoachDetails("coach@gmail.com", "coach",  "last", CoachRole.GOALKEEPER, QualificationCoach.UEFA_A);
        coach1 = coachDbInServer.getCoach("coach@gmail.com");
        System.out.println(coach1.toString());
    }
}
