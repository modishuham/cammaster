package com.m.cammstrind.ui.scanner.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.m.cammstrind.databinding.ItemFilterBinding
import com.m.cammstrind.model.FilterItem
import com.m.cammstrind.ui.scanner.EffectsUtils
import kotlinx.android.synthetic.main.fragment_scanner.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilterAdapter : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    private var filterList: ArrayList<FilterItem> = ArrayList()
    private var mFilterItemClickListener: FilterItemClickListener? = null
    private var selectedFilterPosition = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val view = ItemFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(filterList[position])
    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    inner class FilterViewHolder(itemView: ItemFilterBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        private val mBinding = itemView

        fun bind(item: FilterItem) {
            mBinding.tvFilterName.text = item.filterName
            //mBinding.ivFilter.setImageBitmap(item.bitmap)

            if (item.isSelected) {
                mBinding.ivFilterSelected.visibility = View.VISIBLE
            } else {
                mBinding.ivFilterSelected.visibility = View.GONE
            }

            itemView.setOnClickListener {

                item.isSelected = true
                filterList[selectedFilterPosition].isSelected = false
                notifyItemChanged(selectedFilterPosition)
                selectedFilterPosition = adapterPosition
                notifyItemChanged(selectedFilterPosition)

                mFilterItemClickListener?.onFilterItemClick(item.filterName)
            }

            when (item.filterName) {
                "Original" -> {
                    mBinding.ivFilter.setImageBitmap(item.bitmap)
                }
                "Magic Color" -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val img = EffectsUtils.applyMagicColor(item.bitmap)
                        withContext(Dispatchers.Main) {
                            mBinding.ivFilter.setImageBitmap(img)
                        }
                    }
                }
                "Magic Color 2" -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val img = EffectsUtils.applyMagicColor2(item.bitmap)
                        withContext(Dispatchers.Main) {
                            mBinding.ivFilter.setImageBitmap(img)
                        }
                    }
                }
                "Perfect" -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val img = EffectsUtils.applyPerfectEffect(item.bitmap)
                        withContext(Dispatchers.Main) {
                            mBinding.ivFilter.setImageBitmap(img)
                        }
                    }
                }
                "Perfect 2" -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val img = EffectsUtils.applyPerfect2Effect(item.bitmap)
                        withContext(Dispatchers.Main) {
                            mBinding.ivFilter.setImageBitmap(img)
                        }
                    }
                }
                "Grey Scale" -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val img = EffectsUtils.applyGreyEffect(item.bitmap)
                        withContext(Dispatchers.Main) {
                            mBinding.ivFilter.setImageBitmap(img)
                        }
                    }
                }
                "B&W" -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val img = EffectsUtils.applyBWEffect(item.bitmap)
                        withContext(Dispatchers.Main) {
                            mBinding.ivFilter.setImageBitmap(img)
                        }
                    }
                }
                "B&W2" -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val img = EffectsUtils.applyBW2Effect(item.bitmap)
                        withContext(Dispatchers.Main) {
                            mBinding.ivFilter.setImageBitmap(img)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFilters(filters: ArrayList<FilterItem>) {
        filterList = filters
        notifyDataSetChanged()
    }

    fun setFilterItemClickListener(listener: FilterItemClickListener) {
        this.mFilterItemClickListener = listener
    }

    interface FilterItemClickListener {
        fun onFilterItemClick(filterName: String)
    }
}