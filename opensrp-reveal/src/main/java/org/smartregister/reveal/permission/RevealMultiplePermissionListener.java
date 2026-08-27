//package org.smartregister.reveal.permission;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//
//import androidx.annotation.NonNull;
//
//import com.karumi.dexter.MultiplePermissionsReport;
//import com.karumi.dexter.PermissionToken;
//import com.karumi.dexter.listener.PermissionDeniedResponse;
//import com.karumi.dexter.listener.PermissionGrantedResponse;
//import com.karumi.dexter.listener.PermissionRequest;
//import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import io.ona.kujaku.R;
//import timber.log.Timber;
//
//public class RevealMultiplePermissionListener extends BaseMultiplePermissionsListener {
//    private final Context context;
//    private final String title;
//    private final String message;
//    private final String positiveButtonText;
//
//    public RevealMultiplePermissionListener(@NonNull Context context) {
//        this.context = context;
//        this.title = context.getString(R.string.kujaku_permission);
//        this.message = context.getString(R.string.kujaku_permission_reason);
//        this.positiveButtonText = context.getString(android.R.string.ok);
//    }
//
//    @Override
//    public void onPermissionsChecked(MultiplePermissionsReport report) {
//
//        List<PermissionDeniedResponse> deniedPermissionResponses = report.getDeniedPermissionResponses();
//
//        String denied = deniedPermissionResponses.stream()
//                .map(deniedPermissionResponse -> "denied - "+deniedPermissionResponse.getRequestedPermission().getName())
//                .collect(Collectors.joining(","));
//
//        List<PermissionGrantedResponse> grantedPermissionResponses = report.getGrantedPermissionResponses();
//
//        String granted = grantedPermissionResponses.stream()
//                .map(grantedPermissionResponse -> "granted - " + grantedPermissionResponse.getRequestedPermission().getName())
//                .collect(Collectors.joining(","));
//
//        if (report.isAnyPermissionPermanentlyDenied() || !report.areAllPermissionsGranted()) {
//            new AlertDialog.Builder(context)
//                    .setTitle(title)
//                    .setMessage(denied +"\n\n"+ granted+"\n\n"+report.isAnyPermissionPermanentlyDenied())
//                    .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.cancel();
//
//                        }
//                    })
//                    .show();
//        } else {
//            new AlertDialog.Builder(context)
//                    .setTitle(title)
//                    .setMessage("Permissions granted")
//                    .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.cancel();
//
//                        }
//                    })
//                    .show();
//        }
//    }
//
//    @Override
//    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
//                                                   PermissionToken token) {
//        token.continuePermissionRequest();
//        permissions.stream().forEach(permissionRequest -> Timber.tag("PermissionCheck")
//                .i("name %s", permissionRequest.getName()));
//    }
//}
