package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class CommonProcess {
	
	public static void dataBaseFailure(HttpServletResponse response, Exception e) {
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.write("error\n"
					+ "database initialization failed\n"
					+ e.toString());
			writer.flush();
			writer.close();
			return;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static String[] cookies2Session(Cookie[] cookies) {
		String sessionID = null, username = null;
		for(Cookie cookie : cookies) {
			if(cookie.getName().equals("sessionID"))
				sessionID = cookie.getValue();
			else if(cookie.getName().equals("username"))
				username = cookie.getValue();
		}
		if(sessionID != null && username != null)
			return new String[]{username, sessionID};
		else
			return null;
	}
}
