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
	
	public StaticField findField(String fieldSignature)
	{
		if (!fieldSignature.contains("->"))
			return null;
		String className = fieldSignature.substring(0, fieldSignature.indexOf("->"));
		String subSig = fieldSignature.substring(fieldSignature.indexOf("->")+2);
		StaticClass c = findClassByDexName(className);
		if (c != null)
			return c.getField(subSig);
		return null;
	}
	
	public StaticClass getMainActivity()
	{
		for (StaticClass c : this.classes)
			if (c.isMainActivity())
				return c;
		return null;
	}
	
	/**
	 * Needed input:
	 * 	method signature
	 * 	StaticApp
	 *
	 * Steps:
	 * 	find StaticMethod from method signature. See which one of the 4 conditions it belong to:
	 * 		a) Static/private method. These 2 kinds of method can not be a virtual method
	 * 		b) Abstract method. The method has to be located within the specific object type where the method is implemented
	 * 		c) class name in methodSig NOT EQUALS type of $param0. The method is overriden, need to look for the right one based on the specific object type
	 * 		d) class name in methodSig EQUALS type of $param0. If method body found, return that body; if not, then need to recursively look into super classes
	 * */
	public StaticMethod findDynamicDispatchedMethodBody(String methodSig, String param0Type)
	{
		/**
		 * param0Type will be empty if target method is static
		 * */
		String classNameInSig = methodSig.substring(0, methodSig.indexOf("->"));
		StaticMethod m = findMethod(methodSig);
		if (param0Type.equals(""))
			return m;
		// case (a)
		if (m != null && (m.isStatic() || m.isPrivate()))
			return m;
		// case (b)
		if (m != null && m.isAbstract())
		{
			String newMethdSig = param0Type + methodSig.substring(methodSig.indexOf("->"));
			return findMethod(newMethdSig);
		}
		// case (c)
		if (!classNameInSig.equals(param0Type))
		{
			String newMethdSig = param0Type + methodSig.substring(methodSig.indexOf("->"));
			return findMethod(newMethdSig);
		}
		// case (d)
		StaticClass c = this.findClassByDexName(classNameInSig);
		String subSig = methodSig.substring(methodSig.indexOf("->")+2);
		while (m == null)
		{
			c = c.getSuperClass(this);
			m = c.getMethodBySubSig(subSig);
		}
		return m;
	}
	
}
