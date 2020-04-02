package com.example.covid_19tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import static java.util.Collections.sort;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    // deklarasi semua variabel
    private SwipeRefreshLayout refreshLayout;
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView coronaList;
    private SearchView searchBar;
    private ImageButton refreshButton;
    private FloatingActionButton floatingActionButton;

    private CoronaListAdapter adapter;
    private static String URL = Settings.getURL();

    private ArrayList<Corona> data;
    private ArrayList<Corona> dataFull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // data yang akan ditampilkan di recycler view
        data = new ArrayList<>();
        dataFull = new ArrayList<>(data);

        // setup main layout dan refresh layout
        setUpMainLayout();
        setUpRefreshLayout();

        // setup refresh button
        setUpRefreshButton();

        // setup adapter dan searchbar
        setUpAdapter();
        setUpSearchBar();

        // fetch data dari API
        fetchData();
    }

    public void setUpRefreshLayout() {      // refreshlayout container yang isinya recyclerview agar bisa di pull to refresh
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setColorSchemeColors(Color.parseColor("#FF008DC9"));      // warna lingkaran loading

        // ketika terjadi refresh lakukan ini
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {         // delay 700ms
                        refresh(findViewById(R.id.refreshButton));      // lakukan refresh
                        refreshLayout.setRefreshing(false);         // membuat lingkaran loading berhenti
                    }
                }, 700);

            }
        });
    }

    public void setUpAdapter() {
        coronaList = findViewById(R.id.coronaList);     // recyclerview

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);        // setup layoutmanagernya
        coronaList.setLayoutManager(layoutManager);

        adapter = new CoronaListAdapter(this, data);        // setup adapternya
        coronaList.setAdapter(adapter);

        coronaList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {     // ketika racyclerview di tap, maka close keyboard
                searchBar.clearFocus();
                hideKeyboard(v);
                return false;
            }
        });

        // untuk membuat button go to top jika user scroll kebawah
        coronaList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0)     // jika user scrolling kebawah maka show buttonnya
                    floatingActionButton.show();
                else if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0)       // jika posisi sudah di paling atas maka hide buttonnya
                    floatingActionButton.hide();
            }
        });
    }

    public void setUpSearchBar() {      // search bar untuk melakukan filter data
        searchBar = findViewById(R.id.searchBar);

        // melakukan searching secara otomatis jika teks berubah
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void setUpMainLayout() {        // setup layout utama
        floatingActionButton = findViewById(R.id.floatingActionButton);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        // membuat layout utama jika di klik akan hide keyboard
        findViewById(R.id.coordinatorLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                searchBar.clearFocus();
                hideKeyboard(v);
                return false;
            }
        });
    }

    private void setUpRefreshButton() {
        // membuat efek button ditekan
        refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        refreshButton.setColorFilter(Color.parseColor("#D3D3D3"));
                        refreshButton.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL: {
                        refreshButton.clearColorFilter();
                        refreshButton.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        refreshButton.setEnabled(false);
                        refreshLayout.setRefreshing(true);

                        // delay agar button refresh tidak bisa di spam
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                refresh(v);
                                refreshLayout.setRefreshing(false);
                                refreshButton.clearColorFilter();
                                refreshButton.setEnabled(true);
                            }
                        }, 700);
                    }
                    default:
                        break;
                }
                return true;
            }
        });
    }

    public void fetchData() {
        // membuat request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // membuat request data dari url API
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {       // balasan dari URL API

                try {
                    JSONArray resultByCountry = null;           // array untuk menyimpan data JSON
                    for (Iterator<String> iter = response.keys(); iter.hasNext();) {        // looping untuk setiap negara
                        int count = 2;
                        ArrayList<String> date = new ArrayList<>();     // arraylist untuk menyimpan date series untuk 1 negara
                        ArrayList<Integer> confirmed = new ArrayList<>();       // arraylist untuk menyimpan confirmed series untuk 1 negara
                        ArrayList<Integer> activeCase = new ArrayList<>();       // arraylist untuk menyimpan active case series untuk 1 negara

                        String key = iter.next();
                        resultByCountry = response.getJSONArray(key);       // data array yang berisi semua date, confirmed, deaths, dan recovered untuk 1 negara

                        JSONObject item = resultByCountry.getJSONObject(resultByCountry.length() - 1);  // data paling baru di negara tersebut

                        // mengecek apabila data paling baru tidak lengkap ("recovered" nya null) maka ambil data dihari sebelumnya
                        while (item.isNull("recovered") && resultByCountry.length() - count >= 0) {
                            item = resultByCountry.getJSONObject(resultByCountry.length() - count);
                            count++;
                        }

                        for (int i = 0; i <= resultByCountry.length() - count+1; i++) {     // menyimpan semua date dan confirmed dari 1 negara ke sebuah arraylist
                            JSONObject dataSeries = resultByCountry.getJSONObject(i);

                            date.add(dataSeries.getString("date"));
                            confirmed.add(dataSeries.getInt("confirmed"));

                            if (dataSeries.isNull("recovered")) {
                                activeCase.add(dataSeries.getInt("confirmed") - dataSeries.getInt("deaths"));
                            } else {
                                activeCase.add(dataSeries.getInt("confirmed") - dataSeries.getInt("deaths") - dataSeries.getInt("recovered"));
                            }

                        }

                        // memasukan data untuk 1 negara tersebut kedalam arraylist data
                        data.add(new Corona(key, item, date, confirmed, activeCase));

                        // lakukan terus menerus untuk setiap negara yang ada
                    }
                    // menampilkan bahwa fetch data selesai dan ada berapa negara
                    Snackbar.make(coordinatorLayout,"Fetched data from " + data.size() + " countries", Snackbar.LENGTH_SHORT).setAction("DISMISS", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    }).show();

                    sortAlphabetically();
                    sortByConfirmedAscending();
                    // sort berdasarkan pilihan user
                    if (Settings.getSortBy() == "DEFAULT") {
                        sortByConfirmedAscending();
                    } else if (Settings.getSortBy() == "LEAST_CASES") {
                        sortByConfirmedDescending();
                    } else if (Settings.getSortBy() == "HIGHEST_FATALITY") {
                        sortByFatalityRateAscending();
                    } else if (Settings.getSortBy() == "LOWEST_FATALITY") {
                        sortByFatalityDescending();
                    } else if (Settings.getSortBy() == "ALPHABETICALLY") {
                        sortAlphabetically();
                    }

                    // set data
                    dataFull = new ArrayList<>(data);
                    adapter.setData(data, dataFull);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                  Toast.makeText(MainActivity.this, "Request Error!", Toast.LENGTH_SHORT).show();
            }
        });
        // menjalankan requestnya
        queue.add(request);
    }

    public void refresh(View v) {
        try {
            searchBar.clearFocus();

            // untuk merefresh seluruh data dan fetch data lagi dari URL API
            data.clear();
            setUpAdapter();
            fetchData();

            adapter.setData(data, dataFull);

            // untuk menampilkan sorting berdasarkan apa
            String sortedBy = "";

            switch (Settings.getSortBy()) {
                case "DEFAULT":
                    sortedBy = "Sorted by most cases";
                    break;
                case "LEAST_CASES":
                    sortedBy = "Sorted by least cases";
                    break;
                case "HIGHEST_FATALITY":
                    sortedBy = "Sorted by highest fatality rate";
                    break;
                case "LOWEST_FATALITY":
                    sortedBy = "Sorted by lowest fatality rate";
                    break;
                case "ALPHABETICALLY": sortedBy = "Sorted alphabetically";
                    break;
            }

            Toast toast = Toast.makeText(this, sortedBy, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 365);
            toast.show();

        } catch (Exception e) {
            Toast.makeText(this, "There was a problem fetching the data", Toast.LENGTH_SHORT).show();
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void sortByConfirmedAscending() {        // sort dari case terbanyak
        sort(data, new Comparator<Corona>() {
            @Override
            public int compare(Corona o1, Corona o2) {
                return o2.getConfirmed() - o1.getConfirmed();
            }
        });

        sort(dataFull, new Comparator<Corona>() {
            @Override
            public int compare(Corona o1, Corona o2) {
                return o2.getConfirmed() - o1.getConfirmed();
            }
        });
    }

    public void sortByConfirmedDescending() {       // sort dari case terdikit
        sort(data, new Comparator<Corona>() {
            @Override
            public int compare(Corona o1, Corona o2) {
                return o1.getConfirmed() - o2.getConfirmed();
            }
        });

        sort(dataFull, new Comparator<Corona>() {
            @Override
            public int compare(Corona o1, Corona o2) {
                return o1.getConfirmed() - o2.getConfirmed();
            }
        });
    }

    public void sortAlphabetically() {      // sort berdasarkan abjad
        sort(data, new Comparator<Corona>() {
            @Override
            public int compare(Corona o1, Corona o2) {
                return o1.getCountry().compareTo(o2.getCountry());
            }
        });

        sort(dataFull, new Comparator<Corona>() {
            @Override
            public int compare(Corona o1, Corona o2) {
                return o1.getCountry().compareTo(o2.getCountry());
            }
        });
    }

    public void sortByFatalityRateAscending() {     // sort dari rate kematian terbanyak
        sort(data, new Comparator<Corona>() {
            @Override
            public int compare(Corona o1, Corona o2) {
                return Float.compare(o2.getFatalityRate(), o1.getFatalityRate());
            }
        });

        sort(dataFull, new Comparator<Corona>() {
            @Override
            public int compare(Corona o1, Corona o2) {
                return Float.compare(o2.getFatalityRate(), o1.getFatalityRate());
            }
        });
    }

    public void sortByFatalityDescending() {        // sort dari rate kematian terdikit
        sort(data, new Comparator<Corona>() {
            @Override
            public int compare(Corona o1, Corona o2) {
                return Float.compare(o1.getFatalityRate(), o2.getFatalityRate());
            }
        });

        sort(dataFull, new Comparator<Corona>() {
            @Override
            public int compare(Corona o1, Corona o2) {
                return Float.compare(o1.getFatalityRate(), o2.getFatalityRate());
            }
        });
    }

    public void showPopup(View v) {     // untuk menampilkan popup menu pilihan sorting
        hideKeyboard(v);
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.popup_menu);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {     // lakukan sorting berdasarkan menu apa yang dipilih
        coronaList.smoothScrollToPosition(0);

        sortAlphabetically();
        sortByConfirmedAscending();
        switch (item.getItemId()) {
            case R.id.item1:
                Settings.setSortBy("DEFAULT");
                sortByConfirmedAscending();
                adapter.setData(data, dataFull);
                Snackbar.make(findViewById(R.id.coordinatorLayout), "Sorted by most cases", Snackbar.LENGTH_LONG).setAction("DISMISS", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {       // kosong artinya dismiss
                    }
                }).show();
                return true;
            case R.id.item2:
                Settings.setSortBy("LEAST_CASES");
                sortByConfirmedDescending();
                adapter.setData(data, dataFull);
                Snackbar.make(findViewById(R.id.coordinatorLayout), "Sorted by least cases", Snackbar.LENGTH_LONG).setAction("DISMISS", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }).show();
                return true;
            case R.id.item3:
                Settings.setSortBy("HIGHEST_FATALITY");
                sortByFatalityRateAscending();
                adapter.setData(data, dataFull);
                Snackbar.make(findViewById(R.id.coordinatorLayout), "Sorted by highest fatality rate", Snackbar.LENGTH_LONG).setAction("DISMISS", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }).show();
                return true;
            case R.id.item4:
                Settings.setSortBy("LOWEST_FATALITY");
                sortByFatalityDescending();
                adapter.setData(data, dataFull);
                Snackbar.make(findViewById(R.id.coordinatorLayout), "Sorted by lowest fatality rate", Snackbar.LENGTH_LONG).setAction("DISMISS", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }).show();
                return true;
            case R.id.item5:
                Settings.setSortBy("ALPHABETICALLY");
                sortAlphabetically();
                adapter.setData(data, dataFull);
                Snackbar.make(findViewById(R.id.coordinatorLayout), "Sorted alphabetically", Snackbar.LENGTH_LONG).setAction("DISMISS", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }).show();
                return true;
            default:
                return false;
        }
    }

    public void scrollToTop(View v) {
        coronaList.smoothScrollToPosition(0);
    }
}
