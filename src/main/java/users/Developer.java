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

    public Developer(String username, String email, String role, String hireDate, String expertiseArea, String seniority) {
        super(username, email, role);
        this.hireDate = hireDate;
        this.expertiseArea = ExpertiseArea.valueOf(expertiseArea);
        this.seniority = Seniority.valueOf(seniority);
    }

	public String getHireDate() {
		return hireDate;
	}

	public void setHireDate(String hireDate) {
		this.hireDate = hireDate;
	}

	public ExpertiseArea getExpertiseArea() {
		return expertiseArea;
	}

	public void setExpertiseArea(ExpertiseArea expertiseArea) {
		this.expertiseArea = expertiseArea;
	}

	public Seniority getSeniority() {
		return seniority;
	}

	public void setSeniority(Seniority seniority) {
		this.seniority = seniority;
	}
}
