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
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class ScoreNodeModel extends NodeModel {
    public static final String CFGKEY_IDENTIFIERCOLUMNNAME = "IdentifierColumn";
    public static final String CFGKEY_SEQUENCECOLUMNNAME = "SequenceColumn";

    private final SettingsModelString m_identifierColumnName = new SettingsModelString(CFGKEY_IDENTIFIERCOLUMNNAME, null);
    private final SettingsModelString m_sequenceColumnName = new SettingsModelString(CFGKEY_SEQUENCECOLUMNNAME, null);

    public ScoreNodeModel() {
        super(2, 1);
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        DataTableSpec outputSpec = outputSpec();
        BufferedDataContainer container = exec.createDataContainer(outputSpec);

        BufferedDataTable seqTable = inData[0];
        Map<String, String> sequences = prepareSequences(seqTable);

        BufferedDataTable smTable = inData[1];
        Set<String> subfamily_members = prepareSubfamilyMembers(smTable);

        Scorer scorer = new Scorer();
        List<Score> scores = scorer.scoreit(sequences, subfamily_members);

        addScores2Container(container, scores);

        container.close();
        BufferedDataTable out = container.getTable();
        return new BufferedDataTable[] { out };
    }

    public void addScores2Container(BufferedDataContainer container, List<Score> scores) {
        for (Score score : scores) {
            RowKey key = new RowKey(String.valueOf(score.getSeqPos()));
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
            String sm = ((StringCell) smRow.getCell(smSeqIdIndex)).getStringValue();
            subfamily_members.add(sm);
        }
        return subfamily_members;
    }

    public Map<String, String> prepareSequences(BufferedDataTable seqTable) {
        Map<String, String> sequences = new HashMap<String, String>();
        int seqIdIndex = seqTable.getDataTableSpec().findColumnIndex(m_identifierColumnName.getStringValue());
        int seqIndex = seqTable.getDataTableSpec().findColumnIndex(m_sequenceColumnName.getStringValue());
        for (DataRow seqrow : seqTable) {
            String key = ((StringCell) seqrow.getCell(seqIdIndex)).getStringValue();
            String value = ((StringCell) seqrow.getCell(seqIndex)).getStringValue();
            sequences.put(key, value);
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
    protected void loadInternals(File nodeInternDir, ExecutionMonitor exec) throws IOException, CanceledExecutionException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void saveInternals(File nodeInternDir, ExecutionMonitor exec) throws IOException, CanceledExecutionException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) {
        m_identifierColumnName.saveSettingsTo(settings);
        m_sequenceColumnName.saveSettingsTo(settings);
    }

    @Override
    protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
        m_identifierColumnName.validateSettings(settings);
        m_sequenceColumnName.validateSettings(settings);
    }

    @Override
    protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
        m_identifierColumnName.loadSettingsFrom(settings);
        m_sequenceColumnName.loadSettingsFrom(settings);
    }

    @Override
    protected void reset() {
        // TODO Auto-generated method stub

    }

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return new DataTableSpec[] { outputSpec() };
    }

}
