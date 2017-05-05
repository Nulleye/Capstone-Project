package com.nulleye.yaaa.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.nulleye.common.MapList;
import com.nulleye.common.transitions.EnhancedSharedElementCallback;
import com.nulleye.common.widget.recyclerview.AdvancedItemAnimator;
import com.nulleye.common.widget.recyclerview.AdvancedRecyclerView;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.YaaaApplication;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.data.AlarmDbHelper;
import com.nulleye.yaaa.data.YaaaPreferences;
import com.nulleye.yaaa.dialogs.SettingsMaster;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.gui.GuiUtil;
import com.nulleye.yaaa.util.helpers.AlarmViewHolder;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * AlarmListActivity
 * Alarm list, main app activity
 * TODO Two pane mode for wide devices (tablets) or single item list activity for narrow devices (phones)
 *
 * @author Cristian Alvarez Planas
 * @version 3
 * 3/5/16.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AlarmListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, SettingsMaster.SettingsMasterListener,
        AdvancedRecyclerView.ScrollToPositionListener {

    //Alarm adapter requests
    public static int REQUEST_REPOSITION = -2;
    public static int REQUEST_RETURN = -1;
    public static int REQUEST_NONE = 0;
    public static int REQUEST_EDIT = 1;

    public static String ACTION_NEW = "com.nulleye.yaaa.ALARM_ACTION_NEW";

    public static String TAG = AlarmListActivity.class.getName();
    protected boolean DEBUG = true;

    YaaaPreferences prefs = YaaaApplication.getPreferences();

    private static final int LOADER_ID = 1;

    private boolean twoPane;    //Current mode: true -> wide (tablets), false -> narrow (phones)

    private AlarmAdapter alarmAdapter;

    @BindView(R.id.main_coordinator) CoordinatorLayout main_coordinator;
    @BindView(R.id.alarm_list) AdvancedRecyclerView recyclerView;
    @BindView(R.id.empty_list) View emptyList;

    //Show swipe to delete message
    private boolean showSwipeDelete;

    //Alarm id when reentering
    private long reenter_alarm_id = Alarm.NO_ID;

    //Current action to prevent reenter on notification double-click
    private Integer current_action = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DEBUG) Log.d(TAG, "onCreate()");

        setContentView(R.layout.activity_alarm_list);
        ButterKnife.bind(this);

        //Tablet mode?
        twoPane = (findViewById(R.id.alarm_detail_container) != null);

        //TODO subtitle
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setSubtitle("dsfsdfsdf");
            toolbar.setTitle(getTitle());
            setSupportActionBar(toolbar);
        }

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    createAction();
                }

            });
            //Set alarm list bottom padding (fab button size + margin)
            fab.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            recyclerView.setPadding(recyclerView.getPaddingLeft(), recyclerView.getPaddingTop(), recyclerView.getPaddingRight(),
                    (int) (fab.getMeasuredHeight() + getResources().getDimension(R.dimen.fab_margin)));
        }

        //Setup adapter and recyclerView
        alarmAdapter = new AlarmAdapter(recyclerView);
        alarmAdapter.restoreState(savedInstanceState);
        if (savedInstanceState == null) {
            final Intent intent = getIntent();
            final long alarmId = Alarm.getAlarmId(intent);
            if (Alarm.isValidId(alarmId)) {
                current_action = REQUEST_EDIT;
                if (DEBUG) Log.d(TAG, "onCreate() - Edit " + alarmId);
                alarmAdapter.setItemAction(current_action);
                alarmAdapter.setItemId(alarmId);
            } else if (ACTION_NEW.equals(intent.getAction()))
                main_coordinator.post(new Runnable() {

                    @Override
                    public void run() {
                        createAction();
                    }

                });
        }
        recyclerView.setAdapter(alarmAdapter);
        recyclerView.setExpandAnimationDurationRes(R.integer.expand_animation_duration)
                .setChangeAnimationDurationRes(R.integer.change_animation_duration)
                .setSwipeDirections(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0)
                .setDeleteBackground(R.color.delete_swipe_background)
                .setDeleteIcon(R.drawable.delete_swipe_icon)
                .setDeleteIconMarginRes(R.dimen.delete_swipe_icon_margin)
                .setSwipeController(alarmAdapter)
                .setOnFoldingListener(alarmAdapter)
                .setScrollToPositionListener(this);

        alarmAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {
                super.onChanged();
                if (reenter_alarm_id != Alarm.NO_ID) {
                    Log.d(TAG, "onChanged() - Reenter " + reenter_alarm_id);
                    final Alarm alarm = alarmAdapter.getItem(reenter_alarm_id);
                    if (alarm != null) {
                        final int pos = alarmAdapter.getItemPosition(reenter_alarm_id);
                        Log.d(TAG, "onChanged() - Reenter " + reenter_alarm_id + " position " + pos);
                        recyclerView.scrollToPos(REQUEST_RETURN, pos);
                    } else continueAnimation();
                    reenter_alarm_id = Alarm.NO_ID;
                }
            }

        });

        //Load alarms
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        //Message swipe to delete (limit once per app execution using messageShown)
        showSwipeDelete = prefs.getShowSwipeDelete();
        if (showSwipeDelete && !YaaaApplication.messageShown(R.string.swipe_to_delete)) {
            YaaaApplication.incMessageCount(R.string.swipe_to_delete);
            Snackbar.make(recyclerView, R.string.swipe_to_delete, Snackbar.LENGTH_LONG)
                    .setAction(R.string.btn_dismiss, new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            showSwipeDelete = false;
                            prefs.setShowSwipeDelete(showSwipeDelete);
                        }

                    }).show();
        }

        if (GuiUtil.enableSpecialAnimations(this)) prepareTransitions();

    }

//android.intent.action.SET_ALARM
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (DEBUG) Log.d(TAG, "onNewIntent()");
        if (current_action != null) {
            if (DEBUG) Log.d(TAG, "onNewIntent() - Skipped " + current_action);
            return;
        }
        final long alarmId = Alarm.getAlarmId(intent);
        if (Alarm.isValidId(alarmId)) {
            final int pos = alarmAdapter.getItemPosition(alarmId);
            if (DEBUG) Log.d(TAG, "onNewIntent() - Edit " + alarmId + " position " + pos);
            if (pos != RecyclerView.NO_POSITION) {
                current_action = REQUEST_EDIT;
                recyclerView.scrollToPos(current_action, pos);
            }
        } else if (ACTION_NEW.equals(intent.getAction())) createAction();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            final View view = findViewById(R.id.action_settings);
            SettingsMaster.gotoSettings(this, view, null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        alarmAdapter.storeState(outState);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @SuppressLint("NewApi")
    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        Log.d(TAG,"onActivityReenter()");
        final Alarm alarm = Alarm.getAlarm(data);
        if (alarm == null) return;
        if (GuiUtil.enableSpecialAnimations(this)) postponeEnterTransition();
        reenter_alarm_id = alarm.getId();
        final Alarm currentAlarm = alarmAdapter.getItem(reenter_alarm_id);
        if ((currentAlarm != null) && (alarm.hashCode() == currentAlarm.hashCode()))
            recyclerView.scrollToPos(REQUEST_RETURN, alarmAdapter.getItemPosition(reenter_alarm_id));
        //else dataset update still pending
    }


    private void setSubtitle(final String text) {
//TODO enable to update next alarm info on title
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        if (toolbar != null) {
//            toolbar.setTitle(getTitle());
//            toolbar.setSubtitle(text);
//            setSupportActionBar(toolbar);
//        }
        final ActionBar toolbar = getSupportActionBar();
        if (toolbar != null) toolbar.setSubtitle(text);
    }



    /**
     * Callback function to receive setting dialog results
     * @param type Type of setting
     * @param result Dialog setting result, CHANGED or UNCHANGED
     * @param alarmOrPrefs Object currently affected (Alarm or Preference object)
     */
    @Override
    public void onSettingResult(final SettingsMaster.SettingType type,
            final SettingsMaster.SettingResult result, @Nullable final Object alarmOrPrefs) {
        //Ignore dialog "Cancel" events by now
        if (SettingsMaster.SettingResult.UNCHANGED.equals(result)) return;
        switch (type) {
            case ALARM_TIME:
                alarmAdapter.setItemAction(REQUEST_REPOSITION);
                alarmAdapter.setItemId(((Alarm) alarmOrPrefs).getId());
            case ALARM_REPETITION:
            case ALARM_DATE:
            case ALARM_INTERVAL:
                //Save alarm -> will fire alarm list update
                AlarmDbHelper.saveAlarm(this, prefs, (Alarm) alarmOrPrefs);
                break;
        }
    }


    /**
     * Create a new alarm
     */
    void createAction() {
        current_action = null;
        //TODO special new animation
        startActivity(new Intent(AlarmListActivity.this, AlarmDetailActivity.class));
        if (!GuiUtil.enableSpecialAnimations(this)) overridePendingTransition(R.anim.detail_in, R.anim.list_out);

    }


    /**
     * Edit alarm from holder
     * @param holder
     */
    void editAction(final AlarmAdapter.AlarmHolder holder) {
        current_action = null;
        if (twoPane) {
            final AlarmDetailFragment fragment = new AlarmDetailFragment();
            fragment.setArguments(holder.getAlarm().putAlarm(new Bundle()));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.alarm_detail_container, fragment)
                    .commit();
        } else {
            final Intent intent = new Intent(AlarmListActivity.this, AlarmDetailActivity.class);
            holder.getAlarm().putAlarm(intent);
            if (GuiUtil.enableSpecialAnimations(AlarmListActivity.this)) {
                recyclerView.enableItemAnimator(false);
                final Pair<View, String>[] pairs = holder.getSharedTransitionData();
                recyclerView.startDetailActivity(AlarmListActivity.this, intent, REQUEST_EDIT,
                        (pairs != null)? ActivityOptionsCompat.makeSceneTransitionAnimation(AlarmListActivity.this,
                                pairs).toBundle() : null, holder.itemView, holder.getExitTransitions());
            } else {
                startActivityForResult(intent, REQUEST_EDIT);
                overridePendingTransition(R.anim.detail_in, R.anim.list_out);
            }
        }
    }


    @Override
    public void onScrollToPosition(@Nullable final Integer action, final RecyclerView.ViewHolder holder) {
        if (action != null) {
            if (action == REQUEST_EDIT) editAction((AlarmAdapter.AlarmHolder) holder);
            else if (action == REQUEST_RETURN) continueAnimation();
        }
    }


    @SuppressLint("NewApi")
    protected void continueAnimation() {
        if (GuiUtil.enableSpecialAnimations(this)) {
            if (DEBUG) Log.d(TAG, "continueAnimation()");
            startPostponedEnterTransition();
            recyclerView.enableItemAnimator(true);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER WITH "SWIPE TO DELETE AND UNDO FEATURE" AND "ITEM FOLDING"


    //AlarmAdapter
    public class AlarmAdapter
            extends AdvancedRecyclerView.AdvancedAdapter<AlarmAdapter.AlarmHolder>
            implements AdvancedItemAnimator.OnFoldingListener, AdvancedRecyclerView.SwipeController {

        private MapList<Long, Alarm> alarms = null;


        public AlarmAdapter(final AdvancedRecyclerView recyclerView) {
            super(recyclerView);
        }


        @Override
        public long getItemId(int position) {
            return alarms.getAtPosition(position).getId();
        }


        @Override
        public int getItemCount() {
            return (alarms != null) ? alarms.size() : 0;
        }


        @Override
        public int getItemPosition(final long id) {
            return Alarm.getAlarmPosition(alarms, id);
        }


        public Alarm getItem(long id) {
            return Alarm.getAlarm(alarms, id);
        }


        @Override
        public void swapCursor(final Cursor items) {
            alarms = Alarm.getAlarmsMapList(items);
            if (!FnUtil.isVoid(alarms))
                setSubtitle(Alarm.getNextScheduledAlarmText(AlarmListActivity.this, prefs, alarms, Calendar.getInstance()));
            else setSubtitle(null);
        }


        @Override
        public boolean isSwipedState(final long id) {
            return AlarmDbHelper.hasDeleteAlarm(id);
        }


        @Override
        public AlarmHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_alarm_list_item, parent, false);
            return new AlarmHolder(prefs, this, view);
        }


        @Override
        public void onBindViewHolder(final AlarmHolder holder, final int position) {
            holder.setAlarm(alarms.getAtPosition(position), position);
        }


        @Override
        public void onFold(RecyclerView.ViewHolder holder, int type, float fraction) {
            ((AlarmViewHolder) holder).onFold(type, fraction);
        }


        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return (isSwipedState(viewHolder.getItemId()))? 0 : (ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }


        @Override
        public void onSwiped(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int swipeDir) {
            final int position = viewHolder.getAdapterPosition();
            final Alarm alarm = alarms.getAtPosition(position);
            if (alarm != null) {
                final long alarmId = alarm.getId();
                if (AlarmDbHelper.addDeleteAlarm(AlarmListActivity.this, prefs, alarmId)) notifyItemChanged(position);
                if (showSwipeDelete) {
                    //User has swiped once, disable swipe message forever
                    showSwipeDelete = false;
                    prefs.setShowSwipeDelete(showSwipeDelete);
                }
            }
        }


        @Override
        public boolean onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            //Use default paint method
            return false;
        }


        void undoAction(final long alarmId, final int position) {
            if (AlarmDbHelper.undoDeleteAlarm(alarmId)) notifyItemChanged(position);
            //else notifyItemRemoved(position);
        }


        //AlarmHolder
        class AlarmHolder extends AlarmViewHolder {

            AlarmHolder(final YaaaPreferences prefs, final AlarmAdapter alarmAdapter, final View view) {
                super(prefs, alarmAdapter, view);
            }


            @SuppressWarnings("unchecked")
            protected <ActivityType extends Activity & SettingsMaster.SettingsMasterListener> ActivityType getActivity() {
                return (ActivityType) AlarmListActivity.this;
            }


            @Override
            public boolean doOnClick(View v) {
                if (!super.doOnClick(v)) {
                    final AlarmHolder aholder = (AlarmHolder) recyclerView.findContainingViewHolder(v);
                    switch (v.getId()) {
                        case R.id.undo_action:
                            undoAction(aholder.getItemId(), aholder.getAdapterPosition());
                            break;
                        default:
                            editAction(aholder);
                    }
                }
                return true;
            }

        } //AlarmHolder

    } //AlarmRecyclerViewAdapter


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LOADER


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return AlarmDbHelper.getAlarmsLoader(this);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        emptyList.setVisibility((FnUtil.hasData(data))? View.GONE : View.VISIBLE);
        alarmAdapter.setData(data);   //Don't reset, just refresh data
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        recyclerView.setAdapter(null);
    }


    @SuppressLint("NewApi")
    void prepareTransitions() {
//        getWindow().setExitTransition(
//                TransitionInflater.from(this).inflateTransition(R.transition.alarm_list_exit));
//        getWindow().setReturnTransition(
//                TransitionInflater.from(this).inflateTransition(R.transition.alarm_list_return));
//        getWindow().setSharedElementEnterTransition(
//                TransitionInflater.from(this).inflateTransition(R.transition.alarm_detail_shared_enter));
        setEnterSharedElementCallback(
                new AlarmListEnhancedSharedElementCallback(this, EnhancedSharedElementCallback.ENTER_MODE));
        setExitSharedElementCallback(
                new AlarmListEnhancedSharedElementCallback(this, EnhancedSharedElementCallback.EXIT_MODE));

//        postponeEnterTransition();
//        main_coordinator.getViewTreeObserver().addOnPreDrawListener(
//                new ViewTreeObserver.OnPreDrawListener() {
//                    @Override
//                    public boolean onPreDraw() {
//                        main_coordinator.getViewTreeObserver().removeOnPreDrawListener(this);
//                        startPostponedEnterTransition();
//                        return true;
//                    }
//                }
//        );
    }


    //AlarmListEnhancedSharedElementCallback
    class AlarmListEnhancedSharedElementCallback extends EnhancedSharedElementCallback {


        public AlarmListEnhancedSharedElementCallback(Context context, int mode) {
            super(context, mode);
        }


        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            super.onMapSharedElements(names, sharedElements);
        }


    } //EnterEnhancedSharedElementCallback


}
