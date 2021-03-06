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


package com.ibm.bi.dml.runtime.matrix.operators;

import com.ibm.bi.dml.runtime.functionobjects.IndexFunction;
import com.ibm.bi.dml.runtime.functionobjects.KahanPlus;
import com.ibm.bi.dml.runtime.functionobjects.KahanPlusSq;
import com.ibm.bi.dml.runtime.functionobjects.Minus;
import com.ibm.bi.dml.runtime.functionobjects.Or;
import com.ibm.bi.dml.runtime.functionobjects.Plus;


public class AggregateUnaryOperator  extends Operator 
{
	private static final long serialVersionUID = 6690553323120787735L;

	public AggregateOperator aggOp;
	public IndexFunction indexFn;
	private int k; //num threads

	public AggregateUnaryOperator(AggregateOperator aop, IndexFunction iop)
	{
		//default degree of parallelism is 1 
		//(for example in MR/Spark because we parallelize over the number of blocks)
		this( aop, iop, 1 );
	}

	public AggregateUnaryOperator(AggregateOperator aop, IndexFunction iop, int numThreads)
	{
		aggOp = aop;
		indexFn = iop;
		k = numThreads;
		
		//decide on sparse safe
		if( aggOp.increOp.fn instanceof Plus || 
			aggOp.increOp.fn instanceof KahanPlus ||
			aggOp.increOp.fn instanceof KahanPlusSq ||
			aggOp.increOp.fn instanceof Or ||
			aggOp.increOp.fn instanceof Minus ) 
		{
			sparseSafe=true;
		}
		else
			sparseSafe=false;
	}
	
	public void setNumThreads(int numThreads) {
		k = numThreads;
	}
	
	public int getNumThreads(){
		return k;
	}
}
