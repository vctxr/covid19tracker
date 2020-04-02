package com.example.covid_19tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Sampler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

// CLASS YANG AKAN MENAMPILKAN DETAILS DARI NEGARA YANG DIPILIH

public class CoronaDetails extends AppCompatActivity {

    private TextView country, date, confirmed, deaths, recovered, fatality;
    private RadioButton radioLeft, radioRight;
    private ProgressBar progressBar;

    private ArrayList<String> dateSeries;
    private ArrayList<Integer> confirmedSeries;
    private ArrayList<Integer> activeCaseSeries;

    private AnyChartView graphView;
    private List<DataEntry> seriesData;
    private Cartesian cartesian;
    private Set set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corona_details);

        // konekin variabel dengan element-element view
        setUpIds();

        // menerima parcel untuk menampilkan data yang sesuai dengan negara yang dipilih
        getParcel();

        // setup graph
        setUpGraph();


        // setup segmented button
        setupRadioButton();
    }

    public void setupRadioButton() {
        radioLeft = findViewById(R.id.radioLeft);
        radioRight = findViewById(R.id.radioRight);
    }

    public void onRadioButtonClicked(View v) {
        boolean isSelected = ((RadioButton) v).isChecked();

        switch (v.getId()) {
            case R.id.radioLeft:
                if (isSelected) {
                    radioLeft.setEnabled(false);
                    radioRight.setEnabled(true);

                    radioLeft.setTextColor(Color.WHITE);
                    radioRight.setTextColor(Color.parseColor("#FF8F00"));

                    progressBar.setVisibility(View.VISIBLE);
                    cartesian.title("COVID-19 Confirmed cases in " + country.getText());

                    generateDataConfirmed();
                    set.data(seriesData);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    }, 200);
                }
                break;
            case R.id.radioRight:
                if (isSelected) {
                    radioRight.setEnabled(false);
                    radioLeft.setEnabled(true);

                    radioRight.setTextColor(Color.WHITE);
                    radioLeft.setTextColor(Color.parseColor("#FF8F00"));

                    progressBar.setVisibility(View.VISIBLE);
                    cartesian.title("COVID-19 Active cases in " + country.getText());

                    generateDataActive();
                    set.data(seriesData);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    }, 200);

                }
                break;
        }
    }

    public void setUpIds() {
        country = findViewById(R.id.detailCountryText);
        date = findViewById(R.id.lastUpdated);
        confirmed = findViewById(R.id.confirmedCount);
        deaths = findViewById(R.id.deathsCount);
        recovered = findViewById(R.id.recoveriesCount);
        fatality = findViewById(R.id.fatalityRate);

        progressBar = findViewById(R.id.progressBar);
    }

    public void getParcel() {
        Corona corona = getIntent().getParcelableExtra("CORONA");

        country.setText(corona.getCountry());
        date.setText(corona.getDate());

        // format string agar angka ada koma nya
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        confirmed.setText(formatter.format(Double.parseDouble(corona.getConfirmed().toString())));
        deaths.setText(formatter.format(Double.parseDouble(corona.getDeaths().toString())));
        recovered.setText(formatter.format(Double.parseDouble(corona.getRecovered().toString())));

        fatality.setText(String.format("%.2f", corona.getFatalityRate()) + "%");

        dateSeries = new ArrayList<>(corona.getDateSeries());
        confirmedSeries = new ArrayList<>(corona.getConfirmedSeries());
        activeCaseSeries = new ArrayList<>(corona.getActiveCaseSeries());
    }

    public void setUpGraph() {
        graphView = findViewById(R.id.any_chart_view);

        cartesian = AnyChart.line();
        cartesian.animation(true);

        cartesian.padding(25d, 35d, 25d, 10d);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title("COVID-19 Confirmed cases in " + country.getText().toString());

        cartesian.xAxis(0).labels().padding(5d, 100d, 5d, 100d);

        seriesData = new ArrayList<>();
        for (int i = 0; i < dateSeries.size(); i++) {
            seriesData.add(new ValueDataEntry(dateSeries.get(i), confirmedSeries.get(i)));
        }

        set = Set.instantiate();
        set.data(seriesData);
        Mapping seriesMapping = set.mapAs("{ x: 'x', value: 'value' }");

        Line series = cartesian.line(seriesMapping);
        series.name("Cases: ");
        series.hovered().markers().enabled(true);
        series.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(5);
        series.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        graphView.setChart(cartesian);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        }, 500);
    }

    public void generateDataActive() {
        seriesData.clear();

        for (int i = 0; i < dateSeries.size(); i++) {
            seriesData.add(new ValueDataEntry(dateSeries.get(i), activeCaseSeries.get(i)));
        }
    }

    public void generateDataConfirmed() {
        seriesData.clear();

        for (int i = 0; i < dateSeries.size(); i++) {
            seriesData.add(new ValueDataEntry(dateSeries.get(i), confirmedSeries.get(i)));
        }
    }

    public void moreInfo(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.who.int/health-topics/coronavirus#tab=tab_1"));
        startActivity(browserIntent);
    }

}
