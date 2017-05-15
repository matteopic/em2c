package em2c;

import java.util.List;

public class Entry {

	public Entry(double amount, String note){
		this.amount = amount;
		this.note = note;
	}

	public static String toCashculatorExpression(List<Entry> entries){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < entries.size(); i++){
			if(i > 0)sb.append('+');
			Entry entry = entries.get(i);
			sb.append(entry.amount);
			if(entry.note != null && entry.note.trim().length() > 0){
				sb.append('(').append(entry.note.trim()).append(')');
			}
		}
		return sb.toString();
	}

	private double amount;
	private String note;
}
