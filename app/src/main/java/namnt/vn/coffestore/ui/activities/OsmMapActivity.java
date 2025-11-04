package namnt.vn.coffestore.ui.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import namnt.vn.coffestore.R;

public class OsmMapActivity extends AppCompatActivity {

    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private Marker coffeeMarker;
    private Polyline routeLine;

    // Tọa độ quán cà phê cố định (A) — Bạn đã cung cấp
    private static final double COFFEE_LAT = 10.84142;
    private static final double COFFEE_LON = 106.81004;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean routeDrawnOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cấu hình osmdroid (bắt buộc)
        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osm_prefs", MODE_PRIVATE)
        );

        setContentView(R.layout.activity_osm_map);

        // MapView
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Nút quay lại
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // Xin quyền vị trí
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        // Marker quán A (cố định)
        showCoffeeMarker();

        // Provider vị trí: dùng cả GPS + NETWORK để bắt nhanh hơn
        GpsMyLocationProvider provider = new GpsMyLocationProvider(this);
        provider.addLocationSource(android.location.LocationManager.GPS_PROVIDER);
        provider.addLocationSource(android.location.LocationManager.NETWORK_PROVIDER);

        // Chấm xanh vị trí hiện tại (B)
        locationOverlay = new MyLocationNewOverlay(provider, mapView);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        mapView.getOverlays().add(locationOverlay);

        // Poll: thử lấy vị trí và vẽ route trong 15s (mỗi 1s)
        pollForLocationAndDraw(15);

        // Lắng nghe fix đầu tiên: vẽ route 1 lần nếu chưa vẽ
        provider.startLocationProvider(new IMyLocationConsumer() {
            @Override
            public void onLocationChanged(android.location.Location loc, IMyLocationProvider src) {
                if (loc == null) return;
                if (!routeDrawnOnce) {
                    routeDrawnOnce = true;
                    GeoPoint me = new GeoPoint(loc.getLatitude(), loc.getLongitude());
                    GeoPoint coffee = new GeoPoint(COFFEE_LAT, COFFEE_LON);
                    runOnUiThread(() -> drawRoute(me, coffee));
                }
            }
        });
    }

    /** Thử lấy myLocation mỗi 1s cho tới khi có, tối đa attempts lần */
    private void pollForLocationAndDraw(int attempts) {
        handler.postDelayed(() -> {
            GeoPoint my = locationOverlay != null ? locationOverlay.getMyLocation() : null;
            if (my != null) {
                GeoPoint coffee = new GeoPoint(COFFEE_LAT, COFFEE_LON);
                drawRoute(my, coffee);
                routeDrawnOnce = true;
            } else if (attempts > 0) {
                pollForLocationAndDraw(attempts - 1);
            } else {
                Toast.makeText(this,
                        "Không lấy được vị trí hiện tại. Hãy bật Location hoặc đặt tọa độ trong emulator (… → Location → Set Location).",
                        Toast.LENGTH_LONG).show();
            }
        }, 1000);
    }

    /** Vẽ marker quán A và focus */
    private void showCoffeeMarker() {
        GeoPoint coffeePoint = new GeoPoint(COFFEE_LAT, COFFEE_LON);
        coffeeMarker = new Marker(mapView);
        coffeeMarker.setPosition(coffeePoint);
        coffeeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        coffeeMarker.setTitle("Coffee Store");
        mapView.getOverlays().add(coffeeMarker);

        IMapController controller = mapView.getController();
        controller.setZoom(14.5);
        controller.setCenter(coffeePoint);
    }

    /** Gọi OSRM để vẽ đường đi từ start (B) → end (A) */
    private void drawRoute(GeoPoint start, GeoPoint end) {
        new Thread(() -> {
            try {
                String urlStr = "https://router.project-osrm.org/route/v1/driving/"
                        + start.getLongitude() + "," + start.getLatitude() + ";"
                        + end.getLongitude() + "," + end.getLatitude()
                        + "?overview=full&geometries=geojson";

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject json = new JSONObject(sb.toString());
                JSONArray routes = json.optJSONArray("routes");
                if (routes == null || routes.length() == 0) {
                    Log.e("OSM_ROUTE", "No route found");
                    runOnUiThread(() -> Toast.makeText(this, "Không tìm thấy tuyến đường.", Toast.LENGTH_SHORT).show());
                    return;
                }

                JSONObject geometry = routes.getJSONObject(0).getJSONObject("geometry");
                JSONArray coords = geometry.getJSONArray("coordinates");

                Polyline polyline = new Polyline();
                for (int i = 0; i < coords.length(); i++) {
                    JSONArray c = coords.getJSONArray(i);
                    double lon = c.getDouble(0);
                    double lat = c.getDouble(1);
                    polyline.addPoint(new GeoPoint(lat, lon));
                }
                polyline.setColor(Color.BLUE);
                polyline.setWidth(6f);

                runOnUiThread(() -> {
                    if (routeLine != null) mapView.getOverlays().remove(routeLine);
                    routeLine = polyline;
                    mapView.getOverlays().add(routeLine);
                    mapView.invalidate();
                });
            } catch (Exception e) {
                Log.e("OSM_ROUTE", "Error drawing route: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi vẽ tuyến: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    /** Xin quyền runtime nếu chưa có */
    private void requestPermissionsIfNecessary(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS_REQUEST_CODE);
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (locationOverlay != null) locationOverlay.enableMyLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationOverlay != null) locationOverlay.disableMyLocation();
        mapView.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] perms, @NonNull int[] res) {
        super.onRequestPermissionsResult(requestCode, perms, res);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            boolean granted = true;
            for (int r : res) if (r != PackageManager.PERMISSION_GRANTED) granted = false;
            if (granted && locationOverlay != null) {
                locationOverlay.enableMyLocation();
                locationOverlay.enableFollowLocation();
                pollForLocationAndDraw(15);
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền Vị trí để vẽ đường đi.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
