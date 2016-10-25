package com.elsdoerfer.android.autostarts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.elsdoerfer.android.autostarts.db.IntentFilterInfo;

import java.util.ArrayList;

public class EventDetailsFragment extends DialogFragment {
    private ListActivity mActivity;

    public static EventDetailsFragment newInstance(IntentFilterInfo event) {
        EventDetailsFragment fragment = new EventDetailsFragment();

        Bundle args = new Bundle();
        args.putParcelable("event", event);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mActivity = (ListActivity) getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final IntentFilterInfo event = getArguments().getParcelable("event");
        View view = mActivity.getLayoutInflater().inflate(R.layout.receiver_info_panel, (ViewGroup) getView(), false);

        assert event != null;
        String formattedString = String.format(getString(R.string.receiver_info), event.componentInfo.componentName, event.action, event.priority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ((TextView) view.findViewById(R.id.message)).setText(Html.fromHtml(formattedString, Html.FROM_HTML_MODE_LEGACY));
        } else {
            // noinspection deprecation
            ((TextView) view.findViewById(R.id.message)).setText(Html.fromHtml(formattedString));
        }

        final boolean componentIsEnabled = mActivity.mToggleService.getQueuedState(event.componentInfo, event.componentInfo.isCurrentlyEnabled());

        // Build list of dialog items to show. Optional classes like RootFeatures or
        ArrayList<CharSequence> dialogItems = new ArrayList<>();
        dialogItems.add(getResources().getString((componentIsEnabled) ? R.string.disable : R.string.enable));
        dialogItems.add(getResources().getString(R.string.appliation_info));

        return new AlertDialog.Builder(mActivity)
                .setItems(dialogItems.toArray(new CharSequence[dialogItems.size()]), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // If the first menu item (toggle state) has been removed, account
                        // for this by subtracting one from the index. This is terrible though.
                        // Find a different way to associate the handler code with each item (TODO).
                        if (!RootFeatures.Enabled)
                            which--;

                        boolean doEnable = !componentIsEnabled;
                        switch (which) {
                            case 0:
                                mActivity.addJob(event.componentInfo, doEnable);
                                break;

                            case 1:
                                String packageName =
                                        event.componentInfo.packageInfo.packageName;
                                Intent infoIntent = new Intent();
                                infoIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                infoIntent.setData(Uri.parse("package:" + packageName));
                                try {
                                    startActivity(infoIntent);
                                } catch (ActivityNotFoundException e) {
                                    // 2.2 and below.
                                    infoIntent = new Intent();
                                    infoIntent.setClassName("com.android.settings",
                                            "com.android.settings.InstalledAppDetails");
                                    infoIntent.putExtra("com.android.settings.ApplicationPkgName",
                                            packageName);
                                    try {
                                        startActivity(infoIntent);
                                    } catch (ActivityNotFoundException ignored) {
                                    }
                                }
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .setTitle(event.componentInfo.getLabel())
                .setView(view)
                .create();
    }

}
