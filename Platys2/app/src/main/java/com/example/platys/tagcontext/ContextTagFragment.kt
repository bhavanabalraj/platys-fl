package com.example.platys.tagcontext

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import androidx.work.WorkInfo
import com.example.platys.R
import com.example.platys.data.EventObserver
import com.example.platys.databinding.TagContextFragmentBinding
import com.example.platys.navigation.NavigationDestination
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.tag_context_fragment.*
import javax.inject.Inject
import kotlin.math.abs


class ContextTagFragment: DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<ContextTagViewModel> { viewModelFactory }

    private lateinit var viewPager: ViewPager2

    private lateinit var viewDataBinding: TagContextFragmentBinding

    private lateinit var chipGroup: ChipGroup

    private lateinit var tagLabel: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.tag_context_fragment, container, false)

        viewDataBinding = TagContextFragmentBinding.bind(root).apply {
            this.viewmodel = viewModel
        }

        val settingsImageButton = root.findViewById<ImageButton>(R.id.settingsButton)
        val appImageButton = root.findViewById<ImageButton>(R.id.appButton)
        val editImageButton = root.findViewById<ImageButton>(R.id.editButton)
        chipGroup = root.findViewById(R.id.cgInputTags)
        tagLabel = root.findViewById(R.id.tvTagsLabel)

        val cardCarouselList: List<ContextCardCarouselModel> = listOf(
            ContextCardCarouselModel(R.drawable.card_background_1, getString(R.string.activity_tag), getString(R.string.activity_card_message), R.drawable.cycling_svg, listOf()),
            ContextCardCarouselModel(R.drawable.card_background_2, getString(R.string.place_tag), getString(R.string.place_card_message), R.drawable.place_card_icon, listOf()),
            ContextCardCarouselModel(R.drawable.card_background_3,  getString(R.string.people_tag), getString(R.string.people_card_message), R.drawable.people_card_icon, listOf()),
            ContextCardCarouselModel(R.drawable.card_background_4, getString(R.string.emoji_tag), getString(R.string.emotion_card_message), R.drawable.ic_emoji_icon, listOf())
        )

        val myAdapter = ContextTagCarouselAdapter(cardCarouselList)
        viewPager = root.findViewById<ViewPager2>(R.id.viewPager)
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.adapter = myAdapter
        viewPager.offscreenPageLimit = 3

        val nextItemVisiblePx = resources.getDimension(R.dimen.viewpager_next_item_visible)
        val currentItemHorizontalMarginPx = resources.getDimension(R.dimen.viewpager_current_item_horizontal_margin)
        val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
        val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
            page.translationX = -pageTranslationX * position
            // Next line scales the item's height. You can remove it if you don't want this effect
            page.scaleY = 1 - (0.25f * abs(position))
            // If you want a fading effect uncomment the next line:
            // page.alpha = 0.25f + (1 - abs(position))
        }

        viewPager.setPageTransformer(pageTransformer)

        // The ItemDecoration gives the current (centered) item horizontal margin so that
        // it doesn't occupy the whole screen width. Without it the items overlap
        val itemDecoration = HorizontalMarginItemDecoration(
            requireActivity().applicationContext,
            R.dimen.viewpager_current_item_horizontal_margin
        )

        viewPager.addItemDecoration(itemDecoration)

        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.viewLoaded(position)
                when(position) {
                    0 -> {
                        settingsImageButton.setBackgroundResource(R.drawable.tag_fragment_circle_1)
                        appImageButton.setBackgroundResource(R.drawable.tag_fragment_circle_1)
                        editImageButton.setBackgroundResource(R.drawable.tag_fragment_circle_1)
                    }
                    1 -> {
                        settingsImageButton.setBackgroundResource(R.drawable.tag_fragment_circle_2)
                        appImageButton.setBackgroundResource(R.drawable.tag_fragment_circle_2)
                        editImageButton.setBackgroundResource(R.drawable.tag_fragment_circle_2)
                    }
                    2 -> {
                        settingsImageButton.setBackgroundResource(R.drawable.tag_fragment_circle_3)
                        appImageButton.setBackgroundResource(R.drawable.tag_fragment_circle_3)
                        editImageButton.setBackgroundResource(R.drawable.tag_fragment_circle_3)
                    }
                    3 -> {
                        settingsImageButton.setBackgroundResource(R.drawable.tag_fragment_circle_4)
                        appImageButton.setBackgroundResource(R.drawable.tag_fragment_circle_4)
                        editImageButton.setBackgroundResource(R.drawable.tag_fragment_circle_4)
                    }
                    else -> {}
                }
            }
        })
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        setUpNavigation()
        setUpObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }
    private fun setUpObservers() {
        viewModel.outputWorkInfo.observe(viewLifecycleOwner, workInfoObserver())

        viewModel.addChip.observe(viewLifecycleOwner, EventObserver { data ->
            tvTagsLabel.visibility = View.VISIBLE
            chipGroup.addView(createChip(data))
        })

        viewModel.chipGroupInvalidate.observe(viewLifecycleOwner, EventObserver {
            chipGroup.removeAllViews()
        })

        viewModel.hideInputLabel.observe(viewLifecycleOwner, EventObserver {
            tvTagsLabel.visibility = View.INVISIBLE
        })

        viewModel.setCarousel.observe(viewLifecycleOwner, EventObserver { index ->
            viewPager.setCurrentItem(index, true)
            Toast.makeText(activity,
                "Please provide values for all 4 contexts. Swipe on cards to provide other contexts.",
                Toast.LENGTH_LONG).show()
        })
    }

    private fun setUpNavigation() {
        viewModel.navigationEvent.observe(viewLifecycleOwner, EventObserver { destination ->
            when (destination) {
                NavigationDestination.CONTEXT_TO_TAG -> {
                    val action = ContextTagFragmentDirections.actionContextTagFragmentToContextTagWindow(viewPager.currentItem)
                    findNavController().navigate(action)
                }
                else -> {}
            }
        })
    }

    private fun createChip(chipModel: ChipModel) : Chip {
        val chip = Chip(requireContext())
        chip.setChipDrawable(ChipDrawable.createFromResource(requireContext(), R.xml.standlone_chip))
        chip.text = chipModel.chipText
        chip.setTextColor(resources.getColor(android.R.color.black, null))

        chip.setOnCloseIconClickListener {
            viewModel.chipCloseClicked(chip.text.toString())
            chipGroup.removeView(chip)
        }

        return chip
    }

    private fun workInfoObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->

            // Note that these next few lines grab a single WorkInfo if it exists
            // This code could be in a Transformation in the ViewModel; they are included here
            // so that the entire process of displaying a WorkInfo is in one location.

            // If there are no matching work info, do nothing
            if (listOfWorkInfo.isNullOrEmpty()) {
                return@Observer
            }

            // We only care about the one output status.
            // Every continuation has only one worker tagged TAG_OUTPUT
            val workInfo = listOfWorkInfo[0]

            if (workInfo.state.isFinished) {
                Toast.makeText(activity, "Work done", Toast.LENGTH_SHORT).show()
            } else {

                Toast.makeText(activity, "Work not done", Toast.LENGTH_SHORT).show()
            }
        }
    }
}