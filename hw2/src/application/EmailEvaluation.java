package application;

public class EmailEvaluation {
	public static String checkForASUEmail(String email) {
		
		 String errMessage = "";
		//will check if email entered is an asu email, will reject any other email
		if(email.contains("@asu.edu")) {
			return errMessage;
		} else {
			return errMessage + "Please Input ASU email";
		}
	}
}
