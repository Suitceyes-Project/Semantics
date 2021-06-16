package iti.suitceyes.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyFileLogger {
	
	String absolutePath;
	BufferedWriter bw;
	

	public MyFileLogger(String path, String filename) {
		
		this.bw = null;
		try {
			this.bw = new BufferedWriter(new FileWriter(path + filename, true));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public String appendNewLineWithTimestamp(String line) throws IOException{
	
		String timestamp = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
	
		String appendedLine = "[" + timestamp + "] " + line	 + "\n";		
		this.bw.write(appendedLine);
		this.bw.flush();
		
		return appendedLine;
	}
	
	public String appendNewLine(String line) throws IOException{
		
		String appendedLine = line + "\n";		
		this.bw.write(appendedLine);
		this.bw.flush();
		
		return appendedLine;
	}


}
