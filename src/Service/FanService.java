package Service;

import Controller.FanController;
import Model.Enums.AlertWay;
import Model.Enums.GamesAlert;
import Model.Enums.Status;
import Model.PersonalPage;
import Model.UsersTypes.Fan;

public class FanService {
    private FanController fanController;

    public FanService() {
        this.fanController = new FanController();
    }
    public void addPageToFanList(String pageId, String fanMail) throws Exception {
        fanController.addPageToFanList(pageId, fanMail);
    }
    public void logOut(String fanMail, Status status) throws Exception{
        fanController.logOut(fanMail, status);
    }
    public void askToGetAlerts(String fanMail, GamesAlert alert, AlertWay alertWay) throws Exception {
        fanController.askToGetAlerts(fanMail,alert,alertWay);
    }
    public void wantToEditPassword(String fanMail, String newPassword) throws Exception {
        fanController.wantToEditPassword(fanMail, newPassword);
    }
    public void wantToEditFirstName(String fanMail, String newFirstName) throws Exception {
        fanController.wantToEditFirstName(fanMail, newFirstName);
    }
    public void wantToEditLastName(String fanMail, String newLastName) throws Exception {
        fanController.wantToEditLastName(fanMail, newLastName);
    }





//    public void editPersonalDetails(String fanMail,String password, Integer id, String firstName, String lastName) throws Exception {
//        fanController.editPersonalDetails(fanMail, password, id, firstName, lastName);
//    }
}