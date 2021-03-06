package com.example.wjk32.Fragment;

import com.example.wjk32.AllCategoryActivity;
import com.example.wjk32.CityActivity;
import com.example.wjk32.myutils.MyUtils;
import com.example.wjk32.utils.*;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ViewUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wjk32.R;
import com.example.wjk32.utils.ShareUtils;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;
import org.xutils.ViewInjector;

import java.io.IOException;
import java.util.List;

/**
 * Created by wjk32 on 11/17/2017.
 */

public class FragmentHome extends Fragment implements LocationListener {
    @ViewInject(R.id.index_top_city)
    private TextView topcity;

    private String CityName;
    private LocationManager locationManager;

    @ViewInject(R.id.home_nav_sort)
    private GridView navSort;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homeindex, null);
        x.view().inject(this,view);
        topcity.setText(ShareUtils.getCityName(getActivity()));
        topcity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(),CityActivity.class), MyUtils.REQUEST_CODE);
            }
        });
        navSort.setAdapter(new NavAdapter());
        return view;
    }
    public class NavAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return MyUtils.navsSort.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            MyHolder myHolder = null;
            if (view==null) {
                myHolder = new MyHolder();
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.home_index_nav_item, null);
                x.view().inject(myHolder, view);
                view.setTag(myHolder);
            }else{
                myHolder = (MyHolder) view.getTag();
            }
            myHolder.textView.setText(MyUtils.navsSort[i]);
            myHolder.imageView.setImageResource(MyUtils.navsSortImages[i]);
            if (i == MyUtils.navsSort.length-1) {
                myHolder.imageView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), AllCategoryActivity.class));
                    }
                });
            }
            return view;
        }
    }
    public class MyHolder{
        @ViewInject(R.id.home_nav_item_desc)
        public TextView textView;
        @ViewInject(R.id.home_nav_item_image)
        public ImageView imageView;
    }

//    @Event(value=R.id.index_top_city,type = View.OnClickListener.class)
//    private void onclick(View v){
//        Log.i("TAG","works!");
//        switch(v.getId()){
//            case R.id.index_top_city:
//                startActivityForResult(new Intent(getActivity(),CityActivity.class), MyUtils.REQUEST_CODE);
//                break;
//            default:
//                break;
//        }
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==MyUtils.REQUEST_CITY_CODE && resultCode== Activity.RESULT_OK){
            CityName=data.getStringExtra("cityname");
            topcity.setText(CityName);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        checkGPSisopen();
    }

    private void checkGPSisopen() {
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean isOpen = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isOpen) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, 0);
        }
        startLocation();
    }

    private void startLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
    }


    private Handler handler= new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if(message.what==1){
                topcity.setText(CityName);
            }
            return false;
        }
    });


    //get locaiton and find city
    public void updateWithNewLocation(Location location){
        double lat=0.0,lng=0.0;
        if(location!=null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            Log.i("TAG","lat="+lat+"lng="+lng);
        }else{
            CityName="can't locate";
        }
        //it may return various cities
        List<Address> list=null;
        Geocoder ge=new Geocoder(getActivity());
        try {
            list=ge.getFromLocation(lat,lng,2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(list!=null&&list.size()>0){
            for(int i=0;i<list.size();i++){
                Address ad=list.get(i);
                CityName=ad.getLocality();
            }
        }
        handler.sendEmptyMessage(1);

    }

    @Override
    public void onLocationChanged(Location location) {
        updateWithNewLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //save current city in order to the next use
        ShareUtils.putCityName(getActivity(),CityName);
        stopLocation();

    }

    private void stopLocation(){
        locationManager.removeUpdates(this);
    }
}
