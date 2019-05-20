package org.mtransit.publisher;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.Apk;
import com.google.api.services.androidpublisher.model.ApksListResponse;
import com.google.api.services.androidpublisher.model.AppEdit;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

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
		listAPKs(pkgName);
		System.out.print("\nPublishing... DONE");
	}

	private static void listAPKs(@Nonnull String pkgName) {
		try {
			Preconditions.checkArgument(!Strings.isNullOrEmpty(pkgName),
					"ApplicationConfig.PACKAGE_NAME cannot be null or empty!");

			// Create the API service.
			final AndroidPublisher service = AndroidPublisherHelper.init(
					AppConfig.APPLICATION_NAME,
					AppConfig.SERVICE_ACCOUNT_EMAIL
			);
			final AndroidPublisher.Edits edits = service.edits();

			// Create a new edit to make changes.
			AndroidPublisher.Edits.Insert editRequest = edits
					.insert(pkgName,
							null /* no content */);
			AppEdit appEdit = editRequest.execute();

			// Get a list of apks.
			ApksListResponse apksResponse = edits
					.apks()
					.list(
							pkgName,
							appEdit.getId()
					)
					.execute();

			// Print the apk info.
			for (Apk apk : apksResponse.getApks()) {
				System.out.printf("\nVersion: %d - Binary sha1: %s", apk.getVersionCode(), apk.getBinary().getSha1());
			}
		} catch (IOException | GeneralSecurityException ex) {
			System.out.printf("\nException was thrown while updating listing for '%s'!\n", pkgName);
			ex.printStackTrace();
			System.exit(-1);
		}
	}
}
