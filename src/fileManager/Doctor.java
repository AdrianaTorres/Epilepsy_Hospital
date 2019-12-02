package fileManager;

public class Doctor {

	private String userName;
	private String name;
	private String surname;
	
	public Doctor (String name, String surname, String userName) {
		this.name=name;
		this.surname=surname;
		this.userName=userName;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getName() {
		return name;
	}
	public String getSurname() {
		return surname;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}

	
}
