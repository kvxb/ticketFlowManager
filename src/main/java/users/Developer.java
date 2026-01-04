package users;

public class Developer extends User{
    private String hireDate;
    // TODO: format yyyy-mm-dd to be respected
    public enum ExpertiseArea {
        FRONTEND,
        BACKEND,
        DEVOPS,
        DESIGN,
        DB,
        FULLSTACK
    }
    private ExpertiseArea expertiseArea;

    public enum Seniority {
        JUNIOR,
        MID,
        SENIOR
    }

    private Seniority seniority;
}
