/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.ActivityResult;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import java.util.ArrayList;
import java.util.List;

import in.co.rajkumaar.amritarepo.BuildConfig;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.about.activities.AboutActivity;
import in.co.rajkumaar.amritarepo.aums.activities.LoginActivity;
import in.co.rajkumaar.amritarepo.curriculum.CurriculumActivity;
import in.co.rajkumaar.amritarepo.downloads.DownloadsActivity;
import in.co.rajkumaar.amritarepo.downloads.FTPActivity;
import in.co.rajkumaar.amritarepo.examschedule.ExamCategoryActivity;
import in.co.rajkumaar.amritarepo.faq.ExamsFAQActivity;
import in.co.rajkumaar.amritarepo.helpers.ClearCache;
import in.co.rajkumaar.amritarepo.helpers.EncryptedPrefsUtils;
import in.co.rajkumaar.amritarepo.helpers.Utils;
import in.co.rajkumaar.amritarepo.news.NewsActivity;
import in.co.rajkumaar.amritarepo.notifications.NotificationsActivity;
import in.co.rajkumaar.amritarepo.opac.OPACHomeActivity;
import in.co.rajkumaar.amritarepo.papers.SemesterActivity;
import in.co.rajkumaar.amritarepo.study_materials.StudyMaterialsActivity;
import in.co.rajkumaar.amritarepo.timetable.AcademicTimetableActivity;
import in.co.rajkumaar.amritarepo.timetable.FacultyTimetableActivity;
import in.co.rajkumaar.amritarepo.timings.TimingsHomeActivity;
import in.co.rajkumaar.amritarepo.wifistatus.WifiStatusActivity;

public class LaunchingActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean doubleBackToExitPressedOnce = false;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppUpdateManager appUpdateManager;
    private Task<AppUpdateInfo> appUpdateInfoTask;
    private InstallStateUpdatedListener listener;
    private int APP_UPDATE_REQUEST_CODE = 100;
    private int DAYS_FOR_IMMEDIATE_UPDATE = 1;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launching);
        FirebaseApp.initializeApp(this);
        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        Iconify.with(new FontAwesomeModule());
        Utils.clearUnsafeCredentials(this);
        final SharedPreferences pref = getSharedPreferences("user", Context.MODE_PRIVATE);
        if (ContextCompat.checkSelfPermission(LaunchingActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(LaunchingActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {
            new ClearCache().clear(this);
            if (pref.getBoolean("first", true)) {
                AboutActivity.showDisclaimer(this);
                SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                editor.putBoolean("first", false);
                editor.apply();
            }
        }

        if (pref.getInt("visit", 0) >= 3 && pref.getBoolean("ftp-dialog", true)) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.ftp_dialog);
            dialog.findViewById(R.id.okay).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), FTPActivity.class));
                }
            });
            dialog.show();
            pref.edit().putBoolean("ftp-dialog", false).apply();
        } else if (pref.getInt("visit", 0) <= 7) {
            pref.edit().putInt("visit", pref.getInt("visit", 0) + 1).apply();
        }


        //Subscribing to a topic to receive FCM Topic messages
        if (!pref.getBoolean(getString(R.string.subsribedToTopic), false)) {
            FirebaseMessaging.getInstance().subscribeToTopic("general")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                            if (task.isSuccessful()) {
                                pref.edit().putBoolean(getString(R.string.subsribedToTopic), true).apply();
                            }
                        }
                    });
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = findViewById(R.id.nav_view);
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_home));
        navigationView.setNavigationItemSelectedListener(this);
        TextView versionName = navigationView.getHeaderView(0).findViewById(R.id.versioncode);
        versionName.setText("Version ".concat(BuildConfig.VERSION_NAME));
        navigationView.getMenu().getItem(0).setChecked(true);

        initialize();
    }

    private void initialize() {
        ((ListView) findViewById(R.id.items_list)).setAdapter(new HomeItemAdapter(this));
    }

    private void powerUpOnClickListener(HomeItemAdapter.Item item) {
        switch (item.getName()) {
            case "Question Papers":
                final SharedPreferences pref = LaunchingActivity.this.getSharedPreferences("user", Context.MODE_PRIVATE);
                if (pref.getBoolean("remember_program", false) && pref.getInt("pos", -1) >= 0) {
                    intentSemActivity(pref.getInt("pos", 0), pref.getString("program", null));
                } else {
                    final AlertDialog.Builder programs_builder = new AlertDialog.Builder(LaunchingActivity.this);
                    programs_builder.setCancelable(true);
                    programs_builder.setTitle("Choose your program");
                    final String[] categories = {"B.Tech", "BA Communication", "MA Communication", "Integrated MSc & MA", "MCA", "MSW", "M.Tech"};
                    final ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(LaunchingActivity.this, android.R.layout.simple_list_item_1, categories);
                    programs_builder.setItems(categories, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final int position = which;
                            if (Utils.isConnected(LaunchingActivity.this)) {
                                if (pref.getBoolean("prompt", true)) {
                                    final SharedPreferences.Editor ed = pref.edit();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LaunchingActivity.this);
                                    builder.setMessage("Do you want me to remember your academic program ? You can change it later in settings anytime.");
                                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ed.putBoolean("remember_program", true);
                                            ed.putInt("pos", position);
                                            ed.putString("program", dataAdapter.getItem(position));
                                            intentSemActivity(position, dataAdapter.getItem(position));
                                            ed.apply();
                                        }
                                    });
                                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ed.putBoolean("remember_program", false);
                                            ed.putInt("pos", -1);
                                            ed.putString("program", null);
                                            ed.apply();
                                            intentSemActivity(position, dataAdapter.getItem(position));
                                        }
                                    });
                                    builder.setNeutralButton("Don\'t show again", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ed.putBoolean("prompt", false);
                                            ed.apply();
                                            intentSemActivity(position, dataAdapter.getItem(position));
                                        }
                                    });
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                } else {
                                    intentSemActivity(position, dataAdapter.getItem(position));
                                }

                            } else {
                                Toast.makeText(LaunchingActivity.this, "Device not connected to Internet.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    AlertDialog alertDialog = programs_builder.create();
                    alertDialog.show();
                }
                break;
            case "Academic Timetable":
                if (Utils.isConnected(LaunchingActivity.this)) {
                    startActivity(new Intent(LaunchingActivity.this, AcademicTimetableActivity.class));
                } else {
                    Utils.showInternetError(this);
                }
                break;

            case "Faculty Timetable":
                if (Utils.isConnected(LaunchingActivity.this)) {
                    startActivity(new Intent(LaunchingActivity.this, FacultyTimetableActivity.class));
                } else {
                    Utils.showInternetError(this);
                }
                break;
            case "Exam Schedule":
                if (Utils.isConnected(LaunchingActivity.this))
                    startActivity(new Intent(LaunchingActivity.this, ExamCategoryActivity.class));
                else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
                break;
            case "AUMS":
                CharSequence[] items = {"AUMS - v1", "AUMS - Lite (Easier to login)"};
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LaunchingActivity.this);
                dialogBuilder.setTitle("Try v1 and if it doesn't work for you, choose Lite.");
                dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Utils.isConnected(LaunchingActivity.this)) {
                                    switch (which) {
                                        case 0:
                                            startActivity(new Intent(LaunchingActivity.this, LoginActivity.class));
                                            break;
                                        case 1:
                                            SharedPreferences pref = EncryptedPrefsUtils.get(LaunchingActivity.this, "aums_v2");
                                            if (pref.getBoolean("logged-in", false)) {
                                                startActivity(new Intent(LaunchingActivity.this, in.co.rajkumaar.amritarepo.aumsV2.activities.HomeActivity.class));
                                            } else {
                                                startActivity(new Intent(LaunchingActivity.this, in.co.rajkumaar.amritarepo.aumsV2.activities.LoginActivity.class));
                                            }
                                            break;
                                    }
                                } else {
                                    Utils.showInternetError(LaunchingActivity.this);
                                }
                            }
                        }
                );
                dialogBuilder.create().show();
                break;
            case "Timings":
                startActivity(new Intent(this, TimingsHomeActivity.class));
                break;
            case "Curriculum":
                if (Utils.isConnected(LaunchingActivity.this)) {
                    Intent curriculum_open = new Intent(LaunchingActivity.this, CurriculumActivity.class);
                    startActivity(curriculum_open);
                } else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
                break;
            case "Downloads":
                startActivity(new Intent(LaunchingActivity.this, DownloadsActivity.class));
                break;
            case "Study Materials":
                if (Utils.isConnected(LaunchingActivity.this)) {
                    final CharSequence[] depts = {"Computer Science Engineering"};
                    AlertDialog.Builder departmentDialogBuilder = new AlertDialog.Builder(LaunchingActivity.this);
                    departmentDialogBuilder.setTitle("Select your Department");
                    departmentDialogBuilder.setItems(depts, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            Intent curriculum_open = new Intent(LaunchingActivity.this, StudyMaterialsActivity.class);
                            curriculum_open.putExtra("department", depts[item]);
                            startActivity(curriculum_open);
                        }
                    });
                    AlertDialog departmentDialog = departmentDialogBuilder.create();
                    departmentDialog.show();
                } else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
                break;
            case "Central Library":
                if (Utils.isConnected(LaunchingActivity.this)) {
                    startActivity(new Intent(LaunchingActivity.this, OPACHomeActivity.class));
                } else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
                break;
            case "News":
                if (Utils.isConnected(LaunchingActivity.this))
                    startActivity(new Intent(LaunchingActivity.this, NewsActivity.class));
                else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
                break;
            case "WiFi Status":
                if (Utils.isConnected(LaunchingActivity.this))
                    startActivity(new Intent(LaunchingActivity.this, WifiStatusActivity.class));
                else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
                break;
            case "FAQ - Exams":
                if (Utils.isConnected(LaunchingActivity.this))
                    startActivity(new Intent(LaunchingActivity.this, ExamsFAQActivity.class));
                else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
                break;
            case "CMS":
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("in.co.rajkumaar.amritacms");
                if (launchIntent != null) {
                    startActivity(launchIntent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=in.co.rajkumaar.amritacms"));
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivity(intent);
                }
                break;
            case "About":
                startActivity(new Intent(getBaseContext(), AboutActivity.class));
                break;
            case "Support":
                startActivity(new Intent(getBaseContext(), SupportActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onDestroy() {
        new ClearCache().clear(this);
        super.onDestroy();
        appUpdateManager.unregisterListener(listener);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                NavigationView navigationView = findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.nav_home);
            } else {
                super.onBackPressed();
            }
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Utils.showSnackBar(this, "Please click BACK again to exit");
        //Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                doubleBackToExitPressedOnce = false;
            }
        }, 2000);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.celebs) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.action_download) {
            startActivity(new Intent(this, DownloadsActivity.class));
        } else if (id == R.id.notifications) {
            startActivity(new Intent(this, NotificationsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (id == R.id.nav_downloads) {
            drawer.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, DownloadsActivity.class));
        } else if (id == R.id.nav_bot) {
            drawer.closeDrawer(GravityCompat.START);
            AlertDialog.Builder builder = new AlertDialog.Builder(LaunchingActivity.this);
            builder.setMessage(R.string.telegram_bot_warning);
            builder.setPositiveButton("Okay, I understood", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(getString(R.string.telegram_bot_url)));
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivity(intent);
                }
            });
            builder.setCancelable(false);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else if (id == R.id.nav_feedback) {
            Intent it = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.my_mail), null));
            it.putExtra(Intent.EXTRA_SUBJECT, "Reg. Feedback for Amrita Repository");
            it.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.my_mail)});
            if (it.resolveActivity(getPackageManager()) != null)
                startActivity(it);
        } else if (id == R.id.nav_share) {
            drawer.closeDrawer(GravityCompat.START);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Find all features an Amritian needs under one roof. Download Amrita Repository, the must-have app!\n" + "https://bit.ly/amritarepo");
            sendIntent.setType("text/plain");
            if (sendIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(sendIntent);
            } else {
                Toast.makeText(this, "Error.", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.nav_about) {
            drawer.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.settings) {
            drawer.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_review) {
            drawer.closeDrawer(GravityCompat.START);
            if (Utils.isConnected(this)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Error Opening Play Store.", Toast.LENGTH_SHORT).show();
                }
            } else
                Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
        } else if (id == R.id.nav_donate) {
            startActivity(new Intent(getBaseContext(), SupportActivity.class));
        } else if (id == R.id.nav_bugreport) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.github_issues_url)));
            startActivity(intent);
        }
        return true;
    }

    /**
     * Sends an intent to semester activity with the selected program
     *
     * @param position
     * @param title
     */
    private void intentSemActivity(int position, String title) {
        Bundle params = new Bundle();
        params.putString("Department", title);
        Log.e("Dept", title);
        mFirebaseAnalytics.logEvent("EventDept", params);

        if (Utils.isConnected(this)) {

            Intent intent = new Intent(LaunchingActivity.this, SemesterActivity.class);
            intent.putExtra("course", position);
            intent.putExtra("pageTitle", title);
            startActivity(intent);

        } else {
            Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Create a listener to track request state updates.
        listener = state -> {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate();
            }
        };
        // Before starting an update, register a listener for updates.
        appUpdateManager.registerListener(listener);
        startUpdate();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_UPDATE_REQUEST_CODE && resultCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED) {
            Utils.showToast(this, getString(R.string.update_failed));
            startUpdate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {
                    // If the update is downloaded but not installed,
                    // notify the user to complete the update.
                    if (appUpdateInfo.clientVersionStalenessDays() > DAYS_FOR_IMMEDIATE_UPDATE) {
                        if (appUpdateInfo.updateAvailability()
                                == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                            // If an in-app update is already running, resume the update.
                            try {
                                appUpdateManager.startUpdateFlowForResult(
                                        appUpdateInfo,
                                        AppUpdateType.IMMEDIATE,
                                        this,
                                        APP_UPDATE_REQUEST_CODE);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                            popupSnackbarForCompleteUpdate();
                        }
                    }
                });
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void openSupport(View view) {
        startActivity(new Intent(this, SupportActivity.class));
    }

    class HomeItemAdapter extends BaseAdapter {

        private List<Item> items = new ArrayList<>();
        private LayoutInflater inflater;
        private Context context;

        HomeItemAdapter(Context context) {

            this.context = context;
            inflater = LayoutInflater.from(context);
            items.clear();
            items.add(new Item("#FF201B", "Question Papers", FontAwesomeIcons.fa_paper_plane));
            items.add(new Item("#009688", "AUMS", FontAwesomeIcons.fa_graduation_cap));
            items.add(new Item("#a4123f", "CMS", FontAwesomeIcons.fa_university));
            items.add(new Item("#9c27b0", "Academic Timetable", FontAwesomeIcons.fa_calendar));
            items.add(new Item("#e91e63", "Faculty Timetable", FontAwesomeIcons.fa_users));
            items.add(new Item("#03a9f4", "Central Library", FontAwesomeIcons.fa_laptop));
            items.add(new Item("#3f51b5", "Curriculum", FontAwesomeIcons.fa_paperclip));
            items.add(new Item("#fe5352", "Exam Schedule", FontAwesomeIcons.fa_pencil));
            items.add(new Item("#009688", "Timings", FontAwesomeIcons.fa_clock_o));
            items.add(new Item("#9c27b0", "News", FontAwesomeIcons.fa_newspaper_o));
            items.add(new Item("#03a9f4", "Study Materials", FontAwesomeIcons.fa_book));
            items.add(new Item("#259b24", "Downloads", FontAwesomeIcons.fa_download));
            items.add(new Item("#f13c20", "Support", FontAwesomeIcons.fa_dollar));
            items.add(new Item("#259b24", "About", FontAwesomeIcons.fa_info_circle));
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = view;
            ImageView picture;
            TextView name;

            if (v == null) {
                v = inflater.inflate(R.layout.item_main_grid, viewGroup, false);
                v.setTag(R.id.landing_item_holder, v.findViewById(R.id.landing_item_holder));
                v.setTag(R.id.landing_picture, v.findViewById(R.id.landing_picture));
                v.setTag(R.id.landing_text, v.findViewById(R.id.landing_text));
            }
            picture = (ImageView) v.getTag(R.id.landing_picture);
            name = (TextView) v.getTag(R.id.landing_text);

            final Item item = (Item) getItem(i);
            picture.setImageDrawable(new IconDrawable(context, item.image).color(Color.parseColor(item.color)));
            name.setText(item.name);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    powerUpOnClickListener(item);
                }
            });
            return v;
        }

        private class Item {

            private final String color;
            private final String name;
            private FontAwesomeIcons image;

            Item(String color, String name, FontAwesomeIcons imageID) {
                this.color = color;
                this.name = name;
                this.image = imageID;
            }

            public String getName() {
                return name;
            }
        }
    }

    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.drawer_layout),
                        getString(R.string.update_downloaded),
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(getString(R.string.restart), view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(R.color.white));
        snackbar.show();
    }

    private void startUpdate() {
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.clientVersionStalenessDays() != null) {
                if (appUpdateInfo.clientVersionStalenessDays() > DAYS_FOR_IMMEDIATE_UPDATE) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                // Pass the intent that is returned by 'getAppUpdateInfo()'.
                                appUpdateInfo,
                                // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                                AppUpdateType.IMMEDIATE,
                                // The current activity making the update request.
                                this,
                                // Include a request code to later monitor this update request.
                                APP_UPDATE_REQUEST_CODE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                // Pass the intent that is returned by 'getAppUpdateInfo()'.
                                appUpdateInfo,
                                // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                                AppUpdateType.FLEXIBLE,
                                // The current activity making the update request.
                                this,
                                // Include a request code to later monitor this update request.
                                APP_UPDATE_REQUEST_CODE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}

