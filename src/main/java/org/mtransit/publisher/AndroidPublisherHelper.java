package org.mtransit.publisher;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class AndroidPublisherHelper {

	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	private static final String SRC_RESOURCES_KEY_P12 = "src/resources/key.p12";
	private static final String RESOURCES_CLIENT_SECRETS_JSON = "/resources/client_secrets.json";

	private static final String DATA_STORE_SYSTEM_PROPERTY = "user.home";
	private static final String DATA_STORE_FILE = ".store/android_publisher_api";
	private static final File DATA_STORE_DIR =
			new File(System.getProperty(DATA_STORE_SYSTEM_PROPERTY), DATA_STORE_FILE);

	private static final String INST_APP_USER_ID = "user";

	private static HttpTransport HTTP_TRANSPORT;

	@SuppressWarnings("unused")
	@Nonnull
	protected static AndroidPublisher init(@Nonnull String applicationName) throws Exception {
		return init(applicationName, null);
	}

	@Nonnull
	protected static AndroidPublisher init(@Nonnull String applicationName,
										   @Nullable String serviceAccountEmail) throws IOException, GeneralSecurityException {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(applicationName),
				"applicationName cannot be null or empty!");
		// Authorization.
		newTrustedTransport();
		Credential credential;
		if (serviceAccountEmail == null || serviceAccountEmail.isEmpty()) {
			credential = authorizeWithInstalledApplication();
		} else {
			credential = authorizeWithServiceAccount(serviceAccountEmail);
		}
		// Set up and return API client.
		return new AndroidPublisher.Builder(
				HTTP_TRANSPORT,
				JSON_FACTORY,
				credential)
				.setApplicationName(applicationName)
				.build();
	}

	@Nonnull
	private static Credential authorizeWithServiceAccount(@Nonnull String serviceAccountEmail) throws GeneralSecurityException, IOException {
		System.out.printf("\nAuthorizing using Service Account: %s", serviceAccountEmail);
		// Build service account credential.
		return new GoogleCredential.Builder()
				.setTransport(HTTP_TRANSPORT)
				.setJsonFactory(JSON_FACTORY)
				.setServiceAccountId(serviceAccountEmail)
				.setServiceAccountScopes(
						Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER))
				.setServiceAccountPrivateKeyFromP12File(new File(SRC_RESOURCES_KEY_P12))
				.build();
	}

	@Nonnull
	private static Credential authorizeWithInstalledApplication() throws IOException {
		System.out.print("\nAuthorizing using installed application");
		// load client secrets
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
				JSON_FACTORY,
				new InputStreamReader(
						AndroidPublisherHelper.class
								.getResourceAsStream(RESOURCES_CLIENT_SECRETS_JSON)));
		// Ensure file has been filled out.
		checkClientSecretsFile(clientSecrets);
		FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
		// set up authorization code flow
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
				.Builder(HTTP_TRANSPORT,
				JSON_FACTORY, clientSecrets,
				Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER))
				.setDataStoreFactory(dataStoreFactory).build();
		// authorize
		return new AuthorizationCodeInstalledApp(
				flow, new LocalServerReceiver()).authorize(INST_APP_USER_ID);
	}

	private static void checkClientSecretsFile(@Nonnull GoogleClientSecrets clientSecrets) {
		if (clientSecrets.getDetails().getClientId().startsWith("[[INSERT")
				|| clientSecrets.getDetails().getClientSecret().startsWith("[[INSERT")) {
			System.out.printf("\nEnter Client ID and Secret from APIs console into %s.", RESOURCES_CLIENT_SECRETS_JSON);
			System.exit(-1);
		}
	}

	private static void newTrustedTransport() throws GeneralSecurityException, IOException {
		if (HTTP_TRANSPORT == null) {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		}
	}
}
