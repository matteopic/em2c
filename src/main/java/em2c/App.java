package em2c;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class App {
	
	public App(){
		entries = new TreeMap<String, List<Entry>>();
	}

	public void start(String file) throws SQLException{
		Connection conn = DriverManager.getConnection("jdbc:sqlite:" + file);
		Statement stmt  = conn.createStatement();
		ResultSet rs = stmt.executeQuery(
				"SELECT recorded  AS Data," +
				//"SELECT strftime('%m/%d/%Y',datetime(recorded, 'unixepoch')) AS Data,\r\n" + 
				"    name AS Categoria," + 
				"    CASE WHEN amount > 0" + 
				"       THEN amount" + 
				"       ELSE NULL" + 
				"    END AS Uscite," + 
				"" + 
				"    CASE WHEN amount < 0" + 
				"       THEN -amount" + 
				"       ELSE NULL" + 
				"    END AS Entrate," + 
				"" + 
				"    note AS Note " + 
				"FROM bills INNER JOIN categories ON bills.category_id=categories._id " + 
				//"WHERE recorded > strftime('%s', '2016-11-21 00:00:00.000')\r\n" + 
				"ORDER BY recorded DESC, name ASC" 
				);

		while(rs.next()){
			long unixTS = rs.getLong(1);
			Date date = new Date(unixTS * 1000L);
			String cat = rs.getString(2);
			double out = rs.getDouble(3);
			double in = rs.getDouble(4);
			String note = rs.getString(5);
			collect(date, cat, out, in, note); 
		}

		printEntries(lastY, lastM);

		rs.close();
		stmt.close();
		conn.close();
	}

	private Map<String, List<Entry>>entries;

	private int lastY, lastM, lastD;
	private void collect(Date date, String cat, double out, double in, String note) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int y = cal.get(Calendar.YEAR);
		int m = cal.get(Calendar.MONTH);
		int d = cal.get(Calendar.DAY_OF_MONTH);

		if(y != lastY || m != lastM){
			printEntries(y, m);
			entries.clear();
		}
		
		double amount = out;
		if(in > 0){
			amount = in;
			cat = "ENTRATE";
		}

		List<Entry> data = entries.get(cat);
		if(data == null){
			data = new ArrayList<Entry>();
			entries.put(cat, data);
		}

		data.add(new Entry(amount, note));
		lastY = y;
		lastM = m;
		lastD = d;
	}

	private void printEntries(int year, int month) {
		if(entries.isEmpty())return;

		System.out.println(YearMonth.of(year, month + 1));
		for(Map.Entry<String, List<Entry>> me : entries.entrySet()){
			String category = me.getKey();
			List<Entry> entries = me.getValue();
			String expr = Entry.toCashculatorExpression(entries);

			System.out.println(category+": " + expr);
		}

		System.out.println();
	}

	public static void main(String[] args) throws Exception {
		//Path to .expensemanager file
		String expenseManagerFile = "";
		App app = new App();
		app.start(expenseManagerFile);
	}

}
