package com.devhjs.garim.core.ads
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.devhjs.garim.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.devhjs.garim.core.ui.TextAction

@Composable
fun HomeBanner() {
    if (!BuildConfig.ADS_ENABLED) return
    val activity = LocalActivity.current ?: return
    val consent = remember(activity) { UserMessagingPlatform.getConsentInformation(activity) }
    var ready by remember(activity) { mutableStateOf(false) }
    var optionsRequired by remember(activity) { mutableStateOf(false) }
    LaunchedEffect(activity) {
        fun update() {
            optionsRequired = consent.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
            if (consent.canRequestAds() && !ready) {
                MobileAds.initialize(activity) {}
                ready = true
            }
        }
        consent.requestConsentInfoUpdate(activity, ConsentRequestParameters.Builder().build(),
            { UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { update() } },
            { update() })
        update()
    }
    Column(Modifier.fillMaxWidth().heightIn(min = 74.dp).background(MaterialTheme.colorScheme.surface)
        .padding(bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(6.dp))
        if (ready) {
            Text("광고", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AndroidView(factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = BuildConfig.BANNER_AD_UNIT_ID
                    loadAd(AdRequest.Builder().build())
                }
            }, modifier = Modifier.width(320.dp).height(50.dp), onRelease = { it.destroy() })
        }
        if (optionsRequired) TextAction("광고 개인정보 설정", onClick = {
            UserMessagingPlatform.showPrivacyOptionsForm(activity) {
                optionsRequired = consent.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
                ready = consent.canRequestAds()
            }
        })
    }
}
