package tpsa.dialogs.filters;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Filtr rozszerzeń kodów źródłowych BrainFucka
 * 
 * @author Duga Eye
 * 
 */
public class BFFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if (f.isDirectory())
			return true;
		if (f.getName().endsWith(".bf"))
			return true;
		return false;
	}

	@Override
	public String getDescription() {
		return "BrainFuck source code";
	}

}
