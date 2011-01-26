/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.github;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.springframework.security.oauth.client.InterceptorCallingRestTemplate;
import org.springframework.security.oauth.client.oauth2.OAuth2Draft8ClientRequestInterceptor;
import org.springframework.web.client.RestOperations;

/**
 * <p>
 * The central class for interacting with TripIt.
 * </p>
 * 
 * <p>
 * TripIt operations require OAuth 1 authentication. Therefore TripIt template
 * must be given the minimal amount of information required to sign requests to
 * the TripIt API with an OAuth <code>Authorization</code> header.
 * </p>
 * 
 * @author Craig Walls
 */
public class GitHubTemplate implements GitHubOperations {

	RestOperations restOperations;

	/**
	 * Constructs a GitHubTemplate with the minimal amount of information
	 * required to sign requests with an OAuth <code>Authorization</code>
	 * header.
	 * 
	 * @param accessToken
	 *            An access token granted to the application after OAuth
	 *            authentication.
	 */
	public GitHubTemplate(String accessToken) {
		// RestTemplate restTemplate = new RestTemplate();
		// temporarily use InterceptorCallingRestTemplate instead of a regular
		// RestTemplate. This is to simulate the work that Arjen is doing for
		// SPR-7494. Once Arjen's finished, a regular RestTemplate should be
		// used with the interceptors registered appropriately.
		InterceptorCallingRestTemplate restTemplate = new InterceptorCallingRestTemplate();
		restTemplate.addInterceptor(new OAuth2Draft8ClientRequestInterceptor(accessToken));
		restOperations = restTemplate;
	}

	public String getProfileId() {
		return getUserProfile().getUsername();
	}

	@SuppressWarnings("unchecked")
	public GitHubUserProfile getUserProfile() {
		Map<String, ?> result = restOperations.getForObject(PROFILE_URL, Map.class);
		Map<String, ?> user = (Map<String, String>) result.get("user");

		Long gitHubId = Long.valueOf(String.valueOf(user.get("id")));
		String username = String.valueOf(user.get("login"));
		String name = String.valueOf(user.get("name"));
		String location = user.get("location") != null ? String.valueOf(user.get("location")) : null;
		String company = user.get("company") != null ? String.valueOf(user.get("company")) : null;
		String blog = user.get("blog") != null ? String.valueOf(user.get("blog")) : null;
		String email = user.get("email") != null ? String.valueOf(user.get("email")) : null;
		Date createdDate = toDate(String.valueOf(user.get("created_at")), dateFormat);

		return new GitHubUserProfile(gitHubId, username, name, location, company, blog, email, createdDate);
	}

	public String getProfileUrl() {
		return "https://github.com/" + getProfileId();
	}
	
	private Date toDate(String dateString, DateFormat dateFormat) {
		try {
			return dateFormat.parse(dateString);
		} catch (ParseException e) {
			return null;
		}
	}

	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z", Locale.ENGLISH);

	static final String PROFILE_URL = "https://github.com/api/v2/json/user/show";
}
