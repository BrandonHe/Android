package demo.projection.ford.com.projectiondemo.sdl;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.applink.security.AppLinkSecurityService;
import com.smartdevicelink.exception.SdlException;
import com.smartdevicelink.proxy.SdlProxyALM;
import com.smartdevicelink.proxy.SdlProxyBuilder;
import com.smartdevicelink.proxy.callbacks.OnServiceEnded;
import com.smartdevicelink.proxy.callbacks.OnServiceNACKed;
import com.smartdevicelink.proxy.interfaces.IProxyListenerALM;
import com.smartdevicelink.proxy.rpc.AddCommandResponse;
import com.smartdevicelink.proxy.rpc.AddSubMenuResponse;
import com.smartdevicelink.proxy.rpc.AlertManeuverResponse;
import com.smartdevicelink.proxy.rpc.AlertResponse;
import com.smartdevicelink.proxy.rpc.ButtonPressResponse;
import com.smartdevicelink.proxy.rpc.ChangeRegistrationResponse;
import com.smartdevicelink.proxy.rpc.CreateInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteCommandResponse;
import com.smartdevicelink.proxy.rpc.DeleteFileResponse;
import com.smartdevicelink.proxy.rpc.DeleteInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteSubMenuResponse;
import com.smartdevicelink.proxy.rpc.DiagnosticMessageResponse;
import com.smartdevicelink.proxy.rpc.DialNumberResponse;
import com.smartdevicelink.proxy.rpc.EndAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.GenericResponse;
import com.smartdevicelink.proxy.rpc.GetDTCsResponse;
import com.smartdevicelink.proxy.rpc.GetInteriorVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.GetSystemCapabilityResponse;
import com.smartdevicelink.proxy.rpc.GetVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.GetWayPointsResponse;
import com.smartdevicelink.proxy.rpc.ImageResolution;
import com.smartdevicelink.proxy.rpc.ListFilesResponse;
import com.smartdevicelink.proxy.rpc.OnAudioPassThru;
import com.smartdevicelink.proxy.rpc.OnButtonEvent;
import com.smartdevicelink.proxy.rpc.OnButtonPress;
import com.smartdevicelink.proxy.rpc.OnCommand;
import com.smartdevicelink.proxy.rpc.OnDriverDistraction;
import com.smartdevicelink.proxy.rpc.OnHMIStatus;
import com.smartdevicelink.proxy.rpc.OnHashChange;
import com.smartdevicelink.proxy.rpc.OnInteriorVehicleData;
import com.smartdevicelink.proxy.rpc.OnKeyboardInput;
import com.smartdevicelink.proxy.rpc.OnLanguageChange;
import com.smartdevicelink.proxy.rpc.OnLockScreenStatus;
import com.smartdevicelink.proxy.rpc.OnPermissionsChange;
import com.smartdevicelink.proxy.rpc.OnStreamRPC;
import com.smartdevicelink.proxy.rpc.OnSystemRequest;
import com.smartdevicelink.proxy.rpc.OnTBTClientState;
import com.smartdevicelink.proxy.rpc.OnTouchEvent;
import com.smartdevicelink.proxy.rpc.OnVehicleData;
import com.smartdevicelink.proxy.rpc.OnWayPointChange;
import com.smartdevicelink.proxy.rpc.PerformAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.PerformInteractionResponse;
import com.smartdevicelink.proxy.rpc.PutFileResponse;
import com.smartdevicelink.proxy.rpc.ReadDIDResponse;
import com.smartdevicelink.proxy.rpc.ResetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.ScrollableMessageResponse;
import com.smartdevicelink.proxy.rpc.SendHapticDataResponse;
import com.smartdevicelink.proxy.rpc.SendLocationResponse;
import com.smartdevicelink.proxy.rpc.SetAppIconResponse;
import com.smartdevicelink.proxy.rpc.SetDisplayLayoutResponse;
import com.smartdevicelink.proxy.rpc.SetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.SetInteriorVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.SetMediaClockTimerResponse;
import com.smartdevicelink.proxy.rpc.ShowConstantTbtResponse;
import com.smartdevicelink.proxy.rpc.ShowResponse;
import com.smartdevicelink.proxy.rpc.SliderResponse;
import com.smartdevicelink.proxy.rpc.SpeakResponse;
import com.smartdevicelink.proxy.rpc.StreamRPCResponse;
import com.smartdevicelink.proxy.rpc.SubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.SubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.SubscribeWayPointsResponse;
import com.smartdevicelink.proxy.rpc.SystemRequestResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeWayPointsResponse;
import com.smartdevicelink.proxy.rpc.UpdateTurnListResponse;
import com.smartdevicelink.proxy.rpc.VideoStreamingFormat;
import com.smartdevicelink.proxy.rpc.enums.AppHMIType;
import com.smartdevicelink.proxy.rpc.enums.Language;
import com.smartdevicelink.proxy.rpc.enums.SdlDisconnectedReason;
import com.smartdevicelink.proxy.rpc.enums.VideoStreamingCodec;
import com.smartdevicelink.proxy.rpc.enums.VideoStreamingProtocol;
import com.smartdevicelink.security.SdlSecurityBase;
import com.smartdevicelink.streaming.video.VideoStreamingParameters;
import com.smartdevicelink.transport.BaseTransportConfig;
import com.smartdevicelink.transport.USBTransport;
import com.smartdevicelink.transport.USBTransportConfig;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import demo.projection.ford.com.projectiondemo.display.ProjectionDisplay;


public class MySdlService extends Service implements IProxyListenerALM
{
	private static final int CMD_ID_SWITCH = 100;
	private SdlProxyALM mProxy = null;

	private static final int ID_BTN_YES = 101;
	private static final int ID_BTN_NO = 102;




	private void startProxy(boolean forceConnect, Intent intent)
	{
		if (mProxy == null)
		{
			try
			{
				UsbAccessory acc = null;

				UsbManager usbManager = (UsbManager) getApplicationContext().getSystemService(Context.USB_SERVICE);
				UsbAccessory[] accessories = usbManager.getAccessoryList();
				if (accessories != null) {
					for (UsbAccessory accessory : accessories) {
						if (USBTransport.isAccessorySupported(accessory)) {
							acc = accessory;
							break;
						}
					}
				}
				BaseTransportConfig transportConfig = new USBTransportConfig(getBaseContext(), acc, false, false); // create USB transport

				if(transportConfig != null)
				{
					SdlProxyBuilder.Builder builder =
							new SdlProxyBuilder.Builder(this, "3531255735", "四维导航", false, getApplicationContext());

					Vector<AppHMIType> hmiTypes = new Vector<AppHMIType>();
					hmiTypes.add(AppHMIType.NAVIGATION);
					builder.setVrAppHMITypes(hmiTypes);

					List<Class<? extends SdlSecurityBase>> securityManagers = new ArrayList<>();
					securityManagers.add(AppLinkSecurityService.class);
					builder.setSdlSecurity(securityManagers);
					builder.setTransportType(transportConfig);

					builder.setLangDesired(Language.ZH_CN);
					builder.setHMILangDesired(Language.ZH_CN);

					mProxy = builder.build();
				}
			}
			catch (SdlException e)
			{
				e.printStackTrace();
				if (mProxy == null)
					stopSelf();
			}
		}

		if (forceConnect)
		{
			mProxy.forceOnConnected();
		}

	}

	private void disposeProxy()
	{
		if (mProxy != null)
		{
			try
			{
				mProxy.dispose();
				mProxy = null;
			}
			catch (SdlException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}
	}

	public class ServiceBinder extends Binder
	{
		public boolean ready() { return MySdlService.this.ready(); }
	}


	private boolean ready()
	{
		return mProxy != null && mProxy.getIsConnected();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate()
	{
		startProxy(true, null);

		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		disposeProxy();
		super.onDestroy();
	}


	@Override
	public IBinder onBind(Intent intent) {
		return new ServiceBinder();
	}



	@Override
	public void onOnHMIStatus(OnHMIStatus status)
	{
		switch (status.getSystemContext())
		{
		case SYSCTXT_MAIN:
			break;
		case SYSCTXT_VRSESSION:
			break;
		case SYSCTXT_MENU:
			break;
		default:
			return;
		}

		switch (status.getAudioStreamingState())
		{
		case AUDIBLE:
			// play audio if applicable
			break;
		case NOT_AUDIBLE:
			// pause/stop/mute audio if applicable
			break;
		default:
			return;
		}

		switch (status.getHmiLevel())
		{
		case HMI_FULL:
			if (status.getFirstRun())
			{
				final int SDL_DPI = 240;
				final int SDL_FPS = 30;
				final int SDL_BITRATE = 1024*1024*3;
				final int SDL_IFRAME_INTERVAL = 5;

				final ImageResolution ir = new ImageResolution();
				ir.setResolutionWidth(800);
				ir.setResolutionHeight(350);

				final VideoStreamingFormat vf = new VideoStreamingFormat();
				vf.setCodec(VideoStreamingCodec.H264);
				vf.setProtocol(VideoStreamingProtocol.RAW);

				VideoStreamingParameters vsp = new VideoStreamingParameters(SDL_DPI, SDL_FPS, SDL_BITRATE,
																			SDL_IFRAME_INTERVAL, ir, vf);

				mProxy.startRemoteDisplayStream(getApplicationContext(), ProjectionDisplay.class, vsp, true);
			}

			break;
		case HMI_LIMITED:
			break;
		case HMI_BACKGROUND:
			break;
		case HMI_NONE:
			break;
		default:
			return;
		}
	}



	@Override
	public void onProxyClosed(String s, Exception e, SdlDisconnectedReason sdlDisconnectedReason)
	{
	}

	@Override
	public void onServiceEnded(OnServiceEnded onServiceEnded)
	{

	}

	@Override
	public void onServiceNACKed(OnServiceNACKed onServiceNACKed)
	{

	}

	@Override
	public void onOnStreamRPC(OnStreamRPC onStreamRPC)
	{

	}

	@Override
	public void onStreamRPCResponse(StreamRPCResponse streamRPCResponse)
	{

	}

	@Override
	public void onError(String s, Exception e)
	{

	}

	@Override
	public void onGenericResponse(GenericResponse genericResponse)
	{

	}

	@Override
	public void onOnCommand(OnCommand response)
	{
		switch(response.getCmdID())
		{
		case CMD_ID_SWITCH:
			break;
		default:
			break;
		}
	}


	@Override
	public void onAddCommandResponse(AddCommandResponse response)
	{
	}

	@Override
	public void onAddSubMenuResponse(AddSubMenuResponse addSubMenuResponse)
	{

	}

	@Override
	public void onCreateInteractionChoiceSetResponse(CreateInteractionChoiceSetResponse response)
	{

	}

	@Override
	public void onAlertResponse(AlertResponse response)
	{
//		switch(response.getCorrelationID())
//		{
//		case COID_ALERT_CLOSE_WINDOW:
//			mAlertIsShowing = false;
//			break;
//		default:
//			break;
//		}
	}

	@Override
	public void onDeleteCommandResponse(DeleteCommandResponse deleteCommandResponse)
	{

	}

	@Override
	public void onDeleteInteractionChoiceSetResponse(
			DeleteInteractionChoiceSetResponse deleteInteractionChoiceSetResponse)
	{

	}

	@Override
	public void onDeleteSubMenuResponse(DeleteSubMenuResponse deleteSubMenuResponse)
	{

	}

	@Override
	public void onPerformInteractionResponse(PerformInteractionResponse response)
	{

	}

	@Override
	public void onResetGlobalPropertiesResponse(ResetGlobalPropertiesResponse resetGlobalPropertiesResponse)
	{

	}

	@Override
	public void onSetGlobalPropertiesResponse(SetGlobalPropertiesResponse response)
	{
//		boolean b = response.getSuccess();
//		String info = response.getInfo();
//		String result = response.getResultCode().toString();
//		result = "";
	}

	@Override
	public void onSetMediaClockTimerResponse(SetMediaClockTimerResponse setMediaClockTimerResponse)
	{

	}

	@Override
	public void onShowResponse(ShowResponse showResponse)
	{
	}

	@Override
	public void onSpeakResponse(SpeakResponse speakResponse)
	{

	}

	@Override
	public void onOnButtonEvent(OnButtonEvent onButtonEvent)
	{

	}

	@Override
	public void onOnButtonPress(OnButtonPress notification)
	{
		int customBtnName = 0;
		switch (notification.getButtonName())
		{
		case CUSTOM_BUTTON:
			customBtnName = notification.getCustomButtonName();
			switch (customBtnName)
			{
			case ID_BTN_YES:
				break;
			case ID_BTN_NO:
				break;
			default:
				break;
			}
			break;
		case OK:
			break;
		case SEEKLEFT:
			break;
		case SEEKRIGHT:
			break;
		default:
			break;
		}

	}

	@Override
	public void onSubscribeButtonResponse(SubscribeButtonResponse notification)
	{

	}

	@Override
	public void onUnsubscribeButtonResponse(UnsubscribeButtonResponse unsubscribeButtonResponse)
	{

	}

	@Override
	public void onOnPermissionsChange(OnPermissionsChange onPermissionsChange)
	{

	}

	@Override
	public void onSubscribeVehicleDataResponse(SubscribeVehicleDataResponse subscribeVehicleDataResponse)
	{
		boolean success = subscribeVehicleDataResponse.getSuccess().booleanValue();
		try {
			Log.d("Louis", "subvehicle data success ? " + success + ", respose" + subscribeVehicleDataResponse.serializeJSON().toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUnsubscribeVehicleDataResponse(UnsubscribeVehicleDataResponse unsubscribeVehicleDataResponse)
	{

	}

	@Override
	public void onGetVehicleDataResponse(GetVehicleDataResponse getVehicleDataResponse)
	{

	}

	@Override
	public void onOnVehicleData(OnVehicleData onVehicleData)
	{
	}

	@Override
	public void onPerformAudioPassThruResponse(PerformAudioPassThruResponse response)
	{
		response.getSuccess();

	}

	@Override
	public void onEndAudioPassThruResponse(EndAudioPassThruResponse endAudioPassThruResponse)
	{

	}

	@Override
	public void onOnAudioPassThru(OnAudioPassThru apt)
	{
		//byte[] data = apt.getAPTData();

	}

	@Override
	public void onPutFileResponse(PutFileResponse putFileResponse)
	{
		//putFileResponse.getSpaceAvailable();
	}

	@Override
	public void onDeleteFileResponse(DeleteFileResponse deleteFileResponse)
	{

	}

	@Override
	public void onListFilesResponse(ListFilesResponse listFilesResponse)
	{

	}

	@Override
	public void onSetAppIconResponse(SetAppIconResponse setAppIconResponse)
	{

	}

	@Override
	public void onScrollableMessageResponse(ScrollableMessageResponse scrollableMessageResponse)
	{

	}

	@Override
	public void onChangeRegistrationResponse(ChangeRegistrationResponse changeRegistrationResponse)
	{

	}

	@Override
	public void onSetDisplayLayoutResponse(SetDisplayLayoutResponse setDisplayLayoutResponse)
	{

	}

	@Override
	public void onOnLanguageChange(OnLanguageChange onLanguageChange)
	{

	}

	@Override
	public void onOnHashChange(OnHashChange onHashChange)
	{

	}

	@Override
	public void onSliderResponse(SliderResponse sliderResponse)
	{

	}

	@Override
	public void onOnDriverDistraction(OnDriverDistraction onDriverDistraction)
	{

	}

	@Override
	public void onOnTBTClientState(OnTBTClientState onTBTClientState)
	{

	}

	@Override
	public void onOnSystemRequest(OnSystemRequest onSystemRequest)
	{

	}

	@Override
	public void onSystemRequestResponse(SystemRequestResponse systemRequestResponse)
	{

	}

	@Override
	public void onOnKeyboardInput(OnKeyboardInput onKeyboardInput)
	{

	}

	@Override
	public void onOnTouchEvent(OnTouchEvent onTouchEvent)
	{

	}

	@Override
	public void onDiagnosticMessageResponse(DiagnosticMessageResponse diagnosticMessageResponse)
	{

	}

	@Override
	public void onReadDIDResponse(ReadDIDResponse readDIDResponse)
	{

	}

	@Override
	public void onGetDTCsResponse(GetDTCsResponse getDTCsResponse)
	{

	}

	@Override
	public void onOnLockScreenNotification(OnLockScreenStatus onLockScreenStatus)
	{

	}

	@Override
	public void onDialNumberResponse(DialNumberResponse dialNumberResponse)
	{

	}

	@Override
	public void onSendLocationResponse(SendLocationResponse sendLocationResponse)
	{

	}

	@Override
	public void onShowConstantTbtResponse(ShowConstantTbtResponse showConstantTbtResponse)
	{

	}

	@Override
	public void onAlertManeuverResponse(AlertManeuverResponse alertManeuverResponse)
	{
		
	}

	@Override
	public void onUpdateTurnListResponse(UpdateTurnListResponse updateTurnListResponse)
	{

	}

	@Override
	public void onServiceDataACK(int i)
	{

	}

	@Override
	public void onGetWayPointsResponse(GetWayPointsResponse getWayPointsResponse)
	{

	}

	@Override
	public void onSubscribeWayPointsResponse(SubscribeWayPointsResponse subscribeWayPointsResponse)
	{

	}

	@Override
	public void onUnsubscribeWayPointsResponse(UnsubscribeWayPointsResponse unsubscribeWayPointsResponse)
	{

	}

	@Override
	public void onOnWayPointChange(OnWayPointChange onWayPointChange)
	{

	}

	@Override
	public void onGetSystemCapabilityResponse(GetSystemCapabilityResponse getSystemCapabilityResponse)
	{

	}

	@Override
	public void onGetInteriorVehicleDataResponse(GetInteriorVehicleDataResponse getInteriorVehicleDataResponse)
	{

	}

	@Override
	public void onButtonPressResponse(ButtonPressResponse buttonPressResponse)
	{

	}

	@Override
	public void onSetInteriorVehicleDataResponse(SetInteriorVehicleDataResponse setInteriorVehicleDataResponse)
	{

	}

	@Override
	public void onOnInteriorVehicleData(OnInteriorVehicleData onInteriorVehicleData)
	{

	}

	@Override
	public void onSendHapticDataResponse(SendHapticDataResponse sendHapticDataResponse)
	{

	}

}
