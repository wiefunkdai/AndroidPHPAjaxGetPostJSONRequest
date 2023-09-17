/*
 * AndroidAjaxGetPostJSON by Stephanus Dai
 * @fullname : Stephanus Bagus Saputra
 *             ( 戴 Dai 偉 Wie 峯 Funk )
 * @email    : wiefunk@stephanusdai.web.id
 * @contact  : http://t.me/wiefunkdai
 * @support  : http://opencollective.com/wiefunkdai
 * @weblink  : http://www.stephanusdai.web.id
 * Copyright (c) ID 2023 Stephanus Bagus Saputra. All rights reserved.
 * Terms of the following https://stephanusdai.web.id/p/license.html
 */

package id.web.stephanusdai.ajaxgetpostjson

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.web.stephanusdai.ajaxgetpostjson.databinding.ResultCellViewBinding

class RVResultAdapter(private val cell: ArrayList<ResultModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ItemViewHolder(var viewBinding: ResultCellViewBinding) : RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ResultCellViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemViewHolder = holder as ItemViewHolder
        itemViewHolder.viewBinding.nameTextview.text = cell[position].resultName
        itemViewHolder.viewBinding.emailTextview.text = cell[position].resultEmail
        itemViewHolder.viewBinding.commentTextview.text = cell[position].resultComment
    }

    override fun getItemCount(): Int {
        return cell.size
    }
}