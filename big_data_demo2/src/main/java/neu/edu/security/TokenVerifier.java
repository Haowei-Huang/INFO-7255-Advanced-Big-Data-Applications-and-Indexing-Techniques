package neu.edu.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

@Service
public class TokenVerifier {
	public boolean authorize(String idTokenString) throws GeneralSecurityException, IOException {
		if(idTokenString==null || idTokenString.isBlank()) {
			return false;
		}
		final HttpTransport transport = new NetHttpTransport();
		final JsonFactory jsonFactory = new GsonFactory();
		final String CLIENT_ID = "173758261983-pbkkd6eulht8318lhbi7ssq1gvmogj5v.apps.googleusercontent.com";

		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
				// Specify the CLIENT_ID of the app that accesses the backend:
				.setAudience(Collections.singletonList(CLIENT_ID))
				// Or, if multiple clients access the backend:
				// .setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
				.build();

		// (Receive idTokenString by HTTPS POST)

		GoogleIdToken idToken = verifier.verify(idTokenString);
		if (idToken != null) {
			Payload payload = idToken.getPayload();
			
			// Print user identifier
			String userId = payload.getSubject();
			System.out.println("User ID: " + userId);
			System.out.println("Token verification success");
//
//			// Get profile information from payload
//			String email = payload.getEmail();
//			boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
//			String name = (String) payload.get("name");
//			String pictureUrl = (String) payload.get("picture");
//			String locale = (String) payload.get("locale");
//			String familyName = (String) payload.get("family_name");
//			String givenName = (String) payload.get("given_name");
			return true;

		} else {
			System.out.println("Invalid ID token.");
			throw new IOException("idToken is invalid");
		}
	}
}
