package dml.lops;

import dml.lops.LopProperties.ExecLocation;
import dml.lops.LopProperties.ExecType;
import dml.lops.OutputParameters.Format;
import dml.lops.compile.JobType;
import dml.parser.DataIdentifier;
import dml.parser.Expression.*;

/**
 * <p>Defines a Rand-LOP.</p>
 * 
 * @author schnetter
 */
public class Rand extends Lops
{
	/** minimum of the random values */
	private double minValue;
	/** maximum of the random values */
	private double maxValue;
	/** sparsity of the random object */
	private double sparsity;
	/** probability density function which is used to produce the sparsity */
	private String probabilityDensityFunction;
	/** base dir for rand input */
	private String baseDir;
	
	
	/**
	 * <p>Creates a new Rand-LOP. The target identifier has to hold the dimensions of the new random object.</p>
	 * 
	 * @param id target identifier
	 * @param minValue minimum of the random values
	 * @param maxValue maximum of the random values
	 * @param sparsity sparsity of the random object
	 * @param probabilityDensityFunction probability density function
	 */	
	public Rand(DataIdentifier id, double minValue, double maxValue, double sparsity, String probabilityDensityFunction, String baseDir, DataType dt, ValueType vt)
	{
		super(Type.RandLop, dt, vt);
		init(id, minValue, maxValue, sparsity, probabilityDensityFunction, baseDir, ExecType.MR);
	}

	public Rand(DataIdentifier id, double minValue, double maxValue, double sparsity, String probabilityDensityFunction, String baseDir, DataType dt, ValueType vt, ExecType et) {
		super(Type.RandLop, dt, vt);
		init(id, minValue, maxValue, sparsity, probabilityDensityFunction, baseDir, et);
	}

	public void init(DataIdentifier id, double minValue, double maxValue, double sparsity, String probabilityDensityFunction, String baseDir, ExecType et)
	{
		this.getOutputParameters().setFormat(Format.BINARY);
		this.getOutputParameters().blocked_representation = true;
		this.getOutputParameters().num_rows = id.getDim1();
		this.getOutputParameters().num_cols = id.getDim2();
		this.getOutputParameters().num_rows_per_block = id.getRowsInBlock();
		this.getOutputParameters().num_cols_per_block = id.getColumnsInBlock();
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.sparsity = sparsity;
		this.probabilityDensityFunction = probabilityDensityFunction;
		this.baseDir = baseDir;
		
		boolean breaksAlignment = false;
		boolean aligner = false;
		boolean definesMRJob;
		
		if ( et == ExecType.MR ) {
			definesMRJob = true;
			lps.addCompatibility(JobType.RAND);
			this.lps.setProperties( et, ExecLocation.Map, breaksAlignment, aligner, definesMRJob);
		}
		else {
			definesMRJob = false;
			lps.addCompatibility(JobType.INVALID);
			this.lps.setProperties( et, ExecLocation.ControlProgram, breaksAlignment, aligner, definesMRJob);
		}
	}

	@Override 
	public String getInstructions(String output) {
		StringBuilder inst = new StringBuilder();
		inst.append(getExecType() + Lops.OPERAND_DELIMITOR);
		inst.append("Rand");
		inst.append(OPERAND_DELIMITOR + "rows=" + this.getOutputParameters().num_rows);
		inst.append(OPERAND_DELIMITOR + "cols=" + this.getOutputParameters().num_cols);
		inst.append(OPERAND_DELIMITOR + "min=" + minValue);
		inst.append(OPERAND_DELIMITOR + "max=" + maxValue);
		inst.append(OPERAND_DELIMITOR + "sparsity=" + sparsity);
		inst.append(OPERAND_DELIMITOR + "pdf=" + probabilityDensityFunction);
		inst.append(OPERAND_DELIMITOR + "dir=" + baseDir);
		inst.append(OPERAND_DELIMITOR + output + DATATYPE_PREFIX + this.get_dataType() + VALUETYPE_PREFIX + this.get_valueType());
		return inst.toString();
	}
	
	@Override
	public String getInstructions(int inputIndex, int outputIndex)
	{
		StringBuilder inst = new StringBuilder();
		inst.append(getExecType() + Lops.OPERAND_DELIMITOR);
		inst.append("Rand");
		inst.append(OPERAND_DELIMITOR + inputIndex);
		inst.append(OPERAND_DELIMITOR + outputIndex);
		inst.append(OPERAND_DELIMITOR + "rows=" + this.getOutputParameters().num_rows);
		inst.append(OPERAND_DELIMITOR + "cols=" + this.getOutputParameters().num_cols);
		inst.append(OPERAND_DELIMITOR + "min=" + minValue);
		inst.append(OPERAND_DELIMITOR + "max=" + maxValue);
		inst.append(OPERAND_DELIMITOR + "sparsity=" + sparsity);
		inst.append(OPERAND_DELIMITOR + "pdf=" + probabilityDensityFunction);
		inst.append(OPERAND_DELIMITOR + "dir=" + baseDir);
		return inst.toString();
		
		/*
		StringBuilder sb = new StringBuilder();
		sb.append("Rand " + inputIndex + " " + outputIndex);
		sb.append(" min=" + minValue);
		sb.append(" max=" + maxValue);
		sb.append(" sparsity=" + sparsity);
		sb.append(" pdf=" + probabilityDensityFunction);
		return sb.toString();
		*/
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Rand");
		sb.append(" ; num_rows=" + this.getOutputParameters().getNum_rows());
		sb.append(" ; num_cols=" + this.getOutputParameters().getNum_cols());
		sb.append(" ; num_rows_per_block=" + this.getOutputParameters().getNum_rows_per_block());
		sb.append(" ; num_cols_per_block=" + this.getOutputParameters().getNum_cols_per_block());
		sb.append(" ; format=" + this.getOutputParameters().getFormat());
		sb.append(" ; blocked=" + this.getOutputParameters().isBlocked_representation());
		sb.append(" ; min=" + this.minValue);
		sb.append(" ; max=" + this.maxValue);
		sb.append(" ; sparsity=" + this.sparsity);
		sb.append(" ; pdf=" + this.probabilityDensityFunction);
		sb.append(" ; dir=" + this.baseDir);
		return sb.toString();
	}
}
