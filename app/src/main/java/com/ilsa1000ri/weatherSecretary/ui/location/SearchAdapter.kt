package com.ilsa1000ri.weatherSecretary.ui.location

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ilsa1000ri.weatherSecretary.R

    class SearchAdapter(private val areaList: List<Area>, private val onClick: (String) -> Unit) :
        RecyclerView.Adapter<SearchAdapter.ViewHolder>(), Filterable {

        var filteredAreaList: List<Area> = areaList

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout. area_items, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val area = filteredAreaList[position]
            holder.areaButton.text = "${area.firstLevel} ${area.secondLevel} ${area.thirdLevel}"

            // 버튼 클릭 리스너 설정
            holder.areaButton.setOnClickListener {
                onClick(holder.areaButton.text.toString())
            }
        }

        override fun getItemCount(): Int {
            return filteredAreaList.size
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val query = constraint?.toString()?.lowercase()?.trim()
                    val filteredList = if (query.isNullOrEmpty()) {
                        areaList
                    } else {
                        areaList.filter {
                            it.firstLevel.lowercase().contains(query) ||
                                    it.secondLevel.lowercase().contains(query) ||
                                    it.thirdLevel.lowercase().contains(query)
                        }
                    }

                    return FilterResults().apply { values = filteredList }
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    filteredAreaList = if (results?.values == null) {
                        emptyList()
                    } else {
                        results.values as List<Area>
                    }
                    notifyDataSetChanged()
                }
            }
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val areaButton: Button = view.findViewById(R.id.area_name)
        }
    }