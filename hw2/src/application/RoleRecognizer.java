package application;

public class RoleRecognizer {
	//set local variables for delimiter and String array
	public static String regex;
	public static String[] roles;

	public static String checkForValidRoles(String input) {
		//will ignore commas and spaces in string
		regex = "[,\\s]";
		
		//if no roles inputted, return error
		if(input.length() <= 0) return "*** Error *** No roles specified";
		
		//splits string by specified characters and puts them into string array
		roles = input.split(regex);
		
		//will check each role and see if it is valid, if any role is not valid will return error
		for(String s : roles) {
			if(s.equalsIgnoreCase("student") || 
					s.equalsIgnoreCase("reviewer") || 
					s.equalsIgnoreCase("instructor") || 
					s.equalsIgnoreCase("staff") || 
					s.equalsIgnoreCase("admin")) {
				System.out.println(s);
			} else {
				return "Please input valid roles:student, reviewer, instructor, staff, admin";
			}
		}
		//if all roles valid, will return
	return "";
	}
}
