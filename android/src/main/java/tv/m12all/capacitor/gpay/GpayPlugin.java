package tv.m12all.capacitor.gpay;

import static tv.m12all.capacitor.gpay.GpayConstants.LOAD_PAYMENT_DATA_REQUEST_CODE;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

@CapacitorPlugin(name = "Gpay", requestCodes = LOAD_PAYMENT_DATA_REQUEST_CODE)
public class GpayPlugin extends Plugin {
  private PaymentsClient paymentsClient = null;
  private PluginCall loadPaymentCall = null;

  @PluginMethod
  public void createClient(final PluginCall call) {
    boolean test = call.getBoolean("test", false);
    int environment = test ? WalletConstants.ENVIRONMENT_TEST : WalletConstants.ENVIRONMENT_PRODUCTION;

    Wallet.WalletOptions walletOptions = new Wallet.WalletOptions
        .Builder()
        .setEnvironment(environment)
        .build();

    paymentsClient = Wallet.getPaymentsClient(getActivity(), walletOptions);

    call.resolve();
  }

  @PluginMethod
  public void isReadyToPay(final PluginCall call) {
    if (paymentsClient == null) {
      call.reject("PaymentsClient is not created. Call createClient method first");
      return;
    }

    JSONObject isReadyToPayJson = call.getData();
    IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString());

    Task<Boolean> task = paymentsClient.isReadyToPay(request);

    task.addOnCompleteListener(
        this.getActivity(),
        taskCompleted -> {
          if (taskCompleted.isSuccessful()) {
            final boolean isReady = taskCompleted.getResult();
            JSObject response = new JSObject();
            response.put("isReady", isReady);
            call.resolve(response);
          } else {
            call.reject(Objects.requireNonNull(taskCompleted.getException()).getLocalizedMessage());
            Log.w("isReadyToPay failed", taskCompleted.getException());
          }
        }
    );
  }

  @PluginMethod
  public void loadPaymentData(final PluginCall call) {
    if (paymentsClient == null) {
      call.reject("PaymentsClient is not created. Call createClient method first");
      return;
    }

    loadPaymentCall = call;

    JSONObject paymentDataRequestJson = call.getData();
    PaymentDataRequest request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString());
    Task<PaymentData> task = paymentsClient.loadPaymentData(request);
    AutoResolveHelper.resolveTask(task, this.getActivity(), LOAD_PAYMENT_DATA_REQUEST_CODE);
  }

  @Override
  protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode != LOAD_PAYMENT_DATA_REQUEST_CODE) return;

    switch (resultCode) {
      case Activity.RESULT_OK:
        try {
          PaymentData paymentData = PaymentData.getFromIntent(data);
          assert paymentData != null;
          final String paymentInfo = paymentData.toJson();
          JSONObject paymentInfoJSON = new JSONObject(paymentInfo);
          JSObject jsPaymentData = JSObject.fromJSONObject(paymentInfoJSON);
          loadPaymentCall.resolve(jsPaymentData);
        } catch (final JSONException e) {
          loadPaymentCall.reject(e.getLocalizedMessage(), e);
        }
        break;

      case Activity.RESULT_CANCELED:
        loadPaymentCall.reject("canceled");
        break;

      case AutoResolveHelper.RESULT_ERROR:
        final Status status = AutoResolveHelper.getStatusFromIntent(data);
        assert status != null;
        loadPaymentCall.reject(status.getStatusMessage());
        break;

      default:
        loadPaymentCall.reject("Unknown result code activity");
    }
  }
}
