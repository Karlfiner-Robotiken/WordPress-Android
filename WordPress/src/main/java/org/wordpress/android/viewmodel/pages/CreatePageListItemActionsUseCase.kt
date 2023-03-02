package org.wordpress.android.viewmodel.pages

import org.wordpress.android.fluxc.model.PostModel
import org.wordpress.android.fluxc.model.SiteHomepageSettings.ShowOnFront
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.ui.blaze.BlazeFeatureUtils
import org.wordpress.android.ui.pages.PageItem.Action
import org.wordpress.android.ui.pages.PageItem.Action.CANCEL_AUTO_UPLOAD
import org.wordpress.android.ui.pages.PageItem.Action.COPY
import org.wordpress.android.ui.pages.PageItem.Action.COPY_LINK
import org.wordpress.android.ui.pages.PageItem.Action.DELETE_PERMANENTLY
import org.wordpress.android.ui.pages.PageItem.Action.MOVE_TO_DRAFT
import org.wordpress.android.ui.pages.PageItem.Action.MOVE_TO_TRASH
import org.wordpress.android.ui.pages.PageItem.Action.PROMOTE_WTIH_BLAZE
import org.wordpress.android.ui.pages.PageItem.Action.PUBLISH_NOW
import org.wordpress.android.ui.pages.PageItem.Action.SET_AS_HOMEPAGE
import org.wordpress.android.ui.pages.PageItem.Action.SET_AS_POSTS_PAGE
import org.wordpress.android.ui.pages.PageItem.Action.SET_PARENT
import org.wordpress.android.ui.pages.PageItem.Action.VIEW_PAGE
import org.wordpress.android.viewmodel.pages.PageListViewModel.PageListType
import org.wordpress.android.viewmodel.pages.PageListViewModel.PageListType.DRAFTS
import org.wordpress.android.viewmodel.pages.PageListViewModel.PageListType.PUBLISHED
import org.wordpress.android.viewmodel.pages.PageListViewModel.PageListType.SCHEDULED
import org.wordpress.android.viewmodel.pages.PageListViewModel.PageListType.TRASHED
import org.wordpress.android.viewmodel.pages.PostModelUploadUiStateUseCase.PostUploadUiState
import org.wordpress.android.viewmodel.pages.PostModelUploadUiStateUseCase.PostUploadUiState.UploadFailed
import org.wordpress.android.viewmodel.pages.PostModelUploadUiStateUseCase.PostUploadUiState.UploadWaitingForConnection
import javax.inject.Inject

class CreatePageListItemActionsUseCase @Inject constructor(private val blazeFeatureUtils: BlazeFeatureUtils) {
    fun setupPageActions(
        listType: PageListType,
        uploadUiState: PostUploadUiState,
        siteModel: SiteModel,
        remoteId: Long,
        postModel: PostModel? = null
    ): Set<Action> {
        return when (listType) {
            SCHEDULED -> mutableSetOf(
                VIEW_PAGE,
                SET_PARENT,
                COPY_LINK,
                MOVE_TO_DRAFT,
                MOVE_TO_TRASH
            ).apply {
                if (canCancelPendingAutoUpload(uploadUiState)) {
                    add(CANCEL_AUTO_UPLOAD)
                }
            }
            PUBLISHED -> {
                mutableSetOf(
                    VIEW_PAGE,
                    COPY,
                    COPY_LINK,
                    SET_PARENT
                ).apply {
                    if (siteModel.isUsingWpComRestApi &&
                        siteModel.showOnFront == ShowOnFront.PAGE.value &&
                        remoteId > 0
                    ) {
                        if (siteModel.pageOnFront != remoteId) {
                            add(SET_AS_HOMEPAGE)
                        }
                        if (siteModel.pageForPosts != remoteId) {
                            add(SET_AS_POSTS_PAGE)
                        }
                    }

                    if (siteModel.pageOnFront != remoteId && listType == PUBLISHED) {
                        add(MOVE_TO_DRAFT)
                        add(MOVE_TO_TRASH)
                    }

                    if (canCancelPendingAutoUpload(uploadUiState)) {
                        add(CANCEL_AUTO_UPLOAD)
                    }

                    if(postModel !=null && blazeFeatureUtils.isBlazeEligibleForPage(postModel)) {
                        add(PROMOTE_WTIH_BLAZE)
                    }
                }
            }
            DRAFTS -> mutableSetOf(VIEW_PAGE, SET_PARENT, PUBLISH_NOW, MOVE_TO_TRASH, COPY, COPY_LINK).apply {
                if (canCancelPendingAutoUpload(uploadUiState)) {
                    add(CANCEL_AUTO_UPLOAD)
                }
            }
            TRASHED -> setOf(MOVE_TO_DRAFT, DELETE_PERMANENTLY)
        }
    }

    private fun canCancelPendingAutoUpload(uploadUiState: PostUploadUiState) =
        (uploadUiState is UploadWaitingForConnection ||
                (uploadUiState is UploadFailed && uploadUiState.isEligibleForAutoUpload))
}
