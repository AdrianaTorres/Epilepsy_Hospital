package security;

import java.util.ArrayList;
import java.util.Iterator;

public class FileEncryptor {
	private static String key="AJHjjbkBJHJnjnlnHBLblkb4513451515bhbj.-5";
	private static int len=key.toCharArray().length;
	
	public static String encryptString(String string) {
		String output="";
		string= string+" ";
		int j=0;
		for (int i = 0; i < string.length(); i++) {
			if(j==key.length()-1) {
				j=0;
			}
			int temp= Integer.valueOf(string.charAt(i))^Integer.valueOf(key.charAt(j));
			output=output+" "+temp;
		}
		return output;
	}
	public static String decryptString(String string) {
		ArrayList <Integer> ints=new ArrayList<Integer>();
		char[] input= string.toCharArray();
		String var="";
		
		for (int i = 0; i < input.length; i++) {
			if(input[i]==' ' && !var.equals("")) {
				ints.add(Integer.parseInt(var));
				var="";
			}
			if(input[i]!=' ') {
				var=var+input[i];
			}
		}
		int j=0;
		char val;
		var="";
		for (Iterator iterator = ints.iterator(); iterator.hasNext();) {
			val= (char) (((int) (iterator.next()))^Integer.valueOf(key.charAt(j)));
			var=var+val;
		}
		return var;
	}
}
