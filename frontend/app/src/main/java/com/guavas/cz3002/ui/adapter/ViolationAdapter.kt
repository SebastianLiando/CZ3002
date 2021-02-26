package com.guavas.cz3002.ui.adapter

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import com.guavas.cz3002.R
import com.guavas.cz3002.data.violation.Violation
import com.guavas.cz3002.data.violation.ViolationListItem
import com.guavas.cz3002.databinding.ItemViolationBinding
import com.guavas.cz3002.ui.adapter.base.BindingListAdapter

private val callback = object : DiffUtil.ItemCallback<ViolationListItem>() {
    override fun areItemsTheSame(oldItem: ViolationListItem, newItem: ViolationListItem) =
        oldItem.violation.id == newItem.violation.id

    override fun areContentsTheSame(oldItem: ViolationListItem, newItem: ViolationListItem) =
        oldItem == newItem
}

class ViolationAdapter(
    private val onClickViolation: (Violation, View) -> Unit,
    private val onVerifyViolation: (Violation, Boolean) -> Unit,
    private val onLoadImage: (Violation, ImageView) -> Unit,
) : BindingListAdapter<ItemViolationBinding, ViolationListItem>(callback) {
    override val itemLayoutId = R.layout.item_violation

    override fun onBinding(binding: ItemViolationBinding, item: ViolationListItem) {
        binding.run {
            violation = item.violation
            time = item.timeString

            onLoadImage(item.violation, binding.imageViolation)

            root.setOnClickListener { onClickViolation(item.violation, binding.cardView) }
            buttonApprove.setOnClickListener { onVerifyViolation(item.violation, true) }
            buttonReject.setOnClickListener { onVerifyViolation(item.violation, false) }
        }
    }
}