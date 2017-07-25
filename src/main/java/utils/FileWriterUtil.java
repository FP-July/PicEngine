package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileWriterUtil{
	private FileWriter fw = null;
	private BufferedWriter bw = null;
	
	public FileWriterUtil(String fileName) {
		try {
			DirectoryChecker.dirCheck(fileName);
			fw = new FileWriter(fileName);
			bw = new BufferedWriter(fw);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public FileWriterUtil(String fileName, boolean flag) {
		try {
			DirectoryChecker.dirCheck(fileName);
			fw = new FileWriter(fileName, flag);
			bw = new BufferedWriter(fw);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			if (bw != null) bw.close();
			if (fw != null) fw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void flush() {
		try {
			if (bw != null) bw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean writeLine(String line) {
		try {
			bw.write(line);
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}
