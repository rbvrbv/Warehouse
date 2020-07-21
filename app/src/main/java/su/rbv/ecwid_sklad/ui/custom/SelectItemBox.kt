package su.rbv.ecwid_sklad.ui.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.box_select_items.view.*
import kotlinx.android.synthetic.main.box_select_items_item.view.*
import su.rbv.ecwid_sklad.R

/** Dialog window with vertical string action */
class SelectItemBox : DialogFragment() {

    /** interface for parent fragment to handle items clicks */
    interface ISelectItemBoxListener {
        fun onItemSelect(position: Int)
    }

    class SelectItemsBoxViewHolder(v: View) :
        RecyclerView.ViewHolder(v) {
        var itemName: TextView = v.select_items_item
    }

    /** class to show 'items' dialog, and call onItemSelect method of parent 'fragment' with position */
    class SelectItemsBoxAdapter(private val fragment: DialogFragment, private val items: List<String>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int =
            items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            SelectItemsBoxViewHolder(
                LayoutInflater.from(fragment.requireContext()).inflate(
                    R.layout.box_select_items_item,
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as SelectItemsBoxViewHolder).itemName.apply {
                text = items[position]
                setOnClickListener {
                    if (fragment.parentFragment is ISelectItemBoxListener) {
                        (fragment.parentFragment as ISelectItemBoxListener).onItemSelect(position)
                    }
                    fragment.dismiss()
                }
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.box_select_items, container, false).apply {
            select_items_list.layoutManager = LinearLayoutManager(requireContext())
            select_items_list.adapter =
                SelectItemsBoxAdapter(
                    this@SelectItemBox,
                    arguments?.getStringArrayList(ARG_ITEMS) as List<String>
                )
            select_items_list.suppressLayout(arguments?.getBoolean(ARG_SCROLL_DISABLE, true) ?: true)
        }

    companion object {

        private const val ARG_ITEMS = "list"
        private const val ARG_SCROLL_DISABLE = "scroll_disable"
        private const val FRAGMENT_TAG = "select_item_box"

        fun create(fragment: Fragment, items: ArrayList<String>, isScrollDisable: Boolean = true) =
            SelectItemBox().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_ITEMS, items)
                    putBoolean(ARG_SCROLL_DISABLE, isScrollDisable)
                }
                show(fragment.childFragmentManager,
                    FRAGMENT_TAG
                )
            }
    }

}
