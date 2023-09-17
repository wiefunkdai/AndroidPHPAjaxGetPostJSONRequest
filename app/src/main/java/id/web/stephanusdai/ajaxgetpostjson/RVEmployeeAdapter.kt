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
import id.web.stephanusdai.ajaxgetpostjson.databinding.EmployeeCellViewBinding

class RVEmployeeAdapter(private val cell: ArrayList<EmployeeModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ItemViewHolder(var viewBinding: EmployeeCellViewBinding) : RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = EmployeeCellViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemViewHolder = holder as ItemViewHolder
        itemViewHolder.viewBinding.idTextview.text = cell[position].employeeId
        itemViewHolder.viewBinding.nameTextview.text = cell[position].employeeName
        itemViewHolder.viewBinding.salaryTextview.text = cell[position].employeeSalary
        itemViewHolder.viewBinding.ageTextview.text = cell[position].employeeAge
    }

    override fun getItemCount(): Int {
        return cell.size
    }
}