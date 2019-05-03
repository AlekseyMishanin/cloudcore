package utility;

import controller.AuthorisationController;
import controller.OperatingPanelController;

public class ListController {

    private static ListController list = new ListController();

    public static ListController getInstance(){return list;}

    private AuthorisationController authorisationController;
    private OperatingPanelController operatingPanelController;

    public AuthorisationController getAuthorisationController() {
        return authorisationController;
    }

    public void setAuthorisationController(AuthorisationController authorisationController) {
        this.authorisationController = authorisationController;
    }

    public OperatingPanelController getOperatingPanelController() {
        return operatingPanelController;
    }

    public void setOperatingPanelController(OperatingPanelController operatingPanelController) {
        this.operatingPanelController = operatingPanelController;
    }
}