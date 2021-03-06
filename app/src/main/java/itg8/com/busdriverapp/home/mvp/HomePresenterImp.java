package itg8.com.busdriverapp.home.mvp;

import android.util.Log;

import java.util.List;

import itg8.com.busdriverapp.common.BaseWeakPresenter;
import itg8.com.busdriverapp.home.busModel.BusModel;
import itg8.com.busdriverapp.home.model.RouteModel;
import itg8.com.busdriverapp.utils.UserType;

public class HomePresenterImp extends BaseWeakPresenter<HomeMvp.HomeView> implements HomeMvp.HomePresenter,HomeMvp.HomeListener {
    private static final String TAG = "HomePresenterImp";
    HomeMvp.HomeModule module;
    public HomePresenterImp(HomeMvp.HomeView homeView) {
        super(homeView);
        module=new HomeModuleImp(this);
    }

    @Override
    public void onRouteAvail(RouteModel routeModels) {
        if(hasView()) {
            getView().hideProgress();
            Log.i(TAG, "onRouteAvail: ");
            getView().onRouteAvail(routeModels);
        }
    }

    @Override
    public void onFailToGetRoute(Object e) {
        if(hasView()) {
            getView().hideProgress();
            getView().onRetroError(e);
        }
    }

    @Override
    public void onNoInternetAccess() {
        if(hasView()){
            getView().hideProgress();
            getView().onNoInternet();
        }
    }

    @Override
    public void onBusesAvailable(BusModel busModel) {
        if(hasView()) {
            getView().hideProgress();
            Log.i(TAG, "onBusesAvailable: ");
            getView().onBusesAvailable(busModel);
        }
    }

    @Override
    public void onFailToGetBuses(Object e) {
        if(hasView()) {
            getView().hideProgress();
            getView().onRetroError(e);
        }

    }

    @Override
    public void startGettingRouteInfo() {
        Log.i(TAG, "startGettingRouteInfo: ");
        if(hasView()) {
            getView().showProgress();
            module.onStartGettingRouteInfo();
        }
    }

    @Override
    public void startGettingBusInfo() {
        Log.i(TAG, "startGettingRouteInfo: ");
        if(hasView()){
            getView().showProgress();
            module.onStartGettingBusInfo();
        }
    }

    @Override
    public void sendToken(String url, String token) {
        module.sendTokenToServer(url,token);
    }

    @Override
    public void setFragmentAsPerUser() {
        if(!hasView())
            return;
        if(UserType.isAdmin())
            getView().showBusAdminDetails();
        else
            getView().showBusDriverDetails();

    }
}
