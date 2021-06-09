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

	public static String getValidDoctorID(DBproject esql) {
		List<List<String>> resultset = new ArrayList<List<String>>();

		try {
			String query = "SELECT MAX(doctor_ID) FROM Doctor";
			resultset = esql.executeQueryAndReturnResult(query);
		} catch(Exception e) {
                        System.err.println(e.getMessage());
                }

		return resultset.get(0).get(0);
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

		try {
			String query = "INSERT INTO Doctor(doctor_ID, name, specialty, did) " + 
					"VALUES ('" + newID + "', '" + dname + "', '" + dspecialty + "', '" + did + "')"; 
			esql.executeUpdate(query);

			String query2 = "select * from Doctor where doctor_ID = " + newID;
			int rowcount = esql.executeQueryAndPrintResult(query2);
			System.out.println(rowcount);
		} catch(Exception e) {
                        System.err.println(e.getMessage());
                } 				
	}

	public static void AddPatient(DBproject esql) {//2
		Scanner input = new Scanner(System.in);
		String patientName;
		do{
			System.out.print("Enter name: ");
			patientName = input.nextLine();
		
	
			if(!isCharInput(patientName)){
				System.out.println("Invalid name! No numbers or symbols!");
			}
		}while(!isCharInput(patientName));
		

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
				System.err.println("Invalid age! Please choose a number!");
			}
		} while(patientAge == -1);

		System.out.print("Enter address: ");
		String patientAddress = input.nextLine();

		try{	
			System.out.println("Updating now...");
			String query = "insert into patient values ((select count(*) as yes from patient)+1, '" 
					+ patientName + "', '" + String.valueOf(patientGender) + "', " 
					+ Integer.toString(patientAge) + ", '" + patientAddress + "', 0)";
			esql.executeUpdate(query);
		} catch(Exception e){
			System.err.println(e.getMessage());
		}
					
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
			
	
	public static boolean getValidTimeslot(String input){
		return true;
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
	
	public static String getValidAppointmentID(DBproject esql){
		List<List<String>> resultset = new ArrayList<List<String>>();
		try{
			String query = "select MAX(appnt_ID) from appointment";
			resultset = esql.executeQueryAndReturnResult(query);
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
		return resultset.get(0).get(0) + 1;		
	}
				

	public static void AddAppointment(DBproject esql) {//3
		Scanner input = new Scanner(System.in);
		
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
		
	
		System.out.println("Updating appointment...");
		try{
			String query = "insert into appointment " +
				       "values (" + getValidAppointmentID(esql) + ", '" + appointmentDate + "', '" +
				       timeSlotInput + "', '" + appointmentStatus + "')";
			esql.executeUpdate(query);
		} catch(Exception e){
			System.err.println(e.getMessage());
		}
	}


			
	
			
			 
		
		


	public static void MakeAppointment(DBproject esql) {//4
		// Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB
	}

	public static void ListAppointmentsOfDoctor(DBproject esql) {//5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor
	}

	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
		// For a department name and a specific date, find the list of available appointments of the department
	}

	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
		// Count number of different types of appointments per doctors and list them in descending order
	}

	
	public static void FindPatientsCountWithStatus(DBproject esql) {//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.
	}
	public static boolean isCharInput(String input){
		input = input.toLowerCase();
		char[] search = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
		int count = 0;
		for(int i = 0; i < input.length(); i++){
			char x = input.charAt(i);
			for(int j = 0; j < search.length; j++){
				if(x == search[j]){
					count++;
				}
			}
		}
		if(count == input.length()){
			return true;
		}
		else{
			return false;
		}
	}
}
