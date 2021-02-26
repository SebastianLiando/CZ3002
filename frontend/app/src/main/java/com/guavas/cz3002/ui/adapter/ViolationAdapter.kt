package com.guavas.cz3002.ui.adapter

import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import com.guavas.cz3002.R
import com.guavas.cz3002.data.violation.Violation
import com.guavas.cz3002.databinding.ItemViolationBinding
import com.guavas.cz3002.ui.adapter.base.BindingListAdapter

private val callback = object : DiffUtil.ItemCallback<Violation>() {
    override fun areItemsTheSame(oldItem: Violation, newItem: Violation) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Violation, newItem: Violation) = oldItem == newItem
}

class ViolationAdapter(
    private val owner: LifecycleOwner,
    private val onClickViolation: (Violation) -> Unit,
    private val onVerifyViolation: (Violation, Boolean) -> Unit,
    private val onLoadImage: (Violation, ImageView) -> Unit,
) : BindingListAdapter<ItemViolationBinding, Violation>(callback) {
    override val itemLayoutId = R.layout.item_violation

    override fun onBinding(binding: ItemViolationBinding, item: Violation) {
        binding.run {
            violation = item

            lifecycleOwner = owner

            onLoadImage(item, binding.imageViolation)

            root.setOnClickListener { onClickViolation(item) }
            buttonApprove.setOnClickListener { onVerifyViolation(item, true) }
            buttonReject.setOnClickListener { onVerifyViolation(item, false) }
        }
    }
}