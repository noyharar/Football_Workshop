package Data;

<<<<<<< HEAD
import Model.Court;
import Model.Enums.QualificationJudge;
import Model.Enums.Status;
import Model.Game;
import Model.SeasonLeague;
import Model.Team;
import Model.UsersTypes.Judge;
import Model.PageType;
import Model.Season;
import Model.UsersTypes.TeamOwner;

import java.sql.*;
import java.util.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class GameDbInServer implements GameDb
{
    private static GameDbInServer ourInstance = new GameDbInServer();

    public static GameDbInServer getInstance() { return ourInstance; }

    @Override
    public void insertGame(Game game) throws Exception
    {
        Connection conn = DbConnector.getConnection();
        try
        {
            // the mysql insert statement
            String query = " insert into game (game_id, game_date, season_league, host_team, guest_team, court, host_team_score, guest_team_score, major_judge, end_game_time)"
                    + " values (?,?,?,?,?,?,?,?,?,?)";

            // create the mysql insert preparedStatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, game.getGameID());
            preparedStmt.setDate(2, new java.sql.Date(game.getGameDate().getTime()));
            preparedStmt.setString(3, game.getSeasonLeague().getSeasonLeagueName());
            preparedStmt.setString(4, game.getHostTeam().getTeamName());
            preparedStmt.setString(5, game.getGuestTeam().getTeamName());
            preparedStmt.setString(6, game.getCourt().getCourtName());
            preparedStmt.setInt(7, game.getHostTeamScore());
            preparedStmt.setInt(8, game.getGuestTeamScore());
            preparedStmt.setString(9, game.getMajorJudge());
            preparedStmt.setDate(10, new java.sql.Date(game.getEndGameTime().getTime()));

            // execute the preparedStatement
            preparedStmt.execute();
        }
        catch (Exception e)
        {
            throw new Exception("game already exist in system");
        }
        finally
        {
=======
            String query = " insert into game (game_id,game_date,season_league,host_team,guest_team,court)"
                    + " values (?,?,?,?,?,?)";

            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString (1, game.getGameID());
            SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
            String date = df.format(game.getGameDate());
            preparedStmt.setString (2,date);
            preparedStmt.setString (3, game.getSeasonLeague());
            preparedStmt.setString (4, game.getHostTeam());
            preparedStmt.setString (5, game.getGuestTeam());
            preparedStmt.setString (6, game.getCourt());

            // execute the preparedstatement
            preparedStmt.execute();

        } catch(SQLIntegrityConstraintViolationException e) {
            throw new Exception("game already exist in the system");
        }
        finally {
>>>>>>> a51fbc83ae3cf46c2994cddac4d495cbbbe54858
            conn.close();
        }
    }



    @Override
    public Game getGame(String gameID) throws Exception
    {
        if (gameID == null)
        {
            throw new NullPointerException("Game not found");
        }

        Connection conn = DbConnector.getConnection();

        // the mysql select statement
        String query = "select * from game where game.game_id = \'" + gameID + "\'";

        // create the mysql select resultSet
        Statement preparedStmt = conn.createStatement();
        ResultSet rs = preparedStmt.executeQuery(query);

        // checking if ResultSet is empty
        if (rs.next() == false)
        {
            throw new NotFoundException("Game not found");
        }

        String game_id = rs.getString("game_id");
        Date game_date = rs.getDate("game_date");
        SeasonLeague season_league = SeasonLeagueDbInServer.getInstance().getSeasonLeague(rs.getString("season_league"));
        Team host_team = TeamDbInServer.getInstance().getTeam(rs.getString("host_team"));
        Team guest_team = TeamDbInServer.getInstance().getTeam(rs.getString("guest_team"));
        Court court = CourtDbInServer.getInstance().getCourt(rs.getString("court"));
        Integer host_team_score = rs.getInt("host_team_score");
        Integer guest_team_score = rs.getInt("guest_team_score");
        String major_judge = rs.getString("major_judge");
        Date end_game_time = rs.getDate("end_game_time");

        conn.close();

        Set<String> judgesOfTheGameList = getJudgesOfTheGameList(gameID);

        Game game = new Game(game_id, game_date, season_league, host_team, guest_team, court, judgesOfTheGameList, major_judge, end_game_time);
        game.setHostTeamScore(host_team_score);
        game.setGuestTeamScore(guest_team_score);
        //todo-check what about the event log
//        game.setEventLog();

        return game;
    }




    public List<Game> getAllGames() throws SQLException, NotFoundException, ParseException {
        Connection conn = DbConnector.getConnection();

        // create the mysql select resultSet
        String query = "select * from game";

        Statement preparedStmt = conn.createStatement();
        ResultSet rs = preparedStmt.executeQuery(query);

        // checking if ResultSet is empty
        if (rs.next() == false) {
            throw new NotFoundException("no games in db");
        }

        List<Game> games = new ArrayList<>();
        while(rs.next()){
            String game_id = rs.getString("game_id");
            String game_date = rs.getString("game_date");
            String season_league = rs.getString("season_league");
            String host_team = rs.getString("host_team");
            String guest_team = rs.getString("guest_team");
            String court = rs.getString("court");

            Date date = new SimpleDateFormat("dd/MM/yyyy").parse(game_date);

            Game game = new Game(game_id,date,season_league,host_team,guest_team,court);
            games.add(game);
        }
        return games;
    }













    private Set<String> getJudgesOfTheGameList(String gameID) throws SQLException
    {
        //todo-all function-using table game_judges_list
        Set<String> judgesGameList = new HashSet<>();
        String judge_email_address;

        Connection conn = DbConnector.getConnection();

        // the mysql select statement
        String query = "select judges_email_address from game_judges_list where game_judges_list.game_id = \'" + gameID + "\'";

        // create the mysql select resultSet
        Statement preparedStmt = conn.createStatement();
        ResultSet rs = preparedStmt.executeQuery(query);

        // checking if ResultSet is empty
        if (rs.next() != false)
        {
            judge_email_address = rs.getString("judges_email_address");

            judgesGameList.add(judge_email_address);

            while (rs.next() != false)
            {
                judge_email_address = rs.getString("judges_email_address");

                judgesGameList.add(judge_email_address);
            }
        }
        conn.close();

        return judgesGameList;
    }

    @Override
    public void updateGameLocation(String newLocation, String gameID) throws Exception
    {
        Connection conn = DbConnector.getConnection();
        try
        {
            getGame(gameID);

            // the mysql update statement
            String query = " update game "
                    + "set court = \'" + newLocation + "\' "
                    + "where game_id = \'" + gameID + "\'";

            // create the mysql insert preparedStatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            // execute the preparedStatement
            preparedStmt.execute();
        }
        catch (NotFoundException e)
        {
            throw new Exception("Game not found");
        }
//        catch (Exception e)
//        {
//            throw new Exception("SeasonLeague not found455546486551");
//        }
        finally
        {
            conn.close();
        }
    }

    @Override
    public void updateGameDate(String repMail, Date newDate, String gameID) throws Exception
    {
        Connection conn = DbConnector.getConnection();
        try
        {
            getGame(gameID);

            // the mysql update statement
            String query = " update game "
                    + "set game_date = \'" + newDate + "\' "
                    + "where game_id = \'" + gameID + "\'";

            // create the mysql insert preparedStatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            // execute the preparedStatement
            preparedStmt.execute();
        }
        catch (NotFoundException e)
        {
            throw new Exception("Game not found");
        }
//        catch (Exception e)
//        {
//            throw new Exception("SeasonLeague not found455546486551");
//        }
        finally
        {
            conn.close();
        }
    }

    @Override
    public void deleteAll() throws SQLException
    {
        Connection conn = DbConnector.getConnection();
        Statement statement = conn.createStatement();
        /* TRUNCATE is faster than DELETE since
         * it does not generate rollback information and does not
         * fire any delete triggers
         */

        // the mysql delete statement
        String query = "delete from game";

        // create the mysql delete Statement
        statement.executeUpdate(query);
        conn.close();
    }

    public static void main(String[] args) throws Exception {
        SeasonDbInServer seasonDbInServer = new SeasonDbInServer();
        GameDbInServer gameDbInServer = new GameDbInServer();
        Game game1  = new Game("game1",new Date(),"sl1", "team1", "team2","court");
        Game game2  = new Game("game2",new Date(),"sl1", "team1", "team2","court");
        gameDbInServer.insertGame(game1);
        gameDbInServer.insertGame(game2);

        List<Game> allGames = gameDbInServer.getAllGames();
        System.out.println(allGames);
    }
}

