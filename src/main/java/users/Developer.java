package users;

public class Developer {
    private String hireDate;
    // TODO: format yyyy-mm-dd to be respected
    public enum expertiseArea {
        FRONTEND,
        BACKEND,
        DEVOPS,
        DESIGN,
        DB,
        FULLSTACK
    }

    public enum seniority {
        JUNIOR,
        MID,
        SENIOR
    }
}
