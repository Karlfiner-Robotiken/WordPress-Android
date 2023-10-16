package org.wordpress.android.ui.reader.discover.viewholders

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.wordpress.android.databinding.ReaderInterestItemNewBinding
import org.wordpress.android.ui.reader.discover.ReaderCardUiState.ReaderInterestsCardUiState.ReaderInterestUiState
import org.wordpress.android.ui.utils.UiHelpers
import org.wordpress.android.util.extensions.viewBinding

class ReaderInterestNewViewHolder(
    private val uiHelpers: UiHelpers,
    parent: ViewGroup,
    private val binding: ReaderInterestItemNewBinding = parent.viewBinding(ReaderInterestItemNewBinding::inflate)
) : RecyclerView.ViewHolder(binding.root) {
    fun onBind(uiState: ReaderInterestUiState) = with(binding) {
        uiHelpers.setTextOrHide(chip, uiState.interest)
        chip.setOnClickListener { uiState.onClicked.invoke(uiState.interest) }
    }
}
