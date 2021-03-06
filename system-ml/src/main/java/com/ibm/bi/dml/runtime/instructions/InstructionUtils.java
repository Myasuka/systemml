/**
 * (C) Copyright IBM Corp. 2010, 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.ibm.bi.dml.runtime.instructions;

import java.util.StringTokenizer;

import com.ibm.bi.dml.lops.AppendM;
import com.ibm.bi.dml.lops.BinaryM;
import com.ibm.bi.dml.lops.MapMult;
import com.ibm.bi.dml.lops.MapMultChain;
import com.ibm.bi.dml.lops.PMMJ;
import com.ibm.bi.dml.lops.PartialAggregate.CorrectionLocationType;
import com.ibm.bi.dml.lops.UAggOuterChain;
import com.ibm.bi.dml.lops.WeightedCrossEntropy;
import com.ibm.bi.dml.lops.WeightedCrossEntropyR;
import com.ibm.bi.dml.lops.WeightedDivMM;
import com.ibm.bi.dml.lops.WeightedDivMMR;
import com.ibm.bi.dml.lops.WeightedSigmoid;
import com.ibm.bi.dml.lops.WeightedSigmoidR;
import com.ibm.bi.dml.lops.WeightedSquaredLoss;
import com.ibm.bi.dml.lops.WeightedSquaredLossR;
import com.ibm.bi.dml.runtime.DMLRuntimeException;
import com.ibm.bi.dml.runtime.DMLUnsupportedOperationException;
import com.ibm.bi.dml.runtime.functionobjects.And;
import com.ibm.bi.dml.runtime.functionobjects.Builtin;
import com.ibm.bi.dml.runtime.functionobjects.Divide;
import com.ibm.bi.dml.runtime.functionobjects.Equals;
import com.ibm.bi.dml.runtime.functionobjects.GreaterThan;
import com.ibm.bi.dml.runtime.functionobjects.GreaterThanEquals;
import com.ibm.bi.dml.runtime.functionobjects.IntegerDivide;
import com.ibm.bi.dml.runtime.functionobjects.KahanPlus;
import com.ibm.bi.dml.runtime.functionobjects.KahanPlusSq;
import com.ibm.bi.dml.runtime.functionobjects.LessThan;
import com.ibm.bi.dml.runtime.functionobjects.LessThanEquals;
import com.ibm.bi.dml.runtime.functionobjects.Mean;
import com.ibm.bi.dml.runtime.functionobjects.Minus;
import com.ibm.bi.dml.runtime.functionobjects.Minus1Multiply;
import com.ibm.bi.dml.runtime.functionobjects.MinusNz;
import com.ibm.bi.dml.runtime.functionobjects.Modulus;
import com.ibm.bi.dml.runtime.functionobjects.Multiply;
import com.ibm.bi.dml.runtime.functionobjects.Multiply2;
import com.ibm.bi.dml.runtime.functionobjects.NotEquals;
import com.ibm.bi.dml.runtime.functionobjects.Or;
import com.ibm.bi.dml.runtime.functionobjects.Plus;
import com.ibm.bi.dml.runtime.functionobjects.Power;
import com.ibm.bi.dml.runtime.functionobjects.Power2;
import com.ibm.bi.dml.runtime.functionobjects.ReduceAll;
import com.ibm.bi.dml.runtime.functionobjects.ReduceCol;
import com.ibm.bi.dml.runtime.functionobjects.ReduceDiag;
import com.ibm.bi.dml.runtime.functionobjects.ReduceRow;
import com.ibm.bi.dml.runtime.instructions.cp.CPInstruction.CPINSTRUCTION_TYPE;
import com.ibm.bi.dml.runtime.instructions.mr.MRInstruction.MRINSTRUCTION_TYPE;
import com.ibm.bi.dml.runtime.instructions.spark.SPInstruction.SPINSTRUCTION_TYPE;
import com.ibm.bi.dml.runtime.matrix.operators.AggregateOperator;
import com.ibm.bi.dml.runtime.matrix.operators.AggregateUnaryOperator;
import com.ibm.bi.dml.runtime.matrix.operators.BinaryOperator;
import com.ibm.bi.dml.runtime.matrix.operators.LeftScalarOperator;
import com.ibm.bi.dml.runtime.matrix.operators.RightScalarOperator;
import com.ibm.bi.dml.runtime.matrix.operators.ScalarOperator;


public class InstructionUtils 
{
	
	/**
	 * 
	 * @param str
	 * @param expected
	 * @return
	 * @throws DMLRuntimeException
	 */
	public static int checkNumFields( String str, int expected ) 
		throws DMLRuntimeException 
	{
		//note: split required for empty tokens
		int numParts = str.split(Instruction.OPERAND_DELIM).length;
		int numFields = numParts - 2; // -2 accounts for execType and opcode
		
		if ( numFields != expected ) 
			throw new DMLRuntimeException("checkNumFields() for (" + str + ") -- expected number (" + expected + ") != is not equal to actual number (" + numFields + ").");
		
		return numFields; 
	}
	
	/**
	 * 
	 * @param str
	 * @param expected
	 * @return
	 * @throws DMLRuntimeException
	 */
	public static int checkNumFields( String[] parts, int expected ) 
		throws DMLRuntimeException 
	{
		int numParts = parts.length;
		int numFields = numParts - 1; //account for opcode
		
		if ( numFields != expected ) 
			throw new DMLRuntimeException("checkNumFields() -- expected number (" + expected + ") != is not equal to actual number (" + numFields + ").");
		
		return numFields; 
	}
	
	/**
	 * 
	 * @param parts
	 * @param expected1
	 * @param expected2
	 * @return
	 * @throws DMLRuntimeException
	 */
	public static int checkNumFields( String[] parts, int expected1, int expected2 ) 
		throws DMLRuntimeException 
	{
		int numParts = parts.length;
		int numFields = numParts - 1; //account for opcode
		
		if ( numFields != expected1 && numFields != expected2 ) 
			throw new DMLRuntimeException("checkNumFields() -- expected number (" + expected1 + " or "+ expected2 +") != is not equal to actual number (" + numFields + ").");
		
		return numFields; 
	}
	
	/**
	 * 
	 * @param str
	 * @param expected1
	 * @param expected2
	 * @return
	 * @throws DMLRuntimeException
	 */
	public static int checkNumFields( String str, int expected1, int expected2 ) 
		throws DMLRuntimeException 
	{
		//note: split required for empty tokens
		int numParts = str.split(Instruction.OPERAND_DELIM).length;
		int numFields = numParts - 2; // -2 accounts for execType and opcode
		
		if ( numFields != expected1 && numFields != expected2 ) 
			throw new DMLRuntimeException("checkNumFields() for (" + str + ") -- expected number (" + expected1 + " or "+ expected2 +") != is not equal to actual number (" + numFields + ").");
		
		return numFields; 
	}
	
	/**
	 * Given an instruction string, strip-off the execution type and return 
	 * opcode and all input/output operands WITHOUT their data/value type. 
	 * i.e., ret.length = parts.length-1 (-1 for execution type)
	 * 
	 * @param str
	 * @return 
	 */
	public static String[] getInstructionParts( String str ) 
	{
		StringTokenizer st = new StringTokenizer( str, Instruction.OPERAND_DELIM );
		String[] ret = new String[st.countTokens()-1];
		st.nextToken(); // stripping-off the exectype
		ret[0] = st.nextToken(); // opcode
		int index = 1;
		while( st.hasMoreTokens() ){
			String tmp = st.nextToken();
			int ix = tmp.indexOf(Instruction.DATATYPE_PREFIX);
			ret[index++] = tmp.substring(0,((ix>=0)?ix:tmp.length()));	
		}
		return ret;
	}
	
	/**
	 * Given an instruction string, this function strips-off the 
	 * execution type (CP or MR) and returns the remaining parts, 
	 * which include the opcode as well as the input and output operands.
	 * Each returned part will have the datatype and valuetype associated
	 * with the operand.
	 * 
	 * This function is invoked mainly for parsing CPInstructions.
	 * 
	 * @param str
	 * @return
	 */
	public static String[] getInstructionPartsWithValueType( String str ) 
	{
		//note: split required for empty tokens
		String[] parts = str.split(Instruction.OPERAND_DELIM, -1);
		String[] ret = new String[parts.length-1]; // stripping-off the exectype
		ret[0] = parts[1]; // opcode
		for( int i=1; i<parts.length; i++ )
			ret[i-1] = parts[i];
		
		return ret;
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String getOpCode( String str ) 
	{
		int ix1 = str.indexOf(Instruction.OPERAND_DELIM);
		int ix2 = str.indexOf(Instruction.OPERAND_DELIM, ix1+1);
		return str.substring(ix1+1, ix2);
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 * @throws DMLUnsupportedOperationException
	 */
	public static MRINSTRUCTION_TYPE getMRType( String str ) 
		throws DMLUnsupportedOperationException 
	{
		String opcode = getOpCode(str);
		MRINSTRUCTION_TYPE mrtype = MRInstructionParser.String2MRInstructionType.get( opcode ); 
		return mrtype;
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 * @throws DMLUnsupportedOperationException
	 */
	public static SPINSTRUCTION_TYPE getSPType( String str ) 
		throws DMLUnsupportedOperationException 
	{
		String opcode = getOpCode(str);
		SPINSTRUCTION_TYPE sptype = SPInstructionParser.String2SPInstructionType.get( opcode ); 
		return sptype;
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 * @throws DMLUnsupportedOperationException
	 */
	public static CPINSTRUCTION_TYPE getCPType( String str ) 
		throws DMLUnsupportedOperationException 
	{
		String opcode = getOpCode(str);
		CPINSTRUCTION_TYPE cptype = CPInstructionParser.String2CPInstructionType.get( opcode ); 
		return cptype;
	}
	
	/**
	 * 
	 * @param opcode
	 * @return
	 */
	public static boolean isBuiltinFunction ( String opcode ) 
	{
		Builtin.BuiltinFunctionCode bfc = Builtin.String2BuiltinFunctionCode.get(opcode);
		return (bfc != null);
	}
	
	public static boolean isOperand(String str) 
	{
		//note: split required for empty tokens
		String[] parts = str.split(Instruction.DATATYPE_PREFIX);
		return (parts.length > 1);
	}
	
	/**
	 * Evaluates if at least one instruction of the given instruction set
	 * used the distributed cache; this call can also be used for individual
	 * instructions. 
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isDistributedCacheUsed(String str) 
	{	
		String[] parts = str.split(Instruction.INSTRUCTION_DELIM);
		for(String inst : parts) 
		{
			String opcode = getOpCode(inst);
			if(  opcode.equalsIgnoreCase(AppendM.OPCODE)  
			   || opcode.equalsIgnoreCase(MapMult.OPCODE)
			   || opcode.equalsIgnoreCase(MapMultChain.OPCODE)
			   || opcode.equalsIgnoreCase(PMMJ.OPCODE)
			   || opcode.equalsIgnoreCase(UAggOuterChain.OPCODE)
			   || isDistQuaternaryOpcode( opcode ) //multiple quaternary opcodes
			   || BinaryM.isOpcode( opcode ) ) //multiple binary opcodes	
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param opcode
	 * @return
	 */
	public static AggregateUnaryOperator parseBasicAggregateUnaryOperator(String opcode)
	{
		AggregateUnaryOperator aggun = null;
		
		if ( opcode.equalsIgnoreCase("uak+") ) {
			AggregateOperator agg = new AggregateOperator(0, KahanPlus.getKahanPlusFnObject(), true, CorrectionLocationType.LASTCOLUMN);
			aggun = new AggregateUnaryOperator(agg, ReduceAll.getReduceAllFnObject());
		} 		
		else if ( opcode.equalsIgnoreCase("uark+") ) {
			// RowSums
			AggregateOperator agg = new AggregateOperator(0, KahanPlus.getKahanPlusFnObject(), true, CorrectionLocationType.LASTCOLUMN);
			aggun = new AggregateUnaryOperator(agg, ReduceCol.getReduceColFnObject());
		} 
		else if ( opcode.equalsIgnoreCase("uack+") ) {
			// ColSums
			AggregateOperator agg = new AggregateOperator(0, KahanPlus.getKahanPlusFnObject(), true, CorrectionLocationType.LASTROW);
			aggun = new AggregateUnaryOperator(agg, ReduceRow.getReduceRowFnObject());
		}
		else if ( opcode.equalsIgnoreCase("uasqk+") ) {
			AggregateOperator agg = new AggregateOperator(0, KahanPlusSq.getKahanPlusSqFnObject(), true, CorrectionLocationType.LASTCOLUMN);
			aggun = new AggregateUnaryOperator(agg, ReduceAll.getReduceAllFnObject());
		}
		else if ( opcode.equalsIgnoreCase("uarsqk+") ) {
			// RowSums
			AggregateOperator agg = new AggregateOperator(0, KahanPlusSq.getKahanPlusSqFnObject(), true, CorrectionLocationType.LASTCOLUMN);
			aggun = new AggregateUnaryOperator(agg, ReduceCol.getReduceColFnObject());
		}
		else if ( opcode.equalsIgnoreCase("uacsqk+") ) {
			// ColSums
			AggregateOperator agg = new AggregateOperator(0, KahanPlusSq.getKahanPlusSqFnObject(), true, CorrectionLocationType.LASTROW);
			aggun = new AggregateUnaryOperator(agg, ReduceRow.getReduceRowFnObject());
		}
		else if ( opcode.equalsIgnoreCase("uamean") ) {
			// Mean
			AggregateOperator agg = new AggregateOperator(0, Mean.getMeanFnObject(), true, CorrectionLocationType.LASTTWOCOLUMNS);
			aggun = new AggregateUnaryOperator(agg, ReduceAll.getReduceAllFnObject());
		} 
		else if ( opcode.equalsIgnoreCase("uarmean") ) {
			// RowMeans
			AggregateOperator agg = new AggregateOperator(0, Mean.getMeanFnObject(), true, CorrectionLocationType.LASTTWOCOLUMNS);
			aggun = new AggregateUnaryOperator(agg, ReduceCol.getReduceColFnObject());
		} 
		else if ( opcode.equalsIgnoreCase("uacmean") ) {
			// ColMeans
			AggregateOperator agg = new AggregateOperator(0, Mean.getMeanFnObject(), true, CorrectionLocationType.LASTTWOROWS);
			aggun = new AggregateUnaryOperator(agg, ReduceRow.getReduceRowFnObject());
		}
		else if ( opcode.equalsIgnoreCase("ua+") ) {
			AggregateOperator agg = new AggregateOperator(0, Plus.getPlusFnObject());
			aggun = new AggregateUnaryOperator(agg, ReduceAll.getReduceAllFnObject());
		} 
		else if ( opcode.equalsIgnoreCase("uar+") ) {
			// RowSums
			AggregateOperator agg = new AggregateOperator(0, Plus.getPlusFnObject());
			aggun = new AggregateUnaryOperator(agg, ReduceCol.getReduceColFnObject());
		} 
		else if ( opcode.equalsIgnoreCase("uac+") ) {
			// ColSums
			AggregateOperator agg = new AggregateOperator(0, Plus.getPlusFnObject());
			aggun = new AggregateUnaryOperator(agg, ReduceRow.getReduceRowFnObject());
		}
		else if ( opcode.equalsIgnoreCase("ua*") ) {
			AggregateOperator agg = new AggregateOperator(1, Multiply.getMultiplyFnObject());
			aggun = new AggregateUnaryOperator(agg, ReduceAll.getReduceAllFnObject());
		} 
		else if ( opcode.equalsIgnoreCase("uamax") ) {
			AggregateOperator agg = new AggregateOperator(-Double.MAX_VALUE, Builtin.getBuiltinFnObject("max"));
			aggun = new AggregateUnaryOperator(agg, ReduceAll.getReduceAllFnObject());
		}
		else if ( opcode.equalsIgnoreCase("uamin") ) {
			AggregateOperator agg = new AggregateOperator(Double.MAX_VALUE, Builtin.getBuiltinFnObject("min"));
			aggun = new AggregateUnaryOperator(agg, ReduceAll.getReduceAllFnObject());
		} 
		else if ( opcode.equalsIgnoreCase("uatrace") ) {
			AggregateOperator agg = new AggregateOperator(0, Plus.getPlusFnObject());
			aggun = new AggregateUnaryOperator(agg, ReduceDiag.getReduceDiagFnObject());
		} 
		else if ( opcode.equalsIgnoreCase("uaktrace") ) {
			AggregateOperator agg = new AggregateOperator(0, KahanPlus.getKahanPlusFnObject(), true, CorrectionLocationType.LASTCOLUMN);
			aggun = new AggregateUnaryOperator(agg, ReduceDiag.getReduceDiagFnObject());
		} 		
		else if ( opcode.equalsIgnoreCase("uarmax") ) {
			AggregateOperator agg = new AggregateOperator(-Double.MAX_VALUE, Builtin.getBuiltinFnObject("max"));
			aggun = new AggregateUnaryOperator(agg, ReduceCol.getReduceColFnObject());
		} 
		else if (opcode.equalsIgnoreCase("uarimax") ) {
			AggregateOperator agg = new AggregateOperator(-Double.MAX_VALUE, Builtin.getBuiltinFnObject("maxindex"), true, CorrectionLocationType.LASTCOLUMN);
			aggun = new AggregateUnaryOperator(agg, ReduceCol.getReduceColFnObject());
		}
		else if ( opcode.equalsIgnoreCase("uarmin") ) {
			AggregateOperator agg = new AggregateOperator(Double.MAX_VALUE, Builtin.getBuiltinFnObject("min"));
			aggun = new AggregateUnaryOperator(agg, ReduceCol.getReduceColFnObject());
		} 
		else if (opcode.equalsIgnoreCase("uarimin") ) {
			AggregateOperator agg = new AggregateOperator(Double.MAX_VALUE, Builtin.getBuiltinFnObject("minindex"), true, CorrectionLocationType.LASTCOLUMN);
			aggun = new AggregateUnaryOperator(agg, ReduceCol.getReduceColFnObject());
		}
		else if ( opcode.equalsIgnoreCase("uacmax") ) {
			AggregateOperator agg = new AggregateOperator(-Double.MAX_VALUE, Builtin.getBuiltinFnObject("max"));
			aggun = new AggregateUnaryOperator(agg, ReduceRow.getReduceRowFnObject());
		} 
		else if ( opcode.equalsIgnoreCase("uacmin") ) {
			AggregateOperator agg = new AggregateOperator(Double.MAX_VALUE, Builtin.getBuiltinFnObject("min"));
			aggun = new AggregateUnaryOperator(agg, ReduceRow.getReduceRowFnObject());
		}
		
		return aggun;
	}
	
	/**
	 * 
	 * @param opcode
	 * @param corrExists
	 * @param corrLoc
	 * @return
	 */
	public static AggregateOperator parseAggregateOperator(String opcode, String corrExists, String corrLoc)
	{
		AggregateOperator agg = null;
	
		if ( opcode.equalsIgnoreCase("ak+") || opcode.equalsIgnoreCase("aktrace") ) {
			boolean lcorrExists = (corrExists==null) ? true : Boolean.parseBoolean(corrExists);
			CorrectionLocationType lcorrLoc = (corrLoc==null) ? CorrectionLocationType.LASTCOLUMN : CorrectionLocationType.valueOf(corrLoc);
			agg = new AggregateOperator(0, KahanPlus.getKahanPlusFnObject(), lcorrExists, lcorrLoc);
		}
		else if ( opcode.equalsIgnoreCase("asqk+") ) {
			boolean lcorrExists = (corrExists==null) ? true : Boolean.parseBoolean(corrExists);
			CorrectionLocationType lcorrLoc = (corrLoc==null) ? CorrectionLocationType.LASTCOLUMN : CorrectionLocationType.valueOf(corrLoc);
			agg = new AggregateOperator(0, KahanPlusSq.getKahanPlusSqFnObject(), lcorrExists, lcorrLoc);
		}
		else if ( opcode.equalsIgnoreCase("a+") ) {
			agg = new AggregateOperator(0, Plus.getPlusFnObject());
		} 
		else if ( opcode.equalsIgnoreCase("a*") ) {
			agg = new AggregateOperator(1, Multiply.getMultiplyFnObject());
		}
		else if (opcode.equalsIgnoreCase("arimax")){
			agg = new AggregateOperator(-Double.MAX_VALUE, Builtin.getBuiltinFnObject("maxindex"), true, CorrectionLocationType.LASTCOLUMN);
		}
		else if ( opcode.equalsIgnoreCase("amax") ) {
			agg = new AggregateOperator(-Double.MAX_VALUE, Builtin.getBuiltinFnObject("max"));
		}
		else if ( opcode.equalsIgnoreCase("amin") ) {
			agg = new AggregateOperator(Double.MAX_VALUE, Builtin.getBuiltinFnObject("min"));
		}
		else if (opcode.equalsIgnoreCase("arimin")){
			agg = new AggregateOperator(Double.MAX_VALUE, Builtin.getBuiltinFnObject("minindex"), true, CorrectionLocationType.LASTCOLUMN);
		}
		else if ( opcode.equalsIgnoreCase("amean") ) {
			boolean lcorrExists = (corrExists==null) ? true : Boolean.parseBoolean(corrExists);
			CorrectionLocationType lcorrLoc = (corrLoc==null) ? CorrectionLocationType.LASTTWOCOLUMNS : CorrectionLocationType.valueOf(corrLoc);
			agg = new AggregateOperator(0, KahanPlus.getKahanPlusFnObject(), lcorrExists, lcorrLoc);
		}
		
		return agg;
	}
	
	/**
	 * 
	 * @param opcode
	 * @return
	 */
	public static AggregateUnaryOperator parseCumulativeAggregateUnaryOperator(String opcode)
	{
		AggregateUnaryOperator aggun = null;
		if( "ucumack+".equals(opcode) ) { 
			AggregateOperator agg = new AggregateOperator(0, KahanPlus.getKahanPlusFnObject(), true, CorrectionLocationType.LASTROW);
			aggun = new AggregateUnaryOperator(agg, ReduceRow.getReduceRowFnObject());
		}
		else if ( "ucumac*".equals(opcode) ) { 
			AggregateOperator agg = new AggregateOperator(0, Multiply.getMultiplyFnObject(), false, CorrectionLocationType.NONE);
			aggun = new AggregateUnaryOperator(agg, ReduceRow.getReduceRowFnObject());
		}
		else if ( "ucumacmin".equals(opcode) ) { 
			AggregateOperator agg = new AggregateOperator(0, Builtin.getBuiltinFnObject("min"), false, CorrectionLocationType.NONE);
			aggun = new AggregateUnaryOperator(agg, ReduceRow.getReduceRowFnObject());
		}
		else if ( "ucumacmax".equals(opcode) ) { 
			AggregateOperator agg = new AggregateOperator(0, Builtin.getBuiltinFnObject("max"), false, CorrectionLocationType.NONE);
			aggun = new AggregateUnaryOperator(agg, ReduceRow.getReduceRowFnObject());
		}
		
		return aggun;
	}
	
	/**
	 * 
	 * @param opcode
	 * @return
	 * @throws DMLRuntimeException
	 */
	public static BinaryOperator parseBinaryOperator(String opcode) 
		throws DMLRuntimeException
	{
		if(opcode.equalsIgnoreCase("=="))
			return new BinaryOperator(Equals.getEqualsFnObject());
		else if(opcode.equalsIgnoreCase("!="))
			return new BinaryOperator(NotEquals.getNotEqualsFnObject());
		else if(opcode.equalsIgnoreCase("<"))
			return new BinaryOperator(LessThan.getLessThanFnObject());
		else if(opcode.equalsIgnoreCase(">"))
			return new BinaryOperator(GreaterThan.getGreaterThanFnObject());
		else if(opcode.equalsIgnoreCase("<="))
			return new BinaryOperator(LessThanEquals.getLessThanEqualsFnObject());
		else if(opcode.equalsIgnoreCase(">="))
			return new BinaryOperator(GreaterThanEquals.getGreaterThanEqualsFnObject());
		else if(opcode.equalsIgnoreCase("&&"))
			return new BinaryOperator(And.getAndFnObject());
		else if(opcode.equalsIgnoreCase("||"))
			return new BinaryOperator(Or.getOrFnObject());
		else if(opcode.equalsIgnoreCase("+"))
			return new BinaryOperator(Plus.getPlusFnObject());
		else if(opcode.equalsIgnoreCase("-"))
			return new BinaryOperator(Minus.getMinusFnObject());
		else if(opcode.equalsIgnoreCase("*"))
			return new BinaryOperator(Multiply.getMultiplyFnObject());
		else if(opcode.equalsIgnoreCase("1-*"))
			return new BinaryOperator(Minus1Multiply.getMinus1MultiplyFnObject());
		else if ( opcode.equalsIgnoreCase("*2") ) 
			return new BinaryOperator(Multiply2.getMultiply2FnObject());
		else if(opcode.equalsIgnoreCase("/"))
			return new BinaryOperator(Divide.getDivideFnObject());
		else if(opcode.equalsIgnoreCase("%%"))
			return new BinaryOperator(Modulus.getModulusFnObject());
		else if(opcode.equalsIgnoreCase("%/%"))
			return new BinaryOperator(IntegerDivide.getIntegerDivideFnObject());
		else if(opcode.equalsIgnoreCase("^"))
			return new BinaryOperator(Power.getPowerFnObject());
		else if ( opcode.equalsIgnoreCase("^2") )
			return new BinaryOperator(Power2.getPower2FnObject());
		else if ( opcode.equalsIgnoreCase("max") ) 
			return new BinaryOperator(Builtin.getBuiltinFnObject("max"));
		else if ( opcode.equalsIgnoreCase("min") ) 
			return new BinaryOperator(Builtin.getBuiltinFnObject("min"));
		
		throw new DMLRuntimeException("Unknown binary opcode " + opcode);
	}
	
	/**
	 * scalar-matrix operator
	 * 
	 * @param opcode
	 * @param arg1IsScalar
	 * @return
	 * @throws DMLRuntimeException
	 */
	public static ScalarOperator parseScalarBinaryOperator(String opcode, boolean arg1IsScalar) 
		throws DMLRuntimeException
	{
		//for all runtimes that set constant dynamically (cp/spark)
		double default_constant = 0;
		
		return parseScalarBinaryOperator(opcode, arg1IsScalar, default_constant);
	}
	
	/**
	 * scalar-matrix operator
	 * 
	 * @param opcode
	 * @param arg1IsScalar
	 * @param constant
	 * @return
	 * @throws DMLRuntimeException
	 */
	public static ScalarOperator parseScalarBinaryOperator(String opcode, boolean arg1IsScalar, double constant)
		throws DMLRuntimeException
	{
		//commutative operators
		if ( opcode.equalsIgnoreCase("+") ){ 
			return new RightScalarOperator(Plus.getPlusFnObject(), constant); 
		}
		else if ( opcode.equalsIgnoreCase("*") ) {
			return new RightScalarOperator(Multiply.getMultiplyFnObject(), constant);
		} 
		//non-commutative operators
		else if ( opcode.equalsIgnoreCase("-") ) {
			if(arg1IsScalar)
				return new LeftScalarOperator(Minus.getMinusFnObject(), constant);
			else return new RightScalarOperator(Minus.getMinusFnObject(), constant);
		}
		else if ( opcode.equalsIgnoreCase("-nz") ) {
			//no support for left scalar yet
			return new RightScalarOperator(MinusNz.getMinusNzFnObject(), constant);
		}
		else if ( opcode.equalsIgnoreCase("/") ) {
			if(arg1IsScalar)
				return new LeftScalarOperator(Divide.getDivideFnObject(), constant);
			else return new RightScalarOperator(Divide.getDivideFnObject(), constant);
		}  
		else if ( opcode.equalsIgnoreCase("%%") ) {
			if(arg1IsScalar)
				return new LeftScalarOperator(Modulus.getModulusFnObject(), constant);
			else return new RightScalarOperator(Modulus.getModulusFnObject(), constant);
		}
		else if ( opcode.equalsIgnoreCase("%/%") ) {
			if(arg1IsScalar)
				return new LeftScalarOperator(IntegerDivide.getIntegerDivideFnObject(), constant);
			else return new RightScalarOperator(IntegerDivide.getIntegerDivideFnObject(), constant);
		}
		else if ( opcode.equalsIgnoreCase("^") ){
			if(arg1IsScalar)
				return new LeftScalarOperator(Power.getPowerFnObject(), constant);
			else return new RightScalarOperator(Power.getPowerFnObject(), constant);
		}
		else if ( opcode.equalsIgnoreCase("max") ) {
			return new RightScalarOperator(Builtin.getBuiltinFnObject("max"), constant);
		}
		else if ( opcode.equalsIgnoreCase("min") ) {
			return new RightScalarOperator(Builtin.getBuiltinFnObject("min"), constant);
		}
		else if ( opcode.equalsIgnoreCase("log") || opcode.equalsIgnoreCase("log_nz") ){
			if( arg1IsScalar )
				return new LeftScalarOperator(Builtin.getBuiltinFnObject(opcode), constant);
			return new RightScalarOperator(Builtin.getBuiltinFnObject(opcode), constant);
		}
		else if ( opcode.equalsIgnoreCase(">") ) {
			if(arg1IsScalar)
				return new LeftScalarOperator(GreaterThan.getGreaterThanFnObject(), constant);
			return new RightScalarOperator(GreaterThan.getGreaterThanFnObject(), constant);
		}
		else if ( opcode.equalsIgnoreCase(">=") ) {
			if(arg1IsScalar)
				return new LeftScalarOperator(GreaterThanEquals.getGreaterThanEqualsFnObject(), constant);
			return new RightScalarOperator(GreaterThanEquals.getGreaterThanEqualsFnObject(), constant);
		}
		else if ( opcode.equalsIgnoreCase("<") ) {
			if(arg1IsScalar)
				return new LeftScalarOperator(LessThan.getLessThanFnObject(), constant);
			return new RightScalarOperator(LessThan.getLessThanFnObject(), constant);
		}
		else if ( opcode.equalsIgnoreCase("<=") ) {
			if(arg1IsScalar)
				return new LeftScalarOperator(LessThanEquals.getLessThanEqualsFnObject(), constant);
			return new RightScalarOperator(LessThanEquals.getLessThanEqualsFnObject(), constant);
		}
		else if ( opcode.equalsIgnoreCase("==") ) {
			if(arg1IsScalar)
				return new LeftScalarOperator(Equals.getEqualsFnObject(), constant);
			return new RightScalarOperator(Equals.getEqualsFnObject(), constant);
		}
		else if ( opcode.equalsIgnoreCase("!=") ) {
			if(arg1IsScalar)
				return new LeftScalarOperator(NotEquals.getNotEqualsFnObject(), constant);
			return new RightScalarOperator(NotEquals.getNotEqualsFnObject(), constant);
		}
		
		//operations that only exist for performance purposes (all unary or commutative operators)
		else if ( opcode.equalsIgnoreCase("*2") ) {
			return new RightScalarOperator(Multiply2.getMultiply2FnObject(), constant);
		} 
		else if ( opcode.equalsIgnoreCase("^2") ){
			return new RightScalarOperator(Power2.getPower2FnObject(), constant);
		}
		else if ( opcode.equalsIgnoreCase("1-*") ) {
			return new RightScalarOperator(Minus1Multiply.getMinus1MultiplyFnObject(), constant);
		}
		
		//operations that only exist in mr
		else if ( opcode.equalsIgnoreCase("s-r") ) {
			return new LeftScalarOperator(Minus.getMinusFnObject(), constant);
		} 
		else if ( opcode.equalsIgnoreCase("so") ) {
			return new LeftScalarOperator(Divide.getDivideFnObject(), constant);
		}
		
		throw new DMLRuntimeException("Unknown binary opcode " + opcode);
	}	


	/**
	 * 
	 * @param opcode
	 * @return
	 * @throws DMLRuntimeException
	 */
	public static BinaryOperator parseExtendedBinaryOperator(String opcode) 
		throws DMLRuntimeException
	{
		if(opcode.equalsIgnoreCase("==") || opcode.equalsIgnoreCase("map=="))
			return new BinaryOperator(Equals.getEqualsFnObject());
		else if(opcode.equalsIgnoreCase("!=") || opcode.equalsIgnoreCase("map!="))
			return new BinaryOperator(NotEquals.getNotEqualsFnObject());
		else if(opcode.equalsIgnoreCase("<") || opcode.equalsIgnoreCase("map<"))
			return new BinaryOperator(LessThan.getLessThanFnObject());
		else if(opcode.equalsIgnoreCase(">") || opcode.equalsIgnoreCase("map>"))
			return new BinaryOperator(GreaterThan.getGreaterThanFnObject());
		else if(opcode.equalsIgnoreCase("<=") || opcode.equalsIgnoreCase("map<="))
			return new BinaryOperator(LessThanEquals.getLessThanEqualsFnObject());
		else if(opcode.equalsIgnoreCase(">=") || opcode.equalsIgnoreCase("map>="))
			return new BinaryOperator(GreaterThanEquals.getGreaterThanEqualsFnObject());
		else if(opcode.equalsIgnoreCase("&&"))
			return new BinaryOperator(And.getAndFnObject());
		else if(opcode.equalsIgnoreCase("||"))
			return new BinaryOperator(Or.getOrFnObject());
		else if(opcode.equalsIgnoreCase("+") || opcode.equalsIgnoreCase("map+"))
			return new BinaryOperator(Plus.getPlusFnObject());
		else if(opcode.equalsIgnoreCase("-") || opcode.equalsIgnoreCase("map-"))
			return new BinaryOperator(Minus.getMinusFnObject());
		else if(opcode.equalsIgnoreCase("*") || opcode.equalsIgnoreCase("map*"))
			return new BinaryOperator(Multiply.getMultiplyFnObject());
		else if(opcode.equalsIgnoreCase("1-*") || opcode.equalsIgnoreCase("map1-*"))
			return new BinaryOperator(Minus1Multiply.getMinus1MultiplyFnObject());
		else if ( opcode.equalsIgnoreCase("*2") ) 
			return new BinaryOperator(Multiply2.getMultiply2FnObject());
		else if(opcode.equalsIgnoreCase("/") || opcode.equalsIgnoreCase("map/"))
			return new BinaryOperator(Divide.getDivideFnObject());
		else if(opcode.equalsIgnoreCase("%%") || opcode.equalsIgnoreCase("map%%"))
			return new BinaryOperator(Modulus.getModulusFnObject());
		else if(opcode.equalsIgnoreCase("%/%") || opcode.equalsIgnoreCase("map%/%"))
			return new BinaryOperator(IntegerDivide.getIntegerDivideFnObject());
		else if(opcode.equalsIgnoreCase("^") || opcode.equalsIgnoreCase("map^"))
			return new BinaryOperator(Power.getPowerFnObject());
		else if ( opcode.equalsIgnoreCase("^2") )
			return new BinaryOperator(Power2.getPower2FnObject());
		else if ( opcode.equalsIgnoreCase("max") || opcode.equalsIgnoreCase("mapmax") ) 
			return new BinaryOperator(Builtin.getBuiltinFnObject("max"));
		else if ( opcode.equalsIgnoreCase("min") || opcode.equalsIgnoreCase("mapmin") ) 
			return new BinaryOperator(Builtin.getBuiltinFnObject("min"));
		
		throw new DMLRuntimeException("Unknown binary opcode " + opcode);
	}
	
	
	/**
	 * 
	 * @param opcode
	 * @return
	 */
	public static String deriveAggregateOperatorOpcode(String opcode)
	{
		if ( opcode.equalsIgnoreCase("uak+") || opcode.equalsIgnoreCase("uark+") || opcode.equalsIgnoreCase("uack+"))
			return "ak+";
		else if ( opcode.equalsIgnoreCase("uasqk+") || opcode.equalsIgnoreCase("uarsqk+") || opcode.equalsIgnoreCase("uacsqk+") )
			return "asqk+";
		else if ( opcode.equalsIgnoreCase("uamean") || opcode.equalsIgnoreCase("uarmean") || opcode.equalsIgnoreCase("uacmean") )
			return "amean";
		else if ( opcode.equalsIgnoreCase("ua+") || opcode.equalsIgnoreCase("uar+") || opcode.equalsIgnoreCase("uac+") )
			return "a+";
		else if ( opcode.equalsIgnoreCase("ua*") )
			return "a*";
		else if ( opcode.equalsIgnoreCase("uatrace") || opcode.equalsIgnoreCase("uaktrace") ) 
			return "aktrace";
		else if ( opcode.equalsIgnoreCase("uamax") || opcode.equalsIgnoreCase("uarmax") || opcode.equalsIgnoreCase("uacmax") )
			return "amax";
		else if ( opcode.equalsIgnoreCase("uamin") || opcode.equalsIgnoreCase("uarmin") || opcode.equalsIgnoreCase("uacmin") )
			return "amin";
		else if (opcode.equalsIgnoreCase("uarimax") )
			return "arimax";
		else if (opcode.equalsIgnoreCase("uarimin") )
			return "arimin";
	
		return null;
	}

	/**
	 * 
	 * @param opcode
	 * @return
	 */
	public static CorrectionLocationType deriveAggregateOperatorCorrectionLocation(String opcode)
	{
		if ( opcode.equalsIgnoreCase("uak+") || opcode.equalsIgnoreCase("uark+") ||
				opcode.equalsIgnoreCase("uasqk+") || opcode.equalsIgnoreCase("uarsqk+") ||
				opcode.equalsIgnoreCase("uatrace") || opcode.equalsIgnoreCase("uaktrace") )
			return CorrectionLocationType.LASTCOLUMN;
		else if ( opcode.equalsIgnoreCase("uack+") || opcode.equalsIgnoreCase("uacsqk+") )
			return CorrectionLocationType.LASTROW;
		else if ( opcode.equalsIgnoreCase("uamean") || opcode.equalsIgnoreCase("uarmean") )
			return CorrectionLocationType.LASTTWOCOLUMNS;
		else if ( opcode.equalsIgnoreCase("uacmean") )
			return CorrectionLocationType.LASTTWOROWS;
		else if (opcode.equalsIgnoreCase("uarimax") || opcode.equalsIgnoreCase("uarimin") )	
			return CorrectionLocationType.LASTCOLUMN;
		
		return CorrectionLocationType.NONE;
	}

	/**
	 * 
	 * @param opcode
	 * @return
	 */
	public static boolean isDistQuaternaryOpcode(String opcode) 
	{
		return WeightedSquaredLoss.OPCODE.equalsIgnoreCase(opcode)     //mapwsloss
			|| WeightedSquaredLossR.OPCODE.equalsIgnoreCase(opcode)    //redwsloss
			|| WeightedSigmoid.OPCODE.equalsIgnoreCase(opcode)   	   //mapwsigmoid
			|| WeightedSigmoidR.OPCODE.equalsIgnoreCase(opcode)        //redwsigmoid
			|| WeightedDivMM.OPCODE.equalsIgnoreCase(opcode)           //mapwdivmm
			|| WeightedDivMMR.OPCODE.equalsIgnoreCase(opcode)          //redwdivmm
			|| WeightedCrossEntropy.OPCODE.equalsIgnoreCase(opcode)    //mapwdcemm
			|| WeightedCrossEntropyR.OPCODE.equalsIgnoreCase(opcode);  //redwdcemm
	}
}
