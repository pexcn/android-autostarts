package com.elsdoerfer.android.autostarts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.elsdoerfer.android.autostarts.db.IntentFilterInfo;

import java.util.ArrayList;

public class EventDetailsFragment extends DialogFragment {
    public static EventDetailsFragment newInstance(IntentFilterInfo event) {
        EventDetailsFragment f = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("event", event);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final IntentFilterInfo event = getArguments().getParcelable("event");
        final ListActivity activity = (ListActivity) getActivity();

        View v = activity.getLayoutInflater().inflate(R.layout.receiver_info_panel, (ViewGroup) getView(), false);
        assert event != null;
        String formattedString = String.format(
                getString(R.string.receiver_info),
                event.componentInfo.componentName, event.action, event.priority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ((TextView) v.findViewById(R.id.message)).setText(
                    Html.fromHtml(formattedString, Html.FROM_HTML_MODE_LEGACY));
        } else {
            // noinspection deprecation
            ((TextView) v.findViewById(R.id.message)).setText(Html.fromHtml(formattedString));
        }

        final boolean componentIsEnabled = activity.mToggleService.getQueuedState(
                event.componentInfo, event.componentInfo.isCurrentlyEnabled());

        ArrayList<CharSequence> dialogItems = new ArrayList<>();
        dialogItems.add(getResources().getString((componentIsEnabled) ? R.string.disable : R.string.enable));

        return new AlertDialog.Builder(activity)
                .setItems(dialogItems.toArray(new CharSequence[dialogItems.size()]),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                boolean doEnable = !componentIsEnabled;
                                switch (which) {
                                    case 0:
                                        activity.addJob(event.componentInfo, doEnable);
                                        break;
                                }
                                dialog.dismiss();
                            }
                        }).setTitle(event.componentInfo.getLabel()).setView(v).create();
    }

}
