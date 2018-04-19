package demo.projection.ford.com.projectiondemo.sdl;

import android.content.Context;
import android.content.Intent;

import com.smartdevicelink.transport.SdlBroadcastReceiver;
import com.smartdevicelink.transport.SdlRouterService;

/**
 * Created by leon on 2017/5/22.
 */

public class MySdlBroadcastReceiver extends SdlBroadcastReceiver
{
	@Override
	public void onSdlEnabled(Context context, Intent intent) {
		//Use the provided intent but set the class to your SdlService
		intent.setClass(context, MySdlService.class);
		context.startService(intent);
	}

	@Override
	public Class<? extends SdlRouterService> defineLocalSdlRouterClass() {
		return MySdlRouterService.class;
	}

	@Override
	public void onReceive(Context context, Intent intent){
		super.onReceive(context, intent);
		//Your code
	}

}
