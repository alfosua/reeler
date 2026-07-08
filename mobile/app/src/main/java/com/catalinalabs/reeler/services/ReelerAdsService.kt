package com.catalinalabs.reeler.services

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class ReelerAdsService {
    fun showInterstitial(context: Context) {
        if (context !is Activity) {
            Log.w(TAG, "Skipping interstitial: context is not an activity")
            return
        }
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    // Ads are best-effort: never block or crash the download
                    // flow because an ad failed to load.
                    Log.w(TAG, "Interstitial failed to load: ${adError.message}")
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    interstitialAd.show(context)
                }
            }
        )
    }

    companion object {
        private const val TAG = "ReelerAdsService"
        private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-8588662607604944/6439579637"
    }
}
