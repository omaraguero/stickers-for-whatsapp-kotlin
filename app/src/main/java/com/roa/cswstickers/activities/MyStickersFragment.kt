package com.roa.cswstickers.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.roa.cswstickers.R
import com.roa.cswstickers.utils.StickerPacksManager
//import com.roa.cswstickers.whatsapp_api.StickerPack
import com.roa.cswstickers.whatsapp_api.StickerPackListAdapter
import com.roa.cswstickers.whatsapp_api.StickerPackListItemViewHolder

class MyStickersFragment : Fragment() {

    private val STICKER_PREVIEW_DISPLAY_LIMIT = 5
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var stickersRecyclerView: RecyclerView
    private lateinit var stickerListAdapter: StickerPackListAdapter
    private val onAddButtonClickedListener = StickerPackListAdapter.OnAddButtonClickedListener { pack ->
        (activity as? MainActivity)?.addStickerPackToWhatsApp(
            pack!!.identifier.toString(),
            pack.name.toString()
        )
    }

    override fun onResume() {
        super.onResume()
        stickerListAdapter.setStickerPackList(StickerPacksManager.getStickerPacks(requireActivity()))
        stickerListAdapter.notifyDataSetChanged()
        verifyStickersCount()
    }

    private lateinit var view: View

    private fun initRecyclerView() {
        val stickersPacks = StickerPacksManager.stickerPacksContainer!!.sticker_packs
        layoutManager = GridLayoutManager(view.context, LinearLayoutManager.VERTICAL)
        stickersRecyclerView = view.findViewById(R.id.stickers_recycler_list)
        stickersRecyclerView.layoutManager = layoutManager

        stickerListAdapter = StickerPackListAdapter(stickersPacks, onAddButtonClickedListener, this)


        stickersRecyclerView.adapter = stickerListAdapter
        stickersRecyclerView.setItemViewCacheSize(20)
        stickersRecyclerView.isDrawingCacheEnabled = true
        stickersRecyclerView.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        stickersRecyclerView.isNestedScrollingEnabled = true
        val dividerItemDecoration = DividerItemDecoration(stickersRecyclerView.context, layoutManager.orientation)
        stickersRecyclerView.addItemDecoration(dividerItemDecoration)
        stickersRecyclerView.viewTreeObserver.addOnGlobalLayoutListener(this::recalculateColumnCount)
    }

    private fun initSwipeRefresh() {
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.refresh_my_stickers_swiper)
        swipeRefreshLayout.setOnRefreshListener {
            val stickersPacks = StickerPacksManager.getStickerPacks(requireActivity())
            stickerListAdapter.setStickerPackList(stickersPacks)
            swipeRefreshLayout.isRefreshing = false
            verifyStickersCount()
        }
    }

    private fun recalculateColumnCount() {
        val previewSize = requireActivity().resources.getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size)
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val viewHolder = stickersRecyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition) as? StickerPackListItemViewHolder
        viewHolder?.let {
            val max = Math.max(it.imageRowView.measuredWidth / previewSize, 1)
            val numColumns = Math.min(STICKER_PREVIEW_DISPLAY_LIMIT, max)
            stickerListAdapter.setMaxNumberOfStickersInARow(numColumns)
        }
    }

    private fun initButtons() {

    }

    fun verifyStickersCount() {
        val linearLayout = view.findViewById<View>(R.id.no_stickerspacks_icon)
        linearLayout.visibility = if (stickerListAdapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_my_stickers, container, false)
        initRecyclerView()
        initButtons()
        initSwipeRefresh()
        verifyStickersCount()
        return view
    }
}
