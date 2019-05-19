package org.mtransit.publisher;

import java.util.Arrays;

import javax.annotation.Nullable;

public class PlayPublisher {

	public static void main(@Nullable String[] args) {
		System.out.print("\nPublishing...");
		if (args == null || args.length != 2) {
			System.out.printf("\nUnexpected number of args! (args:%s)", Arrays.toString(args));
			System.exit(-1);
		}
		String pkgName = args[0];
		System.out.printf("\npkgName: %s", pkgName);
		String apkFilePath = args[1];
		System.out.printf("\napkFilePath: %s", apkFilePath);
		System.out.print("\nPublishing... DONE");
	}
}
