package reflection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import staticFamily.StaticApp;
import staticFamily.StaticClass;
import staticFamily.StaticMethod;
import staticFamily.StaticStmt;
import symbolic.Blacklist;
import analysis.StaticInfo;
import analysis.Utility;

public class APISignatures {

	
	public static List<String> Sigs_forName = Arrays.asList(new String[] {
		"Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;",
		"Ljava/lang/Class;->forName(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;"
	});
	
	public static List<String> Sigs_getMethod = Arrays.asList(new String[] {
		"Ljava/lang/Class;->getDeclaredMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
		"Ljava/lang/Class;->getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
		"Ljava/lang/Class;->getMethods()[Ljava/lang/reflect/Method;"
	});
	
	
	public static void main(String[] args) throws Exception
	{
		PrintStream out = new PrintStream(new FileOutputStream("/home/wenhaoc/refreport.txt"));
		System.setOut(out);
		File[] apps = new File("/home/wenhaoc/AppStorage/APAC_engagement12/Apps/").listFiles();
		StaticInfo.instrumentApps = false;
		for (File appFolder : apps)
		{
			if (!appFolder.isDirectory())
				continue;
			//System.out.println("app " + index++ + ": " + appFolder.getName());
			
			for (File app : appFolder.listFiles())
			{
				if (app.isFile() && app.getName().endsWith(".apk"))
				{
					List<String> forNameCallSite = new ArrayList<String>();
					List<String> getMethodCallSite = new ArrayList<String>();
					StaticApp staticApp = StaticInfo.initAnalysis(app.getAbsolutePath(), false);
					for (StaticClass c : staticApp.getClasses())
					{
						if (Blacklist.classInBlackList(c.getDexName()))
							continue;
						for (StaticMethod m : c.getMethods())
							for (StaticStmt s : m.getSmaliStmts())
							{
								if (s.invokesMethod())
								{
									String sig = (String) s.getData();
									if (Sigs_forName.contains(sig))
										forNameCallSite.add(c.getDexName() + " " + s.getSourceLineNumber());
									else if (Sigs_getMethod.contains(sig))
										getMethodCallSite.add(c.getDexName() + " " + s.getSourceLineNumber());
								}
							}
					}
					if (forNameCallSite.size()>0 || getMethodCallSite.size()> 0)
					{
						System.out.println("=======================================================================");
						if (forNameCallSite.size()>0)
						{
							System.out.println("[forName]");
							for (String s: forNameCallSite)
								System.out.println(s);
						}
						if (getMethodCallSite.size()> 0)
						{
							System.out.println("[getMethod]");
							for (String s : getMethodCallSite)
								System.out.println(s);
						}
						System.out.println("=======================================================================\n");
					}
				}
			}

		}

	}
	
	public static void temp() throws Exception
	{
		BufferedReader in = new BufferedReader(new FileReader("/home/wenhaoc/targets"));
		PrintWriter out = new PrintWriter(new FileWriter("/home/wenhaoc/newTargets"));
		String line;
		while ((line = in.readLine())!= null)
		{
			if (line.startsWith("L"))
			{
				String newLine = line.substring(1);
				newLine = newLine.replace("/", ".").replace("; ", ":");
				newLine = "\"" + newLine + "\",";
				line = newLine;
			}
			out.write(line + "\n");
			out.flush();
		}
		in.close();
		out.close();
	}
	
	public void checkReflectionAPIs(StaticApp staticApp)
	{
		List<String> forNameList = new ArrayList<String>();
		List<String> getMethodList = new ArrayList<String>();
		int count_total = 0, count_forName = 0, count_getMethod = 0;
		for (StaticClass c : staticApp.getClasses())
		{
			for (StaticMethod m : c.getMethods())
			{
				for (StaticStmt s : m.getSmaliStmts())
				{
					if (!s.invokesMethod())
						continue;
					String sig = (String) s.getData();
					if (sig.equals("Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;"))
					{
						//TODO definitely A
						
					}
					else if (sig.equals("Ljava/lang/Class;->forName(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;"))
					{
						//TODO definitely B
						System.out.println("[Aug26]" + m.getSignature() + "\t" + s.getSourceLineNumber());
					}
				}
			}
		}
	}
	
}
