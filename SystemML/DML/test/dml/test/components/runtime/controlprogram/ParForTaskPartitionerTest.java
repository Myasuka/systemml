package dml.test.components.runtime.controlprogram;

import java.util.Collection;
import java.util.LinkedList;

import junit.framework.Assert;

import org.junit.Test;

import dml.runtime.controlprogram.ParForProgramBlock;
import dml.runtime.controlprogram.ParForProgramBlock.PTaskPartitioner;
import dml.runtime.controlprogram.parfor.LocalTaskQueue;
import dml.runtime.controlprogram.parfor.Task;
import dml.runtime.controlprogram.parfor.TaskPartitioner;
import dml.runtime.controlprogram.parfor.TaskPartitionerFactoring;
import dml.runtime.controlprogram.parfor.TaskPartitionerFactoringConstrained;
import dml.runtime.controlprogram.parfor.TaskPartitionerFixedsize;
import dml.runtime.controlprogram.parfor.TaskPartitionerNaive;
import dml.runtime.controlprogram.parfor.TaskPartitionerStatic;
import dml.runtime.controlprogram.parfor.Task.TaskType;
import dml.runtime.instructions.CPInstructions.IntObject;
import dml.utils.DMLRuntimeException;

/**
 * Different test cases for task partitioners w.r.t. both number of created tasks
 * and completeness of created tasks.
 * 
 * @author mboehm
 *
 */
public class ParForTaskPartitionerTest 
{
	private static final int _par = 4;
	private static final int _k = 4; 
	private static final int _N = 101;
	private static final String _dat = "i";
	
	//expected results 
	private static final int[] _naiveTP = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
                                           1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1 }; 
	private static final int[] _staticTP = { 26,25,25,25 }; 
	private static final int[] _fixedTP = { 4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,1 }; 
	private static final int[] _factTP = { 13,13,13,13,7,7,7,7,3,3,3,3,2,2,2,2,1 }; 
	private static final int[] _cfactTP = { 13,13,13,13,7,7,7,7,4,4,4,4,4,1 }; 

	@Test
	public void testFixedSizeTaskPartitionerNumSetFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.FIXED, false, false);
		if( !checkExpectedNum(tasks, _fixedTP, false) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testFixedSizeTaskPartitionerNumSetStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.FIXED, false, true);
		if( !checkExpectedNum(tasks, _fixedTP, false) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testFixedSizeTaskPartitionerNumRangeFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.FIXED, true, false);
		if( !checkExpectedNum(tasks, _fixedTP, true) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testFixedSizeTaskPartitionerNumRangeStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.FIXED, true, true);
		if( !checkExpectedNum(tasks, _fixedTP, true) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testFixedSizeTaskPartitionerCompletenessSetFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.FIXED, false, false);
		if( !checkCompleteness(tasks, 1, _N, 1, false) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testFixedSizeTaskPartitionerCompletenessSetStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.FIXED, false, true);
		if( !checkCompleteness(tasks, 1, _N, 1, false) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testFixedSizeTaskPartitionerCompletenessRangeFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.FIXED, true, false);
		if( !checkCompleteness(tasks, 1, _N, 1, true) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testFixedSizeTaskPartitionerCompletenessRangeStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.FIXED, true, true);
		if( !checkCompleteness(tasks, 1, _N, 1, true) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testNaiveTaskPartitionerNumSetFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.NAIVE, false, false);
		if( !checkExpectedNum(tasks, _naiveTP, false) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testNaiveTaskPartitionerNumSetStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.NAIVE, false, true);
		if( !checkExpectedNum(tasks, _naiveTP, false) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testNaiveTaskPartitionerNumRangeFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.NAIVE, true, false);
		if( !checkExpectedNum(tasks, _naiveTP, true) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testNaiveTaskPartitionerNumRangeStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.NAIVE, true, true);
		if( !checkExpectedNum(tasks, _naiveTP, true) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testNaiveTaskPartitionerCompletenessSetFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.NAIVE, false, false);
		if( !checkCompleteness(tasks, 1, _N, 1, false) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testNaiveTaskPartitionerCompletenessSetStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.NAIVE, false, true);
		if( !checkCompleteness(tasks, 1, _N, 1, false) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testNaiveTaskPartitionerCompletenessRangeFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.NAIVE, true, false);
		if( !checkCompleteness(tasks, 1, _N, 1, true) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testNaiveTaskPartitionerCompletenessRangeStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.NAIVE, true, true);
		if( !checkCompleteness(tasks, 1, _N, 1, true) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testStaticTaskPartitionerNumSetFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.STATIC, false, false);
		if( !checkExpectedNum(tasks, _staticTP, false) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testStaticTaskPartitionerNumSetStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.STATIC, false, true);
		if( !checkExpectedNum(tasks, _staticTP, false) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testStaticTaskPartitionerNumRangeFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.STATIC, true, false);
		if( !checkExpectedNum(tasks, _staticTP, true) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testStaticTaskPartitionerNumRangeStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.STATIC, true, true);
		if( !checkExpectedNum(tasks, _staticTP, true) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testStaticTaskPartitionerCompletenessSetFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.STATIC, false, false);
		if( !checkCompleteness(tasks, 1, _N, 1, false ) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testStaticTaskPartitionerCompletenessSetStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.STATIC, false, true);
		if( !checkCompleteness(tasks, 1, _N, 1, false) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testStaticTaskPartitionerCompletenessRangeFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.STATIC, true, false);
		if( !checkCompleteness(tasks, 1, _N, 1, true) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testStaticTaskPartitionerCompletenessRangeStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.STATIC, true, true);
		if( !checkCompleteness(tasks, 1, _N, 1, true) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testFactoringTaskPartitionerNumSetFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.FACTORING, false, false);
		if( !checkExpectedNum(tasks, _factTP, false) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testFactoringTaskPartitionerNumSetStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.FACTORING, false, true);
		if( !checkExpectedNum(tasks, _factTP, false) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testFactoringTaskPartitionerNumRangeFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.FACTORING, true, false);
		if( !checkExpectedNum(tasks, _factTP, true) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testFactoringTaskPartitionerNumRangeStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.FACTORING, true, true);
		if( !checkExpectedNum(tasks, _factTP, true) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testFactoringTaskPartitionerCompletenessSetFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.FACTORING, false, false);
		if( !checkCompleteness(tasks, 1, _N, 1, false) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testFactoringTaskPartitionerCompletenessSetStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.FACTORING, false, true);
		if( !checkCompleteness(tasks, 1, _N, 1, false) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testFactoringTaskPartitionerCompletenessRangeFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.FACTORING, true, false);
		if( !checkCompleteness(tasks, 1, _N, 1, true) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testFactoringTaskPartitionerCompletenessRangeStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.FACTORING, true, true);
		if( !checkCompleteness(tasks, 1, _N, 1, true) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testFactoringConstrainedTaskPartitionerNumSetFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.CFACTORING, false, false);
		if( !checkExpectedNum(tasks, _cfactTP, false) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testFactoringConstrainedTaskPartitionerNumSetStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.CFACTORING, false, true);
		if( !checkExpectedNum(tasks, _cfactTP, false) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testFactoringConstrainedTaskPartitionerNumRangeFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.CFACTORING, true, false);
		if( !checkExpectedNum(tasks, _cfactTP, true) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testFactoringConstrainedTaskPartitionerNumRangeStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.CFACTORING, true, true);
		if( !checkExpectedNum(tasks, _cfactTP, true) )
			Assert.fail("Wrong number of tasks or number of iterations per task.");
	}
	
	@Test
	public void testFactoringConstrainedTaskPartitionerCompletenessSetFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.CFACTORING, false, false);
		if( !checkCompleteness(tasks, 1, _N, 1, false) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testFactoringConstrainedTaskPartitionerCompletenessSetStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.CFACTORING, false, true);
		if( !checkCompleteness(tasks, 1, _N, 1, false) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testFactoringConstrainedTaskPartitionerCompletenessRangeFull() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.CFACTORING, true, false);
		if( !checkCompleteness(tasks, 1, _N, 1, true) )
			Assert.fail("Wrong values in iterations.");
	}
	
	@Test
	public void testFactoringConstrainedTaskPartitionerCompletenessRangeStream() 
		throws DMLRuntimeException, InterruptedException 
	{ 
		Collection<Task> tasks = createTasks(PTaskPartitioner.CFACTORING, true, true);
		if( !checkCompleteness(tasks, 1, _N, 1, true) )
			Assert.fail("Wrong values in iterations.");
	}
	
	private Collection<Task> createTasks( PTaskPartitioner partitioner, boolean range, boolean streaming ) 
		throws DMLRuntimeException, InterruptedException
	{
		ParForProgramBlock.USE_RANGE_TASKS_IF_USEFUL = range; 
		
		TaskPartitioner tp = null;
		switch( partitioner )
		{
			case FIXED:
				tp =  new TaskPartitionerFixedsize( _k, _dat, new IntObject(1), new IntObject(_N), new IntObject(1) );
				break;
			case NAIVE:
				tp =  new TaskPartitionerNaive( _k, _dat, new IntObject(1), new IntObject(_N), new IntObject(1) );
				break;
			case STATIC:
				tp =  new TaskPartitionerStatic( _k, _par, _dat, new IntObject(1), new IntObject(_N), new IntObject(1) );
				break;
			case FACTORING:
				tp =  new TaskPartitionerFactoring( _k, _par, _dat, new IntObject(1), new IntObject(_N), new IntObject(1) );
				break;
			case CFACTORING:
				tp =  new TaskPartitionerFactoringConstrained( _k, _par, _k, _dat, new IntObject(1), new IntObject(_N), new IntObject(1) );
				break;
			default:
				throw new RuntimeException("Unable to create task partitioner");
		}
		
		Collection<Task> tasks = null;
		if( streaming )
		{
			tasks = new LinkedList<Task>();
			LocalTaskQueue tq = new LocalTaskQueue();
			tp.createTasks(tq);
			Task t = null;
			while( (t=tq.dequeueTask())!=LocalTaskQueue.NO_MORE_TASKS )
				tasks.add(t);
		}
		else
		{
			tasks = tp.createTasks();
		}
		
		return tasks;
	}
	
	private boolean checkExpectedNum( Collection<Task> tasks, int[] expected, boolean range )
	{
		boolean ret = ( tasks.size() == expected.length );
		int count=0;
		for( Task t : tasks )
		{
			if( range && t.getType()==TaskType.ITERATION_RANGE )
			{
				int from = t.getIterations().get(0).getIntValue();
				int to = t.getIterations().get(1).getIntValue();
				int incr = t.getIterations().get(2).getIntValue();
				int N = (to-from+1)/incr;
				
				//System.out.println("expected:"+expected[count]+"  /  created:"+N);
				
			}
			else
			{
				//System.out.println("expected:"+expected[count]+"  /  created:"+t.size());
				ret &= (t.size() == expected[count]);
			}
			
			count++;	
		}
		return ret;
	}
	
	private boolean checkCompleteness( Collection<Task> tasks, int from, int to, int incr, boolean range )
	{
		boolean ret = true;
		
		int current = from;
		for( Task t : tasks )
		{
			if( range && t.getType()==TaskType.ITERATION_RANGE )
			{
				int lfrom = t.getIterations().get(0).getIntValue();
				int lto = t.getIterations().get(1).getIntValue();
				int lincr = t.getIterations().get(2).getIntValue();
				
				for( int i=lfrom; i<=lto; i+=lincr )
				{
					//System.out.println("expected:"+current+"  /  created:"+i);
					
					if( current > to )
						return false;
						
					ret &= (i == current);
					current += incr;
				}
			}
			else
			{
				for( IntObject o : t.getIterations() )
				{
					//System.out.println("expected:"+current+"  /  created:"+o.getIntValue());
									
					if( current > to )
						return false;
						
					ret &= (o.getIntValue() == current);
					current += incr;
				}
			}
		}
		
		return ret;		
	}

}
