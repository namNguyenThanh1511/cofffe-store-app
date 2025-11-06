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

    // ====== CONFIG ======
    // Toạ độ quán cà phê cố định (A) — đổi ở đây khi cần
    private static final double COFFEE_LAT = 10.84142;
    private static final double COFFEE_LON = 106.81004;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    // ====== STATE ======
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private Marker coffeeMarker;
    private Polyline routeLine;

    private final Handler handler = new Handler(Looper.getMainLooper());

    // Auto-update route (poll mỗi 2s)
    private static final long AUTO_UPDATE_INTERVAL_MS = 2000L;
    private static final double REDRAW_MIN_DIST_M = 10.0; // đổi route nếu khác >10m
    private GeoPoint lastDrawnStart = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bắt buộc cho osmdroid (user agent & prefs)
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

        // Quyền vị trí
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        // Marker quán (A)
        showCoffeeMarker();

        // Provider vị trí: dùng cả GPS + NETWORK (emulator/indoor nhanh hơn)
        GpsMyLocationProvider provider = new GpsMyLocationProvider(this);
        provider.addLocationSource(android.location.LocationManager.GPS_PROVIDER);
        provider.addLocationSource(android.location.LocationManager.NETWORK_PROVIDER);

        // Overlay chấm xanh (B)
        locationOverlay = new MyLocationNewOverlay(provider, mapView);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        mapView.getOverlays().add(locationOverlay);

        // Đợi vị trí lần đầu để vẽ route (tránh vẽ sai về Mỹ)
        pollForFirstFix(15);

        // Lắng nghe vị trí: vẽ lại ngay khi có callback từ provider (máy thật)
        provider.startLocationProvider(new IMyLocationConsumer() {
            @Override
            public void onLocationChanged(android.location.Location loc, IMyLocationProvider src) {
                if (loc == null) return;
                GeoPoint current = new GeoPoint(loc.getLatitude(), loc.getLongitude());
                GeoPoint coffee  = new GeoPoint(COFFEE_LAT, COFFEE_LON);

                // Hủy poll cũ để tránh vẽ chồng
                handler.removeCallbacksAndMessages(null);
                runOnUiThread(() -> drawRoute(current, coffee));
                // lưu lại điểm đã vẽ để auto-update không vẽ dư thừa
                lastDrawnStart = current;

                // Khởi động lại auto-update sau khi vẽ bằng callback
                startAutoUpdateRoute();
            }
        });

        // Với Emulator: đôi khi không phát sinh callback → bật auto-update định kỳ
        startAutoUpdateRoute();
    }

    /** Poll mỗi 1s để đợi vị trí đầu tiên; hết thời gian thì chỉ cảnh báo (không vẽ sai) */
    private void pollForFirstFix(int attempts) {
        handler.postDelayed(() -> {
            GeoPoint my = (locationOverlay != null) ? locationOverlay.getMyLocation() : null;
            if (my != null) {
                drawRoute(my, new GeoPoint(COFFEE_LAT, COFFEE_LON));
                lastDrawnStart = my;
            } else if (attempts > 0) {
                pollForFirstFix(attempts - 1);
            } else {
                Toast.makeText(
                        this,
                        "Chưa lấy được GPS. Bật Location hoặc đặt tọa độ trong Emulator (… → Location → Set Location).",
                        Toast.LENGTH_LONG
                ).show();
            }
        }, 1000);
    }

    // ====== AUTO UPDATE ROUTE (cho emulator và cả máy thật) ======
    private final Runnable autoUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (locationOverlay != null && locationOverlay.getMyLocation() != null) {
                    GeoPoint current = locationOverlay.getMyLocation();
                    if (shouldRedraw(current)) {
                        drawRoute(current, new GeoPoint(COFFEE_LAT, COFFEE_LON));
                        lastDrawnStart = current;
                    }
                }
            } finally {
                // lặp lại
                handler.postDelayed(this, AUTO_UPDATE_INTERVAL_MS);
            }
        }
    };

    private void startAutoUpdateRoute() {
        handler.removeCallbacks(autoUpdateRunnable);
        handler.postDelayed(autoUpdateRunnable, AUTO_UPDATE_INTERVAL_MS);
    }

    private boolean shouldRedraw(GeoPoint current) {
        if (lastDrawnStart == null) return true;
        return current.distanceToAsDouble(lastDrawnStart) > REDRAW_MIN_DIST_M;
    }
    // ============================================================

    /** Marker quán A + focus camera về quán (zoom ban đầu) */
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
                // OSRM: THỨ TỰ LON,LAT (kinh độ trước, vĩ độ sau)
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
                    runOnUiThread(() ->
                            Toast.makeText(this, "Không tìm thấy tuyến đường.", Toast.LENGTH_SHORT).show()
                    );
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
                    // gỡ route cũ (nếu có) rồi mới add mới
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

    /** Quyền runtime */
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
        startAutoUpdateRoute(); // đảm bảo auto-update lại khi quay về
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationOverlay != null) locationOverlay.disableMyLocation();
        mapView.onPause();
        handler.removeCallbacksAndMessages(null); // tránh rò rỉ & vẽ khi activity không hiển thị
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
                pollForFirstFix(15);
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền Vị trí để vẽ đường đi.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
