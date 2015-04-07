package symbolic;

import java.util.ArrayList;
import java.util.Map;

import staticFamily.StaticApp;
import staticFamily.StaticClass;
import staticFamily.StaticMethod;
import staticFamily.StaticStmt;
import analysis.Utility;

public class SymbolicExecution {

	private StaticApp staticApp;
	public boolean debug = true;
	public boolean blackListOn = true;
	private ArrayList<PathSummary> pathSummaries;
	private ArrayList<ToDoPath> toDoPathList = new ArrayList<ToDoPath>();
	
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
			initPS.setMethodSignature(methodSignature);
			SymbolicContext symbolicContext = initSymbolicContext(entryMethod);
			symbolicExecution(initPS, entryMethod, toDoPath, symbolicContext, true);
			pathSummaries.add(initPS);
			symbolicallyFinishingUp(entryMethod);
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
				 SymbolicContext symbolicContext,
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
			int nextStmtID = s.getStmtID()+1;
			if (s.endsMethod())
			{
				if (!s.getvA().equals(""))
				{
					for (Register reg : symbolicContext.registers)
						if (reg.regName.equals(s.getvA()))
							reg.isReturnedVariable = true;
				}
				break;
			}
			else if (s.invokesMethod())
			{
				/**
				 *  This version didn't properly deal with inheritance
				 * 	and interfacing. In order to properly deal with it,
				 * 	need to:
				 * 	1. maintain a virtual method table when parsing classes.
				 *  So that when met a invoke stmt, check if the method sig
				 *  is the method body that will be invoked
				 * 	2. maintain a Type field for each register. So that when
				 *  met a invoke-interface, we will know which implementation
				 *  of the class to look for the method body.
				 *  Update(04/06/2015): Maybe the solution doesn't need to be that all-around.
				 *  Considering our situation, we are guaranteed to have the object type in the
				 *  symbolic states. If the method sig matches the object type, then good.
				 * */
				String targetSig = (String)s.getData();
				String p0Type = "";
				Expression invokeEx = s.getExpression();
				// find the type of p0
				if (s.getSmaliStmt().startsWith("invoke-static")
						|| s.getSmaliStmt().startsWith("invoke-super")
						|| s.getSmaliStmt().startsWith("invoke-direct"))
					p0Type = "";
				// only need to worry about invoke-virtual and invoke-interface
				else if (invokeEx.getChildCount() > 1)
				{
					Expression p0Ex = (Expression) invokeEx.getChildAt(1);
					Expression p0ValueEx = symbolicContext.findValueOf(p0Ex);
					if (p0ValueEx.getContent().equals("$new-instance")
							|| p0ValueEx.getContent().equals("$Fstatic")
							|| p0ValueEx.getContent().equals("$Finstance"))
					{
						Expression sigOrTypeEx = (Expression) p0ValueEx.getChildAt(0);
						p0Type = sigOrTypeEx.getContent();
						if (p0Type.contains(":"))
							p0Type = p0Type.substring(p0Type.indexOf(":")+1);
					}
				}
				//p0Type = "";
				StaticMethod targetM = staticApp.findDynamicDispatchedMethodBody(targetSig, p0Type);
				if (targetM != null && !targetM.isAbstract() && !(this.blackListOn && blacklistCheck(targetM)))
				{
					// First, initiate the subSymbolicContext by initiating
					// parameter registers(different from entry method)
					// and local registers(same as entry method),
					// then copy the outEx
					SymbolicContext subSymbolicContext = 
							initSymbolicContext(targetM, symbolicContext, s);
					// Then recursively symbolicExecute the invoked method,
					// update ExecutionLog and PathConditions into the same pS,
					// update symbolic states in the sub symbolic context
					// then merge into main context after the execution
					symbolicExecution(pS, targetM, toDoPath, subSymbolicContext, false);
					// Things to merge in:
					//  1. the return variable (from registers)
					//  3. the params that have fieldExs (from registers)
					//  2. $Fstatic = ... (from outExs), also put the $Finstance 
					//     into corresponding register
					for (Register subReg : subSymbolicContext.registers)
					{
						if (subReg.isReturnedVariable) // 1
						{
							Expression returnedEx = (Expression)subReg.ex.getChildAt(1);
							symbolicContext.recentInvokeResult = returnedEx.clone();
						}
						if (subReg.isParam && subReg.fieldExs.size()>0) // 2
						{ // find the original register, then update its fields
							Register originalReg = symbolicContext.findRegister(
									subReg.originalParamName);
							for (Expression newFieldEx : subReg.fieldExs)
								updateFieldEx(originalReg, newFieldEx);
						}
					}
					for (Expression subOutEx : subSymbolicContext.outExs) // 3
					{
						// If it contains $Fstatic, update
						// If it's $Finstance, but contains param's value, update
						Expression theLeft = (Expression) subOutEx.getChildAt(0);
						if (theLeft.contains("$Fstatic"))
						{
							symbolicContext.updateOutExs(subOutEx);
						}
						else
						{
							Expression objValue = (Expression) theLeft.getChildAt(1);
							for (Register reg : symbolicContext.registers)
							{
								if (!reg.isParam)
									continue;
								Expression regValue = (Expression) reg.ex.getChildAt(1);
								if (objValue.contains(regValue))
								{
									symbolicContext.updateOutExs(subOutEx);
									updateFieldEx(reg, subOutEx);
									break;
								}
							}
						}
					}
				}
				else
				{
					// TODO APIs?
					Expression tempResultEx = new Expression("$api");
					tempResultEx.add(new Expression(s.getSmaliStmt()));
					symbolicContext.recentInvokeResult = tempResultEx;
				}

			}
			else if (s.switchJumps())
			{
				String stmtInfo = className + ":" + s.getSourceLineNumber();
				String pastChoice = toDoPath.getAPastChoice();
				String choice = "";
				Expression vAEx = new Expression(s.getvA());
				Expression caseVariableEx = symbolicContext.findValueOf(vAEx);
				ArrayList<Integer> remainingValues = new ArrayList<Integer>();
				@SuppressWarnings("unchecked")
				Map<Integer, String> switchMap = (Map<Integer, String>) s.getData();
				// 1 If found a past choice: follow the choice, no need for new ToDoPath
				if (!pastChoice.equals(""))
				{
					choice = pastChoice;
				}
				// 2 If arrived the cross road: follow the assigned new direction, no need
				else if (toDoPath.getTargetPathStmtInfo().equals(stmtInfo))
				{
					choice = stmtInfo + "," + toDoPath.getNewDirection();
					toDoPath.setTargetPathStmtInfo("");
				}
				// 3 If already past cross road: choose the flowthrough direction
				// build ToDoPath for the rest of directions
				else
				{
					choice = stmtInfo + ",FlowThrough";
					remainingValues.addAll(switchMap.keySet());
					for (int remainingValue : remainingValues)
					{
						ToDoPath toDo = new ToDoPath();
						toDo.setPathChoices(pS.getPathChoices());
						toDo.setTargetPathStmtInfo(stmtInfo);
						toDo.setNewDirection(remainingValue+"");
						this.toDoPathList.add(toDo);
					}
				}
				pS.addPathChoice(choice);
				if (choice.endsWith("FlowThrough"))
				{
					for (int caseValue : switchMap.keySet())
					{
						Expression newCond = new Expression("/=");
						newCond.add(caseVariableEx);
						newCond.add(new Expression(caseValue + ""));
						pS.updatePathConditions(newCond);
					}
					nextStmtID = s.getStmtID()+1;
				}
				else
				{
					int chosenValue = Integer.parseInt(choice.substring(choice.indexOf(",")+1));
					Expression newCond = new Expression("=");
					newCond.add(caseVariableEx);
					newCond.add(new Expression(chosenValue+""));
					pS.updatePathConditions(newCond);
					String targetLabel = switchMap.get(chosenValue);
					nextStmtID = m.getFirstStmtOfBlock(targetLabel).getStmtID();
				}
			}
			else if (s.ifJumps())
			{
				String stmtInfo = className + ":" + s.getSourceLineNumber();
				String pastChoice = toDoPath.getAPastChoice();
				String choice = "", otherChoice = "";
				// 1 If found a past choice: follow the choice, no need for new ToDoPath
				if (!pastChoice.equals(""))
				{
					choice = pastChoice;
				}
				// 2 If arrived the cross road: follow the assigned new direction, no need
				else if (toDoPath.getTargetPathStmtInfo().equals(stmtInfo))
				{
					choice = stmtInfo + "," + toDoPath.getNewDirection();
					toDoPath.setTargetPathStmtInfo("");
				}
				// 3 If already past cross road: choose the flowthrough direction
				// build ToDoPath for the rest of directions
				else
				{
					/*To work around loops:
					 * 1. When making own decision, check pathChoices, if there are choices of that IfStmt, find the most recent one and go the opposite direction
					 * 2. When building ToDoPath for the newDirection, if the newDirection already happened before this, then do not build ToDoPath
					 * */
					choice = stmtInfo + ",FlowThrough";
					otherChoice = "Jump";
					boolean alreadyJumped = false, alreadyFlowed = false;
					for (String pC: pS.getPathChoices())
					{
						if (pC.equals(stmtInfo + ",Jump"))
						{
							alreadyJumped = true;
							choice = stmtInfo + ",FlowThrough";
							otherChoice = "Jump";
						}
						else if (pC.equals(stmtInfo + ",FlowThrough"))
						{
							alreadyFlowed = true;
							choice = stmtInfo + ",Jump";
							otherChoice = "FlowThrough";
						}
					}
					if ((otherChoice.equals("Jump") && !alreadyJumped)
						||(otherChoice.equals("FlowThrough") && !alreadyFlowed))
					{
						ToDoPath toDo = new ToDoPath();
						toDo.setPathChoices(pS.getPathChoices());
						toDo.setTargetPathStmtInfo(stmtInfo);
						toDo.setNewDirection(otherChoice);
						this.toDoPathList.add(toDo);
					}
				}
				pS.addPathChoice(choice);
				// update the cond expression
				Expression cond = s.getExpression().clone();
				Expression left = (Expression) cond.getChildAt(0);
				Expression right = (Expression) cond.getChildAt(1);
				Expression newLeft = symbolicContext.findValueOf(left);
				Expression newRight = symbolicContext.findValueOf(right);
				if (right.getContent().equals("0"))
					newRight = right;
				cond.removeAllChildren();
				cond.add(newLeft);
				cond.add(newRight);
				if (choice.endsWith(",Jump"))
				{
					pS.updatePathConditions(cond);
					String jumpTargetLabel = (String) s.getData();
					nextStmtID = m.getFirstStmtOfBlock(jumpTargetLabel).getStmtID();
				}
				else
				{
					pS.updatePathConditions(cond.getReverseCondition());
					nextStmtID = s.getStmtID()+1;
				}
			}
			else if (s.updatesSymbolicStates())
			{
				boolean updateRegs = true;
				Expression ex = s.getExpression().clone();
				Expression left = (Expression) ex.getChildAt(0);
				Expression right = (Expression) ex.getChildAt(1);
				String leftSymbol = left.getContent();
				String rightSymbol = right.getContent();
				/** perform the calculations according to rightSymbol,
				 *  then update the register */
				if (leftSymbol.startsWith("v"))
				{
					// direct assign vA = vB
					if (rightSymbol.startsWith("v") || rightSymbol.startsWith("p"))
					{
						Expression updatedRight = symbolicContext.findValueOf(right);
						ex.remove(1);
						ex.insert(updatedRight, 1);
					}
					// all kinds of complex stuff
					else if (rightSymbol.startsWith("$"))
					{
						if (rightSymbol.equals("$invokeResult"))
						{
							ex.remove(1);
							ex.insert(symbolicContext.recentInvokeResult.clone(), 1);
							symbolicContext.recentInvokeResult = null;
						}
						else if (rightSymbol.equals("$number"))
						{
							Expression decEx = (Expression) right.getChildAt(2);
							ex.remove(1);
							ex.insert(new Expression(decEx.getContent()), 1);
						}
						else if (rightSymbol.equals("$const-string"))
						{}
						else if (rightSymbol.equals("$const-class"))
						{}
						else if (rightSymbol.equals("$instance-of"))
						{}
						else if (rightSymbol.equals("$new-instance"))
						{}
						/** solve length, and any elements */
						else if (rightSymbol.equals("$array"))
						{
							Expression arrayEx = (Expression) ex.getChildAt(1);
							Expression lengthEx = (Expression) arrayEx.getChildAt(0);
							String lengthSymbol = lengthEx.getContent();
							if (lengthSymbol.startsWith("v")||lengthSymbol.startsWith("p"))
							{
								Expression newLengthEx = symbolicContext.findValueOf(lengthEx);
								arrayEx.remove(0);
								arrayEx.insert(newLengthEx, 0);
							}
							if (arrayEx.getChildCount() > 2)
							{
								for (int i = 2; i < arrayEx.getChildCount(); i++)
								{
									// update the value of index and value of each element node
									Expression elementEx = (Expression) arrayEx.getChildAt(i);
									Expression indexEx = (Expression) elementEx.getChildAt(0);
									Expression valueEx = (Expression) elementEx.getChildAt(1);
									//String indexSymbol = indexEx.getContent();
									//String valueSymbol = valueEx.getContent();
									//if (indexSymbol.startsWith("v")||indexSymbol.startsWith("p"))
									//{
									Expression newIndexEx = symbolicContext.findValueOf(indexEx);
									elementEx.remove(0);
									elementEx.insert(newIndexEx, 0);
									//}
									//if (valueSymbol.startsWith("v")||valueSymbol.startsWith("p"))
									//{
									Expression newValueEx = symbolicContext.findValueOf(valueEx);
									elementEx.remove(1);
									elementEx.insert(newValueEx, 1);
									//}
								}
							}
							ArrayForYices aFY = new ArrayForYices(ex);
							int index = -1;
							for (int i = 0; i < symbolicContext.arrays.size(); i++)
							{
								if (symbolicContext.arrays.get(i).name.equals(aFY.name))
								{
									index = i;
									break;
								}
							}
							if (index > -1)
								symbolicContext.arrays.remove(index);
							symbolicContext.arrays.add(aFY);
						}
						else if (rightSymbol.equals("$array-length"))
						{
							String arrayName = s.getvB();
							Register reg = symbolicContext.findRegister(arrayName);
							Expression arrayEx = (Expression) reg.ex.getChildAt(1);
							Expression lengthEx = (Expression) arrayEx.getChildAt(0);
							ex.remove(1);
							ex.insert(lengthEx.clone(), 1);
						}
						/**
						 * aget and aput might run into trouble if the
						 * index variable is not a constant... 
						 * 
						 * Update(04/05/2015): Using (update) in Yices to represent arrays
						 * The idea is to keep all assignment history recursively in one Yices statement 
						 *  */
						else if (rightSymbol.equals("$aget"))
						{
							String arrayName = s.getvB();
							String indexS = s.getvC();
							Register targetIndexReg = symbolicContext.findRegister(indexS);
							Expression targetIndexEx = (Expression) targetIndexReg.ex.getChildAt(1);
/*							try // if the target index is not directly a number, we can't solve it
							{
								Integer targetIndex = Integer.parseInt(targetIndexEx.getContent());
								if (arrayEx.getChildCount() > 2)
								{
									for (int i = 2; i < arrayEx.getChildCount(); i++)
									{
										Expression elementEx = (Expression) arrayEx.getChildAt(i);
										Expression indexEx = (Expression) elementEx.getChildAt(0);
										Expression valueEx = (Expression) elementEx.getChildAt(1);
										String indexSymbol = indexEx.getContent();
										Integer thisIndex = Integer.parseInt(indexSymbol);
										if (thisIndex == targetIndex)
										{
											ex.remove(1);
											ex.insert(valueEx.clone(), 1);
											break;
										}
									}
								}
							}
							catch (Exception e)
							{
								System.out.println("Can't solve aget:");
								System.out.println("  " + s.getSmaliStmt());
								System.out.println("  array: " + arrayEx.toYicesStatement());
								System.out.println("  index: " + targetIndexEx.toYicesStatement());
							}*/
							for (ArrayForYices aFY : symbolicContext.arrays)
							{
								if (aFY.name.equals(arrayName))
								{
									ex.remove(1);
									ex.insert(aFY.aget(targetIndexEx), 1);
									break;
								}
							}
						}
						else if (rightSymbol.equals("$aput"))
						{
							updateRegs = false;
							String arrayName = s.getvB();
							String indexS = s.getvC();
							Register targetIndexReg = symbolicContext.findRegister(indexS);
							Expression targetIndexEx = (Expression) targetIndexReg.ex.getChildAt(1);
/*							try // if the target index is not directly a number, we can't solve it
							{
								Integer targetIndex = Integer.parseInt(targetIndexEx.getContent());
								if (arrayEx.getChildCount() > 2)
								{
									for (int i = 2; i < arrayEx.getChildCount(); i++)
									{
										Expression elementEx = (Expression) arrayEx.getChildAt(i);
										Expression indexEx = (Expression) elementEx.getChildAt(0);
										String indexSymbol = indexEx.getContent();
										Integer thisIndex = Integer.parseInt(indexSymbol);
										if (thisIndex == targetIndex)
										{
											Expression newElementEx = symbolicContext.findValueOf(left);
											elementEx.remove(1);
											elementEx.insert(newElementEx, 1);
											break;
										}
									}
								}
							}
							catch (Exception e)
							{
								System.out.println("Can't solve aput:");
								System.out.println("  " + s.getSmaliStmt());
								System.out.println("  array: " + arrayEx.toYicesStatement());
								System.out.println("  index: " + targetIndexEx.toYicesStatement());
							}*/
							for (ArrayForYices aFY : symbolicContext.arrays)
							{
								if (aFY.name.equals(arrayName))
								{
									aFY.aput(targetIndexEx, symbolicContext.findValueOf(left));
									break;
								}
							}
						}
						/** update the object variable, then if the same field was assigned
						 *  previously, copy that value
						 *  */
						else if (rightSymbol.equals("$Finstance"))
						{
							Expression fieldSigEx = (Expression) right.getChildAt(0);
							Expression objEx = (Expression) right.getChildAt(1);
							Register objReg = symbolicContext.findRegister(objEx.getContent());
							Expression newObjEx = symbolicContext.findValueOf(objEx);
							right.remove(1);
							right.insert(newObjEx, 1);
							for (Expression fieldEx : objReg.fieldExs)
							{
								Expression thisFieldSigEx =(Expression)((Expression) fieldEx.getChildAt(0)).getChildAt(0);
								if (fieldSigEx.equals(thisFieldSigEx))
								{
									Expression newRight = ((Expression) fieldEx.getChildAt(1)).clone();
									ex.remove(1);
									ex.insert(newRight, 1);
									break;
								}
							}
						}
						/** see if the field was previously assigned, copy that value
						 *  otherwise, no changes
						 * */
						else if (rightSymbol.equals("$Fstatic"))
						{
							//System.out.println("[1]"+ex.toYicesStatement());
							for (Expression outEx : symbolicContext.outExs)
							{
								Expression fieldEx = (Expression) outEx.getChildAt(0);
								if (fieldEx.equals(right))
								{
									//System.out.println("[2]"+outEx.toYicesStatement());
									Expression thisValue = ((Expression) outEx.getChildAt(1)).clone();
									ex.remove(1);
									ex.insert(thisValue, 1);
									//System.out.println("[3]" + ex.toYicesStatement());
									break;
								}
							}
						}
					}
					/** operations (add sub mul div neg ...)
					    vA = vB op vC  or vA = op vB
					 **/
					else
					{
						for (int i = 0; i < right.getChildCount(); i++)
						{
							Expression child = (Expression) right.getChildAt(i);
							Expression updatedChild = symbolicContext.findValueOf(child);
							right.remove(i);
							right.insert(updatedChild, i);
						}
					}
					/** put it in corresponding register.
					   (Note): There might be errors here when:
					   Before updating, v1 stores a field member of a param
					   This update writes a new stuff into v1, which erases old content
					   In the end, only outEx will be reported, and the field
					   operation that were erased earlier is gone before reporting.
					   (Solution):
					   when handling $Finstance = ... (next section), update current
					   register and the paramRegister. So no extra action
					   needed here.
					*/
					if (updateRegs) // no need for aput
					{
						for (Register reg : symbolicContext.registers)
						{
							if (reg.regName.equals(leftSymbol))
							{
								reg.ex = ex;
								reg.fieldExs.clear();
								break;
							}
						}
					}
				}
				/** rightSymbol is a variable, update rightEx,
				 *  then put it to:
				 *   corresponding register's fieldEx
				 *   if ( left contains $Fstatic
				 *     or not a param reg but left contains the value of one of the params)
				 *   put it in outEx
				 *   */
				else if (leftSymbol.equals("$Finstance"))
				{
					// 1. update right
					Expression updatedRight = symbolicContext.findValueOf(right);
					ex.remove(1);
					ex.insert(updatedRight, 1);
					// 2. add to reg's fieldExs
					String objName = ((Expression)left.getChildAt(1)).getContent();
					Register objReg = symbolicContext.findRegister(objName);
					updateFieldEx(objReg, ex);
					// 3. maybe add it to outEx
					Expression newEx = ex.clone();
					left = (Expression) newEx.getChildAt(0);
					Expression objValue = ((Expression) objReg.ex.getChildAt(1)).clone();
					if (objReg.isParam)
					{
						left.remove(1);
						left.insert(objValue, 1);
						symbolicContext.updateOutExs(newEx);
					}
					else
					{
						if (objValue.contains("$Fstatic"))
						{
							left.remove(1);
							left.insert(objValue, 1);
							symbolicContext.updateOutExs(newEx);
						}
						else
						{
							for (Register reg : symbolicContext.registers)
							{
								if (!reg.isParam)
									continue;
								Expression regValue = (Expression) reg.ex.getChildAt(1);
								if (objValue.contains(regValue))
								{
									left.remove(1);
									left.insert(objValue, 1);
									symbolicContext.updateOutExs(newEx);
									break;
								}
							}
						}
					}
				}
				/** rightsymbol is a variable, update rightEx,
				 *  then put the ex to outEx
				 * */
				else if (leftSymbol.equals("$Fstatic"))
				{
					Expression updatedRight = symbolicContext.findValueOf(right);
					ex.remove(1);
					ex.insert(updatedRight, 1);
					symbolicContext.updateOutExs(ex);
				}
			}
			else if (s.gotoJumps())
			{
				String targetLabel = (String) s.getData();
				nextStmtID = m.getFirstStmtOfBlock(targetLabel).getStmtID();
			}
			s = allStmts.get(nextStmtID);
		}
		if (inMainMethod)
		//if (true)
		{
			pS.setSymbolicStates(symbolicContext.outExs);
			if (this.debug)
			{
				System.out.println("======================== symbolic context at end of " + m.getSignature());
				symbolicContext.printAll();
				System.out.println("======================== path summary");
				System.out.println("\n Execution Log: ");
				for (String execL : pS.getExecutionLog())
					System.out.println("  " + execL);
				System.out.println("\n Symbolic States: ");
				for (Expression o : pS.getSymbolicStates())
					System.out.println("  " + o.toYicesStatement());
				System.out.println("\n PathCondition: ");
				for (Expression cond : pS.getPathConditions())
					System.out.println("  " + cond.toYicesStatement());
				System.out.println("\n PathChoices: ");
				for (String pC : pS.getPathChoices())
					System.out.println("  " + pC);
				System.out.println("========================");
			}
		}
	}
	


	private void updateFieldEx(Register reg, Expression newFieldEx) {
		Expression left = (Expression) newFieldEx.getChildAt(0);
		String fieldSig = ((Expression) left.getChildAt(0)).getContent();
		for (Expression fieldEx : reg.fieldExs)
		{
			Expression thisLeft = (Expression) fieldEx.getChildAt(0);
			String thisFieldSig = ((Expression) thisLeft.getChildAt(0)).getContent();
			if (thisFieldSig.equals(fieldSig))
			{
				Expression newRight = (Expression) newFieldEx.getChildAt(1);
				fieldEx.remove(1);
				fieldEx.insert(newRight.clone(), 1);
				return;
			}
		}
		reg.fieldExs.add(newFieldEx.clone());
	}

	private void symbolicallyFinishingUp(StaticMethod entryMethod) throws Exception
	{
		int counter = 1;
		while (!toDoPathList.isEmpty())
		{
			System.out.println("[Symbolic Execution No." + counter++ + "] " + entryMethod.getSignature());
			ToDoPath toDoPath = toDoPathList.remove(toDoPathList.size()-1);
			PathSummary anotherPS = new PathSummary();
			//anotherPS.setSymbolicStates(this.initSymbolicStates(entryMethod));
			anotherPS.setMethodSignature(entryMethod.getSignature());
			SymbolicContext symbolicContext = initSymbolicContext(entryMethod);
			symbolicExecution(anotherPS, entryMethod, toDoPath, symbolicContext, true);
			pathSummaries.add(anotherPS);
		}
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
	
	private SymbolicContext initSymbolicContext(
									StaticMethod targetM,
									SymbolicContext symbolicContext,
									StaticStmt s) 
	{
		SymbolicContext subSymbolicContext = new SymbolicContext();
		// 1. The parameters, instead of 'p0 = $this, p1 = $p1, ...',
		// we look at what variables are feed into the invoke statement,
		// and copy the corresponding register to sub context (including
		//  both the assignment and the field assignments if any)
		Expression invokeEx = s.getExpression();
		if (invokeEx.getChildCount() > 1)
		{
			for (int i = 0; i < invokeEx.getChildCount()-1; i++)
			{
				Register paramReg = new Register();
				Expression paramEx = new Expression("=");
				paramEx.add(new Expression("p"+i));
				String inputParamName = 
						((Expression)invokeEx.getChildAt(i+1)).getContent();
				Register theReg = symbolicContext.findRegister(inputParamName);
				if (theReg.ex != null)
				{
					Expression toPutIn = (Expression) theReg.ex.getChildAt(1);
					paramEx.add(toPutIn.clone());
					for (Expression fieldEx : theReg.fieldExs)
						paramReg.fieldExs.add(fieldEx.clone());
					paramReg.originalParamName = inputParamName;
				}
				paramReg.regName = "p" + i;
				paramReg.ex = paramEx;
				paramReg.isParam = true;
				subSymbolicContext.registers.add(paramReg);
			}
		}
		// Then initiate local variables
		for (int i = 0; i < targetM.getLocalVariableCount(); i++)
		{
			Register localReg = new Register();
			localReg.regName = "v" + i;
			subSymbolicContext.registers.add(localReg);
		}
		// Then copy the out exs
		for (Expression thisOutEx : symbolicContext.outExs)
		{
			subSymbolicContext.outExs.add(thisOutEx.clone());
		}
		return subSymbolicContext;
	}
	
	private SymbolicContext initSymbolicContext(StaticMethod entryMethod) {
		SymbolicContext symbolicContext = new SymbolicContext();
		// First initiate parameters
		int paramIndex = 0;
		for (String paramType : entryMethod.getParamTypes())
		{
			Expression paramEx = new Expression("=");
			paramEx.add(new Expression("p"+paramIndex));
			if (paramIndex == 0 && !entryMethod.isStatic())
				paramEx.add(new Expression("$this"));
			else
				paramEx.add(new Expression("$p" + paramIndex));
			Register paramReg = new Register();
			paramReg.regName = "p" + paramIndex;
			paramReg.ex = paramEx;
			paramReg.isParam = true;
			symbolicContext.registers.add(paramReg);
			paramIndex++;
			if (paramType.equals("long") || paramType.equals("double"))
				paramIndex++;
		}
		// Then initiate local variables
		for (int i = 0; i < entryMethod.getLocalVariableCount(); i++)
		{
			Register localReg = new Register();
			localReg.regName = "v" + i;
			symbolicContext.registers.add(localReg);
		}
		return symbolicContext;
	}
	
	private class SymbolicContext {
		/** registers, including local variables and parameter references */
		ArrayList<Register> registers = new ArrayList<Register>();
		/** the expressions that might changes global context ($Fstatic) */
		ArrayList<Expression> outExs = new ArrayList<Expression>();
		/** the most recent method invocation result */
		Expression recentInvokeResult = null;
		/** store all the array assignments of each array into one Expression */
		ArrayList<ArrayForYices> arrays = new ArrayList<ArrayForYices>();
		public Expression findValueOf(Expression theLeft)
		{
			if (theLeft.getContent().equals("$number"))
			{
				Expression decEx = (Expression) theLeft.getChildAt(2);
				return decEx.clone();
			}
			for (Register reg : registers)
			{
				if (reg.regName.equals(theLeft.getContent()))
				{
					Expression value = (Expression) reg.ex.getChildAt(1);
					return value.clone();
				}
			}
			return theLeft;
		}
		public void printAll()
		{
			for (Register reg : registers)
			{
				System.out.println("[" + reg.regName + "]");
				if (reg.ex != null)
					System.out.println("ex: " + reg.ex.toYicesStatement());
				if (!reg.fieldExs.isEmpty())
				{
					System.out.println("fields: ");
					for (Expression fieldEx : reg.fieldExs)
						System.out.println("  " + fieldEx.toYicesStatement());
				}
			}
			for (Expression outEx : outExs)
			{
				System.out.println("[outEx]" + outEx.toYicesStatement());
			}
		}
		public Register findRegister(String name)
		{
			for (Register reg : registers)
				if (reg.regName.equals(name))
					return reg;
			return null;
		}
		public void updateOutExs(Expression newOutEx)
		{
			Expression newLeft = (Expression) newOutEx.getChildAt(0);
			for (Expression thisOutEx : outExs)
			{
				Expression thisLeft = (Expression) thisOutEx.getChildAt(0);
				if (thisLeft.equals(newLeft))
				{
					Expression newRight = (Expression) newOutEx.getChildAt(1);
					thisOutEx.remove(1);
					thisOutEx.insert(newRight.clone(), 1);
					return;
				}
			}
			outExs.add(newOutEx.clone());
		}
	}
	
	private class Register {
		String regName = "";
		String type = "";
		Expression ex = null;
		ArrayList<Expression> fieldExs = new ArrayList<Expression>();
		//boolean isFirstHalfOfWide = false;
		//boolean isSecondHalfOfWide = false;
		boolean isReturnedVariable = false;
		boolean isParam = false;
		String originalParamName = "";
	}

	
}
