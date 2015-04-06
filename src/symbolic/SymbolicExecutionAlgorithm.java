package symbolic;


public class SymbolicExecutionAlgorithm {


/** ----- Main Algorithm
 * 
 * Components:
 * 	1. Symbolic Context: (Registers + Object Heap + Array Table)
 *  2. Static Information: (StaticApp, StaticMethod)
 *  3. ToDoPath
 *  4. PathSummary
 * 
 * Procedure:
 * 	SymbolicContext = $Input;
 * 	StaticInformation = $Input;
 * 	ToDoPath = $Input;
 * 	PathSummary pS = $Input;
 * 	StaticStmt s = StaticMethod.getFirstStmt();
 *  while (true)
 *  {
 *  	pS.addExecutionLog(stmtInfo);
 *  	s = staticMethod.getStmt(s.getID+1);
 *  	if (s.endsMethod())
 *  	{
 *  		// if returns register, mark that register
 *  		// end execution
 *  		break;
 *  	}
 *  	else if (s.invokesMethod())
 *  	{
 *  		// find the true method body
 *  		// recursively execute target method (same PS, same ToDoPath, new other inputs)
 *  		// merge the result symbolic context into main symbolic context ($Fstatic, $param)
 *  	}
 *  	else if (s.switchJumps())
 *  	{
 *  		// if there is mandatory choice in ToDoPath, follow that
 *  		// else choose flow through
 *  		// pS.addPathChoice()
 *  		// pS.updatePathConditions()
 *  		// build new ToDoPath only if this is a new choice
 *  	}
 *  	else if (s.ifJumps())
 *  	{
 *  		// if there is mandatory choice in ToDoPath, follow that
 *  		// else choose a direction that can avoid infinite loops:
 *  		// 	if pS.pathChoices contains current IfStmt, choose the opposite direction of the most recent choice
 *  		// build new ToDoPath only if the choice never happened in pS.PathChoices
 * 			pS.addPathChoice();
 * 			pS.updatePathConditions();
 *  	}
 *  	else if (s.updatesSymbolicStates())
 *  	{
 *  		//Different cases:
 *  		// 1) left is "v0".
 *  				v0 = v1/p1
 *  				v0 = $invokeResult
 *  				v0 = $number
 *  				v0 = $const-string/class
 *  				v0 = $instance-of
 *  				v0 = $new-instance
 *  				v0 = $array
 *  				v0 = $array-length
 *  				v0 = $aget
 *  				v0 = $aput (not assigning value to register)
 *  				v0 = $Finstance
 *  				v0 = $Fstatic
 *  				v0 = add/sub/mul/.../
 *  			a. Get right value;
 *  			b. Put into left register; (except aput)
 *  		// 2) left is "$Finstance"
 *  				$Finstance = v0
 *  			a. Get right value;
 *  			b. put into Object heap;
 *  		// 3) left is "$Fstatic"
 *  				$Fstatic = v0
 *  			a. Get right value;
 *  			b. put into Object heap;
 *  	}
 *  	else if (s.gotoJumps)
 *  	{
 *			s = StaticMethod.firstStmtOfBlock(s.targetBlockLabel());
 *  	}
 *  	s = nextStmt;
 *  }
 *  // after main method returns, pS.execLog and pS.pathCond are already updated
 *  // need to pick the symbolic context information to add into symbolicStates
 *  // 	$Fstatic in Object heap
 * 
 * */	
	
/** Special Cases of operation:
 * 		
 * 	$invokeResult: there should be a field in SymbolicContext to store most recent result
 * 	$number:	trimm down the whole tree and just return the decimal number
 * 	TODO: finish this
 * 
 * 
 * 
 * 
 * */
	
}
