//package org.smartregister.reveal.template;
//
//import android.os.Bundle;
//import android.view.View;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import androidx.annotation.Nullable;
//import com.vijay.jsonwizard.constants.JsonFormConstants;
//import java.util.List;
//import org.smartregister.domain.Task;
//import org.smartregister.reveal.R;
//import org.smartregister.reveal.application.RevealApplication;
//import org.smartregister.reveal.util.AppExecutors;
//
///**
// * Concrete template: one JSON-driven form section at the top, followed by a
// * searchable task list below.
// *
// * <h3>Responsibilities of this class</h3>
// * <ol>
// *   <li>Sets up the layout ({@code template_form_recycler.xml}).</li>
// *   <li>Commits {@link EmbeddedFormFragment} into {@code R.id.container} and
// *       {@link TaskRecyclerFragment} into {@code R.id.container_recycler}.</li>
// *   <li>Exposes abstract hooks so subclasses supply domain logic without any
// *       coupling to HDSS or other specific repositories.</li>
// * </ol>
// *
// * <h3>Subclass contract</h3>
// * Subclasses must implement:
// * <ul>
// *   <li>{@link #getParentFormJson()}       — the JSON string for the top form</li>
// *   <li>{@link #getFormFragmentTag()}      — tag for the EmbeddedFormFragment</li>
// *   <li>{@link #getTaskDisplayProvider()}  — display labels/colours per task row</li>
// *   <li>{@link #loadTaskItems(TaskItemsCallback)} — fetch task rows off the main thread</li>
// *   <li>{@link #onFormSaved(String)}       — handle saved parent form JSON</li>
// *   <li>{@link #onTaskTapped(Task)}        — open the appropriate form for a task</li>
// *   <li>{@link #onTaskLongPressed(Task)}   — handle reset / undo for a task</li>
// * </ul>
// *
// * Optionally override:
// * <ul>
// *   <li>{@link #getSectionHeaderLabel()}   — text above the task list (default "Tasks")</li>
// *   <li>{@link #onFragmentDataReady}       — react between form save and task refresh</li>
// * </ul>
// */
//public abstract class FormRecyclerFormTemplate extends TemplateHostActivity
//        implements TaskRecyclerFragment.TaskRecyclerListener {
//
//    /** Tag used when committing {@link EmbeddedFormFragment}. */
//    public static final String TAG_FORM   = "embedded_form";
//    /** Tag used when committing {@link TaskRecyclerFragment}. */
//    public static final String TAG_RECYCLER = "task_recycler";
//
//    protected AppExecutors    appExecutors;
//    protected TaskRecyclerFragment taskRecyclerFragment;
//
//    private LinearLayout progressOverlay;
//    private TextView     tvProgressMessage;
//    private TextView     tvSectionHeader;
//
//    /* ------------------------------------------------------------------ lifecycle */
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        /*
//         * Set our layout BEFORE super.onCreate() so JsonFormBaseActivity finds
//         * R.id.tb_top, R.id.container, R.id.native_form_activity in our layout.
//         */
//        setContentView(R.layout.template_form_recycler_form);
//
//        /*
//         * Inject the parent form JSON into the Intent so JsonFormBaseActivity.init()
//         * can parse it normally via getJsonForm().
//         */
//        getIntent().putExtra(JsonFormConstants.JSON_FORM_KEY.JSON, getParentFormJson());
//
//        super.onCreate(savedInstanceState);
//
//        appExecutors  = RevealApplication.getInstance().getAppExecutors();
//        progressOverlay   = findViewById(R.id.layout_progress);
//        tvProgressMessage = findViewById(R.id.tv_progress_message);
//        tvSectionHeader   = findViewById(R.id.tv_section_header);
//
//        setupTaskRecyclerFragment();
//        refreshTaskList();
//    }
//
//    /* ------------------------------------------------------------------ RevealJsonFormActivity */
//
//    /**
//     * Commits the {@link EmbeddedFormFragment} into {@code R.id.container}.
//     * Called by {@link com.vijay.jsonwizard.activities.JsonFormBaseActivity#onCreate}.
//     */
//    @Override
//    public void initializeFormFragment() {
//        EmbeddedFormFragment formFragment = EmbeddedFormFragment.newInstance(
//                JsonFormConstants.FIRST_STEP_NAME, TAG_FORM);
//        getSupportFragmentManager()
//                .beginTransaction()
//                .add(R.id.container, formFragment, TAG_FORM)
//                .commit();
//
//        EmbeddedFormFragment formFragmentSecond = EmbeddedFormFragment.newInstance(
//            JsonFormConstants.FIRST_STEP_NAME, TAG_FORM);
//        getSupportFragmentManager()
//            .beginTransaction()
//            .add(R.id.container_second, formFragmentSecond, TAG_FORM)
//            .commit();
//    }
//
//    /* ------------------------------------------------------------------ TemplateHostActivity */
//
//    /**
//     * Called when the embedded form fragment passes validation and saves.
//     * Stores the JSON, calls the abstract hook, then refreshes the task list.
//     */
//    @Override
//    protected void onFragmentDataReady(String fragmentTag, String json) {
//        if (TAG_FORM.equals(fragmentTag)) {
//            onFormSaved(json);
//            refreshTaskList();
//        }
//    }
//
//    /* ------------------------------------------------------------------ task recycler */
//
//    private void setupTaskRecyclerFragment() {
//        taskRecyclerFragment = TaskRecyclerFragment.newInstance();
//        taskRecyclerFragment.setDisplayProvider(getTaskDisplayProvider());
//        taskRecyclerFragment.setListener(this);
//
//        getSupportFragmentManager()
//                .beginTransaction()
//                .add(R.id.container_recycler, taskRecyclerFragment, TAG_RECYCLER)
//                .commit();
//    }
//
//    /**
//     * Fetches task rows on the disk thread and pushes them into the recycler.
//     * Called on create and after every form save.
//     */
//    public void refreshTaskList() {
//        showOverlayProgress("Loading Tasks…");
//
//        appExecutors.diskIO().execute(() -> {
//
//            loadTaskItems(new TaskItemsCallback() {
//                @Override
//                public void onItemsLoaded(List<TaskRowItem> items) {
//                    appExecutors.mainThread().execute(() -> {
//                        hideOverlayProgress();
//                        boolean hasTasks = items != null && !items.isEmpty();
//                        setSectionVisible(hasTasks);
//                        if (taskRecyclerFragment != null) {
//                            taskRecyclerFragment.setItems(items != null ? items : List.of());
//                        }
//                    });
//                }
//            });
//        });
//    }
//
//    private void setSectionVisible(boolean visible) {
//        int vis = visible ? View.VISIBLE : View.GONE;
//        findViewById(R.id.view_divider).setVisibility(vis);
//        tvSectionHeader.setVisibility(vis);
//        if (visible) {
//            String label = getSectionHeaderLabel();
//            if (label != null) tvSectionHeader.setText(label);
//        }
//    }
//
//    /* ------------------------------------------------------------------ TaskRecyclerListener */
//
//    @Override
//    public void onTaskTap(Task task) {
//        onTaskTapped(task);
//    }
//
//    @Override
//    public void onTaskLongPress(Task task) {
//        onTaskLongPressed(task);
//    }
//
//    /* ------------------------------------------------------------------ progress */
//
//    public void showOverlayProgress(String message) {
//        if (tvProgressMessage != null) tvProgressMessage.setText(message);
//        if (progressOverlay   != null) progressOverlay.setVisibility(View.VISIBLE);
//    }
//
//    public void hideOverlayProgress() {
//        if (progressOverlay != null) progressOverlay.setVisibility(View.GONE);
//    }
//
//    /* ================================================================== abstract contract */
//
//    /**
//     * Return the fully-populated JSON string for the parent form.
//     * Called in {@link #onCreate} before {@code super.onCreate()}.
//     * Use {@link org.smartregister.reveal.util.RevealJsonFormUtils#getFormJSON} here.
//     */
//    protected abstract String getParentFormJson();
//
//    /**
//     * Tag that identifies the {@link EmbeddedFormFragment} within the fragment manager.
//     * Can usually just return {@link #TAG_FORM}.
//     */
//    protected String getFormFragmentTag() { return TAG_FORM; }
//
//    /**
//     * Return the {@link TaskDisplayProvider} that drives row labels and colours.
//     * Typically a small inner class or dedicated class per use-case.
//     */
//    protected abstract TaskDisplayProvider getTaskDisplayProvider();
//
//    /**
//     * Fetch the task rows to display.  Called on the disk thread.
//     * Implementations should build {@link TaskRowItem}s from the task repository
//     * and any other needed source, then invoke {@code callback.onItemsLoaded(items)}.
//     *
//     * <pre>
//     *   protected void loadTaskItems(TaskItemsCallback callback) {
//     *       List&lt;TaskRowItem&gt; items = new ArrayList&lt;&gt;();
//     *       for (Task task : taskRepo.getTasksByEntity(structureId)) {
//     *           items.add(TaskRowItem.from(task, getTaskDisplayProvider(), formatDate(task)));
//     *       }
//     *       callback.onItemsLoaded(items);
//     *   }
//     * </pre>
//     */
//    protected abstract void loadTaskItems(TaskItemsCallback callback);
//
//    /**
//     * Handle the saved parent form JSON (persist via interactor, update task status, etc.).
//     * Called on the main thread after {@link EmbeddedFormFragment#finishWithResult}.
//     *
//     * @param json the complete form JSON with all filled values
//     */
//    protected abstract void onFormSaved(String json);
//
//    /**
//     * Handle a tap on a task row action button.
//     * Open the appropriate form via {@link org.smartregister.reveal.util.RevealJsonFormUtils#startJsonForm}.
//     */
//    protected abstract void onTaskTapped(Task task);
//
//    /**
//     * Handle a long-press on a task row (e.g. show a reset dialog).
//     */
//    protected abstract void onTaskLongPressed(Task task);
//
//    /**
//     * Optional: text shown as the section header above the task list.
//     * Default is the string resource {@code R.string.tasks} if present, else "Tasks".
//     */
//    protected String getSectionHeaderLabel() {
//        return getString(R.string.tasks_section_header);
//    }
//
//    /* ------------------------------------------------------------------ callback interface */
//
//    /**
//     * Callback passed to {@link #loadTaskItems} so the fetch can complete
//     * asynchronously and hand results back to the template.
//     */
//    public interface TaskItemsCallback {
//        void onItemsLoaded(List<TaskRowItem> items);
//    }
//}
