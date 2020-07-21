package su.rbv.ecwid_sklad.ui.main

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import su.rbv.ecwid_sklad.App
import su.rbv.ecwid_sklad.Const
import su.rbv.ecwid_sklad.R
import su.rbv.ecwid_sklad.ui.Screens
import su.rbv.ecwid_sklad.ui.item.ItemsListAdapter
import su.rbv.ecwid_sklad.utils.confirmDialog
import su.rbv.ecwid_sklad.utils.getQueryTextChangesAsFlow

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    private var itemsList: RecyclerView? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        itemsList = view.main_list.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter =
                ItemsListAdapter(requireContext()) {
                    // on item click into list
                    App.cicerone.router.navigateTo(Screens.ItemScreen(it))
                }

            /** swipe to delete handler */
            val itemTouchHelperCallback = object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    (adapter as ItemsListAdapter).getItemByPosition(viewHolder.adapterPosition)?.let {
                        confirmDialog(
                            requireActivity(),
                            R.string.delete_item_message,
                            R.string.button_ok,
                            R.string.button_cancel,
                            {
                                viewModel.deleteItem(it.id)
                            },
                            {
                                (adapter as ItemsListAdapter).notifyItemChanged(viewHolder.adapterPosition)
                            }).show()
                    }
                }
            }

            val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
            itemTouchHelper.attachToRecyclerView(this)

        }

        /** observe items list */
        viewModel.items.observe(viewLifecycleOwner, Observer {
            (view.main_list.adapter as ItemsListAdapter).submitList(it)
        })

        /** add new item button click */
        view.main_add_button.setOnClickListener {
            App.cicerone.router.navigateTo(Screens.ItemScreen())
        }

        /** search menu */
        view.main_toolbar.apply {
            inflateMenu(R.menu.search_menu)
            val item = menu.findItem(R.id.search_menu_item)
            val searchView = item.actionView as SearchView

            /** init search service */
            (context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager).apply {
                searchView.setSearchableInfo(getSearchableInfo(requireActivity().componentName))
            }

            /** observe voice search results */
            viewModel.voiceQueryString.observe(viewLifecycleOwner, Observer {
                searchView.setQuery(it.data, false)
            })

            /** observe search query changes */
            lifecycleScope.launch {
                searchView.getQueryTextChangesAsFlow()
                    .debounce(Const.SEARCH_DELAY)
                    .distinctUntilChanged()
                    .flowOn(Dispatchers.Default)
                    .collect {
                        viewModel.queryString.value = it
                    }
            }
        }

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        /** save items list state */
        if (itemsList != null) {
            outState.putParcelable(ITEMS_LIST_LAYOUT_MANAGER_KEY, itemsList!!.layoutManager?.onSaveInstanceState())
        }
    }

    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            /** restore items list */
            itemsList?.layoutManager?.onRestoreInstanceState(savedInstanceState.getParcelable(
                ITEMS_LIST_LAYOUT_MANAGER_KEY
            ))
        }
    }

    companion object {
        private const val ITEMS_LIST_LAYOUT_MANAGER_KEY = "items_layout_key"
    }

}
