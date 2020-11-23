package ru.studymushrooms.ui.recognize

import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import ru.studymushrooms.R
import ru.studymushrooms.db.Mushroom
import ru.studymushrooms.ui.activities.MushroomActivity

class RecognitionItem(val mushroom: Mushroom, val prob: Double) : Item<GroupieViewHolder>() {
    val typeToRusType: Map<String, String> =
        mapOf("edible" to "Съедобный", "halfedible" to "Полусъедобный", "inedible" to "Несъедобный")

    override fun getLayout(): Int = R.layout.catalog_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val titleEditText = viewHolder.itemView.findViewById<TextView>(R.id.card_title)
        titleEditText.text = mushroom.name
        val primaryEditText = viewHolder.itemView.findViewById<TextView>(R.id.card_primary)
        primaryEditText.text = typeToRusType[mushroom.type]
        val secondaryEditText = viewHolder.itemView.findViewById<TextView>(R.id.card_secondary)
        val secondaryText = "Уверенность: %3f".format(prob * 100) + "%"
        secondaryEditText.text = secondaryText
        var link = mushroom.pictureLink!!
        if (link.startsWith("/image"))
            link = "https://wikigrib.ru" + mushroom.pictureLink
        val image = viewHolder.itemView.findViewById<ImageView>(R.id.card_image)
        Picasso.get().load(link).into(image)

        val card = viewHolder.itemView.findViewById<MaterialCardView>(R.id.card)
        card.setOnClickListener {
            val intent = Intent(it.context, MushroomActivity::class.java)
            intent.putExtra("model", mushroom)
            it.context.startActivity(intent)
        }
    }
}