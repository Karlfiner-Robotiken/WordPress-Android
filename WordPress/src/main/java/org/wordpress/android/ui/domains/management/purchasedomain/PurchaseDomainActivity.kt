package org.wordpress.android.ui.domains.management.purchasedomain

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.ui.domains.management.M3Theme
import org.wordpress.android.ui.domains.management.purchasedomain.PurchaseDomainViewModel.ActionEvent.GoBack
import org.wordpress.android.ui.domains.management.purchasedomain.PurchaseDomainViewModel.ActionEvent.GoToDomainPurchasing
import org.wordpress.android.ui.domains.management.purchasedomain.PurchaseDomainViewModel.ActionEvent.GoToExistingSite
import org.wordpress.android.ui.domains.management.purchasedomain.composable.PurchaseDomainScreen
import org.wordpress.android.ui.main.SitePickerActivity
import org.wordpress.android.ui.main.SitePickerAdapter.SitePickerMode
import org.wordpress.android.ui.mysite.SelectedSiteRepository
import javax.inject.Inject

private typealias NotImplemented = Unit

@AndroidEntryPoint
class PurchaseDomainActivity : AppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: PurchaseDomainViewModel.Factory

    @Inject
    lateinit var siteStore: SiteStore

    private val viewModel: PurchaseDomainViewModel by viewModels {
        PurchaseDomainViewModel.provideFactory(viewModelFactory, domainArg)
    }

    private val domainArg: String get() = intent.getStringExtra(PICKED_DOMAIN_KEY) ?: error("Domain cannot be null.")

    private val chooseSite = registerForActivityResult(
        object : ActivityResultContract<Unit, SiteModel?>() {
            override fun createIntent(context: Context, input: Unit) =
                Intent(context, SitePickerActivity::class.java).apply {
                    putExtra(SitePickerActivity.KEY_SITE_PICKER_MODE, SitePickerMode.DEFAULT_MODE)
                }

            override fun parseResult(resultCode: Int, intent: Intent?) =
                if (resultCode == RESULT_OK) {
                    intent?.getIntExtra(
                        SitePickerActivity.KEY_SITE_LOCAL_ID,
                        SelectedSiteRepository.UNAVAILABLE,
                    )?.let { siteLocalId ->
                        siteStore.getSiteByLocalId(siteLocalId)
                    }
                } else {
                    null
                }
        },
        viewModel::onSiteChosen,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            M3Theme {
                PurchaseDomainScreen(
                    onNewDomainCardSelected = viewModel::onNewDomainSelected,
                    onExistingSiteCardSelected = viewModel::onExistingSiteSelected,
                    onBackPressed = viewModel::onBackPressed,
                )
            }
        }
        observeActions()
    }

    private fun observeActions() {
        viewModel.actionEvents.onEach(this::handleActionEvents).launchIn(lifecycleScope)
    }

    private fun handleActionEvents(actionEvent: PurchaseDomainViewModel.ActionEvent) {
        when (actionEvent) {
            is GoToDomainPurchasing -> NotImplemented
            is GoToExistingSite -> { chooseSite.launch() }
            GoBack -> onBackPressedDispatcher.onBackPressed()
        }
    }

    companion object {
        const val PICKED_DOMAIN_KEY: String = "picked_domain_key"
    }
}
