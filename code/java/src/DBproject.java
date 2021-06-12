/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import java.util.Scanner;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Doctor");
				System.out.println("2. Add Patient");
				System.out.println("3. Add Appointment");
				System.out.println("4. Make an Appointment");
				System.out.println("5. List appointments of a given doctor");
				System.out.println("6. List all available appointments of a given department");
				System.out.println("7. List total number of different types of appointments per doctor in descending order");
				System.out.println("8. Find total number of patients per doctor with a given status");
				System.out.println("9. < EXIT");
				
				switch (readChoice()){
					case 1: AddDoctor(esql); break;
					case 2: AddPatient(esql); break;
					case 3: AddAppointment(esql); break;
					case 4: MakeAppointment(esql); break;
					case 5: ListAppointmentsOfDoctor(esql); break;
					case 6: ListAvailableAppointmentsOfDepartment(esql); break;
					case 7: ListStatusNumberOfAppointmentsPerDoctor(esql); break;
					case 8: FindPatientsCountWithStatus(esql); break;
					case 9: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	//GETTING FUNCTIONS
	public static String getValidDoctorID(DBproject esql) { // should change to newDoctorID
		List<List<String>> resultset = new ArrayList<List<String>>();

		try {
			String query = "SELECT MAX(doctor_ID) FROM Doctor";
			resultset = esql.executeQueryAndReturnResult(query);
		} catch(Exception e) {
                        System.err.println(e.getMessage());
                }

		return resultset.get(0).get(0);
	}
	
	public static String getValidAppointmentID(DBproject esql){
		List<List<String>> resultset = new ArrayList<List<String>>();
		
		try{
			String query = "select MAX(appnt_ID) from appointment";
			resultset = esql.executeQueryAndReturnResult(query);
		}catch(Exception e){
			System.err.println(e.getMessage());
		}

		return resultset.get(0).get(0);		
	}

	public static String getValidPatientID(DBproject esql) { // should change to newDoctorID
                List<List<String>> resultset = new ArrayList<List<String>>();

                try {
                        String query = "SELECT MAX(patient_ID) FROM Patient";
                        resultset = esql.executeQueryAndReturnResult(query);
                } catch(Exception e) {
                        System.err.println(e.getMessage());
                }

                return resultset.get(0).get(0);
        }

	//VALID FUNCTIONS
	public static boolean validString(String str) {
		if (str.length() == 0) {
			return false;
		}

		str = str.toLowerCase();
		char[] charArray = str.toCharArray();

		for (int i = 0; i < charArray.length; ++i) {
			if (!(charArray[i] >= 'a' && charArray[i] <= 'z') && (charArray[i] != ' ')) {
				return false;
			}
		} 

		return true;
	}

	public static boolean validDepartment(DBproject esql, String id) {
		List<List<String>> resultset = new ArrayList<List<String>>();
		try {
			String query = "select dept_ID from department";
			resultset = esql.executeQueryAndReturnResult(query);		
		} catch(Exception e) {
         		System.err.println(e.getMessage());
      		}

		for (List<String> did : resultset) {
			for (int i = 0; i < did.size(); ++i) {
				if ((id).equals(did.get(i))) {
					return true;
				}
			}
		}	

		return false;
	}
  
	public static boolean validDoctor(DBproject esql, String id) {
                List<List<String>> resultset = new ArrayList<List<String>>();
                try {
                        String query = "select doctor_ID from doctor";
                        resultset = esql.executeQueryAndReturnResult(query);
                } catch(Exception e) {
                        System.err.println(e.getMessage());
                }

                for (List<String> did : resultset) {
                        for (int i = 0; i < did.size(); ++i) {
                                if ((id).equals(did.get(i))) {
                                        return true;
                                }
                        }
                }

                return false;
        }
	
	public static boolean getValidDate(String input){
		if(input.length() != 10){
			return false;
		}
		String year, month, day = month = year =  "";
		char[] charArray = input.toCharArray();



		if(!(charArray[4] == '-') && !(charArray[7] == '-')){
			return false;
		}
		for(int i = 0; i < 4; i++){
			year = year + input.charAt(i);
		}
		try{
			Integer.parseInt(year);
		} catch(Exception e){
		//	System.out.println("Not an integer(year)!");
			return false;
		}

		for(int i = 5; i <= 6; i++){
			month = month + input.charAt(i);
		}
		try{
			Integer.parseInt(month);
		} catch(Exception e){
		//	System.out.println("Not an integer (month)!");		
			return false;
		}
		if(Integer.parseInt(month) > 12 || Integer.parseInt(month) < 1){
			return false;
		}
			
		for(int i = 8; i <= 9; i++){
			day = day + input.charAt(i);
		}
		try{
			Integer.parseInt(day);
		} catch(Exception e){
		//	System.out.println("Not an integer(day!)");
			return false;
		}		
	//	System.out.println(Integer.parseInt(month));
		int daysInMonth = 0;
		switch(Integer.parseInt(month)){
			case 1:
			case 3:
			case 5:
			case 7:
			case 8:
			case 10:
			case 12:
				daysInMonth = 31;				
				break;
			case 2:
				daysInMonth = 28;
				break;
			case 4:
			case 6:
			case 9:
			case 11:
				daysInMonth = 30;
				break;
			default:
				daysInMonth = -1;
			//	System.out.println("Error! Invalid month; cannot select proper days of the month");
				return false;
		}
		if(Integer.parseInt(day) > daysInMonth || Integer.parseInt(day) < 1){
			return false;
		}
		return true;		
	}
			
	
	public static boolean getValidTimeslot(String inputTimeslot){
		int symbolcolon = 0;
		int symboldash = 0;
		switch(inputTimeslot.length()){
			case 9:
			for(int i = 0; i < inputTimeslot.length(); i++){
				if(Character.isDigit(inputTimeslot.charAt(i))){
				}
				else if(((inputTimeslot.charAt(i) == '-') && (i == 4)) ||
					 ( inputTimeslot.charAt(i) == ':') && ((i == 1) || (i == 6))){
					symbolcolon++;
				}
				else{
					return false;
				}
			}
			if(symbolcolon == 3){
				return true;
			}
			return false;

			case 10:
			for(int i = 0; i < inputTimeslot.length();i++){
				if(Character.isDigit(inputTimeslot.charAt(i))){
				} else if(((i == 5) && (inputTimeslot.charAt(i) == '-')) || 
					((i == 4) && (inputTimeslot.charAt(i) == '-'))){
					symboldash++;
				}
				else if(((i == 1) && (inputTimeslot.charAt(i) == ':')) || 
					((i == 2) && (inputTimeslot.charAt(i) == ':')) ||
					((i == 6) && (inputTimeslot.charAt(i) == ':')) ||
					((i == 7) && (inputTimeslot.charAt(i) == ':'))){
					symbolcolon++;
				}
				else{
					return false;
				}
			}
			if(symboldash == 1 && symbolcolon == 2){
				return true;
			}
			return false;

			case 11:
			for(int i = 0; i < inputTimeslot.length(); i++){
				if(Character.isDigit(inputTimeslot.charAt(i))){
				} else if(((i == 2) && (inputTimeslot.charAt(i) == ':')) ||
					((i == 5) && (inputTimeslot.charAt(i) == '-')) ||
					((i == 8) && (inputTimeslot.charAt(i) == ':'))){
					symbolcolon++;
				}
				else{
					return false;
				}
			}
			if(symbolcolon == 3){
				return true;
			}
			return false;

			default:
			return false;				
		}
	}
	
	public static boolean getValidAppointmentStatus(String input){

		if(input.equals("PA") ||
		   input.equals("AC") ||
		   input.equals("AV") ||
		   input.equals("WL")){
			return true;
		}
		else{
			return false;
		}
	}

	public static boolean validPatient(DBproject esql, String inputName){
		List<List<String>> resultset = new ArrayList<List<String>>();
		try{
			String query = "select name from patient";
			resultset = esql.executeQueryAndReturnResult(query);
		} catch(Exception e){
			System.err.println(e.getMessage());
		}
		
		for(List<String> name : resultset){
			for(int i = 0; i < name.size(); i++){
				if((inputName).equals(name.get(i))){
					return true;
				}
			}
		}
		return false;
	}

	public static boolean validAppointment(DBproject esql, String inputID){
		List<List<String>> resultset = new ArrayList<List<String>>();
		try{
			String query = "select appnt_id from appointment";
			resultset = esql.executeQueryAndReturnResult(query);
		} catch(Exception e){
			System.err.println(e.getMessage());
		}
		
		for(List<String> appnt_id : resultset){
			for(int i = 0; i < appnt_id.size(); i++){
				if((inputID).equals(appnt_id.get(i))){
					return true;
				}
			}
		}
			
		return false;
	}
  
	public static boolean validAvailableAppointment(DBproject esql, String inputID){
                List<List<String>> resultset = new ArrayList<List<String>>();
                try{
                        String query = "select appnt_id from appointment";
                        resultset = esql.executeQueryAndReturnResult(query);
                } catch(Exception e){
                        System.err.println(e.getMessage());
                }

                for(List<String> appnt_id : resultset){
                        for(int i = 0; i < appnt_id.size(); i++){
                                if((inputID).equals(appnt_id.get(i))){
                                        return false;
                                }
                        }
                }

                return true;
        }

	public static boolean validDepartmentName(DBproject esql, String inputName){
		List<List<String>> resultset = new ArrayList<List<String>>();
		try{
			String query = "select name from department";
			resultset = esql.executeQueryAndReturnResult(query);
		} catch(Exception e){
			System.err.println(e.getMessage());
		}

		for(List<String> name : resultset){
			for(int i = 0; i < name.size(); i++){
				if((inputName).equals(name.get(i))){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean validDoctorName(DBproject esql, String dname) {
		String query = "select * from Doctor where name = '" + dname + "'";
		int rc = 0;		

		try {
			rc = esql.executeQuery(query);	
		} catch(Exception e){
                        System.err.println(e.getMessage());
                }	

		if (rc > 0) {
			return false;
		} else {
			return true;
		}
	} 

	public static boolean validDoctorEntry(DBproject esql, String dname, String specialty, String did) {
		String query = "select * from Doctor where name = '" + dname + "' and specialty = '" + specialty + "' and did = " + did;
		int rc = 0;

		try {
                        rc = esql.executeQuery(query);
                } catch(Exception e){
                        System.err.println(e.getMessage());
                }

                if (rc > 0) {
                        return false;
                } else {
                        return true;
                }
	}

	public static boolean validPatientEntry(DBproject esql, String pname, char gender, int age, String address) {
		String query = "select * from Patient where name = '" + pname + "' and gtype = '" + gender + "' and age = " + age + " and address = '" + address + "'";
		int rc = 0;

                try {
                        rc = esql.executeQuery(query);
                } catch(Exception e){
                        System.err.println(e.getMessage());
                }

                if (rc > 0) {
                        return false;
                } else {
                        return true;
                }
	}

	public static boolean validAppEntry(DBproject esql, String date, String timeslot, String status) {
		String query = "select * from Appointment where adate = '" + date + "' and time_slot = '" + timeslot + "' and status = '" + status + "'";
		int rc = 0;

		try {
                        rc = esql.executeQuery(query);
                } catch(Exception e){
                        System.err.println(e.getMessage());
                }

                if (rc > 0) {
                        return false;
                } else {
                        return true;
                }
	}

	public static void AddDoctor(DBproject esql) {//1
		String lastID = getValidDoctorID(esql);
		int newID = Integer.parseInt(lastID) + 1;

		Scanner in = new Scanner(System.in);
		System.out.println("Enter Doctor name: ");
		String dname = in.nextLine();
		
		while (!validString(dname)) {
			System.out.println("Invalid input, please re-enter Doctor name: ");
			dname = in.nextLine();
		}

		System.out.println("Enter Doctor specialty: ");
		String dspecialty = in.nextLine();
		
		while (!validString(dspecialty)) {
			System.out.println("Invalid input, please re-enter Doctor specialty: ");
			dspecialty = in.nextLine();
		}

		System.out.println("Enter department id: ");
		String did = in.nextLine();
		
		while (!validDepartment(esql, did)) {
			System.out.println("Invalid input, please re-enter Department id: ");
			did = in.nextLine();
		}

		if (!validDoctorEntry(esql, dname, dspecialty, did)) {
			System.out.println("Duplicate entry, invalid input. Please retry.");
		} else {

			try {
				String query = "INSERT INTO Doctor(doctor_ID, name, specialty, did) " + 
					"VALUES ('" + newID + "', '" + dname + "', '" + dspecialty + "', '" + did + "')"; 
				esql.executeUpdate(query);
			
				System.out.println("New record inserted into Doctors: ");
				String query2 = "select * from Doctor group by doctor_id order by doctor_id DESC limit 1";
				int rowcount = esql.executeQueryAndPrintResult(query2);
				System.out.println("Rowcount: " + rowcount);
			} catch(Exception e) {
                       		System.err.println(e.getMessage());
                	} 
		}	 				
	}

	public static void AddPatient(DBproject esql) {//2
		Scanner input = new Scanner(System.in);
		System.out.print("Enter name: ");
		String patientName = input.nextLine();
		
		while(!validString(patientName)) {
			System.out.println("Invalid input, please re-enter Patient name: ");
			patientName = input.nextLine();
		}

		char patientGender = 'X';
		do{
			System.out.print("Enter gender (F/M): ");
			patientGender = input.next().charAt(0);
			input.nextLine();
			if(patientGender == 'f' || patientGender == 'F'){
				patientGender = 'F';
			}
			else if(patientGender == 'm' || patientGender == 'M'){
				patientGender = 'M';
			}
			else{
				patientGender = 'X';
				System.out.println("Invalid gender! Please choose f or m!");
			}
		} while(patientGender == 'X');
	
		int patientAge = -1;
		do{
			System.out.print("Enter age: ");
			try{
				patientAge = Integer.parseInt(input.nextLine());
			} catch(Exception e){
				System.err.println("Invalid age! Please choose a valid number!");
			}
		} while(patientAge < 0 || patientAge > 110);

		System.out.print("Enter address: ");
		String patientAddress = input.nextLine();

		if (!validPatientEntry(esql, patientName, patientGender, patientAge, patientAddress)) {
			System.out.println("Duplicate entry, invalid input. Please retry.");
		} else {

			try{	
				String query = "insert into patient values ((select count(*) as yes from patient)+1, '" 
					+ patientName + "', '" + String.valueOf(patientGender) + "', " 
					+ Integer.toString(patientAge) + ", '" + patientAddress + "', 0)";
				esql.executeUpdate(query);

				System.out.println("New record inserted into Patients: ");
				String query2 = "select * from patient group by patient_ID order by patient_ID DESC limit 1";
				int rowcount = esql.executeQueryAndPrintResult(query2);
				System.out.println("Rowcount: " + rowcount);
			} catch(Exception e){
				System.err.println(e.getMessage());
			}
		}			
	}
				
	public static void AddAppointment(DBproject esql) {//3
		Scanner input = new Scanner(System.in);
	
		int newid = Integer.parseInt(getValidAppointmentID(esql));
		newid += 1;	
	
		System.out.print("Enter date of appointment in format (YYYY-MM-DD): ");
		String appointmentDate = input.nextLine();
		
		while(!getValidDate(appointmentDate)){
			System.out.print("Invalid date! Please follow format (YYYY-MM-DD): ");
			appointmentDate = input.nextLine();
		}

		System.out.print("Enter desired time slot: ");
		String timeSlotInput = input.nextLine();
		
		while(!getValidTimeslot(timeSlotInput)){
			System.out.print("Invalid time slot! Please pick a different time slot: ");
			timeSlotInput = input.nextLine();
		}

		System.out.print("Enter appointment status (PA / AC / AV / WL): ");
		String appointmentStatus = (input.nextLine()).toUpperCase();
		
		while(!getValidAppointmentStatus(appointmentStatus)){
			System.out.print("Invalid appointment status! Please follow format (PA / AC / AV / WL): ");
			appointmentStatus = (input.nextLine()).toUpperCase();
		} 
	
		
		try{
			String query = "insert into appointment " +
				       "values (" + newid + ", '" + appointmentDate + "', '" +
				       timeSlotInput + "', '" + appointmentStatus + "')";
			esql.executeUpdate(query);

			System.out.println("New record inserted into Appointments: ");
			String query2 = "select * from appointment group by appnt_ID order by appnt_ID DESC limit 1";
			int rowcount = esql.executeQueryAndPrintResult(query2);
			System.out.println("Rowcount: " + rowcount);
		} catch(Exception e){
			System.err.println(e.getMessage());
		}
		
	}	
	
	public static boolean updateStatus(DBproject esql, String inputAppointmentID, String inputDoctorID){
		String query;
		List<List<String>> resultset = new ArrayList<List<String>>();
		try{
			query = "select status from appointment " +
				"where appnt_id = " + inputAppointmentID;
			resultset = esql.executeQueryAndReturnResult(query);
		} catch(Exception e){
			System.err.println(e.getMessage());
		}

		if(resultset.get(0).get(0).equals("PA")){	
			return false; //didn't have to update anything
		}
		else if(resultset.get(0).get(0).equals("AC")){
			try{
				query = "update appointment set status = 'WL' " +
					"where appnt_ID = " + inputAppointmentID;
				
				esql.executeUpdate(query);
				
				System.out.println("Updated appointment record: ");
				String query2 = "select * from appointment where appnt_ID = " + inputAppointmentID;
				int rowcount = esql.executeQueryAndPrintResult(query2);
				System.out.println("Rowcount: " + rowcount);
	
			} catch(Exception e){
				System.err.println(e.getMessage());
			}
			
			return true;
		}
		else if(resultset.get(0).get(0).equals("AV")){
                        try{
                                query = "update appointment set status = 'AC' " +
                                        "where appnt_id = " + inputAppointmentID;
	                        
				esql.executeUpdate(query);
				
				System.out.println("Updated appointment record: ");
                                String query2 = "select * from appointment where appnt_ID = " + inputAppointmentID;
                                int rowcount = esql.executeQueryAndPrintResult(query2);
                                System.out.println("Rowcount: " + rowcount);	
                        } catch(Exception e){
                                System.err.println(e.getMessage());
                        }
			
			return true;
		}
		else if(resultset.get(0).get(0).equals("WL")){
                       return true;
		} //returns true if the has_apponitments table needs to be updated		
		return false;
	}

	public static boolean validHasAppointment(DBproject esql, String docid, String appid) {
		String query = "select doctor_id from has_appointment where appt_id = " + appid;
		List<List<String>> resultset = new ArrayList<List<String>>();

		try {
			resultset = esql.executeQueryAndReturnResult(query);
		} catch(Exception e){
                	System.err.println(e.getMessage());
                }
			
		if (resultset.isEmpty()) {
			//need to link appointment with doctor
			/*
			try {
				String query2 = "insert into has_appointment(appt_id, doctor_id) values (" + appid + ", " + docid + ")";
				esql.executeUpdate(query2);
				System.out.println("New record inserted into has_appointment: ");
				String query3 = "select * from has_appointment where appt_id = " + appid;
				int rowcount = esql.executeQueryAndPrintResult(query3);
				System.out.println("Rowcount: " + rowcount);
			} catch(Exception e){
                        	System.err.println(e.getMessage());
                	}*/
		} else if (!resultset.get(0).get(0).equals(docid)) {
			return false;
		}
		return true;
	}

	public static String getHospitalID(DBproject esql, String docid) {
		String getDepartment = "select did from doctor where doctor_ID = " + docid;
		List<List<String>> resultset = new ArrayList<List<String>>();

		try {
			resultset = esql.executeQueryAndReturnResult(getDepartment);
		} catch(Exception e){
                	System.err.println(e.getMessage());
                }

		String getHospital = "select hid from department where dept_ID = " + resultset.get(0).get(0);
		List<List<String>> resultset1 = new ArrayList<List<String>>();

		try {
			resultset1 = esql.executeQueryAndReturnResult(getHospital);
		} catch(Exception e){
                        System.err.println(e.getMessage());
                }

		return resultset1.get(0).get(0);
	}

	public static void MakeAppointment(DBproject esql) {//4
		// Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB
		Scanner input = new Scanner(System.in);	
		System.out.print("Please enter patient name: ");
		String patientName = input.nextLine();
		
		while (!validString(patientName)) {
			System.out.println("Invalid input, please re-enter Patient name: ");
			patientName = input.nextLine();
		}
		
		System.out.println("Enter patient gender: ");
		char patientGender = 'X';
		while (patientGender == 'X') {
			patientGender = input.next().charAt(0);
			input.nextLine();
			if(patientGender == 'f' || patientGender == 'F'){
				patientGender = 'F';
			}
			else if(patientGender == 'm' || patientGender == 'M'){
				patientGender = 'M';
			}
			else{
				patientGender = 'X';
				System.out.println("Invalid gender! Please re-enter patient gender (F/M): ");
			}
		}

		System.out.println("Enter patient age: ");
		int patientAge = Integer.parseInt(input.nextLine());
		while (patientAge < 0 || patientAge > 150) {
			System.out.println("Invalid input! Please re-enter patient age: ");
			patientAge = Integer.parseInt(input.nextLine());
		}

		System.out.println("Enter patient address: "); 
		String patientAddress = input.nextLine();

		//get whether or not patient exists

		boolean exists = validPatientEntry(esql, patientName, patientGender, patientAge, patientAddress); // exists will be false, not exists will be true
		List<List<String>> patientIDresultset = new ArrayList<List<String>>();
		String patientID = "";

		if (!exists) { //actually is when it exists
			//get patient ID
			try {
				String getPatientID = "select patient_ID from patient where name = '" + patientName + "' and gtype = '" + patientGender + "' and age = " + patientAge + " and address = '" + patientAddress + "'";
				patientIDresultset = esql.executeQueryAndReturnResult(getPatientID);
				patientID = patientIDresultset.get(0).get(0);
			} catch(Exception e){
                        	System.err.println(e.getMessage());
                	}
		} else {
			int newID = Integer.parseInt(getValidPatientID(esql));
			newID += 1;
			patientID = Integer.toString(newID);
			
			try {
				String insertNewPatient = "insert into patient (patient_ID, name, gtype, age, address, number_of_appts) values (" + patientID + ", '" + patientName + "', '" + patientGender + "', " + patientAge + ", '" + patientAddress + "', 0)";
				esql.executeUpdate(insertNewPatient);

				System.out.println("Updated patient table with new patient: ");
				String getRecord = "select * from patient group by patient_id order by patient_id DESC limit 1";
				int rowcount = esql.executeQueryAndPrintResult(getRecord);
				System.out.println("Rowcount: " + rowcount); 
			} catch(Exception e){
                                System.err.println(e.getMessage());
                        }
		}

		System.out.print("Please enter doctor ID: ");
		String doctorID = input.nextLine();
		while(!(validDoctor(esql, doctorID))){
			System.out.println("Invalid input, please re-enter Doctor ID: ");
			doctorID = input.nextLine();
		}

		System.out.print("Please enter desired appointment ID: "); 
		String appointmentID = input.nextLine();
		while(!(validAppointment(esql, appointmentID))){
			System.out.println("Invalid input, please re-enter Appointment ID: ");
			appointmentID = input.nextLine();	
		}

		if (validTimeslot(esql, appointmentID, doctorID) && validPatientNumber(esql, doctorID)) {
			if (!validHasAppointment(esql, doctorID, appointmentID)) {
                        	System.out.println("This appointment already exists under a different doctor. Please retry.");
                	} else {
				if (updateStatus(esql, appointmentID, doctorID)) {// updates appointment
				//update searches table as well
					String hid = getHospitalID(esql, doctorID); 
					try {
						String updateSearches = "insert into searches(hid, pid, aid) values (" + hid + ", " + patientID + ", " + appointmentID + ")";
						esql.executeUpdate(updateSearches);
			
						System.out.println("Updated searches table: ");
						String getNewSearches = "select * from searches where hid = " + hid + " and pid = " + patientID + " and aid = " + appointmentID;
						int rowcount = esql.executeQueryAndPrintResult(getNewSearches);
						System.out.println("Rowcount: " + rowcount); 
					} catch(Exception e){
                        	        	System.err.println(e.getMessage());
                        		}

					try {
                                		String query2 = "insert into has_appointment(appt_id, doctor_id) values (" + appointmentID + ", " + doctorID + ")";
                                		esql.executeUpdate(query2);
                  		              	System.out.println("Updated has_appointment table: ");
                        		        String query3 = "select * from has_appointment where appt_id = " + appointmentID;
                        		        int rowcount = esql.executeQueryAndPrintResult(query3);
                  			        System.out.println("Rowcount: " + rowcount);
            		            	} catch(Exception e){
                                		System.err.println(e.getMessage());
                        		}
				} else {
					System.out.println("Appointment is already PA status. Unable to schedule appointment.");
				}
			}
		} else {
			System.out.println("Doctor ID " + doctorID +  " either does not have an open timeslot at the time of appointment " + appointmentID + " or already has the maximum number of patients/appointments for that timeslot. Unable to schedule appointment " + appointmentID + " with Doctor ID " + doctorID + ".");
		}
	}

	public static boolean validTimeslot(DBproject esql, String appid, String docid) {
		String getAppTimeslot = "select time_slot from appointment where appnt_ID = " + appid;
		List<List<String>> appTimeslotResult = new ArrayList<List<String>>();

		String getDocTimeslot = "select time_slot from request_maintenance where did = " + docid;
		List<List<String>> docTimeslotResult = new ArrayList<List<String>>();

		try {
			appTimeslotResult = esql.executeQueryAndReturnResult(getAppTimeslot);
			docTimeslotResult = esql.executeQueryAndReturnResult(getDocTimeslot);
		} catch(Exception e){
                	System.err.println(e.getMessage());
                }	

		if (docTimeslotResult.isEmpty()) {
			return false;
		}

		for (List<String> dtime : docTimeslotResult) {
			for (String timeslot : dtime) {
				if (timeslot.equals(appTimeslotResult.get(0).get(0))) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean validPatientNumber(DBproject esql, String docid) {
		String getMaxNum = "select patient_per_hour from request_maintenance where did = " + docid;
		List<List<String>> getNumPatientsResult = new ArrayList<List<String>>();

		String getTotalAppointments = "select count(appt_id) from has_appointment where doctor_id = " + docid;
		List<List<String>> getTotalAppointmentsResult = new ArrayList<List<String>>();

		try {
			getNumPatientsResult = esql.executeQueryAndReturnResult(getMaxNum);
			getTotalAppointmentsResult = esql.executeQueryAndReturnResult(getTotalAppointments);
		} catch(Exception e){
                        System.err.println(e.getMessage());
                }
		
		if (getNumPatientsResult.isEmpty()) {
			return false;
		} else if (getTotalAppointmentsResult.isEmpty()) {
			return true;
		} else if (Integer.parseInt(getNumPatientsResult.get(0).get(0)) > Integer.parseInt(getTotalAppointmentsResult.get(0).get(0))) {
			return true;
		}

		return false;
	}

	public static void ListAppointmentsOfDoctor(DBproject esql) {//5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor
		
		Scanner in = new Scanner(System.in);
		System.out.println("Enter Doctor ID: ");
		String docid = in.nextLine();

		while (!validDoctor(esql, docid)) {
			System.out.println("Invalid input, please re-enter Doctor ID: ");
                        docid = in.nextLine();			
		}	

		System.out.println("Enter a date range in the format YYYY-MM-DD:\n Date 1: ");
                String first = in.nextLine();

		while(!getValidDate(first)) {
                        System.out.print("Invalid date! Please follow format (YYYY-MM-DD)! Date 1: ");
                        first = in.nextLine();
                }
			
		System.out.println("Date 2: ");				
		String second = in.nextLine();

                while(!getValidDate(second)) {
                        System.out.print("Invalid date! Please follow format (YYYY-MM-DD)! Date 2: ");
                        second = in.nextLine();
                }
	
		System.out.println("List of active and available appointments for Doctor ID " + docid + ": ");

		try {
			String query = "SELECT appnt_ID FROM appointment LEFT JOIN has_appointment ON appointment.appnt_ID = has_appointment.appt_id WHERE (appointment.adate >= '" + first + "' AND appointment.adate <= '" + second + "') AND (appointment.status = 'AC' OR appointment.status = 'AV') AND (has_appointment.doctor_id = " + docid + ")";
			int rc = esql.executeQueryAndPrintResult(query);
			System.out.println("Row count: " + rc);
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
		// For a department name and a specific date, find the list of available appointments of the department
		Scanner input = new Scanner (System.in);
		System.out.println("Enter department name: ");
		String departmentName = input.nextLine();

		while(!(validDepartmentName(esql, departmentName))){
			System.out.print("Please enter a valid department name: ");
			departmentName = input.nextLine();

		}
		
		System.out.print("Enter date: ");
		String inputDate = input.nextLine();

		while(!(getValidDate(inputDate))){
			System.out.print("Please enter a valid date (follow format YYYY-MM-DD): ");
			inputDate = input.nextLine();
		}

		System.out.println("List of available appointments for department " + departmentName + ": ");
		try{
			String query = "select appnt_id, time_slot " + 
					"from appointment join has_appointment on appointment.appnt_ID = has_appointment.appt_id " + 
					"join doctor " + 
					"on has_appointment.doctor_id = doctor.doctor_id " + 
					"join department " +
					"on doctor.did = dept_ID " + 
					"where appointment.status = 'AV' and appointment.adate = '" + inputDate + "' and department.name = '" + departmentName + "'";
			int rowcount = esql.executeQueryAndPrintResult(query);
			System.out.println("Rowcount: " + rowcount);
		} catch(Exception e){
			System.err.println(e.getMessage());
		}
			 
	}

	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
		// Count number of different types of appointments per doctors and list them in descending order
		List<List<String>> resultset = new ArrayList<List<String>>();
		String query = "select doctor.name, count(appointment.appnt_ID) as num_appnt, appointment.status from doctor join has_appointment on doctor.doctor_ID = has_appointment.doctor_id left join appointment on has_appointment.appt_id = appointment.appnt_ID group by doctor.doctor_id, appointment.status order by doctor.doctor_id ASC, num_appnt DESC";

		try {
			resultset = esql.executeQueryAndReturnResult(query);
		} catch(Exception e) {
                        System.err.println(e.getMessage());
                }

		String output = "";
		String prevname = "";

		for (List<String> record : resultset) {
			if (prevname.equals(record.get(0))) {
				output += ", " + record.get(1) + record.get(2);
			} else {
				if (output.length() > 0) {
					System.out.println(output);
				}
				output = record.get(0) + ": " + record.get(1) + record.get(2);
				prevname = record.get(0);
			}
		}
	}
	
	public static void FindPatientsCountWithStatus(DBproject esql) {//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.
		Scanner in = new Scanner(System.in);	

		System.out.println("Please insert a valid appointment status (PA/AC/AV/WL): ");
		String stat = in.nextLine();

		while (!getValidAppointmentStatus(stat)) {
			System.out.println("Invalid appointment status! Please retry (PA/AC/AV/WL): ");
			stat = in.nextLine();
		}
		
		String query = "select doctor.name, count(distinct searches.pid) as number_of_patients from doctor join has_appointment on doctor.doctor_id = has_appointment.doctor_id join appointment on has_appointment.appt_id = appointment.appnt_id join searches on appointment.appnt_id = searches.aid where appointment.status = '" + stat  + "' group by doctor.name, doctor.doctor_id order by doctor.doctor_id";	
		try {
			esql.executeQueryAndPrintResult(query);
		} catch(Exception e) {
                        System.err.println(e.getMessage());
                }
	}
}
