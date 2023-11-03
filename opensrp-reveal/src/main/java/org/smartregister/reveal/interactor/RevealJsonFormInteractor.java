package org.smartregister.reveal.interactor;

import static com.vijay.jsonwizard.constants.JsonFormConstants.BARCODE;
import static com.vijay.jsonwizard.constants.JsonFormConstants.EDIT_TEXT;
import static com.vijay.jsonwizard.constants.JsonFormConstants.LABEL;
import static com.vijay.jsonwizard.constants.JsonFormConstants.MULTI_SELECT_LIST;
import static com.vijay.jsonwizard.constants.JsonFormConstants.NATIVE_RADIO_BUTTON;
import static com.vijay.jsonwizard.constants.JsonFormConstants.REPEATING_GROUP;
import static com.vijay.jsonwizard.constants.JsonFormConstants.TOASTER_NOTES;

import androidx.annotation.NonNull;

import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import org.smartregister.reveal.util.Country;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.util.Utils;
import org.smartregister.reveal.widget.GeoWidgetFactory;
import org.smartregister.reveal.widget.RevealBarcodeFactory;
import org.smartregister.reveal.widget.RevealEditTextFactory;
import org.smartregister.reveal.widget.RevealLabelFactory;
import org.smartregister.reveal.widget.RevealMultiSelectListFactory;
import org.smartregister.reveal.widget.RevealRadioButtonFactory;
import org.smartregister.reveal.widget.RevealRepeatingGroupFactory;
import org.smartregister.reveal.widget.RevealToasterNotesFactory;

/**
 * Created by samuelgithengi on 12/13/18.
 */
public class RevealJsonFormInteractor extends JsonFormInteractor {


    private static final RevealJsonFormInteractor INSTANCE = new RevealJsonFormInteractor();

    private static final String GEOWIDGET = "geowidget";

    public static JsonFormInteractor getInstance() {
        return INSTANCE;
    }

    @Override
    protected void registerWidgets() {
        super.registerWidgets();

        if (getBuildCountry() == Country.NAMIBIA) {
            map.put(GEOWIDGET, new GeoWidgetFactory());
        } else {
            map.put(GEOWIDGET, new GeoWidgetFactory(false));
        }

        map.put(EDIT_TEXT, new RevealEditTextFactory());
        map.put(NATIVE_RADIO_BUTTON, new RevealRadioButtonFactory());
        map.put(LABEL, new RevealLabelFactory());
        map.put(TOASTER_NOTES, new RevealToasterNotesFactory());
        map.put(MULTI_SELECT_LIST,new RevealMultiSelectListFactory());

//        if (getBuildCountry() == Country.SENEGAL || getBuildCountry() == Country.SENEGAL_EN){
            map.put(REPEATING_GROUP, new RevealRepeatingGroupFactory());
//        }

        if(Utils.isCountryBuild(Country.NIGERIA)){
            map.put(BARCODE, new RevealBarcodeFactory());
        }

    }

    @NonNull
    private Country getBuildCountry() {
        return PreferencesUtil.getInstance().getBuildCountry();
    }

}
