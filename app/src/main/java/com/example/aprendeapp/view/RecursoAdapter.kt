package com.example.aprendeapp.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.aprendeapp.databinding.ItemRecursoBinding
import com.example.aprendeapp.model.Recurso
import com.example.aprendeapp.R

class RecursoAdapter(
    private val onAbrirEnlace: (Recurso) -> Unit
) : ListAdapter<Recurso, RecursoAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<Recurso>() {
        override fun areItemsTheSame(a: Recurso, b: Recurso) = a.id == b.id
        override fun areContentsTheSame(a: Recurso, b: Recurso) = a == b
    }

    inner class VH(val b: ItemRecursoBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRecursoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = getItem(position)
        val ctx = holder.itemView.context
        with(holder.b) {
            tvTitulo.text = r.titulo
            tvDescripcion.text = r.descripcion
            chipTipo.text = r.tipo
            tvId.text = ctx.getString(R.string.item_id, r.id ?: "-")
            tvPromedio.text = if (r.totalCalificaciones == 0)
                ctx.getString(R.string.item_sin_calificaciones)
            else ctx.getString(R.string.item_promedio, r.promedio, r.totalCalificaciones)

            Glide.with(ivImagen)
                .load(r.imagen)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(ivImagen)

            btnAbrir.setOnClickListener { onAbrirEnlace(r) }
        }
    }
}