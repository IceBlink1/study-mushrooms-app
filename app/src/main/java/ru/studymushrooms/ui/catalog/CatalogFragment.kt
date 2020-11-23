package ru.studymushrooms.ui.catalog

import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import ru.studymushrooms.MainActivity
import ru.studymushrooms.R
import java.util.*
import kotlin.collections.ArrayList


class CatalogFragment : Fragment() {

    private val catalogViewModel: CatalogViewModel by activityViewModels()
    private val items: ArrayList<CatalogItem> = ArrayList()
    private lateinit var catalogRecyclerView: RecyclerView
    private lateinit var adapter: GroupAdapter<GroupieViewHolder>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_catalog, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        (activity as MainActivity).showBottomNav()
        adapter = GroupAdapter()
        catalogViewModel.mushrooms.observe(viewLifecycleOwner, Observer {
            if (catalogViewModel.mushrooms.value != null) {
                for (i in catalogViewModel.mushrooms.value!!) {
                    items.add(CatalogItem(i))
                }
                adapter.addAll(items)
                catalogRecyclerView = view.findViewById(R.id.catalog_recyclerview)
                catalogRecyclerView.adapter = adapter
                catalogRecyclerView.layoutManager = GridLayoutManager(context, 2)
            }
        })

        catalogViewModel.loadData(requireContext())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val search: MenuItem = menu.findItem(R.id.search)
        val searchView: SearchView = search.actionView as SearchView
        search(searchView)
        super.onCreateOptionsMenu(menu, requireActivity().menuInflater)

    }

    private fun search(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null)
                    adapter.updateAsync(items.filter {
                        it.mushroom.name!!.toLowerCase(Locale.getDefault())
                            .contains(query.toLowerCase(Locale.getDefault()))
                    }, true, null)
                else
                    adapter.updateAsync(items, true, null)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null)
                    adapter.updateAsync(items.filter {
                        it.mushroom.name!!.toLowerCase(Locale.getDefault())
                            .contains(newText.toLowerCase(Locale.getDefault()))
                    }, true, null)
                else
                    adapter.updateAsync(items, true, null)
                return true
            }
        })
    }


}
