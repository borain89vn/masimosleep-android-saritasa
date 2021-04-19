package com.mymasimo.masimosleep.ui.pairing.pair.view

import androidx.recyclerview.widget.RecyclerView
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.PairingListItemBinding

class ModuleViewHolder(
    private val bindings: PairingListItemBinding,
    private val onModuleClickListener: (Module) -> Unit
) : RecyclerView.ViewHolder(bindings.root) {

    fun bind(module: Module) {
        bindings.apply {
            root.setOnClickListener {
                onModuleClickListener(module)
            }
            deviceNameView.text = root.context.getString(R.string.radius_ppg_device_pairing_identifier, module.address)
        }
    }
}
