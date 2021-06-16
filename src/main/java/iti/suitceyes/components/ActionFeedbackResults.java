package iti.suitceyes.components;

public class ActionFeedbackResults {

    private String entity;
    private String move_command;

    public ActionFeedbackResults() {
        this.entity = "";
        this.move_command = "";

    }



    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getEntity() {
        return this.entity;
    }

    public void setCommand(String move_command) {
        this.move_command = move_command;
    }

    public String getCommand() {
        return this.move_command;
    }

    public String toString(){

        String resultsToString = "----- User Component -----\n"
                + "entity: " + this.getEntity() + " command: " + this.getCommand();

        return resultsToString;

    }

}
