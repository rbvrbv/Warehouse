package su.rbv.ecwid_sklad.ui.item

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.items_list_item.view.*
import su.rbv.ecwid_sklad.R
import su.rbv.ecwid_sklad.data.Item

class ItemsListAdapter(
        private val ctx: Context,
        private val clickHandler: ((itemId: Long) -> Unit)?
    ) :  PagedListAdapter<Item, ItemsListAdapter.ItemsViewHolder>(
    ItemDiffCallback
) {

    class ItemsViewHolder(v: View) :  RecyclerView.ViewHolder(v) {
        var name: TextView = v.items_list_item_name
        var price: TextView = v.items_list_item_price
        var image: ImageView = v.items_list_item_image
    }

    fun getItemByPosition(position: Int) =
        getItem(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.items_list_item,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {
        holder.apply {
            getItem(position)?.also { item ->
                name.text = item.name
                price.text = item.priceStr

                if (item.image == null) {
                    Glide.with(itemView)
                        .load(ResourcesCompat.getDrawable(ctx.resources,
                            R.drawable.pic_empty_image, ctx.theme))
                        .into(image)
                } else {
                    Glide.with(itemView).load(item.image).into(image)
                }
                itemView.setOnClickListener {
                    clickHandler?.invoke(item.id)
                }
            }
        }
    }

    companion object {

        /** DifUtil callback for PagedListAdapter */
        val ItemDiffCallback = object : DiffUtil.ItemCallback<Item>() {

            override fun areItemsTheSame(oldItem: Item, newItem: Item) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                if (oldItem.name != newItem.name || oldItem.price != newItem.price) {
                    return false
                }
                if (oldItem.image == null && newItem.image == null) {
                    return true
                }
                if (oldItem.image == null || newItem.image == null) {
                    return false
                }
                return oldItem.image!!.contentEquals(newItem.image!!)
            }

        }
    }

}
