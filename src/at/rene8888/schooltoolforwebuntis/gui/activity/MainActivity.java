package at.rene8888.schooltoolforwebuntis.gui.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import at.rene8888.schooltoolforwebuntis.R;
import at.rene8888.schooltoolforwebuntis.data.ApplicationClass;
import at.rene8888.schooltoolforwebuntis.gui.adapter.SectionsPagerAdapter;
import at.rene8888.schooltoolforwebuntis.gui.listener.PageChangeListener;
import at.rene8888.schooltoolforwebuntis.gui.listener.TabListener;

public class MainActivity extends FragmentActivity {

	private SectionsPagerAdapter mSectionsPagerAdapter;

	private ViewPager mViewPager;
	private ApplicationClass app;
	private static MainActivity MAIN_ACTIVITY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MAIN_ACTIVITY = this;
		super.onCreate(savedInstanceState);

		app = (ApplicationClass) this.getApplication();

		if (app.getUsername() == null || app.getUsername().equals("") || app.getPassword() == null || app.getPassword().equals("")) {
			goToWelcome();
		} else {
			setContentView(R.layout.main_activity);

			ActionBar actionBar = getActionBar();
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayShowTitleEnabled(false);

			this.mSectionsPagerAdapter = new SectionsPagerAdapter(this.getSupportFragmentManager());
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mSectionsPagerAdapter);
			mViewPager.setOnPageChangeListener(new PageChangeListener(actionBar));
			for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
				actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(new TabListener(this.mViewPager)));
			}
			updateScreenLock();
		}

	}

	public void goToWelcome() {
		Intent i = new Intent(this, WelcomeActivity.class);
		startActivity(i);
	}

	public void updateScreenLock() {
		if (app.getPrefs().getBoolean("autoLockOff", false)) {
			getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public static MainActivity getMainActivity() {
		return MAIN_ACTIVITY;
	}

}
