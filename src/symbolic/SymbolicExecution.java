package symbolic;

import java.util.ArrayList;

import builder.Utility;
import staticFamily.StaticApp;
import staticFamily.StaticClass;
import staticFamily.StaticMethod;
import staticFamily.StaticStmt;

public class SymbolicExecution {

	private StaticApp staticApp;
	public boolean debug = true;
	public boolean blackListOn = true;
	private ArrayList<PathSummary> pathSummaries;
	private ArrayList<ToDoPath> toDoPathList;
	
	public SymbolicExecution(StaticApp staticApp) 
	{
		this.staticApp = staticApp;
	}
	
	public ArrayList<PathSummary> doFullSymbolic(String methodSignature)
	{
		init();
		StaticMethod entryMethod = staticApp.findMethod(methodSignature);
		if (entryMethod == null)
		{
			System.out.println("Did not find the method with signature: "
								+ methodSignature);
			return null;
		}
		if (this.blackListOn && blacklistCheck(entryMethod))
		{
			System.out.println("Method " + methodSignature + " is on black list, skipped.");
			return pathSummaries;
		}
		System.out.println("Generating Symbolic PathSummaries for " + methodSignature);
		try 
		{
			ToDoPath toDoPath = new ToDoPath();
			PathSummary initPS = new PathSummary();
			initPS.setSymbolicStates(initSymbolicStates(entryMethod));
			initPS.setMethodSignature(methodSignature);
			symbolicExecution(initPS, entryMethod, toDoPath, true);
			pathSummaries.add(initPS);
			symbolicallyFinishingUp(false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return pathSummaries;
	}
	
	private void symbolicExecution
				(PathSummary pS, 
				 StaticMethod m, 
				 ToDoPath toDoPath, 
				 boolean inMainMethod) throws Exception
	{
		ArrayList<StaticStmt> allStmts = m.getSmaliStmts();
		String className = Utility.dexToJavaTypeName(m.getDeclaringClass());
		if (allStmts.size() < 1)
			return;
		StaticStmt s = allStmts.get(0);
		/**
		 * Each smali statement can have one of the six actions:
		 * 	ends method. (return, throw)
		 *  updates symbolic states. (any kind of expression)
		 *  switch jumps.
		 *  if jumps.
		 *  invoke method.
		 *  goto jumps.
		 * **/
		while (true)
		{
			if (this.debug)
			{
				System.out.println("[currentStmt]" 
							+ className + ":" + s.getSourceLineNumber()
							+ "  " + s.getSmaliStmt());
			}
			pS.addExecutionLog(className + ":" + s.getSourceLineNumber());
			if (s.endsMethod())
			{
				//TODO if not return void, update the returned symbol
				break;
			}
			else if (s.updatesSymbolicStates())
			{
				//TODO PS updates symbolic states
			}
			else if (s.switchJumps())
			{
				//TODO choose next stmt
				//TODO PS update path conditions
			}
			else if (s.ifJumps())
			{
				//TODO choose next stmt
				//TODO PS update path conditions
			}
			else if (s.invokesMethod())
			{
				//TODO get PS of the invoked method, then merge
			}
			else if (s.gotoJumps())
			{
				//TODO get next stmt from goto
			}
			else
			{
				s = allStmts.get(s.getStmtID()+1);
			}
		}
	}
	
	private void symbolicallyFinishingUp(boolean someFlag)
	{
		
	}

	private void init()
	{
		pathSummaries = new ArrayList<PathSummary>();
		toDoPathList = new ArrayList<ToDoPath>();
	}

	private boolean blacklistCheck(StaticMethod m) {
		StaticClass c = staticApp.findClassByDexName(m.getDeclaringClass());
		if (m == null || c == null)
			return false;
		return ( Blacklist.classInBlackList(c.getDexName()) 
				|| Blacklist.methodInBlackList(m.getSignature()));
	}
	
	private ArrayList<Expression> initSymbolicStates(StaticMethod targetM) {
		ArrayList<Expression> symbolicStates = new ArrayList<Expression>();
		int paramCount = targetM.getParamTypes().size();
		if (!targetM.isStatic())
			paramCount++;
		for (int i = 0; i < paramCount; i++) {
			Expression ex = new Expression("=");
			ex.add(new Expression("p" + i));
			if (!targetM.isStatic() && i == 0) {
				Expression thisEx = new Expression("$this");
				thisEx.add(new Expression(targetM.getDeclaringClass()));
				ex.add(thisEx);
			}
			else
				ex.add(new Expression("$parameter" + i));
			symbolicStates.add(ex);
		}
		return symbolicStates;
	}
	
}
