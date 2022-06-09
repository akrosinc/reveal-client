package org.smartregister.family.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.RecyclerViewProvider;
import org.smartregister.reveal.R;
import org.smartregister.family.fragment.BaseFamilyRegisterFragment;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.contract.SmartRegisterClients;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;
import org.smartregister.view.viewholder.OnClickFormLauncher;

import java.text.MessageFormat;
import java.util.Set;

/**
 * Created by keyman on 13/11/2018.
 */

public class FamilyRegisterProvider implements RecyclerViewProvider<FamilyRegisterProvider.RegisterViewHolder> {

    private final LayoutInflater inflater;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;

    private View.OnClickListener onClickListener;
    private View.OnClickListener paginationClickListener;

    private Context context;
    private CommonRepository commonRepository;

    boolean familyHeadFirstNameEnabled = Utils.getBooleanProperty(Constants.Properties.FAMILY_HEAD_FIRSTNAME_ENABLED);

    protected CommonRepository familyMemberRegisterRepository;

    public FamilyRegisterProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.visibleColumns = visibleColumns;

        this.onClickListener = onClickListener;
        this.paginationClickListener = paginationClickListener;

        this.context = context;
        this.commonRepository = commonRepository;
        familyMemberRegisterRepository = Utils.context().commonrepository(Utils.metadata().familyMemberRegister.tableName);
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        CommonPersonObjectClient pc = ( CommonPersonObjectClient ) client;
        if (visibleColumns.isEmpty()) {
            if (familyHeadFirstNameEnabled) {
                String familyHeadId = pc.getColumnmaps().get(DBConstants.KEY.FAMILY_HEAD);

                final CommonPersonObject familyHeadObject = familyMemberRegisterRepository.findByBaseEntityId(familyHeadId);

                String familyHeadName = "";
                if (familyHeadObject != null && familyHeadObject.getColumnmaps() != null)
                    familyHeadName = familyHeadObject.getColumnmaps().get(DBConstants.KEY.FIRST_NAME);

                pc.getColumnmaps().put(Constants.KEY.FAMILY_HEAD_NAME, familyHeadName);
            }
            populatePatientColumn(pc, client, viewHolder);
            populateLastColumn(pc, viewHolder);

            return;
        }
    }

    @Override
    public void getFooterView(RecyclerView.ViewHolder viewHolder, int currentPageCount, int totalPageCount, boolean hasNext, boolean hasPrevious) {
        FooterViewHolder footerViewHolder = (FooterViewHolder) viewHolder;
        footerViewHolder.pageInfoView.setText(
                MessageFormat.format(context.getString(org.smartregister.reveal.R.string.str_page_info), currentPageCount,
                        totalPageCount));

        footerViewHolder.nextPageView.setVisibility(hasNext ? View.VISIBLE : View.INVISIBLE);
        footerViewHolder.previousPageView.setVisibility(hasPrevious ? View.VISIBLE : View.INVISIBLE);

        footerViewHolder.nextPageView.setOnClickListener(paginationClickListener);
        footerViewHolder.previousPageView.setOnClickListener(paginationClickListener);
    }

    private void populatePatientColumn(CommonPersonObjectClient pc, SmartRegisterClient client, final RegisterViewHolder viewHolder) {

        String firstName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String famName;

        if (familyHeadFirstNameEnabled) {

            String familyHeadFirstName = Utils.getValue(pc.getColumnmaps(), Constants.KEY.FAMILY_HEAD_NAME, true);
            famName = context.getString(R.string.family_profile_title_with_firstname, familyHeadFirstName, firstName);

        } else {

            famName = context.getString(R.string.family_profile_title, firstName);
        }

        fillValue(viewHolder.patientName, famName);

        String villageTown = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.VILLAGE_TOWN, true);
        fillValue((viewHolder.villageTown), villageTown);

        View patient = viewHolder.patientColumn;
        attachPatientOnclickListener(patient, client);

        View dueButton = viewHolder.dueButton;
        attachDosageOnclickListener(dueButton, client);

        viewHolder.registerColumns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.patientColumn.performClick();
            }
        });

        viewHolder.dueWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.dueButton.performClick();
            }
        });
    }

    private void populateLastColumn(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
        if (commonRepository != null) {
            CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(pc.entityId());
            if (commonPersonObject != null) {
                viewHolder.dueButton.setVisibility(View.VISIBLE);
                viewHolder.dueButton.setText("Home Visit");
                viewHolder.dueButton.setAllCaps(true);
            } else {
                viewHolder.dueButton.setVisibility(View.GONE);
            }
        }
    }

    private void attachPatientOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(R.id.VIEW_ID, BaseFamilyRegisterFragment.CLICK_VIEW_NORMAL);
    }

    private void attachDosageOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(R.id.VIEW_ID, BaseFamilyRegisterFragment.CLICK_VIEW_DOSAGE_STATUS);
    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption serviceModeOption, FilterOption searchFilter, SortOption sortOption) {
        return null;
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {//Implement Abstract Method
    }

    @Override
    public OnClickFormLauncher newFormLauncher(String formName, String entityId, String metaData) {
        return null;
    }

    @Override
    public LayoutInflater inflater() {
        return inflater;
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.family_register_list_row, parent, false);

        return new RegisterViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder createFooterHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.smart_register_pagination, parent, false);
        return new FooterViewHolder(view);
    }

    @Override
    public boolean isFooterViewHolder(RecyclerView.ViewHolder viewHolder) {
        return FooterViewHolder.class.isInstance(viewHolder);
    }


    public static void fillValue(TextView v, String value) {
        if (v != null)
            v.setText(value);

    }

    ////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////

    public static class RegisterViewHolder extends RecyclerView.ViewHolder {
        public TextView patientName;
        public TextView villageTown;
        public Button dueButton;
        public View patientColumn;
        public View memberIcon;

        public View registerColumns;
        public View dueWrapper;

        public RegisterViewHolder(View itemView) {
            super(itemView);

            patientName = itemView.findViewById(R.id.patient_name);

            villageTown = itemView.findViewById(R.id.village_town);
            dueButton = itemView.findViewById(R.id.due_button);

            patientColumn = itemView.findViewById(R.id.patient_column);

            memberIcon = itemView.findViewById(R.id.member_icon_layout);

            registerColumns = itemView.findViewById(R.id.register_columns);
            dueWrapper = itemView.findViewById(R.id.due_button_wrapper);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public TextView pageInfoView;
        public Button nextPageView;
        public Button previousPageView;

        public FooterViewHolder(View view) {
            super(view);

            nextPageView = view.findViewById(org.smartregister.reveal.R.id.btn_next_page);
            previousPageView = view.findViewById(org.smartregister.reveal.R.id.btn_previous_page);
            pageInfoView = view.findViewById(org.smartregister.reveal.R.id.txt_page_info);
        }
    }

}
