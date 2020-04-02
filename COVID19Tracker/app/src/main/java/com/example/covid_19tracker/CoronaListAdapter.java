package com.example.covid_19tracker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;

// CLASS CUSTOM ADAPTER UNTUK RECYCLERVIEW

public class CoronaListAdapter extends RecyclerView.Adapter<CoronaListAdapter.ViewHolder> implements Filterable {

    private ArrayList<Corona> data;
    private ArrayList<Corona> dataFull;
    private LayoutInflater inflater;
    private Context context;

    private int totalCountries = 0;

    public CoronaListAdapter(Context c, ArrayList<Corona> data) {       // constructor adapter
        this.context = c;
        this.inflater = LayoutInflater.from(c);
        this.data = data;
    }

    public void setData(ArrayList<Corona> data, ArrayList<Corona> dataFull) {       // set data ke adapter
        this.data = data;
        this.dataFull = dataFull;

        // hanya dijalankan sekali agar isi dari totalCountries tidak berubah-ubah
        if (totalCountries == 0) {
            totalCountries = data.size();
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.corona_list_item, parent, false);     // membuat view berdasarkan corona_list_item

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        // bind data
        holder.country.setText(data.get(position).getCountry());

        // format string agar angka ada koma nya
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        holder.confirmed.setText(formatter.format(Double.parseDouble(data.get(position).getConfirmed().toString())));

        // pindah ke activity details berdasarkan negara apa yang di klik
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, CoronaDetails.class);
                i.putExtra("CORONA", data.get(position));       // transfer object corona di posisi yang di klik ke activity view details

                context.startActivity(i);       // menjalankan activity details
            }
        });

        // membuat 3 item paling atas warna merah, oren, dan kuning hanya jika sortingnya berdasarkan default (most cases) atau berdasarkan kematian terbanyak
        if (data.size() == totalCountries && (Settings.getSortBy() == "DEFAULT" || Settings.getSortBy() == "HIGHEST_FATALITY")) {
            if (position == 0) {
                holder.cardView.setCardElevation((float) 10);
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FF2814"));
                //holder.cardView.setCardBackgroundColor(Color.parseColor("#B22222"));
                holder.country.setTextColor(Color.parseColor("#FFFFFF"));
                holder.confirmed.setTextColor(Color.parseColor("#FFFFFF"));
                holder.confirmedText.setTextColor(Color.parseColor("#FFFFFF"));
                holder.chevron.setColorFilter(Color.parseColor("#FFFFFF"));
            } else if (position == 1) {
                holder.cardView.setCardElevation((float) 10);
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FF7300"));
                //holder.cardView.setCardBackgroundColor(Color.parseColor("#E25822"));
                holder.country.setTextColor(Color.parseColor("#FFFFFF"));
                holder.confirmed.setTextColor(Color.parseColor("#FFFFFF"));
                holder.confirmedText.setTextColor(Color.parseColor("#FFFFFF"));
                holder.chevron.setColorFilter(Color.parseColor("#FFFFFF"));
            } else if (position == 2) {
                holder.cardView.setCardElevation((float) 10);
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFB40D"));
                //holder.cardView.setCardBackgroundColor(Color.parseColor("#F1BC31"));
                holder.country.setTextColor(Color.parseColor("#FFFFFF"));
                holder.confirmed.setTextColor(Color.parseColor("#FFFFFF"));
                holder.confirmedText.setTextColor(Color.parseColor("#FFFFFF"));
                holder.chevron.setColorFilter(Color.parseColor("#FFFFFF"));
            } else {
                holder.cardView.setCardElevation((float) 1);
                holder.cardView.setCardBackgroundColor(Color.WHITE);
                holder.country.setTextColor(Color.parseColor("#000000"));
                holder.confirmed.setTextColor(R.attr.subtitleTextColor);
                holder.confirmedText.setTextColor(R.attr.subtitleTextColor);
                holder.chevron.setColorFilter(Color.parseColor("#000000"));
            }
        } else {    // jika item bukan 3 item paling atas, maka stylingnya default saja
            holder.cardView.setCardElevation((float) 2);
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.country.setTextColor(Color.parseColor("#000000"));
            holder.confirmed.setTextColor(R.attr.subtitleTextColor);
            holder.confirmedText.setTextColor(R.attr.subtitleTextColor);
            holder.chevron.setColorFilter(Color.parseColor("#000000"));
        }
    }

    @Override
    public int getItemCount() {     // banyaknya item
        return data.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    // membuat filter
    private Filter filter = new Filter(){

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {     // melakukan filtering data
            ArrayList<Corona> filteredList = new ArrayList<>();     // array untuk menampung data yang sudah difilter

            if (constraint == null || constraint.length() == 0) {       // jika search bar kosong maka tidak perlu memfilter data
                filteredList.addAll(dataFull);
            } else {        // jika search bar ada teks maka lakukan filtering berdasarkan pattern
                String filterPattern = constraint.toString().toLowerCase().trim();      // membuat semua teks huruf kecil dan memotong spasi diawal dan akhir

                for (Corona item: dataFull) {       // looping seluruh dataFull untuk mencari pola yang matching
                    if (item.getCountry().toLowerCase().contains(filterPattern)) {      // jika pola matching, maka masukan item tersebut kedalam filteredList kita
                        filteredList.add(item);
                    }
                }
            }
            // membuat variabel results untuk menampung hasil filter kita
            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            data.clear();       // clear data dari recyclerview terlebih dahulu

            data.addAll((ArrayList) results.values);        // masukan data yang telah difilter tersebut untuk ditampilkan di recyclerview
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {

        // deklarasi variabel yang terdapat di corona_list_item
        TextView country, confirmed, confirmedText;
        CardView cardView;
        ImageButton chevron;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // konekin setiap variable dengan viewnya masing-masing
            cardView = itemView.findViewById(R.id.cardView);
            confirmedText = itemView.findViewById(R.id.confirmedText);
            chevron = itemView.findViewById(R.id.chevron);

            country = itemView.findViewById(R.id.countryText);
            confirmed = itemView.findViewById(R.id.confirmedCount);
        }
    }

}
