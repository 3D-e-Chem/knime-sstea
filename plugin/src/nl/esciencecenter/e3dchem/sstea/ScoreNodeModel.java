package nl.esciencecenter.e3dchem.sstea;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class ScoreNodeModel extends NodeModel {
	public static final String CFGKEY_IDENTIFIERCOLUMNNAME = "IdentifierColumn";
	public static final String CFGKEY_SEQUENCECOLUMNNAME = "SequenceColumn";
	public static final String CFGKEY_INCLUDEGAPS = "IncludeGaps";

	private final SettingsModelString m_identifierColumnName = new SettingsModelString(CFGKEY_IDENTIFIERCOLUMNNAME,
			null);
	private final SettingsModelString m_sequenceColumnName = new SettingsModelString(CFGKEY_SEQUENCECOLUMNNAME, null);
	private final SettingsModelBoolean m_includeGaps = new SettingsModelBoolean(ScoreNodeModel.CFGKEY_INCLUDEGAPS,
			false);

	public ScoreNodeModel() {
		super(2, 1);
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		DataTableSpec outputSpec = outputSpec();
		BufferedDataContainer container = exec.createDataContainer(outputSpec);

		BufferedDataTable seqTable = inData[0];
		BufferedDataTable smTable = inData[1];
		if (seqTable.size() > 0 && smTable.size() > 0) {
			Map<String, String> sequences = prepareSequences(seqTable);

			exec.setProgress(0.1);
			exec.checkCanceled();

			Set<String> subfamily_members = prepareSubfamilyMembers(smTable);

			exec.setProgress(0.2);
			exec.checkCanceled();

			Scorer scorer = new Scorer(m_includeGaps.getBooleanValue());
			List<Score> scores = scorer.scoreit(sequences, subfamily_members);

			exec.setProgress(0.9);
			exec.checkCanceled();

			addScores2Container(container, scores);

			exec.setProgress(1.0);
		}
		container.close();

		BufferedDataTable out = container.getTable();
		return new BufferedDataTable[] { out };
	}

	public void addScores2Container(BufferedDataContainer container, List<Score> scores) {
		long rowIndex = 0;
		for (Score score : scores) {
			RowKey key = RowKey.createRowKey(rowIndex++);
			DataCell[] cells = new DataCell[6];
			cells[0] = new IntCell(score.getSeqPos());
			cells[1] = new DoubleCell(score.getScore());
			cells[2] = new DoubleCell(score.getEntropyInside());
			cells[3] = new DoubleCell(score.getEntropyOutside());
			cells[4] = new IntCell(score.getVariabilityInside());
			cells[5] = new IntCell(score.getVariabilityOutside());
			container.addRowToTable(new DefaultRow(key, cells));
		}
	}

	public Set<String> prepareSubfamilyMembers(BufferedDataTable smTable) {
		Set<String> subfamily_members = new HashSet<String>();
		int smSeqIdIndex = smTable.getDataTableSpec().findColumnIndex(m_identifierColumnName.getStringValue());
		for (DataRow smRow : smTable) {
			if (smRow.getCell(smSeqIdIndex).isMissing()) {
				// Ignore rows with missing values
			} else {
				String sm = ((StringValue) smRow.getCell(smSeqIdIndex)).getStringValue();
				subfamily_members.add(sm);
			}
		}
		return subfamily_members;
	}

	public Map<String, String> prepareSequences(BufferedDataTable seqTable) {
		Map<String, String> sequences = new HashMap<String, String>();
		int seqIdIndex = seqTable.getDataTableSpec().findColumnIndex(m_identifierColumnName.getStringValue());
		int seqIndex = seqTable.getDataTableSpec().findColumnIndex(m_sequenceColumnName.getStringValue());
		for (DataRow seqrow : seqTable) {
			if (seqrow.getCell(seqIdIndex).isMissing() || seqrow.getCell(seqIndex).isMissing()) {
				// Ignore rows with missing values
			} else {
				String key = ((StringValue) seqrow.getCell(seqIdIndex)).getStringValue();
				String value = ((StringValue) seqrow.getCell(seqIndex)).getStringValue();
				sequences.put(key, value);
			}
		}
		return sequences;
	}

	public DataTableSpec outputSpec() {
		DataColumnSpec[] allColSpecs = new DataColumnSpec[6];
		allColSpecs[0] = new DataColumnSpecCreator("Sequence number", IntCell.TYPE).createSpec();
		allColSpecs[1] = new DataColumnSpecCreator("Score", DoubleCell.TYPE).createSpec();
		allColSpecs[2] = new DataColumnSpecCreator("Entropy inside", DoubleCell.TYPE).createSpec();
		allColSpecs[3] = new DataColumnSpecCreator("Entropy outside", DoubleCell.TYPE).createSpec();
		allColSpecs[4] = new DataColumnSpecCreator("Variability inside", IntCell.TYPE).createSpec();
		allColSpecs[5] = new DataColumnSpecCreator("Variability outside", IntCell.TYPE).createSpec();
		DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
		return outputSpec;
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_identifierColumnName.saveSettingsTo(settings);
		m_sequenceColumnName.saveSettingsTo(settings);
		m_includeGaps.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		m_identifierColumnName.validateSettings(settings);
		m_sequenceColumnName.validateSettings(settings);
		m_includeGaps.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		m_identifierColumnName.loadSettingsFrom(settings);
		m_sequenceColumnName.loadSettingsFrom(settings);
		m_includeGaps.validateSettings(settings);
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		DataTableSpec sequenceSpec = inSpecs[0];
		int idColumnIndex = sequenceSpec.findColumnIndex(m_identifierColumnName.getStringValue());
		if (idColumnIndex < 0) {
			throw new InvalidSettingsException("No valid identifier column selected, require a String column");
		}
		int seqColumnIndex = sequenceSpec.findColumnIndex(m_sequenceColumnName.getStringValue());
		if (seqColumnIndex < 0) {
			throw new InvalidSettingsException("No valid sequence column selected, require a String column");
		}
		if (idColumnIndex == seqColumnIndex) {
			throw new InvalidSettingsException(
					"Sequence identifier column and sequence column should not be the same column");
		}
		DataTableSpec subfamSpec = inSpecs[1];
		int subfamColumnIndex = subfamSpec.findColumnIndex(m_identifierColumnName.getStringValue());
		if (subfamColumnIndex < 0) {
			throw new InvalidSettingsException(
					"No valid identifier column selected in subfamily port, require a String column");
		}
		return new DataTableSpec[] { outputSpec() };
	}

}
