package nl.esciencecenter.e3dchem.sstea;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class ScoreNodeDialog extends DefaultNodeSettingsPane {

	public ScoreNodeDialog() {
		super();
		addDialogComponent(new DialogComponentColumnNameSelection(
				new SettingsModelString(ScoreNodeModel.CFGKEY_IDENTIFIERCOLUMNNAME, null),
				"Column with sequence identifier", 0, StringValue.class));
		addDialogComponent(new DialogComponentColumnNameSelection(
				new SettingsModelString(ScoreNodeModel.CFGKEY_SEQUENCECOLUMNNAME, null), "Column with sequence", 0,
				StringValue.class));
		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(ScoreNodeModel.CFGKEY_INCLUDEGAPS, false), "Include gaps"));
	}

}
