package Service;

import Data.*;
import Model.Enums.RoleType;
import Model.Team;
import Model.UsersTypes.Fan;
import Model.UsersTypes.Subscriber;
import Model.UsersTypes.SystemAdministrator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SystemAdministratorServiceTest {
    private SystemAdministratorService systemAdministratorService=new SystemAdministratorService();

    @Before
    public void init()
    {
        final List<Db> dbs = new ArrayList<>();
        dbs.add(SubscriberDbInMemory.getInstance());
        dbs.add(FanDbInMemory.getInstance());
        dbs.add(PageDbInMemory.getInstance());
        dbs.add( TeamDbInMemory.getInstance());
        dbs.add(CoachDbInMemory.getInstance());
        dbs.add(CourtDbInMemory.getInstance());
        dbs.add(JudgeDbInMemory.getInstance());
        dbs.add(PlayerDbInMemory.getInstance());
        dbs.add(TeamManagerDbInMemory.getInstance());
        dbs.add(TeamOwnerDbInMemory.getInstance());
        dbs.add(SeasonLeagueDbInMemory.getInstance());
        dbs.add(JudgeSeasonLeagueDbInMemory.getInstance());
        dbs.add(RoleDbInMemory.getInstance());
        dbs.add(SystemAdministratorDbInMemory.getInstance());
        dbs.add(RepresentativeAssociationDbInMemory.getInstance());
        dbs.add(TeamDbInMemory.getInstance());
        dbs.add(RoleDbInMemory.getInstance());
        for (Db db : dbs)
        {
            db.deleteAll();
        }
    }

    @Test
    public void closeNullTeam(){
        try {
            systemAdministratorService.closeTeamForEver(null);
        } catch (Exception e) {
            Assert.assertEquals("null team name exception!",e.getMessage());
        }
    }

    @Test
    public void closeNotExistTeam(){
        try {
            systemAdministratorService.closeTeamForEver("blaTeam");
        } catch (Exception e) {
            Assert.assertEquals("the team " + "blaTeam" + " doesn't exist in the system",e.getMessage());
        }
    }

    @Test
    public void closeExistTeam(){
        try {
            TeamDbInMemory.getInstance().createTeam("barca");
            systemAdministratorService.closeTeamForEver("barca");
        } catch (Exception e) {
            //not should enter here
            Assert.assertEquals(e.getMessage(),null);
        }
    }

    @Test
    public void removeNullSubscriber(){
        try {
            systemAdministratorService.removeSubscriber(null);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(),"the subscriber with the Email " + null + " doesn't in the system!");
        }
    }

    @Test
    public void removeExistSubscriber(){
        Fan fan=new Fan("noa@gmail.com", "L1o8oy", 111111111, "Noy", "Harary");
        try {
            SubscriberDbInMemory.getInstance().createSubscriber(fan);
            FanDbInMemory.getInstance().createFan(fan);
            RoleDbInMemory.getInstance().createRoleInSystem(fan.getEmailAddress(),RoleType.FAN);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(),null);
        }
        try {
            systemAdministratorService.removeSubscriber(fan.getEmailAddress());
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(),"the subscriber with the Email " + fan.getEmailAddress() + " doesn't in the system!");
        }
        //check if the fan deleted properly
        try {
            SubscriberDbInMemory.getInstance().getSubscriber("noa@gmail.com");
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(),"subscriber not found");
        }
        try {
            FanDbInMemory.getInstance().getFan("noa@gmail.com");
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(),"Fan not found");
        }
    }



}
