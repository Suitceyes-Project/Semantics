package iti.suitceyes.components;

public class UserComponentResults {

    private String timestamp;
    private String query;

    public UserComponentResults() {
        this.timestamp = "0000-00-00T00:00:00";
        this.query = "";

    }



    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return this.query;
    }

    public String toString(){

        String resultsToString = "----- User Component -----\n"
                + "timestamp: " + this.getTimestamp() + " query: " + this.query;

        return resultsToString;

    }

}

