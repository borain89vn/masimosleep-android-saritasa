package com.mymasimo.masimosleep.ui.pairing.pair.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mymasimo.masimosleep.databinding.PairingListItemBinding

class ModuleAdapter(
    private val onModuleClickListener: (Module) -> Unit,
    private val modules: MutableList<Module> = mutableListOf()
) : RecyclerView.Adapter<ModuleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val bindings = PairingListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ModuleViewHolder(
            bindings,
            onModuleClickListener
        )
    }

    override fun getItemCount(): Int = modules.size

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        holder.bind(modules[position])
    }

    fun setModules(modules: List<Module>) {
        this.modules.clear()
        this.modules.addAll(modules)
        notifyDataSetChanged()
    }
}
