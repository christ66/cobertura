package net.sourceforge.cobertura.merge;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.dsl.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Set;

public class MergeProjectDataFilesTask {
	private static final Logger logger = LoggerFactory
			.getLogger(MergeProjectDataFilesTask.class);

	public void mergeProjectDataFiles(Arguments arguments,
			ProjectData projectData) {
		Set<File> filesToMerge = arguments.getFilesToMerge();

		if (filesToMerge.isEmpty()) {
			logger.error("No files were specified for merging.");
		}

		// Merge everything
		for (File newDataFile : filesToMerge) {
			ProjectData projectDataNew = CoverageDataFileHandler
					.loadCoverageData(newDataFile);

			if (projectDataNew != null)
				projectData.merge(projectDataNew);
		}
	}
}
