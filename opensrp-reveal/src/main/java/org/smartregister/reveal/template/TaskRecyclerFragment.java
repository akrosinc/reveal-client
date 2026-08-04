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
import android.widget.TextView;

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
 * Generic fragment that renders a searchable list of {@link Task}s.
 *
 * <p>All domain-specific concerns (which tasks to load, how to label them,
 * which form to open) are delegated outward:
 * <ul>
 *   <li>{@link TaskDisplayProvider} — supplies display strings and colours per row</li>
 *   <li>{@link TaskRecyclerListener} — receives tap/long-press events so the host
 *       activity can open forms, show dialogs, etc.</li>
 * </ul>
 *
 * <p>The fragment itself never reads from any repository.  The host calls
 * {@link #setItems(List)} to push data in, and the fragment notifies the host
 * via {@link TaskRecyclerListener} when the user interacts with a row.
 *
 * <h3>Usage</h3>
 * <pre>
 *   TaskRecyclerFragment fragment = new TaskRecyclerFragment();
 *   fragment.setDisplayProvider(myProvider);
 *   // add to layout, then when data is ready:
 *   fragment.setItems(taskRowItems);
 * </pre>
 */
public class TaskRecyclerFragment extends Fragment implements TaskRowCallbacks {

    /* ------------------------------------------------------------------ listener interface */

    /**
     * Implemented by the host activity (or parent fragment) to react to row events.
     */
    public interface TaskRecyclerListener {
        /** User tapped the action button on a task row. */
        void onTaskTap(Task task);
        /** User long-pressed an already-visited task row (e.g. to reset it). */
        void onTaskLongPress(Task task);
    }

    /* ------------------------------------------------------------------ state */

    private TaskDisplayProvider  displayProvider;
    private TaskRecyclerListener listener;

    private TaskRecyclerAdapter  adapter;
    private RecyclerView         recyclerView;
    private TextView             tvEmpty;

    /* ------------------------------------------------------------------ factory */

    public static TaskRecyclerFragment newInstance() {
        return new TaskRecyclerFragment();
    }

    /* ------------------------------------------------------------------ Fragment lifecycle */

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TaskRecyclerListener) {
            listener = (TaskRecyclerListener) context;
        }
        // listener can also be set programmatically via setListener()
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_recycler, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEmpty     = view.findViewById(R.id.tv_empty);
        recyclerView = view.findViewById(R.id.recycler_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        if (displayProvider == null) {
            throw new IllegalStateException(
                    "TaskRecyclerFragment requires a TaskDisplayProvider. "
                    + "Call setDisplayProvider() before committing the fragment.");
        }

        adapter = new TaskRecyclerAdapter(requireContext(), displayProvider, this);
        recyclerView.setAdapter(adapter);

        wireSearch(view);
    }

    /* ------------------------------------------------------------------ public API */

    /**
     * Must be called before the fragment is attached to a layout.
     */
    public void setDisplayProvider(TaskDisplayProvider provider) {
        this.displayProvider = provider;
    }

    /**
     * Override the listener set via {@link #onAttach} if needed.
     */
    public void setListener(TaskRecyclerListener listener) {
        this.listener = listener;
    }

    /**
     * Push a new list of items into the recycler.
     * Safe to call from any thread via {@code requireActivity().runOnUiThread()}.
     */
    public void setItems(List<TaskRowItem> items) {
        if (adapter != null) {
            adapter.setItems(items);
            tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    /* ------------------------------------------------------------------ TaskRowCallbacks */

    @Override
    public void onTaskTap(Task task) {
        if (listener != null) listener.onTaskTap(task);
    }

    @Override
    public void onTaskLongPress(Task task) {
        if (listener != null) listener.onTaskLongPress(task);
    }

    /* ------------------------------------------------------------------ search */

    private void wireSearch(View root) {
        EditText etSearch   = root.findViewById(R.id.et_search);
        Button   btnClear   = root.findViewById(R.id.btn_clear_search);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (adapter != null) adapter.filter(s.toString());
                btnClear.setEnabled(s.length() > 0);
            }
        });

        btnClear.setOnClickListener(v -> {
            etSearch.setText("");
            if (adapter != null) adapter.filter("");
        });
    }
}
