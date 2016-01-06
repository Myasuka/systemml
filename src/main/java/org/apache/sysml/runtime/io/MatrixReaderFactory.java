/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysml.runtime.io;

import org.apache.sysml.hops.OptimizerUtils;
import org.apache.sysml.runtime.DMLRuntimeException;
import org.apache.sysml.runtime.matrix.data.CSVFileFormatProperties;
import org.apache.sysml.runtime.matrix.data.InputInfo;

/**
 * 
 * 
 */
public class MatrixReaderFactory 
{
	/**
	 * 
	 * @param iinfo
	 * @return
	 * @throws DMLRuntimeException
	 */
	public static MatrixReader createMatrixReader( InputInfo iinfo ) 
		throws DMLRuntimeException
	{
		MatrixReader reader = null;
		
		if( iinfo == InputInfo.TextCellInputInfo || iinfo == InputInfo.MatrixMarketInputInfo )
		{
			if( OptimizerUtils.PARALLEL_CP_READ_TEXTFORMATS )
				reader = new ReaderTextCellParallel( iinfo );
			else
				reader = new ReaderTextCell( iinfo );	
		}
		else if( iinfo == InputInfo.CSVInputInfo )
		{
			if( OptimizerUtils.PARALLEL_CP_READ_TEXTFORMATS )
				reader = new ReaderTextCSVParallel(new CSVFileFormatProperties());
			else
				reader = new ReaderTextCSV(new CSVFileFormatProperties());
		}
		else if( iinfo == InputInfo.BinaryCellInputInfo ) 
			reader = new ReaderBinaryCell();
		else if( iinfo == InputInfo.BinaryBlockInputInfo ) {
			if( OptimizerUtils.PARALLEL_CP_READ_BINARYFORMATS )
				reader = new ReaderBinaryBlockParallel( false );
			else
				reader = new ReaderBinaryBlock( false );
		}
		else {
			throw new DMLRuntimeException("Failed to create matrix reader for unknown input info: "
		                                   + InputInfo.inputInfoToString(iinfo));
		}
		
		return reader;
	}
	
	/**
	 * 
	 * @param props
	 * @return
	 * @throws DMLRuntimeException
	 */
	public static MatrixReader createMatrixReader( ReadProperties props ) 
		throws DMLRuntimeException
	{
		//check valid read properties
		if( props == null )
			throw new DMLRuntimeException("Failed to create matrix reader with empty properties.");
		
		MatrixReader reader = null;
		InputInfo iinfo = props.inputInfo;

		if( iinfo == InputInfo.TextCellInputInfo || iinfo == InputInfo.MatrixMarketInputInfo ) {
			if( OptimizerUtils.PARALLEL_CP_READ_TEXTFORMATS )
				reader = new ReaderTextCellParallel( iinfo );
			else
				reader = new ReaderTextCell( iinfo );
		}
		else if( iinfo == InputInfo.CSVInputInfo ) {
			if( OptimizerUtils.PARALLEL_CP_READ_TEXTFORMATS )
				reader = new ReaderTextCSVParallel( props.formatProperties!=null ? (CSVFileFormatProperties)props.formatProperties : new CSVFileFormatProperties());
			else
				reader = new ReaderTextCSV( props.formatProperties!=null ? (CSVFileFormatProperties)props.formatProperties : new CSVFileFormatProperties());
		}
		else if( iinfo == InputInfo.BinaryCellInputInfo ) 
			reader = new ReaderBinaryCell();
		else if( iinfo == InputInfo.BinaryBlockInputInfo ) {
			if( OptimizerUtils.PARALLEL_CP_READ_BINARYFORMATS )
				reader = new ReaderBinaryBlockParallel( props.localFS );
			else
				reader = new ReaderBinaryBlock( props.localFS );
		}
		else {
			throw new DMLRuntimeException("Failed to create matrix reader for unknown input info: "
		                                   + InputInfo.inputInfoToString(iinfo));
		}
		
		return reader;
	}
}
