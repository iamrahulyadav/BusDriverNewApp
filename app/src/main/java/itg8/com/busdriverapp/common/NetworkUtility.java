package itg8.com.busdriverapp.common;


import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.util.CrashUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import itg8.com.busdriverapp.home.busModel.BusModel;
import itg8.com.busdriverapp.home.busModel.Buses;
import itg8.com.busdriverapp.home.busModel.User;
import itg8.com.busdriverapp.home.busModel.User_;
import itg8.com.busdriverapp.home.busModel.WSResponse;
import itg8.com.busdriverapp.home.model.Checkpoint;
import itg8.com.busdriverapp.home.model.CheckpointData;
import itg8.com.busdriverapp.home.model.RouteModel;
import itg8.com.busdriverapp.leave_request.model.LeaveRequestModel;
import itg8.com.busdriverapp.login.LoginModel;
import itg8.com.busdriverapp.login.LoginModelLoginInfo;
import itg8.com.busdriverapp.notification.model.NotificationModel;
import itg8.com.busdriverapp.request.model.AttendanceAdmin;
import itg8.com.busdriverapp.request.model.Role;
import itg8.com.busdriverapp.request.model.UserRequestModel;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkUtility {

    private static final String TAG = "NetworkUtility";
    private static final String STATUS = "status";
    private static RetroController controller;

    public NetworkUtility(NetworkBuilder builder) {
        controller = Retro.getInstance().getController(builder.token);
    }

    public void login(String url, String username, String password, final ResponseListener listener) {
        if (listener == null) {
            throwNullPointer();
            return;
        }
        LoginModel model = new LoginModel();
        model.setLoginInfo(new LoginModelLoginInfo(username, password));
        Observable<ResponseBody> b = controller.checkLogin(model);
        b.subscribeOn(Schedulers.io())
                .map(new Function<ResponseBody, Boolean>() {
                    @Override
                    public Boolean apply(ResponseBody responseBody) throws Exception {
                        return getResponse(responseBody.string()) != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Boolean responseBody) {
                        if (responseBody)
                            listener.onSuccess("Login Successfully");
                        else
                            listener.onFailure("Invalid Credentials");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        listener.onSomethingWrong(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private JSONObject getResponseOnly(String json) {
        try {
            JSONObject object = new JSONObject(json);
            if (object.has("WSResponse") && !object.isNull("WSResponse")) {
                return object.getJSONObject("WSResponse");
            } else if (object.has("WSError")) {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject getResponse(String json) {
        try {
            JSONObject object = new JSONObject(json);
            if (object.has("WSResponse") && !object.isNull("WSResponse")) {
                JSONObject wsResponse = object.getJSONObject("WSResponse");
                JSONObject dataTemp = wsResponse.getJSONObject("data");
                if (dataTemp.has("LoginInfo")) {
                    JSONObject data = dataTemp.getJSONObject("LoginInfo");
                    if (data.has("RoleName")) {
                        JSONObject roleName = data.getJSONObject("RoleName");
                        if (roleName.has("RoleName")) {
                            String mRoleName = roleName.getString("RoleName");
                            if (mRoleName != null) {
                                Prefs.putString(CommonMethod.TYPE_DATA, mRoleName);
                                return wsResponse;
                            }
                        }
                    }
                }
            } else if (object.has("WSError")) {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getRequestToken(final ResponseListener listener) {
        Observable<ResponseBody> responseBodyObservable = controller.getRequestToken(new JSONObject());
        responseBodyObservable.subscribeOn(Schedulers.io())
                .map(new Function<ResponseBody, String>() {
                    @Override
                    public String apply(ResponseBody responseBody) throws Exception {
                        String s = responseBody.string();
                        Log.d(TAG, "SSSS:apply: " + s);
                        JSONObject response = new JSONObject(s);
                        if (response.has("WSResponse")) {
                            JSONObject responseMain = response.getJSONObject("WSResponse");
                            if (responseMain.has("UserRequestToken")) {
                                MyApplication.getInstance().initUserType();
                                Prefs.putString(CommonMethod.TOKEN, responseMain.getString("UserRequestToken"));
                                return s;
                            }
                        } else if (response.has("WSError")) {
                            return "";
                        }
                        return "";
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        if (TextUtils.isEmpty(s)) {
                            listener.onFailure("Error getting details");
                        } else {
                            listener.onSuccess(s);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        listener.onSomethingWrong(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void downloadAddresses(String source, String destination, String[] addresses, String key, final ResponseListener listener) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.MINUTES);
        builder.addInterceptor(interceptor);
        builder.readTimeout(5, TimeUnit.MINUTES);

        OkHttpClient client = builder.build();
        Gson gson = new GsonBuilder().setLenient().create();


        Retrofit retrofit;

        retrofit = new Retrofit.Builder()

                .baseUrl("https://maps.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();


        RetroController controllerTemp = retrofit.create(RetroController.class);
        StringBuilder addresLines = new StringBuilder();
        for (String address :
                addresses) {
            addresLines.append("|").append(address);
        }
        Observable<ResponseBody> responseBodyObservable = controllerTemp.downloadMapRoute("/maps/api/directions/json", source, destination, addresLines, key, "Transit");
        responseBodyObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        String response = responseBody.string();
                        listener.onSuccess(response);
                    }
                });
    }

    public void mapNotificationToLog(NotificationModel model, ResponseListener listener) {
        Observable<ResponseBody> responseBodyObservable = controller.mapNotificationToLog(model);
        responseBodyObservable.subscribeOn(Schedulers.io())
                .map(new Function<ResponseBody, Boolean>() {
                    @Override
                    public Boolean apply(ResponseBody responseBody) throws Exception {
                        return checkResponseBody(responseBody.string());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private Boolean checkResponseBody(String respnse) {
        Log.d(TAG, "checkResponseBody: " + respnse);
        return true;
    }

    public void getRoute(String url, final ResponseListener listener) {
        Observable<ResponseBody> responseBodyObservable = controller.getRoute(new JSONObject());
        responseBodyObservable.flatMap(new Function<ResponseBody, Observable<RouteModel>>() {
            @Override
            public Observable<RouteModel> apply(ResponseBody responseBody) throws Exception {
                JSONObject s = getResponseOnly(responseBody.string());
                Log.i(TAG, "apply: " + s);
                RouteModel models = new RouteModel();
                List<CheckpointData> checkpointData = new ArrayList<>();
                models.setCheckpointData(checkpointData);
                if (s.get("CheckpointData") instanceof JSONArray) {
                    JSONArray arr = s.getJSONArray("CheckpointData");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject so = arr.getJSONObject(i);
                        CheckpointData model = new Gson().fromJson(so.toString(), CheckpointData.class);
//                        if(model.getCheckpoints().checkpoint instanceof List){
                        String jsonString = new Gson().toJson(model.getCheckpoints().checkpoint);
                        Object json = new JSONTokener(jsonString).nextValue();
                        List<Checkpoint> checkpoints = new ArrayList<>();
                        if (json instanceof JSONObject) {
                            Checkpoint checkpoint = new Gson().fromJson(json.toString(), Checkpoint.class);
                            checkpoints.add(checkpoint);
                        } else if (json instanceof JSONArray) {
                            List<Checkpoint> checkpointList = new Gson().fromJson(json.toString(), new TypeToken<List<Checkpoint>>() {
                            }.getType());
                            checkpoints.addAll(checkpointList);
                        }
                        model.getCheckpoints().setCheckpoints(checkpoints);
//                        }else{

//                        }
                        checkpointData.add(model);
                    }
                } else if (s.get("CheckpointData") instanceof JSONObject) {
                    JSONObject so = s.getJSONObject("CheckpointData");
                    CheckpointData model = new Gson().fromJson(so.toString(), CheckpointData.class);
//                    if(model.getCheckpoints().checkpoint instanceof List){
                    String jsonString = new Gson().toJson(model.getCheckpoints().checkpoint);
                    Object json = new JSONTokener(jsonString).nextValue();
                    List<Checkpoint> checkpoints = new ArrayList<>();
                    if (json instanceof JSONObject) {
                        Checkpoint checkpoint = new Gson().fromJson(json.toString(), Checkpoint.class);
                        checkpoints.add(checkpoint);
                    } else if (json instanceof JSONArray) {
                        List<Checkpoint> checkpointList = new Gson().fromJson(json.toString(), new TypeToken<List<Checkpoint>>() {
                        }.getType());
                        checkpoints.addAll(checkpointList);
                    }
                    model.getCheckpoints().setCheckpoints(checkpoints);
//                    }
                    checkpointData.add(model);
                }
                return Observable.just(models);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RouteModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(RouteModel routeModels) {
                        Log.i(TAG, "onNext: ");
                        listener.onSuccess(routeModels);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "onError: ");
                        e.printStackTrace();
                        listener.onFailure(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private String checkAvail(String busName, JSONObject so) throws JSONException {
        if (so.has(busName))
            return (String) so.get(busName);
        return null;
    }

    private Observable<RouteModel> getRouteModelFromResponse(final ResponseBody responseBody) {
        return Observable.create(new ObservableOnSubscribe<RouteModel>() {
            @Override
            public void subscribe(ObservableEmitter<RouteModel> e) throws Exception {
                String response = responseBody.string();
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has(STATUS)) {
                    if (jsonObject.getInt(STATUS) == 200) {
                        String data = jsonObject.getJSONObject("data").toString();
                        RouteModel model = new Gson().fromJson(data, RouteModel.class);
                        if (model == null) {
                            e.onError(new Exception("Fail to get data", new Throwable("Cannot convert to RouteModel")));
                        } else {
                            e.onNext(model);
                            e.onComplete();
                        }
                    } else {
                        e.onError(new Exception("Fail to fetch route"));
                    }
                }
            }
        });
    }

    private void throwNullPointer() {
        throw new NullPointerException("null provided in NetworkUtility listner");
    }

    public void sendToken(String url, String token, final ResponseListener listener) {
        if (listener != null)
            return;
        Observable<ResponseBody> bodyObservable = controller.sendToken(url, token);
        bodyObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        listener.onSuccess(responseBody);
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onFailure(e);
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void getBuses(final ResponseListener responseListener) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", 1);
        Log.d(TAG, "getBuses: " + jsonObject.toString());
        final Observable<ResponseBody> observable = controller.getBus(jsonObject);
        observable.flatMap(new Function<ResponseBody, Observable<BusModel>>() {
            @Override
            public Observable<BusModel> apply(ResponseBody responseBody) throws Exception {
                String response = responseBody.string();
                BusModel busModel = null;
                Log.d(TAG, "apply outer: " + response);
                if (response.contains("WSResponse")) {
                    busModel = new Gson().fromJson(response, BusModel.class);
                    if (busModel != null) {
                        Log.d(TAG, "apply inner : " + new Gson().toJson(busModel));
                    }
                }
                return Observable.just(busModel);
            }
        }).flatMap(new Function<BusModel, Observable<BusModel>>() {
            @Override
            public Observable<BusModel> apply(BusModel busModel) throws Exception {
                BusModel mNewBusModel = new BusModel();
                WSResponse model = new WSResponse();
                mNewBusModel.setWSResponse(model);
                List<Buses> listBuses = new ArrayList<>();
                List<User> listUser = null;
                List<itg8.com.busdriverapp.home.busModel.Checkpoint> listCheckPoints = null;


                String jsonString = new Gson().toJson(busModel.getWSResponse().getBuses());
                Object json = new JSONTokener(jsonString).nextValue();

                if (json instanceof JSONObject) {
                    Buses bus = new Gson().fromJson(json.toString(), Buses.class);
                    listBuses.add(bus);

                } else if (json instanceof JSONArray) {
                    List<Buses> bus = new Gson().fromJson(json.toString(), new TypeToken<List<Buses>>() {
                    }.getType());
                    listBuses.addAll(bus);
                    Log.d(TAG, "apply: listBuses" + listBuses.size());
                }

                for (Buses bus : listBuses
                        ) {
                    listUser = new ArrayList<>();

                    String jsonUserString = new Gson().toJson(bus.getUser());
                    Object jsonUser = new JSONTokener(jsonUserString).nextValue();


                    if (jsonUser instanceof JSONObject) {
                        User user = new Gson().fromJson(jsonUser.toString(), User.class);
                        listUser.add(user);
                    } else if (json instanceof JSONArray) {
                        List<User> users = new Gson().fromJson(jsonUser.toString(), new TypeToken<List<User>>() {
                        }.getType());
                        listUser.addAll(users);
                        Log.d(TAG, "apply: listUser" + listUser.size());
                    }

                    bus.setUserList(listUser);
                    for (User u :
                            listUser) {
                        listCheckPoints = new ArrayList<>();
                        if (u.getCheckpoints() == null || u.getCheckpoints().getCheckpoint() == null)
                            continue;
                        String jsonCheckPointsString = new Gson().toJson(u.getCheckpoints().getCheckpoint());
                        Object jsonCheck = new JSONTokener(jsonCheckPointsString).nextValue();


                        if (jsonCheck instanceof JSONObject) {
                            itg8.com.busdriverapp.home.busModel.Checkpoint checkpoints = new Gson().fromJson(jsonCheck.toString(), itg8.com.busdriverapp.home.busModel.Checkpoint.class);
                            listCheckPoints.add(checkpoints);
                        } else if (jsonCheck instanceof JSONArray) {

                            List<itg8.com.busdriverapp.home.busModel.Checkpoint> users = new Gson().fromJson(jsonCheck.toString(), new TypeToken<List<itg8.com.busdriverapp.home.busModel.Checkpoint>>() {
                            }.getType());


                            listCheckPoints.addAll(users);
                            Log.d(TAG, "apply: listCheckPoints" + listCheckPoints.size());
                        }

                        u.getCheckpoints().setCheckpointList(listCheckPoints);
                        for (itg8.com.busdriverapp.home.busModel.Checkpoint check : listCheckPoints
                                ) {
                            List<User_> userList = new ArrayList<>();


                            String jsonUsersString = new Gson().toJson(check.getUsers());
                            Object jsonCheckUsers = new JSONTokener(jsonUsersString).nextValue();
                            if (jsonCheckUsers instanceof JSONArray) {

                                List<itg8.com.busdriverapp.home.busModel.User_> users = new Gson().fromJson(jsonCheckUsers.toString(), new TypeToken<List<itg8.com.busdriverapp.home.busModel.User_>>() {
                                }.getType());



                                userList.addAll(users);
                            } else if (jsonCheckUsers instanceof JSONObject) {
                                itg8.com.busdriverapp.home.busModel.User_ checkpoints = new Gson().fromJson(jsonCheckUsers.toString(), itg8.com.busdriverapp.home.busModel.User_.class);


                                userList.add(checkpoints);


                            }

                            check.setChildUser(userList);

                        }
                    }
                }

//                if (listUser.size() > 0) {
//
//                    for (User user : listUser
//                            ) {
//
//                        listCheckPoints = new ArrayList<>();
//
//                        String jsonCheckPointsString = new Gson().toJson(user.getCheckpoints().getCheckpoint());
//                        Object jsonCheck = new JSONTokener(jsonCheckPointsString).nextValue();
//
//
//                        if (jsonCheck instanceof JSONObject) {
//                            itg8.com.busdriverapp.home.busModel.Checkpoint checkpoints = new Gson().fromJson(jsonCheck.toString(), itg8.com.busdriverapp.home.busModel.Checkpoint.class);
//                            listCheckPoints.add(checkpoints);
//                        } else if (jsonCheck instanceof JSONArray) {
//
//                            List<itg8.com.busdriverapp.home.busModel.Checkpoint> users = new Gson().fromJson(jsonCheck.toString(), new TypeToken<List<itg8.com.busdriverapp.home.busModel.Checkpoint>>() {
//                            }.getType());
//
//
//                            listCheckPoints.addAll(users);
//                            Log.d(TAG, "apply: listCheckPoints" + listCheckPoints.size());
//                        }
//
//                        user.setCheckPointList(listCheckPoints);
//
//                    }
//                    if (listCheckPoints.size() > 0)
//                        for (itg8.com.busdriverapp.home.busModel.Checkpoint child : listCheckPoints
//                                ) {
//
//                            List<User_> userList = new ArrayList<>();
//                            userList.addAll(child.getUsers());
//                            child.setChildUser(child.getUsers());
//
//                        }
//                }


                model.setBuses(listBuses);
                return Observable.just(mNewBusModel);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BusModel>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(BusModel busModel) {
                        responseListener.onSuccess(busModel);


                    }

                    @Override
                    public void onError(Throwable e) {
                        responseListener.onFailure(e);

                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }

    public void sendLeaveRequest(LeaveRequestModel model,  final ResponseListener responseListener) {

        if (responseListener == null) {
            throwNullPointer();
            return;
        }
        Observable<ResponseBody> responseBodyObservable = controller.sendRequestServer( model);
        responseBodyObservable.subscribeOn(Schedulers.io())
                .map(new Function<ResponseBody, Boolean>() {
                    @Override
                    public Boolean apply(ResponseBody responseBody) throws Exception {
                        return getResponseOnly(responseBody.string()) != null;
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if(aBoolean){
                            responseListener.onSuccess("Submit");
                        }else
                            responseListener.onFailure("Failed");

                    }

                    @Override
                    public void onError(Throwable e) {
                        responseListener.onSomethingWrong(e.getMessage());

                    }

                    @Override
                    public void onComplete() {

                    }
                });



    }

    public void getCategory(final ResponseListener responseListener) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type",9);
        Observable<ResponseBody> responseBodyObservable = controller.getCategoryFRomSever(jsonObject);
        responseBodyObservable.flatMap(new Function<ResponseBody, Observable<UserRequestModel>>() {
            @Override
            public Observable<UserRequestModel> apply(ResponseBody responseBody) throws Exception {
                String response = responseBody.string();
                Log.d(TAG, "apply: response"+response);
                UserRequestModel model = null;
                Log.d(TAG, "apply outer: " + response);
                if (response.contains("WSResponse")) {
                    model = new Gson().fromJson(response, UserRequestModel.class);
                    if (model != null) {
                        Log.d(TAG, "apply inner : " + new Gson().toJson(model));
                    }
                }
                return Observable.just(model);
            }
        }).flatMap(new Function<UserRequestModel, Observable<AttendanceAdmin>>() {
            @Override
            public Observable<AttendanceAdmin> apply(UserRequestModel userRequestModel) throws Exception {


            return Observable.just(userRequestModel.getWSResponse().getAttendanceAdmin());
            }


        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
             .subscribe(new Observer<AttendanceAdmin>() {
                 @Override
                 public void onSubscribe(Disposable d) {

                 }

                 @Override
                 public void onNext(AttendanceAdmin responseBody) {

                     responseListener.onSuccess("");
                 }

                 @Override
                 public void onError(Throwable e) {

                 }

                 @Override
                 public void onComplete() {

                 }
             });

    }


    public interface ResponseListener {
        void onSuccess(Object message);

        void onFailure(Object err);

        void onSomethingWrong(Object e);
    }

    public static final class NetworkBuilder {
        String token;

        public NetworkBuilder setHeader() {
            token = Prefs.getString(CommonMethod.TOKEN, "-1") + " " + MyApplication.getInstance().getAppToken();
            Log.d(TAG, "setHeader: " + token);
            return this;
        }

        public NetworkUtility build() {
            return new NetworkUtility(this);
        }
    }
}


