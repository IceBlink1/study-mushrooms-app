package ru.studymushrooms.ui.catalog

import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import ru.studymushrooms.R
import ru.studymushrooms.db.Mushroom
import ru.studymushrooms.ui.activities.MushroomActivity

class CatalogItem(val mushroom: Mushroom) : Item() {
    val typeToRusType: Map<String, String> =
        mapOf("edible" to "Съедобный", "halfedible" to "Полусъедобный", "inedible" to "Несъедобный")

    override fun getLayout(): Int = R.layout.catalog_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val titleEditText = viewHolder.itemView.findViewById<TextView>(R.id.card_title)
        titleEditText.text = mushroom.name
        val primaryEditText = viewHolder.itemView.findViewById<TextView>(R.id.card_primary)
        primaryEditText.text = typeToRusType[mushroom.type]
        val secondaryEditText = viewHolder.itemView.findViewById<TextView>(R.id.card_secondary)
        if (mushroom.description?.length ?: 0 > 30)
            secondaryEditText.text = mushroom.description?.substring(0..30) + "..."
        else
            secondaryEditText.text = mushroom.description
        val link = mushroom.pictureLink
        link?.let {
            val image = viewHolder.itemView.findViewById<ImageView>(R.id.card_image)
            if (it.startsWith("/image"))
                Picasso.get().load("https://wikigrib.ru" + it).into(image)
            else
                Picasso.get().load(it).into(image)
        }
        val card = viewHolder.itemView.findViewById<MaterialCardView>(R.id.card)
        card.setOnClickListener {
            val intent = Intent(it.context, MushroomActivity::class.java)
            intent.putExtra("model", mushroom)
            it.context.startActivity(intent)
        }
    }
}