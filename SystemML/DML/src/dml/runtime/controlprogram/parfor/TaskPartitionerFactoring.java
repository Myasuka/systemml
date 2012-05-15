package dml.runtime.controlprogram.parfor;

import java.util.Collection;
import java.util.LinkedList;

import dml.runtime.controlprogram.ParForProgramBlock;
import dml.runtime.controlprogram.parfor.Task.TaskType;
import dml.runtime.instructions.CPInstructions.IntObject;
import dml.utils.DMLRuntimeException;

/**
 * This factoring task partitioner virtually iterates over the given FOR loop (from, to, incr),
 * creates iterations and group them to tasks. Note that the task size is used here.
 * The tasks are created with decreasing size for good load balance of heterogeneous tasks.
 * 
 * 
 * See the original paper for details:
 * [Susan Flynn Hummel, Edith Schonberg, Lawrence E. Flynn: 
 * Factoring: a practical and robust method for scheduling parallel loops. 
 * SC 1991: 610-632]
 * 
 * @author mboehm
 */
public class TaskPartitionerFactoring extends TaskPartitioner
{
	private int _numThreads = -1;
	
	public TaskPartitionerFactoring( int taskSize, int numThreads, String iterVarName, IntObject fromVal, IntObject toVal, IntObject incrVal ) 
	{
		super(taskSize, iterVarName, fromVal, toVal, incrVal);
		
		_numThreads = numThreads;
	}

	@Override
	public Collection<Task> createTasks() 
		throws DMLRuntimeException 
	{
		LinkedList<Task> tasks = new LinkedList<Task>();
		
		int lFrom  = _fromVal.getIntValue();
		int lTo    = _toVal.getIntValue();
		int lIncr  = _incrVal.getIntValue();
		
		int P = _numThreads;     // number of parallel workers
		int N = (lTo-lFrom+1)/lIncr; // total number of iterations
		int R = N;               // remaining number of iterations
		int K = -1;              //next _numThreads task sizes	
		TaskType type = null;    // type of iterations: range tasks (similar to run-length encoding) make only sense if taskSize>3
		
		for( int i = lFrom; i<=lTo;  )
		{
			K = determineNextBatchSize(R, P);
			R -= (K * P);
			
			type = (ParForProgramBlock.USE_RANGE_TASKS_IF_USEFUL && K>3 ) ? 
					   TaskType.ITERATION_RANGE : TaskType.ITERATION_SET;
			
			//for each logical processor
			for( int j=0; j<P; j++ )
			{
				if( i > lTo ) //no more iterations
					break;
				
				//create new task and add to list of tasks
				Task lTask = new Task( type );
				tasks.addLast(lTask);
				
				// add iterations to task 
				if( type == TaskType.ITERATION_SET ) 
				{
					//value based tasks
					for( int k=0; k<K && i<=lTo; k++, i+=lIncr )
					{
						lTask.addIteration(new IntObject(_iterVarName, i));				
					}				
				}
				else 
				{
					//determine end of task
					int to = Math.min( i+(K-1)*lIncr, lTo );
					
					//range based tasks
					lTask.addIteration(new IntObject(_iterVarName, i));	    //from
					lTask.addIteration(new IntObject(_iterVarName, to));    //to
					lTask.addIteration(new IntObject(_iterVarName, lIncr));	//increment
					
					i = to + lIncr;
				}
			}
		}

		return tasks;
	}

	@Override
	public int createTasks(LocalTaskQueue queue) 
		throws DMLRuntimeException 
	{		
		int numCreatedTasks=0;
		
		int lFrom  = _fromVal.getIntValue();
		int lTo    = _toVal.getIntValue();
		int lIncr  = _incrVal.getIntValue();
		
		int P = _numThreads;     // number of parallel workers
		int N = (lTo-lFrom+1)/lIncr; // total number of iterations
		int R = N;               // remaining number of iterations
		int K = -1;              //next _numThreads task sizes	
	    TaskType type = null;    // type of iterations: range tasks (similar to run-length encoding) make only sense if taskSize>3
		
		try
		{
			for( int i = lFrom; i<=lTo;  )
			{
				K = determineNextBatchSize(R, P);
				R -= (K * P);
				
				type = (ParForProgramBlock.USE_RANGE_TASKS_IF_USEFUL && K>3 ) ? 
						   TaskType.ITERATION_RANGE : TaskType.ITERATION_SET;
				
				//for each logical processor
				for( int j=0; j<P; j++ )
				{
					if( i > lTo ) //no more iterations
						break;
					
					//create new task and add to list of tasks
					Task lTask = new Task( type );
					
					// add iterations to task 
					if( type == TaskType.ITERATION_SET ) 
					{
						//value based tasks
						for( int k=0; k<K && i<=lTo; k++, i+=lIncr )
						{
							lTask.addIteration(new IntObject(_iterVarName, i));				
						}				
					}
					else 
					{
						//determine end of task
						int to = Math.min( i+(K-1)*lIncr, lTo );
						
						//range based tasks
						lTask.addIteration(new IntObject(_iterVarName, i));	    //from
						lTask.addIteration(new IntObject(_iterVarName, to));    //to
						lTask.addIteration(new IntObject(_iterVarName, lIncr));	//increment
						
						i = to + lIncr;
					}
					
					//add task to queue (after all iteration added for preventing raise conditions)
					queue.enqueueTask( lTask );
					numCreatedTasks++;
				}
			}
			
			// mark end of task input stream
			queue.closeInput();	
		}
		catch(Exception ex)
		{
			throw new DMLRuntimeException(ex);
		}
	
		return numCreatedTasks;
	}
	
	
	/**
	 * Computes the task size (number of iterations per task) for the next numThreads tasks 
	 * given the number of remaining iterations R, and the number of Threads.
	 * 
	 * NOTE: x can be set to different values, but the original paper argues for x=2.
	 * 
	 * @param R
	 * @return
	 */
	protected int determineNextBatchSize(int R, int P) 
	{
		int x = 2;
		int K = (int) Math.ceil((double)R / ( x * P )); //NOTE: round creates more tasks
		
		if( K < 1 ) //account for rounding errors
			K = 1;
		
		return K;
	}
	
}
