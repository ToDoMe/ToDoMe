package com.todome;

import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.api.client.auth.oauth2.draft10.AccessProtectedResource;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.tasks.Tasks;

public class GoogleTasks {

	String AUTH_TOKEN_TYPE = "Manage your tasks";

	void getGoogleAccounts(Activity activity) {
		AccountManager accountManager = AccountManager.get(activity);
		Account[] accounts = accountManager.getAccountsByType("com.google");

		for (int i = 0; i < accounts.length; i++) {
			Log.i("Google Accounts " + i, "Name: " + accounts[i].name);
			Log.i("Google Accounts " + i, "Type: " + accounts[i].name);

			accountManager.getAuthToken(accounts[i], AUTH_TOKEN_TYPE, null, activity, new AccountManagerCallback<Bundle>() {
				public void run(AccountManagerFuture<Bundle> future) {
					try {
						// If the user has authorized your application to use
						// the tasks API
						// a token is available.
						String token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
						// Now you can use the Tasks API...
						useTasksAPI(token);
					} catch (OperationCanceledException e) {
						// TODO: The user has denied you access to the API, you
						// should handle that
					} catch (Exception e) {
						Log.e("Google tasks", e.getStackTrace().toString());
					}
				}
			}, null);
		}
	}

	void useTasksAPI(String accessToken) {
		// Setting up the Tasks API Service
		HttpTransport transport = AndroidHttp.newCompatibleTransport();
		AccessProtectedResource accessProtectedResource = new GoogleAccessProtectedResource(accessToken);
		Tasks service = new Tasks(transport, accessProtectedResource, new JacksonFactory());
		//service.accessKey = "AIzaSyDRPAPhboFZI7Knmb1Husoyjkuj_ojRodA";
		service.setApplicationName("Google-TasksSample/1.0");

		// Getting all the Task lists
		//List taskLists = service.tasklists.list().execute().getItems();

		// Getting the list of tasks in the default task list
		//List tasks = service.tasks.list("@default").execute().getItems();

		// Add a task to the default task list
		//Task task = new Task();
		//task.title = "New Task";
		//task.notes = "Please complete me";
		//task.due = "2010-10-15T12:00:00.000Z";
		//Task result = service.tasks.insert("@default", task).execute();

	}

}
