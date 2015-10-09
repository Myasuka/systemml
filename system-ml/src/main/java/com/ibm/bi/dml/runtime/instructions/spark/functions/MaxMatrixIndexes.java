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
package com.ibm.bi.dml.runtime.instructions.spark.functions;

import org.apache.spark.api.java.function.Function2;

import com.ibm.bi.dml.runtime.matrix.data.MatrixIndexes;

public class MaxMatrixIndexes implements Function2<MatrixIndexes, MatrixIndexes, MatrixIndexes> {
	private static final long serialVersionUID = -8421979264801112485L;

	@Override
	public MatrixIndexes call(MatrixIndexes left, MatrixIndexes right) throws Exception {
		return new MatrixIndexes(Math.max(left.getRowIndex(), right.getRowIndex()), Math.max(left.getColumnIndex(), right.getColumnIndex()));
	}
	
}
