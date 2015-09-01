package api;

import java.io.File;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import staticFamily.StaticApp;
import staticFamily.StaticClass;
import staticFamily.StaticMethod;
import staticFamily.StaticStmt;
import analysis.StaticInfo;
import analysis.Utility;

public class StringAPICollector {

	public static List<String> EngagementAppSigs_List = Arrays.asList(new String[] {
			"Ljava/lang/String;->offsetByCodePoints(II)I",
			"Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;",
			"Ljava/lang/StringBuilder;->substring(I)Ljava/lang/String;",
			"Ljava/lang/String;->valueOf([CII)Ljava/lang/String;",
			"Ljava/lang/StringBuilder;->replace(IILjava/lang/String;)Ljava/lang/StringBuilder;",
			"Ljava/lang/String;->getBytes()[B",
			"Ljava/lang/StringBuilder;->capacity()I",
			"Ljava/lang/String;->toCharArray()[C",
			"Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
			"Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;",
			"Ljava/lang/String;->toUpperCase()Ljava/lang/String;",
			"Ljava/lang/String;->replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
			"Ljava/lang/String;-><init>([B)V",
			"Ljava/lang/StringBuilder;->setLength(I)V",
			"Ljava/lang/StringBuilder;->append([C)Ljava/lang/StringBuilder;",
			"Ljava/lang/StringBuilder;-><init>(I)V",
			"Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;",
			"Ljava/lang/StringBuilder;->append(Ljava/lang/CharSequence;II)Ljava/lang/StringBuilder;",
			"Ljava/lang/String;->hashCode()I",
			"Ljava/lang/StringBuilder;->insert(IC)Ljava/lang/StringBuilder;",
			"Ljava/lang/String;-><init>([BII)V",
			"Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
			"Ljava/lang/StringBuilder;->insert(II)Ljava/lang/StringBuilder;",
			"Ljava/lang/String;->charAt(I)C",
			"Ljava/lang/String;->compareToIgnoreCase(Ljava/lang/String;)I",
			"Ljava/lang/String;->compareTo(Ljava/lang/String;)I",
			"Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;",
			"Ljava/lang/StringBuilder;->delete(II)Ljava/lang/StringBuilder;",
			"Ljava/lang/StringBuilder;->ensureCapacity(I)V",
			"Ljava/lang/String;->indexOf(Ljava/lang/String;)I",
			"Ljava/lang/String;->indexOf(Ljava/lang/String;I)I",
			"Ljava/lang/StringBuilder;->insert(I[CII)Ljava/lang/StringBuilder;",
			"Ljava/lang/String;->lastIndexOf(II)I",
			"Ljava/lang/String;-><init>(Ljava/lang/StringBuffer;)V",
			"Ljava/lang/String;->codePointCount(II)I",
			"Ljava/lang/String;->valueOf(J)Ljava/lang/String;",
			"Ljava/lang/String;->codePointAt(I)I",
			"Ljava/lang/String;->valueOf(C)Ljava/lang/String;",
			"Ljava/lang/String;->indexOf(II)I",
			"Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;",
			"Ljava/lang/String;->substring(I)Ljava/lang/String;",
			"Ljava/lang/String;->format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;",
			"Ljava/lang/String;-><init>([BLjava/lang/String;)V",
			"Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V",
			"Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;",
			"Ljava/lang/String;->codePointBefore(I)I",
			"Ljava/lang/String;-><init>([BLjava/nio/charset/Charset;)V",
			"Ljava/lang/StringBuilder;->substring(II)Ljava/lang/String;",
			"Ljava/lang/StringBuilder;->getChars(II[CI)V",
			"Ljava/lang/StringBuilder;-><init>(Ljava/lang/CharSequence;)V",
			"Ljava/lang/String;->endsWith(Ljava/lang/String;)Z",
			"Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;",
			"Ljava/lang/StringBuilder;->insert(ILjava/lang/String;)Ljava/lang/StringBuilder;",
			"Ljava/lang/String;->getBytes(Ljava/lang/String;)[B",
			"Ljava/lang/String;->subSequence(II)Ljava/lang/CharSequence;",
			"Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;",
			"Ljava/lang/String;->substring(II)Ljava/lang/String;",
			"Ljava/lang/String;->getBytes(Ljava/nio/charset/Charset;)[B",
			"Ljava/lang/String;->contentEquals(Ljava/lang/CharSequence;)Z",
			"Ljava/lang/String;->startsWith(Ljava/lang/String;I)Z",
			"Ljava/lang/String;->regionMatches(ILjava/lang/String;II)Z",
			"Ljava/lang/String;->toUpperCase(Ljava/util/Locale;)Ljava/lang/String;",
			"Ljava/lang/String;->lastIndexOf(I)I",
			"Ljava/lang/String;-><init>([CII)V",
			"Ljava/lang/String;-><init>(Ljava/lang/String;)V",
			"Ljava/lang/String;->trim()Ljava/lang/String;",
			"Ljava/lang/String;->regionMatches(ZILjava/lang/String;II)Z",
			"Ljava/lang/String;->isEmpty()Z",
			"Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I",
			"Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;",
			"Ljava/lang/String;-><init>()V",
			"Ljava/lang/String;-><init>([BIILjava/lang/String;)V",
			"Ljava/lang/String;->toString()Ljava/lang/String;",
			"Ljava/lang/StringBuilder;->reverse()Ljava/lang/StringBuilder;",
			"Ljava/lang/String;->valueOf(I)Ljava/lang/String;",
			"Ljava/lang/StringBuilder;->append(Ljava/lang/StringBuffer;)Ljava/lang/StringBuilder;",
			"Ljava/lang/String;-><init>([C)V",
			"Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z",
			"Ljava/lang/String;->getChars(II[CI)V",
			"Ljava/lang/String;->getBytes(II[BI)V",
			"Ljava/lang/String;->startsWith(Ljava/lang/String;)Z",
			"Ljava/lang/String;->toLowerCase(Ljava/util/Locale;)Ljava/lang/String;",
			"Ljava/lang/StringBuilder;->appendCodePoint(I)Ljava/lang/StringBuilder;",
			"Ljava/lang/StringBuilder;->length()I",
			"Ljava/lang/String;->valueOf([C)Ljava/lang/String;",
			"Ljava/lang/String;->valueOf(F)Ljava/lang/String;",
			"Ljava/lang/String;->length()I",
			"Ljava/lang/StringBuilder;->setCharAt(IC)V",
			"Ljava/lang/String;->valueOf(Z)Ljava/lang/String;",
			"Ljava/lang/StringBuilder;->deleteCharAt(I)Ljava/lang/StringBuilder;",
			"Ljava/lang/StringBuilder;->lastIndexOf(Ljava/lang/String;)I",
			"Ljava/lang/String;->contentEquals(Ljava/lang/StringBuffer;)Z",
			"Ljava/lang/String;->indexOf(I)I",
			"Ljava/lang/String;->intern()Ljava/lang/String;",
			"Ljava/lang/StringBuilder;-><init>()V",
			"Ljava/lang/String;->lastIndexOf(Ljava/lang/String;I)I",
			"Ljava/lang/String;->valueOf(D)Ljava/lang/String;",
			"Ljava/lang/String;->split(Ljava/lang/String;I)[Ljava/lang/String;",
			"Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;",
			"Ljava/lang/StringBuilder;->append([CII)Ljava/lang/StringBuilder;",
			"Ljava/lang/StringBuilder;->append(D)Ljava/lang/StringBuilder;",
			"Ljava/lang/String;->replaceFirst(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
			"Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;",
			"Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z",
			"Ljava/lang/String;->equals(Ljava/lang/Object;)Z",
			"Ljava/lang/StringBuilder;->charAt(I)C",
			"Ljava/lang/String;->matches(Ljava/lang/String;)Z",
			"Ljava/lang/StringBuilder;->append(Ljava/lang/CharSequence;)Ljava/lang/StringBuilder;",
			"Ljava/lang/String;->toLowerCase()Ljava/lang/String;",
			"Ljava/lang/String;->replace(CC)Ljava/lang/String;",

	});
	
	
	private static ArrayList<String> stringAPISigs = new ArrayList<String>();
	private static Map<String, Integer> stringAPISigMap = new HashMap<String, Integer>();
	
	public static void main(String[] args) throws Exception
	{
		//main2();
		main3();
	}
	
	static void main3()
	{
		try
		{
			stringAPISigMap = (Map<String, Integer>) Utility.readObject("/home/wenhaoc/StringAPI.map");
			stringAPISigMap = sortByValue(stringAPISigMap);
			Map<String, Integer> sMap = new HashMap<String, Integer>();
			Map<String, Integer> sbMap = new HashMap<String, Integer>();
			
			for (Map.Entry<String, Integer> entry : stringAPISigMap.entrySet())
			{
				if (entry.getKey().startsWith("Ljava/lang/String;"))
					sMap.put(entry.getKey(), entry.getValue());
				else if (entry.getKey().startsWith("Ljava/lang/StringBuilder;"))
					sbMap.put(entry.getKey(), entry.getValue());
				else
					System.out.println("weird.");
			}
			sMap = sortByValue(sMap);
			sbMap = sortByValue(sbMap);
			for (Map.Entry<String, Integer> entry : sMap.entrySet())
				System.out.println(entry.getValue() + "\t\t" + entry.getKey());
			System.out.println("--------------------------------------------------------------------------------------");
			for (Map.Entry<String, Integer> entry : sbMap.entrySet())
				System.out.println(entry.getValue() + "\t\t" + entry.getKey());
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
    public static <K, V extends Comparable<? super V>> Map<K, V> 
    sortByValue( Map<K, V> map )
{
    List<Map.Entry<K, V>> list =
        new LinkedList<Map.Entry<K, V>>( map.entrySet() );
    Collections.sort( list, new Comparator<Map.Entry<K, V>>()
    {
        public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
        {
            return (o1.getValue()).compareTo( o2.getValue() );
        }
    } );

    Map<K, V> result = new LinkedHashMap<K, V>();
    for (Map.Entry<K, V> entry : list)
    {
        result.put( entry.getKey(), entry.getValue() );
    }
    return result;
}
	
	static void main2()
	{
		File[] apps = new File("/home/wenhaoc/AppStorage/APAC_engagement12/Apps/").listFiles();
		int index = 1;
		StaticInfo.instrumentApps = false;
		for (File appFolder : apps)
		{
			if (!appFolder.isDirectory())
				continue;
			System.out.println("app " + index++ + ": " + appFolder.getName());
			for (File app : appFolder.listFiles())
			{
				if (app.isFile() && app.getName().endsWith(".apk"))
				{
					StaticApp staticApp = StaticInfo.initAnalysis(app.getAbsolutePath(), false);
					for (StaticClass c : staticApp.getClasses())
						for (StaticMethod m : c.getMethods())
							for (StaticStmt s : m.getSmaliStmts())
							{
								if (s.invokesMethod())
								{
									String sig = (String) s.getData();
									if (!sig.startsWith("Ljava/lang/String;->") && !sig.startsWith("Ljava/lang/StringBuilder;->"))
										continue;
									if (stringAPISigMap.containsKey(sig))
										stringAPISigMap.put(sig, stringAPISigMap.get(sig)+1);
									else
										stringAPISigMap.put(sig, 1);
								}
							}
				}
			}
		}
		System.out.println("\n\n=======================================================================\n");
		for (Map.Entry<String, Integer> entry : stringAPISigMap.entrySet())
			System.out.println("put(\"" + entry.getKey() + "\"," + entry.getValue() + ");");
		Utility.saveObject(stringAPISigMap, "/home/wenhaoc/StringAPI.map");
	}
	
}
