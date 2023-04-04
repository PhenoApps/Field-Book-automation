package com.fieldbook.tracker.traits;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.fieldbook.tracker.R;

import java.util.ArrayList;

public class LayoutCollections {
    private final ArrayList<BaseTraitLayout> traitLayouts;

    public LayoutCollections(Activity _activity) {
        int[] traitIDs = {
                R.id.angleLayout, R.id.audioLayout, R.id.barcodeLayout,
                R.id.booleanLayout, R.id.categoricalLayout, R.id.counterLayout,
                R.id.dateLayout, R.id.diseaseLayout, R.id.locationLayout,
                R.id.multicatLayout, R.id.numericLayout, R.id.percentLayout,
                R.id.photoLayout, R.id.textLayout, R.id.labelprintLayout,
                R.id.gnssLayout, R.id.usb_camera_layout
        };

        traitLayouts = new ArrayList<>();
        for (int traitID : traitIDs) {
            BaseTraitLayout layout = _activity.findViewById(traitID);
            layout.init(_activity);
            traitLayouts.add(layout);
        }
    }

    /**
     * Todo update this with trait name/dbid
     * @param traitFormat the trait layout's format
     * @return the trait layout corresponding to the format
     */
    public BaseTraitLayout getTraitLayout(final String traitFormat) {
        for (BaseTraitLayout layout : traitLayouts) {
            if (layout.isTraitType(traitFormat)) {
                return layout;
            }
        }
        return getTraitLayout("text");
    }

    public PhotoTraitLayout getPhotoTrait() {
        return (PhotoTraitLayout) getTraitLayout("photo");
    }

    public void hideLayouts() {
        for (BaseTraitLayout layout : traitLayouts) {
            layout.setVisibility(View.GONE);
        }
    }

    public void deleteTraitListener(String format) {
        getTraitLayout(format).deleteTraitListener();
    }

    public void setNaTraitsText(String format) {
        getTraitLayout(format).setNaTraitsText();
    }

    public void enableViews() {
        for (LinearLayout traitLayout : traitLayouts) {
            enableViews(true, traitLayout);
        }
    }

    public void disableViews() {
        for (BaseTraitLayout traitLayout : traitLayouts) {
            String type = traitLayout.type();
            if (!type.equals("photo") && !type.equals("audio") && !type.equals("percent"))
                enableViews(false, traitLayout);
        }
    }

    public void enableViews(Boolean toggle, ViewGroup layout) {
        layout.setEnabled(false);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                enableViews(toggle, (ViewGroup) child);
            } else {
                child.setEnabled(toggle);
            }
        }
    }

    /**
     * Triggers trait specific code for refreshing lock status.
     * Some traits may need to refresh UI.
     * Called when range box or trait box moves.
     * @param trait the trait name s.a height
     */
    public void refreshLock(String trait) {
        for (BaseTraitLayout traitLayout : traitLayouts) {
            if (traitLayout.type().equals(trait)) {
                traitLayout.refreshLock();
            }
        }
    }
}