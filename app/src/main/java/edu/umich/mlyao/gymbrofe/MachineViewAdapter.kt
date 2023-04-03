package edu.umich.mlyao.gymbrofe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.umich.mlyao.gymbrofe.databinding.MachineCardBinding

class MachineViewAdapter(context: Context, machine: Machine) :

    fun getMachineView(context: Context, position: Int, convertView: View?, parent: ViewGroup): View {
        val machineView = (convertView?.tag /* reuse binding */ ?: run {
            val rowView = LayoutInflater.from(context).inflate(R.layout.machine_card, parent, false)
            rowView.tag = MachineCardBinding.bind(rowView) // cache binding
            rowView.tag
        }) as MachineCardBinding

        machineView.machineName.text = Machine.name

        return machineView.root
    }