package com.jascal.flora.mvp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jascal.flora.R;
import com.jascal.flora.base.BaseActivity;
import com.jascal.flora.cache.Config;
import com.jascal.flora.cache.sp.SpHelper;
import com.jascal.flora.mvp.MainContract;
import com.jascal.flora.mvp.presenter.MainPresenter;
import com.jascal.flora.mvp.view.adapter.FeedAdapter;
import com.jascal.flora.mvp.view.listener.RecyclerListener;
import com.jascal.flora.net.bean.Feed;
import com.jascal.flora.utils.ThemeUtils;
import com.jascal.flora.widget.DrawableTextView;
import com.jascal.flora.widget.SpaceItemDecoration;
import com.jascal.ophelia_annotation.BindView;
import com.jascal.ophelia_annotation.OnClick;
import com.jascal.ophelia_api.Ophelia;

import java.util.List;

public class MainActivity extends BaseActivity implements MainContract.View, RecyclerListener.OnItemClickListener,
        NavigationView.OnNavigationItemSelectedListener {
    private MainContract.Presenter presenter;
    private List<Feed> feeds;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.navigation)
    NavigationView navigationView;

    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    @BindView(R.id.progress)
    ProgressBar progressBar;

    @BindView(R.id.title)
    DrawableTextView title;

    @OnClick(R.id.back)
    void openDrawer(View view) {
        drawerLayout.openDrawer(navigationView);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Ophelia.bind(this);
        new MainPresenter(this);
        initToolbar();
        initData();

        // getInstanceState if necessary
        if (getIntent().getBooleanExtra("navigation", false)) {
            drawerLayout.openDrawer(navigationView);
        }
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(final List<Feed> feeds) {
        this.feeds = feeds;
        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(getApplicationContext(), "feeds num is " + feeds.size(), Toast.LENGTH_SHORT).show();

        FeedAdapter feedAdapter = new FeedAdapter(feeds);
        recyclerView.setAdapter(feedAdapter);
        recyclerView.addItemDecoration(new SpaceItemDecoration(30));
        recyclerView.addOnItemTouchListener(new RecyclerListener(getApplicationContext(), recyclerView, this));
    }

    @Override
    public void error(String message) {
        Toast.makeText(getApplicationContext(), "get shots error:" + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onItemClick(View view, int position) {
        showImage(feeds.get(position));
    }

    @Override
    public void onItemLongClick(View view, int position) {
        // TODO
        Log.d("recyclerView", "long click");
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getHeaderView(0).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO custom setting
            }
        });
    }

    private void initData() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        presenter.getShots(getApplicationContext());
    }

    private void showImage(Feed feed) {
        PhotoActivity.start(this, feed);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.darkness:
                SpHelper.getInstance(MainActivity.this).put(Config.SP_THEME_KEY, false);
                MainActivity.reStart(this);
                break;
            case R.id.lightness:
                SpHelper.getInstance(MainActivity.this).put(Config.SP_THEME_KEY, true);
                MainActivity.reStart(this);
                break;
            case R.id.profile:
                ProfileActivity.start(this);
                break;
            case R.id.aboutme:
                AboutActivity.start(this);
                break;
            case R.id.setting:
                SettingActivity.start(this);
                break;
        }
        return true;
    }

    public static void reStart(MainActivity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra("navigation", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        activity.finish();
    }
}
