package com.opt.amazon;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.elsdoerfer.android.autostarts.R;

/**
 * Amazon-specific functionality.
 */
public class MarketUtils {

	public static final int FIND_IN_MARKET_TEXT = R.string.find_in_appstore;

	public static void findPackageInMarket(Context ctx, String packageName) {
		try {
			Intent marketIntent = new Intent(Intent.ACTION_VIEW);
			marketIntent.setData(Uri.parse(
					"http://www.amazon.com/gp/mas/dl/android?p="+packageName));
			ctx.startActivity(marketIntent);
		}
		catch (ActivityNotFoundException e) {}
	}

}