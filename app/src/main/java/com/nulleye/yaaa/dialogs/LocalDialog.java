package com.nulleye.yaaa.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.gui.GuiUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.nulleye.yaaa.dialogs.LocalDialog.FileEntryState.NORMAL;

/**
 * LocalDialog
 * Local Folder/File browsing
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 10/11/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class LocalDialog extends PlayerDialog<LocalDialog> {

    public static int NO_ITEM = -1;
    protected List<String> mimeTypes = null;
    protected List<String> extensions = null;

    protected FileEntry currentFolder = null;
    protected List<FileEntry> contentsFolder = null;
    private FileEntry selectedItem = null;
    private int selectedItemNum = NO_ITEM;

    protected ViewGroup contentsView = null;
    protected RecyclerView recyclerView;
    protected TextView currentFolderText;


    /**
     * Backup current source prior to show permission dialog
     */
    public interface LocalDialogPermission {

        void setCurrentSource(final String source);

    } //LocalDialogPermission


    /**
     * Current state for a file/folder entry
     */
    public enum FileEntryState {
        NORMAL,         //Normal - "selectable / clickable" folder / file
        DISABLED,       //Disabled - "Non - selectable / clickable" file
        LOCKED          //Locked - "Non - selectable / clickable" folder
    } //FileEntryState

    
    /**
     * Holds information about a file/folder entry
     */
    class FileEntry extends File {

        FileEntryState state;


        FileEntryState getState() {
            return state;
        }


        boolean isNormal() {
            return NORMAL.equals(state);
        }


        Pair<String,String> getPair() {
            String name = getName();
            if (isDirectory()) {
                final File parent = super.getParentFile();
                if (parent != null) name = parent.getName() + File.separator + name;
            }
            return new Pair<>(name, FnUtil.fileToUriString(this));
        }


        boolean hasExtension(final String extension) {
            return getName().toLowerCase().endsWith("." + extension.toLowerCase());
        }


        boolean hasExtensions(final List<String> extensions) {
            for (String extension : extensions)
                if (hasExtension(extension)) return true;
            return false;
        }


        boolean isMimeType(final String mimeType) {
            return FnUtil.fileIsMimeType(this, mimeType);
        }


        String findMimeType(final List<String> mimeTypes) {
            for (String mimeType : mimeTypes)
                if (isMimeType(mimeType)) return mimeType;
            return null;
        }


        public boolean hasParentFile() {
            final File parent = super.getParentFile();
            if ((parent != null) && parent.canRead()) {
                final String[] files = parent.list();
                if ((files != null) && (files.length > 0)) return true;
            }
            return false;
        }


        @Override
        public FileEntry getParentFile() {
            final File parent = super.getParentFile();
            if (parent != null) return new FileEntry(parent);
            return null;
        }


        FileEntry(final String filename) {
            super(filename);
            if (isDirectory()) {
                if (canRead()) state = NORMAL;
                else state = FileEntryState.LOCKED;
            } else {
                if (alarmType.isLocalFileSound()) state = NORMAL;
                else state = FileEntryState.DISABLED;
            }
        }


        FileEntry(final File file) {
            this(file.getAbsolutePath());
        }

        @Override
        public boolean equals(Object object) {
            return (object instanceof File) &&
                    getAbsolutePath().equals(((File) object).getAbsolutePath());
        }


    } //FileEntry


    /**
     * Sort FileEntry list
     */
    protected static class FileEntrySorter implements Comparator<FileEntry> {

        @Override
        public int compare(FileEntry lhs, FileEntry rhs) {
            if (lhs == null) return -1;
            else if (rhs == null) return 1;
            else if (lhs.isDirectory() && !rhs.isDirectory()) return -1;
            else if (!lhs.isDirectory() && rhs.isDirectory()) return 1;
            else return lhs.getName().compareTo(rhs.getName());
        }

    } //FileEntrySorter


    public LocalDialog() {
        super();
    }


    @Override
    public LocalDialog getThis() {
        return this;
    }


    public static class Builder extends PlayerDialog.Builder<LocalDialog> {

        public Builder(final @NonNull Context context) {
            super(context);
        }

        public LocalDialog newInstance() {
            return new LocalDialog().setContext(context);
        }


    } //Builder


    @Override
    public String getTitle() {
        int resid = NO_RESOURCE;
        switch (alarmType) {
            case LOCAL_FILE:
                resid = R.string.select_song;
                break;
            case LOCAL_FOLDER:
                resid = R.string.select_folder;
                break;
        }
        return (resid != NO_RESOURCE)? context.getString(resid) : null;
    }


    /**
     * @param mimeTypes Set mime types list (separated by ;)
     * @return This
     */
    public LocalDialog setMimeTypes(final @NonNull String mimeTypes) {
        final String[] types = mimeTypes.split(";");
        this.mimeTypes = new ArrayList<>();
        for(String type : types) this.mimeTypes.add(type.trim());
        return this;
    }


    /**
     * @param mimeTypes Set mime types list
     * @return This
     */
    public LocalDialog setMimeTypes(final @NonNull String[] mimeTypes) {
        this.mimeTypes = new ArrayList<>();
        for(String type : mimeTypes) this.mimeTypes.add(type.trim());
        return this;
    }


    /**
     * @param extensions Set extensions list (separated by ;)
     * @return This
     */
    public LocalDialog setExtensions(final @NonNull String extensions) {
        final String[] types = extensions.split(";");
        this.extensions = new ArrayList<>();
        for(String type : types) this.extensions.add("." + type.toLowerCase());
        return this;
    }


    /**
     * @param extensions Set extensions list
     * @return This
     */
    public LocalDialog setExtensions(final @NonNull String[] extensions) {
        this.extensions = new ArrayList<>();
        for(String type : extensions) this.extensions.add(type.toLowerCase());
        return this;
    }


    /**
     * Build the list of allowed/visible files and folders
     * Build the view with path info and contents
     * @return Viewgroup for the material dialog customView
     */
    protected ViewGroup buildContents() {
        //Disable "Choose" button if file mode
        if (alarmType.isLocalFileSound() && (selectedItem == null)) enablePositiveButton(false);

        //Stop current play (if any)
        stopPlayer();

        //Reload directory contents
        final File[] contents = currentFolder.listFiles();
        if (contents != null) {
            contentsFolder = new ArrayList<>();
            if (currentFolder.hasParentFile()) contentsFolder.add(null);    //Add "Go back" item
            for (File file : contents) {
                final FileEntry fileE = new FileEntry(file);
                if (fileE.isDirectory()) contentsFolder.add(fileE);
                else {
                    boolean add = false;
                    if (extensions != null) add = fileE.hasExtensions(extensions);
                    if (!add) {
                        if (mimeTypes != null) add = (fileE.findMimeType(mimeTypes) != null);
                        else add = (extensions == null);
                    }
                    if (add) {
                        contentsFolder.add(fileE);
                        //Dialog startup: find out selectedItem num
                        if ((selectedItem != null) && (selectedItemNum == NO_ITEM) &&
                                selectedItem.equals(fileE)) selectedItemNum = contentsFolder.size() - 1;
                    }
                }
            }
            Collections.sort(contentsFolder, new FileEntrySorter());
        }
        else contentsFolder = null;

        final LocalDialogAdapter adapter;
        if (contentsView == null) {
            //Build base view
            contentsView = (ViewGroup) LayoutInflater
                    .from(getContext()).inflate(R.layout.local_dialog_content, null);
            currentFolderText = (TextView) contentsView.findViewById(R.id.current_path);
            recyclerView = (RecyclerView) contentsView.findViewById(R.id.folder_contents);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setHasFixedSize(true);
            adapter = new LocalDialogAdapter();
            recyclerView.setAdapter(adapter);
            recyclerView.setNestedScrollingEnabled(false);
        }
        else adapter = (LocalDialogAdapter) recyclerView.getAdapter();

        currentFolderText.setText(currentFolder.getAbsolutePath());
        adapter.swapItems(contentsFolder);

        return contentsView;
    }


    public class LocalDialogAdapter extends RecyclerView.Adapter<LocalDialogAdapter.ViewHolder> {

        private List<FileEntry> items;


        public void swapItems(final List<FileEntry> items) {
            this.items = items;
            notifyDataSetChanged();
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new LocalDialog.LocalDialogAdapter
                    .ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.local_dialog_item, parent, false));
        }


        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.setItem(items.get(position), position);
        }


        @Override
        public int getItemCount() {
            return (items != null)? items.size() : 0;
        }


        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View view;
            public final ImageView icon;
            public final TextView item;
            public FileEntry entry = null;
            public int position = NO_ITEM;

            public ViewHolder(View itemView) {
                super(itemView);
                view = itemView;
                icon = (ImageView) itemView.findViewById(R.id.icon);
                item = (TextView) itemView.findViewById(R.id.item);
            }


            public ViewHolder setItem(final FileEntry entry, final int position) {
                this.entry = entry;
                this.position = position;
                final boolean isSelected;
                final FileEntryState state;
                if (entry == null) {
                    icon.setImageResource(R.drawable.ico_folder_back);
                    item.setText(R.string.folder_go_up);
                    isSelected = false;
                    state = NORMAL;
                } else  {
                    if (isPlaying(FnUtil.fileToUri(entry)))
                        icon.setImageResource(R.drawable.ico_file_playing);
                    else icon.setImageResource(FnUtil.getDrawableFile(entry));
                    item.setText(entry.getName());
                    isSelected =  ((selectedItem != null) && selectedItem.equals(entry));
                    state = entry.getState();
                }
                final boolean setClickListener;
                final int color;
                switch(state) {
                    case NORMAL:
                        if (isSelected) {
                            //color = ContextCompat.getColor(getContext(), R.color.whiteText);
                            color = GuiUtil.getColorText(getContext());
                            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccentLighter));
                            icon.setColorFilter(color);
                            item.setTextColor(color);
                        } else {
                            color = GuiUtil.getColorText(getContext());
                            view.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
                            icon.setColorFilter(color);
                            item.setTextColor(color);
                        }
                        setClickListener = true;
                        break;
                    case DISABLED:
                        //Can not be selected
                        color = ContextCompat.getColor(getContext(), R.color.disabledText);
                        view.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
                        icon.setColorFilter(color);
                        item.setTextColor(color);
                        setClickListener = (!entry.isDirectory() && !alarmType.isLocalFileSound());
                        break;
                    case LOCKED:
                        //Can not be selected
                        color = ContextCompat.getColor(getContext(), R.color.warningText);
                        view.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
                        icon.setColorFilter(color);
                        item.setTextColor(color);
                    default:
                        setClickListener = false;
                }
                if (setClickListener)
                    view.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            if (entry == null) currentFolder = currentFolder.getParentFile();
                            else {
                                if (entry.isDirectory()) currentFolder = entry;
                                else {
                                    int pos;
                                    if (alarmType.isLocalFileSound()) {
                                        if ((selectedItem == null) || !entry.equals(selectedItem)) {
                                            if (selectedItemNum != NO_ITEM) notifyItemChanged(selectedItemNum);
                                            selectedItem = entry;
                                            selectedItemNum  = position;
                                            enablePositiveButton(true);
                                            play(FnUtil.fileToUri(entry));
                                        } else {
                                            selectedItem = null;
                                            selectedItemNum  = NO_ITEM;
                                            enablePositiveButton(false);
                                            stopPlayer();
                                        }
                                    }
                                    else if (isPlaying(FnUtil.fileToUri(entry))) stopPlayer();
                                    else play(FnUtil.fileToUri(entry));
                                    notifyItemChanged(position);
                                    return;
                                }
                            }
                            selectedItem = null;
                            selectedItemNum  = NO_ITEM;
                            buildContents();
                        } //onClick

                    });
                else view.setOnClickListener(null);
                return this;
            }

        } //ViewHolder


    } //LocalDialogAdapter


    /**
     * Build the configured dialog
     * @return Created material dialog
     */
    @Override
    public MaterialDialog.Builder builder() {
        final MaterialDialog.Builder builder = super.builder();
        try {
            contentsView = null;
            if (currentFolder == null) {
                currentFolder = new FileEntry(FnUtil.uriStringToFile(soundSource));
                if (!currentFolder.exists())
                    currentFolder = new FileEntry(Environment.getExternalStorageDirectory());
                else if (currentFolder.isFile()) {
                    selectedItem = currentFolder;
                    //Let buildContents() set selectedItemNum
                    currentFolder = currentFolder.getParentFile();
                }
            }
        } catch(Exception e) {
            selectedItem = null;
            currentFolder = new FileEntry(Environment.getExternalStorageDirectory());
        }
        builder.customView(buildContents(), false)
                .positiveText(R.string.btn_choose)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //An item has been chosen
                        if (alarmType.isLocalFileSound())
                            callback.onSoundSelected(alarmType, selectedItem.getPair());
                        else callback.onSoundSelected(alarmType, currentFolder.getPair());
                    }

                })
                .negativeText(R.string.btn_cancel);
        return builder;
    }


    @Override
    public void onShow(DialogInterface dialogInterface) {
        super.onShow(dialogInterface);
        //Scroll to selected item
        if (selectedItemNum > NO_ITEM) {
            int lastVisiblePosition;
            int firstVisiblePosition;
            if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                lastVisiblePosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                firstVisiblePosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            } else if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                lastVisiblePosition = ((GridLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                firstVisiblePosition = ((GridLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            } else {
                //throw new IllegalStateException("Unsupported layout manager type: " + rec.getLayoutManager().getClass().getName());
                return;
            }
            //Is the object outside visible range?
            if ((lastVisiblePosition < selectedItemNum) || (firstVisiblePosition > selectedItemNum)) {
                final int fScrollIndex = selectedItemNum;
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.requestFocus();
                        recyclerView.scrollToPosition(fScrollIndex);
                    }
                });
            }
        }
    }


}
