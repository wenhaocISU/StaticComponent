package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilderFactory;

import main.Paths;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import staticFamily.StaticApp;
import staticFamily.StaticClass;
import tools.Apktool;
import tools.Jarsigner;

public class StaticInfo {

	public static boolean instrumentApps = true;
	public static boolean decodeRes = true;
	public static boolean parseManifest = true;
	
	public static StaticApp initAnalysis(String apkPath, boolean forceBuild)
	{
		File apk = new File(apkPath);
		StaticApp staticApp = new StaticApp();
		try {
			if (!apk.exists())
				throw (new Exception("apk file does not exist!"));
			String staticAppObjectPath = Paths.AppDataDir + "/" + apk.getName().replace(" ", "") + "/static.info";
			// read or build StaticApp
			if (!forceBuild)
			{
				staticApp = (StaticApp) Utility.readObject(staticAppObjectPath);
			}
			if (forceBuild || staticApp == null)
			{
				staticApp = new StaticApp();
				staticApp.setApkPath(apkPath);
				staticApp.setDataFolder(Paths.AppDataDir + "/" + apk.getName().replace(" ", ""));
				buildStaticApp(staticApp);
				Utility.saveObject(staticApp, staticAppObjectPath);
			}
			// build instrumented apk
			if (instrumentApps)
			{
				File instrumentedApp = new File(staticApp.getInstrumentedApkPath());
				if (!instrumentedApp.exists() || forceBuild)
				{
					File unsignedApp = new File(staticApp.getUnsignedApkPath());
					if (!unsignedApp.exists() || forceBuild)
					{
						String sourceDir = staticApp.getDataFolder() + "/apktool";
						String outPath = staticApp.getUnsignedApkPath();
						Apktool.buildAPK(sourceDir, outPath);
					}
					Jarsigner.signAPK(staticApp.getUnsignedApkPath(),
							staticApp.getInstrumentedApkPath());
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return staticApp;
	}
	
	/**
	 * StaticApp building steps:
	 * 1. use apktool to extract source(smali) and resources(xml)
	 * 2. parse each smali file, create StaticClass objects using multithreading
	 * 3. sort call graph, complete inner/outer class relations
	 * 4. parse xmls, including AndroidManifest, layouts, etc.
	 * */
	private static List<StaticClassBuilder> classBuilders = new ArrayList<StaticClassBuilder>();
	private static void buildStaticApp(StaticApp staticApp)
	{
		// Step 1
		String apkPath = staticApp.getApkPath();
		String outDir = staticApp.getDataFolder() + "/apktool/";
		Apktool.extractAPK(apkPath, outDir, decodeRes);
		// Step 2
		File smaliFolder = new File(staticApp.getDataFolder() + "/apktool/smali");
		initClassBuilders(staticApp, smaliFolder);
		int threadCount = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		for (StaticClassBuilder classBuilder : classBuilders)
		{
			try {
				StaticClass c = executor.submit(classBuilder).get();
				staticApp.addClass(c);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		executor.shutdown();
		// Step 3
		for (StaticClass c : staticApp.getClasses())
		{
			/** 
			 Inner classes already know their outer class, but
			 outer classes might now know all of their inner classes.
			 Because those inner classes that were declared within
			 a method body, will not appear in the 'MemberClasses'
			 annotation. 
			**/
			if (c.isInnerClass())
			{
				StaticClass outerC = staticApp.findClassByJavaName(c.getOuterClass());
				if (outerC != null)
					outerC.addInnerClass(c.getJavaName());
			}
// Call graph is skipped for now, it takes too long
/*			for (StaticMethod m : c.getMethods())
			{
				for (StaticStmt s : m.getSmaliStmts())
				{
					if (s.invokesMethod())
					{
						String targetSig = (String) s.getData();
						m.addOutCallTargetSig(targetSig);
						StaticMethod targetM = staticApp.findMethod(targetSig);
						if (targetM != null)
							targetM.addInCallSourceSig(m.getSignature());
					}
					else if ( s.getSmaliStmt().contains("iget")
							||s.getSmaliStmt().contains("iput")
							||s.getSmaliStmt().contains("sget")
							||s.getSmaliStmt().contains("sput"))
					{
						String targetSig = (String) s.getData();
						m.addOutCallTargetSig(targetSig);
						StaticField targetF = staticApp.findField(targetSig);
						if (targetF != null)
							targetF.addInCallSourceSigs(m.getSignature());
					}
				}
			}
*/
		}
		// Step 4
		if (parseManifest)
			parseManifest(staticApp);
	}

	private static void initClassBuilders(StaticApp staticApp, File smaliFile)
	{
		if (smaliFile.isDirectory())
		{
			File subFiles[] = smaliFile.listFiles();
			for (File f : subFiles)
				initClassBuilders(staticApp, f);
		}
		else if (smaliFile.isFile() && smaliFile.getName().endsWith(".smali"))
		{
			try
			{
				int maxLineNumber = 1;
				ArrayList<String> smaliCode = new ArrayList<String>();
				BufferedReader in = new BufferedReader(new FileReader(smaliFile));
				String outFilePrefix = staticApp.getDataFolder() + "/apktool/oldSmali/";
				String outFileSuffix = smaliFile.getAbsolutePath();
				outFileSuffix = outFileSuffix.substring(
						outFileSuffix.indexOf(staticApp.getDataFolder() + "/apktool/smali/")
						+ (staticApp.getDataFolder() + "/apktool/smali/").length());
				File outFile = new File(outFilePrefix + outFileSuffix);
				outFile.getParentFile().mkdirs();
				PrintWriter out = new PrintWriter(new FileWriter(outFile));
				String line;
				while ((line = in.readLine()) != null)
				{
					smaliCode.add(line);
					out.write(line + "\n");
					if (line.contains(".line "))
					{
						int thisLineNumber = Integer.parseInt(line.substring(line.indexOf(".line ")+6));
						if (thisLineNumber > maxLineNumber)
							maxLineNumber = thisLineNumber;
					}
				}
				in.close();
				out.close();
				StaticClassBuilder classBuilder = new StaticClassBuilder();
				classBuilder.setSmaliCode(smaliCode);
				classBuilder.setSmaliFilePath(smaliFile.getAbsolutePath());
				classBuilder.setMaxOriginalLineNumber(maxLineNumber+1);
				classBuilders.add(classBuilder);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	
	private static void parseManifest(StaticApp staticApp) {
		try {
			File manifestFile = new File(staticApp.getDataFolder() + "/apktool/AndroidManifest.xml");
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(manifestFile);
			doc.getDocumentElement().normalize();
			Node manifestNode = doc.getFirstChild();
			String pkgName = manifestNode.getAttributes().getNamedItem("package").getNodeValue();
			staticApp.setPackageName(pkgName);
			NodeList aList = doc.getElementsByTagName("activity");
			boolean mainActFound = false;
			for (int i = 0, len = aList.getLength(); i < len; i++) {
				Node a = aList.item(i);
				String aName = a.getAttributes().getNamedItem("android:name").getNodeValue();
				if (aName.startsWith("."))
					aName = aName.substring(1, aName.length());
				if (!aName.contains("."))
					aName = pkgName + "." + aName;
				StaticClass c = staticApp.findClassByJavaName(aName);
				if (c == null)
					continue;
				c.setIsActivity(true);
				Element e = (Element) a;
				NodeList actions = e.getElementsByTagName("action");
				for (int j = 0, len2 = actions.getLength(); j < len2; j++) {
					Node action = actions.item(j);
					if (action.getAttributes().getNamedItem("android:name").getNodeValue().equals("android.intent.action.MAIN")) {
						c.setIsMainActivity(true);
						mainActFound = true;
						break;
					}
				}
			}
			if (!mainActFound) {
				NodeList aaList = doc.getElementsByTagName("activity-alias");
				for (int i = 0, len = aaList.getLength(); i < len; i++) {
					if (mainActFound)
						break;
					Node aa = aaList.item(i);
					String aName = aa.getAttributes().getNamedItem("android:targetActivity").getNodeValue();
					if (aName.startsWith("."))
						aName = aName.substring(1, aName.length());
					if (!aName.contains("."))
						aName = pkgName + "." + aName;
					Element e = (Element) aa;
					NodeList actions = e.getElementsByTagName("action");
					for (int j = 0, len2 = actions.getLength(); j < len2; j++) {
						Node action = actions.item(j);
						if (action.getAttributes().getNamedItem("android:name").getNodeValue().equals("android.intent.action.MAIN")) {
							StaticClass c = staticApp.findClassByJavaName(aName);
							c.setIsMainActivity(true);
							mainActFound = true;
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
