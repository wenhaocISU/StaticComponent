package staticFamily;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StaticApp implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private List<StaticClass> classes = new ArrayList<StaticClass>();
	private String apkPath;
	private String packageName;
	private String dataFolder;
	
	public List<StaticClass> getClasses() {
		return classes;
	}
	
	public void addClass(StaticClass c) {
		this.classes.add(c);
	} 
	
	public void setClasses(List<StaticClass> classes) {
		this.classes = classes;
	}

	public String getApkPath() {
		return apkPath;
	}

	public void setApkPath(String apkPath) {
		this.apkPath = apkPath;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getDataFolder() {
		return dataFolder;
	}

	public void setDataFolder(String dataFolder) {
		this.dataFolder = dataFolder;
	}
	
	public StaticClass findClassByJavaName(String name) {
		for (StaticClass c : classes)
			if (c.getJavaName().equals(name))
				return c;
		return null;
	}

	public StaticClass findClassByDexName(String name)
	{
		for (StaticClass c : classes)
			if (c.getDexName().equals(name))
				return c;
		return null;
		
	}
	
	public String getInstrumentedApkPath() {
		String result = this.dataFolder + "/";
		result += apkPath.substring(apkPath.lastIndexOf("/")+1, apkPath.lastIndexOf(".apk"));
		result += "_instrumented.apk";
		return result;
	}

	public String getUnsignedApkPath() {
		String result = this.dataFolder + "/";
		result += apkPath.substring(apkPath.lastIndexOf("/")+1, apkPath.lastIndexOf(".apk"));
		result += "_unsigned.apk";
		return result;
	}
	
	public StaticMethod findMethod(String methodSignature)
	{
		if (!methodSignature.contains("->"))
			return null;
		String className = methodSignature.substring(0, methodSignature.indexOf("->"));
		StaticClass c = findClassByDexName(className);
		if (c != null)
		{
			return c.getMethod(methodSignature);
		}
		return null;
	}
	
}
