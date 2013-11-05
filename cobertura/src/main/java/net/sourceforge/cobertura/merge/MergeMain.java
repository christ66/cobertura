/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Joakim Erdfelt
 * Copyright (C) 2005 Mark Sinke
 * Copyright (C) 2005 Grzegorz Lukasik
 *
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package net.sourceforge.cobertura.merge;

import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.dsl.Cobertura;
import net.sourceforge.cobertura.util.CommandLineBuilder;
import net.sourceforge.cobertura.util.Header;

import java.io.File;

public class MergeMain {

	public MergeMain(String[] args) {
		ArgumentsBuilder builder = new ArgumentsBuilder();

		String baseDir = null;

		// Go through all the parameters
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--datafile")) {
				builder.setDataFile(args[++i]);
			} else if (args[i].equals("--basedir")) {
				baseDir = args[++i];
				builder.setBaseDirectory(baseDir);
			} else {
				builder.addFileToMerge(new File(baseDir, args[i])
						.getAbsolutePath());
			}
		}

		new Cobertura(builder.build()).merge().saveProjectData();
	}

	public static void main(String[] args) {
		Header.print(System.out);

		try {
			args = CommandLineBuilder.preprocessCommandLineArguments(args);
		} catch (Exception ex) {
			System.err.println("Error: Cannot process arguments: "
					+ ex.getMessage());
			System.exit(1);
		}
		new MergeMain(args);
	}
}
