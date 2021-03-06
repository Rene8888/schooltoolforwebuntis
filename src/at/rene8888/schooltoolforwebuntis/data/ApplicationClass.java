package at.rene8888.schooltoolforwebuntis.data;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import at.rene8888.schooltoolforwebuntis.data.webuntis.network.WebUntisRequests;

public class ApplicationClass extends Application {

	private static ApplicationClass app;

	private URL url;
	private String username;
	private String password;
	private int delay;
	private SharedPreferences prefs;
	private Editor editor;
	private WebUntisRequests req;

	public ApplicationClass() {
		app = this;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.editor = prefs.edit();
		this.update();
	}

	public void update() {
		this.username = prefs.getString("username", null);
		this.password = prefs.getString("password", null);
		this.delay = prefs.getInt("delay", 0);
		try {
			this.url = new URL(prefs.getString("url", null));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void saveChanges() {
		editor.putString("url", this.getURL().toString());
		editor.putString("username", this.getUsername());
		editor.putString("password", this.getPassword());
		editor.putInt("delay", this.getDelay());
		editor.commit();
	}

	public SharedPreferences getPrefs() {
		return prefs;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getDelay() {
		return this.delay;
	}

	public URL getURL() {
		return this.url;
	}

	public int getClockUpdateTime() {
		return 1000;
	}

	public WebUntisRequests getWebUntisRequests() {
		if (req == null) {
			this.req = new WebUntisRequests(this);
		}
		if (this.getPassword() != null && this.getUsername() != null) {
			this.req.getSessionID();
		}
		return this.req;
	}

	public static ApplicationClass getApplication() {
		return app;
	}

	public void setURL(URL url) {
		this.url = url;

	}
}
