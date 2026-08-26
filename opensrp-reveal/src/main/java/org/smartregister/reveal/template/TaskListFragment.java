package org.smartregister.reveal.template;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.domain.Task;
import org.smartregister.reveal.R;

import java.util.List;

/**
 * Self-contained task list component with:
 * <ul>
 *   <li>A count input field + "Generate" button to create child tasks</li>
 *   <li>A lock/unlock mechanism to prevent accidental regeneration</li>
 *   <li>A searchable RecyclerView of tasks</li>
 * </ul>
 *
 * <h3>State machine</h3>
 * <ul>
 *   <li><b>EMPTY</b>     – no tasks exist → count editable, Generate enabled, Unlock hidden</li>
 *   <li><b>LOCKED</b>    – tasks exist → count disabled (shows current count),
 *                          Generate disabled, Unlock visible</li>
 *   <li><b>UNLOCKED</b>  – user tapped Unlock → count editable, Generate enabled,
 *                          warning shown, Unlock hidden</li>
 * </ul>
 *
 * <p>After "Generate" is tapped, the fragment returns to LOCKED state.
 *
 * <h3>Usage</h3>
 * <pre>
 *   TaskListFragment fragment = TaskListFragment.newInstance();
 *   fragment.setDisplayProvider(provider);
 *   fragment.setCallbacks(hostActivity);
 *   // after committing, push data with:
 *   fragment.setItems(items);
 * </pre>
 */
public class TaskListFragment extends Fragment implements TaskRowCallbacks {

    private enum State { EMPTY, LOCKED, UNLOCKED }

    /* ------------------------------------------------------------------ state */
    private TaskDisplayProvider displayProvider;
    private TaskListCallbacks   callbacks;
    private TaskRecyclerAdapter adapter;
    private State               currentState = State.EMPTY;
    private String              countHintText;
    private String              countLabelText;
    private String              emptyMessageText;
    private String              headerText;

    /* ------------------------------------------------------------------ views */
    private EditText     etCount;
    private Button       btnGenerate;
    private Button       btnUnlock;
    private TextView     tvWarning;
    private TextView     tvError;
    private RecyclerView recyclerView;
    private TextView     tvEmpty;
    private LinearLayout layoutSearch;
    private EditText     etSearch;
    private Button       btnClearSearch;

    /* ------------------------------------------------------------------ factory */

    public static TaskListFragment newInstance() {
        return new TaskListFragment();
    }

    /* ------------------------------------------------------------------ configuration */

    public void setDisplayProvider(TaskDisplayProvider provider) {
        this.displayProvider = provider;
    }

    public void setCallbacks(TaskListCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    /** Set the hint text for the count input field. */
    public void setCountHint(String hint) {
        this.countHintText = hint;
    }

    /** Set the label text shown above the count input field. */
    public void setCountLabel(String label) {
        this.countLabelText = label;
    }

    /** Set the empty state message. */
    public void setEmptyMessage(String message) {
        this.emptyMessageText = message;
    }

    /** Set the header text shown above the task list. */
    public void setHeader(String header) {
        this.headerText = header;
    }

    /* ------------------------------------------------------------------ lifecycle */

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (callbacks == null && context instanceof TaskListCallbacks) {
            callbacks = (TaskListCallbacks) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_list_component, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etCount        = view.findViewById(R.id.et_task_count);
        btnGenerate    = view.findViewById(R.id.btn_generate);
        btnUnlock      = view.findViewById(R.id.btn_unlock);
        tvWarning      = view.findViewById(R.id.tv_warning);
        tvError        = view.findViewById(R.id.tv_error);
        recyclerView   = view.findViewById(R.id.recycler_tasks);
        tvEmpty        = view.findViewById(R.id.tv_empty);
        layoutSearch   = view.findViewById(R.id.layout_search);
        etSearch       = view.findViewById(R.id.et_search);
        btnClearSearch = view.findViewById(R.id.btn_clear_search);

        // Recycler setup
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        // Allow the recycler to scroll independently inside the outer NestedScrollView
        recyclerView.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        if (displayProvider == null) {
            throw new IllegalStateException("TaskListFragment requires a TaskDisplayProvider");
        }

        adapter = new TaskRecyclerAdapter(requireContext(), displayProvider, this);
        recyclerView.setAdapter(adapter);

        // Apply configured hint and header
        if (countHintText != null && !countHintText.isEmpty()) {
            etCount.setHint(countHintText);
        }
        if (countLabelText != null && !countLabelText.isEmpty()) {
            android.widget.TextView tvCountLabel = view.findViewById(R.id.tv_count_label);
            if (tvCountLabel != null) {
                tvCountLabel.setText(countLabelText);
                tvCountLabel.setVisibility(View.VISIBLE);
            }
        }
        if (emptyMessageText != null && !emptyMessageText.isEmpty()) {
            tvEmpty.setText(emptyMessageText);
        }

        // Button listeners
        btnGenerate.setOnClickListener(v -> onGenerateClicked());
        btnUnlock.setOnClickListener(v -> transitionTo(State.UNLOCKED));

        // Search wiring
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
                btnClearSearch.setEnabled(s.length() > 0);
            }
        });
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            adapter.filter("");
        });

        // Initial state
        transitionTo(State.EMPTY);
    }

    /* ------------------------------------------------------------------ public API */

    /**
     * Push items into the list and update the component state.
     * Call from the main thread after loading tasks from the DB.
     */
    public void setItems(List<TaskRowItem> items) {
        if (adapter != null) {
            adapter.setItems(items);
        }

        boolean hasItems = items != null && !items.isEmpty();

        if (hasItems) {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            layoutSearch.setVisibility(View.VISIBLE);
            etCount.setText(String.valueOf(items.size()));
            if (getView() != null) {
                View bottomBorder = getView().findViewById(R.id.view_recycler_bottom_border);
                if (bottomBorder != null) bottomBorder.setVisibility(View.VISIBLE);
            }
            transitionTo(State.LOCKED);
        } else {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            layoutSearch.setVisibility(View.GONE);
            if (getView() != null) {
                View bottomBorder = getView().findViewById(R.id.view_recycler_bottom_border);
                if (bottomBorder != null) bottomBorder.setVisibility(View.GONE);
            }
            transitionTo(State.EMPTY);
        }
    }

    /* ------------------------------------------------------------------ state machine */

    private void transitionTo(State newState) {
        currentState = newState;
        switch (newState) {
            case EMPTY:
                etCount.setEnabled(true);
                btnGenerate.setEnabled(true);
                btnUnlock.setVisibility(View.GONE);
                tvWarning.setVisibility(View.GONE);
                break;

            case LOCKED:
                etCount.setEnabled(false);
                btnGenerate.setEnabled(false);
                btnUnlock.setVisibility(View.VISIBLE);
                tvWarning.setVisibility(View.GONE);
                break;

            case UNLOCKED:
                etCount.setEnabled(true);
                etCount.requestFocus();
                btnGenerate.setEnabled(true);
                btnUnlock.setVisibility(View.GONE);
                tvWarning.setVisibility(View.VISIBLE);
                break;
        }
    }

    /* ------------------------------------------------------------------ Generate logic */

    private void onGenerateClicked() {
        String text = etCount.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a number", Toast.LENGTH_SHORT).show();
            return;
        }

        int count;
        try {
            count = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (count < 0) {
            Toast.makeText(requireContext(), "Number must be 0 or greater",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Delegate to host — host cancels existing + creates new + refreshes
        if (callbacks != null) {
            callbacks.onGenerateTasks(count);
        }
        // After generation, the host calls setItems() which transitions to LOCKED
    }

    /* ------------------------------------------------------------------ public queries */

    /**
     * Returns true if tasks have been generated (i.e., the adapter has at least one item).
     */
    public boolean hasGeneratedTasks() {
        return adapter != null && adapter.getItemCount() > 0;
    }

    /**
     * Returns the current state of the fragment (EMPTY, LOCKED, or UNLOCKED).
     */
    public State getCurrentState() {
        return currentState;
    }

    /* ------------------------------------------------------------------ error display */

    /**
     * Shows an error message in the fragment (red text below the task list).
     * Call from the main thread.
     */
    public void showError(String message) {
        if (tvError != null) {
            tvError.setText(message);
            tvError.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hides the error message.
     */
    public void hideError() {
        if (tvError != null) {
            tvError.setVisibility(View.GONE);
            tvError.setText("");
        }
    }

    /* ------------------------------------------------------------------ TaskRowCallbacks */

    @Override
    public void onTaskTap(Task task) {
        if (callbacks != null) callbacks.onTaskTap(task);
    }

    @Override
    public void onTaskLongPress(Task task) {
        if (callbacks != null) callbacks.onTaskLongPress(task);
    }
}
