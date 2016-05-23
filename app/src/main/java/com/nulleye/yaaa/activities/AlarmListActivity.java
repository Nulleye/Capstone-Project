package com.nulleye.yaaa.activities;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.YaaaApplication;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.data.AlarmDbHelper;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.external.DividerItemDecoration;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * Alarm list, main app activity
 *
 * Two pane mode for wide devices (tablets) or
 * single item list activity for narrow devices (phones)
 *
 * Created by Cristian Alvarez on 3/5/16.
 */
public class AlarmListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        FolderChooserDialog.FolderCallback, FileChooserDialog.FileCallback {

    private static final int LOADER_ID = 1;

    private static String VIEW_ALARM_ID = "view.alarm.id";

    private boolean twoPane;    //Current mode: true -> wide (tablets), false -> narrow (phones)

    private RecyclerView recyclerView;
    private AlarmRecyclerViewAdapter recyclerViewAdapter;

    private View emptyList;

    //Show swipe to delete message
    private boolean showSwipeDelete;

    private int viewAlarmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm_list);

        showSwipeDelete = YaaaApplication.getPreferences().getShowSwipeDelete();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setSubtitle("dsfsdfsdf");
            toolbar.setTitle(getTitle());
            setSupportActionBar(toolbar);
        }


        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null)
            fab.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    startActivity(new Intent(AlarmListActivity.this, AlarmDetailActivity.class));
                    overridePendingTransition(R.anim.detail_in, R.anim.list_out);
                    }

            });

        emptyList = findViewById(R.id.empty_list);

        twoPane = (findViewById(R.id.alarm_detail_container) != null);

        recyclerView = (RecyclerView) findViewById(R.id.alarm_list);
        assert recyclerView != null;
        setUpRecyclerView();

        if (savedInstanceState != null)
            viewAlarmId = savedInstanceState.getInt(VIEW_ALARM_ID, Alarm.NO_ID);

        //Started from notification?
        if (!Alarm.isValidId(viewAlarmId)) viewAlarmId = Alarm.getAlarmId(getIntent());

        //Load alarms
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        //Message swipe to delete (limit once per app execution using messageShown)
        if (showSwipeDelete && !YaaaApplication.messageShown(R.string.swipe_to_delete)) {
            YaaaApplication.incMessageCount(R.string.swipe_to_delete);
            Snackbar.make(recyclerView, R.string.swipe_to_delete, Snackbar.LENGTH_LONG)
                    .setAction(R.string.btn_dismiss, new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            showSwipeDelete = false;
                            YaaaApplication.getPreferences().setShowSwipeDelete(showSwipeDelete);
                        }

                    }).show();
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(SettingsActivity.GO_UP, AlarmListActivity.class.getCanonicalName());
            startActivity(intent);
            overridePendingTransition(R.anim.detail_in, R.anim.list_out);
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
        //Store first currently visible item
        final int pos = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        if (pos != RecyclerView.NO_POSITION) {
            viewAlarmId = (int) recyclerViewAdapter.getItemId(pos);
            if (Alarm.isValidId(viewAlarmId)) outState.putInt(VIEW_ALARM_ID, viewAlarmId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        recyclerViewAdapter.notifyDataSetChanged();
    }


    // VERY VERY WEIRD STUFF HERE!!
    // FileChooserDialog & FolderChooserDialog implementations need an AppCompatActivity that
    // implements a FolderChooserDialog.FolderCallback & FileChooserDialog.FileCallback!!!!
    // This forces to do very very weird things, at least two different parameters one for
    // the app and the other for the callback would have been a better approach
    // TODO Make my own implementation of these dialogs

    @Override
    public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
        //TODO Tablet - if (fragment != null) fragment.onFileSelection(dialog, file);
    }


    @Override
    public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
        //TODO Tablet - if (fragment != null) fragment.onFolderSelection(dialog, folder);
    }


    private void setSubtitle(final String text) {
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        if (toolbar != null) {
//            toolbar.setTitle(getTitle());
//            toolbar.setSubtitle(text);
//            setSupportActionBar(toolbar);
//        }
        final ActionBar toolbar = getSupportActionBar();
        if (toolbar != null) toolbar.setSubtitle(text);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER WITH SWIPE TO DELETE AND UNDO FEATURE


    public class AlarmRecyclerViewAdapter
            extends RecyclerView.Adapter<AlarmRecyclerViewAdapter.ViewHolder> {

        private List<Alarm> alarms;
        private Set<Integer> delayedDeleteAlarms;


        public AlarmRecyclerViewAdapter() {
            setHasStableIds(true);
        }


        //Update data and restore pending runnables
        public void swapCursor(final Cursor cAlarms) {
            alarms = Alarm.getAlarms(cAlarms);

            if (!FnUtil.isVoid(alarms)) {
                setSubtitle(Alarm.getNextScheduledAlarmText(AlarmListActivity.this, alarms, Calendar.getInstance()));
                //cAlarms.close();
            } else setSubtitle(null);

            delayedDeleteAlarms = AlarmDbHelper.getDelayedDeleteAlarms();

            //Scroll to show alarm when started from notification
            if (Alarm.isValidId(viewAlarmId)) {
                final int pos = Alarm.getAlarmPosition(alarms, viewAlarmId);
                if (pos != RecyclerView.NO_POSITION) recyclerView.getLayoutManager().scrollToPosition(pos);
                viewAlarmId = Alarm.NO_ID;
            }

            notifyDataSetChanged();
        }


        @Override
        public long getItemId(int position) {
            if (!FnUtil.isVoid(alarms) && (position > -1) && (position < alarms.size()))
                return alarms.get(position).getId();
            else return Alarm.NO_ID;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.alarm_list_content, parent, false);
            return new ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Alarm alarm = alarms.get(position);
            holder.setAlarm(alarm, delayedDeleteAlarms.contains(alarm.getId()));
        }


        @Override
        public int getItemCount() {
            return (alarms != null) ? alarms.size() : 0;
        }


        public boolean isDelayedDeleteAlarm(final int position) {
            return delayedDeleteAlarms.contains(alarms.get(position).getId());
        }


        public void delayedDeleteAlarm(final int position) {
            final Alarm alarm = alarms.get(position);
            final int alarmId = alarm.getId();
            if (!delayedDeleteAlarms.contains(alarmId)) {
                delayedDeleteAlarms.add(alarmId);
                notifyItemChanged(position);
                AlarmDbHelper.delayedDeleteAlarm(alarmId);
            }
        }


        public void undodDeleteAlarm(final int alarmId) {
            delayedDeleteAlarms.remove(alarmId);
            final int position = Alarm.getAlarmPosition(alarms, alarmId);
            if (position > -1) notifyItemChanged(position);
            // The undo may fail, but subsequent data refresh will correct that and definitely
            // delete or not this alarm
            AlarmDbHelper.undoDeleteAlarm(alarmId);
        }


        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View view;

            public final TextView timeView;
            public final TextView titleView;
            public final TextView nextRingView;
            public final TextView dateConfigView;
            public final Switch onoffView;

            public final FrameLayout undoFrame;
            public final Button undoButton;

            public Alarm alarm = null;


            public ViewHolder(View view) {
                super(view);
                this.view = view;
                timeView = (TextView) view.findViewById(R.id.time);
                titleView = (TextView) view.findViewById(R.id.title);
                nextRingView = (TextView) view.findViewById(R.id.next_ring);
                dateConfigView = (TextView) view.findViewById(R.id.date_config);
                onoffView = (Switch) view.findViewById(R.id.onoff);
                undoFrame = (FrameLayout) view.findViewById(R.id.undo_frame);
                undoButton = (Button) view.findViewById(R.id.undo_button);
            }


            public ViewHolder setAlarm(final Alarm alarm, final boolean undoState) {
                this.alarm = alarm;
                if (undoState) {
                    //Undo mode
                    undoFrame.setVisibility(View.VISIBLE);
                    undoButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            undodDeleteAlarm(alarm.getId());
                        }

                    });
                } else {
                    //Normal mode, show data
                    final Context context = AlarmListActivity.this;
                    undoFrame.setVisibility(View.GONE);

                    timeView.setText(alarm.getTimeText(context));
                    timeView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            final TimePickerDialog timePicker = new TimePickerDialog(context,
                                    new TimePickerDialog.OnTimeSetListener() {

                                        @Override
                                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                            alarm.setTime(selectedHour, selectedMinute);
                                            AlarmDbHelper.saveAlarm(context, alarm);
                                        }

                                    }, alarm.getHour(), alarm.getMinutes(), FnUtil.is24HourMode(context));
                            timePicker.show();
                        }

                    });

                    titleView.setText(alarm.getTitle(context));

                    int schedule = alarm.kindScheduledRing(Calendar.getInstance());
                    String next = alarm.getNextRingText(context, false);
                    if (schedule == Alarm.SCH_YES_VACATION)
                        nextRingView.setText(context.getString(R.string.next_ring_vacation_message, next));
                    else nextRingView.setText(context.getString(R.string.next_ring_message, next));

                    view.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (twoPane) {
                                final AlarmDetailFragment fragment = new AlarmDetailFragment();
                                fragment.setArguments(alarm.putAlarm(new Bundle()));
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.alarm_detail_container, fragment)
                                        .commit();
                            } else {
                                final Context context = AlarmListActivity.this;
                                context.startActivity(alarm.putAlarm(new Intent(context, AlarmDetailActivity.class)));
                                overridePendingTransition(R.anim.detail_in, R.anim.list_out);
                            }
                        }

                    });

                    dateConfigView.setText(alarm.getAlarmSummaryDateConfig(context));

                    onoffView.setTag(alarm.isEnabled());
                    onoffView.setChecked(alarm.isEnabled());
                    onoffView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            //Prevent check from firing the first time
                            if (buttonView.getTag() != null) {
                                if (buttonView.getTag() == isChecked) {
                                    buttonView.setTag(null);
                                    return;
                                }
                                buttonView.setTag(null);
                            }
                            AlarmDbHelper.enableAlarm(context, alarm, isChecked);
                        }

                    });

                    switch(schedule) {
                        case Alarm.SCH_NO:
                            view.setBackgroundColor(ContextCompat.getColor(context, R.color.list_sch_no));
                            break;
                        case Alarm.SCH_YES_VACATION:
                            view.setBackgroundColor(ContextCompat.getColor(context, R.color.list_sch_yes_vacation));
                            break;
                        default:
                            view.setBackgroundColor(ContextCompat.getColor(context, R.color.list_sch_yes));
                            break;
                    }
                }
                return this;
            }


            @Override
            public String toString() {
                return super.toString() + timeView.getText() + " " + nextRingView.getText();
            }

        } //ViewHolder


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
        recyclerViewAdapter.swapCursor(data);   //Don't reset, just swap (refresh data)
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        recyclerView.setAdapter(null);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SWIPE TO DELETE WITH UNDO

    private void setUpRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdapter = new AlarmRecyclerViewAdapter();
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        setUpItemTouchHelper();
    }


    private void setUpItemTouchHelper() {
        final ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            final Drawable background =
                    new ColorDrawable(ContextCompat.getColor(AlarmListActivity.this, R.color.delete_swipe_background));
            final Drawable deleteIcon =
                    ContextCompat.getDrawable(AlarmListActivity.this, R.drawable.ic_delete);
            final int deleteIconMargin =
                    (int) AlarmListActivity.this.getResources().getDimension(R.dimen.swipe_delete_icon_margin);


            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }


            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                final AlarmRecyclerViewAdapter adapter = (AlarmRecyclerViewAdapter) recyclerView.getAdapter();
                if (adapter.isDelayedDeleteAlarm(viewHolder.getAdapterPosition())) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }


            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final AlarmRecyclerViewAdapter adapter = (AlarmRecyclerViewAdapter) recyclerView.getAdapter();
                adapter.delayedDeleteAlarm(viewHolder.getAdapterPosition());
                if (showSwipeDelete) {
                    //User has swiped once, disable swipe message forever
                    showSwipeDelete = false;
                    YaaaApplication.getPreferences().setShowSwipeDelete(showSwipeDelete);
                }
            }


            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if (viewHolder.getAdapterPosition() != -1) {
                    final View itemView = viewHolder.itemView;
                    final int iVTop = itemView.getTop();
                    final int iVBottom = itemView.getBottom();
                    final int iVLeft = itemView.getLeft();
                    final int iVRight = itemView.getRight();
                    int left;
                    int right;
                    if (dX > 0) {
                        //swipe left to right
                        left =  iVLeft;
                        right = iVLeft + (int) dX;
                    } else {
                        //swipe right to left
                        left =  iVRight + (int) dX;
                        right = iVRight;
                    }
                    background.setBounds(left, iVTop, right, iVBottom);
                    background.draw(c);

                    final int intrinsicHeight = deleteIcon.getIntrinsicHeight();
                    final int delTop = iVTop + ((iVBottom - iVTop) - intrinsicHeight) / 2;
                    if (dX > 0) {
                        //swipe left to right
                        left = right - deleteIconMargin - deleteIcon.getIntrinsicWidth();
                        right = right - deleteIconMargin;
                    } else {
                        //swipe right to left
                        left = iVRight - deleteIconMargin - deleteIcon.getIntrinsicWidth();
                        right = iVRight - deleteIconMargin;
                    }
                    deleteIcon.setBounds(left, delTop, right, delTop + intrinsicHeight);
                    deleteIcon.draw(c);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        }; //ItemTouchHelper.SimpleCallback
        final ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    } //setUpItemTouchHelper

}
