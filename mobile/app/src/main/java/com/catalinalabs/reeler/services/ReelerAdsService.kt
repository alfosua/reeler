package com.catalinalabs.reeler.services

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class ReelerAdsService() {
    fun showInterstitial(context: Context) {
        if (context !is Activity) {
            throw Exception("Context is not an activity")
        }
        InterstitialAd.load(
            context,
            "ca-app-pub-8588662607604944/6439579637",
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    TODO("Handle the error.")
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    interstitialAd.show(context)
                }
            }
        )
    }
}
