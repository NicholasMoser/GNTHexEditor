package com.github.hexeditor;

import java.io.File;
import javax.swing.filechooser.FileFilter;

class BinFileFilter extends FileFilter {

	public boolean accept(File var1) {
		return var1.canWrite();
	}

	public String getDescription() {
		return "";
	}
}
