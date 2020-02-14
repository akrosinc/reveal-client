package org.smartregister.reveal.adapter;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;
import org.smartregister.reveal.BaseUnitTest;
import org.smartregister.reveal.R;
import org.smartregister.reveal.model.OfflineMapModel;
import org.smartregister.reveal.util.TestingUtils;
import org.smartregister.reveal.viewholder.DownloadedOfflineMapViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.smartregister.reveal.model.OfflineMapModel.OfflineMapStatus.DOWNLOADED;
import static org.smartregister.reveal.model.OfflineMapModel.OfflineMapStatus.READY;

/**
 * Created by Richard Kareko on 1/27/20.
 */

public class DownloadedOfflineMapAdapterTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private View.OnClickListener onClickListener;

    private DownloadedOfflineMapAdapter adapter;

    private Context context = RuntimeEnvironment.application;

    private List<OfflineMapModel> offlineMapModelList;

    @Before
    public void setUp() {
        adapter = new DownloadedOfflineMapAdapter(context, onClickListener);
        offlineMapModelList = new ArrayList<>();
        offlineMapModelList.add(TestingUtils.getOfflineMapModel());
    }

    @Test
    public void testOnCreateViewHolder() {
        LinearLayout vg = new LinearLayout(context);
        DownloadedOfflineMapViewHolder holder = adapter.onCreateViewHolder(vg,0);
        assertNotNull(holder);
        assertNotNull(Whitebox.getInternalState(holder, "tvOfflineMapNameLabel"));
        assertNotNull(Whitebox.getInternalState(holder, "tvDownloadingLabel"));
        assertNotNull(Whitebox.getInternalState(holder, "offlineMapCheckBox"));

    }

    @Test
    public void testOnbindViewHolder() {
        OfflineMapModel model = TestingUtils.getOfflineMapModel();
        model.setOfflineMapStatus(READY);
        adapter.setOfflineMapModels(Collections.singletonList(model));
        LinearLayout vg = new LinearLayout(context);
        DownloadedOfflineMapViewHolder holder = adapter.onCreateViewHolder(vg,0);
        adapter.onBindViewHolder(holder, 0);

        assertTrue((holder.itemView.findViewById(R.id.offline_map_checkbox).isEnabled()));
        assertFalse((holder.itemView.findViewById(R.id.offline_map_checkbox)).isSelected());
        assertEquals(View.VISIBLE, holder.itemView.findViewById(R.id.downloading_label).getVisibility());

    }

    @Test
    public void testGetItemCount() {
        assertEquals(0, adapter.getItemCount());
        adapter.setOfflineMapModels(offlineMapModelList);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testSetOfflineMapModels() {
        adapter.setOfflineMapModels(new ArrayList<>());
        adapter = spy(adapter);

        assertEquals(0, adapter.getItemCount());

        OfflineMapModel model = new OfflineMapModel();
        model.setOfflineMapStatus(DOWNLOADED);
        model.setLocation(TestingUtils.gson.fromJson(TestingUtils.operationalAreaGeoJSON, org.smartregister.domain.Location.class));

        adapter.setOfflineMapModels(Collections.singletonList(model));
        assertEquals(1, adapter.getItemCount());
        verify(adapter).notifyDataSetChanged();

    }

}
