/**
 * IBM Confidential
 * OCO Source Materials
 * (C) Copyright IBM Corp. 2010, 2013
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.ibm.bi.dml.parser;

import java.util.HashMap;

import com.ibm.bi.dml.parser.Expression.DataOp;
import com.ibm.bi.dml.parser.DataExpression;


public class InputStatement extends IOStatement
{
	@SuppressWarnings("unused")
	private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp. 2010, 2013\n" +
                                             "US Government Users Restricted Rights - Use, duplication  disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";
		
	public static final String[] READ_VALID_MTD_PARAM_NAMES = 
		{ IO_FILENAME, READROWPARAM, READCOLPARAM, READNUMNONZEROPARAM, FORMAT_TYPE, 
			ROWBLOCKCOUNTPARAM, COLUMNBLOCKCOUNTPARAM, DATATYPEPARAM, VALUETYPEPARAM, DESCRIPTIONPARAM }; 
	
	public static final String[] READ_VALID_PARAM_NAMES = 
	{	IO_FILENAME, READROWPARAM, READCOLPARAM, FORMAT_TYPE, DATATYPEPARAM, VALUETYPEPARAM,
		DELIM_FILL_VALUE, DELIM_DELIMITER, DELIM_FILL, DELIM_HAS_HEADER_ROW }; 
    	
	public InputStatement(){
		super();
	}
	
	public InputStatement(DataOp op){
		super (op);
	}
	
	public static boolean isValidParamName(String key, boolean isMTDFile){
		
		if (isMTDFile){
			for (String paramName : READ_VALID_MTD_PARAM_NAMES){
				if (paramName.equals(key)){
					return true;
				}
			}
		}
		else {
			for (String paramName : READ_VALID_PARAM_NAMES){
				if (paramName.equals(key)){
					return true;
				}
			}
		}
		return false;
	}
	
	// rewrites statement to support function inlining (creates deep copy)
	public Statement rewriteStatement(String prefix) throws LanguageException {
		
		InputStatement newStatement = new InputStatement();
		newStatement._beginLine 	= this.getBeginLine();
		newStatement._beginColumn 	= this.getBeginColumn();
		newStatement._endLine 		= this.getEndLine();
		newStatement._endColumn     = this.getEndColumn();
			
		// rewrite target variable name (creates deep copy)
		newStatement._id = (DataIdentifier)this._id.rewriteExpression(prefix);
	
		// rewrite InputStatement expr parameters (creates deep copies)
		DataOp op = _paramsExpr.getOpCode();
		HashMap<String,Expression> newExprParams = new HashMap<String,Expression>();
		for (String key : _paramsExpr.getVarParams().keySet()){
			Expression newExpr = _paramsExpr.getVarParam(key).rewriteExpression(prefix);
			newExprParams.put(key, newExpr);
		}	

		DataExpression newParamerizedExpr = new DataExpression(op, newExprParams);
		newParamerizedExpr.setAllPositions(this.getBeginLine(), this.getBeginColumn(), this.getEndLine(), this.getEndColumn());
		newStatement.setExprParams(newParamerizedExpr);
		return newStatement;
			
	}

	
	public void initializeforwardLV(VariableSet activeIn){}
	public VariableSet initializebackwardLV(VariableSet lo){
		return lo;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		 sb.append(_id.toString() + " = " + Statement.INPUTSTATEMENT + " ( " );
		 sb.append(_paramsExpr.getVarParam(IO_FILENAME));
		 for (String key : _paramsExpr.getVarParams().keySet()){
			 if (key.equals(IO_FILENAME))
				 sb.append(", " + key + "=" + _paramsExpr.getVarParam(key).toString());
		 }
		 sb.append(" );"); 
		 return sb.toString(); 
	}
	
	@Override
	public VariableSet variablesRead() {
		VariableSet result = new VariableSet();
		
		// add variables read by parameter expressions
		for (String key : _paramsExpr.getVarParams().keySet())	
			result.addVariables(_paramsExpr.getVarParam(key).variablesRead()) ;
		
		// for LHS IndexedIdentifier, add variables for indexing expressions
		if (_id instanceof IndexedIdentifier) {
			IndexedIdentifier target = (IndexedIdentifier) _id;
			result.addVariables(target.variablesRead());
		}
		return result;
	}

	@Override
	public VariableSet variablesUpdated() {
		VariableSet result = new VariableSet();
		
		// add variable being populated by InputStatement
		result.addVariable(_id.getName(),_id);
	 	return result;
	}
}