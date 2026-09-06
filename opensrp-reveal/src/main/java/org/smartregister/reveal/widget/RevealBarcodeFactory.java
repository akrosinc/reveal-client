package org.smartregister.reveal.widget;

import android.app.Activity;
import android.content.Context;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.vision.barcode.Barcode;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.Utils;
import com.vijay.jsonwizard.widgets.BarcodeFactory;

import org.joda.time.DateTime;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.Event;
import org.smartregister.domain.Obs;
import org.smartregister.domain.db.EventClient;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.reveal.activity.RevealJsonFormActivity;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.FamilyConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

import static org.smartregister.util.JsonFormUtils.STEP1;

public class RevealBarcodeFactory extends BarcodeFactory {

    public static final String CHILD_FIRST_NAME = "childFirstName";
    public static final String SURNAME_OF_CHILD = "surnameOfChild";
    public static final String SEX = "sex";
    public static final String LAST_NAME = "last_name";
    public static final String GENDER = "gender";
    public static final String REFERRAL_QR_CODE = "referralQRCode";
    public static final String REFERRAL_QR_CODE_SEARCH = "Referral QR Code Search";
    public static final String OTHER = "Other";
    public static final Integer OPTION_COUNT = 7;
    public static final String CHILD_REFERRED_HF = "child_referred_hf";
    public static final String REFERRAL_REASON = "referralReason";
    public static final String REFERRED_HF = "referredHf";
    public static final String REFERRAL_REASONS = "referralReasons";
    public static final String OTHER_REASON = "otherReason";
    public static final String DATE_OF_REFERRAL = "dateOfReferral";
    public static final String HEALTH_FACILITY = "health_facility";
    public static final String OTHER_REFFERAL_REASON = "otherRefferalReason";
    public static final String REFERRAL_REASON1 = "referralReason";
    public static final String BIRTHDATE_UNKNOWN = "birthdate_unknown";
    public static final String DOB = "dob";

    @Override
    protected void launchBarcodeScanner(Activity activity, MaterialEditText editText, String barcodeType) {

        super.launchBarcodeScanner(activity, editText, barcodeType);
    }

    @Override
    protected void addOnBarCodeResultListeners(Context context, MaterialEditText editText) {
        if (context instanceof JsonApi) {
            JsonApi jsonApi = (JsonApi) context;
            jsonApi.addOnActivityResultListener(JsonFormConstants.BARCODE_CONSTANTS.BARCODE_REQUEST_CODE,
                    (requestCode, resultCode, data) -> {
                        if (requestCode == JsonFormConstants.BARCODE_CONSTANTS.BARCODE_REQUEST_CODE && resultCode == RESULT_OK) {
                            if (data != null) {
                                Barcode barcode = data.getParcelableExtra(JsonFormConstants.BARCODE_CONSTANTS.BARCODE_KEY);
                                Timber.d("Scanned QR Code %s ", barcode.displayValue);
                                editText.setText(barcode.displayValue);

                            } else
                                Timber.i("NO RESULT FOR QR CODE");
                        }
                    });
        }
    }

}
