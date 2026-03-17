package com.example.jamboree.ui.calendar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.jamboree.R;
import com.example.jamboree.data.local.SessionManager;
import com.example.jamboree.data.repository.CalendarRepository;
import com.example.jamboree.model.CalendarEvent;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.core.OutDateStyle;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarFragment extends Fragment {
    private LocalDate selectedDate = null;
    private LinearLayout contentLayout;
    private LinearLayout loginRequiredLayout;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private TextView monthTitleTextView;
    private CalendarView monthCalendarView;

    private SessionManager sessionManager;
    private CalendarRepository calendarRepository;

    private final Map<LocalDate, List<CalendarEvent>> eventsByDate = new HashMap<>();
    private YearMonth currentMonth;

    public CalendarFragment() {
        super(R.layout.fragment_calendar);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        contentLayout = view.findViewById(R.id.contentLayout);
        loginRequiredLayout = view.findViewById(R.id.loginRequiredLayout);
        Button loginButton = view.findViewById(R.id.loginButton);
        progressBar = view.findViewById(R.id.progressBar);
        errorTextView = view.findViewById(R.id.errorTextView);
        monthTitleTextView = view.findViewById(R.id.monthTitleTextView);
        monthCalendarView = view.findViewById(R.id.monthCalendarView);

        try {
            sessionManager = new SessionManager(requireContext());
            calendarRepository = new CalendarRepository(requireContext());
        } catch (Exception e) {
            showErrorState("Failed to initialize session: " + e.getMessage());
            return;
        }

        loginButton.setOnClickListener(v -> openLogin());

        if (!sessionManager.isLoggedIn()) {
            showLoginRequiredState();
            return;
        }

        showContentState();
        setupCalendar();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (sessionManager == null) {
            return;
        }

        if (sessionManager.isLoggedIn()) {
            showContentState();
            if (currentMonth != null && eventsByDate.isEmpty()) {
                loadCalendarForMonth(currentMonth);
            }
        } else {
            showLoginRequiredState();
        }
    }

    private void setupCalendar() {
        currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(12);
        YearMonth endMonth = currentMonth.plusMonths(12);
        DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY;

        monthCalendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view, date -> {
                    if (date.getPosition() != DayPosition.MonthDate) {
                        return;
                    }

                    LocalDate oldDate = selectedDate;
                    selectedDate = date.getDate();

                    if (oldDate != null) {
                        monthCalendarView.notifyDateChanged(oldDate);
                    }
                    monthCalendarView.notifyDateChanged(selectedDate);
                });
            }

            @Override
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay data) {
                container.day = data;
                bindDay(container, data);
            }
        });

        monthCalendarView.setup(startMonth, endMonth, firstDayOfWeek);
        monthCalendarView.setOutDateStyle(OutDateStyle.EndOfGrid);
        monthCalendarView.scrollToMonth(currentMonth);

        updateMonthTitle(currentMonth);
        loadCalendarForMonth(currentMonth);

        monthCalendarView.setMonthScrollListener(calendarMonth -> {
            currentMonth = calendarMonth.getYearMonth();
            updateMonthTitle(currentMonth);
            loadCalendarForMonth(currentMonth);
            return null;
        });
    }

    private void bindDay(DayViewContainer container, CalendarDay data) {
        LocalDate date = data.getDate();
        container.dayText.setText(String.valueOf(date.getDayOfMonth()));
        container.eventLine1.setVisibility(View.GONE);
        container.moreText.setVisibility(View.GONE);
        container.dayRoot.setClickable(false);

        if (data.getPosition() != DayPosition.MonthDate) {
            container.dayText.setAlpha(0.35f);
            container.dayRoot.setAlpha(0.45f);
            container.dayRoot.setBackgroundResource(R.drawable.bg_calendar_day);
            return;
        } else {
            container.dayText.setAlpha(1f);
            container.dayRoot.setAlpha(1f);
        }

        if (date.equals(selectedDate)) {
            container.dayRoot.setBackgroundResource(R.drawable.bg_calendar_day_selected);
        } else if (date.equals(LocalDate.now())) {
            container.dayRoot.setBackgroundResource(R.drawable.bg_calendar_day_today);
        } else {
            container.dayRoot.setBackgroundResource(R.drawable.bg_calendar_day);
        }

        List<CalendarEvent> events = eventsByDate.get(date);
        if (events == null || events.isEmpty()) {
            return;
        }

        container.dayRoot.setClickable(true);
        container.eventLine1.setText(events.get(0).getTitle());
        container.eventLine1.setVisibility(View.VISIBLE);

        if (events.size() > 1) {
            container.moreText.setText("+" + (events.size() - 1) + " more");
            container.moreText.setVisibility(View.VISIBLE);
        }
    }

    private void loadCalendarForMonth(YearMonth month) {
        showLoadingState();

        LocalDate firstDay = month.atDay(1);
        String startDate = firstDay.getMonthValue() + "/" + firstDay.getDayOfMonth() + "/" + firstDay.getYear();

        new Thread(() -> {
            try {
                List<CalendarEvent> events = calendarRepository.getCalendarEvents(startDate);
                Map<LocalDate, List<CalendarEvent>> grouped = groupEventsByDate(events);

                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    eventsByDate.clear();
                    eventsByDate.putAll(grouped);
                    showCalendarState();
                    monthCalendarView.notifyMonthChanged(month);
                });
            } catch (Exception e) {
                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    if (e.getMessage() != null && e.getMessage().contains("Session expired")) {
                        showLoginRequiredState();
                    } else {
                        showErrorState("Failed to load calendar: " + e.getMessage());
                    }
                });
            }
        }).start();
    }

    private Map<LocalDate, List<CalendarEvent>> groupEventsByDate(List<CalendarEvent> events) {
        Map<LocalDate, List<CalendarEvent>> grouped = new HashMap<>();

        for (CalendarEvent event : events) {
            try {
                LocalDate date = OffsetDateTime.parse(event.getStart()).toLocalDate();
                grouped.computeIfAbsent(date, key -> new ArrayList<>()).add(event);
            } catch (Exception ignored) {}
        }

        return grouped;
    }

    private void updateMonthTitle(YearMonth month) {
        monthTitleTextView.setText(formatMonth(month));
    }

    private String formatMonth(YearMonth month) {
        String monthName = new DateFormatSymbols(Locale.getDefault()).getMonths()[month.getMonthValue() - 1];
        return monthName + " " + month.getYear();
    }

    private void openLogin() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.loginFragment);
    }

    private void showLoginRequiredState() {
        loginRequiredLayout.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
    }

    private void showContentState() {
        loginRequiredLayout.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
    }

    private void showLoadingState() {
        loginRequiredLayout.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
        monthCalendarView.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        loginRequiredLayout.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        monthCalendarView.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(message);
    }

    private void showCalendarState() {
        loginRequiredLayout.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        errorTextView.setVisibility(View.GONE);
        monthCalendarView.setVisibility(View.VISIBLE);
    }

    public static class DayViewContainer extends ViewContainer {
        public CalendarDay day;
        public final View dayRoot;
        public final TextView dayText;
        public final TextView eventLine1;
        public final TextView moreText;

        public DayViewContainer(@NonNull View view, @NonNull java.util.function.Consumer<CalendarDay> onClick) {
            super(view);
            dayRoot = view.findViewById(R.id.dayRoot);
            dayText = view.findViewById(R.id.dayText);
            eventLine1 = view.findViewById(R.id.eventLine1);
            moreText = view.findViewById(R.id.moreText);

            view.setOnClickListener(v -> {
                if (day != null) {
                    onClick.accept(day);
                }
            });
        }
    }
}
