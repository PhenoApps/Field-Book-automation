package com.fieldbook.tracker.traits;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.fieldbook.tracker.R;
import com.fieldbook.tracker.activities.CollectActivity;
import com.fieldbook.tracker.dialogs.DatePickerFragment;
import com.fieldbook.tracker.preferences.GeneralKeys;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTraitLayout extends BaseTraitLayout {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Button addDayBtn;
    Button minusDayBtn;
    ImageButton saveDayBtn;
    private TextView month;
    private TextView day;
    private String date;

    public DateTraitLayout(Context context) {
        super(context);
    }

    public DateTraitLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DateTraitLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setNaTraitsText() {
        month.setText("");
        day.setText("NA");
        //issue 413 apply the date saved preference color to NA values
        forceDataSavedColor();
    }

    @Override
    public String type() {
        return "date";
    }

    //this will display text color as the preference color
    private void forceDataSavedColor() {
        month.setTextColor(Color.parseColor(getDisplayColor()));
        day.setTextColor(Color.parseColor(getDisplayColor()));
    }

    @Override
    public void init() {
        date = getPrefs().getString(GeneralKeys.CALENDAR_LAST_SAVED_DATE, "2000-01-01");
        month = findViewById(R.id.mth);
        day = findViewById(R.id.day);
        addDayBtn = findViewById(R.id.addDateBtn);
        minusDayBtn = findViewById(R.id.minusDateBtn);
        saveDayBtn = findViewById(R.id.enterBtn);

        ImageButton calendarVisibilityBtn = findViewById(R.id.trait_date_calendar_visibility_btn);

        String minusDayTts = getContext().getString(R.string.trait_date_minus_day_tts);
        String openCalendarTts = getContext().getString(R.string.trait_date_open_calendar_tts);
        String nextDayTts = getContext().getString(R.string.trait_date_next_day_tts);

        /*
         * When the calendar view visibility button is pressed it starts the date picker dialog.
         */
        calendarVisibilityBtn.setOnClickListener((View) -> {
            DialogFragment newFragment = new DatePickerFragment(dateFormat, (y, m, d) -> {

                Calendar calendar = Calendar.getInstance();

                calendar.set(y, m, d);

                updateViewDate(calendar);

                //this saves the date, so update text to display color
                forceDataSavedColor();

                //save date to db
                updateTrait(getCurrentTrait().getTrait(), "date", dateFormat.format(calendar.getTime()));

                triggerTts(getTtsFromCalendar(calendar));

                return true;
            });

            triggerTts(openCalendarTts);
            newFragment.show(((CollectActivity) getContext()).getSupportFragmentManager(),
                    "datePicker");
        });

        // Add day
        addDayBtn.setOnClickListener(arg0 -> {
            Calendar calendar = Calendar.getInstance();

            //Parse date
            try {
                Date d = dateFormat.parse(date);
                if (d != null) {
                    calendar.setTime(d);
                    triggerTts(nextDayTts);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Add day
            calendar.add(Calendar.DATE, 1);

            updateViewDate(calendar);
        });

        // Minus day
        minusDayBtn.setOnClickListener(arg0 -> {
            Calendar calendar = Calendar.getInstance();

            //Parse date
            try {
                Date d = dateFormat.parse(date);
                if (d != null) {
                    calendar.setTime(d);
                    triggerTts(minusDayTts);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Subtract day, rewrite date
            calendar.add(Calendar.DATE, -1);

            updateViewDate(calendar);
        });

        // Saving date data
        saveDayBtn.setOnClickListener(arg0 -> {
            Calendar calendar = Calendar.getInstance();
            //Parse date
            try {
                Date d = dateFormat.parse(date);
                if (d != null) {
                    calendar.setTime(d);
                    triggerTts(getTtsFromCalendar(calendar));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (!day.getText().equals("NA")) { //issue 413, don't update NA when save button is pressed
                if (getPrefs().getBoolean(GeneralKeys.USE_DAY_OF_YEAR, false)) {
                    updateTrait(getCurrentTrait().getTrait(), "date", String.valueOf(calendar.get(Calendar.DAY_OF_YEAR)));
                } else {
                    updateTrait(getCurrentTrait().getTrait(), "date", dateFormat.format(calendar.getTime()));
                }
            }

            // Change the text color accordingly
            forceDataSavedColor();
        });
    }

    private String getTtsFromCalendar(Calendar calendar) {

        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        return month + " " + day;
    }

    private void updateViewDate(Calendar calendar) {

        date = dateFormat.format(calendar.getTime());

        String dayOfMonth = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
        String monthText = Integer.toString(calendar.get(Calendar.MONTH) + 1);
        String yearText = Integer.toString(calendar.get(Calendar.YEAR));

        getPrefs().edit()
                .putString(GeneralKeys.CALENDAR_LAST_SAVED_DATE, yearText + "-" + monthText + "-" + dayOfMonth)
                .apply();

        //Set text
        day.setText(dayOfMonth);
        month.setText(getMonthForInt(calendar.get(Calendar.MONTH)));

        // Change text color
        if (getNewTraits().containsKey(getCurrentTrait().getTrait())) {
            month.setTextColor(Color.BLUE);
            day.setTextColor(Color.BLUE);
        } else {
            month.setTextColor(Color.BLACK);
            day.setTextColor(Color.BLACK);
        }

    }

    @Override
    public void loadLayout() {
        super.loadLayout();

        getEtCurVal().setEnabled(false);
        getEtCurVal().setVisibility(View.GONE);

        final Calendar c = Calendar.getInstance();
        date = dateFormat.format(c.getTime());
    }

    @Override
    public void afterLoadExists(CollectActivity act, @Nullable String value) {
        super.afterLoadExists(act, value);

        //first check if observation values is observed for this plot and the value is not NA
        if (value != null && !value.equals("NA")) {

            forceDataSavedColor();

            //there is a FB preference to save dates as Day of year between 1-365
            if (value.length() < 4 && value.length() > 0) {
                Calendar calendar = Calendar.getInstance();

                //convert day of year to yyyy-mm-dd string
                date = value;
                calendar.set(Calendar.DAY_OF_YEAR, Integer.parseInt(date));
                date = dateFormat.format(calendar.getTime());

                month.setText(getMonthForInt(calendar.get(Calendar.MONTH)));
                day.setText(String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)));

            } else if (value.contains(".")) {
                //convert from yyyy.mm.dd to yyyy-mm-dd
                String[] oldDate = value.split("\\.");
                date = oldDate[0] + "-" + String.format("%02d", Integer.parseInt(oldDate[1])) + "-" + String.format("%02d", Integer.parseInt(oldDate[2]));

                //set month/day text and color
                month.setText(getMonthForInt(Integer.parseInt(oldDate[1]) - 1));
                day.setText(oldDate[2]);

            } else {
                Calendar calendar = Calendar.getInstance();

                //new format
                date = value;

                //Parse date
                try {
                    calendar.setTime(dateFormat.parse(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //set month/day text and color
                month.setText(getMonthForInt(calendar.get(Calendar.MONTH)));
                day.setText(String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)));
            }

        } else if (value != null) {

            //NA is saved as the date

            month.setText("");

            day.setText("NA");

            forceDataSavedColor();
        }
    }

    @Override
    public void afterLoadNotExists(CollectActivity act) {
        super.afterLoadNotExists(act);
        final Calendar c = Calendar.getInstance();
        month.setTextColor(Color.BLACK);
        day.setTextColor(Color.BLACK);
        month.setText(getMonthForInt(c.get(Calendar.MONTH)));
        day.setText(String.format("%02d", c.get(Calendar.DAY_OF_MONTH)));
    }

    @Override
    public void deleteTraitListener() {
        removeTrait(getCurrentTrait().getTrait());

        final Calendar c = Calendar.getInstance();
        date = dateFormat.format(c.getTime());

        month.setTextColor(Color.BLACK);
        day.setTextColor(Color.BLACK);

        //This is used to persist moving between months
        month.setText(getMonthForInt(c.get(Calendar.MONTH)));
        day.setText(String.format("%02d", c.get(Calendar.DAY_OF_MONTH)));
    }

    /**
     * Get month name based on numeric value
     */
    String getMonthForInt(int m) {
        String month = "invalid";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getShortMonths();

        if (m >= 0 && m <= 11) {
            month = months[m];
        }

        return month;
    }
}